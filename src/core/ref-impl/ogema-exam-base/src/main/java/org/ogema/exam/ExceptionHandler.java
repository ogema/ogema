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
package org.ogema.exam;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.ExceptionListener;

/**
 * Can be used to transfer AssertionErrors in an application thread
 * to the test thread, so that the test fails in case of such 
 * errors.<br>
 * 
 * Usage: create an instance, register it at the beginning of the test (or the doBefore method) 
 * via {@link ApplicationManager#addExceptionListener(ExceptionListener)},
 * and call {@link #checkForExceptionsInOtherThreads()} at the end of the 
 * test. 
 *  
 * @author cnoelle
 */
public class ExceptionHandler implements ExceptionListener {

	private volatile AssertionError exception = null;
	
	@Override
	public void exceptionOccured(Throwable exception) {
		exception.printStackTrace();
		if (this.exception == null && exception instanceof AssertionError)  // keep the first exception, dismiss further ones
			this.exception = (AssertionError) exception;
	}
	
	public void reset() {
		this.exception = null;
	}

	public void checkForExceptionsInOtherThreads() throws AssertionError {
		if (exception != null) {
			System.out.println("Exception occured in other thread");
			throw exception;
		}
	}
	
}
