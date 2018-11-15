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
package org.ogema.accesscontrol;

import junit.framework.TestCase;

import org.junit.Test;
import org.ogema.accesscontrol.ChannelPermission;

public class ChannelPermissionTest {

	TestCase tc = new TestCase() {
	};

	@Test
	public void testparsepath() {

		final String pathstring = " busid= toll2, devaddr=5 * 3 7, chaddr= 22";
		ChannelPermission cp = new ChannelPermission(pathstring, "read");
		System.out.println(cp.busId + ";");
		for (String str : cp.devAddrs.values)
			System.out.println("-" + str + "-");
		for (String str : cp.chAddrs.values)
			System.out.println("-" + str + "-");

		final String pathstring2 = " busid= toll2, devaddr=34  65 2, chaddr=14";
		ChannelPermission cp2 = new ChannelPermission(pathstring2, null);

		final String pathstring3 = " " + " busid= toll2,,,, devaddr=65 2 , chaddr = 14 ";
		ChannelPermission cp3 = new ChannelPermission(pathstring3, null);

		if (cp.implies(cp2))
			System.out.println("Implies: true");
		else
			System.out.println("Implies: false");

		if (cp2.equals(cp3))
			System.out.println("Equals: true");
		else
			System.out.println("Equals: false");

		for (String str : cp3.devAddrs.values)
			System.out.println("-" + str + "-");
		System.out.println(cp.getActions());

		new ChannelPermission("*");
		new ChannelPermission(null);
		new ChannelPermission();
	}

}
