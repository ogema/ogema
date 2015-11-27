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
package org.ogema.core.rads.creation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;

public class DefaultPatternFactory<P extends ResourcePattern<?>> implements PatternFactory<P> {

	private final Constructor<P> m_constructor;

	public DefaultPatternFactory(Class<P> type) {
		try {
            m_constructor = type.getConstructor(Resource.class);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException("Could not find default constructor on RAD of type " + type.getCanonicalName() + ". Ensure that the RAD has a public constructur RAD(Resource resource). Otherwise, it cannot be used with the OGEMA advanced access.", ex);
        }
	}

	@Override
	public P createNewPattern(Resource baseResource) {
		P result;
		try {
            result = m_constructor.newInstance(baseResource);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException("could not create a RAD object", ex);
        }
		return result;
	}
}
