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
package org.ogema.core.hardwaremanager;

/**
 * The {@link HardwareDescriptor} subclass for GPIO (Processor Pins). This class is intended for general purpose
 * input/output resources of the used hardware platform. HardwareManager provides descriptors that are used by
 * application to address such resources uniquely.
 */
public interface GpioHardwareDescriptor extends HardwareDescriptor {

	/**
	 * Returns the GPIO interface path like "/sys/class/gpio/gpioXX/".
	 * 
	 * @return the port name
	 */
	String getPortName();
}
