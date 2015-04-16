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
package org.ogema.core.application;

/**
 * Defines a listener that is informed whenever an exception is reported to the application. This includes framework-internal exceptions.
 */
public interface ExceptionListener {
	/**
	 * Notification to application on a framework exception.
	 * 
	 * @param exception
	 *            exception that occurred in the framework and that is caught there
	 */
	void exceptionOccured(Throwable exception);
}
