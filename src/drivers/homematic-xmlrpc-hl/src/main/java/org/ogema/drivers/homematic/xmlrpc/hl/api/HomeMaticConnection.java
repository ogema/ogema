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
package org.ogema.drivers.homematic.xmlrpc.hl.api;

import java.util.List;
import java.util.Map;
import org.ogema.core.model.Resource;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;

/**
 * Used by {@link DeviceHandler} implementations to communicate with its HomeMatic
 * logic interface and to setup OGEMA specific device/resource relations.
 *
 * @author jlapp
 */
public interface HomeMaticConnection {

    /**
     * Register an event listener with this connection.
     * @param l event listener
     */
    void addEventListener(HmEventListener l);

    /**
     * Returns the HmDevice element controlling the given OGEMA resource, as
     * configured with {@link #registerControlledResource }
     *
     * @param ogemaDevice
     * @return HomeMatic device resource controlling the given resource or
     * null.
     * @see #registerControlledResource(org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice, org.ogema.core.model.Resource) 
     */
    @SuppressWarnings(value = "rawtypes")
    HmDevice findControllingDevice(Resource ogemaDevice);

    /**
     * Finds a channel resource for a given device.
     * @param device top level device
     * @param channelAddress 
     * @return device channel with given address or null.
     */
    HmDevice getChannel(HmDevice device, String channelAddress);

    /**
     * Returns the resource representing the HomeMatic device the channel belongs to.
     * If called with a top level device resource, return the argument.
     * @param channel
     * @return top level device resource.
     */
    HmDevice getToplevelDevice(HmDevice channel);

    /**
     * Calls the {@code addLink} method of the HomeMatic logic interface.
     * @param sender homematic address of the sending device.
     * @param receiver homematic address of the receiving device.
     * @param name user defined name for the link.
     * @param description link description.
     */
    void performAddLink(String sender, String receiver, String name, String description);
    
    /**
     * Calls the {@code removeLink} method of the HomeMatic logic interface.
     * @param sender homematic address of the sending device.
     * @param receiver homematic address of the receiving device.
     */
    void performRemoveLink(String sender, String receiver);
    
    List<Map<String, Object>> performGetLinks(String address, int flags);

    void performPutParamset(String address, String set, Map<String, Object> values);

    void performSetValue(String address, String valueKey, Object value);

    /**
     * Configure a control relationship between the homematic device and a resource
     * that can be retrieved by using {@link #findControllingDevice }.
     * A device handler should call this method for every device resource that
     * it creates, so that the control relationship can be retrieved by
     * other device handlers.
     * 
     * @param channel resource of a homematic device channel.
     * @param ogemaDevice resource controlled by the homematic device.
     */
    void registerControlledResource(HmDevice channel, Resource ogemaDevice);

    void removeEventListener(HmEventListener l);
    
}
