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
package org.ogema.accesscontrol;

import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.resourcetree.TreeElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Zekeriya Mansuroglu
 *
 */
public class ResourcePermission extends Permission {

	private static Map<String, Class<?>> resourceTypeClasses = new ConcurrentHashMap<>();

	private static final long serialVersionUID = -7090110935361939550L;
	public static final String READ = "read";
	public static final String WRITE = "write";
	public static final String ADDSUB = "addsub";
	public static final String CREATE = "create";
	public static final String DELETE = "delete";
	public static final String ACTIVITY = "activity";
	public static final String ALLACTIONS = "read,write,create,addsub,delete,activity";
	static final int CANONICAL_ACTIONS_LENGTH = ALLACTIONS.length();

	public static final int _READ = 1 << 0;
	public static final int _WRITE = 1 << 1;
	public static final int _ADDSUB = 1 << 2;
	public static final int _CREATE = 1 << 3;
	public static final int _DELETE = 1 << 4;
	public static final int _ACTIVITY = 1 << 5;
	private static final String[] ALLACTIONSTRINGS = new String[1 << 6];
	public static final int _ALLACTIONS = _READ | _WRITE | _ADDSUB | _CREATE | _DELETE | _ACTIVITY;
	public static final int _NOACTIONS = 0;
	private static final String INVALID_CLASS_NAME = "$";
	private static final Object RESOURCE_LIST_CLASS_NAME = ResourceList.class.getName();

	private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ResourcePermission.class);

	/**
	 * The canonical form of the actions "read,write,create,addsub,delete,activity"
	 */
	String actions;

	public int getActionsAsMask() {
		return actionsAsMask;
	}

	String path;
	boolean star, minus;
	int actionsAsMask;
	String type; // type name string instead of Class itself avoid CNFE after policy entry
	int count;

	public String getPath() {
		return path;
	}

	public boolean isWced() {
		return star || minus;
	}

	public String getType() {
		return type;
	}

	public int getCount() {
		return count;
	}

	private String owner;
	private TreeElement node;

	enum PathType {
		STAR, STAR_ONLY, NO_WILDCARD, MINUS_ONLY, MINUS
	};

	PathType pathType;

	/**
	 * The constructor used to inject the system or the locale permissions into the security administration. This
	 * constructor shouldn't be used to perform a security check. The action string is a comma separated collection of
	 * the possible values CREATE, ADDSUB, READ, WRITE and DELETE or ANY for all possible actions without restrictions.
	 * The filter string is a comma separated key-value pairs like “type=org.ogema.model.hvac.AirCond,path=*,count=12”.
	 * The meanings of the possible keys are as follows. path: The path information of a resource instance. type: Fully
	 * qualified name of the type definition class. This parameter shall not consist of a wildcarded name but it could
	 * be a wildcard only (null) too that means that the type is ignored during the evaluation of the permission and
	 * only the name and count information are considered. count: maximal number of resource instances that could be
	 * created. recursive: set if a recursion is granted. Example Recursive: An app that wants access to a special type
	 * of resources ask for that by a Resource permission like:
	 * ResourcePermission(“path=*,type=org.ogema.model.actors.Actor", “READ, WRITE”) which calls for read and write
	 * permissions for any refrigerator in the system and their subcomponents.
	 * 
	 * @param filter
	 * @param actions
	 */
	public ResourcePermission(String filter, String actions) {
		super(filter);
		try {
			this.count = Integer.MAX_VALUE;
			parseActions(actions);
			/*
			 * If the filter string doesn't specify a path this is equal to "*" That means path=null and wildcarded =
			 * true
			 */
			this.star = true;
			this.minus = true;
			parseFilter(filter);
		} catch (Throwable e) {
			LoggerFactory.getLogger(ResourcePermission.class)
					.error("Failed to create resource permission for filter {} and actions {}", filter, actions);
			throw e;
		}
		setPathType();
	}

	/**
	 * The constructor used by the initiator of the permission check for the action CREATE. The path value can contain a
	 * wildcard. Valid values for this parameter are for instance; “*”, “myResource/theSubresource/*”,
	 * “theTopLevel/firstChild/leaveResource”. A Value like “myResource/theSubresource/*\/leafValue” is invalid because
	 * the wildcard shall be at the end of the path string. Still the recommended way to use the permission is setting a
	 * maximum number for all paths and one or all types like: new ResourcePermission(“*”,null, 200) allows the app to
	 * create 200 resources at any path of any type. A wildcard (-1) or an unspecified count means that no restriction
	 * is enforced. In this case a configurable default maximum count set will be used. Beginning from a resource of the
	 * queried type the whole sub tree is permitted – independent of the type. This option should only be used when an
	 * app tells its demanded permissions. The granted permissions shall be coded in a conservative way. New resources
	 * should be excluded by default.
	 * 
	 * @param path
	 *            The path information of a resource instance.
	 * @param type
	 *            This is the type definition class.
	 * @param maxNumber
	 *            limits the number of resources that can be created in the named path with the named type.
	 */
	public ResourcePermission(String path, Class<? extends Resource> type, int maxNumber) {
		super(path);
		if (path == null)
			path = "*";
		try {
			/*
			 * This constructor is used only for the action CREATE. Set it.
			 */
			actionsAsMask = _CREATE;
			actions = CREATE;
			if (path != null)
				parsePath(path);
			this.count = maxNumber;
			if (type == null)
				this.type = INVALID_CLASS_NAME;
			else
				this.type = type.getName();
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		}
		setPathType();
	}

	/**
	 * Constructor to check READ, WRITE, DELETE and ADDSUB actions on resources.
	 * 
	 * @param action
	 * @param te
	 * @param maxNumber
	 */
	public ResourcePermission(String action, TreeElement te, int maxNumber) {
		super(te.getName());
		try {
			// pType = PermissionType.QUERY;
			parseActions(action);
			this.count = maxNumber;
			Class<?> type = te.getType();
			if (type == ResourceList.class)
				type = te.getResourceListType();
			if (type == null)
				this.type = INVALID_CLASS_NAME;
			else
				this.type = type.getName();
			this.path = te.getLocation(); // Security Requirement PERM-SEC 2: The queried path of a resource is
			// translated into a path free of OGEMA 2.0 references (location) before the
			// check. OGEMA 2.0 references may point to any position of the tree and do
			// not forward any permission.

			// trim '/' at the end of
			int len = 0;
			if (this.path != null) {
				len = this.path.length();
				if (this.path.charAt(len - 1) == '/')
					this.path = this.path.substring(0, len - 1);
			}
			this.owner = te.getAppID();
			this.node = te;
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		}
		setPathType();
	}

	private void setPathType() {
		if (star && path == null)
			pathType = PathType.STAR_ONLY;
		else if (star && path != null)
			pathType = PathType.STAR;
		else if (!star && minus && path == null)
			pathType = PathType.MINUS_ONLY;
		else if (!star && minus && path != null)
			pathType = PathType.MINUS;
		else
			pathType = PathType.NO_WILDCARD;
	}

	private void parsePath(String value) {
		// skip leading /
		try {
			if (value.indexOf('/') == 0)
				value = value.substring(1);
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Invalid path string: " + value);
		}
		int length = value.length();
		/*
		 * Handle wildcard
		 */
		// Case 1 : path consists of a wildcard
		// encode it in a path equal null and wildcard flag true.
		if (value == null || value.equals("*")) {
			this.path = null;
			star = true;
			minus = false;
			return;
		}
		if (value == null || value.equals("-")) {
			this.path = null;
			star = false;
			minus = true;
			return;
		}

		int wcindex = value.indexOf('*');
		int minusindex = value.indexOf('-');
		// Case 3 : path is not wildcarded
		if (wcindex == -1 && minusindex == -1) {
			this.path = value;
			star = false;
			minus = false;
			// if no wildcard remove '/' at the end
			value = this.path;
			int len = value.length();
			if (value.charAt(len - 1) == '/')
				this.path = value.substring(0, len - 1);
		}
		// Case 2 : path ends with a wildcard
		else if (wcindex == length - 1) {
			// the wildcard must be after '/'
			if (value.charAt(length - 2) != '/')
				throw new IllegalArgumentException("Invalid path string: " + value);
			this.path = value.substring(0, length - 1);
			star = true;
			minus = false;
		}
		else if (minusindex == length - 1) {
			// the wildcard must be after '/'
			if (value.charAt(length - 2) != '/')
				throw new IllegalArgumentException("Invalid path string: " + value);
			this.path = value.substring(0, length - 1);
			minus = true;
			star = false;
		}
		// Case 4 : wildcard amid of the path string
		else
			throw new IllegalArgumentException("Invalid path string: " + value);
	}

	private void parseFilter(String filter) {
		/*
		 * Check is the filter consists a wildcard, that would mean unrestricted resource permissions
		 */
		if (filter.equals("*")) {
			this.path = "*";
			this.count = Integer.MAX_VALUE;
			this.star = true;
			this.minus = false;
			this.type = null;
			this.owner = null;
			return;
		}
		else if (filter.equals("-")) {
			this.path = "*";
			this.count = Integer.MAX_VALUE;
			this.star = false;
			this.minus = true;
			this.type = null;
			this.owner = null;
			return;
		}
		else if (filter.indexOf('=') == -1)
			throw new IllegalArgumentException("Invalid filter string: " + filter);
		// to get the tokens (path.., type.., count.., recursive..)
		StringTokenizer st1 = new StringTokenizer(filter, ",");
		while (st1.hasMoreTokens()) {
			String token = st1.nextToken();
			/* to get the keys */
			StringTokenizer st2 = new StringTokenizer(token, "=");
			String key = null;
			String value = null;
			try {
				key = st2.nextToken();
				value = st2.nextToken();
			} catch (NoSuchElementException e1) {
			}
			if (key == null || value == null)
				throw new IllegalArgumentException("Invalid filter string: " + filter);
			key = key.trim();
			value = value.trim();
			/* do the action */
			switch (key) {
			case "path":
				// throw leading /
				try {
					if (value.indexOf('/') == 0)
						value = value.substring(1);
				} catch (IndexOutOfBoundsException e) {
					throw new IllegalArgumentException("Invalid path string: " + value);
				}
				/*
				 * Handle wildcard
				 */
				// Case 1 : path consists of a wildcard
				// encode it in a path equal null and wildcard flag true.
				if (value.equals("*")) {
					this.path = null;
					star = true;
					minus = false;
					break;
				}
				if (value.equals("-")) {
					this.path = null;
					star = false;
					minus = true;
					break;
				}
				// Case 4 : wildcard amid of the path string
				int wcindex = value.indexOf('*');
				int minusindex = value.indexOf('-');
				// Case 3 : path is not wildcarded
				if ((wcindex == -1) && (minusindex == -1)) {
					this.path = value;
					star = false;
					minus = false;
					// break;
				}
				// Case 2 : path ends with a wildcard
				else if (wcindex == value.length() - 1) {
					this.path = value.substring(0, value.length() - 1);
					star = true;
					minus = false;
				}
				else if (minusindex == value.length() - 1) {
					this.path = value.substring(0, value.length() - 1);
					star = false;
					minus = true;
				}
				else
					throw new IllegalArgumentException("Invalid filter string: " + filter);
				// remove /'s at the end of path
				value = this.path;
				int len = this.path.length();
				if (/* !wced && */(value.charAt(len - 1) == '/'))
					this.path = value.substring(0, len - 1);
				break;
			case "type":
				if (value.equals("*")) {
					this.type = null;
				}
				else {
					this.type = value;
				}
				break;
			case "count":
				this.count = Integer.valueOf(value);
				break;
			case "owner":
				this.owner = value;
				break;
			default:
				throw new IllegalArgumentException("invalid filter string" + filter);
			}

		}
	}

	private Class<?> getClass(final String typename) {
		Class<?> c = resourceTypeClasses.get(typename);
		if (c == null) {
			c = getClassPrivileged(typename);
			if (c != null) {
				resourceTypeClasses.put(typename, c);
			}
		}
		return c;
	}

	private Class<?> getClassPrivileged(final String typename) {
		Class<?> result = AccessController.doPrivileged(new PrivilegedAction<Class<?>>() {
			@Override
			public Class<?> run() {
				try {
					return Class.forName(typename);
				} catch (ClassNotFoundException ioe) {
					// This exception occurs in particular, as long as no resource of a custom type has been created.
					// The log message is misleading in this case; if the resource type is really not exported, no
					// resource of the respective type can be created anyway
					// logger.warn(String.format(
					// "Resource type class %s couldn't be loaded. Therefor the type hierarchy can't be considered in
					// the permission check. To avoid this the type class should be exported by the system or any other
					// application.",
					// typename));
					return null;
				}
			}
		});
		return result;
	}

	@SuppressWarnings("fallthrough")
	private int parseActions(String actStr) {
		// Helper variable to detect if the actions string starts with comma
		boolean comma = false;
		int bitMask = _NOACTIONS;
		if (actStr == null) {
			actions = "";
			return _NOACTIONS;
		}
		// Get the chars of actions into a char array and parse it by iteration
		// over the array elements
		char[] chArr = actStr.toCharArray();
		int index = chArr.length;
		if (index == 0) {
			actions = "";
			return _NOACTIONS;
		}
		// Begin at the last element of the array
		index--;
		while (index >= 0) {
			char tmp = chArr[index];
			// Skip all white spaces at the end of the string
			while ((index >= 0) && (tmp == ' ') && (tmp == '\t') && (tmp == '\n') && (tmp == '\r') && (tmp == '\f')) {
				index--;
				tmp = chArr[index];
			}
			// scan actions strings for expected values
			int charsToSkip;
			// check for read
			if (index >= 3 && ((chArr[index - 3] == 'r') || (chArr[index - 3] == 'R'))
					&& ((chArr[index - 2] == 'e') || (chArr[index - 2] == 'E'))
					&& ((chArr[index - 1] == 'a') || (chArr[index - 1] == 'A'))
					&& ((chArr[index] == 'd') || (chArr[index] == 'D'))) {
				charsToSkip = 4;
				bitMask |= _READ;
			}
			// check for write
			else if (index >= 4 && ((chArr[index - 4] == 'w') || (chArr[index - 4] == 'W'))
					&& ((chArr[index - 3] == 'r') || (chArr[index - 3] == 'R'))
					&& ((chArr[index - 2] == 'i') || (chArr[index - 2] == 'I'))
					&& ((chArr[index - 1] == 't') || (chArr[index - 1] == 'T'))
					&& ((chArr[index] == 'e') || (chArr[index] == 'E'))) {
				charsToSkip = 5;
				bitMask |= _WRITE;
			}
			// check for create
			else if (index >= 5 && ((chArr[index - 5] == 'c') || (chArr[index - 5] == 'C'))
					&& ((chArr[index - 4] == 'r') || (chArr[index - 4] == 'R'))
					&& ((chArr[index - 3] == 'e') || (chArr[index - 3] == 'E'))
					&& ((chArr[index - 2] == 'a') || (chArr[index - 2] == 'A'))
					&& ((chArr[index - 1] == 't') || (chArr[index - 1] == 'T'))
					&& ((chArr[index] == 'e') || (chArr[index] == 'E'))) {
				charsToSkip = 6;
				bitMask |= _CREATE;
			}
			// check for add_sub
			else if (index >= 5 && ((chArr[index - 5] == 'a') || (chArr[index - 5] == 'A'))
					&& ((chArr[index - 4] == 'd') || (chArr[index - 4] == 'D'))
					&& ((chArr[index - 3] == 'd') || (chArr[index - 3] == 'D'))
					&& ((chArr[index - 2] == 's') || (chArr[index - 2] == 'S'))
					&& ((chArr[index - 1] == 'u') || (chArr[index - 1] == 'U'))
					&& ((chArr[index] == 'b') || (chArr[index] == 'B'))) {
				charsToSkip = 6;
				bitMask |= _ADDSUB;
			}
			// check for delete
			else if (index >= 5 && ((chArr[index - 5] == 'd') || (chArr[index - 5] == 'D'))
					&& ((chArr[index - 4] == 'e') || (chArr[index - 4] == 'E'))
					&& ((chArr[index - 3] == 'l') || (chArr[index - 3] == 'L'))
					&& ((chArr[index - 2] == 'e') || (chArr[index - 2] == 'E'))
					&& ((chArr[index - 1] == 't') || (chArr[index - 1] == 'T'))
					&& ((chArr[index] == 'e') || (chArr[index] == 'E'))) {
				charsToSkip = 6;
				bitMask |= _DELETE;
			}
			// check for activity
			else if (index >= 7 && ((chArr[index - 7] == 'a') || (chArr[index - 7] == 'A'))
					&& ((chArr[index - 6] == 'c') || (chArr[index - 6] == 'C'))
					&& ((chArr[index - 5] == 't') || (chArr[index - 5] == 'T'))
					&& ((chArr[index - 4] == 'i') || (chArr[index - 4] == 'I'))
					&& ((chArr[index - 3] == 'v') || (chArr[index - 3] == 'V'))
					&& ((chArr[index - 2] == 'i') || (chArr[index - 2] == 'I'))
					&& ((chArr[index - 1] == 't') || (chArr[index - 1] == 'T'))
					&& ((chArr[index] == 'y') || (chArr[index] == 'Y'))) {
				charsToSkip = 8;
				bitMask |= _ACTIVITY;
			}
			else // check for wildcard
			if (index >= 0 && ((chArr[index] == '*'))) {
				charsToSkip = 1;
				bitMask |= _ALLACTIONS;
			}
			else
				throw new IllegalArgumentException(actStr);

			// Now skip all white spaces up to the comma
			comma = false;
			while ((index >= charsToSkip && !comma)) {
				switch (chArr[index - charsToSkip]) {
				case ',':
					comma = true;
				case ' ':
				case '\t':
				case '\n':
				case '\r':
				case '\f':
					break;
				default:
					throw new IllegalArgumentException(actStr);
				}
				index--;
			}
			index -= charsToSkip;
		}
		if (comma)
			throw new IllegalArgumentException("actions string start with comma: " + actStr);

		actionsAsMask = bitMask;
		// Build actions string
		if (bitMask == _ALLACTIONS) {
			actions = ALLACTIONS;
		}
		else {
			actions = ALLACTIONSTRINGS[actionsAsMask];
			if (actions == null) {
				StringBuilder sb = new StringBuilder(CANONICAL_ACTIONS_LENGTH);
				if ((actionsAsMask & _READ) != 0) {
					sb.append(READ);
				}
				if ((actionsAsMask & _WRITE) != 0) {
					if (sb.length() > 0)
						sb.append(',');
					sb.append(WRITE);
				}
				if ((actionsAsMask & _CREATE) != 0) {
					if (sb.length() > 0)
						sb.append(',');
					sb.append(CREATE);
				}
				if ((actionsAsMask & _ADDSUB) != 0) {
					if (sb.length() > 0)
						sb.append(',');
					sb.append(ADDSUB);
				}
				if ((actionsAsMask & _DELETE) != 0) {
					if (sb.length() > 0)
						sb.append(',');
					sb.append(DELETE);
				}
				if ((actionsAsMask & _ACTIVITY) != 0) {
					if (sb.length() > 0)
						sb.append(',');
					sb.append(ACTIVITY);
				}
				actions = sb.toString();
				ALLACTIONSTRINGS[actionsAsMask] = actions;
			}
		}
		return bitMask;
	}

	/**
	 * Check if this granted permission implies the permission given as argument.
	 * 
	 * First the queried path is translated into a path free of OGEMA references. OGEMA references may point to any
	 * position of the tree and do not forward any permission. The implies() Method of the ResourcePermission returns
	 * true if the following conditions are met: • the action flag of the queried permission was set in the granted
	 * permission • first the path is searched backwards, if an element of the granted type is found – starting with the
	 * queried resource. If type is Null, the queried resource is the element. If no element of the type was found, the
	 * permission is denied. • the path of the element found from the type check (further queried path) fits into the
	 * granted path. The element “*” in the granted path includes subpathes, while a granted path without “*” means
	 * equality. • the owner of the queried permission (owner of the referenced resource) is equal to the owner of the
	 * granted permission or the owner of the granted permission is null (Note that the handling of the owner property
	 * depends on the action set. See the description of the owner property above in this section.) • the number of the
	 * created resources due to this granted permission is smaller than the granted number (valid for CREATE, ADDSUB and
	 * DELETE)
	 * 
	 * Example: Grant to create 25 resources of any type in the structure fridge on the first floor: new
	 * ResourcePermission(ResourcePermission.CREATE, “firstFloor/myFridge/*”, null,null, 25); Now creating a resource
	 * would cause the check:
	 * 
	 * permisssionManager.handlePermission(new ResourcePermission(ResourcePermission.CREATE,
	 * “firstFloor/myFridge/Doorsensor”,Sensor.class,null,3)); This permission would be granted.
	 *
	 * @param p
	 *            the permission to be checked.
	 * @return true, if this granted permission implies the queried permission, false otherwise.
	 */
	/* @formatter:on */
	@Override
	public boolean implies(Permission p) {
		if (!(p instanceof ResourcePermission))
			return false;
		ResourcePermission qp = (ResourcePermission) p;
		/*
		 * Condition 1: The action flag of the queried permission was set in the granted permission
		 */
		int queriedActions = qp.actionsAsMask;
		if ((queriedActions & this.actionsAsMask) != queriedActions) {
			return false;
		}

		/*
		 * If a granted type is specified, the queried resource must be from the same type as the granted one or it must
		 * be a any sub resource of a resource from this type. First the path is searched backwards, if an element of
		 * the granted type is found – starting with the queried resource. If type is Null, the queried resource is the
		 * element to be considered in further checks. If no element of the type was found, the permission is denied.
		 */
		if (this.type != null) {
			if (this.type.equals(RESOURCE_LIST_CLASS_NAME)) {
				LOGGER.warn(
						"ResourcePermission should not be defined with ResourceList as type! Such a permission doesn't imply any ResourcePermission!");
				return false;
			}
			if (qp.node == null) {
				if (qp.type != null && (qp.type.equals(RESOURCE_LIST_CLASS_NAME)) && !qp.actions.equals(CREATE)
						&& !qp.actions.equals(ACTIVITY) && !qp.actions.equals(DELETE)) {
					LOGGER.warn(
							"ResourcePermission should be queried with ResourceList as type only at first creation of the resource! Such a permission doesn't imply any ResourcePermission!");
					return false;
				}
				else if (qp.type != null && !(qp.type.equals(RESOURCE_LIST_CLASS_NAME)) && !this.type.equals(qp.type))
					return false;
			}
			else {
				TreeElement parent = qp.node;
				boolean success = false;
				Class<?> cls = getClass(this.type);
				while (parent != null) {
					Class<?> parentCls = parent.getType();
					if (parentCls == ResourceList.class) {
						parentCls = parent.getResourceListType();
						if (parentCls == null) // A resource list its list type not yet specified. In this case
												// permission can be granted.
						{
							success = true;
							break;
						}
					}
					if (cls != null && parentCls != null) {
						if (cls.isAssignableFrom(parentCls)) {
							success = true;
							break;
						}
					}
					else if (parent.getType().getName().equals(this.type)) { // This case is the fall back solution, if
						// the model class couldn't be loaded.
						// In this case the check can not be
						// consider the type hierarchy.
						success = true;
						break;
					}
					parent = parent.getParent();
				}
				if (!success)
					return false;
			}
		}

		/*
		 * The path of the element found from the type check (further queried path) fits into the granted path. The
		 * element “*” in the granted path includes sub paths, while a granted path without “*” means equality.
		 * Generally the path could consist of only the wildcard (Type 1) or of a wildcarded specific path (Type 2) or a
		 * not wildcarded specific path (Type 3). The table below shows the combination of these types for the granted
		 * and queried paths and result of the implies method.
		 */

		/* @formatter:off */
		/*
		 * case | granted	| query 	|									|
		 * 		| path type	| path type	| 			implies					|
		 * ==================================================================
		 * 1    | 		1	| 	1	 	| 			true					|
		 * _____|___________|___________|___________________________________|
		 * 2    | 		1	| 	2	 	| 			true					|
		 * _____|___________|___________|___________________________________|
		 * 3    | 		1	|  	3	 	| 			true					|
		 * _____|___________|___________|___________________________________|
		 * 4    |  		2	| 	1		| 			false					|
		 * _____|___________|___________|___________________________________|
		 * 5    |  		2	| 	2 		| queryPath.startswith(grantedPath)	|
		 * _____|___________|___________|___________________________________|
		 * 6    |  		2	|  	3 		| queryPath.startswith(grantedPath) |
		 * 		|			|			|	grantedPath ends with '/'		|
		 * _____|___________|___________|___________________________________|
		 * 7    |  		3	|  	1		| 			false					|
		 * _____|___________|___________|___________________________________|
		 * 8    |  		3	|  	2		| 			false					|
		 * _____|___________|___________|___________________________________|
		 * 9    |  		3	|  	3 		| queryPath.equals(grantedPath)		|
		 * _____|___________|___________|___________________________________|
		 * True condition is (case 1 || case 2 || case 3 || case 5 || case 6 || case 9)
		 * 
		 * Here we need the false condition as break condition which is
		 *  (! case 1 && ! case 2 && ! case 3 && ! case 5 && ! case 6 && ! case 9)
		 *  
		 *  Note: in case 6 queryPath has to start with grant Path but mustn't be equal to it,
		 *  so "myPath/*" doesn't imply "myPath/"
		 *  additionally queryPath has to contain a '/' at grantPath.length()
		 *   
		 */
		/* @formatter:on */

		// case 1-3
		if ((pathType != PathType.STAR_ONLY))
			// case 16-20
			if ((pathType != PathType.MINUS_ONLY)) {
				String queryPath = qp.path;
				String grantPath = getGrantPath(queryPath);
				int glen = 0;
				if (grantPath != null)
					glen = grantPath.length();

				boolean queryNotStartsWithGrant = true;
				try {
					queryNotStartsWithGrant = grantPath != null && queryPath != null
							&& !queryPath.startsWith(grantPath);
				} catch (Exception e) {
				}

				boolean queryNotEqualsGrant = true;
				try {
					queryNotEqualsGrant = grantPath != null && queryPath != null && !queryPath.equals(grantPath);
				} catch (Exception e) {
				}

				boolean grantEndSlashNotMatchWithQuery = true;
				try {
					if (queryPath != null && queryPath.indexOf('/') != -1) {
						if ((queryPath.length() > glen)) {
							if (queryPath.charAt(glen) != '/') {
								grantEndSlashNotMatchWithQuery = true;
							}
							else {
								grantEndSlashNotMatchWithQuery = false;
							}
						}
					}
					else if (!queryNotEqualsGrant) {
						grantEndSlashNotMatchWithQuery = false;
					}
				} catch (Exception e) {
				}

				// case 7
				if (pathType != PathType.STAR || qp.pathType != PathType.STAR || queryNotStartsWithGrant
						|| grantEndSlashNotMatchWithQuery) {
					// case 8
					if (pathType != PathType.STAR || qp.pathType != PathType.NO_WILDCARD
							|| (queryNotEqualsGrant && (queryNotStartsWithGrant || grantEndSlashNotMatchWithQuery)))
						// case 10
						if (pathType != PathType.STAR || qp.pathType != PathType.MINUS
								|| (queryNotEqualsGrant && (queryNotStartsWithGrant || grantEndSlashNotMatchWithQuery)))
							// case 13
							if (pathType != PathType.NO_WILDCARD || qp.pathType != PathType.NO_WILDCARD
									|| (queryNotEqualsGrant))
								// case 22
								if (pathType != PathType.MINUS || qp.pathType != PathType.STAR
										|| (queryNotStartsWithGrant) || !queryNotEqualsGrant
										|| grantEndSlashNotMatchWithQuery)
									// case 23
									if (pathType != PathType.MINUS || qp.pathType != PathType.NO_WILDCARD
											|| (queryNotStartsWithGrant) || !queryNotEqualsGrant
											|| grantEndSlashNotMatchWithQuery)
										// case 25
										if (pathType != PathType.MINUS || qp.pathType != PathType.MINUS
												|| queryNotStartsWithGrant || grantEndSlashNotMatchWithQuery)
											return false;
				}
			}
		/*
		 * Condition 4: the owner of the queried permission (owner of the referenced resource) is equal to the owner of
		 * the granted permission or the owner of the granted permission is null.
		 * 
		 * Note: For the query permission the owner is the application id that created the addressed resource. For the
		 * CREATE-action the owner is ignored, for the ADDSUB action the owner of the resource to which a sub resource
		 * shall be added is meant and for READ, WRITE, and DELETE actions the resource that shall be read, written or
		 * deleted is referred to. The initiator of the query has to determine the right owner information.
		 * 
		 * The positive expression is ((owner==null)||(rp.owner.equals(owner))). For the negative case return false.
		 */
		// 4.1 If CREATE action is queried, owner is ignored
		if ((qp.actionsAsMask & _CREATE) == 0) {
			// 4.2 owner of the granted should be null or in other case it should be equal to the queried owner.
			if ((owner != null) && (!owner.equals(qp.owner)))
				return false;
		}

		/*
		 * Condition 5: the number of the created resources due to this granted permission is smaller than the granted
		 * number (valid for CREATE, ADDSUB and DELETE)
		 */
		if ((qp.actionsAsMask & (_CREATE | _ADDSUB)) != 0) {
			if (count != 0 && qp.count > count)
				return false;
		}
		return true;
	}

	// if queryPath doesn't contain any '/'s drop an ending '/' of grantedPath
	private String getGrantPath(String queryPath) {
		String result = this.path;
		if ((result != null) && (queryPath != null) && (queryPath.indexOf('/') == -1)) {
			int len = result.length();
			if (result.charAt(len - 1) == '/') {
				result = result.substring(0, len - 1);
			}
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ResourcePermission))
			return false;
		ResourcePermission perm = (ResourcePermission) obj;
		if ((actionsAsMask == perm.actionsAsMask)
				&& ((path == null && perm.path == null)
						|| (path != null && perm.path != null && path.equals(perm.path)))
				&& (star == perm.star) && (minus == perm.minus))
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 31 * 17 + getName().hashCode();
		hash = (hash << 5) - hash + actions.hashCode();
		return hash;
	}

	@Override
	public String getActions() {
		return actions;
	}
}
