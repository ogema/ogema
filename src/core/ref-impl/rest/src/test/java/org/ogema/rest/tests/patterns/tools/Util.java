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
package org.ogema.rest.tests.patterns.tools;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.Access;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.CreateMode;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.Existence;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.rest.patternmimic.FakePattern;
import org.ogema.rest.patternmimic.ResourceProxy;

public class Util {
	
	private static final String TEST_BASE_RES_PATH = "testresources";
	private static int counter = 0;
	
	/*
	 * Note: this is just a convenience method for the generation of FakePatterns from
	 * ResourcePatterns, it does not mean that the two concepts are equivalent. 
	 * In fact, the mapping is neither injective nor surjective
	 * @param pattern
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static FakePattern convert(Class<? extends ResourcePattern> pattern, ApplicationManager am) {
		Class<? extends Resource> demandedModelType = getDemandedModel((Class) pattern);
		FakePattern fp = new FakePattern(demandedModelType);
		ResourcePattern<?> instance = am.getResourcePatternAccess().createResource(TEST_BASE_RES_PATH + counter++, pattern); // we create an instance, in order to be able to determine the relative paths...
		while (!pattern.equals(ResourcePattern.class)) {
			addFields(fp, pattern.getDeclaredFields(), instance);
			pattern = (Class<? extends ResourcePattern>) pattern.getSuperclass();
		}
		instance.model.delete();
		return fp;
	}
	
	public static final TestPattern createMatchingPattern(ResourcePatternAccess rpa, Resource parent, String name) {
		TestPattern pattern;
		if (parent == null)
			pattern = rpa.createResource(name, TestPattern.class);
		else
			pattern = rpa.addDecorator(parent, name, TestPattern.class);
		pattern.reading.setValue(290);
		rpa.activatePattern(pattern);
		return pattern;
	}
	
    @SuppressWarnings("deprecation")
	private static void addFields(FakePattern fp, Field[] fields, ResourcePattern<?> instance) {
		for (Field f: fields) {
			if (!Resource.class.isAssignableFrom(f.getType()))
				continue;
			f.setAccessible(true);
			ResourceProxy proxy = new ResourceProxy();
			final Existence existence = f.getAnnotation(Existence.class);
			CreateMode cm = (existence == null ? CreateMode.MUST_EXIST : existence.required());
			proxy.setOptional(cm == CreateMode.OPTIONAL);
			final Access access = f.getAnnotation(Access.class);
			AccessMode am = (access == null ? AccessMode.SHARED : access.mode());
			proxy.setAccessMode(am.name());
			proxy.setType(f.getType().getName());
			org.ogema.core.resourcemanager.pattern.ResourcePattern.Equals equals =
                    f.getAnnotation(org.ogema.core.resourcemanager.pattern.ResourcePattern.Equals.class);
			if (equals != null) {
				proxy.setValue("" + equals.value());
			}
			try {
				Resource base = instance.model;
				Resource res = (Resource) f.get(instance);
				String relativePath = getRelativePath(base, res);
				proxy.setRelativePath(relativePath);
				proxy.setName(res.getName());
			} catch (IllegalArgumentException | IllegalAccessException | IllegalStateException e) {
				e.printStackTrace();
			}
			fp.resourceFields.add(proxy);
		}
	}
	
	private static String getRelativePath(Resource base, Resource sub) {
		String path = sub.getPath();
		String basePath = base.getPath();
		if (path.startsWith(basePath)) 
			return path.substring(basePath.length()+1);
		else 
			throw new IllegalStateException("Subresource path does not start with resource path: " + path + "; " + basePath);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <M extends Resource> Class<M> getDemandedModel(Class<? extends ResourcePattern<M>> radClass) {
		Type genericSupertype = radClass.getGenericSuperclass();
		while (!(genericSupertype instanceof ParameterizedType)) {
			genericSupertype = ((Class) genericSupertype).getGenericSuperclass();
		}
		final ParameterizedType radtype = (ParameterizedType) genericSupertype;
		return (Class<M>) radtype.getActualTypeArguments()[0];
	}
	
}
