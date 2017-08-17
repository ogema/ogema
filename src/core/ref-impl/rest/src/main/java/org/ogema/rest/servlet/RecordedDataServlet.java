/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.rest.servlet;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.recordeddata.ReductionMode;
import org.ogema.recordeddata.DataRecorder;
import org.ogema.recordeddata.RecordedDataStorage;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
@Component
@Service(Application.class)
public class RecordedDataServlet extends HttpServlet implements Application {

    private static final long serialVersionUID = 1L;
    final boolean SECURITY_ENABLED = "on".equalsIgnoreCase(System.getProperty("org.ogema.security", "off"));

    public final static String PARAM_START = "start";
    public final static String PARAM_END = "end";
    public final static String PARAM_INTERVAL = "interval";
    public final static String PARAM_MODE = "mode";

    final String ALIAS = "/rest/recordeddata";

    //final Pattern endsWithNumber = Pattern.compile("(?:/?\\D.*)(?:/(\\d+))");
    final Pattern endsWithNumber = Pattern.compile("(?:/?\\D.*)(?:/(\\d[^/]+))");

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    HttpService http;

    @Reference
    DataRecorder rda;
    ApplicationManager app;

    @Reference
    private PermissionManager permMan;
    @Reference
    private AdministrationManager adminMan;
    private RestAccess restAcc;

    @Override
    public void start(ApplicationManager appManager) {
        try {
            restAcc = new RestAccess(permMan, adminMan);
            http.registerServlet(ALIAS, this, null, null);
        } catch (ServletException | NamespaceException ex) {
            throw new RuntimeException(ex);
        }
        app = appManager;
    }

    @Override
    public void stop(AppStopReason reason) {
        http.unregister(ALIAS);
        app = null;
        restAcc = null;
    }

    // two alternative ways to restrict the time interval: either append "/" + timestamp; OR use request
    // parameters: ?start=<START_TIME>&end=<END_TIME>
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.isEmpty()) {
            if (!restAcc.setAccessContext(req, resp, SECURITY_ENABLED)) {
                return;
            }

            String id = pathInfo.substring(1);
            long start = 0;
            long end = Long.MAX_VALUE;
            Matcher m1 = endsWithNumber.matcher(id);
            if (m1.matches()) {
                id = id.substring(0, id.length() - m1.group(1).length() - 1);
                Matcher m2 = endsWithNumber.matcher(id);
                if (m2.matches()) {
                    start = parseTimestamp(m2.group(1));
                    end = parseTimestamp(m1.group(1));
                    id = id.substring(0, id.length() - m2.group(1).length() - 1);
                } else {
                    start = parseTimestamp(m1.group(1));
                }
            }

            String startTimestamp = req.getParameter(PARAM_START);
            if (startTimestamp != null) 
                start = parseTimestamp(startTimestamp);
            String endTimestamp = req.getParameter(PARAM_END);
            if (endTimestamp != null)
                end = parseTimestamp(endTimestamp);

            RecordedDataStorage rds = rda.getRecordedDataStorage(id);
            Resource res = app.getResourceAccess().getResource(id);
            if (rds == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "no such recorded data series: " + id);
                return;
            }
            if (res == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "no such resource: " + id);
                return;
            }

            long interval = rds.getConfiguration().getFixedInterval();
            ReductionMode mode = ReductionMode.NONE;

            String pInterval = req.getParameter(PARAM_INTERVAL);
            String pMode = req.getParameter(PARAM_MODE);

            if (pInterval != null) {
                try {
                    interval = Long.parseLong(pInterval);
                } catch (NumberFormatException nfe) {
                    String error = String.format("illegal value for parameter '%s': %s", PARAM_INTERVAL, pInterval);
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
                    return;
                }
            }

            if (pMode != null) {
                try {
                    mode = ReductionMode.valueOf(pMode);
                } catch (IllegalArgumentException ex) {
                    String error = String.format("illegal value for parameter '%s': %s", PARAM_MODE, pMode);
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
                    return;
                }
            }

            logger.info("return RecordedData '{}', {}, {}, {}, {}", id, start, end, interval, mode);

            String accept = req.getHeader("Accept");
            int returnXML = accept.indexOf("application/xml");
            int returnJSON = accept.indexOf("application/json");

            if (returnXML != -1 && (returnJSON == -1 || returnXML < returnJSON)) {
                app.getSerializationManager().writeXml(resp.getWriter(), res, rds, start, end, interval, mode);
            } else {
                app.getSerializationManager().writeJson(resp.getWriter(), res, rds, start, end, interval, mode);
            }
            resp.getWriter().flush();
            resp.flushBuffer();
        } else {
            outputRecordedDataIDs(req, resp);
        }
    }
    
    protected static long parseTimestamp(String ts) {
        long rval;
        try {
            rval = Long.parseLong(ts);
        } catch (NumberFormatException nfe) {
        	// no need to introduce joda just for this purpose...
//            rval = org.joda.time.DateTime.parse(ts).getMillis();
            rval = parseDate(ts);
        }
        return rval;
    }
    
    /**
     * Parses strings of the form
     * <ul>
     * 	<li>2017-08-07
     *  <li>2017-08-07T01
     *  <li>2017-08-07T01:14
     *  <li>2017-08-07T01:14:02
     *  <li>2017-08-07T01:14:02.234
     * </ul>
     * Furthermore, a time zone may be added in the format "+0200" or "-0300" (note that '+' must be escaped by "%2B" in an URL), 
     * or "Z" for UTC
     * @param ts
     * @return
     */
    private static long parseDate(String ts) {
    	Calendar cal = Calendar.getInstance();
    	final Date date;
        ts = ts.replace("Z", "+0000");
        final boolean containsTimezone = (ts.contains("-") && ts.lastIndexOf('-') > 7) || ts.contains("+");
        final int length;
        if (containsTimezone) {
        	final int idx = ts.contains("+") ? ts.indexOf('+') : ts.lastIndexOf('-');
        	length = idx;
        } else
        	length = ts.length();
    	try {
			date = getDateFormat(length,containsTimezone).parse(ts);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Date not parseable: " + ts);
		}
		cal.setTime(date);
		return cal.getTimeInMillis();
    }
    
    private static SimpleDateFormat getDateFormat(int length, boolean containsTimezone) {
    	final StringBuilder sb= new StringBuilder();
    	sb.append("yyyy-MM-dd");
    	if (length > 10) {
    		sb.append("'T'HH");
    		if (length > 13) {
    			sb.append(":mm");
    			if (length > 16) {
    				sb.append(":ss");
    				if (length > 19) {
    					sb.append(".SSS");
    				}
    			}
    		}
    	}
    	if (containsTimezone)
    		sb.append('Z');
    	return new SimpleDateFormat(sb.toString(), Locale.ENGLISH);
    }

    protected void outputRecordedDataIDs(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        for (String id : rda.getAllRecordedDataStorageIDs()) {
            resp.getWriter().println(id);
        }
    }

}
