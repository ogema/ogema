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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.recordeddata.DataRecorder;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
@Service(DataRecorder.class)
public class SlotsDb implements DataRecorder {

	private static Logger logger = LoggerFactory.getLogger(SlotsDb.class.getName());

	/*
	 * File extension for SlotsDB files. Only these Files will be loaded.
	 */
	public static String FILE_EXTENSION = ".slots";

	/*
	 * Root folder for SlotsDB files
	 */
	public static String DB_ROOT_FOLDER = System.getProperty(SlotsDb.class.getPackage().getName().toLowerCase()
			+ ".dbfolder");

	/*
	 * If no other root folder is defined, data will be stored to this folder
	 */
	public static String DEFAULT_DB_ROOT_FOLDER = "data/slotsdb/";

	/*
	 * Root Folder for JUnit Testcases
	 */
	public static String DB_TEST_ROOT_FOLDER = "testdata/";

	/*
	 * limit open files in Hashmap
	 * 
	 * Default Linux Configuration: (should be below)
	 * 
	 * host:/#> ulimit -aH [...] open files (-n) 1024 [...]
	 */
	public static String MAX_OPEN_FOLDERS = System.getProperty(SlotsDb.class.getPackage().getName().toLowerCase()
			+ ".max_open_folders");
	public static int MAX_OPEN_FOLDERS_DEFAULT = 512;

	/*
	 * configures the data flush period. The less you flush, the faster SLOTSDB will be. unset this System Property (or
	 * set to 0) to flush data directly to disk.
	 */
	public static String FLUSH_PERIOD = System.getProperty(SlotsDb.class.getPackage().getName().toLowerCase()
			+ ".flushperiod");

	/*
	 * configures how long data will at least be stored in the SLOTSDB.
	 */
	public static String DATA_LIFETIME_IN_DAYS = System.getProperty(SlotsDb.class.getPackage().getName().toLowerCase()
			+ ".limit_days");

	/*
	 * configures the maximum Database Size (in MB).
	 */
	public static String MAX_DATABASE_SIZE = System.getProperty(SlotsDb.class.getPackage().getName().toLowerCase()
			+ ".limit_size");

	/*
	 * Minimum Size for SLOTSDB (in MB).
	 */
	public static int MINIMUM_DATABASE_SIZE = 2;

	/*
	 * Initial delay for scheduled tasks (size watcher, data expiration, etc.)
	 */
	public static int INITIAL_DELAY = 10000;

	/*
	 * Interval for scanning expired, old data. Set this to 86400000 to scan every 24 hours.
	 */
	public static int DATA_EXPIRATION_CHECK_INTERVAL = 5000;

	final FileObjectProxy proxy;
	private final Map<String, SlotsDbStorage> storagesMap = new HashMap<String, SlotsDbStorage>();

	public SlotsDb() {
		if (DB_ROOT_FOLDER == null) {
			proxy = new FileObjectProxy(DEFAULT_DB_ROOT_FOLDER);
		}
		else {
			proxy = new FileObjectProxy(DB_ROOT_FOLDER);
		}
	}

	@Override
	public RecordedDataStorage createRecordedDataStorage(String id, RecordedDataConfiguration configuration)
			throws DataRecorderException {
		if (storagesMap.containsKey(id)) {
			throw new DataRecorderException("Storage with given ID exists already");
		}
		SlotsDbStorage storage = new SlotsDbStorage(id, configuration, this);
		storagesMap.put(id, storage);
		return storage;
	}

	@Override
	public RecordedDataStorage getRecordedDataStorage(String recDataID) {
		return storagesMap.get(recDataID);
	}

	@Override
	public boolean deleteRecordedDataStorage(String id) {
		// TODO close storage?
		if (storagesMap.remove(id) == null) {
			return false;
		}
		else {
			return true;
		}
	}

	@Override
	public List<String> getAllRecordedDataStorageIDs() {
		List<String> ids = new ArrayList<>(storagesMap.size());
		ids.addAll(storagesMap.keySet());
		return ids;

	}
}
