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
 * Configuration for pushing to a remote OGEMA server.
 */
public interface RestPushConfig extends Resource {

    /**
     * Path relative to target resource of the RemoteRESTConfiguration
     * This is only relevant for individual push configurations. Note
     * that the subresource must exist both locally and remote for this to work (tbc).
     */
    StringResource remoteRelativePath();
    
    /**
     * Push when a value of a subresource changes? Alternatively, the push can be triggered 
     * periodically, by configuring the {@link RestConnection#pushInterval()}. 
     * @return push when a value of a subresource changes? 
     */
    BooleanResource pushOnSubresourceChanged();
    
    /**
     * Push schedule subresources? Default is false, like for the REST interface in general.
     * @return
     */
    BooleanResource schedules();
    
    /**
     * Resolve references? If this is false (default), then local references will lead to references
     * on the remote system if the reference target path exists remotely, but are ignored otherwise. If 
     * this is true, then local references will not be represented as references remotely, but will 
     * be resolved.
     * @return
     */
    BooleanResource resolveReferences();
	
    /**
     * Push subresources up to this depth.
     * Default value (if resource inactive): 0
     * @return
     */
    IntegerResource depth();
}
