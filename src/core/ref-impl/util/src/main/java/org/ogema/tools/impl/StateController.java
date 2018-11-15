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
package org.ogema.tools.impl;

import java.util.HashSet;
import org.ogema.core.model.Resource;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.core.tools.SerializationManager;

/**
 *
 * @author esternberg
 */
public class StateController {

    private final HashSet<Resource> visitedResources = new HashSet<>();
    private SerializationManager serializationManager = null;
    private Resource requestedRootResource = null;
    private int currentDepth = 0;
    
    public StateController(SerializationManager serializationManager, Resource requestedRootResource){
        this.serializationManager = serializationManager;
        this.requestedRootResource = requestedRootResource;
    }
    
    /**
     * This method proofs if we can decent to the next resource using the given boundary
     * conditions of SerializationManager (maxDepth, followReferences and graph-cycle avoidance).
     * If yes the state (currentDepth) is updated, so one call is one step.
     * @param resource
     * @return true if we can proceed traversing to the childresources of the given resource.
     * false if some boundary conditions are no more fulfilled and we can not proceed traversing.
     */
    public boolean doDescent(Resource resource){
        //we can proceed traversing if:
        return visitedResources.add(resource) //the given resource is not visited twice (graph-cycle termination criteria)
                && (currentDepth++ <= serializationManager.getMaxDepth()) // and we still are in range of the allowed depth
                && (resource.isReference(false) ? serializationManager.getFollowReferences() : true) //and we are allowed to follow references  
                && (resource instanceof ReadOnlyTimeSeries ? serializationManager.getSerializeSchedules() : true); //and the resource is no or serializeShedules flag is true
    }
    
    public boolean isRquestedRootResource(Resource resource){
        return this.requestedRootResource.equals(resource);
    }
    
    public void decreaseDepth(){
        currentDepth--;
        assert currentDepth > -1 : "illegal state: depth = " + currentDepth;
    }
    
}
