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
/**
 * 
 */
package org.ogema.accesscontrol;

/**
 * @author Zekeriya Mansuroglu
 *
 */
public class AppPermissionFilter {

	public static final String KEY_APPNAME = "name";
	public static final String KEY_GROUP = "group";
	public static final String KEY_USER = "user";
	public static final String KEY_VERSION = "version";

	public static final AppPermissionFilter ALLAPPSPERMISSION = new AppPermissionFilter(null, null, null, null);

	String appname;
	String ownergroup;
	String owneruser;
	String version;

	public AppPermissionFilter(String name, String group, String user, String version) {
		this.appname = name;
		this.ownergroup = group;
		this.owneruser = user;
		this.version = version;
	}

	public String getAppname() {
		return appname;
	}

	public void setAppname(String appname) {
		this.appname = appname;
	}

	public String getOwnergroup() {
		return ownergroup;
	}

	public void setOwnergroup(String ownergroup) {
		this.ownergroup = ownergroup;
	}

	public String getOwneruser() {
		return owneruser;
	}

	public void setOwneruser(String owneruser) {
		this.owneruser = owneruser;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getFilterString() {
		boolean comma = false;
		StringBuffer sb = new StringBuffer();
		sb.append("name=");
		if (appname != null) {
			sb.append(appname);
		}
		else {
			sb.append("*");
		}
		comma = true;
		if (ownergroup != null) {
			if (comma)
				sb.append(',');
			sb.append("group=");
			sb.append(ownergroup);
			comma = true;
		}
		if (owneruser != null) {
			if (comma)
				sb.append(',');
			sb.append("user=");
			sb.append(owneruser);
			comma = true;
		}
		if (version != null) {
			if (comma)
				sb.append(',');
			sb.append("version=");
			sb.append(version);
			comma = true;
		}
		return sb.toString();
	}
}
