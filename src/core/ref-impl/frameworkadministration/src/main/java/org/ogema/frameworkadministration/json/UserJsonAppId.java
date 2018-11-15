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
