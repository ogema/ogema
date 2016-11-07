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
package org.ogema.tools.resource.visitor;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;

/**
 * A utility class allowing to traverse the fields of a {@link ResourcePattern}. 
 */
public class PatternProxy {

	private final ResourcePattern<?> pattern;

	public PatternProxy(ResourcePattern<?> pattern) {
		this.pattern = pattern;
	}

	public void traversePattern(final ResourceVisitor visitor) {	
		AccessController.doPrivileged(new PrivilegedAction<Void>() {
			@Override
			public Void run() {
//				Field[] fields = pattern.getClass().getDeclaredFields();
				List<Field> fields = new ArrayList<Field>();
				addFieldsRecursively(pattern.getClass(), fields);
				try {
					fields.add(ResourcePattern.class.getField("model"));
				} catch (NoSuchFieldException | SecurityException e1) {
					e1.printStackTrace();
				}
				for (Field field: fields) {
					if (!Resource.class.isAssignableFrom(field.getType())) continue;
					boolean accessible = field.isAccessible();
					if (!accessible)
						field.setAccessible(true);
					Resource resource;
					try {
						resource = (Resource) field.get(pattern);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new RuntimeException("Unexpected access failure");
					}
					if (!accessible)
						field.setAccessible(false);
					if (resource == null || !resource.exists()) continue;
					ResourceProxy proxy = new ResourceProxy(resource);
					proxy.accept(visitor);			
				}
				return null;
			}
		});
	}

	@SuppressWarnings( { "unchecked", "rawtypes" })
	private void addFieldsRecursively(Class<? extends ResourcePattern> clazz, List<Field> fields) {
		while (!clazz.equals(ResourcePattern.class)) {
			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
			clazz = (Class<? extends ResourcePattern<?>>) clazz.getSuperclass();
		}
	}

}
