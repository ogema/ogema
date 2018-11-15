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
package org.ogema.core.rads.tools;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.ogema.core.resourcemanager.pattern.ContextSensitivePattern;

public class ContainerTool {

	public static <C> void setContainer(final ContextSensitivePattern<?, C> pattern, final C container) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, RuntimeException {
        try {
			AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
	
	            @Override
	            public Void run() throws Exception {  
	            	final Field field = ContextSensitivePattern.class.getDeclaredField("context");
	            	field.setAccessible(true);
	                try {
						field.set(pattern, container);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new RuntimeException("Container set method failed. " + e);
					}
	                field.setAccessible(false);
	                return null;
	            }
	        });
        } catch (PrivilegedActionException e) {
        	final Throwable cause = e.getCause();
        	if (cause instanceof NoSuchFieldException)
        		throw (NoSuchFieldException) cause;
        	if (cause instanceof RuntimeException)
        		throw (RuntimeException) cause;
        	throw new RuntimeException(cause);
        }
	}
}
