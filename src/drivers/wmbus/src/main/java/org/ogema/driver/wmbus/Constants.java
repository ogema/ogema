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
package org.ogema.driver.wmbus;

/**
 * 
 * @author mns
 * 
 */
public class Constants {

	// Utility classes should not have public constructors (squid:S1118)
	private Constants() {
	}

	public static final String IF_NAME_PROP = "org.ogema.driver.zwave.portname";
	public static final int CONNECT_WAIT_TIME = 30000;
	public static final String HARDWARE_DESCRIPTOR_PROP = "org.ogema.driver.zwave.hardware-descriptor";
	public static final String IMST_IM871A = "";
}
