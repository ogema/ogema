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

import org.ogema.resourcemanager.impl.transaction.TransactionImpl;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.ogema.core.resourcemanager.Transaction;
import org.ogema.resourcemanager.impl.model.ResourceFactory;
import org.ogema.resourcetree.TreeElement;
import org.ogema.resourcemanager.virtual.VirtualTreeElement;
import org.slf4j.Logger;

/**
 * Implementation of {@link ResourceManagement} and {@link ResourceAccess}. Instances are created for each
 * application/user. The ApplicationResourceManager also holds all result instances requested by that application.
 */
public class ApplicationResourceManager implements ResourceManagement, ResourceAccess {

	protected final Logger logger;
	private final ApplicationManager appMan;
	private final ResourceDBManager dbMan;
	private final ResourceFactory factory;
	protected final Application app;
	protected final PermissionManager permissionManager;
	// contains all listenerregistrations performed by this app
	protected final Map<ResourceListenerRegistration, Object> registeredListeners;
	// contains Resources for which this app has requested an AccessMode != READ_ONLY
	protected final Collection<AccessModeRequest> accessedResources;
	// structure listeners registered by this app
	protected final Collection<StructureListenerRegistration> structureListeners;
	// resource demands registered by this app
	private final Collection<ResourceDemandListenerRegistration> resourceDemands;

	public ApplicationResourceManager(ApplicationManager appMan, Application app, ResourceDBManager dbMan,
			PermissionManager pManager) {
		Objects.requireNonNull(appMan);
		Objects.requireNonNull(app);
		Objects.requireNonNull(dbMan);
		Objects.requireNonNull(pManager);
		this.appMan = appMan;
		this.dbMan = dbMan;
		this.factory = new ResourceFactory(appMan, this, dbMan);
		this.app = app;
		this.permissionManager = pManager;
		this.registeredListeners = new HashMap<>();
		this.accessedResources = new HashSet<>();
		this.structureListeners = new HashSet<>();
		this.resourceDemands = new HashSet<>();
		logger = org.slf4j.LoggerFactory.getLogger("org.ogema.core.resourcemanager-" + app.getClass().getName());
	}

	public void close() {
		// unregister all access demands
		synchronized (accessedResources) {
			for (AccessModeRequest amr : accessedResources.toArray(new AccessModeRequest[accessedResources.size()])) {
				amr.getResource().requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);
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

	@SuppressWarnings("unchecked")
	protected <T extends Resource> T findResource(String path) {
		ResourceBase result;
		VirtualTreeElement el = findTreeElement(path);
		if (el == null) {
			return null;
		}
		/*
		 * @Security: Create ResourceAccessRights instance which is injected into the proxy object.
		 */
		ResourceAccessRights access = getAccessRights(el);

		if (System.getSecurityManager() != null && logger.isDebugEnabled()) {
			logger.debug("{}@{} (created by {}): read={}, write={}, add={}, create={}, delete={}", app.getClass()
					.getSimpleName(), path, el.getAppID(), access.isReadPermitted(), access.isWritePermitted(), access
					.isAddsubPermitted(), access.isCreatePermitted(), access.isDeletePermitted());
		}

		if (!access.isReadPermitted()) {
			throw new SecurityException(String.format(
					"You do not have permission to read the resource at %s, location %s", path, getLocationElement(el)
							.getPath()));
		}

		Class<? extends Resource> type = el.getType();
		if (type != null && type.equals(FloatResource.class)) {
			type = determineUnitResourceType(el);
		}
		result = factory.createResource(type, el, path);
		result.accessRights = access;

		return (T) result;
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
		assert FloatResource.class.isAssignableFrom(realResourceType);
		return realResourceType;
	}

	protected ResourceAccessRights getAccessRights(TreeElement el) {
		return permissionManager.getAccessRights(app, getLocationElement(el));
	}

	protected final TreeElement getLocationElement(TreeElement el) {
		while (el.isReference()) {
			el = el.getReference();
		}
		return el;
	}

	protected VirtualTreeElement findTreeElement(String path) {
		assert path.startsWith("/") : path;
		String[] names = path.split("/");
		assert names[0].isEmpty();
		if (names.length == 1) {
			return null;
		}
		VirtualTreeElement el = dbMan.getToplevelResource(names[1]);
		for (int i = 2; i < names.length && el != null; i++) {
			el = el.getChild(names[i]);
		}
		return el;
	}

	protected <T extends Resource> T findResource(final TreeElement el) {
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
			throw raiseException(new SecurityException(String.format(
					"Permission to create resource '%s' of type '%s'is denied!", name, type)));
		}

		if (!dbMan.hasResourceType(type.getCanonicalName())) {
			throw raiseException(new ResourceException("missing resource type: " + type));
		}

		dbMan.createResource(name, type, getAppId());
		return findResource("/" + name);
	}

	protected void requireValidResourceName(String name) {
		if (name == null) {
			throw raiseException(new NoSuchResourceException("name must not be null"));
		}
		if (name.isEmpty()) {
			throw raiseException(new NoSuchResourceException("name must not be empty"));
		}
		if (!Character.isJavaIdentifierStart(name.charAt(0))) {
			throw raiseException(new NoSuchResourceException(String.format(
					"name '%s' contains illegal character at position %d: '%c'", name, 0, name.charAt(0))));
		}
		for (int i = 1; i < name.length(); i++) {
			if (!Character.isJavaIdentifierPart(name.charAt(i))) {
				throw raiseException(new NoSuchResourceException(String.format(
						"name '%s' contains illegal character at position %d: '%c'", name, i, name.charAt(i))));
			}
		}
	}

	@Override
	public void deleteResource(String name) throws NoSuchResourceException {
		Resource resource = getResource(name);
		if (resource == null) {
			throw raiseException(new NoSuchResourceException("resource '" + name + "' not found."));
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
	public String getUniqueResourceName(String appResourceName) {
		return dbMan.getUniqueResourceName(appResourceName, app);
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

	/*
	 * @Security: This method is based on the sub call to getToplevelResources that ensures that only resources are
	 * returned, their proxy objects already equipped with the resource access rights. getDirectSubResources performs
	 * its the same way like getToplevelResources, so that the list returned contains only proxy references that already
	 * registered for this particular app via getResource or ResourceDemandListener.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Resource> List<T> getResources(Class<T> resourceType) {
		// Check all top-level resources and all their direct resources if they match the required type.
		final List<T> result = new ArrayList<>();
		for (Resource topRes : getToplevelResources(null)) {
			if (resourceType == null || resourceType.isAssignableFrom(topRes.getResourceType())) {
				result.add((T) topRes);
			}
			for (Resource subRes : topRes.getDirectSubResources(true)) {
				// resourceType == null is a special case for ResourceList resources.
				if (resourceType == null
						|| (subRes.getResourceType() != null && resourceType.isAssignableFrom(subRes.getResourceType()))) {
					result.add((T) subRes);
				}
			}
		}
		return result;
	}

	/*
	 * @Security: This method ensures that only resources are returned, their proxy objects already equipped with the
	 * resource access rights.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Resource> List<T> getToplevelResources(Class<T> resourceType) {
		final List<T> result = new ArrayList<>();
		for (TreeElement top : dbMan.getAllToplevelResources()) {
			final Class<? extends Resource> type = top.getType();
			if (!ResourceBase.validResourceName(top.getName())) {
				continue;
			}
			ResourceAccessRights access = permissionManager.getAccessRights(app, top);
			if (!access.isReadPermitted()) {
				continue;
			}
			if (resourceType == null || (type != null && resourceType.isAssignableFrom(type))) {
				result.add((T) getResource("/" + top.getName()));
			}
		}
		return result;
	}

	@Override
	public Transaction createTransaction() {
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
		List<RegisteredValueListener> registeredResourceListeners = new ArrayList<>(registeredListeners.size());
		for (ResourceListenerRegistration rlr : registeredListeners.keySet().toArray(
				new ResourceListenerRegistration[registeredListeners.keySet().size()])) {
			if (rlr instanceof RegisteredValueListener) {
				registeredResourceListeners.add((RegisteredValueListener) rlr);
			}
		}
		return registeredResourceListeners;
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

	public List<RegisteredResourceDemand> getResourceDemands() {
		checkAdminPermission();
		synchronized (resourceDemands) {
			List<RegisteredResourceDemand> rval = new ArrayList<>();
			rval.addAll(resourceDemands);
			return rval;
		}
	}
}
