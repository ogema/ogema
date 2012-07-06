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
package org.ogema.core.resourcemanager.pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.AccessMode;

/**
 * Class to be extended in order to define a resource pattern that can be used to
 * create resource structures and find matches to the pattern defined in the derived
 * class.
 * @param <DemandedModel>
 */
public class ResourcePattern<DemandedModel extends Resource> {
	/**
	 * The primary demand for the pattern. This is going to be set by the framework.
	 * Other resource references in the derived classes may refer to this as their
	 * anchor point..
	 */
	public DemandedModel model;

	/**
	 * Annotation can be used to determine if the field must exist.
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Existence {
		CreateMode required() default CreateMode.MUST_EXIST;
	}

	/**
	 * Specifies the access mode to demand for the field. If the access mode
	 * is required but not granted, the pattern will not be reported as a valid
	 * pattern. If this annotation is not present, the access mode is considered
	 * as "take framework default, required=false".
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Access {
		AccessMode mode() default AccessMode.SHARED;

		//      AccessPriority priority() default AccessPriority.PRIO_LOWEST; // access prio for writes is defined when issuing the demands
		boolean required() default true;
	}

	public enum CreateMode {
		/**
		 * Indicates that the resource must exist and be active for pattern-demands to be completed. During pattern creation
		 * fields with this CreateMode are created.
		 */
		MUST_EXIST,
		/**
		 * Pattern demands can be completed even if this resource does not exist or is not active. In creation
		 * mode, this field is not created.
		 */
		OPTIONAL
	}

	/**
	 * Constructor invoked by the framework
	 * @param match primary demand match found.
	 */
	@SuppressWarnings("unchecked")
	protected ResourcePattern(Resource match) {
		model = (DemandedModel) match;
	}
}
