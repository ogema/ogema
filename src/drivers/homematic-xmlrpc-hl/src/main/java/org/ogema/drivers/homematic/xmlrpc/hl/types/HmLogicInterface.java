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
     * The network interface on which the driver servlet can be reached by the 
     * HomeMatic gateway. Use together with {@link #port()},
     * alternatively to {@link #baseUrl()}.
     * @return Network interface of the XML-RPC servlet (e.g. wlan0)
     */    
    StringResource networkInterface();
    
    /**
     * The port on which the driver servlet can be reached. Use together with {@link #networkInterface()},
     * alternatively to {@link #baseUrl()}.
     * @return
     */
    IntegerResource port();
    
    /**
     * URL of the XML-RPC service. Use alternatively to {@link #serialNumber()} and {@link #clientPort()} 
     * if gateway discovery is not available. With recent CCU software the preferred alternative
     * is the use of {@link #serialNumber()}.  
     * @return
     */
    StringResource clientUrl();
    
    /**
     * Serial number of the CCU (real or emulated). Requires the CCU to support UDP discovery.
     * Use together with {@link #clientPort()}, alternatively to {@link #clientUrl()}.
     * @return
     */
    StringResource serialNumber();
    
    /**
     * Port number of the XML-RPC service interface. Typical values are 2001 for BidCos RF, 2010 for Homematic IP,
     * 2000 for BidCos wired.
     * Use together with {@link #serialNumber()}, alternatively to {@link #clientUrl()}.
     * @return
     */
    IntegerResource clientPort();

    /**
     * Created by driver.
     * @return
     */
    ResourceList<HmDevice> devices();
    
    /**
     * Determine whether installation mode is active (subresource {@link OnOffSwitch#stateFeedback()})
     * and activate installation mode (subresource {@link OnOffSwitch#stateControl()}). 
     * @return
     */
    OnOffSwitch installationMode();
    
    StringResource ccuUser();
    StringResource ccuPw();
    
}
