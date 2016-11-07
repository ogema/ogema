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
package de.iwes.ogema.remote.rest.connector.model;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;

import de.iwes.ogema.remote.rest.connector.RemoteRestConnector;


/**
 * Resource describing a connection to a remote OGEMA resource via REST,
 * should be added as decorator.
 * RestConnection resources are used by {@link RemoteRestConnector}.
 * 
 * @author jlapp
 */
public interface RestConnection extends Resource {
    
    /**
     * REST URL of the remote resource. This has to be the full URL that would
     * would be used to access the remote OGEMA instance's REST interface directly.
     * 
     * @return REST URL of the resource that shall be mapped to this resource's parent.
     */
    StringResource remotePath();
    
    /**
     * @return interval at which to update the parent resource from the remote resource.
     * Time unit: ms
     */
    IntegerResource pollingInterval();
    
    /**
     * @return push local changes to the remote resource?
     */
    BooleanResource push();
    
    /**
     * @return push when a value of a subresource changes? Only 
     * evaluated if {@link #push()} is true.
     */
    BooleanResource pushOnSubresourceChanged();

    /** Call actions registered here before performing a push (allows applications to update resources). For
     * compatibility reasons we just give references to the stateControl resources here.*/
    ResourceList<BooleanResource> callBeforePush();
    /**
     * Write new value even if it does not differ from old one? 
     * (which may trigger {@see ResourceValueListener} callbacks)
     * @return
     */
    BooleanResource forceWrite();
    
    /**
     * remote REST user
     * @return
     */
    StringResource remoteUser();
    
    /**
     * Remote REST user pw
     * @return
     */
    // TODO do not store in visible resource
    StringResource remotePw();
    
}
