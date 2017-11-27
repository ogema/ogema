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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.ogema.accesscontrol.ResourceAccessRights;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
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
import org.ogema.resourcetree.listeners.InternalStructureListenerRegistration;
import org.ogema.resourcetree.listeners.InternalValueChangedListenerRegistration;
import org.ogema.resourcemanager.virtual.VirtualTreeElement;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Base class of all Resource implementations, used as base class of concrete
 * implementations and for the dynamic proxy dispatcher (
 * {@link DynamicProxyResource}). Resource objects are created separately for
 * every application and contain application specific information like access
 * rights.
 *
 * @author jlapp
 */

@SuppressWarnings("deprecation")
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
	
	/*
	 * Use instead of handleResourceUpdate when holding the resource lock
	 */
	protected void handleResourceUpdateInternal(final boolean valueChanged) {
		setLastUpdateTime();
		if (!el.isActive())
			return;
		resMan.getDatabaseManager().getElementInfo(el).fireResourceChanged(this,
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
		return other != null && getLocation().equals(other.getLocation());
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
		assert getEl().getName().equals(getPath("/").substring(getPath("/").lastIndexOf('/') + 1)) : "name does not match path";
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
	@Deprecated
	public void addResourceListener(org.ogema.core.resourcemanager.ResourceListener listener, boolean recursive) {
		ResourceListenerRegistration reg = new ResourceListenerRegistrationImpl(this, listener, recursive);
		resMan.registeredListeners.put(reg, listener);
		reg.performRegistration();
	}

	@Override
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
		InternalValueChangedListenerRegistration reg;
		if (listener instanceof InternalValueChangedListenerRegistration)
			reg = (InternalValueChangedListenerRegistration) listener;
		else
			reg = new ValueListenerRegistration(this, listener, callOnEveryUpdate);
		resMan.registeredValueListeners.put(reg, listener);
		final ResourceDBManager manager = resMan.getDatabaseManager();
		ElementInfo info = manager.getElementInfo(getEl());
		info.addResourceListener(reg);
	}

	@Override
	public boolean removeValueListener(ResourceValueListener<?> listener) {
		ValueListenerRegistration reg = new ValueListenerRegistration(this, listener, false);
		if (resMan.registeredValueListeners.remove(reg) != null) {
			final ResourceDBManager manager = resMan.getDatabaseManager();
			ElementInfo info = manager.getElementInfo(getEl());
			info.removeResourceListener(reg);
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

	public AccessMode getAccessModeInternal() {
		return resMan.getDatabaseManager().getElementInfo(el).getAccessMode(resMan.getApplicationManager());
	}
	
	@Override
	public AccessPriority getAccessPriority() {
		return resMan.getDatabaseManager().getElementInfo(getEl()).getAccessPriority(resMan.getApplicationManager());
	}
	
	private Resource getParentPrivileged() {
		resMan.getDatabaseManager().lockStructureRead();
        try {
            if (getEl().isToplevel()) {
                return null;
            }
            String parentPath = path.substring(0, path.lastIndexOf('/'));
            return resMan.findResourcePrivileged(parentPath);
        } finally {
            resMan.getDatabaseManager().unlockStructureRead();
        }
    }

	@Override
	public <T extends Resource> T getParent() {
        resMan.getDatabaseManager().lockStructureRead();
        try {
            if (getEl().isToplevel()) {
                return null;
            }
            String parentPath = path.substring(0, path.lastIndexOf('/'));
            return resMan.findResource(parentPath);
        } finally {
            resMan.getDatabaseManager().unlockStructureRead();
        }
	}

	
	
	@Override
    public <T extends Resource> List<T> getReferencingResources(Class<T> parentType) {
        resMan.getDatabaseManager().lockStructureRead();
        try {
            ElementInfo info = resMan.getDatabaseManager().getElementInfo(getEl());
            Collection<TreeElement> referencingElements = info.getReferences(getPath(),true);
            if (referencingElements.isEmpty()) {
                return Collections.emptyList();
            }
            Collection<TreeElement> filteredElements = referencingElements;
            if (parentType != null) {
                filteredElements = new ArrayList<>(referencingElements.size());
                for (TreeElement refEl : referencingElements) {
                    if (refEl.getParent().getType() != null && parentType.isAssignableFrom(refEl.getParent().getType())) {
                        filteredElements.add(refEl);
                    }
                }
            }
            List<T> refResources = new ArrayList<>(filteredElements.size());
            for (TreeElement refEl : filteredElements) {
                T ref = resMan.<T>findResource(refEl.getParent());
                if (ref != null) {
                    refResources.add(ref);
                }
            }
            return refResources;
        } finally {
            resMan.getDatabaseManager().unlockStructureRead();
        }
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
        resMan.getDatabaseManager().lockStructureRead();
        try {
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
            resMan.getDatabaseManager().unlockStructureRead();
        }
    }

	@Override
    public List<Resource> getDirectSubResources(boolean recursive) {
        /*
         * note: in case of only direct subresources, no loops can occur in the resource graph. Hence, they do not have
         * to be taken care of.
         */
        resMan.getDatabaseManager().lockStructureRead();
        try {
            List<TreeElement> children = getEl().getChildren();
            List<Resource> result = new ArrayList<>(children.size());
            for (TreeElement child : getEl().getChildren()) {
                if (!validResourceName(child)) {
                    continue;
                }
                if (child.isReference()) {
                    continue;
                }
                Resource resource ;
                try {
                	resource = resMan.getResource(path + "/" + child.getName());
                } catch (InvalidResourceTypeException e) { // happens e.g. in case of missing permission to export a resource type
                	resMan.logger.error("Subresource of {} could not be loaded",this,e);
                	continue;
                }
                result.add(resource);
                if (recursive) {
                    result.addAll(resource.getDirectSubResources(true));
                }
            }
            return result;
        } finally {
            resMan.getDatabaseManager().unlockStructureRead();
        }
    }
    
    /**
     * @param name sub resource name.
     * @return true iff app has read permission for sub resource or sub resource does not exist.
     */
    protected boolean isSubResourceReadable(String name) {
        Objects.requireNonNull(name, "name must not be null");
        resMan.getDatabaseManager().lockStructureRead();
        try {
            TreeElement childElement = getEl().getChild(name);
            if (childElement == null) {
                return true;
            }
            return resMan.getAccessRights(childElement).isReadPermitted();
        } finally {
            resMan.getDatabaseManager().unlockStructureRead();
        }
    }

	@Override
	public <T extends Resource> T getSubResource(String name) {
		Objects.requireNonNull(name, "name must not be null");
        resMan.getDatabaseManager().lockStructureRead();
        try {
            TreeElement childElement = getEl().getChild(name);
            if (childElement == null) {
                return null;
            }
            assert childElement.getName().equals(name) : "name mismatch";
            return resMan.getResource(path + "/" + name);
        } finally {
            resMan.getDatabaseManager().unlockStructureRead();
        }
	}
	
	private <T extends Resource> T getSubResourcePrivileged(String name) {
		Objects.requireNonNull(name, "name must not be null");
        resMan.getDatabaseManager().lockStructureRead();
        try {
            TreeElement childElement = getEl().getChild(name);
            if (childElement == null) {
                return null;
            }
            assert childElement.getName().equals(name) : "name mismatch";
            return resMan.getResourcePrivileged(path + "/" + name);
        } finally {
            resMan.getDatabaseManager().unlockStructureRead();
        }
	}

	@Override
    public <T extends Resource> List<T> getSubResources(Class<T> resourceType, boolean recursive) {
        resMan.getDatabaseManager().lockStructureRead();
        try {
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
            resMan.getDatabaseManager().unlockStructureRead();
        }
    }
    
    private void fireActiveStateChanged(boolean newState) {
    	List<String> aliases = isReference(true) ? ((ResourceBase)getLocationResource()).computeAliases() : computeAliases();
        for (String a: aliases) {
            //FIXME: path string mangling
            for (InternalStructureListenerRegistration l: resMan.getDatabaseManager().getStructureListeners(a.substring(1))) {
                l.queueActiveStateChangedEvent(newState);
            }
        }
    }

	@Override
	public void activate(boolean recursive) {
		checkActiveStatePermission();
		final TreeElement element = getEl();
        /* treated as structure change to allow proper synchronization with
        structure listeners, esp. during recursive (de-)activation
        */
        resMan.getDatabaseManager().lockStructureWrite();
        try {
            if (!element.isActive()) {
                element.setActive(true);
                getResourceDB().resourceActivated(element);
                fireActiveStateChanged(true);
            }
            if (recursive) {
                for (Resource sub : getDirectSubResources(false)) {
                    sub.activate(true);
                }
            }
        } finally {
            resMan.getDatabaseManager().unlockStructureWrite();
        }
	}

	@Override
	public void deactivate(boolean recursive) {
		checkActiveStatePermission();
        resMan.getDatabaseManager().lockStructureWrite();
        try {
            final TreeElement element = getEl();
            if (element.isActive()) {
                element.setActive(false);
                getResourceDB().resourceDeactivated(element);
                fireActiveStateChanged(false);
            }
            if (recursive) {
                for (Resource sub : getDirectSubResources(false)) {
                    sub.deactivate(true);
                }
            }
        } finally {
            resMan.getDatabaseManager().unlockStructureWrite();
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
			@SuppressWarnings("rawtypes")
			Class<?> newElementListType = ((ResourceList) newElement).getElementType();
			if (!elementType.equals(newElementListType)) {
				throw new InvalidResourceTypeException(String.format(
						"Incompatible resource list type '%s' for optional element %s on %s", newElementListType, name,
						this.getPath()));
			}
		}
		Resource existingOptionalElement = getSubResource(name);
		if (existingOptionalElement != null && newElement.equalsLocation(existingOptionalElement) && !existingOptionalElement.isReference(false)) {
			throw new ResourceGraphException(String.format(
					"cannot replace resource %s with a reference to itself (%s)", existingOptionalElement.getPath(),
					newElement.getPath()));
		}
		checkAddPermission();
		resMan.getDatabaseManager().lockStructureWrite();
		try {
			TreeElement newTreeElement = ((ConnectedResource) newElement).getTreeElement();

			TreeElement existingElement = getEl().getChild(name);

			Collection<InternalValueChangedListenerRegistration> oldReferenceListeners = Collections.emptyList();
			Map<String, DeletedLinkInfo> dli = null;
			if (existingElement != null) {
				if (existingElement.isReference()) {
					if (existingElement.getReference().equals(newTreeElement))
						return;
					oldReferenceListeners = replaceReference(existingElement, name);
				} else {
                    oldReferenceListeners = collectResourceListeners((DefaultVirtualTreeElement) existingElement);
                }
                ResourceBase existingResource = (ResourceBase) getSubResource(name);
                //existingResource.notifyDelete(existingResource, false, new HashSet<>(existingResource.computeAliases()));
				dli = existingResource.deleteInternal(null);
			}

			getEl().addReference(newTreeElement, name, false);

			TreeElement dec = getEl().getChild(name);

            if (!oldReferenceListeners.isEmpty()) {
				ElementInfo info = resMan.getDatabaseManager().getElementInfo(dec);
				for (InternalValueChangedListenerRegistration l : oldReferenceListeners) {
					info.addResourceListener(l); //XXX why?
					if (!(l instanceof ResourceListenerRegistration)) {
                        l.getResource().addValueListener(l.getValueListener(), l.isCallOnEveryUpdate());
                    } else {
                    	ResourceListenerRegistration rlr = (ResourceListenerRegistration) l;
                        l.getResource().addResourceListener(rlr.getListener(), rlr.isRecursive());
                    }
				}
				info.updateListenerRegistrations();
			}            
            
			treeElementReferenceAdded(getEl(), dec);

			if (dli != null) {
				postProcessLinks(dli);
			}
            
            notifyReference(this, (ResourceBase) newElement, name);

			assert getEl().getChild(name).isReference() : "should be a reference";
			assert getEl().getChild(name).getName().equals(name) : "reference name not ok";
			assert getEl().getChild(name).getResID() != newTreeElement.getResID() : "wrong id";
			assert getSubResource(name).getName().equals(name) : "wrong name on reference";
			assert dec.getResRef() != null;
		} finally {
			resMan.getDatabaseManager().unlockStructureWrite();
		}
	}

	protected List<InternalValueChangedListenerRegistration> replaceReference(TreeElement existingReference, String name) {
        TreeElement target = existingReference.getReference();
		ElementInfo info = resMan.getDatabaseManager().getElementInfo(existingReference);
		info.removeReference(existingReference, target);
		String referencePath = "/" + getSubResource(name).getPath("/");
		return info.invalidateListenerRegistrations(referencePath);
	}

    private Collection<InternalValueChangedListenerRegistration> collectResourceListeners(DefaultVirtualTreeElement el) {
        List<InternalValueChangedListenerRegistration> listeners = new ArrayList<>();
        DefaultVirtualTreeElement location = el;
        while (location.isReference()) {
            location = (DefaultVirtualTreeElement) location.getReference();
        }
        for (TreeElement element : location.getSubTreeElements()) {
            ElementInfo info = (ElementInfo) element.getResRef();
            if (info != null) {
                listeners.addAll(info.getResourceListeners());
            }
        }
        return listeners;
    }

	@Override
	public Resource addOptionalElement(String name) throws NoSuchResourceException {
		Objects.requireNonNull(name);
		//		if(getEl().isVirtual()) throw new ResourceException("Cannot add optional element to a virtual resource (" + getPath()+")" + el ); 
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

        resMan.getDatabaseManager().lockStructureWrite();
        try {
            List<InternalValueChangedListenerRegistration> oldReferenceListeners = Collections.emptyList();
            if (existingReference != null) {
                oldReferenceListeners = replaceReference(existingReference, name);
                getSubResource(name).delete();
            }
            TreeElement opt = getEl().addChild(name, optionalElementType, false);
            if (!oldReferenceListeners.isEmpty()) {
                ElementInfo info = resMan.getDatabaseManager().getElementInfo(opt);
                for (InternalValueChangedListenerRegistration l : oldReferenceListeners) {
                    info.addResourceListener(l);
                }
                info.updateListenerRegistrations();
            }
            treeElementChildAdded(getEl(), opt);

            assert opt.isActive() == false : "newly-created tree elements must be inactive";
            Resource result = getSubResource(name);
            if (!result.exists()) { // probably fixed; was caused by a call to getParent() in ResourceFactory.createResource() -> no, still a problem
                result.create();
                resMan.logger.error(
                        "Error in resource management: newly created optional element '{}' does not exist", name);
            }
            notifyCreate(this, (ResourceBase)result);
            return result;
        } finally {
            resMan.getDatabaseManager().unlockStructureWrite();
        }
	}

	// setup ElementInfo for a newly added TreeElement child.
	protected void treeElementChildAdded(TreeElement parent, TreeElement child) {
		ElementInfo info = resMan.getDatabaseManager().getElementInfo(child);
		ElementInfo parentInfo = resMan.getDatabaseManager().getElementInfo(parent);
		child.setResRef(info);
		parentInfo.updateListenerRegistrations();
	}

	// modify ElementInfo for a newly created reference: add the parent element's
	// top level resources to all subresources
	private void treeElementReferenceAdded(TreeElement parent, TreeElement ref) {
		ElementInfo parentInfo = resMan.getDatabaseManager().getElementInfo(parent);
		ElementInfo refInfo = resMan.getDatabaseManager().getElementInfo(ref);
		refInfo.addReference(ref);
		parentInfo.updateListenerRegistrations();
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
	protected static Class<? extends Resource> getOptionalElementTypeOfType(Class<?> type, String name) {
    	for (Method m : type.getMethods()) {
            if (m.getName().equals(name) && !m.isBridge() && !m.isSynthetic() && Resource.class.isAssignableFrom(m.getReturnType())) {
                return (Class<? extends Resource>) m.getReturnType();
            }
        }
		return null;
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
        resMan.getDatabaseManager().lockStructureWrite();
        try {
			VirtualTreeElement existingDecorator = getEl().getChild(name);
			if (existingDecorator != null) {
				Class<?> existingType = existingDecorator.getType();
                //TODO: try to simplify conditions
                if (optionalElementType != null) {
                    if (!resourceType.isAssignableFrom(optionalElementType)) {
                        if (existingDecorator.isVirtual()) {
                            existingDecorator.constrainType(resourceType);
                        } else {
                            if (resourceType.isAssignableFrom(existingType)) {
                                //existing resource already matches requested type
                                return getSubResource(name);
                            }
                            throw new ResourceAlreadyExistsException("Cannot add decorator " + name + " of type "
                                + resourceType.getSimpleName() + ": Decorator with type " + existingType.getSimpleName()
                                + " already exists.");
                        }
                    }
                    return resMan.getResource(path + "/" + existingDecorator.getName()).create();
                }
                if (existingType.isAssignableFrom(resourceType)) {
                    if (!resourceType.isAssignableFrom(existingType)) {
                        if (existingDecorator.isVirtual()) {
                            existingDecorator.constrainType(resourceType);
                        } else {
                            throw new ResourceAlreadyExistsException("Cannot add decorator " + name + " of type "
                                + resourceType.getSimpleName() + ": Decorator with type " + existingType.getSimpleName()
                                + " already exists.");
                        }
                    }
					return resMan.getResource(path + "/" + existingDecorator.getName()).create();
				}
				throw (new ResourceAlreadyExistsException("Cannot add decorator " + name + " of type "
						+ resourceType.getSimpleName() + ": Decorator with type " + existingType.getSimpleName()
						+ " already exists."));
			}
			TreeElement dec = getEl().addChild(name, resourceType, true);
			treeElementChildAdded(getEl(), dec);
            T result = resMan.getResource(path + "/" + dec.getName());
            notifyCreate(this, (ResourceBase) result);
            
			assert dec.isActive() == false : "newly-created tree elements must be inactive";
			return result;
		} finally {
			resMan.getDatabaseManager().unlockStructureWrite();
		}
	}
    
    /* used in addDecorator to check for incompatible element change, will be
    overridden in ResourceList implementation */
    protected void checkDecoratorCompatibility(Resource newDecorator, TreeElement existingDecorator) {
        /* anything goes...
        Class<?> existingType = existingDecorator.getType();
        if (!existingType.isAssignableFrom(newDecorator.getResourceType())) {
            throw (new ResourceAlreadyExistsException(
                    "decorator with same name but incomatible type exists. Is: " + existingType.getSimpleName()
                    + ", new: " + newDecorator.getResourceType().getSimpleName()));
        }
        */
    }

    private <T extends Resource> T addDecoratorPrivileged(String name, T decorator, boolean privileged) throws ResourceAlreadyExistsException,
			NoSuchResourceException, ResourceGraphException {
    	Objects.requireNonNull(decorator, "decorator is required");
		Objects.requireNonNull(name, "name is required");
		if (!validResourceName(name)) {
			throw (new NoSuchResourceException("Name " + name
					+ " is not a valid resource name. Will not add the element."));
		}
        if (!exists()) {
            throw new VirtualResourceException(getPath() + " is virtual.");
        }
        if (!decorator.exists()) {
            throw new VirtualResourceException(decorator.getPath() + " is virtual.");
        }
        if (!privileged)
        	checkAddPermission();
		Class<?> optionalElementType = getOptionalElementType(name);
		if (optionalElementType != null) {
			if (!optionalElementType.isAssignableFrom(decorator.getResourceType())) {
				throw (new ResourceAlreadyExistsException(String.format(
						"invalid decorator type: '%s' already defined as optional element with type %s", name,
						optionalElementType)));
			}
		}
        resMan.getDatabaseManager().lockStructureWrite();
        try {
			TreeElement decoratorElement = ((ConnectedResource) decorator).getTreeElement();
			TreeElement existingDecorator = getEl().getChild(name);
            Collection<InternalValueChangedListenerRegistration> oldReferenceListeners = Collections.emptyList();
			if (existingDecorator != null) {
				if (getSubResource(name).equalsLocation(decorator) && !existingDecorator.isReference()) {
					throw new ResourceGraphException(String.format(
							"cannot replace decorator %s with a reference to itself (%s)", this.getPath(), decorator
									.getPath()));
				}
                checkDecoratorCompatibility(decorator, existingDecorator);
                if (existingDecorator.isReference()) {
                    oldReferenceListeners = replaceReference(existingDecorator, name);
                } else {
                    oldReferenceListeners = collectResourceListeners((DefaultVirtualTreeElement) existingDecorator);
                }
                resMan.getDatabaseManager().deleteResource(existingDecorator);
			}
            
			getEl().addReference(decoratorElement, name, optionalElementType == null);
			TreeElement dec = getEl().getChild(name);
            
            if (!oldReferenceListeners.isEmpty()) {
				ElementInfo info = resMan.getDatabaseManager().getElementInfo(dec);
				for (InternalValueChangedListenerRegistration l : oldReferenceListeners) {
					info.addResourceListener(l); //XXX why?
					if (!(l instanceof ResourceListenerRegistration)) {
                        l.getResource().addValueListener(l.getValueListener(), l.isCallOnEveryUpdate());
                    } else {
                    	ResourceListenerRegistration rlr = (ResourceListenerRegistration) l;
                        l.getResource().addResourceListener(rlr.getListener(), rlr.isRecursive());
                    }
				}
				info.updateListenerRegistrations();
			}            
            
			treeElementReferenceAdded(getEl(), dec);

			assert optionalElementType != null ^ dec.isDecorator();
			assert dec.getResRef() != null;
            
            notifyReference(this, (ResourceBase) decorator, name);
            
			return resMan.getResource(path + "/" + name);
		} finally {
			resMan.getDatabaseManager().unlockStructureWrite();
		}
    }
    
	@Override
	public <T extends Resource> T addDecorator(String name, T decorator) throws ResourceAlreadyExistsException,
			NoSuchResourceException, ResourceGraphException {
        return addDecoratorPrivileged(name, decorator, false);
	}

	@Override
	public void deleteElement(String name) {
        resMan.getDatabaseManager().lockStructureWrite();
        try {
            Resource r = getSubResource(name);
            if (r != null && r.exists()) {
                r.delete();
            }
        } finally {
            resMan.getDatabaseManager().unlockStructureWrite();
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
        //XXX return path field with replace???
        resMan.getDatabaseManager().lockStructureRead();
        try {
            StringBuilder sb = new StringBuilder();
            for (Resource r = this; r != null; r = r.getParent()) {
                sb.insert(0, r.getName());
                if (r.getParent() != null) {
                    sb.insert(0, separator);
                }
            }
            return sb.toString();
        } finally {
            resMan.getDatabaseManager().unlockStructureRead();
        }
	}

	@Override
	final public String getLocation() {
		return getLocation("/");
	}

	@Override
	public String getLocation(String delimiter) {
        resMan.getDatabaseManager().lockStructureRead();
        try {
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
        } finally {
            resMan.getDatabaseManager().unlockStructureRead();
        }
	}

	@Override
	public boolean isWriteable() {
		//TODO? security
		return getAccessMode() != READ_ONLY;
	}

	@Override
	public void addStructureListener(ResourceStructureListener listener) {
        /* synchronization required: structure listener registration must not happen
        in the middle of atomic operations like transactions or recusive activate */
        resMan.getDatabaseManager().lockStructureRead();
        try {
            InternalStructureListenerRegistration slr;
            if (listener instanceof InternalStructureListenerRegistration) {
                // registration from some internal component, do not modify / wrap
                slr = (InternalStructureListenerRegistration) listener;
            } else {
                slr = new StructureListenerRegistration(this, listener, resMan.getApplicationManager());
            }
            resMan.getDatabaseManager().addStructureListener(slr);
            resMan.addStructureListenerRegistration(slr);
        } finally {
            resMan.getDatabaseManager().unlockStructureRead();
        }
	}

	@Override
	public boolean removeStructureListener(ResourceStructureListener listener) {
        resMan.getDatabaseManager().lockStructureRead();
        try {
            InternalStructureListenerRegistration slr;
            if (listener instanceof InternalStructureListenerRegistration) {
                slr = (InternalStructureListenerRegistration) listener;
            } else {
                slr = new StructureListenerRegistration(this, listener, resMan.getApplicationManager());
            }
            resMan.getDatabaseManager().removeStructureListener(slr);
            return resMan.removeStructureListenerRegistration(slr);
        } finally {
            resMan.getDatabaseManager().unlockStructureRead();
        }
	}

	@Override
	public boolean isReference(boolean recursive) {
        resMan.getDatabaseManager().lockStructureRead();
        try {
            if (!recursive) {
                return getEl().isReference();
            }
            return getEl().isReference() || (getParent() != null && getParent().isReference(recursive));
        } finally {
            resMan.getDatabaseManager().unlockStructureRead();
        }
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
		resMan.getDatabaseManager().lockStructureWrite();
		try {
            ResourceBase parent = getParent();
			assert parent != null : "create called on non-existitent resource without parent: " + getPath();
			if (!parent.exists()) {
				parent.create();
			}
			assert parent.exists();
			((ResourceBase) getParent()).checkAddPermission();
			getEl().create();
            revision = resMan.getDatabaseManager().incrementRevision(); //reference paths may need to be reloaded
            notifyCreate((ResourceBase)getParent(), this);

			parent.treeElementChildAdded(parent.getTreeElement(), getEl());
			//FIXME somethings broken (in ScheduleTreeElement?)
			assert exists() : "Newly created resource " + path + " does not exist";
			return (T) this;
		} finally {
			resMan.getDatabaseManager().unlockStructureWrite();
		}
	}
    
    //raises structure events for newly created references
    private void notifyReference(ResourceBase parent, ResourceBase reference, String refName) {
        //FIXME loops between root and the reference are not handled correctly
        List<String> parentAliases = parent.computeAliases();
        
        /*
         * referenceChanged on the reference
         * called only on the reference path itself, not its aliases!
         */
        for (InternalStructureListenerRegistration reg : resMan.getDatabaseManager().getStructureListeners(reference.getPath())) {
            reg.queueReferenceChangedEvent(parent.getTreeElement(), true);
        }

        /*
        * subresourceAdded on the parent:
        * need to check all listeners with longer paths, in case a path
        * contains a loop leading back to the parent...
        */
        for (String alias: parentAliases) {
            for (Map.Entry<String, List<InternalStructureListenerRegistration>> e:
                    resMan.getDatabaseManager().getStructureListenersTree(alias).entrySet()) {
                String listenerPath = "/" + e.getKey();
                Resource listenerResource = resMan.findExistingResource(listenerPath, true);
                if (listenerResource != null && listenerResource.equalsLocation(parent)) {
                    for (InternalStructureListenerRegistration reg: e.getValue()) {
                        reg.queueSubResourceAddedEvent(reference.getTreeElement());
                    }
                }
            }
        }
        
        //create and subresourceAdded callbacks inside the 'newly created' reference subtree
        List<String> referenceAliases = new ArrayList<>(parentAliases.size());
        for (String a: parentAliases) {
            referenceAliases.add(a + "/" + refName);
        }
        for (String alias: referenceAliases) {
            for (Map.Entry<String, List<InternalStructureListenerRegistration>> e:
                    resMan.getDatabaseManager().getStructureListenersTree(alias).entrySet()) {
                String listenerPath = "/" + e.getKey();
                Resource listenerResource = resMan.findExistingResource(listenerPath,false);
                if (listenerResource == null || !listenerResource.exists()) {
                    continue;
                }
                for (InternalStructureListenerRegistration reg : e.getValue()) {
                    //FIXME path string mangling
                    reg.queueResourceCreatedEvent(listenerPath.substring(1));
                    for (Resource sub : listenerResource.getSubResources(false)) {
                        reg.queueSubResourceAddedEvent(((ConnectedResource) sub).getTreeElement());
                    }
                }
            }
        }
        
    }
    
    //raises structure events for newly created resources
    private void notifyCreate(ResourceBase parent, ResourceBase newResource) {
        //FIXME loops between root and the created resource are not handled correctly
        if (parent == null) {
            //no listener registrations can exist for new top level resources
            return;
        }
        List<String> parentAliases = parent.computeAliases();
        for (String alias: parentAliases) {
            for (Map.Entry<String, List<InternalStructureListenerRegistration>> e:
                    resMan.getDatabaseManager().getStructureListenersTree(alias).entrySet()) {
                //FIXME path string mangling
                String listenerPath = "/" + e.getKey();
                Resource listenerResource = resMan.findExistingResource(listenerPath, true);
                if (listenerResource == null || !listenerResource.exists()) {
                    continue;
                }
                if (listenerResource.equalsLocation(parent)) {
                    for (InternalStructureListenerRegistration reg: e.getValue()) {
                        reg.queueSubResourceAddedEvent(newResource.getTreeElement());
                    }
                } else {
                    if (listenerResource.equalsLocation(newResource)) {
                        for (InternalStructureListenerRegistration reg: e.getValue()) {
                            //FIXME path string mangling
                            reg.queueResourceCreatedEvent(listenerPath.substring(1));
                        }
                    }
                }
            }
        }
        
    }
    
	@Override
	public <T extends Resource> T getSubResource(String name, Class<T> type) throws NoSuchResourceException {
		Objects.requireNonNull(name, "name must not be null");
		Objects.requireNonNull(type, "type must not be null");
        resMan.getDatabaseManager().lockStructureRead();
        try {
            VirtualTreeElement subRes = getEl().getChild(name);
            if (subRes != null) {
                if (!type.isAssignableFrom(subRes.getType())) { 
                    if (subRes.getType().isAssignableFrom(type) && subRes.isVirtual()) {
                        subRes.constrainType(type);
                        return getSubResource(name);
                    } else {                    
                        throw (new NoSuchResourceException(String.format(
                                "A sub resource called '%s' already exists, but has incompatible type", name)));
                    }
                }
                else {
                	T t = getSubResource(name);
                	// XXX required to avoid weakly referenced element being removed 
                	// 2nd condition to ensure method behaviour is not changed in case of names containing path separator
                	if (!name.equals(subRes.getName()) && !name.endsWith("/" + subRes.getName())) 
                		throw new RuntimeException("Names do not match: requested " + name + ", but got " + subRes.getName());
                    return t;
                }
            }
            else {
                VirtualTreeElement newDecorator = getEl().getChild(name, type); //will create virtual decorator
                assert newDecorator != null;
                T t = getSubResource(name);
                // required to avoid weakly referenced element being removed 
                if (newDecorator == null) 
                	throw new NullPointerException("Virtual element found null, this is a framework bug.");
                return t;
            }
        } finally {
            resMan.getDatabaseManager().unlockStructureRead();
        }
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Resource> T setAsReference(T reference) throws NoSuchResourceException, ResourceException,
			ResourceGraphException, VirtualResourceException {
		Objects.requireNonNull(reference, "reference must not be null");
		if (reference.equalsLocation(this)) {
			if (!isReference(false)) {
				throw new ResourceGraphException(String.format(
						"cannot replace resource %s with a reference to itself (%s)", this.getPath(), reference
								.getPath()));
			}
            if (reference.getPath().equals(getEl().getReference().getPath())) {
				return (T) this;
			}
		}
		if (getParent() == null) {
			throw (new ResourceGraphException("cannot set a top level resource as reference"));
		}
		resMan.getDatabaseManager().lockStructureWrite();
		try {
			getEl();
			if (!getParent().exists()) {
				getParent().create();
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
		} finally {
			resMan.getDatabaseManager().unlockStructureWrite();
		}
	}

	protected static class DeletedLinkInfo {
		final Resource parent;
		final Class<? extends Resource> type;
		final String name;
		final String path;

		DeletedLinkInfo(Resource link) {
			type = link.getResourceType();
			name = link.getName();
			parent = ((ResourceBase) link).getParentPrivileged();
			path = ((ConnectedResource) link).getTreeElement().getReference().getPath();
		}
	}

	protected Map<String, DeletedLinkInfo> deleteInternal(Map<String, DeletedLinkInfo> danglingLinks) {
        if (danglingLinks == null){
            //first call
            notifyDelete(this, false, new HashSet<>(computeAliases()), new HashSet<String>());
            danglingLinks = new HashMap<>();
        }
        if (!exists()) {
            return danglingLinks;
        }

        for (Map.Entry<Resource, String> inc: incoming()) {
            Resource sub = inc.getKey().getSubResource(inc.getValue());
            if (!sub.equals(this) && sub.isReference(false)) {
                if (danglingLinks.containsKey(sub.getPath())) {
                        continue;
                }
                
                danglingLinks.put(sub.getPath(), new DeletedLinkInfo(sub));
                assert ((ResourceBase) sub).getParentPrivileged().equals(inc.getKey());
                ((ResourceBase) sub).deleteInternal(danglingLinks);
            }
        }

        
        if (!isReference(false)) {
            //delete sub resources only if this is not a reference
            //for (Resource sub : getDirectSubResources(false)) {
            for (Resource sub : getSubResources(false)) {
                ((ResourceBase)sub).deleteInternal(danglingLinks);
            }
        }
        
        deleteTreeElement();
        return danglingLinks;
    }
    
	protected void deleteTreeElement() {
		resMan.getDatabaseManager().resourceDeleted(getEl());

		((VirtualTreeElement) getTreeElement()).delete();
		resMan.getDatabaseManager().incrementRevision();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void delete() {
		if (!getAccessRights().isDeletePermitted()) {
			throw new SecurityException(String.format(
					"Application '%s' does not have permission to delete resource %s (path=%s)", resMan.getAppId(),
					getLocation(), getPath()));
		}
		resMan.getDatabaseManager().lockStructureWrite();
		try {
            if (isTopLevel()) {
                resMan.getDatabaseManager().deleteUniqueName(el);
            }
			List<ResourceList> affectedLists = getReferencingResources(ResourceList.class);
			Resource parent = getParent();
			Map<String, DeletedLinkInfo> dl = deleteInternal(null);
			postProcessLinks(dl);
			if (parent != null && parent.exists() && (parent instanceof ResourceList)) {
				((ResourceList) parent).getAllElements();
			}
			for (ResourceList<?> l : affectedLists) {
                //rebuild the list
				if (l.exists()) {
					l.getAllElements();
				}
			}
		} finally {
			resMan.getDatabaseManager().unlockStructureWrite();
		}
	}
    
    //raises structure events for deleted resources
    private void notifyDelete(ResourceBase r, boolean reachedThroughReference, Set<String> affectedPaths, Set<String> visitedPaths) {
        if (visitedPaths.contains(r.getLocation())) {
            return;
        }
        visitedPaths.add(r.getLocation());
        affectedPaths = new HashSet<>(affectedPaths);
        for (Map.Entry<Resource, String> e: r.incoming()) {
            ResourceBase p = (ResourceBase) e.getKey();
            String name = e.getValue();
            if (reachedThroughReference) {
                if (!p.getSubResourcePrivileged(name).isReference(false)) {
                    continue;
                }
                // p/name is reference
                String refPath = p.getTreeElement().getChild(name).getReference().getPath();
                if (!affectedPaths.contains(refPath)) {
                    continue;
                }
            }
            affectedPaths.add("/" + p.getPath() + "/" + name);
        }
        
        for (Resource c : r.getSubResources(false)) {
            notifyDelete((ResourceBase) c, c.isReference(false) || reachedThroughReference, extendPaths(affectedPaths, c.getName()), visitedPaths);
        }
        
        for (String p : affectedPaths) {
            for (InternalStructureListenerRegistration l : resMan.getDatabaseManager().getStructureListeners(p.substring(1))) {
                l.queueResourceDeletedEvent();
            }
            int idx = p.lastIndexOf('/');
            if (idx < 1) {
                continue; //top level
            }
            String pParent = p.substring(0, idx);
            TreeElement e = resMan.findTreeElement(p);
            if (e != null) {
                for (InternalStructureListenerRegistration l : resMan.getDatabaseManager().getStructureListeners(pParent.substring(1))) {
                    l.queueSubResourceRemovedEvent(e);
                }
                
                if (e.isReference()) {
                    String refPath = e.getReference().getPath();
                    for (InternalStructureListenerRegistration l : resMan.getDatabaseManager().getStructureListeners(refPath)) {
                        l.queueReferenceChangedEvent(resMan.findTreeElement(pParent), false);
                    }
                }
                
            }
        }
    }
    
    private static Set<String> extendPaths(Set<String> paths, String name) {
        Set<String> rval = new HashSet<>(paths.size());
        for (String p: paths) {
            if (!p.startsWith("/")) { //FIXME path string mangling
                rval.add("/" + p + "/" + name);
            } else
            rval.add(p + "/" + name);
        }
        return rval;
    }

	private void postProcessLinks(Map<String, DeletedLinkInfo> links) {
        List<DeletedLinkInfo> linkInfo = new ArrayList<>(links.size());
        linkInfo.addAll(links.values());
        //in case of references to references this has to run more than once
        boolean runagain;
        do {
            runagain = false;
            for (Iterator<DeletedLinkInfo> it = linkInfo.iterator(); it.hasNext();) {
                DeletedLinkInfo link = it.next();
                Resource target = resMan.findResourcePrivileged("/" + link.path);
                if (target != null && target.exists()) { //recreate link
                    ((ResourceBase) link.parent).addDecoratorPrivileged(link.name, target, true);
                    it.remove();
                    runagain = true;
                } else {
                    //System.out.printf("RECREATE LINK, MISSING RESOURCE: %s/%s -> %s%n", link.parent.getPath(), link.name, link.path);
                }
            }
        } while (runagain);

	}

	@Override
	public void addAccessModeListener(AccessModeListener listener) {
        resMan.getDatabaseManager().lockRead();
        try {
            resMan.getDatabaseManager().getElementInfo(getEl()).addAccessModeListener(listener, this,
				resMan.getApplicationManager());
        } finally {
            resMan.getDatabaseManager().unlockRead();
        }
        
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
        resMan.getDatabaseManager().lockStructureRead();
        try {
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
            return this.el;
        } finally {
            resMan.getDatabaseManager().unlockStructureRead();
        }
	}

	protected void reload() {
		//for use in subclasses
	}

	protected final ResourceDBManager getResourceDB() {
		return resMan.getDatabaseManager();
	}

	protected void setLastUpdateTime() {
        TreeElement te = getTreeElement();
        while (te.isReference()) {
            te = te.getReference();
        }
		te.setLastModified(resMan.getApplicationManager().getFrameworkTime());
	}

	protected long getLastUpdateTime() {
        TreeElement te = getTreeElement();
        while (te.isReference()) {
            te = te.getReference();
        }
        return te.getLastModified();
	}
	
	private List<String> computeAliases() {
		return computeAliases(new HashSet<Resource>());
	}
    
    /**
     * Returns all paths under which this resource is reachable. In case of loops,
     * the path will contain the same resource at most twice.
     * @return list of alias paths for this resource, irrespective of access rights!
     */
    private List<String> computeAliases(Set<Resource> visited) {
        if (visited == null) {
            visited = new HashSet<>();
        }
        // visited set caught only loops with top level resources 
//        if (isTopLevel()) {
//            if (visited.contains(this)) {
//                return Arrays.asList("/" + getName());
//            }
//            visited.add(this);
//        }
        // FIXME too expensive? Remember only selected resources?
        if (visited.contains(this)) {
        	return Arrays.asList("/" + getPath());
        }
        visited.add(this);
        
        List<String> rval = new ArrayList<>();
        if (isTopLevel()) {
            rval.add("/" + getName());
        }
        for (Map.Entry<Resource, String> pre: incoming()) {
            String nameInPre = pre.getValue();
            for (String preAlias: ((ResourceBase)pre.getKey()).computeAliases(visited)){
                rval.add(preAlias + "/" + nameInPre);
            }
        }
        return rval;
    }
    
    /**
     * Returns all resources which contain this resource as a child element along
     * with that child element's name.
     * (Not a map because it would have to be a multimap)
     * @return resources containing this resource along with the name this resource has in the containing resource.
     */
    private Collection<Map.Entry<Resource, String>> incoming() {
        List<Resource> refs = getReferencingNodes(true);
        if (isTopLevel() && refs.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map.Entry<Resource, String>> rval = new ArrayList<>(refs.size()+1);
        if (!isTopLevel()) {
            rval.add(new AbstractMap.SimpleImmutableEntry<>(getParentPrivileged(), getName()));
        }
        // need privileged access to parents at this point... method must not throw a security exception
        for (Resource ref: refs) {
       		rval.add(new AbstractMap.SimpleImmutableEntry<>(((ResourceBase) ref).getParentPrivileged(), ref.getName()));
        }
        return rval;
    }
    
    /**
     * Similar to {@link #getReferences()}, except that no transitive refernces are reported
     * @return
     */
    @Override
    public List<Resource> getReferencingNodes(boolean transitive) {
        resMan.getDatabaseManager().lockStructureRead();
        try {
            Collection<TreeElement> refs = resMan.getDatabaseManager().getElementInfo(getEl()).getReferences(getPath(), transitive);
            if (refs == null || refs.isEmpty()) {
                return Collections.emptyList();
            }
            List<Resource> rval = new ArrayList<>(refs.size());
            for (TreeElement ref: refs) {
                Resource r = resMan.findResource(ref);
                if (r != null){
                    rval.add(r);
                }
            }
            return rval;
        } finally {
            resMan.getDatabaseManager().unlockStructureRead();
        }
    }
    
    @Override
    public <T extends Resource> T getLocationResource() {
    	return resMan.getApplicationManager().getResourceAccess().getResource(getLocation());
    }

}
