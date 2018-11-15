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
package org.ogema.resourcemanager.virtual;

import com.google.common.collect.MapMaker;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

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
import org.ogema.persistence.impl.mem.DefaultSimpleResourceData;
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
    // parent field exists to keep a strong reference on intermediary virtual resources
    volatile DefaultVirtualTreeElement parent;

    final DefaultVirtualResourceDB resourceDB;
    //final Map<String, DefaultVirtualTreeElement> virtualSubresources = new HashMap<>();
    final Map<String, DefaultVirtualTreeElement> virtualSubresources;// = new HashMap<>();

    private static final AtomicLong IDCOUNTER = new AtomicLong(0);
    final long id = IDCOUNTER.getAndIncrement();

    public DefaultVirtualTreeElement(TreeElement el, DefaultVirtualResourceDB resourceDB) {
        Objects.requireNonNull(el, "TreeElement must not be null");
        this.el = getRealElement(el);
        resref = this.el.getResRef();
        this.resourceDB = resourceDB;
        virtualSubresources = new MapMaker().weakValues().concurrencyLevel(2).initialCapacity(4).makeMap();
        parent = getParent();
    }

    private TreeElement getRealElement(TreeElement el) {
        while (el instanceof DefaultVirtualTreeElement) {
            el = ((DefaultVirtualTreeElement) el).el;
        }
        return el;
    }

    @Override
    public String toString() {
        //return "DefaultVirtualTreeElement " + getName();
        return String.format("DefaultVirtualTreeElement (%d) %s: %s", id, getPath(), getType().getSimpleName());
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
    public final DefaultVirtualTreeElement getParent() {
        TreeElement parent = el.getParent(); // FIXME need to ensure that the parent element is up to date, otherwise getElement overwrites the updated version
        return parent != null ? this.parent = resourceDB.getElement(parent) : null;
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
    public VirtualTreeElement addChild(final String name, final Class<? extends Resource> type, final boolean isDecorating)
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
        TreeElement newElement;
        try {
            newElement = el.addChild(name, type, isDecorating);
        } catch (ResourceAlreadyExistsException e) {
            newElement = el.getChild(name);
            if (!type.isAssignableFrom(newElement.getType())) {
                throw new ResourceAlreadyExistsException(
                        String.format("cannot create subresource %s:%s on resource %s, subresource already exists and has incompatible type (%s)",
                                name, type, getPath(), newElement.getType()
                        )
                );
            }
        }
        virtualSubresources.remove(name);
        DefaultVirtualTreeElement newChild = resourceDB.getElement(newElement);
        if (existingResRef != null) {
            newChild.setResRef(existingResRef);
        }
        return newChild;
    }

    @Override
    public synchronized DefaultVirtualTreeElement addReference(TreeElement ref, String name, boolean isDecorating) {
        assert !isVirtual() : "addReference called on virtual element " + getPath();
        TreeElement direktRef = ref;
        while (direktRef instanceof DefaultVirtualTreeElement) {
            direktRef = ((DefaultVirtualTreeElement) direktRef).el;
        }
        DefaultVirtualTreeElement existingChild = getChild(name);

        DefaultVirtualTreeElement reference;
        if (existingChild != null) {
            reference = existingChild;
            TreeElement refEl = getEl().addReference(direktRef, name, isDecorating);
            reference.setEl(refEl);
        } else {
            reference = resourceDB.getElement(getEl().addReference(direktRef, name, isDecorating));
        }

        DefaultVirtualTreeElement existingVirtualChild = virtualSubresources.get(name);
        if (existingVirtualChild != null) {
            TreeElement newRealElement = getEl().getChild(name);
            existingVirtualChild.realizedBy(newRealElement);
            virtualSubresources.remove(name);
        }

        return reference;
    }

    /* the tree element was virtual and is now replaced by a real one. */
    private void realizedBy(TreeElement e) {
        assert e.getName().equals(getName()) : "name mismatch: " + getPath() + " / " + e.getPath();
        for (TreeElement child : e.getChildren()) {
            DefaultVirtualTreeElement vChild = virtualSubresources.get(child.getName());
            if (vChild != null) {
                vChild.realizedBy(child);
                virtualSubresources.remove(child.getName());
            }
        }
        for (DefaultVirtualTreeElement vChild : virtualSubresources.values()) {
        	MemoryTreeElement mtw = (MemoryTreeElement) vChild.getEl();
        	mtw.setParent(e);
        }

        ElementInfo oldElementInfo = (ElementInfo) getResRef();
        setEl(getRealElement(e));
        if (oldElementInfo != null) {
            oldElementInfo.transferListeners(el);
        }
    }
    
    @Override
    public synchronized List<TreeElement> getChildren() {
        return el.getChildren();
    }

    Collection<DefaultVirtualTreeElement> getVirtualChildren() {
        return virtualSubresources.values();
    }

    /** @return all real and virtual children. */
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
        //System.out.printf("getChild %s on %s%n", childName, this);
        TreeElement child = el.getChild(childName);
        if (child == null) {
            return getVirtualChild(childName);
        } else {
            return resourceDB.getElement(child);
        }
    }

    @Override
    public VirtualTreeElement getExistingChild(String name) {
        TreeElement child = el.getChild(name);
        if (child == null) {
            return null;
        } else {
            return resourceDB.getElement(child);
        }
    }

    protected synchronized DefaultVirtualTreeElement getVirtualChild(String name) {
        DefaultVirtualTreeElement child = virtualSubresources.get(name);
        if (child != null) {
        	assert child.getParent().equals(this) : "Child resource has multiple parents";
        	assert child.el instanceof MemoryTreeElement : "Virtual resource has invalid tree element; this: " + getPath() 
        		+ ", child: " + child.el.getPath() + "; type: " + child.el.getClass().getSimpleName();
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
        child = resourceDB.getElement(name, el, optType, false);
        virtualSubresources.put(name, child);
        return child;
    }

    @Override
    public synchronized VirtualTreeElement getChild(String name, Class<? extends Resource> type) {
        DefaultVirtualTreeElement child = virtualSubresources.get(name);
        if (child != null) {
        	assert child.getParent().equals(this) : "Child resource has multiple parents";
        	assert child.el instanceof MemoryTreeElement : "Virtual resource has invalid tree element: " + child.el.getPath(); 
            return child;
        }
        //child = resourceDB.getElement(new MemoryTreeElement(name, type, el, true));
        child = resourceDB.getElement(name, el, type, true);
        virtualSubresources.put(name, child);
        return child;
    }

    //XXX there are several methods of this kind, all slightly different?
    @SuppressWarnings("unchecked")
    protected final Class<? extends Resource> findOptionalElementType(Class<? extends Resource> type,
            String optionalName) {
        for (Method m : type.getMethods()) {
            if (m.getName().equals(optionalName) && !m.isBridge() && !m.isSynthetic() && Resource.class.isAssignableFrom(m.getReturnType())) {
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
    public synchronized boolean create() {
        if (isVirtual()) {
            DefaultVirtualTreeElement parent = getParent();
            assert parent != null : "Virtual resource parent is null";

            if (parent.isVirtual()) {
                parent.create();
            }
            assert !parent.isVirtual();
            Object userObject = el.getResRef();
            Class<? extends Resource> type = getType();
            final boolean optionalElement = !isDecorator();
            TreeElement realElement = getRealElement(parent.addChild(getName(), type, !optionalElement));
            setEl(realElement);
            // Quickfix for bug when storing schedule resources
            initEmptyArrays(type);
            
            rebuildVirtualSubtree(this);

            if (userObject != null) {
                el.setResRef(userObject);
            }
            assert !isVirtual();
            return true;
        } else {
            return false;
        }
    }
    
    /* after a create, the MemoryTreeElements in virtual subresources need to
    be replaced with elements containing the new parent resource */
    private static void rebuildVirtualSubtree(DefaultVirtualTreeElement e) {
        for (Map.Entry<String, DefaultVirtualTreeElement> entrySet : e.virtualSubresources.entrySet()) {
            DefaultVirtualTreeElement value = entrySet.getValue();
            if (value.el instanceof MemoryTreeElement) {
            	value.el = new MemoryTreeElement((MemoryTreeElement) value.el, e);
            }
            rebuildVirtualSubtree(value);
        }
    }

    private void initEmptyArrays(Class<? extends Resource> type) {
        if (ArrayResource.class.isAssignableFrom(type)) {
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
    
    // try to determine this elements type according to the type of its parent
    protected final Class<? extends Resource> findDefinedType() {
        Class<? extends Resource> definedType = null;
        if (getParent() != null) {
            Class<? extends Resource> parentType = getParent().getType();
            definedType = findOptionalElementType(parentType, getName());
            if (definedType == null) {
                if (ResourceList.class.isAssignableFrom(parentType)) {
                    if (getParent().getResourceListType() != null && getParent().getResourceListType().isAssignableFrom(getType())){
                        definedType = getParent().getResourceListType();
                    }
                }
            }
        }
        return definedType;
    }

    @Override
    @SuppressWarnings("deprecation")
    public synchronized void delete() {
        //System.out.println("deleting " + this);
        if (isVirtual()) {
            return;
        }
        
        Class<? extends Resource> definedType = findDefinedType();
        
        resourceDB.deleteResource(this);
        registerAsVirtualChild();
        
        if (definedType != null) {
            ((MemoryTreeElement) getEl()).setType(definedType);
        }

        final String thisPath = getPath();
        for (Map.Entry<String, DefaultVirtualTreeElement> e : resourceDB.elements.asMap().entrySet()) {
            /* FIXME this need to process ALL affected reference paths */
            String path = e.getKey();
            DefaultVirtualTreeElement childEl = e.getValue();

            if (path.startsWith(thisPath)) {// && !path.equals(thisPath)) {
                if (childEl.isVirtual() || childEl.isToplevel()) {
                    continue;
                }
                String parentPath = path.substring(0, path.lastIndexOf('/'));
                VirtualTreeElement parent = resourceDB.elements.getIfPresent(parentPath);
                if (!childEl.isVirtual()) {
                   Object oldResRef = childEl.getResRef();
                   MemoryTreeElement replacement;
                    if (org.ogema.core.model.SimpleResource.class.isAssignableFrom(childEl.getType())) {
                        SimpleResourceData data;
                        data = new DefaultSimpleResourceData();
                        replacement = new MemoryTreeElement(childEl.getName(), childEl.getType(), parent, childEl.isDecorator(), data);
                    } else {
                        replacement = new MemoryTreeElement(childEl.getName(), childEl.getType(), parent, childEl.isDecorator());
                    }
                    if (oldResRef != null) {
                        replacement.setResRef(oldResRef);
                    }
                    childEl.setEl(replacement);
                } else {
                    childEl.setEl(childEl.el); //xxx why?
                }
            }
        }
        assert isVirtual() : "not virtual after delete: " + el;
        assert getParent() == null || getParent().getChild(getName()).isVirtual() : "after delete: parent has wrong child element";
    }

    private synchronized void registerAsVirtualChild() {
        DefaultVirtualTreeElement parent = getParent();
        if (parent != null) {
            assert parent.el.getChild(getName()) == null;
            assert el instanceof MemoryTreeElement : "existing resource registered as virtual child";
            parent.virtualSubresources.put(getName(), this);
        }
    }

    @Override
    public int hashCode() {
        return el.getPath().hashCode();
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
        return this.el.getPath().equals(other.el.getPath());
    }

    protected void setEl(TreeElement el) {
        this.el = el;
    }

    protected TreeElement getEl() {
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
        return el.getLocation();
    }

    @Override
    public synchronized void constrainType(Class<? extends Resource> type) {
        Objects.requireNonNull(type, "type must not be null");
        if (!type.equals(getType())) {
            if (!isVirtual()) {
                throw new IllegalStateException("cannot constrain type: resource exists");
            }
            if (!getType().isAssignableFrom(type)) {
                throw new IllegalStateException(String.format("cannot constrain type: %s is not a sub type of %s", type, getType()));
            }
            ((MemoryTreeElement)getEl()).setType(type);
        }
    }
    
}
