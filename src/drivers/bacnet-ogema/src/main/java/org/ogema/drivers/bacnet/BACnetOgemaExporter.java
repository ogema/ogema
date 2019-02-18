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

import de.fhg.iee.bacnet.enumerations.BACnetEventState;
import de.fhg.iee.bacnet.enumerations.BACnetObjectType;
import de.fhg.iee.bacnet.enumerations.BACnetPropertyIdentifier;
import de.fhg.iee.bacnet.tags.BitStringTag;
import de.fhg.iee.bacnet.tags.EnumeratedValueTag;
import de.fhg.iee.bacnet.tags.ObjectIdentifierTag;
import de.fhg.iee.bacnet.tags.Tag;
import de.fhg.iee.bacnet.tags.TagConstants;
import de.fhg.iee.bacnet.tags.UnsignedIntTag;
import org.ogema.drivers.bacnet.models.BACnetExportConfig;
import java.io.Closeable;
import java.io.IOException;
import java.util.BitSet;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exports configured OGEMA resources as BACnet objects.
 * @author jlapp
 */
public class BACnetOgemaExporter implements Closeable {
    
    final Logger logger = LoggerFactory.getLogger(getClass());
    
    final BACnetDriver transport;
    final ApplicationManager appman;
    final BACnetExportConfig config;
    
    Resource targetResource;

    public BACnetOgemaExporter(BACnetDriver transport, ApplicationManager appman, BACnetExportConfig config) {
        this.transport = transport;
        this.appman = appman;
        this.config = config;
        setup();
    }
    
    final void setup() {
        if (config.target().exists()) {
            targetResource = config.target();
        } else {
            targetResource = config.getParent();
        }
        if (targetResource == null) {
            throw new IllegalArgumentException("missing target resource on " + config.getPath());
        }
        int objectType = config.objectIdentifier().type().getValue();
        ObjectIdentifierTag oid = new ObjectIdentifierTag(objectType, 
            config.objectIdentifier().instanceNumber().getValue());


        //TODO: other types
        if (!BooleanResource.class.isAssignableFrom(targetResource.getResourceType())) {
            throw new IllegalArgumentException("unsupported target resource type on " + config.getPath());
        }        
        BooleanResource br = (BooleanResource) targetResource;
        BitSet supportedObjectTypes = transport.supportedObjectType.toBitSet();
        supportedObjectTypes.set(BACnetObjectType.binary_value.getBACnetEnumValue());
        transport.supportedObjectType = new BitStringTag(TagConstants.TAG_BIT_STRING,
                Tag.TagClass.Application, supportedObjectTypes);
        
        transport.ds.addProperty(oid, BACnetPropertyIdentifier.object_name,
                BACnetDriver.stringTag(config.objectName(), targetResource.getLocation()));
        transport.ds.addProperty(oid, BACnetPropertyIdentifier.object_identifier, BACnetDriver.staticValue(oid));
        transport.ds.addProperty(oid, BACnetPropertyIdentifier.object_type,
                BACnetDriver.staticValue(new UnsignedIntTag(oid.getObjectType())));
        transport.ds.addProperty(oid, BACnetPropertyIdentifier.present_value,
                () -> { return new EnumeratedValueTag(br.getValue()? 1 : 0); },
                ct -> {
                    int value = ct.getSubTags().iterator().next().getUnsignedInt().intValue();
                    logger.debug("writing to {}", br.getLocation());
                    br.setValue(value != 0);
                });
        transport.ds.addProperty(oid, BACnetPropertyIdentifier.out_of_service,
                BACnetDriver.booleanTag(config.outOfService(), false));
        //TODO: bitString <--> string conversion
        transport.ds.addProperty(oid, BACnetPropertyIdentifier.member_status_flags,
                BACnetDriver.staticValue(new BitStringTag()));
        transport.ds.addProperty(oid, BACnetPropertyIdentifier.event_state,
                BACnetDriver.unsignedIntTag(config.eventState(), BACnetEventState.normal.getBACnetEnumValue()));
    }

    @Override
    public void close() throws IOException {
    }    
    
}
