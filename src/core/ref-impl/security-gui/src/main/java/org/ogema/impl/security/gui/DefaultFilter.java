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
package org.ogema.impl.security.gui;

import java.io.File;
import java.io.FilePermission;
import java.lang.management.ManagementPermission;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ReflectPermission;
import java.net.NetPermission;
import java.net.SocketPermission;
import java.nio.file.LinkPermission;
import java.security.Permission;
import java.security.Permissions;
import java.security.SecurityPermission;
import java.util.Enumeration;
import java.util.List;
import java.util.PropertyPermission;

import javax.net.ssl.SSLPermission;

import org.ogema.accesscontrol.FilterValue;
import org.ogema.accesscontrol.Util;
import org.ogema.core.security.AppPermission;
import org.osgi.framework.AdaptPermission;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.PackagePermission;
import org.osgi.framework.ServicePermission;
import org.osgi.service.permissionadmin.PermissionInfo;
import org.slf4j.Logger;

/**
 * This class handles a list of permissions that should never be granted to user applications. The list of the
 * permission can be placed in the file ./security/forbiddens.perm in utf-8 format. If this file doesn't exist a default
 * set of filtered permissions are used.
 * 
 * @author Zekeriya Mansuroglu
 *
 */
public class DefaultFilter {
	private static final String ALL_FILES = "<<ALL FILES>>";
	/*
	 * All permissions that are implied by one of the permission defined in forbiddens must not be granted or if
	 * AllPermission is granted the forbiddens should be denied explicitly.
	 * 
	 */
	Permissions forbiddens;
	Permissions permitteds;
	private Logger logger;;

	public DefaultFilter(Logger logger) {
		this.logger = logger;
		init();
	}

	void init() {
		List<String> perms = SecurityGui.getForbiddens();
		boolean success = true;
		forbiddens = new Permissions();
		permitteds = new Permissions();
		Class<?>[] paramT = new Class<?>[2];
		paramT[0] = String.class;
		paramT[1] = String.class;
		Object[] paramV = new Object[2];
		for (String s : perms) {
			boolean exception = false;
			s = s.trim();
			if (s.startsWith("#") || s.startsWith("//") || (s.length() == 0)) {
				continue;
			}
			logger.debug("Add filter permission " + s);
			if (s.startsWith("!")) {
				s = s.substring(1).trim();
				exception = true;
			}
			try {
				PermissionInfo pi = new PermissionInfo(s);
				String type = pi.getType();
				Class<?> cls = Class.forName(type);
				Constructor<?> constr = cls.getConstructor(paramT);
				String name = pi.getName();
				String actions = pi.getActions();
				paramV[0] = name;
				paramV[1] = actions;
				Permission perm = (Permission) constr.newInstance(paramV);
				if (exception)
					permitteds.add(perm);
				else
					forbiddens.add(perm);
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
					| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				success = false;
				logger.warn("Filter file forbiddens.perm could not be prosecced. Fall back to default filter.");
				break;
			}
		}

		if (!success) {
			forbiddens = new Permissions();
			permitteds = new Permissions();
			forbiddens.add(new ServicePermission("*", "get,register")); // this permission covers following
			forbiddens.add(new ReflectPermission("*"));
			forbiddens.add(new AdminPermission());
			forbiddens.add(new FilePermission("<<ALL FILES>>", "read,write,execute,delete,readlink"));
			forbiddens.add(new ManagementPermission("control"));
			forbiddens.add(new RuntimePermission("*"));
			forbiddens.add(new NetPermission("*"));
			forbiddens.add(new SocketPermission("*", "connect,listen,accept"));
			forbiddens.add(new LinkPermission("hard"));
			forbiddens.add(new LinkPermission("symbolic"));
			forbiddens.add(new SecurityPermission("*"));
			forbiddens.add(new PropertyPermission("*", "read,write"));
			forbiddens.add(new SSLPermission("*"));
			forbiddens.add(new AdaptPermission("*", "ADAPT"));
			forbiddens.add(new PackagePermission("org.ogema.impl.*", "import,export"));
		}
	}

	@SuppressWarnings("unchecked")
	boolean filterPermission(String mode, String permname, String[] args, AppPermission ap) {
		if (mode.equals("deny"))
			return false;
		Class<? extends Permission> cls;
		Permission gPerm = null;
		try {
			cls = ((Class<? extends Permission>) Class.forName(permname));
			Constructor<?> cons = cls.getConstructor(String.class, String.class);
			gPerm = (Permission) cons.newInstance(args[0], args[1]);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		if (gPerm == null)
			return true;
		// Check first whether the permission is a subset of the exceptions. In this case the permission is not
		// filtered.
		if (permitteds.implies(gPerm))
			return false;
		Enumeration<Permission> elems = forbiddens.elements();
		while (elems.hasMoreElements()) {
			Permission fPerm = elems.nextElement();
			// differentiate between 4 different cases
			// 1. prohibition implies the grant => granted permission is fully filtered
			if (fPerm.implies(gPerm))
				return true;
			// 2. prohibition doesn't imply grant but grant implies prohibition => grant allowed, prohibition denied
			// explicitly
			if (gPerm.implies(fPerm)) {
				addException(ap, fPerm);
				return false;
			}
			// 3. grant and prohibition intersect in some way => grant allowed, prohibition denied explicitly
			boolean intersection = intersect(fPerm, gPerm);
			if (intersection) {
				addException(ap, fPerm);
				return false;
			}
		}
		// 4. grant and prohibition don't intersect in any way => nothing to do
		return false;
	}

	private void addException(AppPermission ap, Permission perm) {
		String[] args = new String[2];
		args[0] = perm.getName();
		args[1] = perm.getActions();
		ap.addException(perm.getClass().getName(), args, null);
	}

	void addDefaultExceptions(AppPermission ap) {
		String[] args = new String[2];
		Enumeration<Permission> elems = forbiddens.elements();
		for (; elems.hasMoreElements();) {
			Permission perm = elems.nextElement();
			args[0] = perm.getName();
			args[1] = perm.getActions();
			ap.addException(perm.getClass().getName(), args, null);
		}
	}

	boolean intersect(Permission filter, Permission grant) {
		String fType = filter.getClass().getName();
		String gType = grant.getClass().getName();

		if (!fType.equals(gType))
			return false;

		String fActions = filter.getActions();
		String gActions = grant.getActions();
		String[] fActArr = fActions.split(",");
		String[] gActArr = gActions.split(",");
		boolean result = Util.intersect(fActArr, gActArr);
		if (!result)
			return false;

		String fName = filter.getName();
		String gName = grant.getName();
		// FilePermission should be handled separately due to special permission name <<ALL FILES>>
		if (filter.getClass() == FilePermission.class)
			return intersectFileName(fName, gName);
		// Check if the permissions names have filter structure
		if ((fName.indexOf('=') != -1) && (gName.indexOf('=') != -1))
			return intersectFilterName(fName, gName);
		else if (fName.startsWith(gName) || gName.startsWith(fName))
			return true;
		else
			return false;
	}

	private boolean intersectFilterName(String fName, String gName) {
		// Split names in parts like 'key=value'
		String[] fParts = fName.split(",");
		String[] gParts = gName.split(",");
		int i = 0;
		while (true) {
			try {
				if (intersectPart(fParts[i++], gParts))
					return true;
			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}
		}
		return false;
	}

	private boolean intersectPart(String part, String[] gParts) {
		String[] fkeyValue = part.split("=");
		int i = 0;
		while (true) {
			String str;
			try {
				str = gParts[i++];
			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}
			String[] gkeyValue = str.split("=");
			if (gkeyValue[0].equals(fkeyValue[0])) {
				FilterValue fparam = new FilterValue();
				FilterValue gparam = new FilterValue();
				fparam.parse(gkeyValue[1]);
				gparam.parse(fkeyValue[1]);
				if (fparam.implies(gparam) || gparam.implies(fparam))
					return true;
			}
		}
		return false;
	}

	private boolean intersectFileName(String fName, String gName) {
		if (fName.equals(ALL_FILES) || gName.equals(ALL_FILES))
			return true;
		else if ((fName.equals("*") || fName.equals("-")) && (gName.equals("*") || gName.equals("-")))
			return true;
		else {
			fName = fName.substring(0, fName.lastIndexOf(File.pathSeparatorChar));
			gName = gName.substring(0, gName.lastIndexOf(File.pathSeparatorChar));
			if (fName.startsWith(gName) || gName.startsWith(fName))
				return true;
		}
		return false;
	}
}
