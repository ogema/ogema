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
package org.ogema.resourcemanager.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.ogema.resourcemanager.impl.transaction.ResourceTransactionImpl;
import org.ogema.resourcemanager.impl.transaction.TransactionImpl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.ogema.accesscontrol.AdminPermission;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.ResourceAccessRights;
import org.ogema.core.administration.RegisteredAccessModeRequest;
import org.ogema.core.administration.RegisteredResourceDemand;
import org.ogema.core.administration.RegisteredResourceListener;
import org.ogema.core.administration.RegisteredStructureListener;
import org.ogema.core.administration.RegisteredValueListener;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.core.resourcemanager.NoSuchResourceException;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.transaction.ResourceTransaction;
import org.ogema.resourcemanager.impl.model.ResourceFactory;
import org.ogema.resourcetree.TreeElement;
import org.ogema.resourcetree.listeners.InternalStructureListenerRegistration;
import org.ogema.resourcetree.listeners.InternalValueChangedListenerRegistration;
import org.ogema.resourcetree.listeners.ResourceLock;
import org.ogema.resourcemanager.virtual.DefaultVirtualResourceDB;
import org.ogema.resourcemanager.virtual.VirtualTreeElement;
import org.slf4j.Logger;

/**
 * Implementation of {@link ResourceManagement} and {@link ResourceAccess}. Instances are created for each
 * application/user. The ApplicationResourceManager also holds all result instances requested by that application.
 */
public class ApplicationResourceManager implements ResourceManagement, ResourceAccess, ResourceLock {

	protected final Logger logger;
	private final ApplicationManager appMan;
	private final ResourceDBManager dbMan;
	private final ResourceFactory factory;
	protected final Application app;
	protected final PermissionManager permissionManager;
	// contains all listenerregistrations performed by this app
//	 key is either of type ResourceListenerRegistration or InternalValueChangedListenerRegistration
	protected final Map<RegisteredResourceListener, Object> registeredListeners;
	protected final Map<InternalValueChangedListenerRegistration, Object> registeredValueListeners;
	// contains Resources for which this app has requested an AccessMode != READ_ONLY
	protected final Collection<AccessModeRequest> accessedResources;
	// structure listeners registered by this app
	private final Collection<InternalStructureListenerRegistration> structureListeners;
	// resource demands registered by this app
	private final Collection<ResourceDemandListenerRegistration> resourceDemands;
	private final Cache<String, ResourceAccessRights> accessRights;
	
	public ApplicationResourceManager(ApplicationManager appMan, Application app, ResourceDBManager dbMan,	PermissionManager pManager) {
		Objects.requireNonNull(appMan);
		Objects.requireNonNull(app);
		Objects.requireNonNull(dbMan);
		Objects.requireNonNull(pManager);
		this.appMan = appMan;
		this.dbMan = dbMan;
		this.factory = new ResourceFactory(appMan, this, dbMan);
		this.app = app;
		this.permissionManager = pManager;
		this.registeredListeners = new ConcurrentHashMap<>();
		this.registeredValueListeners = new ConcurrentHashMap<>();
		this.accessedResources = new HashSet<>();
		this.structureListeners = new HashSet<>();
		this.resourceDemands = new HashSet<>();
        this.accessRights = CacheBuilder.newBuilder().softValues().build();//new ConcurrentHashMap<>();
		logger = org.slf4j.LoggerFactory.getLogger("org.ogema.core.resourcemanager-" + app.getClass().getName());
	}

	/*
	 * Since we cannot guarantee that the application manager is not leaked by the application,
	 * we better remove all existing references, as far as possible
	 */
	public void close() {
		unregisterListeners();
		// unregister all access demands
		// must be closed before dbMan is invalidated
		dbMan.lockRead();
		try {
			synchronized (accessedResources) {
				for (AccessModeRequest amr : accessedResources.toArray(new AccessModeRequest[accessedResources.size()])) {
					amr.getResource().requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);
				}
				accessedResources.clear();
			}
		} finally {
			dbMan.unlockRead();
		}
//		registeredListeners.clear();
//		registeredValueListeners.clear();
//		synchronized (structureListeners) {
//			structureListeners.clear();
//		}
//		synchronized (resourceDemands) {
//			resourceDemands.clear();
//		}
		synchronized (accessRights) {
			accessRights.invalidateAll();
		}
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	private void unregisterListeners() {
		// this will clear the registeredListeners map as well
		if (!registeredListeners.isEmpty()) {
			for (RegisteredResourceListener l : new ArrayList<>(registeredListeners.keySet())) {
				try {
					l.getResource().removeResourceListener(l.getListener());
				} catch (Exception e) {
					// should not be too relevant
					logger.info("Listener removal failed: " + e); 
				}
			}
		}
		if (!registeredValueListeners.isEmpty()) {
			for (InternalValueChangedListenerRegistration l : new ArrayList<>(registeredValueListeners.keySet())) {
				try {
					l.getResource().removeValueListener(l.getValueListener());
				} catch (Exception e) {
					logger.info("Listener removal failed: " + e); 
				}
			}
		}
		synchronized (structureListeners) {
			if (!structureListeners.isEmpty()) {
				for (InternalStructureListenerRegistration l : new ArrayList<>(structureListeners)) {
					try {
						l.getResource().removeStructureListener(l.getListener());
					} catch (Exception e) {
						logger.info("Listener removal failed: " + e); 
					}
				}
			}
		}
		synchronized (resourceDemands) {
			if (!resourceDemands.isEmpty()) {
				for (ResourceDemandListenerRegistration l : new ArrayList<>(resourceDemands)) {
					try {
						removeResourceDemand(l.getTypeDemanded(), l.getListener());
					} catch (Exception e) {
						
					}
				}
			}
		}
	}
	

	// @Override
	// public boolean controlResource(Resource resource, AccessMode accessMode, AccessPriority priority)
	// throws SecurityException {
	// throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose
	// Tools | Templates.
	// }

	protected final String getAppId() {
		return appMan.getAppID().getIDString();
	}

	protected <T extends Resource> T findResource(String path) {
		VirtualTreeElement el = findTreeElement(path);
		if (el == null) {
			return null;
		}
		return createResourceObject(el, path, false);
	}
    
	protected <T extends Resource> T findResourcePrivileged(String path) {
		VirtualTreeElement el = findTreeElement(path);
		if (el == null) {
			return null;
		}
		return createResourceObject(el, path, true);
	}
	
    protected <T extends Resource> T findExistingResource(String path, boolean privileged) {
		VirtualTreeElement el = findExistingTreeElement(path);
		if (el == null) {
			return null;
		}
		return createResourceObject(el, path, privileged);
	}
    
    @SuppressWarnings("unchecked")
	<T extends Resource> T createResourceObject(VirtualTreeElement el, String path, boolean privileged) {
        ResourceBase result;
        /*
		 * @Security: Create ResourceAccessRights instance which is injected into the proxy object.
		 */
		final ResourceAccessRights access = getAccessRights(el);
		if (!privileged) {
			if (System.getSecurityManager() != null && logger.isTraceEnabled()) {
				logger.trace("{}@{} (created by {}): read={}, write={}, add={}, create={}, delete={}", app.getClass()
						.getSimpleName(), path, el.getAppID(), access.isReadPermitted(), access.isWritePermitted(), access
						.isAddsubPermitted(), access.isCreatePermitted(), access.isDeletePermitted());
			}
	
			if (!access.isReadPermitted()) {
				throw new SecurityException(String.format(
						"Application %s does not have permission to read the resource at %s, location %s",
                        appMan.getAppID().getIDString(),path, getLocationElement(el).getPath()));
			}
		} 
		Class<? extends Resource> type = el.getType();
        if (type == null) {
            throw new IllegalStateException("TreeElement " + el.getPath() + " has type null");
        }
		if (type.equals(FloatResource.class)) {
			type = determineUnitResourceType(el);
		}
		result = factory.createResource(type, el, path);
		result.accessRights = access;
        
        T typedResult = (T) result;
		return typedResult;
    }

	protected Class<? extends FloatResource> determineUnitResourceType(TreeElement el) {
		while (el.isReference()) {
			el = el.getReference();
		}
		TreeElement parent = el.getParent();
		if (parent == null || el.isDecorator()) {
			return FloatResource.class;
		}
		@SuppressWarnings("unchecked")
		Class<? extends FloatResource> realResourceType = (Class<? extends FloatResource>) ResourceBase
				.getOptionalElementTypeOfType(parent.getType(), el.getName());
		// null should not occur, but has been seen in rare occasions after manipulating the resource tree
		// maybe after modifying the type definition?
		if (realResourceType == null || !FloatResource.class.isAssignableFrom(realResourceType)) {
			return FloatResource.class;
		}
		return realResourceType;
	}

	protected ResourceAccessRights getAccessRights(TreeElement el) {
		TreeElement location = getLocationElement(el);
		// here "no user" is treated as a special system user
		final String user = permissionManager.getAccessManager().getCurrentUser();
		// since pound signs are not allowed in Java variable names they cannot appear in valid resource paths either;
		// note however that there may exist hidden resources with invalid names; TODO ensure they do not conflict with this convention
		final String userSpecificLocation = location.getPath() + "##" + user;
		ResourceAccessRights r = accessRights.getIfPresent(userSpecificLocation);
		if (r == null) {
			r = permissionManager.getAccessRights(app, location, user);
			accessRights.put(userSpecificLocation, r);
		}
		return r;
	}

	protected final TreeElement getLocationElement(TreeElement el) {
		while (el.isReference()) {
			el = el.getReference();
		}
		return el;
	}

	protected VirtualTreeElement findTreeElement(String path) {
		assert path.startsWith("/") : "illegal path: " + path;
		String[] names = path.split("/");
		assert names[0].isEmpty();
		if (names.length == 1) {
			return null;
		}
        int level = 1;
		VirtualTreeElement el = dbMan.getToplevelResource(names[level]);
		for (level = 2; level < names.length && el != null; level++) {
			el = el.getChild(names[level]);
		}
        if (el == null && !ResourceBase.validResourceName(names[level-1])) {
            throw new NoSuchResourceException("Path " + path + " contains illegal name '" + names[level-1] + "'");
        }
		return el;
	}
    
    // return null or a non-virtual element
    protected VirtualTreeElement findExistingTreeElement(String path) {
		assert path.startsWith("/") : "illegal path: " + path;
		String[] names = path.split("/");
		assert names[0].isEmpty();
		if (names.length == 1) {
			return null;
		}
		VirtualTreeElement el = dbMan.getToplevelResource(names[1]);
		for (int i = 2; i < names.length && el != null; i++) {
			el = el.getExistingChild(names[i]);
            if (el == null) {
                return null;
            }
		}
		return el;
	}

	protected <T extends Resource> T findResource(final TreeElement el) {
        Objects.requireNonNull(el);
        if (el instanceof VirtualTreeElement)
        	return createResourceObject((VirtualTreeElement) el, "/" + el.getPath(),  false);
		Deque<String> nameStack = new ArrayDeque<>();
		for (TreeElement p = el; p != null; p = p.getParent()) {
			nameStack.push(p.getName());
		}
		StringBuilder sb = new StringBuilder();
		while (!nameStack.isEmpty()) {
			sb.append("/").append(nameStack.pop());
		}
		return findResource(sb.toString());
	}

	// result management methods ------------------------------------>>>>
	@Override
	public <T extends Resource> T createResource(String name, Class<T> type) throws ResourceException {
		Objects.requireNonNull(type, "type must not be null");
		requireValidResourceName(name);
		addOrUpdateResourceType(type);

		/*
		 * @Securtiy: Check if create Resource permission is granted for this type. The resource to be created is a top
		 * level one, so the path information is not relevant for the permission check. The count is set to 0, so its
		 * ignored during permission check. When resource management supports maximum count of created resources, the
		 * count should be set to the number of resources from this type that already created by this application.
		 */
		if (System.getSecurityManager() != null && !permissionManager.checkCreateResource(app, type, name, 0)) {
//			throw raiseException(new SecurityException(String.format(
//					"Permission to create resource '%s' of type '%s'is denied!", name, type)));
			throw new SecurityException(String.format(
					"Permission to create resource '%s' of type '%s'is denied!", name, type));
		}

		if (!dbMan.hasResourceType(type.getCanonicalName()) || !factory.hasResourceType(type)) {
//			throw raiseException(new ResourceException("missing resource type: " + type));
			throw new ResourceException("missing resource type: " + type);
		}

		dbMan.createResource(name, type, getAppId());
		return findResource("/" + name);
	}

	protected void requireValidResourceName(String name) {
		if (name == null) {
//			throw raiseException(new NoSuchResourceException("name must not be null"));
			throw new NoSuchResourceException("name must not be null");
		}
		if (name.isEmpty()) {
//			throw raiseException(new NoSuchResourceException("name must not be empty"));
			throw new NoSuchResourceException("name must not be empty");
		}
		if (!Character.isJavaIdentifierStart(name.charAt(0))) {
//			throw raiseException(new NoSuchResourceException(String.format(
//					"name '%s' contains illegal character at position %d: '%c'", name, 0, name.charAt(0))));
			throw new NoSuchResourceException(String.format(
					"name '%s' contains illegal character at position %d: '%c'", name, 0, name.charAt(0)));
		}
		for (int i = 1; i < name.length(); i++) {
			if (!Character.isJavaIdentifierPart(name.charAt(i))) {
//				throw raiseException(new NoSuchResourceException(String.format(
//						"name '%s' contains illegal character at position %d: '%c'", name, i, name.charAt(i))));
				throw new NoSuchResourceException(String.format(
						"name '%s' contains illegal character at position %d: '%c'", name, i, name.charAt(i)));
			}
		}
	}

	@Override
	public void deleteResource(String name) throws NoSuchResourceException {
		Resource resource = getResource(name);
		if (resource == null) {
//			throw raiseException(new NoSuchResourceException("resource '" + name + "' not found."));
			throw new NoSuchResourceException("resource '" + name + "' not found.");
		}
		resource.delete();
	}

	@Override
	public List<Class<? extends Resource>> getResourceTypes() {
		return dbMan.getAllResourceTypesInstalled();
	}

	private void addOrUpdateResourceType(Class<? extends Resource> resourceType) {
		if (resourceType == null)
			return;
		try {
			dbMan.addOrUpdateResourceType(resourceType);
		} catch (ResourceAlreadyExistsException | InvalidResourceTypeException ex) {
			// FIXME what should happen if an exception occurs?
			logger.error("fixme: it is not clear what should happen if an exception occurs here.", ex);
			throw new IllegalStateException("fixme", ex);
		}
	}

	@Override
    @Deprecated
	public String getUniqueResourceName(String appResourceName) {
		return dbMan.getUniqueResourceName(appResourceName, Long.toString(appMan.getAppID().getBundle().getBundleId()));
	}

	/*
	 * @Securtity: No check is required. Before the call back to resourceAvailable is performed the access rights are
	 * determined and injected into the resource proxy object.
	 */
	@Override
	public <S extends Resource, T extends S> void addResourceDemand(Class<T> resourceType,
			ResourceDemandListener<S> listener) {
		Objects.requireNonNull(resourceType);
		Objects.requireNonNull(listener);
		final ResourceDemandListenerRegistration demandHandler = new ResourceDemandListenerRegistration(listener,
				resourceType, this);
		dbMan.addResourceDemandListener(resourceType, demandHandler);
		synchronized (resourceDemands) {
			resourceDemands.add(demandHandler);
		}
		// queue callbacks for all already-existing resources.
		for (Resource resource : getResources(resourceType)) {
			if (resource.isActive()) {
				final TreeElement element = ((ConnectedResource) resource).getTreeElement();
				demandHandler.resourceAvailable(element);
			}
		}
	}

	@Override
	public <S extends Resource, T extends S> void removeResourceDemand(Class<T> resourceType,
			ResourceDemandListener<S> listener) {
		ResourceDemandListenerRegistration reg = new ResourceDemandListenerRegistration(listener, resourceType, this);
		dbMan.removeResourceDemandListener(resourceType, reg);
		synchronized (resourceDemands) {
			resourceDemands.remove(reg);
		}
	}

	@Override
	public <T extends Resource> T getResource(String path) throws SecurityException {
		// if user did not bother to add leading slash, add it here.
		if (path.length() > 0 && path.charAt(0) != '/') {
			path = "/" + path;
		}
		return findResource(path);
	}
	
	protected <T extends Resource> T getResourcePrivileged(String path) throws SecurityException {
		// if user did not bother to add leading slash, add it here.
		if (path.length() > 0 && path.charAt(0) != '/') {
			path = "/" + path;
		}
		return findResourcePrivileged(path);
	}
	
	@Override
	public <T extends Resource> List<T> getResources(Class<T> resourceType) {
		if (resourceType == Resource.class)
			resourceType = null; // more efficient
		dbMan.lockStructureRead();
		try {
//			return getResourcesClassic(resourceType);
			return getResourcesWithFilter(resourceType);
		} finally {
			dbMan.unlockStructureRead();
		}
	}

	/*
	 * @Security: This method is based on the sub call to getToplevelResources that ensures that only resources are
	 * returned, their proxy objects already equipped with the resource access rights. getDirectSubResources performs
	 * its the same way like getToplevelResources, so that the list returned contains only proxy references that already
	 * registered for this particular app via getResource or ResourceDemandListener.
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	private <T extends Resource> List<T> getResourcesClassic(final Class<T> resourceType) {
		// Check all top-level resources and all their direct resources if they match the required type.
		final List<T> result = new ArrayList<>();
		for (Resource topRes : getToplevelResources(null)) {
			if (resourceType == null || resourceType.isAssignableFrom(topRes.getResourceType())) {
				result.add((T) topRes);
			}
			for (Resource subRes : ((ResourceBase)topRes).getDirectSubResources(resourceType, true)) {
				// resourceType == null is a special case for ResourceList resources.
				if (resourceType == null
						|| (subRes.getResourceType() != null && resourceType.isAssignableFrom(subRes.getResourceType()))) {
					result.add((T) subRes);
				}
			}
		}
		return result;
	}
    
    @SuppressWarnings("unchecked")
	private <T extends Resource> List<T> getResourcesWithFilter(final Class<T> resourceType) {
		final List<T> result = new ArrayList<>();
        final boolean secure = System.getSecurityManager() != null;
        for (TreeElement e: dbMan.getElementsByType(resourceType, true)) {
            if ((!secure || isReadPermitted(e)) && ResourceBase.validResourceName(e)) {
                result.add((T) createResourceObject((VirtualTreeElement) e, "/" + e.getPath(), true));
            }
        }
		return result;
	}    
	
	@SuppressWarnings("unchecked")
	private <T extends Resource> List<T> getResourcesViaTreeElements(final Class<T> resourceType) {
		final List<Resource> result0 = new ArrayList<>();
		final Collection<TreeElement> topElements = dbMan.getAllToplevelResources();
		final boolean secure = System.getSecurityManager() != null;
		for (TreeElement el : topElements) {
			appendSubResources(resourceType, result0, el, true, secure);			
		}
		return (List<T>) result0;		
	}
    
	void appendSubResources(final Class<? extends Resource> resourceType, final List<Resource> result, final TreeElement base, 
			final boolean recursive, final boolean secure) {
		if (!ResourceBase.validResourceName(base.getName()))
			return;
		if ((resourceType == null || resourceType.isAssignableFrom(base.getType()))	&& (!secure || isReadPermitted(base))) {
			try {
//				final Resource r = findResource("/" + base.getPath());
				result.add(createResourceObject(((DefaultVirtualResourceDB) dbMan.resdb).getElement(base), "/" + base.getPath(), false));
			} catch (SecurityException | InvalidResourceTypeException e) {
				logger.warn("Unexpected exception", e);
			}
		}
		if (!recursive)
			return;
		for (TreeElement sub : base.getChildren()) {
			if (sub.isReference())
				continue;
			appendSubResources(resourceType, result, sub, recursive, secure);
		}
	}
	
	private final boolean isReadPermitted(final TreeElement element) {
		return getAccessRights(element).isReadPermitted();
	}
	
	/*
	 * @Security: This method ensures that only resources are returned, their proxy objects already equipped with the
	 * resource access rights.
	 */
	@Override
	public <T extends Resource> List<T> getToplevelResources(Class<T> resourceType) {
		final List<T> result = new ArrayList<>();
		for (TreeElement top : dbMan.getAllToplevelResources()) {
			final Class<? extends Resource> type = top.getType();
			if (!ResourceBase.validResourceName(top.getName())) {
				continue;
			}
			if (!isReadPermitted(top))
				continue;
			if (resourceType == null || (type != null && resourceType.isAssignableFrom(type))) {
                @SuppressWarnings("unchecked")
                T resource = (T) getResource("/" + top.getName());
                if (resource != null) {
                    result.add(resource);
                }
			}
		}
		return result;
	}

	@Override
    @Deprecated
	public org.ogema.core.resourcemanager.Transaction createTransaction() {
		return new TransactionImpl(dbMan, appMan);
	}

	final ResourceDBManager getDatabaseManager() {
		return dbMan;
	}

	ApplicationManager getApplicationManager() {
		return appMan;
	}

	protected <T extends Throwable> T raiseException(T ex) {
		getApplicationManager().reportException(ex);
		return ex;
	}

	private void checkAdminPermission() {
		if (System.getSecurityManager() != null
				&& !permissionManager.handleSecurity(new AdminPermission(AdminPermission.APP)))
			throw new SecurityException("Operation requires application administration permission");
	}

	public List<RegisteredResourceListener> getResourceListeners() {
		checkAdminPermission();
		List<RegisteredResourceListener> registeredResourceListeners = new ArrayList<>(registeredListeners.size());
		for (ResourceListenerRegistration rlr : registeredListeners.keySet().toArray(
				new ResourceListenerRegistration[registeredListeners.keySet().size()])) {
			if (!(rlr instanceof ValueListenerRegistration)) {
				registeredResourceListeners.add(rlr);
			}
		}
		return registeredResourceListeners;
	}

	public List<RegisteredValueListener> getValueListeners() {
		checkAdminPermission();
		List<RegisteredValueListener> list = new ArrayList<>();
		for (InternalValueChangedListenerRegistration l: registeredValueListeners.keySet()) {
			list.add(l);
		}
		return list;
	}

	public List<RegisteredAccessModeRequest> getAccessRequests() {
		checkAdminPermission();
		synchronized (accessedResources) {
			List<RegisteredAccessModeRequest> rval = new ArrayList<>(accessedResources.size());
			rval.addAll(accessedResources);
			return rval;
		}
	}

	public List<RegisteredStructureListener> getStructureListeners() {
		checkAdminPermission();
		synchronized (structureListeners) {
			List<RegisteredStructureListener> rval = new ArrayList<>(structureListeners.size());
			rval.addAll(structureListeners);
			return rval;
		}
	}
    
    protected void addStructureListenerRegistration(InternalStructureListenerRegistration slr) {
        synchronized (structureListeners) {
            structureListeners.add(slr);
        }
    }
    
    protected boolean removeStructureListenerRegistration(InternalStructureListenerRegistration slr) {
        synchronized (structureListeners) {
            return structureListeners.remove(slr);
        }
    }

	public List<RegisteredResourceDemand> getResourceDemands() {
		checkAdminPermission();
		synchronized (resourceDemands) {
			List<RegisteredResourceDemand> rval = new ArrayList<>();
			rval.addAll(resourceDemands);
			return rval;
		}
	}
	
	@Override
	public ResourceTransaction createResourceTransaction() {
		return new ResourceTransactionImpl(dbMan, appMan);
	}
	
	public void lockRead() {
		dbMan.lockRead();
//		dbMan.lockStructureRead(); // currently does the same as lockRead
	}
	
	public void unlockRead() {
//		dbMan.unlockStructureRead();
		dbMan.unlockRead();
	}
	
	public void lockWrite() {
		dbMan.lockWrite();
	}
	
	public void unlockWrite() {
		dbMan.unlockWrite();
	}
	
}
