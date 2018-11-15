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
package org.ogema.resourcemanager.impl;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.ogema.core.model.Resource;
import org.ogema.resourcemanager.virtual.VirtualTreeElement;

/**
 * 
 * @author jlapp
 */
public class DynamicProxyResource extends ResourceBase implements InvocationHandler, Serializable {

	private static final long serialVersionUID = 1434574137743006313L;

	public DynamicProxyResource(VirtualTreeElement el, String path, ApplicationResourceManager resMan) {
		super(el, path, resMan);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Class<?> declaringType = method.getDeclaringClass();
		try {
			if (declaringType.equals(Resource.class)) {
				return method.invoke(this, args);
			}

			if (declaringType.equals(Object.class)) {
				if (method.getName().equals("toString")) {
					return (this.path == null) ? getEl().getName() : this.path + "::" + getEl().getName();
				}
				return method.invoke(this, args);
			}

			if (declaringType.equals(ConnectedResource.class)) {
				return method.invoke(this, args);
			}

			if (Resource.class.isAssignableFrom(method.getReturnType())) {
				return getSubResource(method.getName());
			}
			// method is not valid for an OGEMA resource
			throw new UnsupportedOperationException("cannot handle method on dynamic proxy: "
					+ method.getDeclaringClass().getCanonicalName() + "." + method.getName() + "(...)");
		} catch (InvocationTargetException ex) {
//			resMan.getApplicationManager().reportException(ex.getCause());
			throw ex.getCause();
		}
	}

}
