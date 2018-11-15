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
     * (which may trigger ResourceValueListener callbacks)
     * @return
     * 
     */
    BooleanResource forceWrite();
}
