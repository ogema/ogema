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
/**
 * Metering devices and information. All meter models derive from the {@link GenericMeter},
 * which defines a common structure but no metering registers by itself (they depend on
 * the actual type of meter). All meters refer to metering a connection. If a physical
 * meter delivers real-time information about the measured quantities (e.g. power), the
 * meter driver is advised to put this information into the connection's respective
 * sensor readings in addition to the meter's register's resource.
 */
package org.ogema.model.metering;

