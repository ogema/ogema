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

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceAccess;

/**
 * A version of the {@link ResourcePattern} that allows to pass a context (which can be any object) to the pattern 
 * immediately after calling the constructor. The {@link #context} field will be set by the 
 * framework if the pattern is created using any of the methods in {@link ResourceAccess} with a
 * <code>C context</code> parameter. <br>
 * The context is set before the {@link #accept()} method is called (when retrieving patterns from existing
 * resources), respectively before the {@link #init()} method is called (when creating resources using the pattern class).
 * Hence, the context can be used in these methods for initialization of the pattern. Note however, that the
 * {@link #accept} of a single pattern may be called multiple times by the framework. 
 */
public class ContextSensitivePattern<DemandedModel extends Resource, C> extends ResourcePattern<DemandedModel> {

	protected C context;

	/**
	 * Subclasses must provide a public constructor of the same signature
	 */
	protected ContextSensitivePattern(Resource match) {
		super(match);
	}

	/**
	 * Override this method to initialize the pattern. It will be called by the framework when 
	 * creating a new pattern using
	 * {@link ResourcePatternAccess#createResource(String, Class, Object)} or
	 * {@link ResourcePatternAccess#addDecorator(Resource, String, Class, Object)},
	 * after the {@link #context} has been set. 
	 */
	public void init() {
	};

}
