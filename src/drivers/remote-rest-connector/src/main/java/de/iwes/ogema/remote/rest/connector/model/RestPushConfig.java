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
