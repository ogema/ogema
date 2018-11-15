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
