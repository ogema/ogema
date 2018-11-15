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
package org.ogema.frameworkadministration.json.get;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author tgries
 */
public class UserInformationJsonGet implements Serializable {

	private static final long serialVersionUID = -8725777055903096839L;

	private String name;
	private boolean isAdmin;
	private Map<Object, Object> credentials;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public Map<Object, Object> getCredentials() {
		return credentials;
	}

	public void setCredentials(Map<Object, Object> credentials) {
		this.credentials = credentials;
	}

}
