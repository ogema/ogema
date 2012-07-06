/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
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

