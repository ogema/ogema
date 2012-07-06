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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ogema.persistence.impl.mem;

import org.junit.BeforeClass;
import org.ogema.core.model.Resource;
import org.ogema.impl.persistence.ResourceDBImpl;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.persistence.ResourceDB;

/**
 * 
 * @author jlapp
 */
public class TestBase {

	final Class<? extends Resource> TEST_TYPE = OnOffSwitch.class;
	final String TEST_RESOURCE_NAME = "test";
	static int COUNTER = 0;
	private final String APP_ID = "NoSuchApp";

	static ResourceDB db;

	@BeforeClass
	public static void init() {
		db = new ResourceDBImpl();
	}

	protected String resname() {
		return TEST_RESOURCE_NAME + COUNTER++;
	}

	protected String getAppId() {
		return APP_ID;
	}

}
