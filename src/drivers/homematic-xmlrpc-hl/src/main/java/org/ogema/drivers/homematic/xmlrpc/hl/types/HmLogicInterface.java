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
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.actors.OnOffSwitch;

/**
 *
 * @author jlapp
 */
public interface HmLogicInterface extends Resource {
    
    public static final String ALIAS = "/homematic";

    /** @return Alias of the XML-RPC servlet on the local machine, defaults to {@value #ALIAS} */
    StringResource alias();
    
    /**
     * The base URL of the HomeMatic XML-RPC servlet. Use either this value or {@link #networkInterface() }
     * and {@link #port() }, which will also work in case local IP address changes.
     * @return Base URL of the XML-RPC servlet (e.g. http://192.168.0.10:8080)
     */
    StringResource baseUrl();
   
    /**
     * The network interface on which the HomeMatic XML-RPC can be reached by the 
     * HomeMatic gateway.
     * @return Network interface of the XML-RPC servlet (e.g. wlan0)
     */    
    StringResource networkInterface();
    IntegerResource port();
    
    StringResource clientUrl();

    ResourceList<HmDevice> devices();
    
    OnOffSwitch installationMode();
    
}
