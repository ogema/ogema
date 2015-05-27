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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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

	@SuppressWarnings("unchecked")
	protected Class<T> elementType;

	@SuppressWarnings("unchecked")
	public DefaultResourceList(VirtualTreeElement el, String path, ApplicationResourceManager appman) {
		super(el, path, appman);
		while (el.isReference()) {
			el = (VirtualTreeElement) el.getReference();
		}
		elementType = (Class<T>) el.getResourceListType();
		if (elementType == null && !el.isDecorator()) {
			elementType = (Class<T>) findElementTypeOnParent();
		}
	}

	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
	protected void checkType(Resource r) {
		if (elementType == null) {
			elementType = (Class<T>) findElementTypeOnParent();
		}
		if (elementType == null) {
			elementType = (Class<T>) getEl().getResourceListType();
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
        synchronized (getListEl()) {
            List<String> elementNames = getElementNames();
            List<T> rval = new ArrayList<>(elementNames.size());
            boolean update = false;
            for (Iterator<String> it = elementNames.iterator(); it.hasNext();) {
                T sub = getSubResource(it.next());
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
        }
    }

	@Override
	public int size() {
		getEl();
		return getElementNames().size();
	}

	@Override
	public <T extends Resource> T addDecorator(String name, Class<T> resourceType)
			throws ResourceAlreadyExistsException, NoSuchResourceException {
		if (getElementType() != null && getElementType().isAssignableFrom(resourceType)) {
			synchronized (getListEl()) {
				T dec = super.addDecorator(name, resourceType);
				List<String> elementNames = getElementNames();
				if (!elementNames.contains(name)) {
					elementNames.add(name);
					updateElementsNode(elementNames);
				}
				return dec;
			}
		}
		else {
			return super.addDecorator(name, resourceType);
		}
	}

	@Override
	public <T extends Resource> T addDecorator(String name, T decorator) throws ResourceAlreadyExistsException,
			NoSuchResourceException, ResourceGraphException {
		if (getElementType() != null && getElementType().isAssignableFrom(decorator.getResourceType())) {
			synchronized (getListEl()) {
				T dec = super.addDecorator(name, decorator);
				List<String> elementNames = getElementNames();
				if (!elementNames.contains(name)) {
					elementNames.add(name);
					updateElementsNode(elementNames);
				}
				return dec;
			}
		}
		return super.addDecorator(name, decorator);
	}

	@Override
	public void add(T arg0) {
		Objects.requireNonNull(arg0, "reference must not be null");
		checkType(arg0);
		synchronized (getListEl()) {
			String name = findNewName();
			addDecorator(name, arg0);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public T add() {
		if (elementType == null) {
			throw new IllegalStateException("array element type has not been set.");
		}
		synchronized (getListEl()) {
			String name = findNewName();
			return addDecorator(name, elementType);
		}
	}

	@Override
	public <S extends T> S add(Class<S> type) {
		Objects.requireNonNull(type, "type must not be null");
		if (elementType == null) {
			throw new IllegalStateException("array element type has not been set.");
		}
		synchronized (getListEl()) {
			String name = findNewName();
			return addDecorator(name, type);
		}
	}

	@Override
	public void remove(T element) {
		synchronized (getListEl()) {
			List<String> elementNames = getElementNames();
			for (T e : getAllElements()) {
				if (e.equalsLocation(element)) {
					elementNames.remove(e.getName());
					e.delete();
				}
			}
			updateElementsNode(elementNames);
		}
	}

	@Override
	public Class<T> getElementType() {
		return elementType;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setElementType(Class<? extends Resource> resType) {
		Objects.requireNonNull(resType, "resource type must not be null");
		if (elementType != null) {
			throw new IllegalStateException("resource type already set");
		}
		elementType = (Class<T>) resType;
		getEl().setResourceListType(resType);
	}

	@Override
	public boolean contains(Resource resource) {
		Objects.requireNonNull(resource, "resource must not be null");
		for (Resource element : getAllElements()) {
			if (element.equalsLocation(element)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void deleteElement(String name) {
		synchronized (getListEl()) {
			List<String> elementNames = getElementNames();
			super.deleteElement(name);
			if (elementNames.remove(name)) {
				updateElementsNode(elementNames);
			}
		}
	}

	@Override
	protected void reload() {
		getAllElements();
	}

}
