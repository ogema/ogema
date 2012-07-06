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
 * @param <T>
 *            type of array elements.
 * @author jlapp
 */
public class DefaultResourceList<T extends Resource> extends ResourceBase implements ResourceList<T> {

	/**
	 * store array element names in an additional String array resource.
	 */
	protected final List<String> elements;
	static final String ELEMENTS = "@elements";
	protected TreeElement elementsNode;

	@SuppressWarnings("unchecked")
	protected Class<T> elementType;

	@SuppressWarnings("unchecked")
	public DefaultResourceList(VirtualTreeElement el, String path, ApplicationResourceManager appman) {
		super(el, path, appman);
		elementsNode = el.getChild(ELEMENTS);
		elements = new ArrayList<>();
		elementType = (Class<T>) el.getType();
        if (elementType == null && !el.isDecorator()){
            elementType = (Class<T>) findElementTypeOnParent();
        }
		if (elementsNode != null) {
			String[] a = elementsNode.getData().getStringArr();
			if (a != null) {
				elements.addAll(Arrays.asList(a));
			}
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
			elementType = (Class<T>) r.getResourceType();
		}
		else {
			if (!elementType.isAssignableFrom(r.getResourceType())) {
				throw new IllegalArgumentException(String.format("Illegal element type '%s', require '%s'", r
						.getResourceType(), elementType));
			}
		}
	}

	protected void updateElementsNode() {
		if (elementsNode == null) {
			elementsNode = getEl().addChild(ELEMENTS, StringArrayResource.class, true);
		}
		elementsNode.getData().setStringArr(elements.toArray(new String[elements.size()]));
	}

	protected String findNewName() {
		int number = elements.size();
		String name = getName() + "_" + number;
		while (getSubResource(name) != null) {
			name = getName() + "_" + ++number;
		}
		return name;
	}

	@Override
	public List<T> getAllElements() {
		synchronized (elements) {
			List<T> rval = new ArrayList<>(elements.size());
            boolean update = false;
            for (Iterator<String> it = elements.iterator(); it.hasNext();){
                T sub = getSubResource(it.next());
                if (sub != null && sub.exists()){
                    rval.add(sub);
                } else {
                    it.remove();
                    update = true;
                }
            }
            if (update){
                updateElementsNode();
            }
			return rval;
		}
	}

	@Override
	public int size() {
		getEl();
		return elements.size();
	}

	@Override
	public <T extends Resource> T addDecorator(String name, Class<T> resourceType)
			throws ResourceAlreadyExistsException, NoSuchResourceException {
		if (getElementType() != null && getElementType().isAssignableFrom(resourceType)) {
			synchronized (elements) {
				T dec = super.addDecorator(name, resourceType);
				if (!elements.contains(name)) {
					elements.add(name);
					updateElementsNode();
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
			synchronized (elements) {
				T dec = super.addDecorator(name, decorator);
				if (!elements.contains(name)) {
					elements.add(name);
					updateElementsNode();
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
		synchronized (elements) {
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
		synchronized (elements) {
			String name = findNewName();
			return addDecorator(name, elementType);
		}
	}

	@Override
	public void remove(T element) {
		for (T e : getAllElements()) {
			if (e.equalsLocation(element)) {
				elements.remove(e.getName());
				e.delete();
			}
		}
		updateElementsNode();
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
		super.deleteElement(name);
		if (elements.remove(name)) {
			updateElementsNode();
		}
	}

	@Override
	protected void reload() {
		getAllElements();
	}

}
