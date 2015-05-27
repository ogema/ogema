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
