/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.impl.persistence;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.impl.persistence.testmodels.NonPersistentChild;
import org.ogema.persistence.DBConstants;
import org.ogema.resourcetree.TreeElement;

import junit.framework.TestCase;

//@RunWith(Parameterized.class)
public class NonPersistentTest extends DBBasicTest {

	@Parameterized.Parameters
	public static Collection<Object[]> params() {
		return Arrays.asList(new Object[][] { { 1, "" } });
	}

	// private final int executionOrder;
	// private final String name;
	//
	// public NonPersistentTest(int count, String name) {
	// this.executionOrder = count;
	// this.name = name;
	// }
	//
	// static ResourceDBImpl db;

	@BeforeClass
	public static void init() {
		System.setProperty("org.ogema.persistence", "active");
	}

	// Just an example appID, which has to be unique and probably will have the
	// format of a file path.
	private String testAppID = "/persistence/target/persistence-2.1.1-SNAPSHOT.jar";

	@Before
	public void before() throws InterruptedException {
		System.setProperty(DBConstants.DB_PATH_PROP, "nonPersistentTest");
		db = new ResourceDBImpl();
		db.setName("PersistenceTest");
		db.restart();
	}

	@After
	public void after() throws InterruptedException {

	}

	//@Ignore // Test fails on the CI server only !!
	@Test
	public void changeNonpersistentChildActivityWorks() {
		String name = "resourceWithNonpersistentChild";
		TreeElement te;
		if (db.hasResource(name)) {
			te = db.getToplevelResource(name);
			db.deleteResource(te);
		}
		te = db.addResource(name, NonPersistentChild.class, testAppID);
		try {
			te = te.addChild("f", FloatResource.class, false);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		// NonPersistent child not yet active
		TestCase.assertFalse(te.isActive());
		restartAndCompareDynamicData();
		te = db.getToplevelResource(name);
		te = te.getChild("f");
		TestCase.assertNotNull(te);
		TestCase.assertFalse(te.isActive());

		// activate NonPersistent child
		te.setActive(true);
		TestCase.assertTrue(te.isActive());
		restartAndCompareDynamicData();
		te = db.getToplevelResource(name);
		te = te.getChild("f");
		TestCase.assertTrue(te.isActive());
	}

	@Test
	public void addReferenceToNonpersistentChildWorks() {
		String name = "resourceWithNonpersistentChild";
		TreeElement te;
		if (db.hasResource(name)) {
			te = db.getToplevelResource(name);
			db.deleteResource(te);
		}
		te = db.addResource(name, NonPersistentChild.class, testAppID);
		TreeElement teCh = te.addChild("f", FloatResource.class, false);
		TreeElement teRef = te.addReference(teCh, "fRef", true);
		TestCase.assertNotNull(teRef);
		TestCase.assertEquals(teRef.getReference(), teCh);
		restartAndCompareDynamicData();
		te = db.getToplevelResource(name);
		teCh = te.getChild("f");
		teRef = te.getChild("fRef");
		TestCase.assertNotNull(teRef);
		TestCase.assertEquals(teRef.getReference(), teCh);
	}

}
