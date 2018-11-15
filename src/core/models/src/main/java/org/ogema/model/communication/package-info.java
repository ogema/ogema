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
 * Information on how devices interact with the gateway. The base class in this
 * package is the {@link CommunicationInformation} that is intended to be added
 * as a decorator by drivers that want to persistently store such information 
 * about a device they control. The exact location where the decorator is attached
 * is up to the device driver, but attaching it to the most top-level resource
 * controlled by the driver is advised.
 */
package org.ogema.model.communication;

