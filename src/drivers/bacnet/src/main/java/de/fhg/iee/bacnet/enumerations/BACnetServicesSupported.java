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
public enum BACnetServicesSupported implements BACnetEnumeration {
    // Alarm and Event Services 
    acknowledgeAlarm(0),
    confirmedCOVNotification(1),
    confirmedEventNotification(2),
    getAlarmSummary(3),
    getEnrollmentSummary(4),
    // getEventInformation (39), 
    subscribeCOV(5),
    // subscribeCOVProperty (38), 
    // lifeSafetyOperation (37), 
    // File Access Services 
    atomicReadFile(6),
    atomicWriteFile(7),
    // Object Access Services 
    addListElement(8),
    removeListElement(9),
    createObject(10),
    deleteObject(11),
    readProperty(12),
    readPropertyConditional(13),
    readPropertyMultiple(14),
    // readRange (35), 
    writeProperty(15),
    writePropertyMultiple(16),
    // Remote Device Management Services 
    deviceCommunicationControl(17),
    confirmedPrivateTransfer(18),
    confirmedTextMessage(19),
    reinitializeDevice(20),
    // Virtual Terminal Services 
    vtOpen(21),
    vtClose(22),
    vtData(23),
    // Security Services 
    authenticate(24),
    requestKey(25),
    // Unconfirmed Services 
    i_Am(26),
    i_Have(27),
    unconfirmedCOVNotification(28),
    unconfirmedEventNotification(29),
    unconfirmedPrivateTransfer(30),
    unconfirmedTextMessage(31),
    timeSynchronization(32),
    // utcTimeSynchronization (36), 
    who_Has(33),
    who_Is(34),
    // Services added after 1995 
    readRange(35), // Object Access Service 
    utcTimeSynchronization(36), // Remote Device Management Service 
    lifeSafetyOperation(37), // Alarm and Event Service 
    subscribeCOVProperty(38), // Alarm and Event Service 
    getEventInformation(39) // Alarm and Event Service 
    ;

    private final int code;

    private BACnetServicesSupported(int code) {
        this.code = code;
    }

    @Override
    public int getBACnetEnumValue() {
        return code;
    }

    public static BACnetServicesSupported forEnumValue(int val) {
        for (BACnetServicesSupported o : values()) {
            if (val == o.getBACnetEnumValue()) {
                return o;
            }
        }
        throw new IllegalArgumentException("unknown enum value: " + val);
    }

}
