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
package org.ogema.recordeddata.slotsdb;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
// ---------------------------------------
@SuiteClasses( { RecordedDataTest.class, //
		SingleIntervalTest.class, //
		ArgumentTest.class,//
		EmptyStorageTest.class, //
		MultiplePartlyIntervalsTest.class //
})
// ---------------------------------------
public class RecordedDataTestSuite {

	@BeforeClass
	public static void setUp() {
		System.out.println("setting up RecordedDataTestSuite");

		// delete testfolder if tearDown has failed before or old files exist due to single test execution, which were
		// not started via this test suite class
		deleteTestFolder();
	}

	@AfterClass
	public static void tearDown() {
		System.out.println("tearing down RecordedDataTestSuite");
		deleteTestFolder();
	}

	private static void deleteTestFolder() {

		//String path = System.getProperty("user.dir") + "/" + SlotsDb.DEFAULT_DB_ROOT_FOLDER;
		// delete whole data folder 
		String path = System.getProperty("user.dir") + "/data";
		System.out.println("deleting old files in: " + path);
		try {
			Runtime.getRuntime().exec("rm -r " + path);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
