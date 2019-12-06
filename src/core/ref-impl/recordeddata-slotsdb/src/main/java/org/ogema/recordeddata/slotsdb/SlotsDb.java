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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferencePolicyOption;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.administration.FrameworkClock;
import org.ogema.core.administration.FrameworkClock.ClockChangeListener;
import org.ogema.core.administration.FrameworkClock.ClockChangedEvent;
import org.ogema.core.channelmanager.measurements.SampledValue;
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
@Reference(
		referenceInterface = FrameworkClock.class,
		cardinality = ReferenceCardinality.OPTIONAL_UNARY,
		policy = ReferencePolicy.STATIC,
		policyOption = ReferencePolicyOption.RELUCTANT,
		bind = "setFrameworkClock",
		unbind = "removeFrameworkClock"
)
@Component(immediate = true)
@Service(DataRecorder.class)
public class SlotsDb implements DataRecorder, ClockChangeListener {

//	private final static Logger logger = LoggerFactory.getLogger(SlotsDb.class);

	/*
	 * File extension for SlotsDB files. Only these Files will be loaded.
	 */
	public final static String FILE_EXTENSION = ".slots";

	/*
	 * Root folder for SlotsDB files
	 */
	public final static String DB_ROOT_FOLDER = AccessController.doPrivileged(new PrivilegedAction<String>() {

		@Override
		public String run() {
			return System.getProperty("org.ogema.recordeddata.slotsdb.dbfolder");
		}
	});
			

	/*
	 * If no other root folder is defined, data will be stored to this folder
	 */
	public final static String DEFAULT_DB_ROOT_FOLDER = "data/slotsdb/";

	/*
	 * Root Folder for JUnit Testcases
	 */
	public final static String DB_TEST_ROOT_FOLDER = "testdata/";

	//public static String CONFIGURATION_PATH = DEFAULT_DB_ROOT_FOLDER + "configurations.ser";

	/*
	 * limit open files in Hashmap
	 * MultiplePartlyIntervalsTest
	 * Default Linux Configuration: (should be below)
	 * 
	 * host:/#> ulimit -aH [...] open files (-n) 1024 [...]
	 */
	public final static String MAX_OPEN_FOLDERS = AccessController.doPrivileged(new PrivilegedAction<String>() {

		@Override
		public String run() {
			return System.getProperty("org.ogema.recordeddata.slotsdb.max_open_folders");
		}
	});
			
	public final static int MAX_OPEN_FOLDERS_DEFAULT = 512;

	/*
	 * configures the data flush period. The less you flush, the faster SLOTSDB
	 * will be. unset this System Property (or set to 0) to flush data directly
	 * to disk.
	 */
	// this is now evaluated in the FileObjectProxy constructor, so the property can be set explicitly in the tests
//	public static String FLUSH_PERIOD = System.getProperty(SlotsDb.class.getPackage().getName().toLowerCase()
//			+ ".flushperiod");

	/*
	 * configures how long data will at least be stored in the SLOTSDB.
	 */
	public final static String DATA_LIFETIME_IN_DAYS = AccessController.doPrivileged(new PrivilegedAction<String>() {

		@Override
		public String run() {
			return System.getProperty("org.ogema.recordeddata.slotsdb.limit_days");
		}
		
	});
	
	/*
	 * configures the maximum Database Size (in MB).
	 */
	public final static String MAX_DATABASE_SIZE = AccessController.doPrivileged(new PrivilegedAction<String>() {

		@Override
		public String run() {
			return System.getProperty("org.ogema.recordeddata.slotsdb.limit_size");
		}
	});

	/*
	 * Minimum Size for SLOTSDB (in MB).
	 */
	public final static int MINIMUM_DATABASE_SIZE = 2;

	/*
	 * Initial delay for scheduled tasks (size watcher, data expiration, etc.)
	 */
	public final static int INITIAL_DELAY = 10000;

    /**
     * System property ({@value}) for setting {@link #DATA_EXPIRATION_CHECK_INTERVAL},
     * default value is 86400000 (24 hours).
     */
    public final static String DATA_EXPIRATION_CHECK_INTERVAL_PROPERTY = "org.ogema.recordeddata.slotsdb.scanning_interval";
    
	/**
	 * Interval (milliseconds) for scanning expired, old data. Set this to 86400000 to scan
	 * every 24 hours.
	 */
	public final static int DATA_EXPIRATION_CHECK_INTERVAL = AccessController.doPrivileged(new PrivilegedAction<Integer>() {

		@Override
		public Integer run() {
			return Integer.getInteger(DATA_EXPIRATION_CHECK_INTERVAL_PROPERTY, 86400000);
		}
	});
    
	private String dbRootFolder; // quasi-final
	private FileObjectProxy proxy; // quasi-final
	private String SLOTS_DB_STORAGE_ID_PATH; // quasi-final
	private final Map<String, SlotsDbStorage> slotsDbStorages = new HashMap<String, SlotsDbStorage>();
	
	volatile FrameworkClock clock;  
	
	void setFrameworkClock(FrameworkClock clock) {
		this.clock = clock;
		try {
			clock.addClockChangeListener(this);
		} catch (Exception ignore) {}
	
	}
	
	void removeFrameworkClock(FrameworkClock clock) {
		if (clock == this.clock)
			this.clock = null;
		try {
			clock.removeClockChangeListener(this);
		} catch (Exception ignore) {}
	}
	
	public SlotsDb() {}
	
	// only for tests
	SlotsDb(String baseFolder) {
		init(baseFolder);
	}
	
	private void init(String baseFolder) {
		if (baseFolder.endsWith("/") || baseFolder.endsWith("\\"))
			baseFolder = baseFolder.substring(0, baseFolder.length()-1);
		dbRootFolder = baseFolder;
		SLOTS_DB_STORAGE_ID_PATH = baseFolder + "/slotsDbStorageIDs.ser";
		this.proxy = new FileObjectProxy(baseFolder, clock);
		readPersistedSlotsDbStorages();
	}

	@Activate
    protected synchronized void activate(BundleContext ctx, Map<String, Object> config) {
		String baseFolder = (DB_ROOT_FOLDER != null ? DB_ROOT_FOLDER : DEFAULT_DB_ROOT_FOLDER);
		init(baseFolder);
	}

	@Deactivate
	protected synchronized void deactivate(Map<String, Object> config) {
		if (proxy != null)
			proxy.close();
		synchronized (slotsDbStorages) {
			slotsDbStorages.clear();
		}
		proxy = null;
		dbRootFolder = null;
		SLOTS_DB_STORAGE_ID_PATH = null;
	}
	
	final FileObjectProxy getProxy() {
		final FileObjectProxy proxy = this.proxy;
		if (proxy == null)
			throw new IllegalStateException("SlotsDb has not been initialized yet");
		return proxy;
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
	
	@Override
	public void clockChanged(ClockChangedEvent e) {
		final FrameworkClock clock = e.getClock();
		if (clock != this.clock)
			return;
		final long now = clock.getExecutionTime();
		synchronized (slotsDbStorages) {
			// folder lock must be obtained after locking the time series! We cannot completely rule out the possibility
			// that further future data is written while we do the check here.
//			proxy.folderLock.writeLock().lock();
			boolean futureDataExists = false;
			for (SlotsDbStorage s: slotsDbStorages.values()) {
				final SampledValue sv = s.getPreviousValue(Long.MAX_VALUE);
				if (sv != null && sv.getTimestamp() > now) {
					futureDataExists = true;
					break;
				}
			}
			if (!futureDataExists)
				return;
			proxy.folderLock.writeLock().lock();
			try {
				FileObjectProxy.logger.info("Found future log data after a clock change event... cleaning up.");
				proxy.deleteFutureFolders();
			} catch (IOException e1) {
				FileObjectProxy.logger.error("Clean up operation failed",e1);
			} finally {
				proxy.folderLock.writeLock().unlock();
			}
		}
	}
	
	
	

}
