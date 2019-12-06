package org.ogema.pattern.debugger;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ogema.core.administration.PatternCondition;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ValueResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.Access;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.CreateMode;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.Existence;
import org.ogema.tools.resource.util.ValueResourceUtils;
import org.slf4j.LoggerFactory;

class Condition implements PatternCondition {
	
	static List<PatternCondition> getResourceInfoRecursively(final Class<? extends ResourcePattern<?>> radClass, final ResourcePattern<?> pattern) {
		if (pattern == null)
			return Collections.emptyList();
        Class<? extends ResourcePattern<?>> clazz = radClass;
        final List<PatternCondition> result = new ArrayList<>();
        while (clazz != null && !clazz.equals(ResourcePattern.class)) {
            getResourceInfo(clazz, pattern, result);
            clazz = (Class<? extends ResourcePattern<?>>) clazz.getSuperclass();
        }
        return result;
    }
	
	private static void getResourceInfo(final Class<? extends ResourcePattern<?>> radClass, 
			final ResourcePattern<?> pattern, final List<PatternCondition> result) {
        for (Field field : radClass.getDeclaredFields()) {
            if (Resource.class.isAssignableFrom(field.getType())) {
                try {
					result.add(new Condition(field, pattern));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					LoggerFactory.getLogger(Condition.class).error("?",e);
				}
            }
        }
    }

	private final Resource value;
	private final Field field;
	
	public Condition(Field field, ResourcePattern<?> pattern) throws IllegalArgumentException, IllegalAccessException {
		this.field = field;
		field.setAccessible(true);
		this.value = (Resource) field.get(pattern);
	}
	
	
	@Override
	public String getFieldName() {
		return field.getName();
	}

	@Override
	public boolean isSatisfied() {
		return isOptional() || (value != null && value.isActive());
	}

	@Override
	public boolean isOptional() {
		final Existence e = field.getAnnotation(Existence.class);
		return e != null && e.required() == CreateMode.OPTIONAL;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class<? extends Resource> getResourceType() {
		return (Class) field.getType();
	}

	@Override
	public boolean exists() {
		return value != null && value.exists();
	}

	@Override
	public boolean isActive() {
		return value != null && value.isActive();
	}

	@Override
	public boolean isReference() {
		return value != null && value.isReference(true);
	}

	@Override
	public String getPath() {
		return value == null ? null : value.getPath();
	}

	@Override
	public String getLocation() {
		return value == null ? null : value.getLocation();
	}

	@Override
	public AccessMode getAccessMode() {
		final Access a = field.getAnnotation(Access.class);
		return a == null ? AccessMode.SHARED : a.mode();
	}

	@Override
	public Object getValue() {
		return value instanceof ValueResource ? ValueResourceUtils.getValue((ValueResource) value) : null;
	}

}
