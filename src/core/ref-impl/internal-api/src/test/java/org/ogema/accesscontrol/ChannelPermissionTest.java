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
