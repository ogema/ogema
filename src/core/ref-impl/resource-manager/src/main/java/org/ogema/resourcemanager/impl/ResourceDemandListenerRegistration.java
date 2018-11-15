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

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.RegisteredResourceDemand;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.resourcetree.TreeElement;

/**
 *
 * @author jlapp
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ResourceDemandListenerRegistration implements RegisteredResourceDemand {
    protected final ResourceDemandListener listener;
    protected final Class<? extends Resource> type;
    /* TreeElements that have been reported via resourceAvailable() */
    private final Collection<TreeElement> availableResources = new HashSet<>();
    private final ApplicationResourceManager resman;

    public ResourceDemandListenerRegistration(ResourceDemandListener l, Class<? extends Resource> type, final ApplicationResourceManager resman) {
        this.resman = resman;
        this.listener = l;
        this.type = type;
    }

    /*
     * called by {@link ResourceDBManager} when a result with compatible
     * type has been added, activated or access rights changed(?). also used
     * for existing resources when a new listener is registered
     */
    public void resourceAvailable(final TreeElement el) {
        try{
            final Resource res = resman.findResource(el);
            availableResources.add(el);
            Callable<Void> listenerCall = createResourceAvaillableCallback(res);
            resman.getApplicationManager().submitEvent(listenerCall);
        } catch (SecurityException se){
            resman.logger.info("No permissions for resource matching ResourceDemand: {}", se.getMessage());
        }
    }
    
    private Callable<Void> createResourceAvaillableCallback(final Resource res){
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    /*
                     * @Security: Access rights injection is already done in findResource
                     */
                    listener.resourceAvailable(res);
                } catch (Throwable t) {
                    resman.logger.error("{}.resourceAvailable({}): ", listener, res, t);
                    throw t;
                }
                return null;
            }
        };
    }

    public void resourceDeleted(TreeElement el) {
        if (!availableResources.contains(el)) {
            return;
        }
        availableResources.remove(el);
        final Resource resource;
        try {
        	resource = resman.findResource(el);
        } catch (SecurityException e) {
        	// the other resource manager may not have the permission to read this resource, or some parent of it
        	// unclear when exactly this happens
        	return;
        }
        if (resource == null) { 
        	resman.logger.error("Resource unavailable callback cannot be fired, resource {} not found. This is an internal framework error.", 
        			(el != null ? el.getPath() : null));
        	return;
        }
        
        Callable<Void> listenerCall = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    /*
                     * @Security: Access rights injection is already done in findResource
                     */
                    //XXX holding lock during callback...
                    //resman.getDatabaseManager().lockStructureWrite();
                    try{
                        listener.resourceUnavailable(resource);
                    } finally {
                        //resman.getDatabaseManager().unlockStructureWrite();
                    }
                } catch (Throwable t) {
                    throw t;
                }
                return null;
            }
        };
        resman.getApplicationManager().submitEvent(listenerCall);
    }

    public void resourceDeactivated(final TreeElement el) {
        if (!availableResources.contains(el)) {
            return;
        }
        availableResources.remove(el);
        Callable<Void> listenerCall = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
            	Resource r = resman.findResource(el);
            	if (r != null)
            		listener.resourceUnavailable(r);
                return null;
            }
        };
        resman.getApplicationManager().submitEvent(listenerCall);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.type);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ResourceDemandListenerRegistration other = (ResourceDemandListenerRegistration) obj;
        if (!Objects.equals(this.listener, other.listener)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        return true;
    }

    @Override
    public AdminApplication getApplication() {
        return resman.getApplicationManager().getAdministrationManager().getAppById(resman.getApplicationManager().getAppID().getIDString());
    }

    @Override
    public Class<? extends Resource> getTypeDemanded() {
        return type;
    }

    @Override
    public ResourceDemandListener getListener() {
        return listener;
    }

}
