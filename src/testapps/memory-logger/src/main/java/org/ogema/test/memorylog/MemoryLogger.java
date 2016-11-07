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
package org.ogema.test.memorylog;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.json.JSONObject;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;

/**
 * Simple application that periodically writes information about the system's
 * current memory usage to the logger. Intended to be used in long-term tests of
 * a system. A simple GUI is included.<br>
 * Default logging interval is 5 minutes, it can be set via the system property
 * "org.ogema.test.memorylog.interval"
 * (in ms).
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
final public class MemoryLogger extends HttpServlet implements Application {

	private static final long serialVersionUID = 1L;
	private static final long DEFAULT_UPDATE_INTERVAL = 5 * 60 * 1000;  // 5 min
	private static final String UPDATE_INTERVAL_PROPERTY = "org.ogema.test.memorylog.interval";
	private static final long UPDATE_INTERVAL = Long.getLong(UPDATE_INTERVAL_PROPERTY, DEFAULT_UPDATE_INTERVAL);
	private ApplicationManager appMan;
	private OgemaLogger logger;
	private long startTime;
	private Timer timer;
	private static final String webResourcePackagePath = "org/ogema/test/memory/log";
	private static final String webResourceBrowserPath = "/ogema/test/memorylog"; 
	private static final String servletPath = "/ogema/servlet/memorycheckapp";

	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
		this.logger = appManager.getLogger();
		this.startTime = appManager.getFrameworkTime();
		timer = appMan.createTimer(UPDATE_INTERVAL, statusWriter);
		appMan.getWebAccessManager().registerWebResource(webResourceBrowserPath, webResourcePackagePath);
		appMan.getWebAccessManager().registerWebResource(servletPath, this);
		logger.debug("{} started, update interval {}s", getClass().getName(), (UPDATE_INTERVAL / 1000));

	}

	@Override
	public void stop(AppStopReason reason) {
		try {
			appMan.getWebAccessManager().unregisterWebResource(webResourceBrowserPath);
			appMan.getWebAccessManager().unregisterWebResource(servletPath);
		} catch (Exception e) { /* ignore */ }
		if (timer != null)
			timer.destroy();
		logger.debug("{} stopped", getClass().getName());
		appMan = null;
		startTime = -1;
		logger = null;
		timer = null;
	}

	protected TimerListener statusWriter = new TimerListener() {

		@Override
		public void timerElapsed(Timer timer) {
			long currentInterval = timer.getTimingInterval();
			long newInterval = Long.getLong(UPDATE_INTERVAL_PROPERTY, currentInterval);
			if (newInterval != currentInterval) {
				timer.setTimingInterval(newInterval);
			}
			if (!logger.isDebugEnabled())
				return;
			final Runtime runtime = Runtime.getRuntime();
			runtime.gc();
			int mb = 1024*1024;
			long used = ( runtime.totalMemory() - runtime.freeMemory()) / mb;
			long free = runtime.freeMemory() / mb;
			long memTotal = runtime.totalMemory() / mb;
			long memMax = runtime.maxMemory() / mb;
			int numRes = appMan.getResourceAccess().getResources(Resource.class).size();
			int kbPerResource = (int) (runtime.totalMemory() - runtime.freeMemory()) / 1024/numRes;
			long cr = appMan.getFrameworkTime();
			long sFull = (cr - startTime)/1000;
			long s = sFull;
			long d = s/ 24/3600;
			s = s - d * 24*3600;
			long h = s/3600;
			s  = s- 3600*h;
			long m = s/60;
			s = s - m*60;
			String duration = String.valueOf(d) + "d " + String.valueOf(h) + "h " + String.valueOf(m) + "m " + String.valueOf(s) + "s";
			// Note: analysis tools exist which depend on the exact format of this logging output. Do not change.
			logger.debug(
					"Time = " + cr + "ms, running = " + sFull + "s (" + duration + "), used mem = " + used + "MB, free mem = " + free + "MB, total mem = " + memTotal + "MB, max mem = " + memMax 
						+ "MB, number of resources = " + numRes + ", memory per resource = " + kbPerResource + "kB");
		}
	};
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		JSONObject obj = new JSONObject();
		Runtime runtime = Runtime.getRuntime();
		int mb = 1024*1024;
		long used = ( runtime.totalMemory() - runtime.freeMemory()) / mb;
		obj.put("memUsed",used);
		obj.put("memFree",  runtime.freeMemory() / mb);
		obj.put("memTotal",  runtime.totalMemory() / mb);
		obj.put("memMax",  runtime.maxMemory() / mb);
		int numRes = appMan.getResourceAccess().getResources(Resource.class).size();
		obj.put("resNum",numRes);
		obj.put("kBytesPerResource", ( runtime.totalMemory() - runtime.freeMemory()) / 1024/numRes);
		long s = (appMan.getFrameworkTime() - startTime)/1000;
		long d = s/ 24/3600;
		s = s - d * 24*3600;
		long h = s/3600;
		s  = s- 3600*h;
		long m = s/60;
		s = s - m*60;
		String duration = String.valueOf(d) + "d " + String.valueOf(h) + "h " + String.valueOf(m) + "m " + String.valueOf(s) + "s";
		obj.put("appRunTime", duration);
		resp.getWriter().write(obj.toString());
		resp.setStatus(200);
	} 
	
}
