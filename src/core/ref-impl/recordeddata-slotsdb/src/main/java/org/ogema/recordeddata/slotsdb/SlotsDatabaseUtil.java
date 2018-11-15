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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SlotsDatabaseUtil {

	private final static Logger logger = LoggerFactory.getLogger(SlotsDatabaseUtil.class);

	public static void printWholeFile(File file) throws IOException {
		if (!file.getName().contains(SlotsDb.FILE_EXTENSION)) {
			System.err.println(file.getName() + " is not a \"" + SlotsDb.FILE_EXTENSION + "\" file.");
			return;
		}
		else {
			DataInputStream dis = new DataInputStream(new FileInputStream(file));
			try {
				if (file.length() >= 16) {
					logger.debug("StartTimestamp: " + dis.readLong() + "  -  StepIntervall: " + dis.readLong());
					while (dis.available() >= 9) {
						logger.debug(dis.readDouble() + "  -\t  Flag: " + dis.readByte());
					}
				}
			} finally {
				dis.close();
			}
		}
	}

	public static void printWholeFile(String filename) throws IOException {
		printWholeFile(new File(filename));
	}
}
