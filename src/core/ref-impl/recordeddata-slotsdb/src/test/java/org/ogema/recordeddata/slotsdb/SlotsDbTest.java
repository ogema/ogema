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

	private static final String testDirectory = SlotsDb.DB_TEST_ROOT_FOLDER;
	// the basic unit according to which SlotsDb organises its file storage
	public static final long ONE_DAY = 24 * 3600 * 1000;

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
