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
package org.ogema.patternaccess;

import java.util.List;

import org.ogema.core.administration.RegisteredPatternListener;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;

/**
 * Provides additional methods related to the pattern access, which are only 
 * accessible via the {@see AdministrationManager}, instead of the {@link ResourcePatternAccess}
 */
public interface AdministrationPatternAccess extends ResourcePatternAccess {
	
	/**
	 * Get the pattern listeners registered by this application. 
	 * @return
	 */
	List<RegisteredPatternListener> getRegisteredPatternListeners();
	
	/**
	 * Get the RegisteredPatternListener object belonging to a listener, which
	 * contains additional information about incomplete matches.<br>
	 * Note that the listener must have been registered for a pattern demand prior to
	 * calling this 
	 * (see {@link ResourcePatternAccess#addPatternDemand(Class, PatternListener, org.ogema.core.resourcemanager.AccessPriority)},
	 * and related methods), otherwise null is returned.
	 * 
	 * @param listener
	 * @param pattern
	 * 		FIXME can the type be derived from the listener? 
	 * @return
	 * 		The pattern info object, or null if the listener registration has not been found.
	 */
	<P extends ResourcePattern<?>> RegisteredPatternListener getListenerInformation(PatternListener<P> listener, Class<P> pattern);
	
	void close();
	
}
