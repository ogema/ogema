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
package org.ogema.drivers.homematic.xmlrpc.ll.api;

import java.util.List;

/**
 *
 * @author jlapp
 */
public interface HmBackend {
    
    /**
     * Retrieve the list of known devices from the backend / persistence for use
     * in the HomeMatic listDevices(...) function. According to the HomeMatic spec,
     * the DevicDescription only need their ADDRESS and VERSION fields set for
     * this use case.
     */
    public List<DeviceDescription> getKnownDevices(String interfaceId);
    
}
