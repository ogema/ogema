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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import org.ogema.core.model.Resource;
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
	final Constructor<P> m_constructor;
	final Class<T> m_demandedModel;
	private final List<ResourceFieldInfo> m_requiredFields; // = new ArrayList<>();

	public RadFactory(Class<P> type, AccessPriority priority) {
        m_type = type;
        try {
            m_constructor = type.getConstructor(Resource.class);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException("Could not find default constructor on RAD of type " + type.getCanonicalName() + ". Ensure that the RAD has a public constructur RAD(Resource resource). Otherwise, it cannot be used with the OGEMA advanced access.", ex);
        }
        m_demandedModel = getDemandedModel(type);
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
        try {
            result = m_constructor.newInstance(demandMatch);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException("could not create a RAD object", ex);
        }
        return result;
    }

	@SuppressWarnings("unchecked")
	public static <M extends Resource> Class<M> getDemandedModel(Class<? extends ResourcePattern<M>> radClass) {
		Type genericSupertype = radClass.getGenericSuperclass();
		while (!(genericSupertype instanceof ParameterizedType)) {
			genericSupertype = ((Class) genericSupertype).getGenericSuperclass();
		}
		final ParameterizedType radtype = (ParameterizedType) genericSupertype;
		return (Class<M>) radtype.getActualTypeArguments()[0];
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
