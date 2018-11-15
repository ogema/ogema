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
 * The API of the channel manager. The channel manager can optionally be used by a high-level driver for communication
 * with real devices. Numerous communication protocols have been implemented by so called low-level drivers. Installed
 * low-level drivers are automatically detected by the channel manager. The channel manager offers a single convenient
 * API to the high-level drivers. The main interface of the channel manager is {@link ChannelAccess}. Among other things
 * it can be used to configure and access channels or set update listeners that are notified of new incoming values.
 */
package org.ogema.core.channelmanager;

