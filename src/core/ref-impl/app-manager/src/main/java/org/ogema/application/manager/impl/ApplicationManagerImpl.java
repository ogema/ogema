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
package org.ogema.application.manager.impl;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.administration.FrameworkClock;
import org.ogema.core.administration.RegisteredTimer;
import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.ExceptionListener;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.ogema.core.installationmanager.InstallationManagement;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.rads.impl.AdvancedAccessImpl;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.core.security.WebAccessManager;
import org.ogema.core.tools.SerializationManager;
import org.ogema.resourcemanager.impl.ApplicationResourceManager;
import org.ogema.timer.TimerScheduler;
import org.ogema.tools.impl.SerializationManagerImpl;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationManagerImpl implements ApplicationManager {

	private static final long serialVersionUID = 11534545646813546L;
	final TimerScheduler scheduler;
	final List<Timer> timers;
	private final FrameworkClock clock;

	protected final ExecutorService executor;
	private final Queue<Future<?>> workQueue;
	private static final int WORKQUEUE_FORCE_DRAIN_SIZE = 50;
	private final Callable<Void> drainWorkQueueTask;
	private final ApplicationThreadFactory tfac;

	private final Application application;
	private ApplicationTracker tracker;
	protected final Logger logger;
	final ApplicationResourceManager resMan;
	private ResourcePatternAccess advAcc;
	private final AppID appID;
	private final List<ExceptionListener> exceptionListeners = new ArrayList<>();

	BundleContext bContext;

	public ApplicationManagerImpl(Application app, ApplicationTracker tracker, AppID id) {
		this.drainWorkQueueTask = new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				drainWorkQueue();
				return null;
			}
		};
		this.tracker = Objects.requireNonNull(tracker);
		this.scheduler = tracker.getTimerScheduler();
		this.clock = tracker.getClock();
		this.application = Objects.requireNonNull(app);
		this.appID = id;
		workQueue = new ConcurrentLinkedQueue<>();
		this.executor = Executors.newSingleThreadExecutor(tfac = new ApplicationThreadFactory(application));
		logger = LoggerFactory.getLogger("org.ogema.core.application-" + app.getClass().getName());
		this.resMan = new ApplicationResourceManager(this, app, tracker.getResourceDBManager(),
				tracker.getPermissionManager());
		timers = new LinkedList<>();

		this.bContext = id.getBundle().getBundleContext();
	}

	// constructor used only for timer tests
	protected ApplicationManagerImpl(Application app, TimerScheduler sched, FrameworkClock clock) {
		this.drainWorkQueueTask = new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				drainWorkQueue();
				return null;
			}
		};
		this.application = app;
		this.scheduler = sched;
		this.clock = clock;
		workQueue = new ConcurrentLinkedQueue<>();
		this.executor = Executors.newSingleThreadExecutor(tfac = new ApplicationThreadFactory(application));
		logger = LoggerFactory.getLogger("AppMan." + app.getClass().getName());
		resMan = null;
		this.appID = AppIDImpl.getNewID(app);
		timers = new LinkedList<>();
	}

	@Override
	public void shutdown() {
		tracker.removeApplication(application);
	}

	protected void startApplication() {
		Callable<Boolean> callStart = new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				try {
					application.start(ApplicationManagerImpl.this);
				} catch (Throwable t) {
					logger.error("app {} failed to start: ", application, t);
					close();
					throw t;
				}
				return true;
			}

		};
		submitEvent(callStart);
	}

	protected boolean stopApplication() {
		boolean stopped = false;
		if (isApplicationThread()) {
			application.stop(Application.AppStopReason.APP_STOP);
			stopped = true;
		}
		else {
			Callable<Boolean> callStop = new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
					// XXX actually, i have no idea why it is stopped...
					application.stop(Application.AppStopReason.APP_STOP);
					return true;
				}

			};

			try {
				// TODO consider removing all other callbacks in queue before
				// submitting this.
				// FIXME the timeout does not seem to work...
				final Future<Boolean> stopEvent = submitEvent(callStop);
				if (stopEvent != null) {
					stopped = stopEvent.get(30, TimeUnit.SECONDS);
				}
				else {
					stopped = executor.isShutdown(); // if event was rejected, assume stopped from executor status.
				}
			} catch (InterruptedException ex) {
				logger.warn("stop() call of application " + application.getClass().getName() + " interrupted.", ex);
			} catch (ExecutionException ex) {
				logger.warn("stop() call of application " + application.getClass().getName() + " failed.", ex);
			} catch (TimeoutException te) {
				logger.warn("stop() call of application " + application.getClass().getName() + " timed out.");
				if (tfac.getLastThread() != null) {
					List<StackTraceElement> l = Arrays.asList(tfac.getLastThread().getStackTrace());
					logger.debug("application '%s' stop() call timed out, application thread stack trace: %s",
							application.getClass().getName(), l);
				}
			}
		}
		if (stopped) {
			exceptionListeners.clear();
		}
		return stopped;
	}

	/**
	 * @return the resManager
	 */
	@Override
	public ResourceManagement getResourceManagement() {
		return resMan;
	}

	@Override
	public ResourceAccess getResourceAccess() {
		return resMan;
	}

	@Override
	public Timer createTimer(long period) {
		Timer t = scheduler.createTimer(executor, getLogger());
		t.setTimingInterval(period);
		synchronized (timers) {
			timers.add(t);
		}
		return t;
	}

	@Override
	public Timer createTimer(long millies, TimerListener listener) {
		Timer timer = createTimer(millies);
		timer.addListener(listener);
		return timer;
	}

	@Override
	public void destroyTimer(Timer t) {
		t.destroy();
	}

	@Override
	public InstallationManagement getInstallationManagement() {
		// TODO Auto-generated method stub
		return null;
	}

	public ChannelAccess getChannelDriverManager() {
		return tracker.getChannelAccess();
	}

	@Override
	public HardwareManager getHardwareManager() {
		return tracker.getHardwareManager();
	}

	@Override
	public void addExceptionListener(ExceptionListener listener) {
		if (exceptionListeners.contains(listener)) {
			logger.debug(
					"Application tried to add an already-registered exception listener a second time. Request has been ignored.");
		}
		else {
			exceptionListeners.add(listener);
		}
	}

	@Override
	public WebAccessManager getWebAccessManager() {
		return tracker.getWebAccessManager(appID);
	}

	@Override
	public ResourcePatternAccess getResourcePatternAccess() {
		if (advAcc == null) {
			advAcc = new AdvancedAccessImpl(this);
		}
		return advAcc;
	}

	@Override
	public AdministrationManager getAdministrationManager() {
		return tracker.administration;
	}

	@Override
	public ChannelAccess getChannelAccess() {
		return tracker.channelAccess;
	}

	@Override
	public OgemaLogger getLogger() {
		return (OgemaLogger) LoggerFactory.getLogger(application.getClass().getName());
	}

	@Override
	public long getFrameworkTime() {
		return clock.getExecutionTime();
	}

	/**
	 * shutdown timers and executor, calling stop() on the application is done by {@link ApplicationTracker}
	 */
	protected void close() {
		synchronized (timers) {
			for (Iterator<Timer> it = timers.iterator(); it.hasNext();) {
				it.next().destroy();
				it.remove();
			}
		}
		executor.shutdown();
		resMan.close();
		advAcc = null;
		logger.debug("shut down application manager for app '{}'", appID.getIDString());
	}

	@Override
	public <T> Future<T> submitEvent(Callable<T> application) {
		if (executor.isShutdown()) {
			return null;
		}
		Future<T> f = executor.submit(application);
		workQueue.add(f);
		if (workQueue.size() > WORKQUEUE_FORCE_DRAIN_SIZE) {
			if (workQueue.peek().isDone()) {
				executor.submit(drainWorkQueueTask);
			}
		}
		return f;
	}

	/**
	 * Removes completed futures from the workqueue and logs all exceptions as warnings.
	 */
	protected void drainWorkQueue() {
		int done = 0;
		while (!workQueue.isEmpty() && workQueue.peek().isDone()) {
			Future<?> f = workQueue.remove();
			try {
				f.get();
				done++;
			} catch (ExecutionException ee) {
				reportException(ee.getCause());
			} catch (InterruptedException ie) {
				// after isDone() == true?!?
				getLogger().error("really unexpected exception in ApplicationManagerImpl.drainWorkQueue(), review code",
						ie);
			}
		}
		// System.out.printf("%d jobs done, %d in queue%n", done, workQueue.size());
	}

	@Override
	public SerializationManager getSerializationManager() {
		if (System.getSecurityManager() == null) {
			return new SerializationManagerImpl(getResourceAccess(), getResourceManagement());
		}
		else {
			return AccessController.doPrivileged(new PrivilegedAction<SerializationManager>() {

				@Override
				public SerializationManager run() {
					return new SerializationManagerImpl(getResourceAccess(), getResourceManagement());
				}
			});
		}
	}

	/**
	 * @return true iff the current thread is this application's thread.
	 */
	public boolean isApplicationThread() {
		return Thread.currentThread() == tfac.getLastThread();
	}

	@Override
	public AppID getAppID() {
		return appID;
	}

	@Override
	public File getDataFile(String filename) {
		return this.bContext.getDataFile(filename);
	}

	@Override
	public SerializationManager getSerializationManager(int maxDepth, boolean followReferences,
			boolean writeSchedules) {
		final SerializationManagerImpl result = new SerializationManagerImpl(getResourceAccess(),
				getResourceManagement());
		result.setMaxDepth(maxDepth);
		result.setFollowReferences(followReferences);
		result.setSerializeSchedules(writeSchedules);
		return result;
	}

	@Override
	public void reportException(Throwable exception) {

		if (exceptionListeners.isEmpty()) {
			if (exception instanceof RuntimeException) {
				throw (RuntimeException) exception;
			}
			else {
				getLogger().error(
						"Non-runtime exception was reported but no ExceptionListener was registered to handle it",
						exception);
			}
		}
		for (ExceptionListener listener : exceptionListeners) {
			listener.exceptionOccured(exception);
		}

	}

	protected List<RegisteredTimer> getTimers() {
		synchronized (timers) {
			return DefaultRegisteredTimer.asRegisteredTimers(this, timers);
		}
	}

}
