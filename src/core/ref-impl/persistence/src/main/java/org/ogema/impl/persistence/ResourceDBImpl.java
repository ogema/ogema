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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.ogema.core.model.ModelModifiers;
import org.ogema.core.model.Resource;
import org.ogema.core.model.units.ColourResource;
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceNotFoundException;
import org.ogema.persistence.DBConstants;
import org.ogema.persistence.PersistencePolicy;
import org.ogema.persistence.ResourceDB;
import org.ogema.persistence.PersistencePolicy.ChangeInfo;
import org.ogema.resourcetree.TreeElement;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;

/**
 * This class implements the interface ResourceDB supporting OGEMA Resource Management with persistent data storage. The
 * data base consists of two files resourcesDB and resourceTypesDB which are persistently stored. At the boot time the
 * stored data is read into tables which are provided for read and write operations through the OGEMA framework.
 * 
 */
public class ResourceDBImpl implements ResourceDB, BundleActivator {

	private static final int INITIAL_MAP_SIZE = 64;

	private ServiceRegistration<ResourceDB> registration;

	final Logger logger = org.slf4j.LoggerFactory.getLogger("persistence");

	/**
	 * Map of all top level resources as Proxy instances with resource name as key.
	 */
	final ConcurrentHashMap<String, TreeElementImpl> root;

	/**
	 * These counter help determining unique IDs for resource types resources and sub resources. At the boot time the
	 * stored ids are observed and the counter are initialized with maximum detected id plus 1. nexttypeID starts at
	 * 1024 so the range 0-1024 could be used for standard types constantly.
	 */
	int nextresourceID = 1;

	/**
	 * Like the resource types the data of the resources are organized in a random access file.
	 */
	DBResourceIO resourceIO;

	/**
	 * Monitor objects for synchronized access to the types and resources tables.
	 */
	final Object tablesLock;

	// FIXME make these final?
	ConcurrentHashMap<String, Class<?>> typeClassByName;
	ConcurrentHashMap<String, Integer> resIDByName;
	ConcurrentHashMap<Integer, TreeElementImpl> resNodeByID;
	ConcurrentHashMap<String, Vector<Integer>> resIDsByType;

	final boolean activatePersistence;

	private boolean dbReady;

	PersistencePolicy persistence;

	String name;

	private Object storageLock;

	private boolean inited;

	/**
	 * Get the archive instances for the types and resources. If the files already exist then they are opened as
	 * RandomAccessFile otherwise a new file is created.
	 */
	public ResourceDBImpl() {
		root = new ConcurrentHashMap<>();
		// Allocate enough memory for all entries in the archive and some memory
		// as reserves.
		typeClassByName = new ConcurrentHashMap<>(INITIAL_MAP_SIZE);

		// Allocate enough memory for all entries in the archive and some memory
		// as reserves.
		resIDByName = new ConcurrentHashMap<>(INITIAL_MAP_SIZE);
		resNodeByID = new ConcurrentHashMap<>(INITIAL_MAP_SIZE);
		resIDsByType = new ConcurrentHashMap<>(INITIAL_MAP_SIZE);

		tablesLock = new Object();

		// check if the property to activate persistence is set
		String persActive = System.getProperty(DBConstants.PROP_NAME_PERSISTENCE_ACTIVE,
				DBConstants.PROP_VALUE_PERSISTENCE_INACTIVE);
		if (persActive.equals(DBConstants.PROP_VALUE_PERSISTENCE_ACTIVE))
			activatePersistence = true;
		else
			activatePersistence = false;
		init();
	}

	synchronized void init() {
		if (inited)
			return;
		if (activatePersistence) {
			persistence = new TimedPersistence(this);

			resourceIO = new DBResourceIO(this);
			resourceIO.initFiles();

			this.storageLock = persistence.getStorageLock();
			resourceIO.parseResources();
			persistence.startStorage();

			/*
			 * Remove ResourceLists, their types is not yet specified
			 */
			// for (TreeElementImpl te : resourceIO.resourceLists) {
			// if (te.type == ResourceList.class) {
			// removeTree(te, true);
			// if (activatePersistence)
			// persistence.store(te.resID, ChangeInfo.DELETED);
			// }
			// }
		}
		else
			this.storageLock = new Object();
		dbReady = true;
		this.inited = true;
	}

	int getNextresourceID() {
		return nextresourceID++;
	}

	/**
	 * The method argument type is an interface describing a data model. It could be a basic Resource (a Resource from
	 * this type is a Tree consisting of one leaf) or a complex one (in this case the Tree could have any structure with
	 * branches and leafs). The branches and leafs are on the way as detected that the return types of the interface
	 * methods are checked. This is done for all branches recursively until a basic type (an interface implementing
	 * SimpleResopurce is found.
	 */
	@Override
	public Class<? extends Resource> addOrUpdateResourceType(Class<? extends Resource> type)
			throws InvalidResourceTypeException {
		// check if its a valid type
		if (!isValidType(type))
			throw new InvalidResourceTypeException("Type definition couldn't be verified as valid: " + type.getName());
		// check if the type already registered
		String typeName = type.getName();
		Class<?> regType = typeClassByName.get(typeName);
		if (regType == null) {
			typeClassByName.put(typeName, type);

		}
		return type.asSubclass(Resource.class);
	}

	/**
	 * Initialize a (sub)tree for the given resource type. Some of the node information are set by the caller and some
	 * of them are determined in the context of this method and set.
	 * 
	 * @param type
	 * @param node
	 * @throws InvalidResourceTypeException
	 */
	void createTree(TreeElementImpl node) throws InvalidResourceTypeException {
		Class<?> type = node.type;
		if (type == null) {
			if (node.getPath() == null)
				throw new IllegalArgumentException("Node type and name were null.");
			else
				throw new IllegalArgumentException("Node type of node " + node.getPath() + " is null.");
		}

		Class<?>[] ifaces = type.getInterfaces();
		boolean simple = isSimple(type);
		// For a simple type interfaces consist of {Resource, SimpleResource}
		// For a complex type interfaces consist of {Resource} or
		// {anyComplexResource}

		// save the type class
		int typekey = node.typeKey = getTypeKeyFromClass(type);

		// If neither SimpleResource nor Resource nor another complex resource
		// is inherited by the data model
		// its an invalid one.
		if (typekey == DBConstants.TYPE_KEY_INVALID)
			throw new InvalidResourceTypeException(type.getName());

		/*
		 * If the type is the ResourceList set the complexArray flag
		 */
		if (typekey == DBConstants.TYPE_KEY_COMPLEX_ARR)
			node.complexArray = true;

		// For simple resources and resources that hold values, add a data container
		if (simple || (type == DBConstants.CLASS_COMPLEX_ARR_TYPE)) {
			if (node.simpleValue == null)
				initSimpleNode(node);
		}

		// Create entries for defined sub-resources (recursively)
		Class<?> superModel = type;
		while ((superModel != DBConstants.CLASS_BASIC_TYPE) && (superModel != DBConstants.CLASS_SIMPLE_TYPE)
				&& (superModel != DBConstants.CLASS_VALUE_RESOURCE)) {
			// Create the nodes for the direct children of the type
			parseComplex(superModel, node);
			// iterate over all of the non-optional direct children and
			// create a
			// sub tree each child and hook it on the parent tree.
			Set<Entry<String, TreeElementImpl>> tlrs = node.requireds.entrySet();
			for (Map.Entry<String, TreeElementImpl> entry : tlrs) {

				TreeElementImpl res = entry.getValue();
				createTree(res);
			}
			ifaces = superModel.getInterfaces();
			superModel = ifaces[0];
		}
		node.flagsChildren = getChildFlags(node.type);
		node.typeChildren = getChildTypes(node.type);
	}

	/*
	 * Called during the creation of the resource tree to initialize SimpleResourceData for Leaf nodes.
	 */
	private void initSimpleNode(TreeElementImpl node) {
		node.initDataContainer();
	}

	/*
	 * kypeKey is a field in treeElementImpl which says if the represented resource is a complex one or a simple one and
	 * which simple type it is. This method can not recognize non Resource class.
	 */
	int getTypeKeyFromClass(Class<?> cls) {
		// Is the type itself a basic or complex one
		// Basic types implement SimpleResource
		if (isSimple(cls)) {
			// FloatValues can be different resource types (TemperatureResource, ...). Persistence only needs to store
			// the basis type FloatResource.
			if (DBConstants.CLASS_FLOAT_TYPE.isAssignableFrom(cls)) {
				return DBConstants.TYPE_KEY_FLOAT;
			}
			else if (cls == DBConstants.CLASS_BOOL_ARR_TYPE) {
				return DBConstants.TYPE_KEY_BOOLEAN_ARR;
			}
			else if (cls == DBConstants.CLASS_BOOL_TYPE) {
				return DBConstants.TYPE_KEY_BOOLEAN;
			}
			else if (cls == DBConstants.CLASS_FLOAT_ARR_TYPE) {
				return DBConstants.TYPE_KEY_FLOAT_ARR;
			}
			else if (cls == DBConstants.CLASS_INT_ARR_TYPE) {
				return DBConstants.TYPE_KEY_INT_ARR;
			}
			else if (cls == DBConstants.CLASS_INT_TYPE) {
				return DBConstants.TYPE_KEY_INT;
			}
			else if (cls == DBConstants.CLASS_STRING_ARR_TYPE) {
				return DBConstants.TYPE_KEY_STRING_ARR;
			}
			else if (cls == DBConstants.CLASS_STRING_TYPE) {
				return DBConstants.TYPE_KEY_STRING;
			}
			else if (cls == DBConstants.CLASS_TIME_ARR_TYPE) {
				return DBConstants.TYPE_KEY_LONG_ARR;
			}
			else if (cls == DBConstants.CLASS_TIME_TYPE) {
				return DBConstants.TYPE_KEY_LONG;
			}
			else if (cls == DBConstants.CLASS_OPAQUE_TYPE) {
				return DBConstants.TYPE_KEY_OPAQUE;
			}
			else if (cls == DBConstants.CLASS_BYTE_ARR_TYPE) {
				return DBConstants.TYPE_KEY_OPAQUE;
			}
			else if (cls == ColourResource.class) {
				return DBConstants.TYPE_KEY_FLOAT_ARR;
			}
			else
				return DBConstants.NONSPECIFIC_VALUE;
		}
		else {
			/*
			 * The type is not simple. It must be any complex resource or the ResourceList.
			 */
			if (cls == DBConstants.CLASS_COMPLEX_ARR_TYPE)
				return DBConstants.TYPE_KEY_COMPLEX_ARR;
			else
				return DBConstants.TYPE_KEY_COMPLEX;
		}
	}

	/*
	 * Parse all direct children of a type each in an instance of TreeElementImpl as optionals of this TreeElement.
	 */
	private void parseComplex(final Class<?> type, TreeElementImpl node) {
		typeClassByName.put(node.typeName, node.type);

		node.typeChildren = getChildTypes(type);
		node.flagsChildren = getChildFlags(type);
	}

	/*
	 * Put all class definitions of the direct children of the type in a Vector instance.
	 */
	Collection<Class<?>> getTypeChildren0(Class<?> type) {
		Class<?> clazz;
		Method[] methods = type.getDeclaredMethods();
		int len = methods.length;
		Vector<Class<?>> result = new Vector<>(len);

		for (Method m : methods) {

			/*
			 * Type elements are detected as return type of a method which is derived from Resource
			 */
			clazz = m.getReturnType();
			if (isValidType(clazz)) {
				result.add(clazz.asSubclass(Resource.class));
			}
			else {
				// method doesn't represent a type element but its a
				// regular interface method. Such methods are ignored.
				if (Configuration.LOGGING)
					logger.debug("Invalid sub resource type ignored " + clazz.getName());
			}
		}
		return result;
	}

	/**
	 * Check if the type is a simple one or its a complex one extending Resource or its a complex one extending another
	 * complex type.
	 * 
	 * @param type
	 * @return
	 */
	boolean isValidType(Class<?> type) {
		return Resource.class.isAssignableFrom(type);
	}

	/**
	 * Check if a type is a simple one. A simple type is a type extending the marker interface SimpleResource.
	 * 
	 * @param type
	 * @return
	 */
	@SuppressWarnings("deprecation")
	boolean isSimple(Class<?> type) {
		return (org.ogema.core.model.SimpleResource.class.isAssignableFrom(type));
	}

	/**
	 * At first we have to check the compatibility of the data base with the new type definition. if resources of the
	 * type exist and the new model demands to add non-optional elements and if resources exist that are from the type
	 * to be updated or demands to remove elements that are present in an existing resource we have to reject the type
	 * registration.
	 */

	@Override
	public boolean hasResourceType(String name) {
		Class<?> res = typeClassByName.get(name);
		return (res != null);
	}

	Class<?> getResourceType(String name) {
		return typeClassByName.get(name);
	}

	@Override
	public List<Class<? extends Resource>> getAllResourceTypesInstalled() {
		// List<Class<? extends Resource>> rval = new ArrayList<>(typeClassByName.size());
		// for (Class<?> clazz : typeClassByName.values()) {
		// rval.add(clazz.asSubclass(Resource.class));
		// }
		Collection<Class<?>> col = typeClassByName.values();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Class<? extends Resource>> result = new ArrayList(col);
		return Collections.unmodifiableList(result);
	}

	@Override
	public List<Class<? extends Resource>> getResourceTypesInstalled(Class<? extends Resource> cls) {
		List<Class<? extends Resource>> rval = new ArrayList<>(typeClassByName.size());
		for (Class<?> clazz : typeClassByName.values()) {
			if (cls == null || cls.isAssignableFrom(clazz))
				rval.add(clazz.asSubclass(Resource.class));
		}
		return rval;
	}

	/**
	 * Setup a tree for a top level resource and register it and its sub resources in the dynamic tables. Generate an ID
	 * for the resource and all of the nodes in the tree.
	 * 
	 * @throws InvalidResourceTypeException
	 * @throws TypeNotFoundException
	 * 
	 */
	@Override
	public TreeElement addResource(String name, Class<? extends Resource> type, String appID)
			throws ResourceAlreadyExistsException, InvalidResourceTypeException {
		// check if a top level resource exists with the demanded name
		TreeElement tmp;
		tmp = getToplevelResource(name);
		if (tmp != null)
			throw new ResourceAlreadyExistsException("top level resource already exists: " + name);

		// check if the Type is already registered
		// ResourceList doesn't need to be registered, instead of its
		// type the annotated Type of the elements of the array is registered.
		if (!typeClassByName.containsValue(type) && (type != DBConstants.CLASS_COMPLEX_ARR_TYPE)) {
			addOrUpdateResourceType(type);
		}
		// init a node object for the top level resource
		TreeElementImpl e = new TreeElementImpl(this);
		/*
		 * If the new resource is of type ResourceList than the type info stay null until it gets a child which sets the
		 * type of all children added later.
		 */
		if (type != DBConstants.CLASS_COMPLEX_ARR_TYPE) {
			e.type = type;
			e.typeName = type.getName();
		}
		else {
			e.complexArray = true;
			e.typeKey = DBConstants.TYPE_KEY_COMPLEX_ARR;
		}
		e.appID = appID;
		e.name = name;
		e.path = name;
		e.parent = null;
		e.parentID = DBConstants.INVALID_ID;
		e.resRef = null;
		e.topLevelParent = e;
		e.toplevel = true;
		e.active = false;
		e.nonpersistent = false;
		e.decorator = false;

		// get a resourceID for this node
		int id = getNextresourceID();
		e.resID = id;

		// setup the tree for this type only if itsn't a ComplexArrayResourse
		if (type != DBConstants.CLASS_COMPLEX_ARR_TYPE)
			createTree(e);
		// put the generated tree in the table of the top level resources.
		root.put(e.name, e);
		registerRes(e);

		// inform persistence policy about the change
		// The first creation of a ResourceList is not relevant for persistence. ResourceLists's are persisted only if
		// the list type is set.
		if (activatePersistence && !e.complexArray)
			persistence.store(id, ChangeInfo.NEW_RESOURCE);

		return e;
	}

	/*
	 * ResourceDBImpl registers all resources in a set of tables for different access strategies. This method registers
	 * each resource created via addResource or addChild in these tables.
	 */
	synchronized void registerRes(TreeElementImpl e) {
		// register in table of id's by name as key
		resIDByName.put(e.path, e.resID);

		/*
		 * If e is a node of type ResourceList the type info is not yet known.
		 */
		Class<?> type;
		if (e.complexArray)
			type = DBConstants.CLASS_COMPLEX_ARR_TYPE;
		else
			type = e.type;
		if (type != null) {
			String name = type.getName();
			// register in table of id's by type as key
			Vector<Integer> v = resIDsByType.get(name);
			if (v == null) {
				v = new Vector<Integer>();
				resIDsByType.put(name, v);
			}

			if (!v.contains(e.resID))
				v.add(e.resID);
		}

		// register in table of nodes by id as type
		resNodeByID.put(e.resID, e);
	}

	/*
	 * The resources registered via registerResource have to be unregistered in case of deleteResource via calling of
	 * this method.
	 */
	void unRegisterRes(TreeElementImpl e) {
		// commit deletion of the resource to the storage policy.
		if (activatePersistence)
			persistence.store(e.resID, ChangeInfo.DELETED);

		// unregister in table of id's by name as key
		boolean exist = resIDByName.remove(e.path, e.resID);
		if (!exist)
			logger.error("Registration table resIDByName is corrupted!");
		/*
		 * If e is a node of type ResourceList the type info is not yet known.
		 */
		Class<?> type;
		if (e.complexArray)
			type = DBConstants.CLASS_COMPLEX_ARR_TYPE;
		else
			type = e.type;

		if (type != null) {
			// register in table of id's by type as key
			Vector<Integer> v = resIDsByType.get(type.getName());
			if (v == null) {
				logger.error("Registration table resIDsByType is corrupted!");
			}
			else {
				exist = v.remove(new Integer(e.resID));
				if (!exist)
					logger.error("Registration table resIDByName is corrupted!");
			}
		}
		synchronized (storageLock) {
			// register in table of nodes by id as type
			exist = resNodeByID.remove(e.resID, e);
		}
		if (!exist)
			logger.error("Registration table resNodeByID is corrupted!");
	}

	@Override
	public Collection<Class<?>> getTypeChildren(String name) {
		Class<?> cls = typeClassByName.get(name);
		if (cls == null)
			return null;

		Vector<Integer> v = resIDsByType.get(name);
		if (v == null || v.size() == 0) {
			return getTypeChildren0(cls);
		}
		else {
			int id = v.get(0);
			TreeElementImpl e = resNodeByID.get(id);
			return e.typeChildren.values();
		}
	}

	@Override
	public void deleteResource(TreeElement elem) {
		TreeElementImpl node = (TreeElementImpl) elem;
		// check if the resource exists
		if (!hasResource0(node))
			throw new ResourceNotFoundException(elem.toString());
		// check if the top level resource is known and remove it from the root
		// table
		if (node.toplevel) {
			root.remove(node.name);
		}

		/*
		 * If the node to be deleted is a top level one or a child of a ResourceList, the node and all of its sub
		 * resources are garbage. In other cases the nodes are moved from requireds to the optionals except in case that
		 * the sub resource is a decorator.
		 */
		boolean delete = node.toplevel || node.complexArray;
		removeTree(node, delete);
	}

	boolean hasResource0(TreeElementImpl elem) {
		if (elem == null)
			return false;
		TreeElementImpl e = root.get(elem.name);
		if (e != null && e.equals(elem))
			return true;
		else if ((e != null) && Configuration.LOGGING)
			logger.debug("A top level Resource exists with the same name: " + elem.name);
		e = resNodeByID.get(elem.resID);
		if (e != null && e.equals(elem))
			return true;
		else if ((e != null) && Configuration.LOGGING)
			logger.debug("A sub level Resource exists with the same name: " + elem.name);
		return false;
	}

	void removeTree(TreeElementImpl node, boolean delete) {

		/*
		 * If a part of a top level resource is deleted recycle the nodes and keeps them in the optionals of the
		 * parents. If isDecorating just remove it from requireds of the parent and drop it.
		 */
		TreeElementImpl parent = node.parent;
		if (parent != null)
			parent.requireds.remove(node.name);

		unRegisterRes(node);

		if (!node.requireds.isEmpty()) {
			// iterate over all of the children (optionals and requireds) and
			// delete their nodes.
			Set<Entry<String, TreeElementImpl>> tlrs = node.requireds.entrySet();
			for (Map.Entry<String, TreeElementImpl> entry : tlrs) {
				TreeElementImpl res = entry.getValue();
				removeTree(res, delete);
			}
		}

		if (!delete && !node.decorator && parent != null) {
			node.reset();
		}
	}

	@Override
	public boolean hasResource(String name) {
		// 1. check if there is a top level resource with the given name
		if (root.containsKey(name))
			return true;
		// 2. check if there is a sub resource with the given name
		// Integer id = resIDByName.get(name);
		// if (id != null)
		// return true;
		return false;
	}

	@Override
	public TreeElement getToplevelResource(String name) {
		// Check if an unloadable custom resource (UCR) is pending
		TreeElementImpl e;
		if (activatePersistence) {
			e = resourceIO.handleUCR(name, null);
			if (e != null)
				return e;
		}
		return root.get(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<TreeElement> getAllToplevelResources() {
		Vector<TreeElement> result = new Vector<TreeElement>(root.values());
		return (Collection<TreeElement>) result.clone();
	}

	@Override
	public Collection<TreeElement> getFilteredNodes(Map<String, String> dict) {
		HashSet<TreeElement> result = new HashSet<TreeElement>();
		String type = dict.get("type");
		String path = dict.get("path");
		String owner = dict.get("owner");
		String residStr = dict.get("id");

		boolean isRoot = residStr.equals("#");
		if (residStr != null && !isRoot) {

			int residInt = Integer.valueOf(residStr);
			TreeElementImpl element = resNodeByID.get(residInt);
			if (element != null)
				return element.getChildren();
			else
				return result;
		}

		// In case of root node id but not a top level path
		if (isRoot && path != null) {
			int firstslash = path.indexOf('/');
			if (firstslash == 0 && path.length() > 1) {
				path = path.substring(1);
				firstslash = path.indexOf('/');
			}
			int lastslash = path.lastIndexOf('/');
			if (firstslash != -1 && firstslash != lastslash)
				path = path.substring(0, firstslash);
		}

		// 1. filter by path
		if (path != null) {
			// normalize path info
			int end, lastindex = path.length(), begin = 0;
			end = lastindex;
			boolean wc = false;
			if (path.equals("*") || path.equals("/*") || path.equals("/")) {
				path = "";
				wc = true;
			}
			if (path.endsWith("/*")) {
				end -= 2;
				wc = true;
			}
			else if (path.endsWith("*")) {
				end -= 1;
				wc = true;
			}
			if (path.startsWith("/"))
				begin = 1;
			if (end != lastindex || begin != 0)
				path = path.substring(begin, end);

			// Handle the case if the path is '*' or '/*' only
			if (path.equals("/") || (path.equals("") && wc)) {
				Collection<TreeElementImpl> tops = resNodeByID.values();
				result.addAll(tops);
				// if a type specified additionally filter the results
				if (type != null)
					filterByType(tops, type, result);
				if (owner != null)
					filterByOwner(result, owner);
				return result;
			}

			// path has an unique value
			Integer id = resIDByName.get(path);
			TreeElementImpl te = null;
			if (id != null) {
				te = resNodeByID.get(id);
				if (!te.reference)
					result.add(te);
				if (owner != null) {
					if (!te.appID.equals(owner)) {
						result.remove(te);
						return result;
					}
				}
				if (type != null) {
					if (!te.typeName.equals(type)) {
						result.remove(te);
					}
				}
				return result;
			}
		}
		else

		// 2. filter by type (path is null)
		if (type != null) {
			Vector<Integer> ids = resIDsByType.get(type);
			if (ids != null)
				for (int id : ids) {
					TreeElementImpl e = resNodeByID.get(id);
					if (owner != null) {
						if (e.appID.equals(owner))
							if (!e.reference) {
								if (!e.isToplevel())
									result.add(e.topLevelParent);
								else
									result.add(e);
							}
					}
					else if (!e.reference) {
						if (!e.isToplevel())
							result.add(e.topLevelParent);
						else
							result.add(e);
					}
				}
			return result;
		}
		else
		// 3. filter by owner (path and type is null)
		if (owner != null) {
			Set<Entry<String, TreeElementImpl>> tops = root.entrySet();
			for (Entry<String, TreeElementImpl> entry : tops) {
				TreeElementImpl te = entry.getValue();
				if (te.appID.equals(owner))
					if (!te.reference)
						result.add(te);
			}
		}
		return result;
	}

	private void filterByOwner(HashSet<TreeElement> result, String owner) {
		Iterator<TreeElement> it = result.iterator();
		while (it.hasNext()) {
			TreeElement te = it.next();
			if (!te.getAppID().equals(owner))
				result.remove(te);
		}
	}

	private void filterByType(Collection<TreeElementImpl> tops, String type, Collection<TreeElement> result) {
		Class<?> filtercls = null;
		try {
			filtercls = Class.forName(type);
		} catch (ClassNotFoundException e) {
			return;
		}
		Iterator<TreeElementImpl> it = tops.iterator();
		while (it.hasNext()) {
			TreeElementImpl te = it.next();
			Class<?> cls = te.getType();
			if (!filtercls.isAssignableFrom(cls))
				result.remove(te);
		}
	}

	@Override
	public void finishTransaction() {
		if (activatePersistence)
			persistence.finishTransaction(0);
	}

	@Override
	public void startTransaction() {
		if (activatePersistence)
			persistence.startTransaction(0);
	}

	@Override
	public boolean isDBReady() {
		if (!activatePersistence)
			return true;
		else
			return dbReady;
	}

	@Override
	public synchronized void start(BundleContext context) throws Exception {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		registration = context.registerService(ResourceDB.class, this, null);
	}

	@Override
	public synchronized void stop(BundleContext context) throws Exception {
		if (registration != null) {
			registration.unregister();
		}
		registration = null;
		if (activatePersistence) {
			if (persistence != null) {
				persistence.triggerStorage();
				persistence.stopStorage();
			}
			if (resourceIO != null)
				resourceIO.closeAll();
		}

		if (persistence != null) {
			for (int i = 0; i < 30; i++) {
				if (!(((TimedPersistence) persistence).running))
					break;
				Thread.sleep(100);
			}
			if (((TimedPersistence) persistence).running) {
				logger.error("Could not shut down timed persistence");
			}
		}
		persistence = null;
		resourceIO = null;
		storageLock = null;
		root.clear();
		typeClassByName.clear();
		resIDByName.clear();
		resNodeByID.clear();
		resIDsByType.clear();
		// resTable.clear();
	}

	/*
	 * Used by the tests only
	 */
	synchronized void stopStorage() {
		logger.debug(((TimedPersistence) persistence).storageTask.toString());
		persistence.stopStorage();
		while (((TimedPersistence) persistence).running) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

	/*
	 * Used by the tests only
	 */
	synchronized void restart() {
		if (activatePersistence)
			stopStorage();
		logger.debug("Restart DB!");
		if (resourceIO != null)
			resourceIO.closeAll();
		root.clear();
		this.inited = false;
		typeClassByName = new ConcurrentHashMap<>(INITIAL_MAP_SIZE);
		resIDByName = new ConcurrentHashMap<>(INITIAL_MAP_SIZE);
		resNodeByID = new ConcurrentHashMap<>(INITIAL_MAP_SIZE);
		resIDsByType = new ConcurrentHashMap<>(INITIAL_MAP_SIZE);
		init();
	}

	protected void setName(String name) {
		this.name = name;
	}

	@Override
	public TreeElement getByID(int id) {
		return resNodeByID.get(id);
	}

	@Override
	public Map<String, Class<?>> getModelDeclaredChildren(String name) {
		Class<?> type = null;
		HashMap<String, Class<?>> result = new HashMap<>();
		try {
			type = Class.forName(name);
		} catch (ClassNotFoundException e) {
			return result;
		}
		Class<?> clazz;
		String typeName;
		Method[] methods = type.getDeclaredMethods();

		for (Method m : methods) {

			/*
			 * Type elements are detected as return type of a method which is derived from Resource
			 */
			clazz = m.getReturnType();
			typeName = m.getName();
			if (isValidType(clazz)) {
				result.put(typeName, clazz.asSubclass(Resource.class));
			}
			else {
				// method doesn't represent a type element but its a
				// regular interface method. Such methods are ignored.
				if (Configuration.LOGGING)
					System.err.println("Invalid sub resource type ignored " + clazz.getName());
			}
		}
		return result;
	}

	@Override
	public TreeElement getFilteredNodesByPath(String path, boolean isRoot) {

		// In case of root node id but not a top level path
		if (isRoot && path != null) {
			int firstslash = path.indexOf('/');
			if (firstslash == 0 && path.length() > 1) {
				path = path.substring(1);
				firstslash = path.indexOf('/');
			}
			int lastslash = path.lastIndexOf('/');
			if (firstslash != -1 && firstslash != lastslash)
				path = path.substring(0, firstslash);
		}

		TreeElementImpl te = null;
		// 1. filter by path
		if (path != null) {
			// normalize path info
			@SuppressWarnings("unused")
			int end, lastindex = path.length(), begin = 0;
			end = lastindex;

			// path has an unique value
			Integer id = resIDByName.get(path);
			if (id != null) {
				te = resNodeByID.get(id);
				if (!te.reference)
					return te;
			}
		}
		return te;
	}

	static final Class<?>[] emptyParams = {};

	Class<?> getListType(TreeElementImpl parent, final String chName) {
		Class<?> clazz;
		final Class<?> type = parent.type;
		Method m = AccessController.doPrivileged(new PrivilegedAction<Method>() {
			public Method run() {
				try {
					return type.getDeclaredMethod(chName, emptyParams);
				} catch (NoSuchMethodException e) {
					return null;
				}
			}
		});

		/*
		 * Type elements are detected as return type of a method which is derived from Resource
		 */
		if (m == null || (clazz = m.getReturnType()) != DBConstants.CLASS_COMPLEX_ARR_TYPE) {
			return null;
		}
		else {
			Type genericType = m.getGenericReturnType();
			if (genericType instanceof ParameterizedType) {
				Type[] actualTypes = ((ParameterizedType) genericType).getActualTypeArguments();
				if (actualTypes.length > 0) {
					clazz = (Class<?>) actualTypes[0];
				}
			}
			return clazz;
		}
	}

	Map<Class<?>, Map<String, Class<?>>> childTypes = new ConcurrentHashMap<>();
	Map<Class<?>, Map<String, Integer>> childFlags = new ConcurrentHashMap<>();

	protected Map<String, Class<?>> getChildTypes(Class<?> baseType) {
		Map<String, Class<?>> rval = childTypes.get(baseType);
		if (rval == null) {
			initChildMaps(baseType);
			rval = childTypes.get(baseType);
		}
		assert rval != null;
		return rval;
	}

	protected Map<String, Integer> getChildFlags(Class<?> baseType) {
		Map<String, Integer> rval = childFlags.get(baseType);
		if (rval == null) {
			initChildMaps(baseType);
			rval = childFlags.get(baseType);
		}
		assert rval != null;
		return rval;
	}

	private void initChildMaps(final Class<?> type) {
		Class<?> clazz;
		String name;
		Method[] methods = AccessController.doPrivileged(new PrivilegedAction<Method[]>() {
			public Method[] run() {
				return type.getMethods();
			}
		});

		Map<String, Class<?>> typesMap = new HashMap<>();
		Map<String, Integer> flagsMap = new HashMap<>();

		Annotation an;
		for (Method m : methods) {
			if (m.getDeclaringClass().equals(Resource.class)) {
				continue;
			}
			/*
			 * if (m.isBridge() || m.isSynthetic()) { continue; // skip overridden methods. }
			 */
			/*
			 * Type elements are detected as return type of a method which is derived from Resource
			 */
			clazz = m.getReturnType();
			name = m.getName();
			if (isValidType(clazz)) {
				// save the type class
				int typekey = getTypeKeyFromClass(clazz);
				// If neither SimpleResource nor Resource nor another complex
				// resource
				// is inherited by the data model
				// its an invalid one.
				if (typekey == DBConstants.TYPE_KEY_INVALID)
					throw new InvalidResourceTypeException(type.getName());

				/*
				 * if its a ComplexResourceType set the type of the elements as the type of the node.
				 */
				if (typekey == DBConstants.TYPE_KEY_COMPLEX_ARR) {

					Type genericType = m.getGenericReturnType();
					if (genericType instanceof ParameterizedType) {
						Type[] actualTypes = ((ParameterizedType) genericType).getActualTypeArguments();
						if (actualTypes.length > 0) {
							clazz = (Class<?>) actualTypes[0];
						}
					}
				}
				Class<?> cls = typesMap.get(name);
				if (cls == null)
					typesMap.put(name, clazz);

				Integer flags = flagsMap.get(name);

				an = m.getAnnotation(ModelModifiers.NonPersistent.class);
				if (an != null) {

					if (flags == null)
						flags = DBConstants.RES_NONPERSISTENT;
					else
						flags |= DBConstants.RES_NONPERSISTENT;
				}
				if (flags == null)
					flags = DBConstants.RES_ISCHILD;
				else
					flags |= DBConstants.RES_ISCHILD;
				flagsMap.put(name, flags);
				// register type definition of the child in the table of known
				// model definitions
				typeClassByName.put(clazz.getName(), clazz);
				// register type definition of the child in the table of known
				// model definitions
				typeClassByName.put(clazz.getName(), clazz);
			}
			else {
				// method doesn't represent a type element but its a
				// regular interface method. Such methods are ignored.
				if (Configuration.LOGGING)
					logger.debug("Invalid sub resource type ignored " + clazz.getName());
			}
		}
		childTypes.put(type, typesMap);
		childFlags.put(type, flagsMap);
	}

	@Override
	public void doStorage() {
		if (activatePersistence)
			persistence.triggerStorage();
	}
}
