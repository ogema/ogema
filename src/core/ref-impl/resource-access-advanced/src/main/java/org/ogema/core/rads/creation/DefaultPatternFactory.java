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
package org.ogema.core.rads.creation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;

public class DefaultPatternFactory<P extends ResourcePattern<?>> implements PatternFactory<P> {

	private final Constructor<P> m_constructor;

	public DefaultPatternFactory(final Class<P> type) {
		try {
			m_constructor = AccessController.doPrivileged(new PrivilegedExceptionAction<Constructor<P>>() {

				@Override
				public Constructor<P> run() throws Exception {
					 final Constructor<P> constructor = type.getConstructor(Resource.class);
			         constructor.setAccessible(true);
			         return constructor;
				}
			});
		} catch (PrivilegedActionException e) {
            throw new RuntimeException("Could not find default constructor on RAD of type " + type.getCanonicalName() 
            	+ ". Ensure that the RAD has a public constructur RAD(Resource resource). Otherwise, it cannot be used with the OGEMA advanced access.", e.getCause());
		}
	}

	@Override
	public P createNewPattern(final Resource baseResource) {
		P result;
		try {
            result = m_constructor.newInstance(baseResource);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException("could not create a RAD object of type " + m_constructor.getDeclaringClass() + " for resource " + baseResource, ex);
        }
		return result;
	}
}
