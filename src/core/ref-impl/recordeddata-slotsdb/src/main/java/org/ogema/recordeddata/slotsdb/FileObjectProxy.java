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
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ogema.core.administration.FrameworkClock;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.recordeddata.DataRecorderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileObjectProxy {

	private static final int FLEXIBLE_STORING_PERIOD = -1;

	private final static Logger logger = LoggerFactory.getLogger(FileObjectProxy.class);
	
	// not thread safe, need to create new instances 
	private final static SimpleDateFormat getDateFormat() {
		return new SimpleDateFormat("yyyyMMdd");
	}
	
	/**
	 * For creation and deletion (write lock), resp. parsing (read lock), of folders
	 */
	private final ReadWriteLock folderLock = new ReentrantReadWriteLock();

	private final File rootNode;
	/**
	 *  guarded by folderLock; note: this is cleaned up at certain times... do not rely on this
	 *  to contain all available files
	 */
	private final HashMap<String, FileObjectList> openFilesHM = new HashMap<>();
	private final ConcurrentMap<String, String> encodedLabels = new ConcurrentHashMap<>();
	private final static Date min;
	private final static Date max;
	private final static long minL;
	private final static long maxL;
	private final Date date;
	private final Timer timer;
	// guarded by folder lock TODO check
	private List<File> days;
	// can be null, if data is written to disk immediately
	private final Flusher flusher;
	private final DeleteJob deleteJob;
	private final SizeWatcher sizeWatcher;
	// guarded by folderLock
	private long size;
	private final FrameworkClock clock;

	/*
	 * Flush Period in Seconds. if flush_period == 0 -> write directly to disk.
	 */
	private final int flush_period;
	private final int limit_days;
	private final int limit_size;
	private final int max_open_files;

	private final Object currentDayLock = new Object();
	// following three guarded by currentDayLock
	private String strCurrentDay;
	private long currentDayFirstTS;
	private long currentDayLastTS;
	
	private final SlotsDbCache cache = new SlotsDbCache();
	
	static {
		try {
			SimpleDateFormat sdf = getDateFormat();
			min = sdf.parse("00010101");
			max = sdf.parse("99981231");
			minL = min.getTime();
			maxL = max.getTime();
		} catch (ParseException e1) {
			throw new RuntimeException("Invalid date format ");
		}
	}

	/*
	 * Constructor used exclusively by tests.
	 * @param rootNodePath
	 */
	FileObjectProxy(String rootNodePath) {
		this(rootNodePath, null);
	}
	
	/**
	 * Creates an instance of a FileObjectProxy<br>
	 * The rootNodePath (output folder) can be specified in JVM flag: org.ogema.recordeddata.slotsdb.dbfolder
	 */
	public FileObjectProxy(String rootNodePath, FrameworkClock clock) {
		this.clock = clock;
		timer = new Timer();
		date = new Date();

		if (!rootNodePath.endsWith("/")) {
			rootNodePath += "/";
		}
		logger.info("Storing to: " + rootNodePath);
		rootNode = new File(rootNodePath);
		rootNode.mkdirs();

		loadDays();
		
		Integer flush_period = Integer.getInteger(SlotsDb.class.getPackage().getName().toLowerCase() + ".flushperiod");
//		if (SlotsDb.FLUSH_PERIOD != null) {
		if (flush_period != null && flush_period > 0) {
			this.flush_period = flush_period;
			logger.info("Flushing Data every: " + flush_period + "s. to disk.");
			this.flusher = createScheduledFlusher();
		} else {
			logger.info("No Flush Period set. Writing Data directly to disk.");
			this.flusher = null;
			this.flush_period = 0;
		}

		if (SlotsDb.DATA_LIFETIME_IN_DAYS != null) {
			limit_days = Integer.parseInt(SlotsDb.DATA_LIFETIME_IN_DAYS);
			logger.info("Maximum lifetime of stored Values: " + limit_days + " Days.");
			deleteJob = createScheduledDeleteJob();
		}
		else {
			logger.info("Maximum lifetime of stored Values: UNLIMITED Days.");
			deleteJob = null;
			limit_days = 0;
		}
		int limit_size = 0;
		if (SlotsDb.MAX_DATABASE_SIZE != null) {
			limit_size = Integer.parseInt(SlotsDb.MAX_DATABASE_SIZE);
			if (limit_size < SlotsDb.MINIMUM_DATABASE_SIZE) {
				limit_size = SlotsDb.MINIMUM_DATABASE_SIZE;
			}
			logger.info("Size Limit: " + limit_size + " MB.");
			sizeWatcher = createScheduledSizeWatcher();
		}
		else {
			logger.info("Size Limit: UNLIMITED MB.");
			sizeWatcher = null;
		}
		this.limit_size = limit_size;

		if (SlotsDb.MAX_OPEN_FOLDERS != null) {
			max_open_files = Integer.parseInt(SlotsDb.MAX_OPEN_FOLDERS);
			logger.info("Maximum open Files for Database changed to: " + max_open_files);
		}
		else {
			max_open_files = SlotsDb.MAX_OPEN_FOLDERS_DEFAULT;
			logger.info("Maximum open Files for Database is set to: " + max_open_files + " (default).");
		}
	}
	
	public void close() {
		stopTask(flusher);
		stopTask(deleteJob);
		stopTask(sizeWatcher);
		timer.cancel();
		folderLock.writeLock().lock();
		try {
			cache.clearCache();
			clearOpenFilesHashMap();
		} catch (IOException e) {
			logger.warn("Closing log files failed",e);
		} finally {
			folderLock.writeLock().unlock();
		}
		encodedLabels.clear();
			
	}
	
	private static final void stopTask(InfoTask task) {
		if (task != null) {
			try {
				task.cancel();
				boolean wasRunning = waitForTask(task);
				if (!wasRunning) {
					task.run(); // execute task once more, so no data is lost
					waitForTask(task);
				}
			} catch (Exception e) {
				logger.error("Error stopping task and executing it",e);
			}
		}
	}
	
	private static boolean waitForTask(InfoTask task) throws InterruptedException {
		if (!task.isRunning())
			return false;
		for (int i=0; i< 100; i++) {
			if (!task.isRunning())
				break;;
			Thread.sleep(50);
		}
		return true;
	}

	/*
	 * loads a sorted list of all days in SLOTSDB. Necessary for search- and delete jobs.
	 */
	private void loadDays() {
		days = new Vector<File>();
		for (File f : rootNode.listFiles()) {
			if (f.isDirectory()) {
				days.add(f);
			}
		}
		days = sortFolders(days);
	}

	private List<File> sortFolders(List<File> days) {
		final SimpleDateFormat sdf = getDateFormat();
		Collections.sort(days, new Comparator<File>() {

			@Override
			public int compare(File f1, File f2) {
				int i = 0;
				try {
					i = Long.valueOf(sdf.parse(f1.getName()).getTime()).compareTo(sdf.parse(f2.getName()).getTime());
				} catch (ParseException | NumberFormatException e) {
					logger.error("Error during sorting Files: Folder doesn't match yyyymmdd Format?");
				}
				return i;
			}
		});
		return days;
	}

	/**
	 * Creates a Thread, that causes Data Streams to be flushed every x-seconds.<br>
	 * Define flush-period in seconds with JVM flag: org.ogema.recordeddata.slotsdb.flushperiod
	 */
	private Flusher createScheduledFlusher() {
		Flusher f = new Flusher();
		timer.schedule(f, flush_period * 1000, flush_period * 1000);
		return f;
	}
	
	abstract class InfoTask extends TimerTask {
		
		abstract boolean isRunning();
		
	}

	class Flusher extends InfoTask {

		private volatile boolean running = false;
		
		@Override
		public void run() {
			running = true;
			try {
				flush();
			} catch (IOException e) {
				logger.error("Flushing Data failed in IOException: " + e.getMessage());
			} finally {
				running = false;
			}
		}
		
		@Override
		boolean isRunning() {
			return running;
		}
		
	}

	private DeleteJob createScheduledDeleteJob() {
		DeleteJob dj = new DeleteJob();
		timer.schedule(dj, SlotsDb.INITIAL_DELAY, SlotsDb.DATA_EXPIRATION_CHECK_INTERVAL);
		return dj;
	}

	class DeleteJob extends InfoTask {
		
		private volatile boolean running = false;
		
		@Override
		boolean isRunning() {
			return running;
		}

		@Override
		public void run() {
			running = true;
			folderLock.writeLock().lock();
			try {
				deleteFoldersOlderThen(limit_days);
			} catch (IOException e) {
				logger.error("Deleting old Data failed in IOException: " + e.getMessage());
			} finally {
				running = false;
				folderLock.writeLock().unlock();
			}
		}

		/*
		 * requires synchro
		 */
		private void deleteFoldersOlderThen(int limit_days) throws IOException {
			Calendar limit = Calendar.getInstance(); // TODO make a field calendar instead of creation of a new instance each time
//			limit.setTimeInMillis(System.currentTimeMillis() - (86400000L * limit_days));
			if (clock != null)
				limit.setTimeInMillis(clock.getExecutionTime() - (86400000L * limit_days));
			else // only relevant for tests
				limit.setTimeInMillis(System.currentTimeMillis() - (86400000L * limit_days));
			final SimpleDateFormat sdf = getDateFormat();
			Iterator<File> iterator = days.iterator();
			try {
				while (iterator.hasNext()) {
					File curElement = iterator.next();
					logger.trace("Deleting old log data: {}", curElement);
					if (sdf.parse(curElement.getName()).getTime() + 86400000 < limit.getTimeInMillis()) { /*
																											 * compare
																											 * folder 's
																											 * oldest value
																											 * to limit
																											 */
						logger.info("Folder: " + curElement.getName() + " is older then " + limit_days
								+ " Days. Will be deleted.");
						deleteRecursiveFolder(curElement);
					}
					else {
						/* oldest existing Folder is not to be deleted yet */
						break;
					}
				}
				loadDays();
			} catch (ParseException e) {
				logger.error("Error during sorting Files: Any Folder doesn't match yyyymmdd Format?");
			}
		}
	}

	private SizeWatcher createScheduledSizeWatcher() {
		SizeWatcher zw = new SizeWatcher();
		timer.schedule(zw, SlotsDb.INITIAL_DELAY, SlotsDb.DATA_EXPIRATION_CHECK_INTERVAL);
		return zw;
	}

	class SizeWatcher extends InfoTask {
		
		private volatile boolean running = false;
		
		@Override
		boolean isRunning() {
			return running;
		}

		@Override
		public void run() {
			running = true;
			folderLock.writeLock().lock();
			try {
				while ((getDiskUsage(rootNode) / 1000000 > limit_size) && (days.size() >= 2)) { /*
																								 * avoid deleting
																								 * current folder
																								 */
					deleteOldestFolder();
				}
			} catch (IOException e) {
				logger.error("Deleting old Data failed in IOException: " + e.getMessage());
			} finally {
				running = false;
				folderLock.writeLock().unlock();
			}
		}

		private void deleteOldestFolder() throws IOException {
			if (days.size() >= 2) {
				logger.info("Exceeded Maximum Database Size: " + limit_size + " MB. Current size: " + (size / 1000000)
						+ " MB. Deleting: " + days.get(0).getCanonicalPath());
				deleteRecursiveFolder(days.get(0));
				days.remove(0);
				clearOpenFilesHashMap();
			}
		}
	}

	private static void deleteRecursiveFolder(File folder) {
		if (folder.exists()) {
			for (File f : folder.listFiles()) {
				if (f.isDirectory()) {
					deleteRecursiveFolder(f);
					if (f.delete()) {
						;
					}
				}
				else {
					f.delete();
				}
			}
			folder.delete();
		}
	}

	/*
	 * recursive function to get the size of a folder. sums up all files. needs an initial LONG to store size to.
	 */
	private long getDiskUsage(File folder) throws IOException {
		size = 0;
		recursive_size_walker(folder);
		return size;
	}

	private void recursive_size_walker(File folder) throws IOException {
		for (File f : folder.listFiles()) {
			size += f.length();
			if (f.isDirectory()) {
				recursive_size_walker(f);
			}
		}
	}
	
	/**
	 * Appends a new Value to Slots Database.
	 * 
	 * @param id
	 * @param value
	 * @param timestamp
	 * @param state
	 * @param storingPeriod
	 * @throws IOException
	 */
	public void appendValue(String id, double value, long timestamp, byte state, RecordedDataConfiguration configuration) throws IOException {
		appendValue(id, value, timestamp, state, configuration, false);
	}

	private void appendValue(String id, double value, long timestamp, byte state,
			RecordedDataConfiguration configuration, boolean hasWriteLock) throws IOException {

		long storingPeriod;
		if (configuration.getStorageType().equals(StorageType.FIXED_INTERVAL)) {
			// fixed interval
			storingPeriod = configuration.getFixedInterval();
		}
		else {
			/* flexible interval */
			storingPeriod = FLEXIBLE_STORING_PERIOD;
		}

		FileObject toStoreIn = null;
		id = encodeLabel(id);
		String strDate = getStrDate(timestamp);
		
		final boolean requiresNewFolder;
		if (!hasWriteLock) 
			folderLock.readLock().lock();
		try {
			requiresNewFolder = !openFilesHM.containsKey(id + strDate)|| openFilesHM.get(id + strDate).size() == 0;
			// in this case we need to abort the current operation and start again, this time holding the write lock
			if (requiresNewFolder && !hasWriteLock) {
				hasWriteLock = true; // required to avoid releasing the read lock twice (see finally block)
				folderLock.readLock().unlock();
				folderLock.writeLock().lock();
				try {
					appendValue(id, value, timestamp, state, configuration, true);
				} finally {
					folderLock.writeLock().unlock();
				}
				return;
			}

			/*
			 * If there is no FileObjectList for this folder, a new one will be created. (This will be the first value
			 * stored for this day) Eventually existing FileObjectLists from the day before will be flushed and closed. Also
			 * the Hashtable size will be monitored, to not have too many opened Filestreams.
			 */
			if (requiresNewFolder) {
				deleteEntryFromLastDay(timestamp, id);
				controlHashtableSize();
				FileObjectList first = new FileObjectList(rootNode.getPath() + "/" + strDate + "/" + id, cache, id);
				openFilesHM.put(id + strDate, first);
	
				/*
				 * If FileObjectList for this label does not contain any FileObjects yet, a new one will be created. Data
				 * will be stored and List reloaded for next Value to store.
				 */
				if (first.size() == 0) {
	
					if (configuration.getStorageType().equals(StorageType.FIXED_INTERVAL)) {
						// fixed interval
						toStoreIn = new ConstantIntervalFileObject(rootNode.getPath() + "/" + strDate + "/" + id + "/c"
								+ timestamp + SlotsDb.FILE_EXTENSION, cache.getCache(id, "c" + timestamp  + SlotsDb.FILE_EXTENSION));
					}
					else {
						/* flexible interval */
						toStoreIn = new FlexibleIntervalFileObject(rootNode.getPath() + "/" + strDate + "/" + id + "/f"
								+ timestamp + SlotsDb.FILE_EXTENSION, cache.getCache(id, "f" + timestamp  + SlotsDb.FILE_EXTENSION));
					}
	
					long roundedTimestamp = getRoundedTimestamp(timestamp, configuration);
					toStoreIn.createFileAndHeader(roundedTimestamp, storingPeriod);
					toStoreIn.append(value, roundedTimestamp, state);
					
					toStoreIn.close(); /* close() also calls flush(). */
					
					openFilesHM.get(id + strDate).reLoadFolder(cache, id);
					return;
				}
			}
	
			/*
			 * There is a FileObjectList for this day.
			 */
			FileObjectList listToStoreIn = openFilesHM.get(id + strDate);
			if (listToStoreIn.size() > 0) {
				toStoreIn = listToStoreIn.getCurrentFileObject();
	
				/*
				 * If StartTimeStamp is newer then the Timestamp of the value to store, this value can't be stored.
				 */
				long roundedTimestamp = getRoundedTimestamp(timestamp, configuration);
				if (toStoreIn.getStartTimeStamp() > roundedTimestamp) {
					return;
				}
			}
	
			if (toStoreIn == null) {
				throw new IllegalStateException("could not find log file"); // FIXME
			}
	
			/*
			 * The storing Period may have changed. In this case, a new FileObject must be created.
			 */
			if (toStoreIn.getStoringPeriod() == storingPeriod || toStoreIn.getStoringPeriod() == 0) {
				toStoreIn = openFilesHM.get(id + strDate).getCurrentFileObject();
				long roundedTimestamp = getRoundedTimestamp(timestamp, configuration);
				toStoreIn.append(value, roundedTimestamp, state);
				if (flusher == null) {
					toStoreIn.flush();
				}
				else {
					return;
				}
			}
			else {
				/*
				 * Intervall changed -> create new File (if there are no newer values for this day, or file)
				 */
				if (toStoreIn.getTimestampForLatestValue() < timestamp) {
					if (storingPeriod != FLEXIBLE_STORING_PERIOD) { /* constant intervall */
						toStoreIn = new ConstantIntervalFileObject(rootNode.getPath() + "/" + strDate + "/" + id + "/c"
								+ timestamp + SlotsDb.FILE_EXTENSION, cache.getCache(id, "c" + timestamp  + SlotsDb.FILE_EXTENSION));
					}
					else { /* flexible intervall */
						toStoreIn = new FlexibleIntervalFileObject(rootNode.getPath() + "/" + strDate + "/" + id + "/f"
								+ timestamp + SlotsDb.FILE_EXTENSION, cache.getCache(id, "f" + timestamp  + SlotsDb.FILE_EXTENSION));
					}
					toStoreIn.createFileAndHeader(timestamp, storingPeriod);
					toStoreIn.append(value, timestamp, state);
					if (flusher == null) {
						toStoreIn.flush();
					}
					openFilesHM.get(id + strDate).reLoadFolder(cache, id);
				}
			}
		} finally {
			if (!hasWriteLock)
				folderLock.readLock().unlock();
		}
	}

	/**
	 * Rounds the timestamp to the next matching interval.
	 * 
	 * @param timestamp
	 *            the timestamp to round
	 * @param configuration
	 *            RecordedDataConfiguration
	 * @return
	 */
	public static long getRoundedTimestamp(long timestamp, RecordedDataConfiguration configuration) {

		// FIXME: checking the configuration shouldn't be part of this method. If flexible interval is used, this method
		// shouldn't be even called. Requires major refactoring!

		if (configuration == null) {
			// configuration doesn't exist. assuming flexible interval which needs no rounding
			return timestamp;
		}

		if (configuration.getStorageType() == null) {
			// storage type doesn't exist. assuming flexible interval which needs no rounding
			return timestamp;
		}

		if (configuration.getStorageType().equals(StorageType.FIXED_INTERVAL)) {
			// fixed interval

			long stepInterval = configuration.getFixedInterval();

			if (stepInterval <= 0) {
				throw new IllegalArgumentException("FixedInterval size needs to be greater than 0 ms");
			}
			return getRoundedTimestamp(timestamp, stepInterval);

		}
		else {
			// flexible interval - which needs no rounding
			return timestamp;
		}

	}

	// FIXME shouldn't be accessible from outside. ConstantIntervalFileObjects needs this as workaround.
	public static long getRoundedTimestamp(long timestamp, long stepInterval) {
		long distance = timestamp % stepInterval;
		long diff = stepInterval - distance;
//		if (distance > stepInterval / 2) { // beware of value overflow...
		if ((distance > stepInterval / 2 && Long.MAX_VALUE - timestamp >= diff) || timestamp - distance < Long.MIN_VALUE) { 
			// go up 
			return timestamp - distance + stepInterval;
		}
		else {
			// go down
			return timestamp - distance;
		}
	}

	String encodeLabel(String label) throws IOException {
		String encodedLabel = encodedLabels.get(label);
		if (encodedLabel == null) {
			// encoding should be compatible with usual linux & windows file system file names
			encodedLabel = URLEncoder.encode(label, "UTF-8");
			// should be false in any reasonable system
			boolean use252F = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
				@Override
				public Boolean run() {
					return Boolean.getBoolean("org.ogema.recordeddata.slotsdb.use252F");
				}
			});
					
			if (use252F) {
				encodedLabel = encodedLabel.replace("%2F", "%252F");				
			} else {
				encodedLabel = encodedLabel.replace("%252F", "%2F");				
			}
			encodedLabels.put(label, encodedLabel);
		}
		return encodedLabel;
	}

	//Note: Comprehensive revision to fix the method. The previous version could just find data from the
	//current day
	public SampledValue readNextValue(String label, long t, RecordedDataConfiguration configuration)
			throws IOException {
		long timestamp = getRoundedTimestamp(t, configuration);
		label = encodeLabel(label);
//		List<FileObjectList> days = getFoldersForIntervalSorted(label, timestamp, Long.MAX_VALUE);
//		if(days.isEmpty()) return null;
		FileObjectList folder;
		SampledValue result = null;
		folderLock.readLock().lock();
		try {
			 folder = getNextFolder(label, timestamp);
		
//			List<FileObject> folList = days.get(0).getFileObjectsStartingAt(timestamp);
			if (folder == null)
				return null;
			List<FileObject> folList = folder.getFileObjectsStartingAt(timestamp);
			if(folList.isEmpty()) {
				//check next day // XXX should probably not happen
				folder = getNextFolder(label, folder);
				if (folder == null)
					return null;
				folList = folder.getFileObjectsStartingAt(timestamp);
				if(folList.isEmpty()) {
					return null;
				}
			}
			FileObject toReadFrom = null;
			long minTime = Long.MAX_VALUE;
			for (FileObject toReadFrom2: folList) {
				if (toReadFrom2.startTimeStamp < minTime) {
					minTime = toReadFrom2.startTimeStamp;
					toReadFrom = toReadFrom2;
				}
			}
			//FileObject toReadFrom = folList.get(0).startTimeStamp;
			//FileObject toReadFrom = openFilesHM.get(label + strDate).getFileObjectForTimestamp(timestamp);
			
			if (toReadFrom != null) {
				timestamp = Math.max(timestamp,toReadFrom.getStartTimeStamp());
				result = toReadFrom.read(timestamp);
				if (result == null) {
					result = toReadFrom.readNextValue(timestamp); // null if no value for timestamp
				}
				// is available
			}
		} finally {
			folderLock.readLock().unlock();
		}
		// this can happen if rounding takes place
		if (result != null && result.getTimestamp() < t) {
			long delta = configuration.getFixedInterval();
			if (delta <= 0) 
				throw new IllegalStateException("Fixed interval <= 0");
			timestamp += delta;
			if (timestamp <= t)
				timestamp += delta;
			return readNextValue(label, timestamp, configuration);
		}
		
		return result;

	}
	
	public SampledValue readPreviousValue(String label, long t, RecordedDataConfiguration configuration) throws IOException {

		long timestamp = getRoundedTimestamp(t, configuration);
		label = encodeLabel(label);
		final List<FileObjectList> days;
		SampledValue result= null;
		folderLock.readLock().lock();
		try {
			days = getFoldersForIntervalSorted(label, Long.MIN_VALUE, timestamp);
		
			if (days.isEmpty()) 
				return null;
			List<FileObject> folList = null;
			for (int i=days.size()-1;i>=0;i--) {
				folList = days.get(i).getFileObjectsUntil(timestamp);
				if (!folList.isEmpty())
					break;
			}
			if (folList == null || folList.isEmpty())
				return null;
			FileObject toReadFrom = null;
			long maxTime = Long.MIN_VALUE;
			for(FileObject toReadFrom2: folList) {
				if (toReadFrom2.startTimeStamp > maxTime) {
					maxTime = toReadFrom2.startTimeStamp;
					toReadFrom = toReadFrom2;
				}
			}
			if (toReadFrom != null) {
				timestamp = Math.min(timestamp,toReadFrom.getTimestampForLatestValue());
				result = toReadFrom.read(timestamp);
				if (result == null) { 
					result = toReadFrom.readPreviousValue(timestamp); // null if no value for timestamp
				}
				// is available
			}
		} finally {
			folderLock.readLock().unlock();
		}
		// this can happen if rounding takes place
		if (result != null && result.getTimestamp() > t) {
			long delta = configuration.getFixedInterval();
			if (delta <= 0) 
				throw new IllegalStateException("Fixed interval <= 0");
			timestamp = timestamp- delta;
			if (timestamp >= t)
				timestamp =timestamp- delta;
			return readNextValue(label, timestamp, configuration);
		}
		return result;

	}
	
	// requires folder read lock
	private final FileObjectList getFileObjectList(final String label, final long timestamp) throws IOException {
		final FileObjectList fol = openFilesHM.get(label + getStrDate(timestamp));
		if (fol != null)
			return fol;
		final List<FileObjectList> fols = getFoldersForIntervalSorted(label, timestamp, timestamp);
		if (fols != null && !fols.isEmpty())
			return fols.get(0);
		return null;
	}

	public SampledValue read(String label, long timestamp, RecordedDataConfiguration configuration) throws IOException {
		// label = URLEncoder.encode(label,Charset.defaultCharset().toString());
		// //encodes label to supported String for Filenames.

		timestamp = getRoundedTimestamp(timestamp, configuration);

		label = encodeLabel(label);

		// XXX we cannot do this in a method which must only hold the read lock; why is it here, but not in the other read methods?
//		if (!openFilesHM.containsKey(label + strDate)) {
//			controlHashtableSize();
//			FileObjectList fol = new FileObjectList(rootNode.getPath() + "/" + strDate + "/" + label);
//			openFilesHM.put(label + strDate, fol);
//		}
		final FileObject toReadFrom;
		folderLock.readLock().lock();
		try {
			final FileObjectList fol = getFileObjectList(label, timestamp);
			if (fol == null)
				return null;
			toReadFrom = fol.getFileObjectForTimestamp(timestamp);
			if (toReadFrom != null) {
				return toReadFrom.read(timestamp); // null if no value for timestamp
				// is available
			}
		} finally {
			folderLock.readLock().unlock();
		}
		return null;
	}
	
	// requires folder read lock 
	private List<FileObjectList> getFoldersForIntervalSorted(String label, long start, long end) throws IOException {
		List<FileObjectList> days = new Vector<FileObjectList>();
		/*
		 * Check for Folders matching criteria: Folder contains data between start & end timestamp. Folder contains
		 * label.
		 */
		String strSubfolder;
		for (File folder : rootNode.listFiles()) {
			if (folder.isDirectory()) {
				if (isFolderBetweenStartAndEnd(folder.getName(), start, end)) {
					if (Arrays.asList(folder.list()).contains(label)) {
						strSubfolder = rootNode.getPath() + "/" + folder.getName() + "/" + label;
						days.add(new FileObjectList(strSubfolder, cache, label));
						logger.trace(strSubfolder + " contains " + SlotsDb.FILE_EXTENSION + " files to read from.");
					}
					else if (Arrays.asList(folder.list()).contains(URLEncoder.encode(label,"UTF-8"))) {
						strSubfolder = rootNode.getPath() + "/" + folder.getName() + "/" + URLEncoder.encode(label,"UTF-8");
						days.add(new FileObjectList(strSubfolder, cache, label));
						logger.trace(strSubfolder + " contains " + SlotsDb.FILE_EXTENSION + " files to read from.");
					}
				}
			}
		}
		/*
		 * Sort days, because rootNode.listFiles() is unsorted. FileObjectLists MUST be sorted, otherwise data
		 * output wouldn't be sorted.
		 */
		Collections.sort(days, new Comparator<FileObjectList>() {

			@Override
			public int compare(FileObjectList f1, FileObjectList f2) {
				return Long.valueOf(f1.getFirstTS()).compareTo(f2.getFirstTS());
			}
		});

		return days;
	}
	
	
	// requires folder read lock
	// label must be encoded already
	FileObjectList getNextFolder(String label, long start) throws IOException {
		/*
		 * Check for Folders matching criteria: Folder contains data between start & end timestamp. Folder contains
		 * label.
		 */
		String strSubfolder;
		List<File> folders = Arrays.asList(rootNode.listFiles());
		Collections.sort(folders);
		// TODO avoid iteration over all folders, use heuristics to guess start element, in case of large lists of folders
		for (File folder : folders) {
			if (folder.isDirectory()) {
				if (isFolderBetweenStartAndEnd(folder.getName(), start, Long.MAX_VALUE)) {
					if (Arrays.asList(folder.list()).contains(label)) {
						strSubfolder = rootNode.getPath() + "/" + folder.getName() + "/" + label;
						if (logger.isTraceEnabled())
							logger.trace(strSubfolder + " contains " + SlotsDb.FILE_EXTENSION + " files to read from.");
						return new FileObjectList(strSubfolder, cache, label);
					}
					else if (Arrays.asList(folder.list()).contains(URLEncoder.encode(label,"UTF-8"))) {
						strSubfolder = rootNode.getPath() + "/" + folder.getName() + "/" + URLEncoder.encode(label,"UTF-8");
						if (logger.isTraceEnabled())
							logger.trace(strSubfolder + " contains " + SlotsDb.FILE_EXTENSION + " files to read from.");
						return new FileObjectList(strSubfolder, cache, label);
					}
				}
			}
		}
		return null;
	}

	// requires folder read lock 
	// label must be encoded
	FileObjectList getNextFolder(String label, FileObjectList folder) throws IOException {
		String currentDate = folder.getFolderName();
		int lastIdx = currentDate.lastIndexOf('/');
		if (lastIdx < 0)
			lastIdx = currentDate.lastIndexOf('\\');
		if (lastIdx < 0) // XXX isn't his an error?
			return null;
		currentDate = currentDate.substring(0, lastIdx);
		Path current = Paths.get(currentDate);
		
		List<File> folders = Arrays.asList(rootNode.listFiles());
		Collections.sort(folders);
		File f;
		int i = Integer.MIN_VALUE;
		for (int idx = 0; idx < folders.size(); idx++) {
			f = folders.get(idx);
			if (Files.isSameFile(current, Paths.get(f.toString()))) {
				i = idx;
				break;
			}
		}
		if (i < 0)
			return null;
		for (int j=i+1;j<folders.size();j++) {
			f = folders.get(j);
			if (f.isDirectory() && Arrays.asList(f.list()).contains(label)) {
				String strSubfolder = rootNode.getPath() + "/" + f.getName() + "/" + label;
				if (logger.isTraceEnabled())
					logger.trace(strSubfolder + " contains " + SlotsDb.FILE_EXTENSION + " files to read from.");
				return new FileObjectList(strSubfolder, cache, label);
			} else if (f.isDirectory() && Arrays.asList(f.list()).contains(URLEncoder.encode(label,"UTF-8"))) {
				String strSubfolder = rootNode.getPath() + "/" + f.getName() + "/" + URLEncoder.encode(label,"UTF-8");
				if (logger.isTraceEnabled())
					logger.trace(strSubfolder + " contains " + SlotsDb.FILE_EXTENSION + " files to read from.");
				return new FileObjectList(strSubfolder, cache, label);
			}
		}
		
		return null;
		
	}
	
	static List<SampledValue> readFolder(FileObjectList folder) throws IOException {
		if (folder.size() == 1)
			return folder.getAllFileObjects().get(0).readFully();
		List<SampledValue> values = new ArrayList<>();
		for (FileObject fo : folder.getAllFileObjects()) {
			values.addAll(fo.readFully());
		}
		return values;
	}
	
	/*
	 * Note: if start is too small (< minL) or end is too large (end > maxL), this fails to work.
	 * Essentially, the year must be a positive number with at most 4 digits.
	 */
	public List<SampledValue> read(String label, long start, long end,
			RecordedDataConfiguration configuration) throws IOException {
		if (logger.isTraceEnabled()) {
			logger.trace("Called: read(" + label + ", " + start + ", " + end + ")");
		}

		start = getRoundedTimestamp(start, configuration);
		end = getRoundedTimestamp(end, configuration);

		List<SampledValue> toReturn = new Vector<SampledValue>();

		if (start > end) {
			logger.trace("Invalid Read Request: startTS > endTS");
			return toReturn;
		}

		if (start == end) {
			toReturn.add(read(label, start, configuration)); // let other read function handle.
			toReturn.removeAll(Collections.singleton(null));
			return toReturn;
		}
/*		if (end > 50000000000000L) { // to prevent buffer overflows. in cases of multiplication -> see checkForExtremeValues below
			end = 50000000000000L;
		} 
*/
		// label = URLEncoder.encode(label,Charset.defaultCharset().toString());
		// //encodes label to supported String for Filenames.
		label = encodeLabel(label);
		
		// ensure year strings have no more than four digits,
		// otherwise we run into problems...
		start = checkForExtremeValues(start);
		end = checkForExtremeValues(end);

		String strStartDate = getStrDate(start);
		String strEndDate = getStrDate(end);
		List<FileObject> toRead = new Vector<FileObject>();

		folderLock.readLock().lock();
		try {
			if (!strStartDate.equals(strEndDate)) {
				logger.trace("Reading Multiple Days. Scanning for Folders.");
				List<FileObjectList> days = getFoldersForIntervalSorted(label, start, end);
	
				/*
				 * Create a list with all file-objects that must be read for this reading request.
				 */
				if (days.size() == 0) {
					return toReturn;
				}
				else if (days.size() == 1) {
					toRead.addAll(days.get(0).getFileObjectsFromTo(start, end));
				}
				else { // days.size()>1
					toRead.addAll(days.get(0).getFileObjectsStartingAt(start));
					for (int i = 1; i < days.size() - 1; i++) {
						toRead.addAll(days.get(i).getAllFileObjects());
					}
					toRead.addAll(days.get(days.size() - 1).getFileObjectsUntil(end));
				}
				toRead.removeAll(Collections.singleton(null));
			}
			else { // Start == End Folder -> only 1 FileObjectList must be read.
				File folder = new File(rootNode.getPath() + "/" + strStartDate + "/" + label);
				FileObjectList fol;
				if (folder.list() != null) {
					if (folder.list().length > 0) { // Are there Files in the
						// folder, that should be read?
						fol = new FileObjectList(rootNode.getPath() + "/" + strStartDate + "/" + label, cache, label);
						toRead.addAll(fol.getFileObjectsFromTo(start, end));
					}
				}
			}
		
			logger.trace("Found " + toRead.size() + " " + SlotsDb.FILE_EXTENSION + " files to read from.");
	
			/*
			 * Read all FileObjects: first (2nd,3rd,4th....n-1) last first and last will be read separately, to not exceed
			 * timestamp range.
			 */
			if (toRead != null) {
				if (toRead.size() > 1) {
					toReturn.addAll(toRead.get(0).read(start, toRead.get(0).getTimestampForLatestValue()));
					toRead.get(0).close();
					for (int i = 1; i < toRead.size() - 1; i++) {
						toReturn.addAll(toRead.get(i).readFully());
						toRead.get(i).close();
					}
					toReturn.addAll(toRead.get(toRead.size() - 1).read(toRead.get(toRead.size() - 1).getStartTimeStamp(),
							end));
					toRead.get(toRead.size() - 1).close();
	
					/*
					 * Some Values might be null -> remove
					 */
					toReturn.removeAll(Collections.singleton(null));
	
				}
				else if (toRead.size() == 1) { // single FileObject
					toReturn.addAll(toRead.get(0).read(start, end));
					toReturn.removeAll(Collections.singleton(null));
				}
			}
		} finally {
			folderLock.readLock().unlock();
		}
		logger.trace("Selected " + SlotsDb.FILE_EXTENSION + " files contain " + toReturn.size() + " Values.");
		return toReturn;
	}

	/**
	 * Parses a Timestamp in Milliseconds from a String in yyyyMMdd Format <br>
	 * e.g.: 25.Sept.2011: 20110925 <br>
	 * would return: 1316901600000 ms. equal to (25.09.2011 - 00:00:00) <br>
	 * 
	 * @param name
	 *            in "yyyyMMdd" Format
	 * @param start
	 * @param end
	 * @return
	 * @throws IOException
	 */
	private static boolean isFolderBetweenStartAndEnd(String name, long start, long end) throws IOException {
		final SimpleDateFormat sdf = getDateFormat();
		try {
			sdf.parse(name);
		} catch (ParseException e) {
			logger.error("Unable to parse Timestamp from: " + name + " folder. " + e.getMessage());
		}
		if (start <= sdf.getCalendar().getTimeInMillis() + 86399999 && sdf.getCalendar().getTimeInMillis() <= end) { // if
			// start
			// <=
			// folder.lastTSofDay
			// &&
			// folder.firstTSofDay
			// <=
			// end
			return true;
		}
		return false;
	}

	/*
	 * strCurrentDay holds the current Day in yyyyMMdd format, because SimpleDateFormat uses a lot cpu-time.
	 * currentDayFirstTS and ... currentDayLastTS mark the first and last timestamp of this day. If a TS exceeds this
	 * range, strCurrentDay, currentDayFirstTS, currentDayLastTS will be updated.
	 */
	private String getStrDate(long timestamp) throws IOException {
		synchronized (currentDayLock) {
			if (strCurrentDay != null) {
				if (timestamp >= currentDayFirstTS && timestamp <= currentDayLastTS) {
					return strCurrentDay;
				}
			}
			final SimpleDateFormat sdf = getDateFormat();
			/*
			 * timestamp for other day or not initialized yet.
			 */
			date.setTime(timestamp);
			strCurrentDay = sdf.format(date);
			
			try {
				currentDayFirstTS = sdf.parse(strCurrentDay).getTime();
			} catch (ParseException e) {
				logger.error("Unable to parse Timestamp from: " + currentDayFirstTS + " String.");
			}
			currentDayLastTS = currentDayFirstTS + 86399999;
			return strCurrentDay;
		}
	}

	/** 
	 * requires folder write lock
	 */
	private void deleteEntryFromLastDay(long timestamp, String label) throws IOException {
		String strDate = getStrDate(timestamp - 86400000);
		if (openFilesHM.containsKey(label + strDate)) {
			/*
			 * Value for new day has been registered! Close and flush all connections! Empty Hashtable!
			 */
			clearOpenFilesHashMap();
			logger.info("Started logging to a new Day. <" + strDate
					+ "> Folder has been closed and flushed completely.");
			/* reload days */
			loadDays();
		}
	}

	/** 
	 * requires folder write lock
	 */
	private void clearOpenFilesHashMap() throws IOException {
		Iterator<FileObjectList> itr = openFilesHM.values().iterator();
		while (itr.hasNext()) { // kick out everything
			itr.next().closeAllFiles();
		}
		openFilesHM.clear();;
	}
	
	public int size(String label, long start, long end) throws DataRecorderException, IOException {
		label = encodeLabel(label);
		int size = 0;
		folderLock.readLock().lock();
		try {
			List<FileObjectList> folders = getFoldersForIntervalSorted(label, start, end);
			for (FileObjectList folder: folders) {
				for (FileObject file: folder.getAllFileObjects()) {
					size += file.getDataSetCount(start, end);
				}
			}
			return size;
		} catch (IOException e) {
			throw new DataRecorderException("",e);
		} finally {
			folderLock.readLock().unlock();
		}
	}

	/** 
	 * requires folder write lock 
	 */
	private void controlHashtableSize() throws IOException {
		/*
		 * hm.size() doesn't really represent the number of open files, because it contains FileObjectLists, which may
		 * contain 1 ore more FileObjects. In most cases, there is only 1 File in a List. There will be a second File if
		 * storage Intervall is reconfigured. Continuous reconfiguring of measurement points may lead to a
		 * "Too many open files" Exception. In this case SlotsDb.MAX_OPEN_FOLDERS should be decreased...
		 */
		if (openFilesHM.size() > max_open_files) {
			logger.debug("More then " + max_open_files
					+ " DataStreams are opened. Flushing and closing some to not exceed OS-Limit.");
			Iterator<FileObjectList> itr = openFilesHM.values().iterator();
			for (int i = 0; i < (max_open_files / 5); i++) { // randomly kick
				// out some of
				// the
				// FileObjectLists.
				// -> the needed
				// ones will be
				// reinitialized,
				// no problem
				// here.
				itr.next().closeAllFiles();
				itr.remove();
			}
		}
	}

	/**
	 * Flushes all Datastreams from all FileObjectLists and FileObjects
	 * 
	 * @throws IOException
	 */
	public void flush() throws IOException {
		folderLock.writeLock().lock();
		try {
			Iterator<FileObjectList> itr = openFilesHM.values().iterator();
			while (itr.hasNext()) {
				itr.next().flush();
			}
			logger.debug("Data from {} folders flushed to disk.",openFilesHM.size());
		} finally {
			folderLock.writeLock().unlock();
		}
	}
	
	// ensure year strings have no more than four digits and are positive
	private static long checkForExtremeValues(long timestamp) {
		if (timestamp < minL)
			timestamp = minL;
		else if (timestamp > maxL)
			timestamp = maxL;
		return timestamp;
	}
}
