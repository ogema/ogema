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

import java.util.List;

public class ComplexResourceBean extends ResourceBean {

	private static final long serialVersionUID = -1917055267160838118L;

	// private final ResourceBean parent;
	private final List<ResourceBean> subResources;

	public ComplexResourceBean(String name, Object value, /* ResourceBean parent, */
	List<ResourceBean> subResources) {
		super();
		this.name = name;
		this.value = value;
		// this.parent = parent;
		this.subResources = subResources;
	}

	/*
	 * public ResourceBean getParent() { return parent; }
	 */

	public List<ResourceBean> getSubResources() {
		return subResources;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		// result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((subResources == null) ? 0 : subResources.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ComplexResourceBean other = (ComplexResourceBean) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		/*
		 * if (parent == null) { if (other.parent != null) return false; } else if (!parent.equals(other.parent)) return
		 * false;
		 */
		if (subResources == null) {
			if (other.subResources != null)
				return false;
		}
		else if (!subResources.equals(other.subResources))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		}
		else if (!value.equals(other.value))
			return false;
		return true;
	}

	public String toJSON() {
		// System.out.println(name);
		// System.out.println(value);

		String string = "ResourceBean [name=" + name + ", value = " + value.toString() + "]";
		return string;
	}

}
