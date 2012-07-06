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
 * Generic information and tools relevant to an 
 * application that do not fit in a more specialized category. 
 * Its most important interfaces are the interface {@link Application}
 * that must be implemented by any OGEMA application class and the 
 * {@link ApplicationManager} that is passed to each application at startup. The
 * application can then reach all framework services via the ApplicationManager.
 */
package org.ogema.core.application;

