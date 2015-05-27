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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ogema.accesscontrol.AccessManager;
import org.ogema.core.application.Application;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.persistence.ResourceDB;
import org.ogema.recordeddata.DataRecorder;
import org.ogema.resourcemanager.impl.timeseries.DefaultRecordedData;
import org.ogema.resourcemanager.virtual.DefaultVirtualResourceDB;
import org.ogema.resourcemanager.virtual.VirtualResourceDB;
import org.ogema.resourcemanager.virtual.VirtualTreeElement;
import org.ogema.resourcetree.TreeElement;
import org.ogema.timer.TimerScheduler;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;

/**
 * Wraps a {@link ResourceDB} and adds listener handling for {@link ResourceDemandListener}s. The
 * {@link ApplicationResourceManager} instances must only access the ResourceDB through the ResourceDBManager.
 * 
 * @author jlapp
 */
public class ResourceDBManager {

	/* app id used by this class when creating TreeElements for internal use */
	final static String APP_ID_SYSTEM = "system";
	final static String ELEMENTNAME_UNIQUENAMES = "@uniquenames";

	private final VirtualResourceDB resdb;
	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
	private final AccessManager access;
	private final DataRecorder recordedDataAccess;
    private final AtomicInteger revisionCounter = new AtomicInteger(0);
    
	final private Map<Class<? extends Resource>, List<ResourceDemandListenerRegistration>> resourceTypeListeners = new HashMap<>();
	/**
	 * Global lock for read- and write-operations.
	 */
	private final ReadWriteLock commitLock = new ReentrantReadWriteLock();
	/** global lock guarding structural changes */
	private final ReadWriteLock structureLock = new ReentrantReadWriteLock();
	protected RecordedDataManager recordedDataManager;

	public ResourceDBManager(ResourceDB resdb, DataRecorder recordedDataAccess, TimerScheduler scheduler,
			AccessManager access) {
		Objects.requireNonNull(resdb);

		// this.resdb = resdb;
		this.resdb = new DefaultVirtualResourceDB(resdb);
		this.recordedDataAccess = recordedDataAccess;
		this.access = access;

		this.recordedDataManager = new RecordedDataManager(this, recordedDataAccess, scheduler);
		init();

	}

	// initialize ElementInfo on all TreeElements
	private void init() {
		for (TreeElement top : getAllToplevelResources()) {
			Deque<TreeElement> stack = new ArrayDeque<>(10);
			Deque<Boolean> isReferencePath = new ArrayDeque<>(10);
			Set<TreeElement> visited = new HashSet<>();
			stack.push(top);
			isReferencePath.push(false);
			TreeElement el;
			while (!stack.isEmpty()) {
				el = stack.pop();
				boolean isRef = isReferencePath.pop();
				if (!visited.contains(el)) {
					visited.add(el);
					for (TreeElement child : el.getChildren()) {
						stack.push(child);
						boolean childIsReference = child.isReference();
						isReferencePath.push(childIsReference | isRef);
						if (childIsReference) {
							getElementInfo(child).addReference(el);
						}
					}
				}
			}
		}

		initTimeseries();
	}

	private void initTimeseries() {
		for (String id : recordedDataAccess.getAllRecordedDataStorageIDs()) {
			TreeElement el = DefaultRecordedData.findTreeElement(id, this);
			if (el == null) {
				logger.warn("found recorded data for unknown resource: {}, deleting...", id);
				recordedDataAccess.deleteRecordedDataStorage(id);
			}
			else {
				DefaultRecordedData d = recordedDataManager.getRecordedData(el);
				logger.debug("initialized recorded data: {}", d);
			}
		}
	}

    public DefaultRecordedData getRecordedData(TreeElement e){
        return recordedDataManager.getRecordedData(e);
    }

	public synchronized String getUniqueResourceName(String name, Application app) {
		String uniqueName = getStoredUniqueName(name, app);
		if (uniqueName != null) {
			return uniqueName;
		}
		else {
			if (resdb.getToplevelResource(name) == null) {
				storeUniqueName(name, app, name);
				return name;
			}
			else {
				int c = 0;
				do {
					uniqueName = name + "_" + ++c;
				} while (resdb.getToplevelResource(uniqueName) != null);
				storeUniqueName(name, app, uniqueName);
				return uniqueName;
			}
		}
	}

	private String getStoredUniqueName(String name, Application app) {
		TreeElement uniqueNamesEl = resdb.getToplevelResource(ELEMENTNAME_UNIQUENAMES);
		if (uniqueNamesEl == null) {
			return null;
		}
		String appId = app.getClass().getCanonicalName();
		Bundle b = FrameworkUtil.getBundle(app.getClass());
		if (b != null) {
			appId = b.getSymbolicName();
		}
        appId = appId.replace('.', '_').replace('/', '_');
		TreeElement namesForApp = uniqueNamesEl.getChild(appId);
		if (namesForApp == null) {
			return null;
		}
		TreeElement storedName = namesForApp.getChild(name);
		if (storedName == null) {
			return null;
		}
		else {
            String uniqueName = storedName.getData().getString();
            logger.info("read stored unique name {}/{}: {}", appId, name, uniqueName);
			return uniqueName;
		}
	}

	private void storeUniqueName(String requestedName, Application app, String uniqueName) {
		TreeElement uniqueNamesEl = resdb.getToplevelResource(ELEMENTNAME_UNIQUENAMES);
		if (uniqueNamesEl == null) {
			resdb.addOrUpdateResourceType(Resource.class);
			resdb.addOrUpdateResourceType(StringResource.class);
			uniqueNamesEl = resdb.addResource(ELEMENTNAME_UNIQUENAMES, Resource.class, APP_ID_SYSTEM);
		}
		String appId = app.getClass().getCanonicalName();
		Bundle b = FrameworkUtil.getBundle(app.getClass());
		if (b != null) {
			appId = b.getSymbolicName();
		}
        appId = appId.replace('.', '_').replace('/', '_');
		TreeElement namesForApp = uniqueNamesEl.getChild(appId);
		if (namesForApp == null) {
			namesForApp = uniqueNamesEl.addChild(appId, Resource.class, true);
		}
		TreeElement storedName = namesForApp.addChild(requestedName, StringResource.class, true);
		storedName.getData().setString(uniqueName);
		storedName.fireChangeEvent();
        logger.debug("stored unique name {}/{}: {}", appId, requestedName, uniqueName);
	}

	/*
	 * return a TreeElement's user object, if el is a reference, return the referenced TreeElement's user object.
	 */
	public ElementInfo getElementInfo(TreeElement el) {
		if (el.getResRef() == null) {
			if (el.isReference()) {
                TreeElement r = el;
                while(r.isReference()){
                    r = r.getReference();
                    assert r != el : "reference loop at " + el.getPath();
                }
                ElementInfo info = getElementInfo(r);
				el.setResRef(info);
				return info;
			}
			else {
				ElementInfo info = new ElementInfo(this, el);
				el.setResRef(info);
				return info;
			}
		}
		if (el.isReference()) {
			return getElementInfo(el.getReference());
		}
		return (ElementInfo) el.getResRef();
	}

	/* returns the TreeElements primary path, that is the path without any references */
	protected String buildPrimaryPath(TreeElement el) {
		Deque<String> nameStack = new ArrayDeque<>();
		for (TreeElement p = el; p != null; p = p.getParent()) {
			nameStack.push(p.getName());
		}
		StringBuilder sb = new StringBuilder();
		while (!nameStack.isEmpty()) {
			sb.append("/").append(nameStack.pop());
		}
		return sb.toString();
	}

	/*
	 * checks that end is reachable from start, also test for infinite loops, throwing an IllegalStateException if a
	 * loop is encountered
	 */
	protected boolean pathExists(TreeElement start, TreeElement end, boolean useReferences) {
		if (start == end) {
			return true;
		}
		Set<Integer> visitedIDs = new HashSet<>();
		Deque<TreeElement> stack = new ArrayDeque<>();
		stack.push(start);
		while (!stack.isEmpty()) {
			TreeElement el = stack.pop();
			if (visitedIDs.contains(el.getResID())) {
				throw new IllegalStateException("infinite loop including " + buildPrimaryPath(start));
			}
			visitedIDs.add(el.getResID());
			if (end.getResID() == el.getResID()) {
				return true;
			}
			for (TreeElement child : el.getChildren()) {
				if (child.isReference() && !useReferences) {
					continue;
				}
				stack.push(child);
			}
		}
		return false;
	}

	/*
	 * returns all TreeElements in the subtree at 'start' in the order encountered by a depth first traversal, beginning
	 * with 'start'
	 */
	protected List<TreeElement> collectSubTreeElements(TreeElement start) {
		Deque<TreeElement> stack = new ArrayDeque<>();
		List<TreeElement> rval = new ArrayList<>();
		stack.push(start);
		while (!stack.isEmpty()) {
			TreeElement el = stack.pop();
			if (el.isReference() && rval.contains(el)) {
				continue;
			}
			rval.add(el);
			for (TreeElement child : el.getChildren()) {
				stack.push(child);
			}
		}
		return rval;
	}

	/*
	 * add a listeners for resource demands by type, does no security checks
	 */
	public void addResourceDemandListener(Class<? extends Resource> type, ResourceDemandListenerRegistration reg) {
		synchronized (resourceTypeListeners) {
			List<ResourceDemandListenerRegistration> l = resourceTypeListeners.get(type);
			if (l == null) {
				l = new ArrayList<>();
				resourceTypeListeners.put(type, l);
			}
			l.add(reg);
		}
	}

	public void removeResourceDemandListener(Class<? extends Resource> type, ResourceDemandListenerRegistration reg) {
		synchronized (resourceTypeListeners) {
			List<ResourceDemandListenerRegistration> l = resourceTypeListeners.get(type);
			if (l == null) {
				return;
			}
			Iterator<ResourceDemandListenerRegistration> it = l.iterator();
			while (it.hasNext()) {
				ResourceDemandListenerRegistration o = it.next();
				if (o.type == type && o.listener == reg.listener) {
					it.remove();
				}
			}
		}
	}

	// Map<Integer, TreeElementInfo> elements = new HashMap<>();
	public Class<? extends Resource> addOrUpdateResourceType(Class<? extends Resource> type)
			throws ResourceAlreadyExistsException, InvalidResourceTypeException {
		return resdb.addOrUpdateResourceType(type);
	}

	public Collection<Class<?>> getTypeChildren(String name) throws InvalidResourceTypeException {
		return resdb.getTypeChildren(name);
	}

	public boolean hasResourceType(String name) {
		return resdb.hasResourceType(name);
	}

	public List<Class<? extends Resource>> getAllResourceTypesInstalled() {
		return resdb.getAllResourceTypesInstalled();
	}

	public TreeElement createResource(String name, Class<? extends Resource> type, String appId)
			throws ResourceAlreadyExistsException, InvalidResourceTypeException {
		if (resdb.hasResource(name)) {
			TreeElement existingElement = resdb.getToplevelResource(name);
			if (!existingElement.getType().equals(type)) {
				throw new ResourceAlreadyExistsException(String.format(
						"resource '%s' already exists with different type (%s)", name, type));
			}
			else {
				return existingElement;
			}
		}
		// create new resource. resource is created as inactive, so no listener callbacks are necessary
		TreeElement el = resdb.addResource(name, type, appId);
		ElementInfo info = new ElementInfo(this, el);
		el.setResRef(info);
		return el;
	}

	public void deleteResource(TreeElement elem) {
		resdb.deleteResource(elem);
        revisionCounter.incrementAndGet();
        resourceDeleted(elem);
    }

	public boolean hasResource(String name) {
		return resdb.hasResource(name);
	}

	public VirtualTreeElement getToplevelResource(String name) throws InvalidResourceTypeException {
		return resdb.getToplevelResource(name);
	}

	public Collection<TreeElement> getAllToplevelResources() {
		return resdb.getAllToplevelResources();
	}

	public void finishTransaction() {
		resdb.finishTransaction();
	}

	public void startTransaction() {
		resdb.startTransaction();
	}

	public boolean isDBReady() {
		return resdb.isDBReady();
	}

	/*
	 * Called by Resource implementation when an inactive Resource is activated
	 */
	public void resourceActivated(TreeElement el) {
		Class<? extends Resource> elType = el.getType();
		synchronized (resourceTypeListeners) {
			for (Map.Entry<Class<? extends Resource>, List<ResourceDemandListenerRegistration>> e : resourceTypeListeners
					.entrySet()) {
				if (e.getKey().isAssignableFrom(elType)) {
					for (ResourceDemandListenerRegistration reg : e.getValue()) {
						reg.resourceAvailable(el);
					}
				}
			}
		}

		ElementInfo info = getElementInfo(el);
		info.fireResourceActiveStateChanged(el, true);
	}

	/*
	 * Called by Resource implementation when an active Resource is deactivated
	 */
	public void resourceDeactivated(TreeElement el) {
		resourceUnavailable(el, ResourceDemandListener.AccessLossReason.RESOURCE_INACTIVE);

		ElementInfo info = getElementInfo(el);
		info.fireResourceActiveStateChanged(el, false);
	}
    
    public void resourceDeleted(TreeElement el) {
        resourceUnavailable(el, ResourceDemandListener.AccessLossReason.RESOURCE_DELETED);
    }

	/*
	 * used to inform all listener registrations that a resource may have become unavailable. listener registations need
	 * to decide themselves if the listener (app) has to be notified
	 */
	private void resourceUnavailable(TreeElement el, ResourceDemandListener.AccessLossReason reason) {
		Class<? extends Resource> elType = el.getType();
		synchronized (resourceTypeListeners) {
			for (Map.Entry<Class<? extends Resource>, List<ResourceDemandListenerRegistration>> e : resourceTypeListeners
					.entrySet()) {
				if (e.getKey().isAssignableFrom(elType)) {
					for (ResourceDemandListenerRegistration reg : e.getValue()) {
						switch (reason) {
						case RESOURCE_DELETED:
							reg.resourceDeleted(el);
							break;
						case RESOURCE_INACTIVE:
							reg.resourceDeactivated(el);
							break;
						}
					}
				}
			}
		}
	}

	public DataRecorder getRecordedDataAccess() {
		return recordedDataAccess;
	}

	/**
	 * Returns the locking handle for structural changes.
	 */
	ReadWriteLock getStructureLock() {
		return structureLock;
	}

	/**
	 * Lock for reading.
	 */
	public void lockRead() {
		commitLock.readLock().lock();
	}

	/**
	 * Lock for writing.
	 */
	public void lockWrite() {
		commitLock.writeLock().lock();
	}

	/**
	 * Unlock reading.
	 */
	public void unlockRead() {
		commitLock.readLock().unlock();
	}

	/**
	 * Unlock writing.
	 */
	public void unlockWrite() {
		commitLock.writeLock().unlock();
	}
    
    public int getRevision(){
        return revisionCounter.get();
    }
    
    public int incrementRevision(){
        return revisionCounter.incrementAndGet();
    }

}
