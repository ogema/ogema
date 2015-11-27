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
 * Actor devices, i.e. switches. All actors inherit from the {@link Actor} basis model. 
 * The models
 * defined in this package can be physical devices by themselves, but often are 
 * control switches that are part of another device. For actual devices, using the
 * models in {@link org.ogema.model.devices.sensoractordevices} is recommended.<br>
 * While {@link org.ogema.model.sensors.Sensor}s only
 * have a single {@link org.ogema.model.sensors.Sensor#reading()} entry, actors have two equivalent fields:
 * the {@link Actor#stateControl()} setting is used by the application to send their
 * required actor states. The {@link Actor#stateFeedback} holds the actual actor
 * setting as reported by the device (which may ignore the request for some reason).
 */
package org.ogema.model.actors;

