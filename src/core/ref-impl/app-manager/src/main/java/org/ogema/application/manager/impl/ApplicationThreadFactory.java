/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.ogema.core.application.Application;

/**
 * Creates threads for {@link ApplicationManagerImpl}.
 * 
 * @author jlapp
 */
/*
 * TODO: review security context stuff once that is implemented.
 */
class ApplicationThreadFactory implements ThreadFactory {

	protected final Application application;

	protected static ThreadGroup group = new ThreadGroup("ogema.applications");

	private volatile Thread lastThread;

	private AtomicInteger threadCount = new AtomicInteger(0);

	/**
	 * @return the last thread created by this factory or null if no thread has been created yet.
	 */
	public Thread getLastThread() {
		return lastThread;
	}

	public ApplicationThreadFactory(Application application) {
		this.application = application;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(group, r);
		t.setName("App '" + application.getClass().getName() + "' (" + threadCount.incrementAndGet() + ")");
		lastThread = t;
		return t;
	}

}
