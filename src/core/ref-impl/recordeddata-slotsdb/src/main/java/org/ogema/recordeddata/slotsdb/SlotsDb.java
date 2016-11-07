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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.administration.FrameworkClock;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.recordeddata.DataRecorder;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;
import org.osgi.framework.BundleContext;

/*
 * Timers: 
 * Whereas the timers responsible for removing old or bulky log data,
 * and for flushing, run on system time, the criterion when to remove 
 * old data is based on framework time.
 */
@Component(immediate = true)
@Service(DataRecorder.class)
public class SlotsDb implements DataRecorder {

//	private final static Logger logger = LoggerFactory.getLogger(SlotsDb.class);

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

	public static String SLOTS_DB_STORAGE_ID_PATH = DEFAULT_DB_ROOT_FOLDER + "slotsDbStorageIDs.ser";
	//public static String CONFIGURATION_PATH = DEFAULT_DB_ROOT_FOLDER + "configurations.ser";

	/*
	 * limit open files in Hashmap
	 * MultiplePartlyIntervalsTest
	 * Default Linux Configuration: (should be below)
	 * 
	 * host:/#> ulimit -aH [...] open files (-n) 1024 [...]
	 */
	public static String MAX_OPEN_FOLDERS = System.getProperty(SlotsDb.class.getPackage().getName().toLowerCase()
			+ ".max_open_folders");
	public static int MAX_OPEN_FOLDERS_DEFAULT = 512;

	/*
	 * configures the data flush period. The less you flush, the faster SLOTSDB
	 * will be. unset this System Property (or set to 0) to flush data directly
	 * to disk.
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
	 * Interval for scanning expired, old data. Set this to 86400000 to scan
	 * every 24 hours.
	 */
	public static int DATA_EXPIRATION_CHECK_INTERVAL = 5000;

	FileObjectProxy proxy; // quasi-final
	private final Map<String, SlotsDbStorage> slotsDbStorages = new HashMap<String, SlotsDbStorage>();
	
	@Reference
	FrameworkClock clock;  

//	public SlotsDb() {
	@Activate
    protected synchronized void activate(BundleContext ctx, Map<String, Object> config) {
		if (DB_ROOT_FOLDER == null) {
			proxy = new FileObjectProxy(DEFAULT_DB_ROOT_FOLDER, clock);
		}
		else {
			proxy = new FileObjectProxy(DB_ROOT_FOLDER, clock);
		}
		readPersistedSlotsDbStorages();
	}

	@Deactivate
	protected synchronized void deactivate(Map<String, Object> config) {
		if (proxy != null)
			proxy.close();
		proxy = null;
		synchronized (slotsDbStorages) {
			slotsDbStorages.clear();
		}
	}
	
	/**
	 * Persist the all SlotsDbStorage objects
	 */
	public void persistSlotsDbStorages() {

		Map<String, RecordedDataConfiguration> configurations = new HashMap<String, RecordedDataConfiguration>();

		synchronized (slotsDbStorages) {
			for (Iterator<String> iterator = slotsDbStorages.keySet().iterator(); iterator.hasNext();) {
				String id = iterator.next();
				configurations.put(id, slotsDbStorages.get(id).getConfiguration());
			}
		}

		ObjectOutputStream oos = null;

		try {
			oos = new ObjectOutputStream(new FileOutputStream(SLOTS_DB_STORAGE_ID_PATH));
			oos.writeObject(configurations);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Read back previously persisted SlotsDbStorage objects
	 */
	@SuppressWarnings("unchecked")
	private void readPersistedSlotsDbStorages() {

		ObjectInputStream ois = null;
		File file = new File(SLOTS_DB_STORAGE_ID_PATH);

		Map<String, RecordedDataConfiguration> configurations = new HashMap<String, RecordedDataConfiguration>();

		if (file.exists()) {
			try {
				ois = new ObjectInputStream(new FileInputStream(SLOTS_DB_STORAGE_ID_PATH));

				configurations = (Map<String, RecordedDataConfiguration>) ois.readObject();
				synchronized (slotsDbStorages) {
					for (Iterator<String> iterator = configurations.keySet().iterator(); iterator.hasNext();) {
						String id = iterator.next();
						slotsDbStorages.put(id, new SlotsDbStorage(id, configurations.get(id), this));
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (ois != null) {
					try {
						ois.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}

	// FIXME
	// caller must synchronize -> not any more
	@Override
	public RecordedDataStorage createRecordedDataStorage(String id, RecordedDataConfiguration configuration)
			throws DataRecorderException {
		SlotsDbStorage storage;
		synchronized (slotsDbStorages) {
			if (slotsDbStorages.containsKey(id)) {
				throw new DataRecorderException("Storage with given ID exists already");
			}
	
			storage = new SlotsDbStorage(id, configuration, this);
			slotsDbStorages.put(id, storage);
		}
		persistSlotsDbStorages();
		return storage;
	}

	@Override
	public RecordedDataStorage getRecordedDataStorage(String recDataID) {
		synchronized (slotsDbStorages) {
			return slotsDbStorages.get(recDataID);
		}
	}

	@Override
	public boolean deleteRecordedDataStorage(String id) {
		synchronized (slotsDbStorages) {
			if (slotsDbStorages.remove(id) == null) {
				return false;
			}
		}
		persistSlotsDbStorages();
		return true;
	}

	@Override
	public List<String> getAllRecordedDataStorageIDs() {
		List<String> ids = new ArrayList<String>();
		synchronized (slotsDbStorages) {
			for (Iterator<String> iterator = slotsDbStorages.keySet().iterator(); iterator.hasNext();) {
				ids.add(iterator.next());
			}
		}
		return ids;
	}

}
