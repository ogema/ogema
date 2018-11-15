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
