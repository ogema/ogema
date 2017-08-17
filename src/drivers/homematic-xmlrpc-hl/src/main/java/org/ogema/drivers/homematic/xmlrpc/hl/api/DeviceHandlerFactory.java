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

/**
 * Used by the HomeMatic driver to provide {@link DeviceHandler} instances
 * working on an available {@link HomeMaticConnection}. Objects of this class
 * shall be registered as OSGi services and will be picked up by the driver.
 * The driver will call DeviceHandlers from all available factories in a simple
 * chain-of-responsibility fashion ordered by the OSGi service ranking of
 * the factory.
 * 
 * @author jlapp
 */
public interface DeviceHandlerFactory {
    
    /**
     * Creates a new {@link DeviceHandler}. The handler instance is expected to
     * store the given connection.
     * @param connection 
     * @return new handler
     */
    DeviceHandler createHandler(HomeMaticConnection connection);
    
}
