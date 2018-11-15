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
package org.ogema.core.rads.tools;

import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ogema.core.model.Resource;
import org.ogema.core.rads.creation.PatternFactory;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;

/**
 * Combined analysis and factory class for RADs. Analyzes a RAD-class via
 * reflections and also allows to instantiate raw objects.
 *
 * @author Timo Fischer, Fraunhofer IWES
 * @param <T>
 * @param <P>
 */
public class RadFactory<T extends Resource, P extends ResourcePattern<T>> {

	final Class<P> m_type;
	//	final Constructor<P> m_constructor;
	final Class<T> m_demandedModel;
	private final List<ResourceFieldInfo> m_requiredFields; // = new ArrayList<>();
	private final PatternFactory<P> m_factory;

	public RadFactory(Class<P> type, AccessPriority priority, PatternFactory<P> factory) {
		m_type = type;
		m_factory = factory;
		//        try {
		//            m_constructor = type.getConstructor(Resource.class);
		//        } catch (NoSuchMethodException | SecurityException ex) {
		//            throw new RuntimeException("Could not find default constructor on RAD of type " + type.getCanonicalName() + ". Ensure that the RAD has a public constructur RAD(Resource resource). Otherwise, it cannot be used with the OGEMA advanced access.", ex);
		//        }
//		m_demandedModel = getDemandedModel(type);
		m_demandedModel = getGenericClassParameter(type, ResourcePattern.class);
		m_requiredFields = getResourceInfoRecursively(type, priority);
	}

	public Class<? extends ResourcePattern<?>> getRadType() {
		return m_type;
	}

	public Class<T> getDemandedModel() {
		return m_demandedModel;
	}

	public List<ResourceFieldInfo> getResourceFieldInfos() {
		return m_requiredFields;
	}

	public P create(T demandMatch) {
		final P result;
		//        try {
		//            result = m_constructor.newInstance(demandMatch);
		//        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
		//            throw new RuntimeException("could not create a RAD object", ex);
		//        }
		try {
			result = m_factory.createNewPattern(demandMatch);
		} catch (Exception ex) {
			throw new RuntimeException("could not create a RAD object for " + demandMatch, ex);
		} 
		return result;
	}

	// https://stackoverflow.com/questions/18707582/get-actual-type-of-generic-type-argument-on-abstract-superclass
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final <T> Class<T> getGenericClassParameter(final Class<?> parameterizedSubClass, final Class<?> genericSuperClass) {
	    // a mapping from type variables to actual values (classes)
	    final Map<TypeVariable<?>, Class<?>> mapping = new HashMap<>();

	    Class<?> klass = parameterizedSubClass;
	    while (klass != null) {
	        final Type type = klass.getGenericSuperclass();
	        if (type instanceof ParameterizedType) {
	            final ParameterizedType parType = (ParameterizedType) type;
	            final Type rawType = parType.getRawType();
	            if (rawType == genericSuperClass) {
	                // found
	                final Type t = parType.getActualTypeArguments()[0];
	                if (t instanceof Class<?>) {
	                    return (Class<T>) t;
	                } else {
	                    return (Class<T>) mapping.get((TypeVariable<?>)t);
	                }
	            }
	            // resolve
	            final Type[] vars = ((GenericDeclaration)(parType.getRawType())).getTypeParameters();
	            final Type[] args = parType.getActualTypeArguments();
	            for (int i = 0; i < vars.length; i++) {
	                if (args[i] instanceof Class<?>) {
	                    mapping.put((TypeVariable)vars[i], (Class<?>)args[i]);
	                } else {
	                    mapping.put((TypeVariable)vars[i], mapping.get((TypeVariable<?>)(args[i])));
	                }
	            }
	            klass = (Class<?>) rawType;
	        } else {
	            klass = klass.getSuperclass();
	        }
	    }
	    throw new IllegalArgumentException("no generic supertype for " + parameterizedSubClass + " of type " + genericSuperClass);
	}

	/*
	 * Gets the resource of rad that corresponds to the field required. Returns
	 * null if the field is uninitialized or if the field does not correspond to
	 * a resource.
	 */
	public static Resource getResource(final Field field, ResourcePattern<?> rad) {
        final Resource result;
        final boolean access = field.isAccessible();
        if (!access) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {

                @Override
                public Void run() {
                    field.setAccessible(true);
                    return null;
                }
            });
        }
        try {
            Class<?> type = field.getType();
            if (!Resource.class.isAssignableFrom(type)) return null;
            result = (Resource) field.get(rad);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException("Could not access field " + field.toGenericString() + " on RAD with primary match at ???");
        } finally {
            if (!access) {
                setAccessiblePrivileged(field);
            }
        }
        return result;
    }

	private static void setAccessiblePrivileged(final Field field) {
		AccessController.doPrivileged(new PrivilegedAction<Void>() {

			@Override
			public Void run() {
				field.setAccessible(false);
				return null;
			}
		});
	}

	/*
	 * Re-sets the field in a RAD to a new resource.
	 */
	public static void setResourceField(Field field, ResourcePattern<?> rad, Resource resource) {
        final boolean access = field.isAccessible();
        if (!access) field.setAccessible(true);
        try {
            field.set(rad, resource);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException("Could not set a resource field.", ex);
        } finally {
            if (!access) field.setAccessible(false);
        }
    }

	/*
	 * Gets the initialized resources of the created RAD that correspond to the
	 * list of fields passed.
	 */
	public static List<Resource> getInitializedResources(final List<Field> fields, final ResourcePattern<?> rad) {
        final List<Resource> result = new ArrayList<>(fields.size());
        for (Field field : fields) {
            final Resource resource = getResource(field, rad);
            if (resource != null) result.add(resource);
        }
        return result;
    }

	/*
	 * Gets the initialized resources of the created RAD that correspond to the
	 * list of fields passed.
	 */
	public static List<Resource> getInitializedResourcesFromParameters(final List<ResourceFieldInfo> parameters, final ResourcePattern<?> rad) {
        final List<Field> fields = new ArrayList<>(parameters.size());
        for (ResourceFieldInfo parameter : parameters)
            fields.add(parameter.getField());
        return getInitializedResources(fields, rad);
    }

	/*
	 * Same as getResourceInfo, but also parses ancestor classes.
	 */
	@SuppressWarnings("unchecked")
	public static List<ResourceFieldInfo> getResourceInfoRecursively(final Class<? extends ResourcePattern<?>> radClass, final AccessPriority writePriority) {
        Class<? extends ResourcePattern<?>> clazz = radClass;
        final List<ResourceFieldInfo> result = new ArrayList<>();
        while (!clazz.equals(ResourcePattern.class)) {
            result.addAll(getResourceInfo(clazz, writePriority));
            clazz = (Class<? extends ResourcePattern<?>>) clazz.getSuperclass();
        }
        return result;
    }

	/*
	 * Gets the list of all resource fields with their annotated parameters.
	 * Write priority must be explicitly given, since it is not encoded in the
	 * annotations.
	 */
	private static List<ResourceFieldInfo> getResourceInfo(final Class<? extends ResourcePattern<?>> radClass, final AccessPriority writePriority) {
        AccessControlContext ctx = AccessController.getContext();
        List<ResourceFieldInfo> result = AccessController.doPrivileged(new PrivilegedAction<List<ResourceFieldInfo>>() {
            @Override
            public List<ResourceFieldInfo> run() {
                final List<ResourceFieldInfo> result = new ArrayList<>();
                for (Field field : radClass.getDeclaredFields()) {
                    if (Resource.class.isAssignableFrom(field.getType())) {
                        result.add(new ResourceFieldInfo(field, writePriority));
                    }
                }
                return result;
            }

        });
        return result;
    }
}
