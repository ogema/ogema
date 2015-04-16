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
	 * List of children which are defined as optional in the type definition and not yet added to the resource.
	 */
	public ConcurrentHashMap<String, TreeElementImpl> optionals;
	/*
	 * List of all children which are part of the type definition.
	 */
	public ConcurrentHashMap<String, Class<?>> typeChildren;

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
		requireds = new ConcurrentHashMap<String, TreeElementImpl>();
		optionals = new ConcurrentHashMap<String, TreeElementImpl>();
		typeChildren = new ConcurrentHashMap<String, Class<?>>();
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
			node = refered;
		return node.resRef;
	}

	public void setResRef(Object resRef) {
		// if this is a reference than addChild to the reference
		TreeElementImpl node = this;
		if (reference)
			node = refered;
		node.resRef = resRef;
		if (db.activatePersistence) {
			store(ChangeInfo.STATUS_CHANGED);
		}
	}

	public boolean isActive() {
		// if this is a reference than addChild to the reference
		TreeElementImpl node = this;
		if (reference)
			node = refered;
		return node.active;
	}

	public void setActive(boolean active) {
		// if this is a reference than set the referenced node as active
		TreeElementImpl node = this;
		if (reference)
			node = refered;
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
		// if the reference to be added is also a reference
		// or this-Object is also a reference
		// the operation is not supported
		// if (refimpl.reference || this.reference)
		// throw new UnsupportedOperationException();
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
		 * If this is a reference the call is delegated to the refered object.
		 */
		if (this.reference)
			return refered.addReference(ref, refName, decorating);

		// check if the demanded model member exists and has the right type
		TreeElementImpl e = optionals.get(refName);
		if (!decorating) {
			if ((e == null))
				throw new ResourceNotFoundException(refName);
			// Check if the reference to be added has the right type.
			// If the reference is a complexArray the child has to such a type
			// too
			if ((refimpl.complexArray && !e.complexArray) || (!refimpl.complexArray && e.complexArray)) {
				throw new InvalidResourceTypeException(refimpl.getName());
			}
			// If the child is complexArray but the type is not yet set, its set
			// equal to the type of the reference
			if (e.complexArray) {
				e.type = refimpl.type;
			}
			// If the child is not complexArray the references type has to be an
			// ancestor of the childs type.
			// else if (!isAncestor(e.type, refimpl.type)) {
			// throw new InvalidResourceTypeException(refimpl.getName());
			// }
			else if (!e.type.isAssignableFrom(refimpl.type)) {
				throw new InvalidResourceTypeException(refimpl.getName());
			}

		}
		TreeElementImpl result;
		// for a decorator we need a new node object whereas a node object for a
		// model member already exists.
		if (decorating) {
			result = new TreeElementImpl(db);
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
			result.complexArray = refimpl.complexArray;

		}
		else {
			result = e;
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

		// remove child from optionals and add it to the requireds
		if (!decorating) {
			optionals.remove(result.name);
		}
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

	// private boolean isAncestor(Class<?> ancestor, Class<?> child) {
	// if (child == ancestor)
	// return true;
	// Class<?> superModel = child;
	// Class<?>[] ifaces;
	// while ((superModel != DBConstants.CLASS_BASIC_TYPE) && (superModel != DBConstants.CLASS_SIMPLE_TYPE)) {
	//
	// ifaces = superModel.getInterfaces();
	//
	// superModel = ifaces[0];
	// if (superModel == ancestor)
	// return true;
	// }
	// return false;
	// }

	@Override
	public TreeElement addChild(String chName, Class<? extends Resource> chType, boolean isDecorating)
			throws ResourceAlreadyExistsException, ResourceNotFoundException, InvalidResourceTypeException {
		// check if the resource exists
		if (!db.hasResource0(this))
			throw new ResourceNotFoundException(this.toString());
		// if this is a reference than addChild to the reference
		TreeElementImpl node = this;
		if (reference)
			node = refered;
		/*
		 * add child to node check if a child with this name already added.
		 */
		if (node.requireds.containsKey(chName))
			throw new ResourceAlreadyExistsException(chName);
		/*
		 * If a child to be added to a ResourceList some exceptions are to be considered
		 */
		if (node.complexArray)
			return node.addCompArrChild(chName, chType, isDecorating);

		/*
		 * if a decorator is to be added its name mustn't match the name of a model member
		 */
		TreeElementImpl e = node.optionals.get(chName);
		boolean isChild = (e != null);
		boolean typeMatch = isChild && ((e.complexArray && chType == ResourceList.class) || (e.type == chType));
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
		// setup the tree for this type only if itsn't a ComplexArrayResourse
		if ((result != null && !result.complexArray && result.type != null))
			db.createTree(result);
		db.registerRes(result);

		// remove child from optionals and add it to the required
		if (!isDecorating) {
			node.optionals.remove(result.name);
		}
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
			throw new UnsupportedOperationException("Adding of a child to a ComplexResourceArray with a wrong tpye: "
					+ chType.getName());
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
						"Adding of a child to a ComplexResourceArray with a wrong tpye: " + chType.getName());
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
		/*
		 * If a complexArray is to be added as element operation is unsupported.
		 */
		if (refimpl.complexArray) {
			throw new UnsupportedOperationException(
					"Adding of a referencewith a wrong tpye to a ComplexResourceArray : " + refimpl.typeName);
		}

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
				// if (isAncestor(this.type, refimpl.type)) {
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
			node = refered;
		Vector<TreeElement> v = new Vector<TreeElement>(node.requireds.values());
		return v;
	}

	@Override
	public TreeElement getChild(String name) {
		// check if the resource exists
		if (!db.hasResource0(this))
			throw new ResourceNotFoundException(this.toString());
		// if this is a reference than addChild to the reference
		TreeElementImpl node = this;
		if (reference)
			node = refered;
		node = node.requireds.get(name);
		return node;
	}

	void initDataContainer() {
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
		if (!this.nonpersistent)
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
			TreeElementImpl node = this;
			if (reference) {
				node = refered;
			}
			Class<?> cls = node.type;
			if (cls != null) {
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
			if (type != null && type != cls)
				throw new InvalidResourceTypeException("ResourceList type already set to " + type.getName());
			else
				type = cls;
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
			try {
				te = te.getReference();
			} catch (UnsupportedOperationException e) {
				result = te.getPath();
				break;
			}
		}
		return result;
	}
}
