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

