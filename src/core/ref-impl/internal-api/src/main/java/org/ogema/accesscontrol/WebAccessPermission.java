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
package org.ogema.accesscontrol;

import java.security.Permission;
import java.util.StringTokenizer;

import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

/**
 * @author Zekeriya Mansuroglu
 *
 */
public class WebAccessPermission extends Permission {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5251833673446763300L;

	Version version;
	VersionRange vRange;
	Param user, group, app;

	public WebAccessPermission() {
		this(null, null);
	}

	public WebAccessPermission(String path) {
		this(path, null);
	}

	public WebAccessPermission(String path, String actions) {
		super((path == null) ? path = "*" : path);
		try {
			parseFilter(path);
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		}
	}

	public WebAccessPermission(String appname, String user, String group, Version version) {
		super("appname=" + appname + ",user=" + user + ",group=" + group + ",version=" + version == null ? "*"
				: version.toString());
		if (appname == null)
			appname = "*";
		if (user == null)
			user = "*";
		if (group == null)
			group = "*";
		try {
			this.app = new Param(appname == null ? "*" : appname);
			this.user = new Param(user);
			this.group = new Param(group);
			this.version = version;
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (!(obj instanceof WebAccessPermission))
			return false;

		WebAccessPermission that = (WebAccessPermission) obj;

		if (!this.app.value.equals(that.app.value) || !this.app.wc == that.app.wc)
			return false;
		if (!this.user.value.equals(that.user.value) || !this.user.wc == that.user.wc)
			return false;
		if (!this.group.value.equals(that.group.value) || !this.group.wc == that.group.wc)
			return false;
		return true;
	}

	private void parseFilter(String filter) {
		/*
		 * Check if the filter consists of a wildcard, that would mean unrestricted resource permissions
		 */
		if (filter.indexOf('=') == -1)
			throw new IllegalArgumentException("Invalid filter string: " + filter);

		this.app = Param.wildcardonly;
		this.user = Param.wildcardonly;
		this.group = Param.wildcardonly;

		// to get the tokens (path.., type.., count.., recursive..)
		StringTokenizer st1 = new StringTokenizer(filter, ",");
		while (st1.hasMoreTokens()) {
			String token = st1.nextToken();
			/* to get the keys */
			StringTokenizer st2 = new StringTokenizer(token, "=");
			String key = st2.nextToken();
			String value = st2.nextToken();
			if (key == null || value == null)
				throw new IllegalArgumentException("Invalid filter string: " + filter);
			key = key.trim();
			value = value.trim();
			/* do the action */
			switch (key) {
			case "name":
				this.app = new Param(value);
				break;
			case "user":
				this.user = new Param(value);
				break;
			case "group":
				this.group = new Param(value);
				break;
			case "version":
				vRange = new VersionRange(value);
				break;
			default:
				throw new IllegalArgumentException("invalid filter string" + filter);
			}
		}
	}

	@Override
	public boolean implies(Permission p) {
		if (!(p instanceof WebAccessPermission))
			return false;

		WebAccessPermission that = (WebAccessPermission) p;

		if (!this.app.implies(that.app))
			return false;
		if (!this.user.implies(that.user))
			return false;
		if (!this.group.implies(that.group))
			return false;

		if (vRange != null && that.version == null)
			return false;
		if (vRange != null && that.version != null && !vRange.includes(that.version))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return super.getName().hashCode();
	}

	@Override
	public String getActions() {
		return "";
	}

	public static class Param {
		/*
		 * This value is used when queried permissions created
		 */
		String value;
		boolean wc;
		static final Param wildcardonly = new Param("*");

		public Param(String param) {
			parse(param);
		}

		public void parse(String param) {
			value = param;

			int len = value.length();
			int wcindex = value.indexOf('*');
			// Case 3 : path is not wildcarded
			if (wcindex == -1) {
				wc = false;
			}
			// Case 2 : path ends with a wildcard
			else if (wcindex == len - 1) {
				if (len > 1)
					value = value.substring(0, len - 1);
				else
					value = "*";
				wc = true;
			}
			else {
				RuntimeException e = new IllegalArgumentException("Invalid filter string: " + param);
				e.printStackTrace();
				throw e;
			}
		}

		/* @formatter:off */
		/*
		 * case | granted	| query 	|									|
		 * 		| path type	| path type	| 			implies					| example
		 * ===================================================================================================
		 * 1    | 		1	| 	1	 	| 			true					| 
		 * _____|___________|___________|___________________________________|___________|____________
		 * 2    | 		1	| 	2	 	| 			true					|
		 * _____|___________|___________|___________________________________|___________|____________
		 * 3    | 		1	|  	3	 	| 			true					|   
		 * _____|___________|___________|___________________________________|___________|____________
		 * 4    |  		2	| 	1		| 			false					|	
		 * _____|___________|___________|___________________________________|___________|____________
		 * 5    |  		2	| 	2 		| queryPath.startswith(grantedPath)	|			|
		 * _____|___________|___________|___________________________________|___________|____________
		 * 6    |  		2	|  	3 		| queryPath.startswith(grantedPath)	|			|
		 * _____|___________|___________|___________________________________|___________|____________
		 * 7    |  		3	|  	1		| 			false					|			|
		 * _____|___________|___________|___________________________________|___________|____________
		 * 8    |  		3	|  	2		| 			false					|			|
		 * _____|___________|___________|___________________________________|___________|____________
		 * 9    |  		3	|  	3 		| queryPath.equals(grantedPath)		|			|
		 * 
		 * True condition is (case 1 || case 2 || case 3 || case 5 || case 6 || case 9)
		 * 
		 * Here we need the false condition as break condition which is
		 *  (! case 1 && ! case 2 && ! case 3 && ! case 5 && ! case 6 && ! case 9)
		 *  
		 */
		/* @formatter:on */

		boolean implies(Param req) {
			boolean wcOnly = value.equals("*");
			// case 1-3
			if (wcOnly)
				return true;
			String str = req.value;
			if (!value.equals(str))
				return false;
			// case 5
			if (wc || req.wc || (value != null && str != null && !str.startsWith(value)))
				// case 6
				if (!wc || req.wc || (value != null && str != null && !str.startsWith(value)))
					// case 9
					if (wc || req.wc || (value != null && str != null && !str.startsWith(value)))
						return false;
			return true;
		}
	}

}
