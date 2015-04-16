/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
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
 * Information on how devices interact with the gateway. The base class in this
 * package is the {@link CommunicationInformation} that is intended to be added
 * as a decorator by drivers that want to persistently store such information 
 * about a device they control. The exact location where the decorator is attached
 * is up to the device driver, but attaching it to the most top-level resource
 * controlled by the driver is advised.
 */
package org.ogema.model.communication;

