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

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.impl.persistence.TimedPersistence.Change;
import org.ogema.persistence.DBConstants;
import org.ogema.persistence.PersistencePolicy.ChangeInfo;
import org.slf4j.Logger;

public class DBResourceIO {

	/**
	 * Initial size of buffer the type entry is set up therein.
	 */
	static final int ENTRY_MAX_SIZE = 1024;

	private static final int MAGIC1 = 0xFEEDC0DE;

	private static final int MAGIC2 = 0xFEEDFACE;

	private static final int INITIAL_MAP_SIZE = 256;

	private final Logger logger = org.slf4j.LoggerFactory.getLogger("persistence");

	PersistentFileSet resDataFiles, dirFiles;
	int dirFileSuffix;

	ConcurrentHashMap<Integer, Integer> offsetByID;
	int dbFileInitialOffset;
	MapValueSorter sorter;

	ConcurrentHashMap<Integer, TreeElementImpl> unsortedParents;
	ConcurrentHashMap<Integer, TreeElementImpl> unsortedRefs;
	ConcurrentHashMap<Integer, TreeElementImpl> unreachableCusomTypes;

	private ResourceDBImpl database;

	private int dirEntryCount;

	private int maxID;

	/**
	 * Path and file name informations for the archives containing the persistent data. The values of these field are
	 * read from system properties. If the appropriate properties are not set, the default values from DBConstans are
	 * used.
	 */
	String dbPathName;
	String dbResourcesFileName;
	String dbDirFileName;

	String currentDataFileName;

	private static final int CHANGES_BUFFER_SIZE = 1024;
	private static final int DEFAULT_MIN_COMPACTION_FILE_SIZE = 1020 * 1024; // 1MB
	private static final float DEFAULT_MIN_COMPACTION_GARBAGE_SIZE = .75f; // 75%

	private int garbage;

	ConcurrentHashMap<Integer, Change> changes;

	private int minimunCompationFileSize;
	private float minimumCompationGarbageSize;

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
		unsortedParents = new ConcurrentHashMap<>(INITIAL_MAP_SIZE);
		unsortedRefs = new ConcurrentHashMap<>(INITIAL_MAP_SIZE);
		unreachableCusomTypes = new ConcurrentHashMap<>(INITIAL_MAP_SIZE);

		offsetByID = new ConcurrentHashMap<Integer, Integer>();
		sorter = new MapValueSorter(offsetByID);
		this.database = db;

		this.changes = new ConcurrentHashMap<>(CHANGES_BUFFER_SIZE);

		// check if the property to activate persistence debugging is set
		String persDebug = System.getProperty(DBConstants.PROP_NAME_PERSISTENCE_DEBUG, "false");
		if (persDebug.equals("true"))
			Configuration.LOGGING = true;
		// check if the minimum compaction file size is set
		String filesize = System.getProperty(DBConstants.PROP_NAME_PERSISTENCE_COMPACTION_START_SIZE_FILE, null);
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
		dbResourcesFileName = System.getProperty(DBConstants.RESOURCES_FILE_PROP, DBConstants.RESOURCES_ARCHIVE_NAME);
		dbDirFileName = System.getProperty(DBConstants.DIR_FILE_PROP, DBConstants.DIR_FILE_NAME);

		initFiles();
	}

	private void initFiles() {
		File dir = new File(dbPathName);
		if (!dir.exists()) {
			dir.mkdir();
		}
		/*
		 * Init directory file
		 */
		dirFiles = new PersistentFileSet();
		dirFiles.initFiles(dir, dbDirFileName);
		/*
		 * Init resource archive file
		 */
		resDataFiles = new PersistentFileSet();
		resDataFiles.initFiles(dir, dbResourcesFileName);

		// dbFile = new File(dbPathName, dbResourcesFileName);
		if (resDataFiles.fileNew == null)
			dbFileInitialOffset = 0;
		else
			dbFileInitialOffset = (int) resDataFiles.fileNew.length();

		/*
		 * if no directory file exists, resource data file is unusable and can be deleted
		 */
		if (dirFiles.fileOld == null && dirFiles.fileNew == null)
			resDataFiles.backup();
	}

	void setChValue(int ch) {
		try {
			resDataFiles.out.writeChar(ch);
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
			resDataFiles.out.write(typeKey);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void setByte(int flags) {

		try {
			resDataFiles.out.write(flags);
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
			 * database -1 is written instead of the UTF-8 string. During the parse process is this case to be
			 * evaluated.
			 */
			if (name == null) {
				resDataFiles.out.writeShort(-1);
				return;
			}
			resDataFiles.out.writeUTF(name);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setBValue(boolean b) {
		try {
			resDataFiles.out.writeBoolean(b);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setFValue(float f) {
		try {
			resDataFiles.out.writeFloat(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setIValue(int i) {
		try {
			resDataFiles.out.writeInt(i);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setJValue(long l) {
		try {
			resDataFiles.out.writeLong(l);
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

	boolean compactionRequired() {
		int currentFileLength = getCurrentOffset();
		if (currentFileLength == -1)
			return false;
		if (currentFileLength > minimunCompationFileSize && (garbage > currentFileLength * minimumCompationGarbageSize))
			return true;
		return false;
	}

	void compact() {
		boolean fileChanged = false;
		/*
		 * Changes can be cleared, because all of the resources which are alive are to be stored into the new file.
		 */
		changes.clear();
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
		// reset size of the resource within the persistence
		// node.footprint = 0;

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
		/*
		 * First read the directory structure
		 */
		if (Configuration.LOGGING)
			logger.debug("Parse Resources...");
		resDataFiles.updateCurrentIn();
		if (resDataFiles.in == null) {
			resDataFiles.updateCurrentOut();
			currentDataFileName = resDataFiles.fileNew.getName();
			return;
		}
		RandomAccessFile dirRaf = null;
		// is the newer directory file valid
		if (dirFiles.fileNew != null) {
			try {
				dirFiles.updateCurrentIn();
				dirRaf = dirFiles.in;
				if (!dirFileValid(dirRaf)) {
					if (dirRaf != null) {
						dirRaf.close();
						dirFiles.deleteNew();
					}
					// is the older directory file valid
					dirFiles.updateCurrentIn();
					dirRaf = dirFiles.in;
					if (!dirFileValid(dirRaf)) {
						if (dirRaf != null) {
							dirRaf.close();
							dirFiles.deleteNew();
						}
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// read the entries
		RandomAccessFile dataRaf = resDataFiles.in;
		this.garbage = 0;
		int endGarbage = 0, beginGarbage = 0;
		if (dirRaf != null) {
			try {
				dirRaf.seek(0);
				while (dirEntryCount > 0) {
					int key = dirRaf.readInt();
					int value = dirRaf.readInt();
					dataRaf.seek(value);
					endGarbage = (int) dataRaf.getFilePointer();
					int tmpGarbage = endGarbage - beginGarbage;
					if (tmpGarbage > 0)
						garbage += tmpGarbage;
					readEntry();
					beginGarbage = (int) dataRaf.getFilePointer();
					offsetByID.put(key, value);
					dirEntryCount--;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		database.nextresourceID = maxID + 1;

		/*
		 * Remove sub resources of deleted custom model resources from the unhandled list.
		 */
		handleRemoved(unsortedParents);
		handleRemoved(unsortedRefs);
		/*
		 * Place the unsorted node in the tree.
		 */
		handleUnsorted(unsortedParents);
		handleUnsorted(unsortedRefs);
		if (Configuration.LOGGING)
			logger.debug("...Resources parsed");
		try {

			/*
			 * Check if a compaction of the data is required. The policy here is that at least 90% of the data file
			 * consists of garbage.
			 */
			dataRaf.close();
			resDataFiles.in = null;
			resDataFiles.updateCurrentOut();
			currentDataFileName = resDataFiles.fileNew.getName();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove all unsorted sub resources that are unreachable because their top level resource is removed due to failed
	 * load of the model class.
	 * 
	 * @param map
	 */
	private void handleRemoved(Map<Integer, TreeElementImpl> map) {
		Set<Entry<Integer, TreeElementImpl>> unreachables = unreachableCusomTypes.entrySet();
		for (Map.Entry<Integer, TreeElementImpl> entry1 : unreachables) {
			TreeElementImpl e1 = entry1.getValue();
			if (database.activatePersistence)
				database.persistence.store(e1.resID, ChangeInfo.DELETED);
			Set<Entry<Integer, TreeElementImpl>> unsorteds = map.entrySet();
			for (Map.Entry<Integer, TreeElementImpl> entry2 : unsorteds) {
				TreeElementImpl e2 = entry2.getValue();
				if (e2.path.startsWith(e1.path)) {
					map.remove(e2.resID);
					if (database.activatePersistence)
						database.persistence.store(e2.resID, ChangeInfo.DELETED);
				}
			}
		}
	}

	/**
	 * When database starts up, the persistent stored resource information are parsed and the resource tree is set up.
	 * During parsing of the persistent data sub resources may be parsed before their parent resources. In these cases
	 * the nodes of the sub resources are collected in a separate map which is to be processed at the end of parse
	 * process.
	 * 
	 * @param map
	 */
	private void handleUnsorted(Map<Integer, TreeElementImpl> map) {
		int countBefore, countAfter;
		do {
			Set<Entry<Integer, TreeElementImpl>> tlrs = map.entrySet();
			countBefore = map.size();
			if (countBefore == 0)
				break;
			for (Map.Entry<Integer, TreeElementImpl> entry : tlrs) {
				TreeElementImpl e = entry.getValue();
				if (putResource(e))
					map.remove(e.resID);
			}
			countAfter = map.size();
			assert (countBefore != countAfter);
		} while (countBefore > 0);
	}

	private boolean dirFileValid(RandomAccessFile raf) {
		if (raf == null)
			return false;
		int fileLen, entryCount = 0, dataFileNameOffset;
		String dataFileName;
		try {
			fileLen = (int) raf.length();
			raf.seek(fileLen - 12);
			entryCount = raf.readInt();
			/*
			 * Read name of the data archive which is corresponding to the directory file.
			 */
			dataFileNameOffset = (entryCount << 3);
			raf.seek(dataFileNameOffset);
			dataFileName = raf.readUTF();
			this.currentDataFileName = dataFileName;
			// 8 bytes each entry + 4 bytes entry count + 8 bytes magic + utf8
			// data file name
			if (((entryCount << 3) + 12 + dataFileName.length() + 2) != fileLen)
				return false;
			raf.seek(fileLen - 8);
			if ((raf.readInt() != MAGIC1) && (raf.readInt() != MAGIC2))
				return false;

		} catch (IOException e) {
			return false;
		}
		dirEntryCount = entryCount;
		return true;
	}

	/**
	 * Read a resource from data base file into an resolved tree.
	 * 
	 * @param type
	 * @param ifaces
	 * @return
	 */
	void readEntry() throws IOException, EOFException {
		boolean clsLoaded = true;
		boolean isSimple = false;
		// 1. read header of the entry
		TreeElementImpl node = new TreeElementImpl(database);
		readHeader(node);
		int typeKey = node.typeKey;
		/*
		 * prepare data container, so the simple data can be filled in.
		 */

		// check if the interface is a simple or a complex one
		RandomAccessFile raf = resDataFiles.in;
		try {
			switch (typeKey) {
			// read simple resource
			case DBConstants.TYPE_KEY_BOOLEAN:
				node.initDataContainer();
				node.type = DBConstants.CLASS_BOOL_TYPE;
				node.simpleValue.Z = raf.readBoolean();
				isSimple = true;
				break;
			case DBConstants.TYPE_KEY_FLOAT:
				node.initDataContainer();
				node.type = DBConstants.CLASS_FLOAT_TYPE;
				node.simpleValue.F = raf.readFloat();
				isSimple = true;
				break;
			case DBConstants.TYPE_KEY_INT:
				node.initDataContainer();
				node.type = DBConstants.CLASS_INT_TYPE;
				node.simpleValue.I = raf.readInt();
				isSimple = true;
				break;
			case DBConstants.TYPE_KEY_STRING:
				node.initDataContainer();
				node.type = DBConstants.CLASS_STRING_TYPE;
				if (isNullString(raf))
					node.simpleValue.S = null;
				else
					node.simpleValue.S = raf.readUTF();
				isSimple = true;
				break;
			case DBConstants.TYPE_KEY_LONG:
				node.initDataContainer();
				node.type = DBConstants.CLASS_TIME_TYPE;
				node.simpleValue.J = raf.readLong();
				isSimple = true;
				break;
			// read array resource
			case DBConstants.TYPE_KEY_OPAQUE:
				node.initDataContainer();
				node.type = DBConstants.CLASS_OPAQUE_TYPE;
				readAB(node);
				isSimple = true;
				break;
			case DBConstants.TYPE_KEY_INT_ARR:
				node.initDataContainer();
				node.type = DBConstants.CLASS_INT_ARR_TYPE;
				readAI(node);
				isSimple = true;
				break;
			case DBConstants.TYPE_KEY_LONG_ARR:
				node.initDataContainer();
				node.type = DBConstants.CLASS_TIME_ARR_TYPE;
				readAJ(node);
				isSimple = true;
				break;
			case DBConstants.TYPE_KEY_FLOAT_ARR:
				node.initDataContainer();
				node.type = DBConstants.CLASS_FLOAT_ARR_TYPE;
				readAF(node);
				isSimple = true;
				break;
			case DBConstants.TYPE_KEY_COMPLEX_ARR:
				node.type = DBConstants.CLASS_COMPLEX_ARR_TYPE;
				clsLoaded = true;
				break;
			case DBConstants.TYPE_KEY_BOOLEAN_ARR:
				node.initDataContainer();
				node.type = DBConstants.CLASS_BOOL_ARR_TYPE;
				readAZ(node);
				isSimple = true;
				break;
			case DBConstants.TYPE_KEY_STRING_ARR:
				node.initDataContainer();
				node.type = DBConstants.CLASS_STRING_ARR_TYPE;
				readAS(node);
				isSimple = true;
				break;
			case DBConstants.TYPE_KEY_COMPLEX:
				// create complex resource
				// first register the type of the resource
				clsLoaded = setTypeFromName(node);
				break;
			default:
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (node.type != null) {
			String clsName = node.type.getName();
			if (isSimple && !node.typeName.equals(clsName)) {
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
			unreachableCusomTypes.put(node.resID, node);
			logger.debug("Type couldn't be loaded: " + node.typeName);
		}
	}

	@SuppressWarnings("unchecked")
	private boolean setTypeFromName(TreeElementImpl node) {
		boolean result = false;
		String typeName = node.typeName;
		try {
			if (typeName != null) {
				Class<?> type = database.getResourceType(typeName);
				if (type == null) {
					try {
						type = Class.forName(node.typeName).asSubclass(Resource.class);
					} catch (ClassNotFoundException e) {
					}
					if (type != null) {
						type = database.addOrUpdateResourceType((Class<? extends Resource>) type);
						result = true;
					}
					else { // potentially this happens if a custom data model can no longer be loaded, because the
						// exporter
						// bundle is not at least installed.
						logger.warn(String.format("Resouce class %s to the persistent data couldn't be loaded!",
								node.typeName));
					}
				}
				if (type != null) {
					result = true;
					node.type = type;
				}
			}
		} catch (InvalidResourceTypeException e) {
			e.printStackTrace();
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

	private void readAS(TreeElementImpl node) {
		RandomAccessFile raf = resDataFiles.in;
		try {
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readAZ(TreeElementImpl node) {
		RandomAccessFile raf = resDataFiles.in;
		try {
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
			// node.simpleValue.arrLength = arrLength;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readAF(TreeElementImpl node) {
		RandomAccessFile raf = resDataFiles.in;
		try {
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
			// node.simpleValue.arrLength = arrLength;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readAJ(TreeElementImpl node) {
		RandomAccessFile raf = resDataFiles.in;
		try {
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
			// node.simpleValue.arrLength = arrLength;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readAI(TreeElementImpl node) {
		RandomAccessFile raf = resDataFiles.in;
		try {
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
			// node.simpleValue.arrLength = arrLength;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readAB(TreeElementImpl node) {
		RandomAccessFile raf = resDataFiles.in;
		try {
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
			// node.simpleValue.arrLength = arrLength;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	boolean putResource(TreeElementImpl e) {
		boolean unsorted = false;
		// determine the parent node
		TreeElementImpl parent = database.resNodeByID.get(e.parentID);
		if ((parent == null) && (!e.toplevel)) // parent not yet read from
		// archive file
		{
			unsortedParents.put(e.resID, e);
			unsorted = true;
		}// temporarily its hold as unsorted
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
					&& !e.name.equals("@elements"))
				parent.type = e.type;
		}
		// Check if the node is a reference
		TreeElementImpl refered = null;
		if (e.reference) {
			refered = database.resNodeByID.get(e.refID);
			if (refered == null) {
				unsortedRefs.put(e.resID, e);
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
			e.parent.optionals.remove(e.name);
			e.parent.requireds.put(e.name, e);
		}
		database.registerRes(e);
		return true;
	}

	private void readHeader(TreeElementImpl entry) throws IOException, EOFException {
		RandomAccessFile raf = resDataFiles.in;
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
			resDataFiles.out.flush();
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
		dirFiles.updateNextOut();
		DataOutputStream dos = dirFiles.out;
		while (dirEntries.hasNext()) {
			Entry<Integer, Integer> currEntry = dirEntries.next();
			// Put the ID
			try {
				dos.writeInt(currEntry.getKey());
				dos.writeInt(currEntry.getValue());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			dos.writeUTF(currentDataFileName);
			dos.writeInt(numOfEntries);
			dos.writeInt(MAGIC1);
			dos.writeInt(MAGIC2);
			dos.flush();
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	int getCurrentOffset() {
		int result = -1;
		if (resDataFiles.out == null)
			return result;
		result = dbFileInitialOffset + resDataFiles.out.size();
		if (Configuration.LOGGING)
			logger.debug("Current file offset: " + result);
		return result;
	}

	/*
	 * Used by the tests only
	 */
	void reset() {
		dirFiles.closeAll();
		resDataFiles.closeAll();
		System.gc();
		dirFiles.reset();
		resDataFiles.reset();
		initFiles();
	}

	void closeAll() {
		dirFiles.closeAll();
		resDataFiles.closeAll();

	}

	protected void finalize() {
		closeAll();
	}
}
