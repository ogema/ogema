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
package org.ogema.resourcemanager.impl;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.ogema.accesscontrol.ResourceAccessRights;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.AccessMode;
import static org.ogema.core.resourcemanager.AccessMode.READ_ONLY;
import org.ogema.core.resourcemanager.AccessModeListener;
import org.ogema.core.resourcemanager.NoSuchResourceException;
import org.ogema.core.resourcemanager.ResourceAccessException;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.core.resourcemanager.ResourceGraphException;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.resourcemanager.VirtualResourceException;
import org.ogema.resourcemanager.impl.model.DefaultResourceList;
import org.ogema.resourcemanager.virtual.DefaultVirtualTreeElement;
import org.ogema.resourcetree.TreeElement;
import org.ogema.resourcemanager.virtual.VirtualTreeElement;

/**
 * Base class of all Resource implementations, used as base class of concrete
 * implementations and for the dynamic proxy dispatcher (
 * {@link DynamicProxyResource}). Resource objects are created separately for
 * every application and contain application specific information like access
 * rights.
 *
 * @author jlapp
 */
public abstract class ResourceBase implements ConnectedResource {

	private VirtualTreeElement el;
	private int revision;
	protected final ApplicationResourceManager resMan;
	protected final String path;
	protected ResourceAccessRights accessRights;

	public ResourceBase(VirtualTreeElement el, String path, ApplicationResourceManager resMan) {
		Objects.requireNonNull(el);
		Objects.requireNonNull(resMan);
		this.el = el;
		this.resMan = resMan;
		this.path = path;
		revision = resMan.getDatabaseManager().getRevision();

		assert el.getType() != null;
	}

	// called when resource has been updated
	protected void handleResourceUpdate(boolean valueChanged) {
		setLastUpdateTime();
		if (!el.isActive()) {
			return;
		}
		resMan.getDatabaseManager().getElementInfo(getEl()).fireResourceChanged(this,
				resMan.getApplicationManager().getFrameworkTime(), valueChanged);
	}

	@Override
	@JsonIgnore
	//FIXME! annotation does not belong here
	public TreeElement getTreeElement() {
		return getEl();
	}

	/**
	 * Two resources are defined "equal" when their path (not only their
	 * location) is equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Resource)) {
			return false;
		}

		Resource other = (Resource) obj;
		return getPath().equals(other.getPath());
	}

	@Override
	public boolean equalsPath(Resource resource) {
		return this.equals(resource);
	}

	@Override
	public boolean equalsLocation(Resource other) {
		return getLocation().equals(other.getLocation());
	}

	@Override
	public String toString() {
		return getPath() + ": " + getResourceType();
	}

	public static String valueString(Resource res) {
		String val;
		switch (res.getResourceType().getSimpleName()) {
		case "BooleanResource":
			val = Boolean.toString(((BooleanResource) res).getValue());
			break;
		case "FloatResource":
			val = Float.toString(((FloatResource) res).getValue());
			break;
		case "IntegerResource":
			val = Integer.toString(((IntegerResource) res).getValue());
			break;
		case "StringResource":
			val = ((StringResource) res).getValue();
			break;
		case "TimeResource":
			val = Long.toString(((TimeResource) res).getValue());
			break;
		default:
			val = "...";
		}
		return val;
	}

	@Override
	public String getName() {
		assert getEl().getName().equals(getPath("/").substring(getPath("/").lastIndexOf("/") + 1)) : "name does not match path";
		return getEl().getName();
	}

	@Override
	public Class<? extends Resource> getResourceType() {
		return getEl().getType();
	}

	/**
	 * Checks if the name of the TreeElement given is a valid name for a
	 * resource. {
	 *
	 * @see validResourceName(String)}
	 */
	static boolean validResourceName(TreeElement element) {
		Objects.requireNonNull(element);
		return validResourceName(element.getName());
	}

	/**
	 * Checks if the name is a valid name for a resource. Invalid names may be
	 * used internally to persistently store data not directly associated to a
	 * resource (e.g. for schedules).
	 *
	 * @param name name that shall be checked for validity.
	 * @return true if the name is valid for resources, false if it is not.
	 */
	static boolean validResourceName(String name) {
		Objects.requireNonNull(name);
		if (name.isEmpty()) {
			return false;
		}
		if (!Character.isJavaIdentifierStart(name.charAt(0))) {
			return false;
		}
		for (int i = 0; i < name.length(); i++) {
			if (!Character.isJavaIdentifierPart(name.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	@Deprecated
	public void addResourceListener(org.ogema.core.resourcemanager.ResourceListener listener, boolean recursive) {
		ResourceListenerRegistration reg = new ResourceListenerRegistrationImpl(this, listener, recursive);
		resMan.registeredListeners.put(reg, listener);
		reg.performRegistration();
	}

	@Override
	@SuppressWarnings("deprecation")
	@Deprecated
	public boolean removeResourceListener(org.ogema.core.resourcemanager.ResourceListener listener) {
		ResourceListenerRegistration reg = new ResourceListenerRegistrationImpl(this, listener, true);
		if (resMan.registeredListeners.remove(reg) != null) {
			reg.unregister();
			return true;
		}
		return false;
	}

	@Override
	public void addValueListener(ResourceValueListener<?> listener) {
		addValueListener(listener, false);
	}

	@Override
	public void addValueListener(ResourceValueListener<?> listener, boolean callOnEveryUpdate) {
		ValueListenerRegistration reg = new ValueListenerRegistration(this, listener, callOnEveryUpdate);
		resMan.registeredListeners.put(reg, listener);
		reg.performRegistration();
	}

	@Override
	public boolean removeValueListener(ResourceValueListener<?> listener) {
		ValueListenerRegistration reg = new ValueListenerRegistration(this, listener, false);
		if (resMan.registeredListeners.remove(reg) != null) {
			reg.unregister();
			return true;
		}
		return false;
	}

	@Override
	public boolean isActive() {
		return getEl().isActive();
	}

	@Override
	public boolean isTopLevel() {
		return getEl().isToplevel();
	}

	/*
	 * Check if we may write to this resource. Use this method in the setValue... implementations of the DynamicProxy,
	 * the concrete SimpleResource implementations (TBD) and the TransactionHandler (before enqueuing update commands).
	 * This methods first tests the applications access mode (must be != READ_ONLY)
	 * and then performs securtiy checks.
	 */
	protected void checkWriteAccess() throws SecurityException {
		if (getAccessMode() == READ_ONLY) {
			throw new ResourceAccessException(String.format(
					"Application %s does not currently have write access to resource %s", resMan
							.getApplicationManager().getAppID(), getPath()));
		}

	}

	/**
	 * Weenie-version of the checkWriteAccess: checks if write access to the
	 * resource exists and returns a boolean instead of throwing around
	 * exceptions.
	 *
	 * @return
	 */
	protected boolean hasWriteAccess() {
		return (getAccessMode() != READ_ONLY);
	}

	@Override
	public boolean requestAccessMode(AccessMode accessMode, AccessPriority priority) throws SecurityException {
		Objects.requireNonNull(priority);
		Objects.requireNonNull(accessMode);
		AccessModeRequest req = resMan.getDatabaseManager().getElementInfo(getEl()).addAccessModeRequest(this,
				resMan.getApplicationManager(), accessMode, priority);
		synchronized (resMan.accessedResources) {
			if (accessMode == READ_ONLY) {
				resMan.accessedResources.remove(req);
			}
			else {
				resMan.accessedResources.add(req);
			}
		}
		return req.isFulfilled();
	}

	@Override
	public AccessMode getAccessMode() {
		return resMan.getDatabaseManager().getElementInfo(getEl()).getAccessMode(resMan.getApplicationManager());
	}

	@Override
	public AccessPriority getAccessPriority() {
		return resMan.getDatabaseManager().getElementInfo(getEl()).getAccessPriority(resMan.getApplicationManager());
	}

	@Override
	public <T extends Resource> T getParent() {
		if (getEl().isToplevel()) {
			return null;
		}
		String parentPath = path.substring(0, path.lastIndexOf("/"));
		return resMan.findResource(parentPath);
	}

	@Override
    public <T extends Resource> List<T> getReferencingResources(Class<T> parentType) {
        ElementInfo info = resMan.getDatabaseManager().getElementInfo(getEl());
        Collection<TreeElement> referencingElements = info.getReferences();
        Collection<TreeElement> filteredElements = referencingElements;
        if (parentType != null) {
            filteredElements = new ArrayList<>(referencingElements.size());
            for (TreeElement refEl : referencingElements) {
                if (refEl.getType() != null && parentType.isAssignableFrom(refEl.getType())) {
                    filteredElements.add(refEl);
                }
            }
        }
        List<T> refResources = new ArrayList<>(filteredElements.size());
        for (TreeElement refEl : filteredElements) {
            refResources.add(resMan.<T>findResource(refEl));
        }
        return refResources;
    }

	/**
	 * Recursively scans all sub-elements of an element that represent a
	 * resource and collects all elements visited during the recursion. The
	 * corresponding resources are also collected, provided they extend the
	 * required resource type targetType. This is called from getSubResources in
	 * the recursive case and is resistant to loops in the resource graph.
	 */
	private <T extends Resource> void getSubTreeElementsRecursively(Class<T> targetType, TreeElement element,
			String elementPath, Set<TreeElement> visitedElements, List<T> resources) {

		final List<TreeElement> children = element.getChildren();
		for (TreeElement child : children) {
			if (!validResourceName(child)) {
				continue;
			}
			if (visitedElements.contains(child)) {
				continue;
			}
			final String childPath = elementPath + "/" + child.getName();
			final T resource = resMan.getResource(childPath);
			if (targetType.isAssignableFrom(resource.getResourceType())) {
				resources.add(resource);
			}
			visitedElements.add(child);
			getSubTreeElementsRecursively(targetType, child, childPath, visitedElements, resources);
		}
	}

	@Override
    public List<Resource> getSubResources(boolean recursive) {
        try {
            resMan.getDatabaseManager().getStructureLock().readLock().lock();
            if (recursive) {
                return getSubResources(Resource.class, true);
            }

            List<TreeElement> children = getEl().getChildren();
            List<Resource> result = new ArrayList<>(children.size());
            for (TreeElement child : children) {
                if (!validResourceName(child)) {
                    continue;
                }
                try {
                    Resource resource = resMan.getResource(path + "/" + child.getName());
                    result.add(resource);
                } catch (SecurityException se) {
                    resMan.logger.trace("missing permission for sub resource", se);
                }
            }
            return result;
        } finally {
            resMan.getDatabaseManager().getStructureLock().readLock().unlock();
        }
    }

	@Override
    public List<Resource> getDirectSubResources(boolean recursive) {
        /*
         * note: in case of only direct subresources, no loops can occur in the resource graph. Hence, they do not have
         * to be taken care of.
         */
        List<TreeElement> children = getEl().getChildren();
        List<Resource> result = new ArrayList<>(children.size());
        for (TreeElement child : getEl().getChildren()) {
            if (!validResourceName(child)) {
                continue;
            }
            if (child.isReference()) {
                continue;
            }
            Resource resource = resMan.getResource(path + "/" + child.getName());
            result.add(resource);
            if (recursive) {
                result.addAll(resource.getDirectSubResources(true));
            }
        }
        return result;
    }

	@Override
	public <T extends Resource> T getSubResource(String name) {
		Objects.requireNonNull(name, "name must not be null");
		TreeElement childElement = getEl().getChild(name);
		if (childElement == null) {
			return null;
		}
		assert childElement.getName().equals(name) : "name mismatch";
		return resMan.getResource(path + "/" + name);
	}

	@Override
    public <T extends Resource> List<T> getSubResources(Class<T> resourceType, boolean recursive) {
        try {
            resMan.getDatabaseManager().getStructureLock().readLock().lock();
            if (recursive) {
                // note: getting subresources has to take care of loops in the resource graph. This is taken care of by a
                // specialized method.
                final List<T> result = new ArrayList<>(10);
                getSubTreeElementsRecursively(resourceType, getEl(), path, new HashSet<TreeElement>(), result);
                return result;
            }

            List<TreeElement> children = getEl().getChildren();
            final List<T> result = new ArrayList<>(children.size());
            for (TreeElement child : children) {
                // do not include tree elements starting with illegal character (used in Schedules as a hack).
                if (!validResourceName(child)) {
                    continue;
                }
                final T subresource = resMan.getResource(path + "/" + child.getName());
                Class<?> childType = child.getType();
                if (childType != null && resourceType.isAssignableFrom(childType)) {
                    result.add(subresource);
                }
            }
            return result;
        } finally {
            resMan.getDatabaseManager().getStructureLock().readLock().unlock();
        }
    }

	@Override
	public void activate(boolean recursive) {
		checkActiveStatePermission();
		final TreeElement element = getEl();
		if (!element.isActive()) {
			element.setActive(true);
			getResourceDB().resourceActivated(element);
		}
		if (recursive) {
			for (Resource sub : getDirectSubResources(false)) {
				sub.activate(true);
			}
		}
	}

	@Override
	public void deactivate(boolean recursive) {
		checkActiveStatePermission();
		final TreeElement element = getEl();
		if (element.isActive()) {
			element.setActive(false);
			getResourceDB().resourceDeactivated(element);
		}
		if (recursive) {
			for (Resource sub : getDirectSubResources(false)) {
				sub.deactivate(true);
			}
		}
	}

	@Override
	public void setOptionalElement(final String name, final Resource newElement) throws NoSuchResourceException,
			ResourceException, ResourceGraphException {
		Objects.requireNonNull(newElement);
		if (!validResourceName(name)) {
			throw (new NoSuchResourceException("Name " + name
					+ " is not a valid resource name. Will not add the element."));
		}
		Class<?> optionalElementType = getOptionalElementType(name);
		if (optionalElementType == null) {
			throw (new NoSuchResourceException(String.format("type %s has no optional element named %s", getEl()
					.getType(), name)));
		}
		Class<? extends Resource> newElementType = newElement.getResourceType();
		if (!optionalElementType.isAssignableFrom(newElementType)) {
			throw (new InvalidResourceTypeException(String.format(
					"Incompatible types: type %s cannot be assigned as reference for type %s", newElementType,
					optionalElementType)));
		}
		if (ResourceList.class.isInstance(newElement)) {
			Class<?> elementType = DefaultResourceList.getOptionalElementTypeParameter(getResourceType(), name);
			assert elementType != null : "illegal type definition, missing type parameter for ResourceList";
			Class<?> newElementListType = ((ResourceList) newElement).getElementType();
			if (!elementType.equals(newElementListType)) {
				throw new InvalidResourceTypeException(String.format(
						"Incompatible resource list type '%s' for optional element %s on %s", newElementListType, name,
						this.getPath()));
			}
		}
		Resource existingOptionalElement = getSubResource(name);
		if (existingOptionalElement != null && newElement.equalsLocation(existingOptionalElement)) {
			throw new ResourceGraphException(String.format(
					"cannot replace resource %s with a reference to itself (%s)", existingOptionalElement.getPath(),
					newElement.getPath()));
		}
		checkAddPermission();
		resMan.getDatabaseManager().getStructureLock().writeLock().lock();
		try {
			TreeElement newTreeElement = ((ConnectedResource) newElement).getTreeElement();

			if (newTreeElement.isReference()) {
				newTreeElement = newTreeElement.getReference();
			}

			TreeElement existingElement = getEl().getChild(name);

			List<ResourceListenerRegistration> oldReferenceListeners = Collections.emptyList();
			Collection<StructureListenerRegistration> oldStructureListenerRegistrations = null;
			if (existingElement != null) {
				if (existingElement.isReference()) {
					oldReferenceListeners = replaceReference(existingElement, name);
				}
				oldStructureListenerRegistrations = collectStructureListeners((DefaultVirtualTreeElement) existingElement);
				//resMan.getDatabaseManager().deleteResource(existingElement);
				deleteElement(name);
			}

			getEl().addReference(newTreeElement, name, false);
			TreeElement dec = getEl().getChild(name);
			if (!oldReferenceListeners.isEmpty()) {
				ElementInfo info = resMan.getDatabaseManager().getElementInfo(dec);
				for (ResourceListenerRegistration l : oldReferenceListeners) {
					info.addResourceListener(l);
				}
				info.updateListenerRegistrations();
			}
			if (oldStructureListenerRegistrations != null && !oldStructureListenerRegistrations.isEmpty()) {
				reRegisterStructureListeners(oldStructureListenerRegistrations);
			}
			treeElementReferenceAdded(getEl(), dec);

			assert getEl().getChild(name).isReference() : "should be a reference";
			assert getEl().getChild(name).getName().equals(name) : "reference name not ok";
			assert getEl().getChild(name).getResID() != newTreeElement.getResID() : "wrong id";
			assert getSubResource(name).getName().equals(name) : "wrong name on reference";
			assert dec.getResRef() != null;
		} finally {
			resMan.getDatabaseManager().getStructureLock().writeLock().unlock();
		}
	}

	protected List<ResourceListenerRegistration> replaceReference(TreeElement existingReference, String name) {
		ElementInfo info = resMan.getDatabaseManager().getElementInfo(existingReference);
		info.removeReference(getEl());
		String referencePath = "/" + getSubResource(name).getPath("/");
		return info.invalidateListenerRegistrations(referencePath);
	}

	private Collection<StructureListenerRegistration> collectStructureListeners(DefaultVirtualTreeElement el) {
        List<StructureListenerRegistration> listeners = new ArrayList<>();
        DefaultVirtualTreeElement location = el;
        while (location.isReference()) {
            location = (DefaultVirtualTreeElement) location.getReference();
        }
        for (TreeElement element : location.getSubTreeElements()) {
            ElementInfo info = (ElementInfo) element.getResRef();
            if (info != null) {
                listeners.addAll(info.getStructureListeners());
            }
        }
        return listeners;
    }

	private void reRegisterStructureListeners(final Collection<StructureListenerRegistration> listeners) {
		PrivilegedAction<Void> registerStructureListeners = new PrivilegedAction<Void>() {

			@Override
			public Void run() {
				for (StructureListenerRegistration slr : listeners) {
					ConnectedResource r = resMan.getResource(slr.getResource().getPath());
					ElementInfo info = resMan.getDatabaseManager().getElementInfo(r.getTreeElement());
					info.addStructureListener(slr.getResource(), slr.getListener(), slr.appman);
				}
				return null;
			}

		};
		AccessController.doPrivileged(registerStructureListeners);

	}

	@Override
	public Resource addOptionalElement(String name) throws NoSuchResourceException {
		Objects.requireNonNull(name);
		if (!validResourceName(name)) {
			throw (new NoSuchResourceException("Name " + name
					+ " is not a valid resource name. Will not add the element."));
		}
		checkAddPermission();
		VirtualTreeElement existingReference = null;

		VirtualTreeElement existingChild = getEl().getChild(name);
		if (existingChild != null && !existingChild.isVirtual()) {
			if (existingChild.isReference()) {
				existingReference = existingChild;
			}
			else {
				return getSubResource(name);
			}
		}

		Class<? extends Resource> optionalElementType = getOptionalElementType(name);
		if (optionalElementType == null) {
			throw (new NoSuchResourceException(String.format("type %s has no optional element named %s", getEl()
					.getType(), name)));
		}

		List<ResourceListenerRegistration> oldReferenceListeners = Collections.emptyList();
		Collection<StructureListenerRegistration> oldStructureListenerRegistrations = null;
		if (existingReference != null) {
			oldReferenceListeners = replaceReference(existingReference, name);
			oldStructureListenerRegistrations = collectStructureListeners((DefaultVirtualTreeElement) existingReference);
			existingReference.delete();
		}

		TreeElement opt = getEl().addChild(name, optionalElementType, false);
		if (!oldReferenceListeners.isEmpty()) {
			ElementInfo info = resMan.getDatabaseManager().getElementInfo(opt);
			for (ResourceListenerRegistration l : oldReferenceListeners) {
				info.addResourceListener(l);
			}
			info.updateListenerRegistrations();
		}
		if (oldStructureListenerRegistrations != null && !oldStructureListenerRegistrations.isEmpty()) {
			reRegisterStructureListeners(oldStructureListenerRegistrations);
		}
		treeElementChildAdded(getEl(), opt);

		assert opt.isActive() == false : "newly-created tree elements must be inactive";
		return getSubResource(name);
	}

	// setup ElementInfo for a newly added TreeElement child.
	private void treeElementChildAdded(TreeElement parent, TreeElement child) {
		ElementInfo info = resMan.getDatabaseManager().getElementInfo(child);
		ElementInfo parentInfo = resMan.getDatabaseManager().getElementInfo(parent);
		child.setResRef(info);
		if (child.getType() != null && Schedule.class.isAssignableFrom(child.getType())) {
			info.setSchedule(child);
		}
		parentInfo.updateListenerRegistrations();
		resMan.getDatabaseManager().getElementInfo(child).fireResourceCreated(child.getPath());
		parentInfo.fireSubResourceAdded(child);
	}

	// modify ElementInfo for a newly created reference: add the parent element's
	// top level resources to all subresources
	private void treeElementReferenceAdded(TreeElement parent, TreeElement ref) {
		ElementInfo parentInfo = resMan.getDatabaseManager().getElementInfo(parent);
		ElementInfo refInfo = resMan.getDatabaseManager().getElementInfo(ref);
		refInfo.addReference(parent);
		parentInfo.updateListenerRegistrations();

		resMan.getDatabaseManager().getElementInfo(ref).fireResourceCreated(ref.getPath());
		parentInfo.fireSubResourceAdded(ref);
		revision = resMan.getDatabaseManager().incrementRevision();
	}

	/* return the type of the optional element of that name, or null if no such
	 optional element exists. */
	protected Class<? extends Resource> getOptionalElementType(String name) {
		if (getEl().getType() == null) { //special case: ResourceList decorators
			return null;
		}
		return getOptionalElementTypeOfType(getEl().getType(), name);
	}

	@SuppressWarnings("unchecked")
	static Class<? extends Resource> getOptionalElementTypeOfType(Class<?> type, String name) {
		try {
			Method m = type.getMethod(name);
			return (Class<? extends Resource>) m.getReturnType();
		} catch (NoSuchMethodException nsme) {
			return null;
		}
	}

	/*
	 check for ADDSUB permission and throw a SecurityException if it's not available
	 */
	protected void checkAddPermission() {
		if (!getAccessRights().isAddsubPermitted()) {
			throw new SecurityException(String.format(
					"Application '%s' does not have permission to add subresources to %s (path=%s)", resMan.getAppId(),
					getLocation(), getPath()));
		}
	}

	protected void checkWritePermission() {
		if (!getAccessRights().isWritePermitted()) {
			throw new SecurityException(String.format(
					"Application '%s' does not have permission to write to resource %s (path=%s)", resMan.getAppId(),
					getLocation(), getPath()));
		}
	}

	protected void checkActiveStatePermission() {
		if (!getAccessRights().isActivityPermitted()) {
			throw new SecurityException(String.format(
					"Application '%s' does not have permission to change the active state of resource %s (path=%s)",
					resMan.getAppId(), getLocation(), getPath()));
		}
	}

	protected void checkReadPermission() {
		if (!getAccessRights().isReadPermitted()) {
			throw new SecurityException(String.format(
					"Application '%s' does not have permission to read resource %s (path=%s)", resMan.getAppId(),
					getLocation(), getPath()));
		}
	}

	@Override
	public <T extends Resource> T addDecorator(String name, Class<T> resourceType)
			throws ResourceAlreadyExistsException, NoSuchResourceException {
		Objects.requireNonNull(resourceType, "resourceType is required");
		Objects.requireNonNull(name, "name is required");
		if (!validResourceName(name)) {
			throw (new NoSuchResourceException("Name " + name
					+ " is not a valid resource name. Will not add the element."));
		}
		checkAddPermission();
		Class<?> optionalElementType = getOptionalElementType(name);
		if (optionalElementType != null) {
			if (!optionalElementType.isAssignableFrom(resourceType)) {
				throw (new ResourceAlreadyExistsException(String.format(
						"invalid decorator type: '%s' already defined as optional element with type %s", name,
						optionalElementType)));
			}
		}
		try {
			resMan.getDatabaseManager().getStructureLock().writeLock().lock();
			TreeElement existingDecorator = getEl().getChild(name);
			if (existingDecorator != null) {
				Class<?> existingType = existingDecorator.getType();
				if (existingType.equals(resourceType)) {
					return resMan.getResource(path + "/" + existingDecorator.getName());
				}
				throw (new ResourceAlreadyExistsException("Cannot add decorator " + name + " of type "
						+ resourceType.getSimpleName() + ": Decorator with type " + existingType.getSimpleName()
						+ " already exists."));
			}

			TreeElement dec = getEl().addChild(name, resourceType, true);
			treeElementChildAdded(getEl(), dec);

			assert dec.isActive() == false : "newly-created tree elements must be inactive";
			return resMan.getResource(path + "/" + dec.getName());
		} finally {
			resMan.getDatabaseManager().getStructureLock().writeLock().unlock();
		}
	}

	@Override
	public <T extends Resource> T addDecorator(String name, T decorator) throws ResourceAlreadyExistsException,
			NoSuchResourceException, ResourceGraphException {
		if (!validResourceName(name)) {
			throw (new NoSuchResourceException("Name " + name
					+ " is not a valid resource name. Will not add the element."));
		}
		checkAddPermission();
		Class<?> optionalElementType = getOptionalElementType(name);
		if (optionalElementType != null) {
			if (!optionalElementType.isAssignableFrom(decorator.getResourceType())) {
				throw (new ResourceAlreadyExistsException(String.format(
						"invalid decorator type: '%s' already defined as optional element with type %s", name,
						optionalElementType)));
			}
		}
		try {
			resMan.getDatabaseManager().getStructureLock().writeLock().lock();
			TreeElement decoratorElement = ((ConnectedResource) decorator).getTreeElement();
			/*
			while (decoratorElement.isReference()) {
				decoratorElement = decoratorElement.getReference();
			}
			 */
			TreeElement existingDecorator = getEl().getChild(name);
			if (existingDecorator != null) {
				if (getSubResource(name).equalsLocation(decorator)) {
					throw new ResourceGraphException(String.format(
							"cannot replace decorator %s with a reference to itself (%s)", this.getPath(), decorator
									.getPath()));
				}
				Class<?> existingType = existingDecorator.getType();
				if (!decorator.getResourceType().isAssignableFrom(existingType)) {
					throw (new ResourceAlreadyExistsException("decorator with same name but incomatible type exists."));
				}
				else {
					replaceReference(existingDecorator, name);
					resMan.getDatabaseManager().deleteResource(existingDecorator);
				}
			}

			getEl().addReference(decoratorElement, name, true);
			TreeElement dec = getEl().getChild(name);
			treeElementReferenceAdded(getEl(), dec);

			assert dec.isDecorator();
			assert dec.getResRef() != null;
			return resMan.getResource(path + "/" + name);
		} finally {
			resMan.getDatabaseManager().getStructureLock().writeLock().unlock();
		}
	}

	@Override
	public void deleteElement(String name) {
		Resource r = getSubResource(name);
		if (r != null && r.exists()) {
			r.delete();
		}
	}

	@Override
	final public String getPath() {
		return getPath("/");
	}

	@Override
	public String getPath(String separator) {
		if (separator.equals("/")) {
			return path.substring(1);
		}
		StringBuilder sb = new StringBuilder();
		for (Resource r = this; r != null; r = r.getParent()) {
			sb.insert(0, r.getName());
			if (r.getParent() != null) {
				sb.insert(0, separator);
			}
		}
		return sb.toString();
	}

	@Override
	final public String getLocation() {
		return getLocation("/");
	}

	@Override
	public String getLocation(String delimiter) {
		TreeElement currentElement = this.getEl();
		StringBuilder location = new StringBuilder();
		while (true) {
			while (currentElement.isReference()) {
				currentElement = currentElement.getReference();
			}
			if (location.length() == 0) {
				location.append(currentElement.getName());
			}
			else {
				location.insert(0, currentElement.getName() + delimiter);
			}
			if (currentElement.isToplevel()) {
				break;
			}
			currentElement = currentElement.getParent();
		}
		return location.toString();
	}

	@Override
	public boolean isWriteable() {
		//TODO? security
		return getAccessMode() != READ_ONLY;
	}

	@Override
	public void addStructureListener(ResourceStructureListener listener) {
		synchronized (resMan.structureListeners) {
			resMan.structureListeners.add(resMan.getDatabaseManager().getElementInfo(getEl()).addStructureListener(
					this, listener, resMan.getApplicationManager()));
		}
	}

	@Override
	public boolean removeStructureListener(ResourceStructureListener listener) {
		synchronized (resMan.structureListeners) {
			return resMan.structureListeners.remove(resMan.getDatabaseManager().getElementInfo(getEl())
					.removeStructureListener(this, listener, resMan.getApplicationManager()));
		}
	}

	@Override
	public boolean isReference(boolean recursive) {
		if (!recursive) {
			return getEl().isReference();
		}
		return getEl().isReference() || (getParent() != null && getParent().isReference(recursive));
	}

	@Override
	public boolean isDecorator() {
		return getEl().isDecorator();
	}

	@Override
	public int hashCode() {
		return getPath("/").hashCode();
	}

	@Override
	public boolean exists() {
		return !getEl().isVirtual();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Resource> T create() {
		if (exists()) {
			return (T) this;
		}

		resMan.getDatabaseManager().getStructureLock().writeLock().lock();
		try {
			assert getParent() != null;
			if (!getParent().exists()) {
				getParent().create();
			}
			assert getParent().exists();
			((ResourceBase) getParent()).checkAddPermission();
			getEl().create();
			treeElementChildAdded(((ConnectedResource) getParent()).getTreeElement(), getEl());
			assert exists();
			return (T) this;
		} finally {
			resMan.getDatabaseManager().getStructureLock().writeLock().unlock();
		}
	}

	@Override
	public <T extends Resource> T getSubResource(String name, Class<T> type) throws NoSuchResourceException {
		Objects.requireNonNull(name, "name must not be null");
		Objects.requireNonNull(type, "type must not be null");
		TreeElement subRes = getEl().getChild(name);
		if (subRes != null) {
			if (!type.isAssignableFrom(subRes.getType())) {
				throw (new NoSuchResourceException(String.format(
						"A sub resource called '%s' already exists, but has incompatible type", name)));
			}
			else {
				return getSubResource(name);
			}
		}
		else {
			VirtualTreeElement newSubres = getEl().getChild(name, type);
			return getSubResource(name);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Resource> T setAsReference(T reference) throws NoSuchResourceException, ResourceException,
			ResourceGraphException, VirtualResourceException {
		Objects.requireNonNull(reference, "reference must not be null");
		if (reference.equalsLocation(this)) {
			if (reference.isReference(false)) {
				throw new ResourceGraphException(String.format(
						"cannot replace resource %s with a reference to itself (%s)", this.getPath(), reference
								.getPath()));
			}
			else {
				return (T) this;
			}
		}
		if (getParent() == null) {
			throw (new ResourceGraphException("cannot set a top level resource as reference"));
		}
		if (!getParent().exists()) {
			throw (new VirtualResourceException(
					"parent resource is virtual, cannot set a reference on a virtual resource"));
		}
		if (!reference.exists()) {
			throw (new VirtualResourceException("reference is virtual, cannot use a virtual resource as reference"));
		}
		Class<? extends Resource> optType = getOptionalElementTypeOfType(getParent().getResourceType(), getName());
		T rval;
		// permissions are checked by addDecorator / setOptionalElement calls
		if (optType == null) {
			rval = getParent().addDecorator(getName(), reference);
		}
		else {
			getParent().setOptionalElement(getName(), reference);
			rval = getParent().getSubResource(getName());
		}
		return rval;
	}

	@Override
	public void delete() {
		if (!getAccessRights().isDeletePermitted()) {
			throw new SecurityException(String.format(
					"Application '%s' does not have permission to delete resource %s (path=%s)", resMan.getAppId(),
					getLocation(), getPath()));
		}
		resMan.getDatabaseManager().getStructureLock().writeLock().lock();
		try {
			if (!isReference(false)) {
				//delete sub resources only if this is not a reference
				for (Resource sub : getDirectSubResources(false)) {
					sub.delete();
				}
				//delete references to this resource
				for (Resource referer : getReferencingResources(Resource.class)) {
					for (Resource sub : referer.getSubResources(getResourceType(), false)) {
						if (sub.isReference(false) && sub.equalsLocation(this)) {
							sub.delete();
						}
					}
				}
			}

			TreeElement parent = getEl().getParent();
			if (parent != null) {
				ElementInfo info = resMan.getDatabaseManager().getElementInfo(parent);
				info.fireSubResourceRemoved(getEl());
			}

			resMan.getDatabaseManager().getElementInfo(getEl()).fireResourceDeleted();
			resMan.getDatabaseManager().resourceDeleted(getEl());

			getEl().delete();
			resMan.getDatabaseManager().incrementRevision();
		} finally {
			resMan.getDatabaseManager().getStructureLock().writeLock().unlock();
		}
	}

	@Override
	public void addAccessModeListener(AccessModeListener listener) {
		resMan.getDatabaseManager().getElementInfo(getEl()).addAccessModeListener(listener, this,
				resMan.getApplicationManager());
	}

	@Override
	public boolean removeAccessModeListener(AccessModeListener listener) {
		return resMan.getDatabaseManager().getElementInfo(getEl()).removeAccessModeListener(listener, this,
				resMan.getApplicationManager());
	}

	protected ResourceAccessRights getAccessRights() {
		return accessRights;
	}

	protected VirtualTreeElement getEl() {
		if (revision != resMan.getDatabaseManager().getRevision()) {
			synchronized (this) {
				if (revision == resMan.getDatabaseManager().getRevision()) {
					return this.el;
				}
				int newRevision = resMan.getDatabaseManager().getRevision();
				VirtualTreeElement newEl = resMan.findTreeElement(path);
				if (newEl != null && newEl != el) {
					accessRights = resMan.getAccessRights(newEl);
					this.el = newEl;
				}
				revision = newRevision;
				reload();
			}
		}
		return this.el;
	}

	protected void reload() {
		//for use in subclasses
	}

	protected void setEl(VirtualTreeElement el) {
		this.el = el;
	}

	protected final ResourceDBManager getResourceDB() {
		return resMan.getDatabaseManager();
	}

	protected void setLastUpdateTime() {
		getTreeElement().setLastModified(resMan.getApplicationManager().getFrameworkTime());
	}

	protected long getLastUpdateTime() {
		return getTreeElement().getLastModified();
	}

}
