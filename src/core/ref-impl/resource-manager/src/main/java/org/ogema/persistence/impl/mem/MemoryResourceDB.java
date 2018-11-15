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
package org.ogema.persistence.impl.mem;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.persistence.ResourceDB;
import org.ogema.resourcetree.TreeElement;
import org.slf4j.Logger;

/**
 * 
 * @author jlapp
 */
// FIXME unsynchronized access to types and resources variables?
public class MemoryResourceDB implements ResourceDB {

	final AtomicInteger TYPE_IDS = new AtomicInteger(0);
	final Map<Class<? extends Resource>, Integer> types = new HashMap<>();
	final Map<String, MemoryTreeElement> resources = new HashMap<>();
	final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
	
	@Override
	public Class<? extends Resource> addOrUpdateResourceType(Class<? extends Resource> type)
			throws ResourceAlreadyExistsException, InvalidResourceTypeException {
		synchronized (types) {
			Integer typeId = types.get(type);
			if (typeId == null) {
				typeId = TYPE_IDS.incrementAndGet();
				types.put(type, typeId);
			}
			return type;
		}
	}

	@Override
	public Collection<Class<?>> getTypeChildren(String name) throws InvalidResourceTypeException {
		try {
			Class<?> type = Class.forName(name);
			List<Class<?>> rval = new ArrayList<>();
			for (Method m : type.getMethods()) {
				if (Resource.class.isAssignableFrom(m.getReturnType())) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Resource> childType = (Class<? extends Resource>) m.getReturnType();
					rval.add(childType);
				}
			}
			return rval;
		} catch (ClassNotFoundException cnfe) {
			throw new InvalidResourceTypeException("Could not find the required resource type.");
		}
	}

	@Override
    @SuppressWarnings("unchecked")
	public boolean hasResourceType(String name) {
		try {
			Class<?> type = Class.forName(name);
			if (!Resource.class.isAssignableFrom(type)) {
				return false;
			}
            synchronized(types) {
                return types.containsKey((Class<? extends Resource>) type);
            }
		} catch (ClassNotFoundException cnfe) {
			return false;
		}
	}

	@Override
	public List<Class<? extends Resource>> getAllResourceTypesInstalled() {
        synchronized(types) {
            List<Class<? extends Resource>> rval = new ArrayList<>(types.size());
            rval.addAll(types.keySet());
            return rval;
        }
	}

	@Override
	public TreeElement addResource(String name, Class<? extends Resource> type, String appID)
			throws ResourceAlreadyExistsException, InvalidResourceTypeException {
        synchronized (resources) {
            if (resources.containsKey(name)) {
                throw new ResourceAlreadyExistsException("resource '" + name + "' already exists");
            }
            MemoryTreeElement el = new MemoryTreeElement(name, type, null);
            el.setAppID(appID);
            resources.put(name, el);        
            return el;
        }
	}

	@Override
	public void deleteResource(TreeElement elem) {
        if (!elem.isToplevel()){
            throw new UnsupportedOperationException("delete only implemented for top level resources");
        }
		synchronized (resources) {
			for (Map.Entry<String, MemoryTreeElement> e : resources.entrySet()) {
				if (e.getValue() == elem) {
					resources.remove(e.getKey());
					break;
				}
			}
		}
	}

	@Override
	public boolean hasResource(String name) {
        synchronized (resources) {
            return resources.containsKey(name);
        }
	}

	@Override
	public TreeElement getToplevelResource(String name) throws InvalidResourceTypeException {
        synchronized(resources) {
            TreeElement el = resources.get(name);
            return el;
        }
	}

	@Override
	public List<TreeElement> getAllToplevelResources() {
        synchronized (resources) {
    		List<TreeElement> rval = new ArrayList<>(resources.size());
            rval.addAll(resources.values());
			return rval;
		}
	}

	@Override
	public void finishTransaction() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
																		// Tools | Templates.
	}

	@Override
	public void startTransaction() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
																		// Tools | Templates.
	}

    @Override
    public boolean isDBReady() {
        return true;
    }

	@Override
	public TreeElement getByID(int id) {
		return null;
	}

	@Override
	public Collection<TreeElement> getFilteredNodes(Map<String, String> dict) {
		return null;
	}

	@Override
	public TreeElement getFilteredNodesByPath(String path, boolean isRoot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Class<?>> getModelDeclaredChildren(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Class<? extends Resource>> getResourceTypesInstalled(Class<? extends Resource> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void doStorage() {
		// TODO Auto-generated method stub
		
	}
}
