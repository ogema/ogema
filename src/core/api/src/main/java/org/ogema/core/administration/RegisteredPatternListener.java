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
package org.ogema.core.administration;

import java.util.List;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;

//* TODO Ensure that an application cannot access any resources 
//* it doesn't hold permissions for, via the methods provided here!
/**
 * Representation of a {@link PatternListener} registered by an application.
 */
public interface RegisteredPatternListener {
	
	/** 
	 * Gets the administrator access to the application that registered the demand.
	 * @return admin access of registering application
	 */
	AdminApplication getApplication();

	/**
	 * Gets the pattern type demanded.
	 * @return pattern type of the demand
	 */
	Class<? extends ResourcePattern<?>> getDemandedPatternType();
	
	/**
	 * Gets the resource type of the pattern's primary demanded model
	 * @return
	 */
	Class<? extends Resource> getPatternDemandedModelType();

	/**
	 * Gets the listener that is informed about new patterns of the demanded type.
	 * @return the listener for this demand
	 */
	PatternListener<?> getListener();
	
	/**
	 * Get the list of incomplete patterns, i.e. those whose primary demand is satisfied, but 
	 * not all of the other conditions for a 
	 * {@link PatternListener#patternAvailable(ResourcePattern) patternAvailable} callback.<br>
	 * This is mainly intended for debugging purposes.
	 * @return
	 */
	List<? extends ResourcePattern<?>> getIncompletePatterns();
	
	/**
	 * Get the list of complete patterns, i.e. those for which a 
	 * {@link PatternListener#patternAvailable(ResourcePattern) patternAvailable} callback
	 * has been issued already.<br>
	 * This is mainly intended for debugging purposes.
	 * @return
	 */
	List<? extends ResourcePattern<?>> getCompletedPatterns();

//	 TODO implement security checks... need read access to the resources corresponding to the conditions
	/**
	 * Returns a list of pattern conditions (each condition corresponds to a resource field of the pattern).
	 * Note that conditions corresponding to resources for which the calling application does not have read 
	 * access are filtered out. 
	 * @return
	 * @throws IllegalArgumentException
	 * 		If the pattern type does not match the requested type of the listener.
	 */
	List<PatternCondition> getConditions(ResourcePattern<?> pattern) throws IllegalArgumentException;

}
