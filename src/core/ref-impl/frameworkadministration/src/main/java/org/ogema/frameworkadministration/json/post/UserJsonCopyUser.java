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
package org.ogema.frameworkadministration.json.post;

import java.io.Serializable;

/**
 *
 * @author tgries
 */
public class UserJsonCopyUser implements Serializable {

	private static final long serialVersionUID = -6121673993546972489L;

	private String userOld;
	private String userNew;
	private String pwd;

	public UserJsonCopyUser() {
	}

	public UserJsonCopyUser(String userOld, String userNew, String pwd) {
		this.userOld = userOld;
		this.userNew = userNew;
		this.pwd = pwd;
	}

	public String getUserOld() {
		return userOld;
	}

	public void setUserOld(String userOld) {
		this.userOld = userOld;
	}

	public String getUserNew() {
		return userNew;
	}

	public void setUserNew(String userNew) {
		this.userNew = userNew;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

}
