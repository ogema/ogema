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
public enum BACnetDeviceStatus implements BACnetEnumeration {
    operational(0),
    operational_read_only(1),
    download_required(2),
    download_in_progress(3),
    non_operational(4),
    backup_in_progress(5);

    private final int code;

    private BACnetDeviceStatus(int code) {
        this.code = code;
    }

    @Override
    public int getBACnetEnumValue() {
        return code;
    }

    public static BACnetDeviceStatus forEnumValue(int val) {
        for (BACnetDeviceStatus o : values()) {
            if (val == o.getBACnetEnumValue()) {
                return o;
            }
        }
        throw new IllegalArgumentException("unknown enum value: " + val);
    }

}
