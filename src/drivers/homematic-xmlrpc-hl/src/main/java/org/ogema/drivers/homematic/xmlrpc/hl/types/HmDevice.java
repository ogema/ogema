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
