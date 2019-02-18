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
public enum BACnetObjectType implements BACnetEnumeration {
    access_door(30),
    accumulator(23),
    analog_input(0),
    analog_output(1),
    analog_value(2),
    averaging(18),
    binary_input(3),
    binary_output(4),
    binary_value(5),
    calendar(6),
    command(7),
    device(8),
    event_enrollment(9),
    event_log(25),
    file(10),
    group(11),
    life_safety_point(21),
    life_safety_zone(22),
    load_control(28),
    loop(12),
    multi_state_input(13),
    multi_state_output(14),
    multi_state_value(19),
    notification_class(15),
    program(16),
    pulse_converter(24),
    schedule(17),
    // see averaging  (18), 
    // see multi_state_value  (19), 
    structured_view(29),
    trend_log(20),
    trend_log_multiple(27), // see life_safety_point  (21), 
    // see life_safety_zone  (22), 
    // see accumulator  (23), 
    // see pulse_converter  (24), 
    // see event_log  (25), 
    // enumeration value 26 is reserved for a future addendum 
    // see trend_log_multiple (27), 
    // see load_control  (28), 
    // see structured_view  (29), 
    // see access_door  (30), 
    ;

    private final int code;
    
    private BACnetObjectType(int code) {
        this.code = code;
    }
    
    public int getBACnetEnumValue() {
        return code;
    }
    
    public static BACnetObjectType forEnumValue(int val) {
        for (BACnetObjectType o: values()) {
            if (val == o.getBACnetEnumValue()){
                return o;
            }
        }
        throw new IllegalArgumentException("unknown enum value: " + val);
    }

}
