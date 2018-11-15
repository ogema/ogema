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
package org.ogema.driver.knxdriver;

import org.ogema.core.model.ValueResource;
import org.ogema.core.model.simple.BooleanResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tuwien.auto.calimero.cemi.CEMI;
import tuwien.auto.calimero.dptxlator.DPT;
import tuwien.auto.calimero.dptxlator.DPTXlator;
import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean;
import tuwien.auto.calimero.exception.KNXFormatException;

/**
 * Stores data point values (as reveived by a NetworkLinkListener indication) to 
 * a resource.
 * @author jlapp
 */
abstract class IndicationHandler<R extends ValueResource, X extends DPTXlator> {
    
    final R resource;
    
    final X xlator;
    
    final Logger logger = LoggerFactory.getLogger(ComSystemKNX.class);

    public IndicationHandler(R resource, X xlator) {
        this.resource = resource;
        this.xlator = xlator;
    }
    
    void indication(CEMI c) {
        xlator.setData(c.getPayload(), 1); //XXX offset? only tested with single Boolean
        storeIndicationValue();
    }
    
    abstract void storeIndicationValue();
    
    static IndicationHandler<BooleanResource, DPTXlatorBoolean> createBooleanHandler(BooleanResource r, DPT dpt) throws KNXFormatException {
        return new IndicationHandler<BooleanResource, DPTXlatorBoolean>(r, new DPTXlatorBoolean(dpt)) {
            @Override
            void storeIndicationValue() {
                resource.setValue(xlator.getValueBoolean());
            }
        };
    }
    
}
