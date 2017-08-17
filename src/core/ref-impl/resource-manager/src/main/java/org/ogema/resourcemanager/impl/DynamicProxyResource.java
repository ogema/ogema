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
