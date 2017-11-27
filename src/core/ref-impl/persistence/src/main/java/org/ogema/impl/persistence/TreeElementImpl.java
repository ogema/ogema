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

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceNotFoundException;
import org.ogema.persistence.DBConstants;
import org.ogema.persistence.PersistencePolicy;
import org.ogema.persistence.PersistencePolicy.ChangeInfo;
import org.ogema.resourcetree.SimpleResourceData;
import org.ogema.resourcetree.TreeElement;

public class TreeElementImpl implements TreeElement {

	/*
	 * List of children which are defined as non-optional in the type definition or added as optional or decorator.
	 */
	public ConcurrentHashMap<String, TreeElementImpl> requireds;
	/*
	 * List of all children which are part of the type definition.
	 */
	public Map<String, Class<?>> typeChildren;
	public Map<String, Integer> flagsChildren;

	public Object resRef;
	public TreeElementImpl parent;
	public TreeElementImpl topLevelParent;

	/*
	 * If the node is a reference this field holds the referenced node.
	 */
	public TreeElementImpl refered;
	public int parentID;
	public int resID;
	public int typeKey;
	public String name;
	public Class<?> type;
	public String typeName;
	String appID;
	public int refID;

	public boolean optional;
	public boolean nonpersistent;
	public boolean decorator;
	public boolean active;
	public boolean toplevel;
	public boolean reference;
	public boolean complexArray;

	// resource path information which consists of the names of all parents up
	// to the top level resource in the tree
	public String path;

	// The following fields both are the same Reference where dataContainer is
	// the interface with the getter/setter to the primitive values and
	// simpleValue is instance of the implementing class for ResourceDB internal
	// use.
	SimpleResourceData dataContainer;
	LeafValue simpleValue;

	ResourceDBImpl db;
	int footprint;

	/*
	 * Last modified time stamp of the node
	 */
	long lastModified = -1;

	public TreeElementImpl(ResourceDBImpl db) {
		this.db = db;
		requireds = new ConcurrentHashMap<>();
		typeKey = DBConstants.TYPE_KEY_INVALID;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + this.resID;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final TreeElementImpl other = (TreeElementImpl) obj;
		return this.resID == other.resID;
	}

	public String getAppID() {
		return appID;
	}

	public void setAppID(String appID) {
		this.appID = appID;
		if (db.activatePersistence) {
			store(ChangeInfo.STATUS_CHANGED);
		}
	}

	@Override
	public SimpleResourceData getData() {
		// check if the resource exists
		if (!db.hasResource0(this))
			throw new ResourceNotFoundException(this.toString());

		if (reference)
			return refered.getData();
		// if its not a simple resource throw a UnsupportedOperationException
		if (dataContainer == null)
			throw new UnsupportedOperationException("Resource of type " + getType().getCanonicalName()
					+ " is not registered as a simple resource. Cannot return a data container for it. Type key is "
					+ getTypeKey());
		return dataContainer;
	}

	public Object getResRef() {
		// if this is a reference than addChild to the reference
		TreeElementImpl node = this;
		if (reference)
			return refered.getResRef();
		return node.resRef;
	}

	public void setResRef(Object resRef) {
		// if this is a reference than addChild to the reference
		TreeElementImpl node = this;
		if (reference) {
			refered.setResRef(resRef);
			return;
		}
		node.resRef = resRef;
		if (db.activatePersistence) {
			store(ChangeInfo.STATUS_CHANGED);
		}
	}

	public boolean isActive() {
		// if this is a reference than addChild to the reference
		TreeElementImpl node = this;
		if (reference)
			return refered.isActive();
		return node.active;
	}

	public void setActive(boolean active) {
		// if this is a reference than set the referenced node as active
		TreeElementImpl node = this;
		if (reference) {
			refered.setActive(active);
			return;
		}
		node.active = active;
		if (db.activatePersistence) {
			store(ChangeInfo.STATUS_CHANGED);
		}
	}

	public TreeElementImpl getParent() {
		return parent;
	}

	public int getResID() {
		return resID;
	}

	public int getTypeKey() {
		return typeKey;
	}

	public String getName() {
		return name;
	}

	public Class<? extends Resource> getType() {
		if (complexArray) {
			return ResourceList.class;
		}
		// if this is a reference than get the type of the referenced node
		TreeElementImpl node = this;
		if (reference)
			node = refered;
		Class<?> cls = node.type;
		if (cls != null)
			return cls.asSubclass(Resource.class);
		else
			return null;
	}

	public boolean isNonpersistent() {
		return nonpersistent;
	}

	public boolean isDecorator() {
		return decorator;
	}

	public boolean isToplevel() {
		return toplevel;
	}

	public boolean isReference() {
		return reference;
	}

	@Override
	public boolean isComplexArray() {
		return complexArray;
	}

	public int getFlags() {
		TreeElementImpl node = this;
		/**
		 * Resource status flags which are to be stored persistently
		 * 
		 * RES_VOLATILE = 1 << 0; RES_NONPERSISTENT = 1 << 1; RES_COMPLEX_ARRAY = 1 << 2; RES_ACTIVE = 1 << 3;
		 * RES_TOPLEVEL = 1 << 4; RES_DECORATOR = 1 << 5;
		 */
		int flags = 0;
		if (node.complexArray)
			flags |= DBConstants.RES_COMPLEX_ARRAY;
		if (node.nonpersistent)
			flags |= DBConstants.RES_NONPERSISTENT;
		if (node.decorator)
			flags |= DBConstants.RES_DECORATOR;
		if (node.active)
			flags |= DBConstants.RES_ACTIVE;
		if (node.toplevel)
			flags |= DBConstants.RES_TOPLEVEL;
		if (node.reference)
			flags |= DBConstants.RES_REFERENCE;
		return flags;
	}

	public void setFlags(int flags) {
		/**
		 * Resource status flags which are to be stored persistently
		 * 
		 * RES_VOLATILE = 1 << 0; RES_NONPERSISTENT = 1 << 1; RES_COMPLEX_ARRAY = 1 << 2; RES_ACTIVE = 1 << 3;
		 * RES_TOPLEVEL = 1 << 4; RES_DECORATOR = 1 << 5;
		 */
		if ((flags & DBConstants.RES_COMPLEX_ARRAY) != 0)
			this.complexArray = true;
		if ((flags & DBConstants.RES_NONPERSISTENT) != 0)
			this.nonpersistent = true;
		if ((flags & DBConstants.RES_DECORATOR) != 0)
			this.decorator = true;
		if ((flags & DBConstants.RES_ACTIVE) != 0)
			this.active = true;
		if ((flags & DBConstants.RES_TOPLEVEL) != 0)
			this.toplevel = true;
		if ((flags & DBConstants.RES_REFERENCE) != 0)
			this.reference = true;
	}

	// TODO split this method in two for the cases decorating and !decorating
	@Override
	public TreeElement addReference(TreeElement ref, String refName, boolean decorating) {
		// check if the resource exists
		if (!db.hasResource0(this))
			throw new ResourceNotFoundException(this.toString());

		TreeElementImpl refimpl = (TreeElementImpl) ref;

		// Check if the reference causes a loop in the resource graph
		if (checkRefLoop(refimpl))
			throw new UnsupportedOperationException();
		// check if a child with this name already exists.
		if (requireds.containsKey(refName))
			throw new ResourceAlreadyExistsException(refName);

		/*
		 * If the reference to be added to a ResourceList call the special implementation method
		 */
		if (this.complexArray)
			return addCompArrRef(ref, refName, decorating);

		/*
		 * If this is a reference the call is delegated to the referred object.
		 */
		if (this.reference)
			return refered.addReference(ref, refName, decorating);

		if (refimpl.type == null && refimpl.typeKey == DBConstants.TYPE_KEY_COMPLEX_ARR)
			refimpl.type = DBConstants.CLASS_COMPLEX_ARR_TYPE;
		// check if the demanded model member exists and has the right type
		TreeElementImpl result = initChild(refName, refimpl.type);

		@SuppressWarnings("unused")
		boolean isComplexArr = refimpl.complexArray;// !db.isSimple(refimpl.type);
		Integer flag = flagsChildren.get(refName);
		@SuppressWarnings("unused")
		boolean isChild;
		if (flag != null && (flag & DBConstants.RES_ISCHILD) != 0)
			isChild = true;

		result.complexArray = refimpl.complexArray;

		// for a decorator we need a new node object whereas a node object for a
		// model member already exists.
		if (decorating) {
			result.appID = topLevelParent.appID;
			result.type = refimpl.type;
			result.typeName = refimpl.typeName;
			result.name = refName;
			result.path = this.path + DBConstants.PATH_SEPARATOR + refName;
			result.parent = this;
			result.parentID = this.resID;
			result.resRef = null;
			result.topLevelParent = topLevelParent;
			result.toplevel = false;
			result.active = false;
			result.nonpersistent = false;
			result.decorator = true;
		}
		else {

			// Determine the type of the ResourceList member. In case of decorating the application has to set the type
			// via
			// ResourceList#setElementType(Class<? extends Resource>)
			if (result.complexArray)
				result.type = db.getListType(result.parent, refName);

			// If the child is not complexArray the references type has to be an
			// ancestor of the childs type.
			else if (!result.type.isAssignableFrom(refimpl.type)) {
				throw new InvalidResourceTypeException(refimpl.getName());
			}
			// typeChildren.put(refName, refimpl.type);
		}

		// the difference of a reference to a child comes now
		result.refered = refimpl;
		result.reference = true;
		result.refID = refimpl.resID;
		result.typeKey = refimpl.typeKey;
		// get a resourceID for this node
		int id = db.getNextresourceID();
		result.resID = id;
		// setup the tree for this type
		// db.createTree(result);
		db.registerRes(result);

		requireds.put(result.name, result);
		if (db.activatePersistence)
			db.persistence.store(id, PersistencePolicy.ChangeInfo.NEW_SUBRESOURCE);
		return result;
	}

	private boolean checkRefLoop(TreeElementImpl refimpl) {
		TreeElementImpl e = refimpl.refered;
		while (e != null && e.reference) {
			if (e == refimpl)
				return true;
			e = e.refered;
		}
		return false;
	}

	@Override
	public TreeElement addChild(String chName, Class<? extends Resource> chType, boolean isDecorating)
			throws ResourceAlreadyExistsException, ResourceNotFoundException, InvalidResourceTypeException {
		// check if the resource exists
		if (!db.hasResource0(this))
			throw new ResourceNotFoundException(this.toString());
		// if this is a reference than addChild to the reference
		if (reference)
			return refered.addChild(chName, chType, isDecorating);// node = refered;
		/*
		 * add child to node check if a child with this name already added.
		 */
		TreeElementImpl node = this;
		if (node.requireds.containsKey(chName))
			throw new ResourceAlreadyExistsException(chName);
		/*
		 * If a child to be added to a ResourceList some exceptions are to be considered
		 */
		if (node.complexArray)
			return node.addCompArrChild(chName, chType, isDecorating);

		// Check if an unloadable custom resource (UCR) is pending
		TreeElementImpl e;
		if (db.activatePersistence) {
			e = db.resourceIO.handleUCR(this.path + "/" + chName, chType);
			if (e != null)
				return e;
		}

		/*
		 * if a decorator is to be added its name mustn't match the name of a model member
		 */
		e = initChild(chName, chType);
		Integer flags = flagsChildren.get(chName);
		boolean isChild = flags != null && (flags & DBConstants.RES_ISCHILD) != 0;
		boolean typeMatch = isChild
				&& ((e.complexArray && chType == ResourceList.class) || (e.type.isAssignableFrom(chType)));
		/*
		 * The demanded model member doesn't exist as optional member and it is not a decorating one.
		 */
		if ((!isDecorating && !isChild))
			throw new ResourceNotFoundException(chName);
		/*
		 * Try to add a optional child where the type definition doesn't match the desired one.
		 */
		if ((!isDecorating && isChild && !typeMatch))
			throw new ResourceNotFoundException(chName);
		/*
		 * Try to decorate a resource with a child which is a part of the type definition as a optional element.
		 */
		if ((isDecorating && isChild))
			throw new ResourceAlreadyExistsException(chName);

		TreeElementImpl result;
		/*
		 * for a decorator we need a new node object whereas a node object for a model member already exists. At this
		 * point a member of a complexArrayResource is threaten like a decorator
		 */
		if (isDecorating) {
			result = new TreeElementImpl(db);
			result.appID = node.topLevelParent.appID;
			/*
			 * If the child is of type ResourceList than the type info stay null until it gets a child which sets the
			 * type of all children added later.
			 */
			if (chType != DBConstants.CLASS_COMPLEX_ARR_TYPE) {
				result.type = chType;
				result.typeName = chType.getName();
			}
			else {
				result.complexArray = true;
				result.typeKey = DBConstants.TYPE_KEY_COMPLEX_ARR;
			}
			result.name = chName;
			result.path = node.path + DBConstants.PATH_SEPARATOR + chName;
			result.parent = node;
			result.parentID = node.resID;
			result.resRef = null;
			result.topLevelParent = node.topLevelParent;
			result.toplevel = false;
			result.active = false;
			result.nonpersistent = false;
			result.decorator = true;
		}
		else {
			result = e;
		}
		// get a resourceID for this node
		int id = db.getNextresourceID();
		result.resID = id;
		result.parentID = node.resID;
		// setup the tree for this type only if itsn't a ComplexArrayResourse
		if ((result != null && !result.complexArray && result.type != null))
			db.createTree(result);

		// Determine the type of the ResourceList member. In case of decorating the application has to set the type via
		// ResourceList#setElementType(Class<? extends Resource>)
		if (result.complexArray && !isDecorating)
			result.type = db.getListType(result.parent, result.name);

		db.registerRes(result);

		node.requireds.put(result.name, result);
		if (db.activatePersistence)
			db.persistence.store(id, PersistencePolicy.ChangeInfo.NEW_SUBRESOURCE);
		return result;
	}

	public TreeElement addCompArrChild(String chName, Class<? extends Resource> chType, boolean isDecorating) {

		/*
		 * If a complexArray is to be added as element operation is unsupported.
		 */
		if (chType == DBConstants.CLASS_COMPLEX_ARR_TYPE) {
			throw new UnsupportedOperationException(
					"Adding of a child to a ComplexResourceArray with a wrong type: " + chType.getName());
		}
		TreeElementImpl result = new TreeElementImpl(db);
		/*
		 * If the child to be added is not the first one in the ResourceList, the type has to match the array members
		 * before.
		 */
		if (!isDecorating) {
			if (this.type == null) {
				this.type = result.type = chType;
				this.typeName = result.typeName = chType.getName();
			}
			else if (chType != this.type) {
				throw new UnsupportedOperationException(
						"Adding of a child to a ComplexResourceArray with a wrong type: " + chType.getName());
			}
			else {
				result.type = chType;
				result.typeName = chType.getName();
			}
		}
		/* If a decorator is to be added, any type is accepted. */
		else {
			result.type = chType;
			result.typeName = chType.getName();
		}
		result.appID = this.topLevelParent.appID;
		result.name = chName;
		result.path = this.path + DBConstants.PATH_SEPARATOR + chName;
		result.parent = this;
		result.parentID = this.resID;
		result.resRef = null;
		result.topLevelParent = this.topLevelParent;
		result.toplevel = false;
		result.active = false;
		result.nonpersistent = this.nonpersistent;
		result.decorator = isDecorating;
		// get a resourceID for this node
		int id = db.getNextresourceID();
		result.resID = id;
		// setup the tree for this type
		db.createTree(result);
		db.registerRes(result);

		this.requireds.put(result.name, result);
		if (db.activatePersistence)
			db.persistence.store(id, PersistencePolicy.ChangeInfo.NEW_SUBRESOURCE);
		return result;
	}

	public TreeElement addCompArrRef(TreeElement ref, String refName, boolean decorating) {
		TreeElementImpl refimpl = (TreeElementImpl) ref;

		// check if the reference to be added has the right type
		if (!decorating) {
			if (!isCompArrRefCompatible(refimpl))
				throw new UnsupportedOperationException(refimpl.getName()
						+ " can't be added as reference to the ResourceList of type " + this.typeName);
		}
		TreeElementImpl result = new TreeElementImpl(db);
		result.appID = topLevelParent.appID;
		result.type = refimpl.type;
		result.typeName = refimpl.typeName;
		result.name = refName;
		result.path = this.path + DBConstants.PATH_SEPARATOR + refName;
		result.parent = this;
		result.parentID = this.resID;
		result.resRef = null;
		result.topLevelParent = topLevelParent;
		result.toplevel = false;
		result.active = false;
		result.nonpersistent = this.nonpersistent;
		result.decorator = decorating;

		// the difference of a reference to a child comes now
		result.refered = refimpl;
		result.reference = true;
		result.refID = refimpl.resID;
		result.typeKey = refimpl.typeKey;

		// get a resourceID for this node
		int id = db.getNextresourceID();
		result.resID = id;
		db.registerRes(result);

		requireds.put(result.name, result);
		if (db.activatePersistence)
			db.persistence.store(id, PersistencePolicy.ChangeInfo.NEW_SUBRESOURCE);
		return result;
	}

	/**
	 * Checks if this ResourceList is compatible with a node to be added as reference.
	 * 
	 * @param refimpl
	 * @return
	 */
	private boolean isCompArrRefCompatible(TreeElementImpl refimpl) {
		/*
		 * If this is not a ResourceList result is negative
		 */
		if (!this.complexArray)
			return false;
		/*
		 * If the type of the ResourceList is already set, the type must an ancestor of the reference.
		 */
		if (this.type != null) {
			if (this.type.isAssignableFrom(refimpl.type)) {
				return true;
			}
			else
				return false;
		}
		else
			this.type = refimpl.type;

		/*
		 * If the type of the ResourceList is not set yet, its compatible any new type to be added.
		 */
		return true;
	}

	@Override
	public List<TreeElement> getChildren() {
		// check if the resource exists
		if (!db.hasResource0(this))
			throw new ResourceNotFoundException(this.toString());

		// if this is a reference than addChild to the reference
		TreeElementImpl node = this;
		if (reference)
			return refered.getChildren();
		Vector<TreeElement> v = new Vector<TreeElement>(node.requireds.values());
		return v;
	}

	@Override
	public TreeElement getChild(String name) {
		// check if the resource exists
		if (!db.hasResource0(this))
			throw new ResourceNotFoundException(this.toString() + ", subresource: " + name);
		// if this is a reference than addChild to the reference
		TreeElementImpl node = this;
		if (reference)
			node = (TreeElementImpl) refered.getChild(name);
		else
			node = node.requireds.get(name);
		return node;
	}

	void initDataContainer() {
		// For complex and complex array resources is nothing todo
		if (typeKey == DBConstants.TYPE_KEY_COMPLEX || typeKey == DBConstants.TYPE_KEY_COMPLEX_ARR)
			return;
		// if this is a reference than addChild to the reference
		TreeElementImpl node = this;
		LeafValue value = new LeafValue(this);
		node.dataContainer = (SimpleResourceData) value;// (SimpleResourceData)
		// instance;
		node.simpleValue = value;

		switch (typeKey) {
		case DBConstants.TYPE_KEY_BOOLEAN:
			value.footprint = 1;
		case DBConstants.TYPE_KEY_FLOAT:
		case DBConstants.TYPE_KEY_INT:
			value.footprint = 4;
			break;
		case DBConstants.TYPE_KEY_LONG:
			value.footprint = 8;
			break; // nothing to do
		case DBConstants.TYPE_KEY_STRING:
			value.S = "";
			break;
		case DBConstants.TYPE_KEY_OPAQUE:
		case DBConstants.TYPE_KEY_INT_ARR:
		case DBConstants.TYPE_KEY_FLOAT_ARR:
		case DBConstants.TYPE_KEY_BOOLEAN_ARR:
		case DBConstants.TYPE_KEY_STRING_ARR:
		case DBConstants.TYPE_KEY_LONG_ARR:
		case DBConstants.TYPE_KEY_COMPLEX_ARR:
			break;
		default:
			break;
		}
	}

	@Override
	public TreeElement getReference() {
		if (reference)
			return refered;
		else
			throw new UnsupportedOperationException("The sub resource is not a reference: " + name);
	}

	public String toString() {
		return "ResourceType: " + typeName + " ResourcePath: " + path;
	}

	public void reset() {
		resRef = null;
		complexArray = false;
		resID = DBConstants.INVALID_ID;
		refered = null;
		reference = false;
		refID = DBConstants.INVALID_ID;
		typeKey = DBConstants.TYPE_KEY_INVALID;
	}

	public boolean compare(TreeElementImpl e) {

		if ((e.parent != null && this.parent != null) && (!e.parent.compare(this.parent)))
			return false;

		if (e.reference != reference)
			return false;

		if (e.refered != null && refered != null)
			if (!e.refered.compare(refered))
				return false;
		if (e.parentID != parentID)
			return false;
		if (e.resID != resID)
			return false;
		if (e.typeKey != typeKey)
			return false;
		if (!e.name.equals(name))
			return false;
		if (e.type != (type))
			return false;
		if (!e.typeName.equals(typeName))
			return false;
		if (!e.appID.equals(appID))
			return false;
		if (e.refID != refID)
			return false;
		if (e.nonpersistent != nonpersistent)
			return false;
		if (e.decorator != decorator)
			return false;
		if (e.active != active)
			return false;
		if (e.toplevel != toplevel)
			return false;
		if (e.reference != reference)
			return false;
		if (e.complexArray != complexArray)
			return false;
		if (!e.path.equals(path))
			return false;
		return true;
	}

	void store(ChangeInfo change) {
		if (!this.nonpersistent || !change.equals(ChangeInfo.VALUE_CHANGED))
			db.persistence.store(this.resID, change);
	}

	@Override
	public void fireChangeEvent() {
		if (db.activatePersistence)
			store(ChangeInfo.VALUE_CHANGED);
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public Class<? extends Resource> getResourceListType() {
		if (complexArray) {
			if (reference) {
				return refered.getResourceListType();
			}
			final Class<?> cls = type;
			if (cls != null) {
				if (cls == DBConstants.CLASS_COMPLEX_ARR_TYPE)
					return null;
				return cls.asSubclass(Resource.class);
			}
			else {
				return null;
			}
		}
		else
			throw new InvalidResourceTypeException("TreeElement is not an instance of ResourceList but of " + typeName);
	}

	@Override
	public void setResourceListType(Class<? extends Resource> cls) {
		if (complexArray) {
			if (reference) {
				refered.setResourceListType(cls);
				return;
			}
			if (type != null && type != DBConstants.CLASS_COMPLEX_ARR_TYPE && type != cls)
				throw new InvalidResourceTypeException("ResourceList type already set to " + type.getName());
			else {
				type = cls;
				typeName = cls.getName();
				if (db.activatePersistence)
					db.persistence.store(resID, ChangeInfo.STATUS_CHANGED);
			}
		}
		else
			throw new InvalidResourceTypeException("TreeElement is not an instance of ResourceList but of " + typeName);
	}

	@Override
	public void setLastModified(long time) {
		this.lastModified = time;
		if (db.activatePersistence) {
			store(ChangeInfo.STATUS_CHANGED);
		}
	}

	@Override
	public long getLastModified() {
		return lastModified;
	}

	@Override
	public String getLocation() {
		String result = path;
		TreeElement te = this;
		while (true) {
			if (te.isReference()) {
				te = te.getReference();
			}
			else {
				result = te.getPath();
				break;
			}
		}
		return result;
	}

	public TreeElementImpl initChild(String chName, Class<?> clazz) {
		TreeElementImpl elem = requireds.get(chName);
		if (elem == null) {
			// Hash sub resource as child owned by the model definition
			TreeElementImpl e = new TreeElementImpl(db);
			// save the type class
			int typekey = e.typeKey;
			typekey = e.typeKey = db.getTypeKeyFromClass(clazz);
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
				e.complexArray = true;
			}

			// init node
			// a resourceID is generated if the child is added as a required
			// child.
			/*
			 * e.type = typeChildren.get(chName); if (e.type != null) e.typeName = e.type.getName();
			 */
			e.type = clazz;
			Class<?> definedType = typeChildren.get(chName);
			if (definedType != null) {
				e.typeName = clazz.getName();
			}

			e.parent = this;
			e.parentID = this.resID;
			e.toplevel = false; // its a child of a parent node, so never
			// top level
			e.topLevelParent = this.topLevelParent;
			e.appID = this.appID;
			e.name = chName;
			e.path = this.path + DBConstants.PATH_SEPARATOR + chName;
			e.active = false;

			Integer flags = flagsChildren.get(chName);
			if (flags != null && (flags & DBConstants.RES_NONPERSISTENT) != 0)
				e.nonpersistent = true;

			// register type definition of the child in the table of known
			// model definitions
			db.typeClassByName.put(clazz.getName(), clazz);
			return e;
		}
		else
			return elem;
	}
}
