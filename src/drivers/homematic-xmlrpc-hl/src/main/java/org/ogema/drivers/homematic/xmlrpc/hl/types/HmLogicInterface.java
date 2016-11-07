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
