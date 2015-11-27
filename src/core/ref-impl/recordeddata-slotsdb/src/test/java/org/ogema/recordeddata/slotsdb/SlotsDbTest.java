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

import java.io.File;

import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collection of helper methods. All tests extend this class. 
 */
public class SlotsDbTest {

	private static Logger logger = LoggerFactory.getLogger(SlotsDbTest.class.getName());

	private static final String testDirectory = System.getProperty("user.dir") + "/data";

	/**
	 * Prints the test method name when entering a test.
	 */
	@Rule
	public TestWatcher watchman = new TestWatcher() {
		@Override
		public void starting(final Description method) {
			logger.info("RUNNING TEST: " + method.getMethodName());
		}
	};

	public static void deleteTestFiles() {
		deleteTree(new File(testDirectory));
	}

	/**
	 * @param path
	 *            Deletes all files and folders under under the specified path.
	 */
	private static void deleteTree(File path) {

		if (path.exists()) {
			for (File file : path.listFiles()) {
				if (file.isDirectory())
					deleteTree(file);
				else if (!file.delete())
					logger.error(file + " could not be deleted!");
			}

			if (!path.delete()) {
				logger.error(path + " could not be deleted!");
			}
		}
	}

}
