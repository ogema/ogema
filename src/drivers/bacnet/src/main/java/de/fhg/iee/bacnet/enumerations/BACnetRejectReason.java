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
public enum BACnetRejectReason implements BACnetEnumeration {

    other(0),
    buffer_overflow(1),
    inconsistent_parameters(2),
    invalid_parameter_data_type(3),
    invalid_tag(4),
    missing_required_parameter(5),
    parameter_out_of_range(6),
    too_many_arguments(7),
    undefined_enumeration(8),
    unrecognized_service(9);

    private final int code;

    private BACnetRejectReason(int code) {
        this.code = code;
    }

    @Override
    public int getBACnetEnumValue() {
        return code;
    }

}
