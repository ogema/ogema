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
 * Energy storage devices and tightly related devices. All energy storage devices inherit from the common 
 * base class {@link EnergyStorage}. Storages can have control settings of themselves,
 * which is the case for simple storages with only one in- and out-connection or
 * for storages with internal controlling that is not exposed to the framework.
 * More complex storages may not have a direct control. Their behavior is then
 * set by the actors on the {@link Connection}s leading to the storage.
 */
package org.ogema.model.devices.storage;

