/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.frameworkadministration.json;

import java.io.Serializable;

/**
 *
 * @author tgries
 */
public class UserJsonAppId implements Serializable {

	private static final long serialVersionUID = 7299345064284785584L;

	private String AppID;
	private String readableName;
	private long bundleID;
	private boolean permitted;

	public UserJsonAppId() {
	}

	public UserJsonAppId(String AppID, String readableName, long bundleID, boolean permitted) {
		this.AppID = AppID;
		this.readableName = readableName;
		this.bundleID = bundleID;
		this.permitted = permitted;
	}

	public long getBundleID() {
		return bundleID;
	}

	public void setBundleID(long bundleID) {
		this.bundleID = bundleID;
	}

	public String getReadableName() {
		return readableName;
	}

	public void setReadableName(String readableName) {
		this.readableName = readableName;
	}

	public String getAppID() {
		return AppID;
	}

	public void setAppID(String AppID) {
		this.AppID = AppID;
	}

	public boolean isPermitted() {
		return permitted;
	}

	public void setPermitted(boolean permitted) {
		this.permitted = permitted;
	}

}
