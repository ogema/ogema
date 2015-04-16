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
 * The API of the channel manager. The channel manager can optionally be used by a high-level driver for communication
 * with real devices. Numerous communication protocols have been implemented by so called low-level drivers. Installed
 * low-level drivers are automatically detected by the channel manager. The channel manager offers a single convenient
 * API to the high-level drivers. The main interface of the channel manager is {@link ChannelAccess}. Among other things
 * it can be used to configure and access channels or set update listeners that are notified of new incoming values.
 */
package org.ogema.core.channelmanager;

