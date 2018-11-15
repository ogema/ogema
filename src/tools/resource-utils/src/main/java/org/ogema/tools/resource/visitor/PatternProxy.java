/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
