/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.persistence;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.array.BooleanArrayResource;
import org.ogema.core.model.array.FloatArrayResource;
import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.array.TimeArrayResource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.OpaqueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;

/**
 * This class contains the definition of some constant data which is used by the ResourceDB implementation.
 * 
 * @author mns
 * 
 */
@SuppressWarnings("unchecked")
public class DBConstants {

	/*
	 * Resource status flags which are to be stored persistently
	 */
	public static final int RES_VOLATILE = 1;
	public static final int RES_NONPERSISTENT = 1 << 1;
	public static final int RES_COMPLEX_ARRAY = 1 << 2;
	public static final int RES_ACTIVE = 1 << 3;
	public static final int RES_TOPLEVEL = 1 << 4;
	public static final int RES_DECORATOR = 1 << 5;
	public static final int RES_REFERENCE = 1 << 6;

	/*
	 * Type keys to be stored persistently in order to parse values properly. Only basic types have different type keys
	 * where all complex types have the same type key.
	 */
	static final public int TYPE_KEY_BOOLEAN = 0;
	static final public int TYPE_KEY_FLOAT = 1;
	static final public int TYPE_KEY_INT = 2;
	static final public int TYPE_KEY_LONG = 3;
	static final public int TYPE_KEY_STRING = 4;
	static final public int TYPE_KEY_BOOLEAN_ARR = 5;
	static final public int TYPE_KEY_FLOAT_ARR = 6;
	static final public int TYPE_KEY_INT_ARR = 7;
	static final public int TYPE_KEY_LONG_ARR = 8;
	static final public int TYPE_KEY_STRING_ARR = 9;
	static final public int TYPE_KEY_COMPLEX_ARR = 10;
	static final public int TYPE_KEY_COMPLEX = 11;
	static final public int TYPE_KEY_OPAQUE = 12;

	/*
	 * Default path and file name informations for the archives containing the persistent data. The default values can
	 * be overwritten be setting of the properties DB_PATH_PROP RESOURCES_FILE_PROP DIR_FILE_PROP.
	 */
	public static final String DB_PATH_NAME = "./database/";
	public static final String RESOURCES_ARCHIVE_NAME = "resData";
	public static final String DIR_FILE_NAME = "resMap";

	/*
	 * Properties which can be set in the command line to control DB configuration.
	 */
	public static final String DB_PATH_PROP = "org.ogema.persistence.dbpath";
	public static final String RESOURCES_FILE_PROP = "org.ogema.persistence.resources";
	public static final String DIR_FILE_PROP = "org.ogema.persistence.dirmap";

	public static final String PROP_PERSISTENCE_IMPL_CLASS = "org.ogema.persistence.impl";
	public static final String PROP_NAME_PERSISTENCE_ACTIVE = "org.ogema.persistence";
	public static final String PROP_VALUE_PERSISTENCE_ACTIVE = "active";
	public static final String PROP_VALUE_PERSISTENCE_INACTIVE = "inactive";

	public static final String PROP_NAME_PERSISTENCE_DEBUG = "org.ogema.persistence.debug";
	public static final String PROP_NAME_PERSISTENCE_COMPACTION_START_SIZE_FILE = "org.ogema.persistence.compaction.start.size.file";
	public static final String PROP_NAME_PERSISTENCE_COMPACTION_START_SIZE_GARBAGE = "org.ogema.persistence.compaction.start.size.garbage";
	public static final String PROP_NAME_TIMEDPERSISTENCE_PERIOD = "org.ogema.timedpersistence.period";

	/*
	 * A constant value which indicates, that a node haven't yet a valid id.
	 */
	public static final int INVALID_ID = -1;

	/*
	 * The delimiter character in the path information of resources.
	 */
	public static final char RESOURCE_PATH_DELIMITER = '.';

	/*
	 * Class references of known basic resources to be cached for more efficient access.
	 */
	public static Class<?> CLASS_SIMPLE_TYPE;
	public static Class<Resource> CLASS_BASIC_TYPE;
	public static Class<BooleanResource> CLASS_BOOL_TYPE;
	public static Class<FloatResource> CLASS_FLOAT_TYPE;
	public static Class<IntegerResource> CLASS_INT_TYPE;
	public static Class<TimeResource> CLASS_TIME_TYPE;
	public static Class<StringResource> CLASS_STRING_TYPE;
	public static Class<OpaqueResource> CLASS_OPAQUE_TYPE;
	public static Class<BooleanArrayResource> CLASS_BOOL_ARR_TYPE;
	public static Class<FloatArrayResource> CLASS_FLOAT_ARR_TYPE;
	public static Class<IntegerArrayResource> CLASS_INT_ARR_TYPE;
	public static Class<TimeArrayResource> CLASS_TIME_ARR_TYPE;
	public static Class<StringArrayResource> CLASS_STRING_ARR_TYPE;
	@SuppressWarnings("rawtypes")
	public static Class<ResourceList> CLASS_COMPLEX_ARR_TYPE;

	public static String CLASS_COMPLEX_ARR_NAME = "org.ogema.core.model.ResourceList";

	public static Class<Boolean> CLASS_BOOL_VALUE;
	public static Class<Integer> CLASS_INT_VALUE;
	public static Class<Float> CLASS_FLOAT_VALUE;
	public static Class<Long> CLASS_LONG_VALUE;
	public static Class<byte[]> CLASS_OPAQUE_VALUE;
	public static Class<String> CLASS_STRING_VALUE;
	public static Class<boolean[]> CLASS_BOOL_ARR_VALUE;
	public static Class<int[]> CLASS_INT_ARR_VALUE;
	public static Class<float[]> CLASS_FLOAT_ARR_VALUE;
	public static Class<String[]> CLASS_STRING_ARR_VALUE;

	static {
		try {
			CLASS_SIMPLE_TYPE = org.ogema.core.model.SimpleResource.class;
			CLASS_BASIC_TYPE = org.ogema.core.model.Resource.class;
			CLASS_BOOL_TYPE = org.ogema.core.model.simple.BooleanResource.class;
			CLASS_FLOAT_TYPE = org.ogema.core.model.simple.FloatResource.class;
			CLASS_INT_TYPE = org.ogema.core.model.simple.IntegerResource.class;
			CLASS_STRING_TYPE = org.ogema.core.model.simple.StringResource.class;
			CLASS_OPAQUE_TYPE = org.ogema.core.model.simple.OpaqueResource.class;
			CLASS_BOOL_ARR_TYPE = org.ogema.core.model.array.BooleanArrayResource.class;
			CLASS_FLOAT_ARR_TYPE = org.ogema.core.model.array.FloatArrayResource.class;
			CLASS_INT_ARR_TYPE = org.ogema.core.model.array.IntegerArrayResource.class;
			CLASS_STRING_ARR_TYPE = org.ogema.core.model.array.StringArrayResource.class;
			CLASS_COMPLEX_ARR_TYPE = org.ogema.core.model.ResourceList.class;
			CLASS_TIME_TYPE = org.ogema.core.model.simple.TimeResource.class;
			CLASS_TIME_ARR_TYPE = org.ogema.core.model.array.TimeArrayResource.class;
			CLASS_BOOL_VALUE = java.lang.Boolean.class;
			CLASS_INT_VALUE = java.lang.Integer.class;
			CLASS_LONG_VALUE = java.lang.Long.class;
			CLASS_FLOAT_VALUE = java.lang.Float.class;
			CLASS_STRING_VALUE = java.lang.String.class;
			CLASS_OPAQUE_VALUE = (Class<byte[]>) Class.forName("[B");
			CLASS_BOOL_ARR_VALUE = (Class<boolean[]>) Class.forName("[Z");
			CLASS_INT_ARR_VALUE = (Class<int[]>) Class.forName("[I");
			CLASS_FLOAT_ARR_VALUE = (Class<float[]>) Class.forName("[F");
			CLASS_STRING_ARR_VALUE = (Class<String[]>) Class.forName("[Ljava.lang.String;");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
