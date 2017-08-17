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
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;

/**
 * Configuration for pulling from a remote OGEMA server. 
 * 
 * @author dnestle
 */
public interface RestPullConfig extends Resource {
    
    /**
     * Path relative to target resource of the RemoteRESTConfiguration.
     * This is only relevant for individual push configurations. Note
     * that the subresource must exist both locally and remote for this to work (tbc),
     * unless {@link #pushOnInit()} is true, in which case the remote resource
     * will be created if it does not exist.
     */
    StringResource remoteRelativePath();
    
    /**
     * If not specified standard depth will be returned (no sub resources)
     */
    IntegerResource depth();
    
    /**
     * Pull schedule subresources? Default is false, like for the REST interface in general.
     * @return
     */
    BooleanResource schedules();
    
    /**
     * Resolve references? If this is false (default), then remote references will lead to references
     * on the local system if the reference target path exists locally, but are ignored otherwise. If 
     * this is true, then remote references will not be represented as references locally, but will 
     * be resolved.
     * @return
     */
    BooleanResource resolveReferences();
    
    /**
     * If true and the remote resource does not exist, the client will push the target resource
     * before starting to pull from the server. If the target resource does not exist on the client,
     * this is ignored.
     * 
     * @return
     */
    BooleanResource pushOnInit();
    
    /**
     * Write new value even if it does not differ from old one? 
     * (which may trigger {@see ResourceValueListener} callbacks)
     * @return
     * 
     */
    BooleanResource forceWrite();
}
