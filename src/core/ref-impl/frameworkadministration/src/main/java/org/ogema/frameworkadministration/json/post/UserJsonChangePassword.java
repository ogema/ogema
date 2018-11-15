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
public class UserJsonChangePassword implements Serializable {

	private static final long serialVersionUID = -5986976838897210404L;

	private String user;
	private String pwd;
	private String oldpwd;

	public UserJsonChangePassword() {
	}

	public UserJsonChangePassword(String user, String oldpwd, String pwd) {
		this.user = user;
		this.pwd = pwd;
		this.oldpwd = oldpwd;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getoldpwd() {
		return oldpwd;
	}

	public void setoldpwd(String pwd) {
		this.oldpwd = pwd;
	}

}
