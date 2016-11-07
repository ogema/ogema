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

public class SimpleResource extends ResourceBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5083969191329048992L;
	/**
	 * 
	 */

	@SuppressWarnings("unused")
	private int size;

	public SimpleResource(String name, Object value) {
		this.name = name;
		this.value = value;
		this.size = 123;
	}

}
