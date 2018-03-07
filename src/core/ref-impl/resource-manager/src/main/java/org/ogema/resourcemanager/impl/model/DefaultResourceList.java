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
package org.ogema.resourcemanager.impl.model;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.resourcemanager.NoSuchResourceException;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceGraphException;
import org.ogema.resourcemanager.impl.ApplicationResourceManager;
import org.ogema.resourcemanager.impl.ResourceBase;
import org.ogema.resourcemanager.virtual.VirtualTreeElement;
import org.ogema.resourcetree.TreeElement;
import org.slf4j.LoggerFactory;

/**
 *
 * @param <T> type of array elements.
 * @author jlapp
 */
public class DefaultResourceList<T extends Resource> extends ResourceBase implements ResourceList<T> {

	/**
	 * store array element names in an additional String array resource.
	 */
	static final String ELEMENTS = "@elements";

	public DefaultResourceList(VirtualTreeElement el, String path, ApplicationResourceManager appman) {
		super(el, path, appman);
		while (el.isReference()) {
			el = (VirtualTreeElement) el.getReference();
		}
        
        if (el.getResourceListType() == null && !el.isDecorator()) {
            Class<? extends Resource> elementType = findElementTypeOnParent();
            if (elementType != null) {
                el.setResourceListType(elementType);
            }
        }
	}

	protected final Class<? extends Resource> findElementTypeOnParent() {
		Resource p = getParent();
		if (p == null) {
			return null;
		}
		return getOptionalElementTypeParameter(p.getResourceType(), getName());
	}

	@SuppressWarnings("unchecked")
	public static Class<? extends Resource> getOptionalElementTypeParameter(Class<? extends Resource> resType,
			String elementName) {
		for (Method m : resType.getMethods()) {
			if (m.getName().equals(elementName) && Resource.class.isAssignableFrom(m.getReturnType())) {
				Type type = m.getGenericReturnType();
				if (type instanceof ParameterizedType) {
					Type[] actualTypes = ((ParameterizedType) type).getActualTypeArguments();
					if (actualTypes.length > 0) {
						return (Class<? extends Resource>) actualTypes[0];
					}
				}
			}
		}
		return null;
	}

	// write lock must be held
	@SuppressWarnings("unchecked")
	protected void checkType(Resource r) {
		Class<T> elementType = getElementType();
		if (elementType == null) {
			elementType = (Class<T>) findElementTypeOnParent();
			setElementType(elementType);
		}
		if (elementType == null) {
			elementType = (Class<T>) getEl().getResourceListType();
			setElementType(elementType);
		}
		else {
			if (!elementType.isAssignableFrom(r.getResourceType())) {
				throw new IllegalArgumentException(String.format("Illegal element type '%s', require '%s'", r
						.getResourceType(), elementType));
			}
		}
	}

	protected TreeElement getListEl() {
		TreeElement el = getEl();
		while (el.isReference()) {
			el = el.getReference();
		}
		return el;
	}

	protected TreeElement getElementsNode(boolean create) {
		TreeElement el = getListEl().getChild(ELEMENTS);
		if (el == null && create) {
			return getListEl().addChild(ELEMENTS, StringArrayResource.class, true);
		}
		else {
			return el;
		}
	}

	protected void updateElementsNode(List<String> elementNames) {
		getElementsNode(true).getData().setStringArr(elementNames.toArray(new String[elementNames.size()]));
	}

	protected List<String> getElementNames() {
        TreeElement el = getElementsNode(false);
        if (el == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(Arrays.asList(el.getData().getStringArr()));
        }
    }

	protected String findNewName() {
		int number = getElementNames().size();
		String name = getName() + "_" + number;
		while (getSubResource(name) != null) {
			name = getName() + "_" + ++number;
		}
		return name;
	}

	@Override
    public List<T> getAllElements() {
        getResourceDB().lockRead();
        try {
            List<String> elementNames = getElementNames();
            List<T> rval = new ArrayList<>(elementNames.size());
            boolean update = false;
            for (Iterator<String> it = elementNames.iterator(); it.hasNext();) {
                String name = it.next();
                if (!isSubResourceReadable(name)) {
                    continue;
                }
                T sub = getSubResource(name);
                if (sub != null && sub.exists()) {
                    rval.add(sub);
                } else {
                    it.remove();
                    update = true;
                }
            }
            if (update) {
                updateElementsNode(elementNames);
            }
            return rval;
        } finally {
            getResourceDB().unlockRead();
        }
    }

	@Override
	public int size() {
		getEl();
		final TreeElement el = getElementsNode(false);
		return el == null ? 0 : el.getData().getArrayLength();
	}

	@Override
	public <S extends Resource> S addDecorator(String name, Class<S> resourceType)
			throws ResourceAlreadyExistsException, NoSuchResourceException {
		final Class<T> elementType = getElementType();
		if (elementType != null && elementType.isAssignableFrom(resourceType)) {
			getResourceDB().lockStructureWrite();
			try {
				S dec = super.addDecorator(name, resourceType);
				List<String> elementNames = getElementNames();
				if (!elementNames.contains(name)) {
					elementNames.add(name);
					updateElementsNode(elementNames);
				}
				return dec;
			} finally {
				getResourceDB().unlockStructureWrite();
			}
		}
		else {
			return super.addDecorator(name, resourceType);
		}
	}

    @Override
    protected void checkDecoratorCompatibility(Resource newDecorator, TreeElement existingDecorator) {
    	final Class<T> elementType = getElementType();
        if (elementType != null && elementType.isAssignableFrom(existingDecorator.getType())) {
            //when replacing a list element, the replacement must also be compatible with the list element type
            if (!elementType.isAssignableFrom(newDecorator.getResourceType())) {
                throw new ResourceAlreadyExistsException("decorator exists and is a list element, cannot be replaced by a non-list element");
            }
        }        
    }    

	@Override
	public <S extends Resource> S addDecorator(String name, S decorator) throws ResourceAlreadyExistsException,
			NoSuchResourceException, ResourceGraphException {
		final Class<T> elementType = getElementType();
		if (elementType != null && elementType.isAssignableFrom(decorator.getResourceType())) {
			getResourceDB().lockStructureWrite();
			try {
				S dec = super.addDecorator(name, decorator);
				List<String> elementNames = getElementNames();
				if (!elementNames.contains(name)) {
					elementNames.add(name);
					updateElementsNode(elementNames);
				}
				return dec;
			} finally {
				getResourceDB().unlockStructureWrite();
			}
		}
		return super.addDecorator(name, decorator);
	}

	@Override
	public T add(T arg0) {
		Objects.requireNonNull(arg0, "reference must not be null");
		getResourceDB().lockStructureWrite();
		try {
			checkType(arg0);
			String name = findNewName();
			return addDecorator(name, arg0);
		} finally {
			getResourceDB().unlockStructureWrite();
		}
	}

	@Override
	public T add() {
		final Class<T> elementType = getElementType();
		if (elementType == null) {
			throw new IllegalStateException("array element type has not been set.");
		}
		getResourceDB().lockStructureWrite();
		try {
			String name = findNewName();
			return addDecorator(name, elementType);
		} finally {
			getResourceDB().unlockStructureWrite();
		}
	}

	@Override
	public <S extends T> S add(Class<S> type) {
		Objects.requireNonNull(type, "type must not be null");
		if (getElementType() == null) {
			throw new IllegalStateException("array element type has not been set.");
		}
		getResourceDB().lockStructureWrite();
		try {
			String name = findNewName();
			return addDecorator(name, type);
		} finally {
			getResourceDB().unlockStructureWrite();
		}
	}

	@Override
	public void remove(T element) {
		getResourceDB().lockStructureWrite();
		try {
			List<String> elementNames = getElementNames();
            boolean removed = false;
			for (T e : getAllElements()) {
				if (e.equalsLocation(element)) {
					elementNames.remove(e.getName());
					e.delete();
				}
			}
            if (removed) {
                updateElementsNode(elementNames);
            }
		} finally {
			getResourceDB().unlockStructureWrite();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<T> getElementType() {
		getResourceDB().lockRead();
		try {
			return (Class<T>) getEl().getResourceListType();
		} finally {
			getResourceDB().unlockRead();
		}
	}

	@Override
	public final void setElementType(final Class<? extends Resource> resType) {
		Objects.requireNonNull(resType, "resource type must not be null");
        checkWritePermission();
		getResourceDB().lockWrite();
		try {
			final Class<? extends Resource> oldType = getElementType();
            if (oldType != null && !oldType.equals(resType)) {
                throw new IllegalStateException("resource type already set and not equal to new type");
            }
			getEl().setResourceListType(resType);
			if (oldType == null) {
                try {
                    // explicit class load to create a bundle wiring to the element type class.
                    // necessary for custom types, so a bundle update will refresh
                    // the resource manager.
                    getClass().getClassLoader().loadClass(resType.getCanonicalName());
                } catch (ClassNotFoundException ex) {
                    // might happen during unsynchronized bundle shutdown / restart
                    LoggerFactory.getLogger(ApplicationResourceManager.class).error("very unexpected exception, review code!", ex);
                }
                final List<? extends Resource> subs
                        = AccessController.doPrivileged(new PrivilegedAction<List<? extends Resource>>() {
                            @Override
                            public List<? extends Resource> run() {
                                return getSubResources(resType, false);
                            }
                        });
                if (subs.isEmpty()) {
                    return;
                }
                final List<String> names = new ArrayList<>(subs.size());
                for (Resource r : subs) {
                    names.add(r.getName());
                }
                updateElementsNode(names);
			}
		} finally {
			getResourceDB().unlockWrite();
		}
	}

	@Override
	public boolean contains(Resource resource) {
		Objects.requireNonNull(resource, "resource must not be null");
		for (Resource element : getAllElements()) {
			if (element.equalsLocation(resource)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void deleteElement(String name) {
		getResourceDB().lockStructureWrite();
		try {
			List<String> elementNames = getElementNames();
			super.deleteElement(name);
			if (elementNames.remove(name)) {
				updateElementsNode(elementNames);
			}
		} finally {
			getResourceDB().unlockStructureWrite();
		}
	}

	@Override
	protected void reload() {
		getAllElements();
	}

    @Override
    @SuppressWarnings("unchecked")
    protected Map<String, DeletedLinkInfo> deleteInternal(Map<String, DeletedLinkInfo> danglingLinks) {
        updateElementsNode(Collections.EMPTY_LIST);
        return super.deleteInternal(danglingLinks);
    }

	//TODO: get rid of parent parameter?
	@Override
	protected void treeElementChildAdded(TreeElement parent, TreeElement child) {
		super.treeElementChildAdded(parent, child);
		if (parent.equals(getEl())) {
			final Class<? extends Resource> elementType = getElementType();
			if (elementType == null || !elementType.isAssignableFrom(child.getType()))
				return;
			String name = child.getName();
			List<String> elementNames = getElementNames();
			if (!elementNames.contains(name)) {
				elementNames.add(name);
				updateElementsNode(elementNames);
			}
		}
	}

}
