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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.persistence.ResourceDB;
import org.ogema.persistence.impl.mem.MemoryResourceDB;
import org.ogema.persistence.impl.mem.MemoryTreeElement;
import org.ogema.resourcetree.TreeElement;

/**
 *
 * @author jlapp
 */
public class DefaultVirtualResourceDB implements VirtualResourceDB {

    final ResourceDB realResources;
    final ResourceDB virtualResources;
    final Map<String, VirtualTreeElement> topLevelElementCache = new ConcurrentHashMap<>();

    final Map<String, DefaultVirtualTreeElement> elements = new ConcurrentHashMap<>();

    public DefaultVirtualResourceDB(ResourceDB realResources) {
        this.realResources = realResources;
        this.virtualResources = new MemoryResourceDB();
    }

    @Override
    public VirtualTreeElement addResource(String name, Class<? extends Resource> type, String appID)
            throws ResourceAlreadyExistsException, InvalidResourceTypeException {
        TreeElement te = realResources.addResource(name, type, appID);
        return getElement(te);
    }

    @Override
    public VirtualTreeElement getToplevelResource(String name) {
        VirtualTreeElement vte = topLevelElementCache.get(name);
        if (vte != null) {
            return vte;
        }
        TreeElement te = realResources.getToplevelResource(name);
        if (te != null) {
            vte = getElement(te);
            topLevelElementCache.put(name, vte);
        }
        return vte;
    }

    @Override
    public Class<? extends Resource> addOrUpdateResourceType(Class<? extends Resource> type)
            throws ResourceAlreadyExistsException, InvalidResourceTypeException {
        virtualResources.addOrUpdateResourceType(type);
        return realResources.addOrUpdateResourceType(type);
    }

    @Override
    public Collection<Class<?>> getTypeChildren(String name) throws InvalidResourceTypeException {
        return realResources.getTypeChildren(name);
    }

    @Override
    public boolean hasResourceType(String name) {
        return realResources.hasResourceType(name);
    }

    @Override
    public List<Class<? extends Resource>> getAllResourceTypesInstalled() {
        return realResources.getAllResourceTypesInstalled();
    }

    @Override
    public void deleteResource(TreeElement elem) {
        makeVirtual(elem);
    }

    @SuppressWarnings("deprecation")
    private void makeVirtual(TreeElement elem) {
        assert elements.containsKey(elem.getPath()) : "element not referenced as resource " + elem.getPath();

        DefaultVirtualTreeElement virtualElement = getElement(elem);
        if (!elem.isReference()) {
            for (TreeElement child : virtualElement.getChildren()) {
                makeVirtual(child);
            }
        }
        if (!virtualElement.isVirtual()) {
            TreeElement realElement = virtualElement.getEl();
            TreeElement realParent = realElement.getParent();
            Object oldResRef = realElement.getResRef();
            TreeElement newElementParent = realParent != null ? getElement(realParent) : null;
            MemoryTreeElement replacement = org.ogema.core.model.SimpleResource.class.isAssignableFrom(elem.getType())
                    ? new MemoryTreeElement(elem.getName(), elem.getType(), newElementParent, elem.isDecorator(), elem.getData())
                    : new MemoryTreeElement(elem.getName(), elem.getType(), newElementParent, elem.isDecorator());
            if (oldResRef != null) {
                replacement.setResRef(oldResRef);
            }
            virtualElement.setEl(replacement);
            if (elem.isToplevel()) {
                topLevelElementCache.remove(elem.getName());
            }
            realResources.deleteResource(realElement);
        }
    }

    @Override
    public boolean hasResource(String name) {
        return realResources.hasResource(name);
    }

    @Override
    public Collection<TreeElement> getAllToplevelResources() {
        Collection<TreeElement> real = realResources.getAllToplevelResources();
        Collection<TreeElement> rval = new ArrayList<>(real.size());
        for (TreeElement e : real) {
            rval.add(getElement(e));
        }
        return rval;
    }

    @Override
    public void startTransaction() {
        realResources.startTransaction();
    }

    @Override
    public void finishTransaction() {
        realResources.finishTransaction();
    }

    @Override
    public boolean isDBReady() {
        return realResources.isDBReady();
    }

    @Override
    public TreeElement getByID(int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected DefaultVirtualTreeElement getElement(TreeElement el) {
        String path = el.getPath();
        while (el instanceof DefaultVirtualTreeElement) {
            el = ((DefaultVirtualTreeElement) el).getEl();
        }
        DefaultVirtualTreeElement vEl = elements.get(path);
        if (vEl == null) {
            vEl = new DefaultVirtualTreeElement(el, this);
            elements.put(path, vEl);
        } else if (vEl.getEl() != el) {
            vEl.setEl(el);
        }
        return vEl;
    }
    
    protected DefaultVirtualTreeElement getElement(String name, TreeElement parent, Class<? extends Resource> type, boolean decorator) {
        String path = parent.getPath() + "/" + name;
        DefaultVirtualTreeElement vEl = elements.get(path);
        if (vEl == null){
            MemoryTreeElement el = new MemoryTreeElement(name, type, parent, decorator);
            vEl = new DefaultVirtualTreeElement(el, this);
            elements.put(path, vEl);
        }
        return vEl;
    }

	@Override
	public Collection<TreeElement> getFilteredNodes(Map<String, String> dict) {
		return null;
	}

}
