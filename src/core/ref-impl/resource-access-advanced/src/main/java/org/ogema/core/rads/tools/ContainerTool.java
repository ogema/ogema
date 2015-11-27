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
package org.ogema.core.rads.tools;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.ogema.core.resourcemanager.pattern.ContextSensitivePattern;

public class ContainerTool {

	public static <C> void setContainer(final ContextSensitivePattern<?, C> pattern, final C container) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, RuntimeException {
		final Field field = ContextSensitivePattern.class.getDeclaredField("context");
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

            @Override
            public Void run() {  
                synchronized(field) {  // synchronization?
	            	field.setAccessible(true);
	                try {
						field.set(pattern, container);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new RuntimeException("Container set method failed. " + e);
					}
	                field.setAccessible(false);
                }
                return null;
            }
        });
		
	}
}
