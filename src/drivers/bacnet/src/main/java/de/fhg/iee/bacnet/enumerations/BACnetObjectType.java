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
    timer(31),
    access_credential(32),
    access_point(33),
    access_rights(34),
    access_user(35),
    access_zone(36),
    credential_data_input(37),
    network_security(38),
    bitstring_value(39),
    characterstring_value(40),
    datepattern_value(41),
    date_value(42),
    datetimepattern_value(43),
    datetime_value(44),
    integer_value(45),
    large_analog_value(46),
    octetstring_value(47),
    positive_integer_value(48),
    timepattern_value(49),
    time_value(50),
    notification_forwarder(51),
    alert_enrollment(52),
    channel(53),
    lighting_output(54),
    binary_lighting_output(55),
    network_port(56),
    elevator_group(57),
    escalator(58),
    lift(59),
    custom(199),
    hierarchy(200),
	component(201)
    ;

    private final int code;
    
    private BACnetObjectType(int code) {
        this.code = code;
    }
    
    public int getBACnetEnumValue() {
        return code;
    }
    
    /** Get type value
     * 
     * @param val
     * @return null if no type for the value is found. The value may correspond to a custom type.
     */
    public static BACnetObjectType forEnumValue(int val) {
        for (BACnetObjectType o: values()) {
            if (val == o.getBACnetEnumValue()){
                return o;
            }
        }
        return null;
        //throw new IllegalArgumentException("unknown enum value: " + val);
    }

}
