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
package org.ogema.resourcemanager.virtual;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.array.ArrayResource;
import org.ogema.core.model.array.BooleanArrayResource;
import org.ogema.core.model.array.ByteArrayResource;
import org.ogema.core.model.array.FloatArrayResource;
import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.array.TimeArrayResource;
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceNotFoundException;
import org.ogema.core.resourcemanager.VirtualResourceException;
import org.ogema.persistence.impl.mem.MemoryTreeElement;
import org.ogema.resourcemanager.impl.ElementInfo;
import org.ogema.resourcetree.SimpleResourceData;
import org.ogema.resourcetree.TreeElement;

/**
 *
 * @author jlapp
 */
public class DefaultVirtualTreeElement implements VirtualTreeElement {

	volatile private TreeElement el;
	volatile Object resref;

	final DefaultVirtualResourceDB resourceDB;
	final Map<String, DefaultVirtualTreeElement> virtualSubresources = new HashMap<>();

	public DefaultVirtualTreeElement(TreeElement el, DefaultVirtualResourceDB resourceDB) {
		Objects.requireNonNull(el, "TreeElement must not be null");
		this.el = getRealElement(el);
		resref = this.el.getResRef();
		this.resourceDB = resourceDB;
	}

	private TreeElement getRealElement(TreeElement el) {
		while (el instanceof DefaultVirtualTreeElement) {
			el = ((DefaultVirtualTreeElement) el).el;
		}
		return el;
	}

	@Override
	public String toString() {
		return "DefaultVirtualTreeElement " + getName();
	}

	@Override
	public String getAppID() {
		return el.getAppID();
	}

	@Override
	public void setAppID(String appID) {
		el.setAppID(appID);
	}

	@Override
	public Object getResRef() {
		return el.getResRef();
	}

	@Override
	public void setResRef(Object resRef) {
		el.setResRef(resRef);
	}

	@Override
	public boolean isActive() {
		return !isVirtual() && el.isActive();
	}

	@Override
	public void setActive(boolean active) {
		if (isVirtual() && active) {
			throw new VirtualResourceException(String.format("Resource %s is virtual, cannot activate", getPath()));
		}
		el.setActive(active);
	}

	@Override
	public DefaultVirtualTreeElement getParent() {
		TreeElement parent = el.getParent();
		return parent != null ? resourceDB.getElement(parent) : null;
	}

	@Override
	public int getResID() {
		return el.getResID();
	}

	@Override
	public int getTypeKey() {
		return el.getTypeKey();
	}

	@Override
	public String getName() {
		return el.getName();
	}

	@Override
	public Class<? extends Resource> getType() {
		return el.getType();
	}

	@Override
	public boolean isNonpersistent() {
		return el.isNonpersistent();
	}

	@Override
	public boolean isDecorator() {
		return el.isDecorator();
	}

	@Override
	public boolean isToplevel() {
		return el.isToplevel();
	}

	@Override
	public boolean isReference() {
		return el.isReference();
	}

	@Override
	public boolean isComplexArray() {
		return el.isComplexArray();
	}

	@Override
	public VirtualTreeElement getReference() {
		return resourceDB.getElement(el.getReference());
	}

	@Override
	public VirtualTreeElement addChild(String name, Class<? extends Resource> type, boolean isDecorating)
			throws ResourceAlreadyExistsException, ResourceNotFoundException, InvalidResourceTypeException {
        Objects.requireNonNull(type);
		VirtualTreeElement existingChild = getChild(name);
		if (existingChild != null && !existingChild.isVirtual()) {
			if (type != null && !type.isAssignableFrom(existingChild.getType())) {
				throw new ResourceAlreadyExistsException("resource exists with incompatible type");
			}
			return existingChild;
		}
		Object existingResRef = (existingChild != null) ? existingChild.getResRef() : null;
		TreeElement newElement = el.addChild(name, type, isDecorating);
		virtualSubresources.remove(name);
		DefaultVirtualTreeElement newChild = resourceDB.getElement(newElement);
		if (existingResRef != null) {
			newChild.setResRef(existingResRef);
		}
		return newChild;
	}

	@Override
	public DefaultVirtualTreeElement addReference(TreeElement ref, String name, boolean isDecorating) {
		TreeElement direktRef = ref;
		while (direktRef instanceof DefaultVirtualTreeElement) {
			direktRef = ((DefaultVirtualTreeElement) direktRef).el;
		}
		DefaultVirtualTreeElement existingChild = getChild(name);

		DefaultVirtualTreeElement reference;
		if (existingChild != null) {
			reference = existingChild;
            reference.setEl(el.addReference(direktRef, name, isDecorating));
		}
		else {
			reference = resourceDB.getElement(el.addReference(direktRef, name, isDecorating));
		}

		if (virtualSubresources.containsKey(name)) {
			virtualSubresources.get(name).realizedBy(el.getChild(name));
			virtualSubresources.remove(name);
		}

		return reference;
	}

	/* the tree element was virtual and is now replaced by a real one. */
	private void realizedBy(TreeElement e) {
		assert e.getName().equals(getName()) : "name mismatch: " + getPath() + " / " + e.getPath();
        String createdPath = getPath();
		for (TreeElement child : e.getChildren()) {
			DefaultVirtualTreeElement vChild = virtualSubresources.get(child.getName());
			if (vChild != null) {
				vChild.realizedBy(child);
				if (getResRef() != null) {
					((ElementInfo) getResRef()).fireSubResourceAdded(child);
				}
				else {
					if (vChild.getResRef() != null) {
						((ElementInfo) vChild.getResRef()).fireResourceCreated(createdPath);
					}
				}
				virtualSubresources.remove(child.getName());
			}
		}

		ElementInfo oldElementInfo = (ElementInfo) getResRef();
		setEl(getRealElement(e));
		if (oldElementInfo != null) {
			oldElementInfo.transferListeners(el);
			oldElementInfo.fireResourceCreated(createdPath);
		}
	}

	@Override
	public List<TreeElement> getChildren() {
		return el.getChildren();
	}

	private Collection<DefaultVirtualTreeElement> getVirtualChildren() {
		return virtualSubresources.values();
	}

	public List<TreeElement> getAllChildren() {
		List<TreeElement> realChildren = getChildren();
		List<TreeElement> allChildren = new ArrayList<>(realChildren.size() + virtualSubresources.size());
		allChildren.addAll(realChildren);
		allChildren.addAll(virtualSubresources.values());
		return allChildren;
	}

	public Collection<TreeElement> getSubTreeElements() {
		Deque<TreeElement> queue = new ArrayDeque<>();
		Set<TreeElement> visitedElements = new HashSet<>();
		queue.add(this);
		while (!queue.isEmpty()) {
			TreeElement element = queue.pop();
			if (element.isReference()) {
				continue;
			}
			visitedElements.add(element);
			queue.addAll(element.getChildren());
			if (element instanceof DefaultVirtualTreeElement) {
				queue.addAll(((DefaultVirtualTreeElement) element).getVirtualChildren());
			}
		}
		return visitedElements;
	}

	@Override
	public synchronized DefaultVirtualTreeElement getChild(String childName) {
		TreeElement child = el.getChild(childName);
		if (child == null) {
			return getVirtualChild(childName);
		}
		else {
			return resourceDB.getElement(child);// new DefaultVirtualTreeElement(child, resourceDB);
		}
	}

	protected synchronized DefaultVirtualTreeElement getVirtualChild(String name) {
		DefaultVirtualTreeElement child = virtualSubresources.get(name);
		if (child != null) {
			return child;
		}
		final Class<? extends Resource> resType = getType();
		if (resType == null) { // ResourceList decorator
			return null;
		}
		Class<? extends Resource> optType = findOptionalElementType(resType, name);
		if (optType == null) {
			return null; // requested non existing decorator
		}
		//child = resourceDB.getElement(new MemoryTreeElement(name, optType, this));
        child = resourceDB.getElement(name, el, optType, false);
		virtualSubresources.put(name, child);
		return child;
	}

	@Override
	public synchronized VirtualTreeElement getChild(String name, Class<? extends Resource> type) {
		DefaultVirtualTreeElement child = virtualSubresources.get(name);
		if (child != null) {
			return child;
		}
		//child = resourceDB.getElement(new MemoryTreeElement(name, type, el, true));
        child = resourceDB.getElement(name, el, type, true);
		virtualSubresources.put(name, child);
		return child;
	}

	@SuppressWarnings("unchecked")
	protected final Class<? extends Resource> findOptionalElementType(Class<? extends Resource> type,
			String optionalName) {
		for (Method m : type.getMethods()) {
			if (m.getName().equals(optionalName) && Resource.class.isAssignableFrom(m.getReturnType())) {
				return (Class<? extends Resource>) m.getReturnType();
			}
		}
		return null;
	}

	@Override
	public SimpleResourceData getData() throws ResourceNotFoundException, UnsupportedOperationException {
		return el.getData();
	}

	@Override
	public void fireChangeEvent() {
		el.fireChangeEvent();
	}

	@Override
	public boolean isVirtual() {
		// XXX
		return (el instanceof MemoryTreeElement);
	}

	@Override
	public boolean create() {
		if (isVirtual()) {
			DefaultVirtualTreeElement parent = getParent();
			assert parent != null;

			if (parent.isVirtual()) {
				parent.create();
			}
			assert !parent.isVirtual();
			Object userObject = el.getResRef();
			Class<? extends Resource> type = getType();
			setEl(getRealElement(parent.addChild(getName(), type, isDecorator())));
			// Quickfix for bug when storing schedule resources
			initEmptyArrays(type);
			
			if (userObject != null) {
				el.setResRef(userObject);
			}
			assert !isVirtual();
			return true;
		}
		else {
			return false;
		}
	}

	private void initEmptyArrays(Class<? extends Resource> type) {
		if(ArrayResource.class.isAssignableFrom(type)) {
			if (BooleanArrayResource.class.isAssignableFrom(type)) {
				el.getData().setBooleanArr(new boolean[0]);
			}
			if (ByteArrayResource.class.isAssignableFrom(type)) {
				el.getData().setByteArr(new byte[0]);
			}
			if (FloatArrayResource.class.isAssignableFrom(type)) {
				el.getData().setFloatArr(new float[0]);
			}
			if (IntegerArrayResource.class.isAssignableFrom(type)) {
				el.getData().setIntArr(new int[0]);
			}
			if (StringArrayResource.class.isAssignableFrom(type)) {
				el.getData().setStringArr(new String[0]);
			}
			if (TimeArrayResource.class.isAssignableFrom(type)) {
				el.getData().setLongArr(new long[0]);
			}
		}
	}

	@Override
	public String getPath() {
		return el.getPath();
	}

	@Override
	public void delete() {
		if (isVirtual()) {
			resourceDB.deleteResource(el);
			return;
		}
		resourceDB.deleteResource(el);
		registerAsVirtualChild();
	}

	private void registerAsVirtualChild() {
        DefaultVirtualTreeElement parent = getParent();
		if (parent != null) {
            assert parent.el.getChild(getName()) == null;
			parent.virtualSubresources.put(getName(), this);
		}
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 41 * hash + Objects.hashCode(this.el.getPath());
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
		final DefaultVirtualTreeElement other = (DefaultVirtualTreeElement) obj;
		if (!Objects.equals(this.el.getPath(), other.el.getPath())) {
			return false;
		}
		return true;
	}

    protected void setEl(TreeElement el) {
        this.el = el;
    }
    
    protected TreeElement getEl(){
        return el;
    }

	@Override
	public Class<? extends Resource> getResourceListType() {
		return el.getResourceListType();
	}

	@Override
	public void setResourceListType(Class<? extends Resource> cls) {
		el.setResourceListType(cls);
	}

	@Override
	public void setLastModified(long time) {
		el.setLastModified(time);
	}

	@Override
	public long getLastModified() {
		return el.getLastModified();
	}

	@Override
	public String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

}
