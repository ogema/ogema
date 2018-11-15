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
package org.ogema.drivers.homematic.xmlrpc.hl;

import org.apache.xmlrpc.XmlRpcException;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HomeMatic;
import org.slf4j.Logger;

/**
 *
 * @author jlapp
 */
class DeletionListener implements ResourceStructureListener, Runnable {
    
    final String device;
    final HomeMatic driver;
    final boolean resetOnDelete;
    final Logger logger;

    DeletionListener(String device, HomeMatic driver, boolean resetOnDelete, Logger logger) {
        this.device = device;
        this.driver = driver;
        this.resetOnDelete = resetOnDelete;
        this.logger = logger;
    }

    @Override
    public void resourceStructureChanged(ResourceStructureEvent event) {
        if (ResourceStructureEvent.EventType.RESOURCE_DELETED.equals(event.getType())) {
            new Thread(this).start();
        }
    }

    @Override
    public void run() {
        logger.debug("trying to delete {}, reset: {}", device, resetOnDelete);
        try {
            driver.deleteDevice(device, resetOnDelete ? 1 : 0);
            logger.debug("HomeMatic device {} un-paired and deleted", device);
        } catch (XmlRpcException ex) {
            logger.warn("deletion of {} failed, forcing removal from driver", device);
            try {
                driver.deleteDevice(device, 2);
                logger.debug("HomeMatic device {} removed without actual connection", device);
            } catch (XmlRpcException ex2) {
                logger.error("could not delete device {}", device);
            }
        }
    }
    
}
