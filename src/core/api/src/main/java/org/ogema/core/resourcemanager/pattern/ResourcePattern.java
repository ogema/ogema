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
package org.ogema.core.resourcemanager.pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ValueResource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.resourcemanager.AccessMode;

/**
 * Class to be extended in order to define a resource pattern that can be used
 * to create resource structures and find matches to the pattern defined in the
 * derived class.
 *
 * @param <DemandedModel>
 */
public class ResourcePattern<DemandedModel extends Resource> {

	/**
	 * The primary demand for the pattern. This is going to be set by the
	 * framework. Other resource references in the derived classes may refer to
	 * this as their anchor point..
	 */
	public final DemandedModel model;

	/**
	 * Annotation can be used to determine if the field must exist.
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Existence {

		CreateMode required() default CreateMode.MUST_EXIST;
	}

	/**
	 * Specifies the access mode to demand for the field. If the access mode is
	 * required but not granted, the pattern will not be reported as a valid
	 * pattern. If this annotation is not present, the access mode is considered
	 * as "take framework default, required=false".
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Access {

		/**
		 * Defines the kind of access that is requested from the framework.
		 * Whether this access mode is strictly required or optional is
		 * configured with the {@link #required()} parameter.
		 */
		AccessMode mode() default AccessMode.SHARED;

		/**
		 * Defines whether fulfillment of the access mode requirement in
		 * {@link #mode() } is required or not. If this is false, patterns can
		 * report as matches to a request even when the required access mode has
		 * not been granted. Otherwise, the reporting of a match is delayed
		 * until the access mode is evnetually granted.
		 */
		boolean required() default true;
	}

	public enum CreateMode {

		/**
		 * Indicates that the resource must exist and be active for
		 * pattern-demands to be completed. During pattern creation fields with
		 * this CreateMode are created.
		 */
		MUST_EXIST,
		/**
		 * Pattern demands can be completed even if this resource does not exist
		 * or is not active. In creation mode, this field is not created.
		 */
		OPTIONAL
	}

	/**
	 * Defines that the annotated resource should equal the given value. If a
	 * resource pattern with this annotation is used in a pattern demand and the
	 * annotated resource exists, the pattern is only returned as a hit if the
	 * annotated resource equals {@link #value()}. Only active resources are
	 * checked for equality (so be careful when combining this annotation with
	 * {@link CreateMode#OPTIONAL}). The annotation has no effect when a resource
	 * pattern is created or activated.
	 * <br>
	 * The annotation is intended to be used only on resources of type {@link IntegerResource}
	 * or {@link BooleanResource}. <br>
	 * Deprecated: use {@link ResourcePattern#accept()} method instead
	 */
	@Deprecated
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Equals {

		/**
		 * The mapping between integers and boolean is as follows: zero is
		 * interpreted as "false", ever non-zero value is interpreted as "true".
		 */
		int value();
	}

	/**
	 * Activate this only if the value of the requested resource is used in
	 * your {@link ResourcePattern#accept() accept()} method. If set to true,
	 * the {@link ResourcePattern#accept() accept()} method is called every time the
	 * resource value changes (as long as the pattern is complete otherwise), 
	 * and a {@link PatternListener#patternUnavailable(ResourcePattern) 
	 * patternUnavailable} is triggered if the accept() condition is no longer satisfied.<br>
	 * The annotation is intended to be used only on resources of type {@link ValueResource}.
	 * 
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ValueChangedListener {

		boolean activate() default false;
	}

	/**
	 * Constructor invoked by the framework. All patterns must implement this as
	 * a public constructor.
	 *
	 * @param match primary demand match found.
	 */
	@SuppressWarnings("unchecked")
	protected ResourcePattern(Resource match) {
		model = (DemandedModel) match;
	}

	/**
	 * Override this method to implement a custom acceptance test for the pattern. As long as the method returns
	 * false, no  {@link PatternListener#patternAvailable(ResourcePattern) patternAvailable} call 
	 * will be triggered. Use the annotation {@link ResourcePattern.ValueChangedListener ValueChangedListener} 
	 * to trigger renewed execution of the method upon value changes of a {@link ValueResource}. 
	 */
	public boolean accept() {
		return true;
	}

	@Override
	public int hashCode() {
		return model.getLocation().hashCode();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		return this.model.getLocation().equals(((ResourcePattern) obj).model.getLocation());
	}
}
