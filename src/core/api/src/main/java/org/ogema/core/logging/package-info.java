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
 * Definition of the OGEMA text logging capabilities, which are employed using an
 * {@link OgemaLogger}. The logging is defined as an extension over the well-known
 * logging framework slf4j. It defines an additional {@link LogOutput}, the
 * cache. It is a cyclic memory constructed for debug messages that only become
 * relevant in case of an error. If an error occurs, the cache can be written to
 * disk for further analysis. Otherwise, entries in the cache will be overwritten
 * by newer entries, later.
 */
package org.ogema.core.logging;

