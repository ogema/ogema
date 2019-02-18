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
public enum BACnetErrorCode implements BACnetEnumeration {

    other(0),
    abort_buffer_overflow(51),
    abort_invalid_apdu_in_this_state(52),
    abort_preempted_by_higher_priority_task(53),
    abort_segmentation_not_supported(54),
    abort_proprietary(55),
    abort_other(56),
    authentication_failed(1),
    character_set_not_supported(41),
    communication_disabled(83),
    configuration_in_progress(2),
    datatype_not_supported(47),
    device_busy(3),
    duplicate_name(48),
    duplicate_object_id(49),
    dynamic_creation_not_supported(4),
    file_access_denied(5),
    incompatible_security_levels(6),
    inconsistent_parameters(7),
    inconsistent_selection_criterion(8),
    invalid_array_index(42),
    invalid_configuration_data(46),
    invalid_data_type(9),
    invalid_event_state(73),
    invalid_file_access_method(10),
    invalid_file_start_position(11),
    invalid_operator_name(12),
    invalid_parameter_data_type(13),
    invalid_tag(57),
    invalid_time_stamp(14),
    key_generation_error(15),
    log_buffer_full(75),
    logged_value_purged(76),
    missing_required_parameter(16),
    network_down(58),
    no_alarm_configured(74),
    no_objects_of_specified_type(17),
    no_property_specified(77),
    no_space_for_object(18),
    no_space_to_add_list_element(19),
    no_space_to_write_property(20),
    no_vt_sessions_available(21),
    not_configured_for_triggered_logging(78),
    object_deletion_not_permitted(23),
    object_identifier_already_exists(24),
    operational_problem(25),
    optional_functionality_not_supported(45),
    password_failure(26),
    property_is_not_a_list(22),
    property_is_not_an_array(50),
    read_access_denied(27),
    reject_buffer_overflow(59),
    reject_inconsistent_parameters(60),
    reject_invalid_parameter_data_type(61),
    reject_invalid_tag(62),
    reject_missing_required_parameter(63),
    reject_parameter_out_of_range(64),
    reject_too_many_arguments(65),
    reject_undefined_enumeration(66),
    reject_unrecognized_service(67),
    reject_proprietary(68),
    reject_other(69),
    security_not_supported(28),
    service_request_denied(29),
    timeout(30),
    unknown_object(31),
    unknown_property(32),
    unknown_vt_class(34),
    unknown_vt_session(35),
    unsupported_object_type(36),
    value_out_of_range(37),
    vt_session_already_closed(38),
    vt_session_termination_failure(39),
    write_access_denied(40),
    cov_subscription_failed(43),
    not_cov_property(44);

    private final int code;

    private BACnetErrorCode(int code) {
        this.code = code;
    }

    @Override
    public int getBACnetEnumValue() {
        return code;
    }

}
