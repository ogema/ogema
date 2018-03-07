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
