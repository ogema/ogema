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
package org.ogema.core.model;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** List of annotations used to describe the meaning and behavior of elements of resource types */
public class ModelModifiers {
	/**
	 * Only relevant to simple resources. The value of a simple resource with this annotation is not stored
	 * persistently, which is usually applied to sensor values that change frequently.
	 */
	@Target(ElementType.METHOD)
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	public @interface NonPersistent {
	}
}
