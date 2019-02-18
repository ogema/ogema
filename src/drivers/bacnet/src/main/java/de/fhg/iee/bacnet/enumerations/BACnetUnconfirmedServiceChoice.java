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
package de.fhg.iee.bacnet.enumerations;

import de.fhg.iee.bacnet.api.BACnetEnumeration;

/**
 *
 * @author jlapp
 */
public enum BACnetUnconfirmedServiceChoice implements BACnetEnumeration {

    i_Am(0),
    i_Have(1),
    unconfirmedCOVNotification(2),
    unconfirmedEventNotification(3),
    unconfirmedPrivateTransfer(4),
    unconfirmedTextMessage(5),
    timeSynchronization(6),
    who_Has(7),
    who_Is(8),
    utcTimeSynchronization(9);

    private final int code;

    private BACnetUnconfirmedServiceChoice(int code) {
        this.code = code;
    }

    @Override
    public int getBACnetEnumValue() {
        return code;
    }

}
