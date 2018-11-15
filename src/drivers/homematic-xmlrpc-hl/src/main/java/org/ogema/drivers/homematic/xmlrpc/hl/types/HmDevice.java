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
package org.ogema.drivers.homematic.xmlrpc.hl.types;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;

/**
 * OGEMA resource representing a logical device as defined in the HomeMatic
 * XML-RPC specification.
 * 
 * @see DeviceDescription
 * 
 * @author jlapp
 */
public interface HmDevice extends Resource {
    
    StringResource type();
    
    StringResource address();
    
    IntegerResource version();
    
    ResourceList<HmDevice> channels();
    
    StringArrayResource children();
    
    StringArrayResource paramsets();
    
    /**
     * List of OGEMA resources controlled by this HomeMatic device / channel.
     * May be a many to many relationship.
     * @return OGEMA resources controlled by this device.
     */
    ResourceList<Resource> controlledResources();
    
}
