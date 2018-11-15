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
package org.ogema.impl.persistence;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.impl.persistence.TimedPersistence.Change;
import org.ogema.persistence.DBConstants;
import org.ogema.persistence.PersistencePolicy.ChangeInfo;
import org.slf4j.Logger;

public class DBResourceIO {

	private final Logger logger = org.slf4j.LoggerFactory.getLogger("persistence");

	PersistentFileSet resDataFiles, dirFiles;
	int dirFileSuffix;

	final ConcurrentHashMap<Integer, Integer> offsetByID;
	int dbFileInitialOffset;
	final MapValueSorter sorter;

	final ConcurrentHashMap<String, TreeElementImpl> unsortedParents;
	final ConcurrentHashMap<String, TreeElementImpl> unsortedRefs;
	final ConcurrentHashMap<String, TreeElementImpl> unloadableCustomResources;
	// final Set<TreeElementImpl> resourceLists;

	private final ResourceDBImpl database;

	private int maxID;

	/**
	 * Path and file name informations for the archives containing the persistent data. The values of these field are
	 * read from system properties. If the appropriate properties are not set, the default values from DBConstans are
	 * used.
	 */
	final String dbPathName;
	final String dbResourcesFileName;
	final String dbDirFileName;

	// String currentDataFileName;

	private static final int CHANGES_BUFFER_SIZE = 128;
	private static final int DEFAULT_MIN_COMPACTION_FILE_SIZE = 1020 * 1024; // 1MB
	private static final float DEFAULT_MIN_COMPACTION_GARBAGE_SIZE = .75f; // 75%

	private int garbage;

	Queue<Change> fifo = new ArrayDeque<Change>(CHANGES_BUFFER_SIZE);

	final ConcurrentHashMap<Integer, Change> changes;

	private int minimunCompationFileSize;
	private float minimumCompationGarbageSize;

	MapFile mapFile;

	DataFile dataFile;

	private boolean parsed;

	private static String currentPath;

	class MapValueSorter implements Comparator<Integer> {

		Map<Integer, Integer> base;

		public MapValueSorter(Map<Integer, Integer> base) {
			this.base = base;
		}

		public int compare(Integer a, Integer b) {
			int compare = base.get(a).compareTo(base.get(b));
			if (compare == 0) {
				return -1;
			}
			return compare;
		}
	}

	DBResourceIO(ResourceDBImpl db) {
		unsortedParents = new ConcurrentHashMap<>();
		unsortedRefs = new ConcurrentHashMap<>();
		unloadableCustomResources = new ConcurrentHashMap<>();
		// resourceLists = new HashSet<>();

		offsetByID = new ConcurrentHashMap<Integer, Integer>();
		sorter = new MapValueSorter(offsetByID);
		this.database = db;

		this.changes = new ConcurrentHashMap<>(CHANGES_BUFFER_SIZE);

		// check if the property to activate persistence debugging is set
		String persDebug = System.getProperty(DBConstants.PROP_NAME_PERSISTENCE_DEBUG, "false");
		if (persDebug.equals("true"))
			Configuration.LOGGING = true;
		// check if the minimum compaction file size is set
		String filesize = System.getProperty(DBConstants.PROP_NAME_PERSISTENCE_COMPACTION_START_SIZE, null);
		try {
			minimunCompationFileSize = Integer.valueOf(filesize) << 10; // KB to Bytes -> * 1024
		} catch (NumberFormatException e) {
			minimunCompationFileSize = DEFAULT_MIN_COMPACTION_FILE_SIZE;
		}
		// check if the minimum amount of garbage size is set that triggers compaction process.
		String garbagesize = System.getProperty(DBConstants.PROP_NAME_PERSISTENCE_COMPACTION_START_SIZE_GARBAGE, null);
		try {
			minimumCompationGarbageSize = Integer.valueOf(garbagesize) / 100;
			if (minimumCompationGarbageSize < .10f)
				minimumCompationGarbageSize = .1f; // not less than 10%
			if (minimumCompationGarbageSize > .90f)
				minimumCompationGarbageSize = .9f; // not higher than 90%
		} catch (NumberFormatException e) {
			minimumCompationGarbageSize = DEFAULT_MIN_COMPACTION_GARBAGE_SIZE;
		}
		/*
		 * Get the application specific file locations
		 */
		dbPathName = System.getProperty(DBConstants.DB_PATH_PROP, DBConstants.DB_PATH_NAME);
		currentPath = dbPathName;
		dbResourcesFileName = DBConstants.RESOURCES_ARCHIVE_NAME;
		dbDirFileName = DBConstants.DIR_FILE_NAME;

		parsed = false;
	}

	void initFiles() {
		File dir = new File(dbPathName);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		/*
		 * Initialize directory file
		 */
		dirFiles = new PersistentFileSet();
		dirFiles.initFiles(dir, dbDirFileName);
		/*
		 * Initialize resource archive file
		 */
		resDataFiles = new PersistentFileSet();
		resDataFiles.initFiles(dir, dbResourcesFileName);

		/*
		 * Check the integrity of the resource data files
		 */
		postInitFiles();

		/*
		 * If no valid map file is there but some valid data files, the biggest data file is parsed completely without
		 * map file.
		 */
		if (mapFile == null) {
			File f = resDataFiles.getBiggest();
			if (f != null) {
				resDataFiles.fileNew = f;
				dataFile = new DataFile(f);
				emergencyParser(dataFile.raf);
				dataFile.close();
				// dataFile = null;
			}
		}

		if (dataFile == null) {
			resDataFiles.shiftF();
			dataFile = new DataFile(resDataFiles.fileNew, true);
		}

		dataFile.initOutput();

		if (resDataFiles.fileNew == null)
			dbFileInitialOffset = 0;
		else
			dbFileInitialOffset = (int) resDataFiles.fileNew.length();
	}

	private void emergencyParser(RandomAccessFile raf) {
		logger.debug("EmergencyParser parses Resources...");

		// read the entries
		this.garbage = 0;
		try {
			raf.seek(0);
			while (true) {
				int offset = (int) raf.getFilePointer();
				TreeElementImpl node = readEntry(raf);
				int key = node.resID;
				offsetByID.put(key, offset);
			}
		} catch (IOException e) {
			if (Configuration.LOGGING)
				logger.debug("...Resources parsing aborted with exception");
		}
		database.nextresourceID = maxID + 1;

		postProcess();

		if (Configuration.LOGGING)
			logger.debug("...Resources parsed");
		try {
			if (raf != null)
				raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		parsed = true;
	}

	private void postInitFiles() {
		/*
		 * Case 1: no map file exists. This is the case if its the initial run or the last run was initial run and no
		 * storage period is successfully terminated. If no directory file exists, resource data file is unusable and
		 * can be deleted.
		 */
		if (dirFiles.fileNew == null && dirFiles.fileOld == null) {
			resDataFiles.backup();
		}
		/*
		 * Case 2: only one map file exists. This occurs if only one storage period appears after an initial run. The
		 * map file should be consistent in order to be usable with the appropriate data file.
		 */
		else if (dirFiles.fileNew != null && dirFiles.fileOld == null) {
			File newestMap = dirFiles.fileNew;
			MapFile mf = new MapFile(newestMap);
			String dataName = mf.dataFileName;
			DataFile df = verifyDataFile(mf, dataName);
			if (!mf.isValid() || df == null) {
				// No valid file set exist, backup the files and begin a new file set when the storage period starts.
				mf.close();
				dirFiles.backup();
				if (df != null) {
					df.close();
					resDataFiles.backup();
				}
				return;
			}
			else {
				resDataFiles.setAsCurrentFile(dataName); // This will not make any changes, but for the sake of
															// completeness.
				this.mapFile = mf;
				this.dataFile = df;
			}
		}
		/*
		 * Case 3: two or more map files exist. This is the most general case. One or two data files could be there.
		 */
		else if (dirFiles.fileNew != null && dirFiles.fileOld != null) {
			File newestMap = dirFiles.fileNew;
			MapFile mf = new MapFile(newestMap);
			String dataName = mf.dataFileName;
			DataFile df = null;

			if (mf.isValid()) {
				df = verifyDataFile(mf, dataName);
				if (df != null) {
					// success
					resDataFiles.setAsCurrentFile(dataName);
					this.mapFile = mf;
					this.dataFile = df;
					return;
				}
			}

			if (!mf.isValid() || df == null) {
				// The newest map file is invalid, try the older one.
				mf.close();
				dirFiles.shiftB();
				newestMap = dirFiles.fileNew;
				mf = new MapFile(newestMap);
				dataName = mf.dataFileName;
				if (mf.isValid()) {
					// Valid map file detected, look for its data file
					df = verifyDataFile(mf, dataName);
					if (df != null) {
						// success
						resDataFiles.setAsCurrentFile(dataName);
						this.mapFile = mf;
						this.dataFile = df;
					}
				}
				else
					mf.close();
			}

			if (this.mapFile == null) {
				// Both map files are invalid, backup the files and begin an new file set when the storage
				// period
				// starts.
				// resDataFiles.backup();
				dirFiles.backup();
			}
		}
	}

	private DataFile verifyDataFile(MapFile map, String dataName) {
		File dataFile = resDataFiles.getFileByName(dataName);
		if (dataFile == null)
			return null;
		DataFile df = new DataFile(dataFile);
		this.dataFile = df;
		@SuppressWarnings("unused")
		RandomAccessFile ram = map.raf;
		int offset = map.getLastOffset();
		int dataFileLength = (int) dataFile.length();
		if (dataFileLength <= offset)
			return null;
		try {
			df.raf.seek(offset);
			tryReadEntry(df.raf);
		} catch (Exception e) {
			df.close();
			return null; // In this case resource data file is smaller than it's expected. The resource tree could be
							// created in part only.
		}
		return df;
	}

	void setChValue(int ch) {
		try {
			dataFile.out.writeChar(ch);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param typeKey
	 *            the typeKey to set
	 */
	void setTypeKey(int typeKey) {
		try {
			dataFile.out.write(typeKey);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void setByte(int flags) {

		try {
			dataFile.out.write(flags);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setUTF8(String name) {
		try {
			/*
			 * If the string is null wrtiteUTF throws an exception. In order to encode this case correctly in the
			 * database -1 is written instead of the UTF-8 string. During the parse process this case is to be
			 * evaluated.
			 */
			if (name == null) {
				dataFile.out.writeShort(-1);
				return;
			}
			dataFile.out.writeUTF(name);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setBValue(boolean b) {
		try {
			dataFile.out.writeBoolean(b);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setFValue(float f) {
		try {
			dataFile.out.writeFloat(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setIValue(int i) {
		try {
			dataFile.out.writeInt(i);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setJValue(long l) {
		try {
			dataFile.out.writeLong(l);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void storeHeader(TreeElementImpl node) {
		String tmpstr;
		int tmpint = 0;
		/* 1. Set type ID */
		tmpstr = node.typeName;
		if (node.complexArray && tmpstr == null)
			node.typeName = tmpstr = DBConstants.CLASS_COMPLEX_ARR_NAME;
		setUTF8(tmpstr);
		tmpint += tmpstr.length();
		// 2. set resource ID
		setIValue(node.resID);
		// 3. set resources parent ID
		setIValue(node.parentID);
		// 4. setFlags
		setByte(node.getFlags());
		// 5. setTypeKey
		int typeKey = node.typeKey;
		setByte(typeKey);
		tmpint += 10;
		// 6. set name string
		tmpstr = node.path;
		setUTF8(tmpstr);
		tmpint += tmpstr.length();
		// 7. set the owner app id
		tmpstr = node.appID;
		setUTF8(tmpstr);
		tmpint += tmpstr.length();
		// 8. set the id of the referenced node. If the node doesn't reference any node, this field is just 0
		setIValue(node.refID);
		tmpint += 4;
		// 9. set the lastmodified time stamp.
		setJValue(node.lastModified);
		tmpint += 8;
		// update footprint info
		node.footprint += tmpint;
	}

	boolean check4Compaction() {
		int currentFileLength = getCurrentOffset();
		if (currentFileLength == -1)
			return false;
		if (currentFileLength > minimunCompationFileSize
				&& (garbage > currentFileLength * minimumCompationGarbageSize)) {
			dataFile.close();
			resDataFiles.shiftF();
			dataFile = new DataFile(resDataFiles.fileNew, true);
			dbFileInitialOffset = 0;
			compact();
			return true;
		}
		else {
			return false;
		}

	}

	void compact() {
		boolean fileChanged = false;
		/*
		 * Changes can be cleared, because all of the resources which are alive are to be stored into the new file.
		 */
		changes.clear();
		fifo.clear();
		offsetByID.clear();
		Set<Entry<Integer, TreeElementImpl>> tlrs = database.resNodeByID.entrySet();
		for (Map.Entry<Integer, TreeElementImpl> entry : tlrs) {

			TreeElementImpl e = entry.getValue();
			storeResource(e);
			fileChanged = true;
		}
		if (fileChanged) {
			writeEntry();
			updateDirectory();
		}
		garbage = 0;
	}

	void storeResource(TreeElementImpl node) {
		// The old content is now garbage
		garbage += node.footprint;

		if (Configuration.LOGGING)
			logger.debug("Store Resource " + node.path);
		/*
		 * Determine the offset of the resource data in the archive and put it in the map of offsets.
		 */
		int offset = getCurrentOffset();
		offsetByID.put(node.resID, offset);
		// 1. store entry header
		storeHeader(node);
		int typeKey = node.typeKey;

		/*
		 * Get the simple value reference of the node. Note that the node is represents a complex resource or a
		 * reference if its simple value is null.
		 */
		LeafValue value = node.simpleValue;

		int length = 0;
		// 2. set the resource value(s) for the simple types or simple type
		// arrays if they aren't a reference.
		if (!node.reference)
			switch (typeKey) {
			case DBConstants.TYPE_KEY_BOOLEAN:
				setBValue(value.Z);
				node.footprint += 1;
				break;
			case DBConstants.TYPE_KEY_FLOAT:
				setFValue(value.F);
				node.footprint += 4;
				break;
			case DBConstants.TYPE_KEY_INT:
				setIValue(value.I);
				node.footprint += 4;
				break;

			case DBConstants.TYPE_KEY_LONG:
				setJValue(value.J);
				node.footprint += 8;
				break;

			case DBConstants.TYPE_KEY_STRING:
				setUTF8(value.S);
				node.footprint += value.footprint;
				break;
			// set Primitive array resource values
			case DBConstants.TYPE_KEY_BOOLEAN_ARR:
				boolean zArr[] = value.aZ;
				length = value.getArrayLength();
				setIValue(length);
				if (zArr != null)
					for (boolean b : zArr) {
						setBValue(b);
					}
				node.footprint += value.footprint;
				break;
			case DBConstants.TYPE_KEY_FLOAT_ARR:
				float fArr[] = value.aF;
				length = value.getArrayLength();
				setIValue(length);
				if (fArr != null)
					for (float f : fArr) {
						setFValue(f);
					}
				node.footprint += value.footprint;
				break;
			case DBConstants.TYPE_KEY_INT_ARR:
				int iArr[] = value.aI;
				length = value.getArrayLength();
				setIValue(length);
				if (iArr != null)
					for (int I : iArr) {
						setIValue(I);
					}
				node.footprint += value.footprint;
				break;
			case DBConstants.TYPE_KEY_LONG_ARR:
				long jArr[] = value.aJ;
				length = value.getArrayLength();
				setIValue(length);
				if (jArr != null)
					for (long l : jArr) {
						setJValue(l);
					}
				node.footprint += value.footprint;
				break;
			case DBConstants.TYPE_KEY_STRING_ARR:
				String sArr[] = value.aS;
				length = value.getArrayLength();
				setIValue(length);
				if (sArr != null)
					for (String s : sArr) {
						setUTF8(s);
					}
				node.footprint += value.footprint;
				break;
			case DBConstants.TYPE_KEY_OPAQUE:
				byte bArr[] = value.aB;
				length = value.getArrayLength();
				setIValue(length);
				if (bArr != null)
					for (byte b : bArr) {
						setByte(b);
					}
				node.footprint += value.footprint;
				break;
			// set complex childs
			case DBConstants.TYPE_KEY_COMPLEX:
				break;
			// set complex array elements
			case DBConstants.TYPE_KEY_COMPLEX_ARR:
				break;
			default:
				break;
			}
	}

	/**
	 * Read the persistent data of the resources and setup the resource tree.
	 * 
	 */
	public void parseResources() {
		if (parsed || (mapFile == null))
			return;
		/*
		 * First read the directory structure
		 */
		if (Configuration.LOGGING)
			logger.debug("Parse Resources...");

		RandomAccessFile dirRaf = mapFile.raf;
		// read the entries
		RandomAccessFile dataRaf = dataFile.raf;
		this.garbage = 0;
		int endGarbage = 0, beginGarbage = 0;
		if (dirRaf != null) {
			try {
				dirRaf.seek(0);
				int dirEntryCount = mapFile.entryCount;
				while (dirEntryCount > 0) {
					int key = dirRaf.readInt();
					int value = dirRaf.readInt();
					dataRaf.seek(value);
					endGarbage = (int) dataRaf.getFilePointer();
					int tmpGarbage = endGarbage - beginGarbage;
					if (tmpGarbage > 0)
						garbage += tmpGarbage;
					readEntry(dataRaf);
					beginGarbage = (int) dataRaf.getFilePointer();
					offsetByID.put(key, value);
					dirEntryCount--;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		database.nextresourceID = maxID + 1;

		postProcess();
		if (Configuration.LOGGING)
			logger.debug("...Resources parsed");
		try {
			if (dataRaf != null)
				dataRaf.close();
			if (dirRaf != null)
				dirRaf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void postProcess() {
		/*
		 * Place the unsorted node in the tree.
		 */
		handleUnsorted(unsortedParents);
		handleUnsorted(unsortedRefs);

		// all of the remaining unsorted node can be added to the UCR (unloadable custom resources) list
		unloadableCustomResources.putAll(unsortedParents);
		unloadableCustomResources.putAll(unsortedRefs);
		// let gc get rid of the unsorted maps
		unsortedParents.clear();
		unsortedRefs.clear();
	}

	/**
	 * When database starts up, the persistent stored resource information are parsed and the resource tree is set up.
	 * During parsing of the persistent data sub resources may be parsed before their parent resources. In these cases
	 * the nodes of the sub resources are collected in a separate map which is to be processed at the end of parse
	 * process.
	 * 
	 * @param map
	 */
	private void handleUnsorted(Map<String, TreeElementImpl> map) {
		int countBefore, countAfter;
		do {
			Set<Entry<String, TreeElementImpl>> tlrs = map.entrySet();
			countBefore = map.size();
			if (countBefore == 0)
				break;
			for (Map.Entry<String, TreeElementImpl> entry : tlrs) {
				TreeElementImpl e = entry.getValue();
				if (putResource(e))
					map.remove(e.path);
			}
			countAfter = map.size();
			if (countBefore == countAfter)
				break; // This means there are undeleted nodes and they couldn't be sorted into the tree. This could
						// happen when unloadable custom resources exist.
		} while (countBefore > 0);
	}

	/**
	 * Read a resource from data base file into an resolved tree.
	 * 
	 * @param type
	 * @param ifaces
	 * @return
	 */
	TreeElementImpl readEntry(RandomAccessFile raf) throws IOException, EOFException {
		boolean clsLoaded = true;
		@SuppressWarnings("unused")
		// 1. read header of the entry
		TreeElementImpl node = new TreeElementImpl(database);
		readHeader(node, raf);
		// if the node is a reference no own value container is needed
		int typeKey = node.typeKey;
		setNodeType(node);
		// check if the resource is a simple or a complex one
		if (!node.isReference()) {
			node.initDataContainer();
			switch (typeKey) {
			// read simple resource
			case DBConstants.TYPE_KEY_BOOLEAN:
				node.simpleValue.Z = raf.readBoolean();
				break;
			case DBConstants.TYPE_KEY_FLOAT:
				node.simpleValue.F = raf.readFloat();
				break;
			case DBConstants.TYPE_KEY_INT:
				node.simpleValue.I = raf.readInt();
				break;
			case DBConstants.TYPE_KEY_STRING:
				if (isNullString(raf))
					node.simpleValue.S = null;
				else
					node.simpleValue.S = raf.readUTF();
				break;
			case DBConstants.TYPE_KEY_LONG:
				node.simpleValue.J = raf.readLong();
				break;
			// read array resource
			case DBConstants.TYPE_KEY_OPAQUE:
				readAB(node);
				break;
			case DBConstants.TYPE_KEY_INT_ARR:
				readAI(node);
				break;
			case DBConstants.TYPE_KEY_LONG_ARR:
				readAJ(node);
				break;
			case DBConstants.TYPE_KEY_FLOAT_ARR:
				readAF(node);
				break;
			case DBConstants.TYPE_KEY_COMPLEX_ARR:
				break;
			case DBConstants.TYPE_KEY_BOOLEAN_ARR:
				readAZ(node);
				break;
			case DBConstants.TYPE_KEY_STRING_ARR:
				readAS(node);
				break;
			case DBConstants.TYPE_KEY_COMPLEX:
				clsLoaded = false;
				break;
			default:
				break;
			}
		}

		if (node.type != null) {
			clsLoaded = true;
			String clsName = node.type.getName();
			if (!node.typeName.equals(clsName)) {
				try {
					clsLoaded = setTypeFromName(node);
				} catch (Exception e) {
					clsLoaded = false;
				}
			}
		}
		// register node only if its data model was found. If the data model couldn't be loaded, the resource is removed
		// from persistent data storage.
		if (clsLoaded)
			putResource(node);
		else {
			unloadableCustomResources.put(node.path, node);
			logger.debug("Type couldn't be loaded: " + node.typeName);
		}
		return node;
	}

	private void setNodeType(TreeElementImpl node) {
		int typeKey = node.typeKey;
		switch (typeKey) {
		// read simple resource
		case DBConstants.TYPE_KEY_BOOLEAN:
			node.type = DBConstants.CLASS_BOOL_TYPE;
			break;
		case DBConstants.TYPE_KEY_FLOAT:
			node.type = DBConstants.CLASS_FLOAT_TYPE;
			break;
		case DBConstants.TYPE_KEY_INT:
			node.type = DBConstants.CLASS_INT_TYPE;
			break;
		case DBConstants.TYPE_KEY_STRING:
			node.type = DBConstants.CLASS_STRING_TYPE;
			break;
		case DBConstants.TYPE_KEY_LONG:
			node.type = DBConstants.CLASS_TIME_TYPE;
			break;
		// read array resource
		case DBConstants.TYPE_KEY_OPAQUE:
			node.type = DBConstants.CLASS_OPAQUE_TYPE;
			break;
		case DBConstants.TYPE_KEY_INT_ARR:
			node.type = DBConstants.CLASS_INT_ARR_TYPE;
			break;
		case DBConstants.TYPE_KEY_LONG_ARR:
			node.type = DBConstants.CLASS_TIME_ARR_TYPE;
			break;
		case DBConstants.TYPE_KEY_FLOAT_ARR:
			node.type = DBConstants.CLASS_FLOAT_ARR_TYPE;
			break;
		case DBConstants.TYPE_KEY_COMPLEX_ARR:
			node.type = DBConstants.CLASS_COMPLEX_ARR_TYPE;
			break;
		case DBConstants.TYPE_KEY_BOOLEAN_ARR:
			node.type = DBConstants.CLASS_BOOL_ARR_TYPE;
			break;
		case DBConstants.TYPE_KEY_STRING_ARR:
			node.type = DBConstants.CLASS_STRING_ARR_TYPE;
			break;
		case DBConstants.TYPE_KEY_COMPLEX:
			// create complex resource
			// first register the type of the resource
			setTypeFromName(node);
			break;
		default:
			break;
		}
	}

	void tryReadEntry(RandomAccessFile raf) throws IOException, EOFException {
		// 1. read header of the entry
		TreeElementImpl node = new TreeElementImpl(database);
		readHeader(node, raf);
		// if the node is a reference no own value container is needed
		if (node.isReference())
			return;
		/*
		 * prepare data container, so the simple data can be filled in.
		 */

		int typeKey = node.typeKey;
		// check if the interface is a simple or a complex one
		switch (typeKey) {
		// read simple resource
		case DBConstants.TYPE_KEY_BOOLEAN:
			node.initDataContainer();
			node.type = DBConstants.CLASS_BOOL_TYPE;
			node.simpleValue.Z = raf.readBoolean();
			break;
		case DBConstants.TYPE_KEY_FLOAT:
			node.initDataContainer();
			node.type = DBConstants.CLASS_FLOAT_TYPE;
			node.simpleValue.F = raf.readFloat();
			break;
		case DBConstants.TYPE_KEY_INT:
			node.initDataContainer();
			node.type = DBConstants.CLASS_INT_TYPE;
			node.simpleValue.I = raf.readInt();
			break;
		case DBConstants.TYPE_KEY_STRING:
			node.initDataContainer();
			node.type = DBConstants.CLASS_STRING_TYPE;
			if (isNullString(raf))
				node.simpleValue.S = null;
			else
				node.simpleValue.S = raf.readUTF();
			break;
		case DBConstants.TYPE_KEY_LONG:
			node.initDataContainer();
			node.type = DBConstants.CLASS_TIME_TYPE;
			node.simpleValue.J = raf.readLong();
			break;
		// read array resource
		case DBConstants.TYPE_KEY_OPAQUE:
			node.initDataContainer();
			node.type = DBConstants.CLASS_OPAQUE_TYPE;
			readAB(node);
			break;
		case DBConstants.TYPE_KEY_INT_ARR:
			node.initDataContainer();
			node.type = DBConstants.CLASS_INT_ARR_TYPE;
			readAI(node);
			break;
		case DBConstants.TYPE_KEY_LONG_ARR:
			node.initDataContainer();
			node.type = DBConstants.CLASS_TIME_ARR_TYPE;
			readAJ(node);
			break;
		case DBConstants.TYPE_KEY_FLOAT_ARR:
			node.initDataContainer();
			node.type = DBConstants.CLASS_FLOAT_ARR_TYPE;
			readAF(node);
			break;
		case DBConstants.TYPE_KEY_COMPLEX_ARR:
			node.type = DBConstants.CLASS_COMPLEX_ARR_TYPE;
			break;
		case DBConstants.TYPE_KEY_BOOLEAN_ARR:
			node.initDataContainer();
			node.type = DBConstants.CLASS_BOOL_ARR_TYPE;
			readAZ(node);
			break;
		case DBConstants.TYPE_KEY_STRING_ARR:
			node.initDataContainer();
			node.type = DBConstants.CLASS_STRING_ARR_TYPE;
			readAS(node);
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("unchecked")
	private boolean setTypeFromName(TreeElementImpl node) {
		boolean result = false;
		String typeName = node.typeName;

		if (typeName != null) {
			Class<?> type = database.getResourceType(typeName);
			if (type == null) {
				try {
					type = Class.forName(node.typeName).asSubclass(Resource.class);
					type = database.addOrUpdateResourceType((Class<? extends Resource>) type);
					result = true;
				} catch (ClassNotFoundException e) {
					// potentially this happens if a custom data model can no longer be loaded, because the
					// exporter
					// bundle is not at least installed.
					logger.warn(String.format("Resouce class %s to the persistent data couldn't be loaded!",
							node.typeName));
				} catch (InvalidResourceTypeException e) {
					e.printStackTrace();
					type = null;
				}
			}
			if (type != null) {
				result = true;
				node.type = type;
			}
		}
		return result;
	}

	/*
	 * If the string is null wrtiteUTF throws an exception. In order to encode this case correctly in the database -1 is
	 * written instead of the UTF-8 string. During the parse process is this case to be evaluated. This method
	 */
	private boolean isNullString(RandomAccessFile raf) {
		try {
			int strlen = raf.readShort();
			if (strlen == -1) { // null string encoded
				return true;
			}
			else { // valid utf-8 string encoded
					// seek back to the beginning of the string so readUTF can work.
				raf.seek(raf.getFilePointer() - 2);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void readAS(TreeElementImpl node) throws IOException {
		RandomAccessFile raf = dataFile.raf;
		int length = raf.readInt();
		String sArr[] = new String[length];
		String val = null;
		node.simpleValue.aS = sArr;
		/*
		 * Reset the number of valid entries to 0
		 */
		int arrLength = 0;
		while (length > 0) {
			if (isNullString(raf))
				val = null;
			else
				val = raf.readUTF();
			sArr[arrLength] = val;
			arrLength++;
			length--;
		}
	}

	private void readAZ(TreeElementImpl node) throws IOException {
		RandomAccessFile raf = dataFile.raf;
		int length = raf.readInt();
		boolean zArr[] = new boolean[length];// node.simpleValue.aZ;
		boolean val = false;
		node.simpleValue.aZ = zArr;
		/*
		 * Reset the number of valid entries to 0
		 */
		int arrLength = 0;
		while (length > 0) {
			val = raf.readBoolean();
			zArr[arrLength] = val;
			arrLength++;
			length--;
		}
	}

	private void readAF(TreeElementImpl node) throws IOException {
		RandomAccessFile raf = dataFile.raf;
		int length = raf.readInt();
		float fArr[] = new float[length];// node.simpleValue.aF;
		float val = 0;
		node.simpleValue.aF = fArr;
		/*
		 * Reset the number of valid entries to 0
		 */
		int arrLength = 0;
		while (length > 0) {
			val = raf.readFloat();
			fArr[arrLength] = val;
			arrLength++;
			length--;
		}
	}

	private void readAJ(TreeElementImpl node) throws IOException {
		RandomAccessFile raf = dataFile.raf;
		int length = raf.readInt();
		long jArr[] = new long[length];// node.simpleValue.aJ;
		long val = 0;
		node.simpleValue.aJ = jArr;
		/*
		 * Reset the number of valid entries to 0
		 */
		int arrLength = 0;
		while (length > 0) {
			val = raf.readLong();
			jArr[arrLength] = val;
			arrLength++;
			length--;
		}
	}

	private void readAI(TreeElementImpl node) throws IOException {
		RandomAccessFile raf = dataFile.raf;
		int length = raf.readInt();
		int iArr[] = new int[length];// node.simpleValue.aI;
		int val = 0;
		node.simpleValue.aI = iArr;
		/*
		 * Reset the number of valid entries to 0
		 */
		int arrLength = 0;
		while (length > 0) {
			val = raf.readInt();
			iArr[arrLength] = val;
			arrLength++;
			length--;
		}
	}

	private void readAB(TreeElementImpl node) throws IOException {
		RandomAccessFile raf = dataFile.raf;
		int length = raf.readInt();
		byte bArr[] = new byte[length]; // node.simpleValue.aB;
		int val = 0;
		node.simpleValue.aB = bArr;
		/*
		 * Reset the number of valid entries to 0
		 */
		int arrLength = 0;
		while (length > 0) {
			val = raf.readByte();
			bArr[arrLength] = (byte) val;
			arrLength++;
			length--;
		}
	}

	boolean putResource(TreeElementImpl e) {
		boolean unsorted = false;
		// determine the parent node
		TreeElementImpl parent = database.resNodeByID.get(e.parentID);
		if ((parent == null) && (!e.toplevel)) // parent not yet read from
		// archive file
		{
			unsortedParents.put(e.path, e);
			unsorted = true;
		} // temporarily its hold as unsorted
			// node in order to be sorted in the
			// resource tree later.
		else if (e.toplevel) {
			e.parent = null;
			e.topLevelParent = e;
		}
		else {
			e.parent = parent;
			e.topLevelParent = parent.topLevelParent;
			// Check if the parent is a ResourceList. In this case, this is the right moment to set the resource type
			// info.
			if (parent.typeKey == DBConstants.TYPE_KEY_COMPLEX_ARR && parent.type == DBConstants.CLASS_COMPLEX_ARR_TYPE
					&& !e.name.equals("@elements")) {
				//parent.type = e.type;
            }
		}
		// Check if the node is a reference
		TreeElementImpl refered = null;
		if (e.reference) {
			refered = database.resNodeByID.get(e.refID);
			if (refered == null) {
				unsortedRefs.put(e.path, e);
				unsorted = true;
			}
			else {
				e.refered = refered;
			}
		}

		if (unsorted) {
			if (Configuration.LOGGING)
				logger.debug("Unsorted resource " + e.path);
			return false;
		}
		else if (Configuration.LOGGING)
			logger.debug("Hook up the resource " + e.path);

		// setup the tree for this type only if itsn't a ComplexArrayResourse or
		// a reference
		if (!e.complexArray && !e.reference && e.type != null)
			database.createTree(e);

		// put the generated tree in the table of the top level resources.
		if (e.toplevel)
			database.root.put(e.name, e);
		else {
			// e.parent.optionals.remove(e.name);
			e.parent.getOrCreateRequireds(true).put(e.name, e);
		}
		database.registerRes(e);
		return true;
	}

	private void readHeader(TreeElementImpl entry, RandomAccessFile raf) throws IOException, EOFException {
		/* 1. Set type ID */
		entry.typeName = raf.readUTF();
		// 2. set resource ID
		int id = raf.readInt();
		entry.resID = id;
		maxID = java.lang.Math.max(maxID, id);
		// 3. set resources parent ID
		entry.parentID = raf.readInt();
		// 4. setFlags
		entry.setFlags(raf.read());
		if (Configuration.LOGGING && entry.complexArray)
			logger.debug("ResourceList name: " + entry.typeName);
		// 5. setTypeKey
		entry.typeKey = raf.read();
		// 6. set name and path strings
		String path = raf.readUTF();
		if (Configuration.LOGGING)
			logger.debug("Resourcepath " + path);
		entry.path = path;
		// separate the name from path if its a sub resource
		int index = path.lastIndexOf(DBConstants.RESOURCE_PATH_DELIMITER);
		if (index != -1)
			entry.name = path.substring(index + 1);
		else
			entry.name = path;
		// 7. set the owner app id
		entry.appID = raf.readUTF();
		// 8. set the id of any referenced node. If no node is referenced, the id is read as 0
		entry.refID = raf.readInt();
		// 9. set the lastmodified time stamp
		entry.lastModified = raf.readLong();
	}

	public void writeEntry() {
		try {
			dataFile.out.flush();
			dataFile.fos.getFD().sync();
		} catch (IOException e) {
		}
	}

	/**
	 * After each change of the archive file by putting or removing of an entry, the part at the end of the file
	 * containing the directory structure is to be updated. The file has to be truncated in case of deleting an entry.
	 * 
	 * @param offset
	 *            the offset the directory is written at.
	 */
	void updateDirectory() {

		TreeMap<Integer, Integer> sortedOffsets = new TreeMap<Integer, Integer>(sorter);
		sortedOffsets.putAll(offsetByID);
		// Save the previous length of the file.
		int numOfEntries = sortedOffsets.size();
		Iterator<Entry<Integer, Integer>> dirEntries = sortedOffsets.entrySet().iterator();
		dirFiles.shiftF();
		mapFile = new MapFile(dirFiles.fileNew, dataFile.fileName);
		DataOutputStream dos = mapFile.out;
		while (dirEntries.hasNext()) {
			Entry<Integer, Integer> currEntry = dirEntries.next();
			// Put the ID
			try {
				dos.writeInt(currEntry.getKey());
				logger.debug(currEntry.getKey().toString());
				logger.debug("=");
				logger.debug(currEntry.getValue().toString());
				logger.debug(",");
				dos.writeInt(currEntry.getValue());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			dos.writeUTF(mapFile.dataFileName);
			dos.writeInt(numOfEntries);
			dos.writeInt(MapFile.MAGIC1);
			dos.writeInt(MapFile.MAGIC2);
			logger.debug(mapFile.dataFileName);
			logger.debug(Integer.toString(numOfEntries));
			logger.debug(Integer.toHexString(MapFile.MAGIC1));
			logger.debug(Integer.toHexString(MapFile.MAGIC2));
			dos.flush();
			mapFile.fos.getFD().sync();
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	int getCurrentOffset() {
		int result = -1;
		if (dataFile == null || dataFile.out == null)
			return result;
		result = dbFileInitialOffset + dataFile.out.size();
		if (Configuration.LOGGING)
			logger.debug("Current file offset: " + result);
		return result;
	}

	/*
	 * Used by the tests only
	 */
	void reset() {
		closeAll();
		System.gc();
		dirFiles.reset();
		resDataFiles.reset();
		initFiles();
	}

	void closeAll() {
		if (mapFile != null)
			mapFile.close();
		if (dataFile != null)
			dataFile.close();
	}

	protected void finalize() {
		closeAll();
	}

	// This method is needed for test purposes only
	public static void copyFiles() {
		File dir = new File(currentPath);
		Path targetDir = FileSystems.getDefault().getPath("./" + System.currentTimeMillis() + "");
		try {
			Files.createDirectories(targetDir);
			String files[] = dir.list();
			for (String file : files) {
				Path source = FileSystems.getDefault().getPath(currentPath, file);
				if (!Files.isDirectory(source, LinkOption.NOFOLLOW_LINKS)) {
					Path targetFile = FileSystems.getDefault().getPath(currentPath, file);
					Files.copy(source, targetFile, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public TreeElementImpl handleUCR(String name, Class<? extends Resource> type) {
		// Is there an UCR with this name
		TreeElementImpl tei = unloadableCustomResources.remove(name);
		if (tei == null)
			return null;
		String prefix = name;
		if (!prefix.endsWith("/"))
			prefix = prefix + "/";
		// Is the existing UCR from the required type
		if (type != null && type != ResourceList.class && !tei.typeName.equals(type.getName())) {
			// UCR is to be deleted
			if (database.activatePersistence)
				database.persistence.store(tei.resID, ChangeInfo.DELETED);
			// unreachableCustomTypes.remove(name);
			// and all of its subresources too

			Set<Entry<String, TreeElementImpl>> unreachables = unloadableCustomResources.entrySet();
			for (Map.Entry<String, TreeElementImpl> entry1 : unreachables) {
				String path = entry1.getKey();
				if (path.startsWith(prefix))
					unloadableCustomResources.remove(path);
			}
			return null;
		}
		else {
			// UCR is to be reactivated
			// is there a type specified?
			if (type == null) {
				if (!setTypeFromName(tei)) // There is an UCR with the name but the type couldn't be resolved
				{
					unloadableCustomResources.put(name, tei);
					return null;
				}
			}
			else {
				tei.type = type;
			}
			putResource(tei);
			// and its children too
			int count = 0;
			do { // Try it recursively as long as any UCRs can be resolved
				count = 0;
				Set<Entry<String, TreeElementImpl>> unreachables = unloadableCustomResources.entrySet();
				for (Map.Entry<String, TreeElementImpl> entry1 : unreachables) {
					String path = entry1.getKey();
					TreeElementImpl child = entry1.getValue();
					TreeElementImpl parent = database.resNodeByID.get(child.parentID);
					if (parent != null) { // Is the parent resource already part of the tree?
						if (child.type == null)
							setTypeFromName(child); // Try to load type class
						if (child.type != null) { // Set parent and hook it on the tree
							child.parent = parent;
							putResource(child);
							count++;
							unloadableCustomResources.remove(path);
						}
					}
				}
			} while (count > 0);
			return tei;
		}
	}
}
