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
 * The HardwareManager is responsible to manage a list of currently available hardware.
 * Additional Information is available in HardwareDescriptors that can be accessed via
 * the HardwareManager and contain additional information according to the connection type
 * of the device. An interface is defined which provides the available devices, the OGEMA framework.
 * The Hardware interface supports channel management and the entire framework,
 * including the applications. Mainly binding of devices to the Ogema gateway is done via USB ports.
 * But there are other devices which could be connected via digital IOs or native UARTs.
 */
package org.ogema.core.hardwaremanager;

