/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ogema.rest.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.accesscontrol.RestAccess;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.ReductionMode;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.recordeddata.DataRecorder;
import org.ogema.recordeddata.RecordedDataStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
//@Component
//@Service(Servlet.class)
//@Property(name = HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, value = RecordedDataServlet.ALIAS)
class RecordedDataServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public final static String PARAM_START = "start";
    public final static String PARAM_END = "end";
    public final static String PARAM_INTERVAL = "interval";
    public final static String PARAM_MODE = "mode";

    static final String ALIAS = "/rest/recordeddata";

    //final Pattern endsWithNumber = Pattern.compile("(?:/?\\D.*)(?:/(\\d+))");
    final Pattern endsWithNumber = Pattern.compile("(?:/?\\D.*)(?:/(\\d[^/]+))");

    final Logger logger = LoggerFactory.getLogger(getClass());

    private final DataRecorder rda;
	private final RestAccess restAcc;
	
	RecordedDataServlet(RestAccess restAcc, DataRecorder rda) {
		this.restAcc = Objects.requireNonNull(restAcc);
		this.rda = Objects.requireNonNull(rda);
	}
	

    // two alternative ways to restrict the time interval: either append "/" + timestamp; OR use request
    // parameters: ?start=<START_TIME>&end=<END_TIME>
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	final ApplicationManager appman = restAcc.authenticate(req, resp);
        if (appman == null) {
            return;
        }
        final String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
        	resp.setContentType("text/plain");
        	resp.setCharacterEncoding("UTF-8");
        	outputRecordedDataIDs(resp.getWriter(), appman.getResourceAccess());
        	resp.setStatus(HttpServletResponse.SC_OK);
        	return;
        }
        String id = pathInfo.substring(1);
        RecordedDataStorage rds = rda.getRecordedDataStorage(id);
        if (rds == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "no such recorded data series: " + id);
            return;
        }
        final Resource res;
        try {
        	res = appman.getResourceAccess().getResource(id);
        } catch (SecurityException e) {
        	resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        	return;
        }
        if (res == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "no such resource: " + id);
            return;
        }
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

        if (Utils.xmlOrJson(req)) {
        	resp.setContentType(Utils.XML);
            appman.getSerializationManager().writeXml(resp.getWriter(), res, rds, start, end, interval, mode);
        } else {
        	resp.setContentType(Utils.JSON);
            appman.getSerializationManager().writeJson(resp.getWriter(), res, rds, start, end, interval, mode);
        }
        resp.setStatus(HttpServletResponse.SC_OK);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	final ApplicationManager appman = restAcc.authenticate(req, resp);
    	if (appman == null)
            return;
        final String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
        	resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        	return;
        }
        final String content;
        try (final BufferedReader reader = req.getReader()) {
        	final StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) > 0) {
            	sb.append(buffer, 0 , read);
            	if (sb.length() > 1024) {
            		resp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            		return;
            	}
            }
            content = sb.toString();
        }
        final JSONObject json;
        try {
        	json = new JSONObject(content);
        } catch (JSONException e) {
        	resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON content");
        	return;
        }
        final StorageType storageType = json.has("storageType") ? StorageType.valueOf(json.getString("storageType").toUpperCase()) : StorageType.ON_VALUE_UPDATE;
        final long fixedInterval = storageType != StorageType.FIXED_INTERVAL ? 0 : json.has("fixedInterval") ? json.getLong("fixedInterval") : 300000; // default: 5 minutes
        
        String id = pathInfo.substring(1);
        final Resource res;
        try {
        	res = appman.getResourceAccess().getResource(id); // TODO check for write access?
        } catch (SecurityException e) {
        	resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        	return;
        }
        if (!(res instanceof SingleValueResource) || res instanceof StringResource) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No such single value resource: " + id);
            return;
        }
        final RecordedData rd = getHistoricalData(res);
        if (rd == null) {
        	resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Historical data not found");
        	return;
        }
        RecordedDataConfiguration config = rd.getConfiguration();
        if (config == null)
        	config = new RecordedDataConfiguration();
        config.setStorageType(storageType);
        config.setFixedInterval(fixedInterval);
        rd.setConfiguration(config);
        resp.setStatus(HttpServletResponse.SC_OK);
        logger.info("Logging enabled for resource {} via REST interface. Configuration: {}", pathInfo, config);
    }
    
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	final ApplicationManager appman = restAcc.authenticate(req, resp);
    	if (appman == null)
            return;
        final String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
        	resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        	return;
        }
        String id = pathInfo.substring(1);
        final Resource res;
        try {
        	res = appman.getResourceAccess().getResource(id);
        } catch (SecurityException e) {
        	resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        	return;
        }
        if (res == null || !(res instanceof SingleValueResource) || res instanceof StringResource) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "no such resource: " + id);
            return;
        }
        final RecordedData rd = getHistoricalData(res);
        if (rd == null) {
        	resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Historical data not found");
        	return;
        }
        rd.setConfiguration(null);
        resp.setStatus(HttpServletResponse.SC_OK);
        logger.info("Logging disabled for resource {} via REST interface.", pathInfo);
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

    protected void outputRecordedDataIDs(PrintWriter writer, ResourceAccess ra) throws ServletException, IOException {
        for (String id : rda.getAllRecordedDataStorageIDs()) {
        	try {
        		final Resource r = ra.getResource(id);
        		if (r == null)
        			continue;
        	} catch (SecurityException expected) {
        		continue;
        	}
            writer.println(id);
        }
    }
    
    private static RecordedData getHistoricalData(Resource resource) throws IllegalArgumentException {
		RecordedData rd = null;
		if (resource instanceof FloatResource)
			rd = ((FloatResource) resource).getHistoricalData();
		else if (resource instanceof IntegerResource)
			rd = ((IntegerResource) resource).getHistoricalData();
		else if (resource instanceof TimeResource)
			rd = ((TimeResource) resource).getHistoricalData();
		else if (resource instanceof BooleanResource)
			rd = ((BooleanResource) resource).getHistoricalData();
		else if (resource instanceof StringResource)
			throw new IllegalArgumentException("Logging for StringResources not possible");
		return rd;
	}

}
