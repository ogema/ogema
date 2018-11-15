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
