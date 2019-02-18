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
package org.ogema.drivers.bacnet;

import de.fhg.iee.bacnet.api.Transport;
import de.fhg.iee.bacnet.enumerations.BACnetObjectType;
import de.fhg.iee.bacnet.services.CovSubscriber;
import de.fhg.iee.bacnet.tags.BitStringTag;
import org.ogema.drivers.bacnet.models.BACnetTransportConfig;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
@Component(service = Application.class)
public class BACnetDriverApp implements Application {

    final Logger logger = LoggerFactory.getLogger(getClass());
    ApplicationManager appman;
    
    Map<BACnetTransportConfig, BACnetDriver> configs = new HashMap<>();
    
    ResourceDemandListener<BACnetTransportConfig> configListener = new ResourceDemandListener<BACnetTransportConfig>() {
        @Override
        public void resourceAvailable(BACnetTransportConfig resource) {
            logger.info("adding transport config {}", resource.getPath());
            configAdded(resource);
        }

        @Override
        public void resourceUnavailable(BACnetTransportConfig resource) {
            BACnetDriver t = configs.get(resource);
            if (t != null) {
                try {
                    logger.info("closing transport for removed config {}", resource.getPath());
                    t.close();
                } catch (Exception ioex) {
                    logger.warn("closing transport failed", ioex);
                }
            }
        }
    };

    void configAdded(BACnetTransportConfig config) {
        try {
            BACnetDriver t = new BACnetDriver(appman, config);
            t.start();
            configs.put(config, t);
        } catch (IOException ex) {
            logger.error("failed to start BACnet config", ex);
        }
    }

    @Override
    public void start(ApplicationManager appManager) {
        this.appman = appManager;
        appman.getResourceAccess().addResourceDemand(BACnetTransportConfig.class, configListener);
    }

    @Override
    public void stop(AppStopReason reason) {
        configs.forEach((cfg, t) -> {
            try {
                t.close();
                logger.info("closed transport for {}", cfg.getPath());
            } catch (Exception ex) {
                logger.warn("close failed for transport " + cfg.getPath(), ex);
            }
        });
    }

}
