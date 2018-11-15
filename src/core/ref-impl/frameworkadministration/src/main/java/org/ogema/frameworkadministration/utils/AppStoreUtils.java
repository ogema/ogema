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
package org.ogema.frameworkadministration.utils;

/**
 *
 * @author tgries
 */
public class AppStoreUtils {

	//	public static final String ALLPERMISSION = "java.security.AllPermission";
	//	public static final String LOCAL_APPSTORE_NAME = "localAppDirectory";
	//	public static final String LOCAL_APPSTORE_LOCATION = "../../appstore/";
	//	public static final String PROP_NAME_LOCAL_APPSTORE_LOCATION = "org.ogema.local.appstore";
	public static final String INSTALLATION_STATE_ATTR_NAME = "installstate";
	//	public static final String TMPDIR = "./";
	public static final String GRANTED_PERMS_NAME = "permission";
	//	public static final String PERMS_ENTRY_NAME = "OSGI-INF/permissions.perm";
	public static final String[] FILTERED_APPS = { "org.ogema.ref-impl" };

	public static final String[] FILTER_EXCEPTIONS = { "org.ogema.ref-impl.framework-gui",
			"org.ogema.ref-impl.security-gui", "org.ogema.ref-impl.rest" }; // "framework-administration"?

}
