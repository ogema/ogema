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
import java.util.ArrayList;
import java.util.List;

import org.ogema.core.application.AppID;

/**
 *
 * @author tgries
 */
public class UserJsonPermittedApps implements Serializable {

	private static final long serialVersionUID = 4590072284091784714L;

	private String user;
	private List<AppID> permittedApps = new ArrayList<AppID>();

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public List<AppID> getPermittedApps() {
		return permittedApps;
	}

	public void setPermittedApps(List<AppID> permittedApps) {
		this.permittedApps = permittedApps;
	}

}
