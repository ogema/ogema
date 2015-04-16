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
package org.ogema.app.resource.management.gui.util;

import java.io.Serializable;

public abstract class ResourceBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5636380768675879210L;
	protected String name;
	protected int id;
	protected Object value;

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

}
