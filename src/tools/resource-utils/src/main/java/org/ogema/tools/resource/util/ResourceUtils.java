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
package org.ogema.tools.resource.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceOperationException;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.locations.Room;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.tools.activation.impl.ActivationVisitor;
import org.ogema.tools.resource.visitor.PatternProxy;
import org.ogema.tools.resource.visitor.ResourceProxy;

/**
 * Generic utility methods for resources. See also {@link ValueResourceUtils}.
 */
public class ResourceUtils {

	// no need to construct this
	private ResourceUtils() {}
	
	/**
	 * Activates a resource and recursively all of its complex subresources, but not
	 * the primitive subresources (primitive resource meaning ValueResource here). Referenced resources
	 * and their subresources are not activated.<br>
	 * Use {@link Resource#activate(boolean)} or {@link Resource#deactivate(boolean)} with argument 
	 * <code>true</code> instead if you want to activate or deactivate all subresources, including primitive ones.<br>
	 * The activation is done in a transaction.
	 * @param resource
	 * 		start resource
	 * @param activate
	 * 		activate or deactivate; for most application scenarios only the activation should be relevant,
	 * 		it happens rarely that one wants to deactivate all but the value resources. 
	 */
	public static void activateComplexResources(Resource resource, boolean activate, ResourceAccess ra) {
		ActivationVisitor visitor = new ActivationVisitor(ra, activate);
		ResourceProxy proxy = new ResourceProxy(resource);
		proxy.depthFirstSearch(visitor, false);
		visitor.commit();
	}

	/**
	 * Activates all existing resource fields of a pattern, except the ValueResources.
	 * Use ResourcePatternAccess#activatePattern(ResourcePattern) and 
	 * ResourcePatternAccess#deactivatePattern(ResourcePattern) to activate or deactivate all
	 * resources of a pattern, including value resources.<br>
	 * The activation is done in a transaction.
	 * @param pattern
	 * @param activate
	 * 		activate or deactivate; for most application scenarios only the activation should be relevant,
	 * 		it happens rarely that one wants to deactivate all but the value resources. 
	 * @param ra
	 */
	public static void activateComplexResources(ResourcePattern<?> pattern, boolean activate, ResourceAccess ra) {
		ActivationVisitor visitor = new ActivationVisitor(ra, activate);
		PatternProxy proxy = new PatternProxy(pattern);
		proxy.traversePattern(visitor);
		visitor.commit();
	}

	/**
	 * Returns a valid resource name for an arbitrary String, by replacing invalid
	 * characters by an underscore, and prepending an underscore if the first character
	 * is not allowed as start character. Valid resource names are precisely the 
	 * valid Java variable names. 
	 * @param nameIn
	 * @return
	 */
	public static String getValidResourceName(final String nameIn) {
		if (nameIn.isEmpty())
			return "_";
		final StringBuilder sb = new StringBuilder();
		if (!Character.isJavaIdentifierStart(nameIn.charAt(0))) {
			sb.append("_");
		}
		for (char c : nameIn.toCharArray()) {
			if (!Character.isJavaIdentifierPart(c)) {
				sb.append("_");
			}
			else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public static boolean isValidResourceName(final String name) {
		if (name == null || name.isEmpty() || !Character.isJavaIdentifierStart(name.charAt(0)))
			return false;
		for (char c : name.toCharArray()) {
			if (!Character.isJavaIdentifierPart(c))
				return false;
		}
		return true;
	}
	
	/**
	 * @param pathName
	 * @return Returns if a Resource has a valid ResourcePath. 
	 */
	public static boolean isValidResourcePath(final String pathName) {
		if (pathName == null)
			return false;
		if (pathName.isEmpty()) {
			return false;
		}
		if (!Character.isJavaIdentifierStart(pathName.charAt(0))) {
			return false;
		}
		boolean lastWasSlash = false;
		for (char c : pathName.toCharArray()) {
			if (!Character.isJavaIdentifierPart(c)) {
				if (c == '/') {
					if (lastWasSlash)
						return false;
					lastWasSlash = true;
					continue;
				}
				return false;
			}
			lastWasSlash = false;
		}
		if (lastWasSlash)
			return false;
		return true;
	}
	
	/**
	 * Get an unused toplevel resource name.
	 * @param resAcc
	 * @param prefix
	 * 		a resource name prefix, such as "myRes"
	 * @return
	 * 		a resource name that is not in use yet, such as "myRes" or "myRes_4"
	 * @throws IllegalArgumentException if prefix is not a valid resource name
	 * @throws SecurityException if the caller does not have permission to access any of the tested resource names
	 * @throws NullPointerException if any of the arguments is null
	 */
	public static String getAvailableResourceName(final ResourceAccess resAcc, final String prefix) {
		Objects.requireNonNull(resAcc);
		Objects.requireNonNull(prefix);
		if (!isValidResourceName(prefix))
			throw new IllegalArgumentException("Not a valid resource name: " + prefix);
		String candidate = prefix;
		Resource r = resAcc.getResource(candidate);
		int cnt = 1;
		while (r != null) {
			candidate = prefix + "_" + cnt++;
			r = resAcc.getResource(candidate);
		}
		return candidate;
	}
	
	/**
	 * Get an unused subresource name.
	 * @param parent
	 * @param prefix
	 * 		a resource name prefix, such as "myRes"
	 * @return
	 * 		a resource name for a subresource that is not in use yet, such as "myRes" or "myRes_4"
	 * @throws IllegalArgumentException if prefix is not a valid resource name
	 * @throws SecurityException if the caller does not have permission to access any of the tested resource names
	 * @throws NullPointerException if any of the arguments is null
	 */
	public static String getAvailableResourceName(final Resource parent, final String prefix) {
		Objects.requireNonNull(parent);
		Objects.requireNonNull(prefix);
		if (!isValidResourceName(prefix))
			throw new IllegalArgumentException("Not a valid resource name: " + prefix);
		String candidate = prefix;
		Resource r = parent.getSubResource(candidate);
		int cnt = 1;
		while (r != null) {
			candidate = prefix + "_" + cnt++;
			r = parent.getSubResource(candidate);
		}
		return candidate;
	}
	

	/**
	 * Check if the given resource has a <code>name</code> subresource, and return its value, 
	 * if present. Otherwise, the resource path (location) is returned.
	 * @param resource
	 */
	public static String getHumanReadableName(Resource resource) {
		final String name = getNameResourceValue(resource);
		return name != null ? name : resource.getLocation();	
	}
	
	/**Like getHumanReadableName, but just return Resource.getName when no other name is specified*/
	public static String getHumanReadableShortName(Resource resource) {
		final String name = getNameResourceValue(resource);
		return name != null ? name : resource.getName();		
	}
	
	/**
	 * Get the trimmed value of the "name" subresource, if it exists, is active, is a StringResource,
	 * and has a non-empty value. Otherwise returns null.
	 * @param resource
	 * @return
	 */
	public static String getNameResourceValue(Resource resource) {
		if (resource == null)
			return null;
		return ValueResourceUtils.getStringValue(resource.getSubResource("name"));
	}
	
	/** 
	 * Find all Resources of the given ResourceType connected to the Room.
	 * @param resourceAccess,  
	 * @param resType
	 * @param room
	 * @return
	 * 		List of Resources that match the given type and room
	 */
	public static <R extends Resource> List<R> getDevicesFromRoom(ResourceAccess resourceAccess, Class<R> resType, Room room) {
		Objects.requireNonNull(resourceAccess);
		Objects.requireNonNull(room);
		Objects.requireNonNull(resType);
		List<R> res = new ArrayList<R>();
		List<? extends R> list = resourceAccess.getResources(resType);
		for (R resource : list) {
			try {
				if (room.equalsLocation(getDeviceRoom(resource))) {
					res.add(resource);
				}
			} catch (SecurityException ignore) {}
		}
		return res;
	}
	
	/** 
	 * Find room in resource itself or in super resource.
	 * @param device 
	 * @return
	 * 		Room in which device is located, or null if this information is not available
	 * @throws SecurityException
	 * 		if the caller does not have the read permission for one of the resources necessary 
	 * 		to access when checking for the room, typically one of the parent resources of res 
	 */
	public static Room getDeviceRoom(Resource device) {
		while (device != null) {
			if (device instanceof Room)
				return (Room) device;
			if (device instanceof PhysicalElement) {
				Room room = ((PhysicalElement) device).location().room();
				if (room.isActive()) 
					return room;
			}
			// possibly the caller does not have permission to access the parent resource
			device = device.getParent();
		}
		return null;
	}
	
	/**
	 * Get the first matching context resource of the specified type. See {@link #getContextResources(Resource, Class, boolean)}.
	 * @param target
	 * @param targetType
	 * @return
	 * 		A matching context resource, or null if none was found.
	 * @throws NullPointerException if targetType is null
	 */
	public static <R extends Resource> R getFirstContextResource(final Resource target, final Class<R> targetType) {
		return getFirstContextResource(target, targetType, null, null);
	}
	
	/**
	 * Get the first matching context resource of the specified type. See {@link #getContextResources(Resource, Class, boolean)}.
	 * @param target
	 * @param targetType
	 * @param typePattern
	 * 		restrict to resources whose type matches the pattern; may be null
	 * @param namePattern
	 * 		restrict to resources whose name matches the pattern; may be null
	 * @return
	 * 		A matching context resource, or null if none was found.
	 * @throws NullPointerException if targetType is null
	 */
	public static <R extends Resource> R getFirstContextResource(final Resource target, final Class<R> targetType, final Pattern typePattern, final Pattern namePattern) {
		final List<R> resources = getContextResources(target, targetType, false, typePattern, namePattern);
		return !resources.isEmpty() ? resources.get(0) : null;
	}
	
	/**
	 * Get all context resource of the specified type. The following conditions define 'context resources':
	 * <ul>
	 *    <li>subresources of this resource of the specified type
	 *    <li>subresources of some parent resource of the specified type
	 *    <li>subresources of some resource that references this resource (respectively, subresource of a parent of the referencing node), of the specified type
	 * </ul>
	 * If the inclusive parameter is false, then the first matches to one of the three conditions above are returned. If inclusive is true,
	 * then the matches for all conditions are returned.<br>
	 * 
	 * Note that this method may be quite expensive, depending on the complexity of the resource tree of the passed resource and its
	 * referencing nodes.
	 * 
	 * @param target
	 * @param targetType
	 * @param inclusive
	 * 		if true, the search will continue even when the first matches have been found, otherwise it breaks
	 * @return
	 * 		never null
	 * @throws NullPointerException if targetType is null
	 */
	public static <R extends Resource> List<R> getContextResources(final Resource target, final Class<R> targetType, final boolean inclusive) {
		return getContextResources(target, targetType, inclusive, null, null);
	}
	
	/**
	 * Get all context resource of the specified type. The following conditions define 'context resources':
	 * <ul>
	 *    <li>subresources of this resource of the specified type
	 *    <li>subresources of some parent resource of the specified type
	 *    <li>subresources of some resource that references this resource (respectively, subresource of a parent of the referencing node), of the specified type
	 * </ul>
	 * If the inclusive parameter is false, then the first matches to one of the three conditions above are returned. If inclusive is true,
	 * then the matches for all conditions are returned.<br>
	 * 
	 * Note that this method may be quite expensive, depending on the complexity of the resource tree of the passed resource and its
	 * referencing nodes.
	 * 
	 * @param target
	 * @param targetType
	 * @param inclusive
	 * 		if true, the search will continue even when the first matches have been found, otherwise it breaks
	 * @param typePattern
	 * 		restrict to resources whose type matches the pattern; may be null
	 * @param namePattern
	 * 		restrict to resources whose name matches the pattern; may be null
	 * @return
	 * 		never null
	 * @throws NullPointerException if targetType is null
	 */
	public static <R extends Resource> List<R> getContextResources(final Resource target, final Class<R> targetType, final boolean inclusive, final Pattern typePattern, final Pattern namePattern) {
		Objects.requireNonNull(targetType);
		if (target == null)
			return Collections.emptyList();
		return getContextResources(target, targetType, inclusive, true, typePattern, namePattern);
	}
	
	// FIXME if inclusive is false, we can traverse the subresources non-recursively
	@SuppressWarnings("unchecked")
	private static <R extends Resource> List<R> getContextResources(final Resource target, final Class<R> targetType, final boolean inclusive, final boolean recursive, 
			final Pattern typePattern, final Pattern namePattern) {
		final List<R> result = new ArrayList<>();
		if (inclusive) {
			final Resource top = getHighestAccessibleParent(target);
			if (typePattern == null && namePattern == null)
				result.addAll(top.getSubResources(targetType, true));
			else {
				for (R r : top.getSubResources(targetType, true)) {
					boolean matches = true;
					if (typePattern != null) {
						matches = typePattern.matcher(r.getResourceType().getName()).find();
					}
					if (matches && namePattern != null) {
						matches = namePattern.matcher(r.getName()).find();
					}
					if (matches)
						result.add(r);
				}
			}
			if (targetType.isAssignableFrom(top.getResourceType())) {
				boolean matches = true;
				if (typePattern != null)
					matches = typePattern.matcher(top.getResourceType().getName()).find();
				if (matches && namePattern != null)
					matches = namePattern.matcher(top.getName()).find();
				if (matches)
					result.add((R) top);
			}
		}
		else {
			try {
				for (Resource r= target; r != null; r = r.getParent()) {
					final List<R> contextResources = r.getSubResources(targetType, true);
					if (!contextResources.isEmpty()) {
						if (typePattern == null && namePattern == null) {
							return contextResources;
						}
						else {
							for (R c : contextResources) {
								boolean matches = true;
								if (typePattern != null) {
									matches = typePattern.matcher(c.getResourceType().getName()).find();
								}
								if (matches && namePattern != null) {
									matches = namePattern.matcher(c.getName()).find();
								}
								if (matches)
									result.add(c);
							}
							if (!result.isEmpty())
								return result;
						}
					}
					if (targetType.isAssignableFrom(r.getResourceType())) {
						boolean matches = true;
						if (typePattern != null) {
							matches = typePattern.matcher(r.getResourceType().getName()).find();
						}
						if (matches && namePattern != null) {
							matches = namePattern.matcher(r.getName()).find();
						}
						if (matches) {
							return Collections.singletonList((R) r);
						}
					}

				}
			} catch (SecurityException expected) { /* maybe we do not have the permission to access some parent */ }
		}
		if (recursive) {
			for (Resource ref : target.getReferencingNodes(true)) {
				result.addAll(getContextResources(ref, targetType, inclusive, false, typePattern, namePattern));
			}
		}
		return result;
	}

	/** 
	 * Find room in resource itself or in super resource. Use the resource location to step
	 * up to super resources
	 * @param res 
	 * @return
	 * 		Room in which device is located, or null if this information is not available
	 * @throws SecurityException
	 * 		if the caller does not have the read permission for one of the resources necessary 
	 * 		to access when checking for the room, typically one of the parent resources of res 
	 */
	public static Room getDeviceLocationRoom(Resource res) {
		return getDeviceRoom(res.getLocationResource());
	}
	
	
	/**
	 * @param r
	 * @return
	 * 		returns the toplevel-resource above r, if all resources in the tree between r and the
	 * 		top-level resource are accessible, or the highest resource in the tree up from r that 
	 * 		the caller can access (resource read permission).
	 */
	public static Resource getHighestAccessibleParent(final Resource r) {
		if (r == null)
			return null;
		Resource parent = r;
		while (true) {
			try {
				final Resource p = parent.getParent();
				if (p == null)
					break;
				parent = p;
			} catch (SecurityException expected) {
				break;
			}
		}
		return parent;
	}
	
	/** 
	 * Return parent going up a defined number of levels. Returns null if it is not
	 * possible to go up this number of levels for the resource. For levelUp == 1 the
	 * method equals r.getParent().
	 * @param resource
	 * @param levelUp
	 * @return parent resource at levelUp levels above resource, or null if levelUp is too high.
	 * @throws SecurityException
	 * 		if the caller does not have the read permission for one of the parent resources 
	 */
	public static Resource getParentLevelsAbove(Resource resource, int levelUp) {
		for (int i=0; i<levelUp; i++) {
			resource = resource.getParent();
			if (resource == null) 
				return null;
		}
		return resource;
	}

	/**
	 * Get top-level resource.
	 * @param resource
	 *      not null
	 * @return top-level parent of resource, or resource itself if it is already top-level
	 * @throws SecurityException
	 * 		if the caller does not have the read permission for one of the parent resources 
	 */
	public static Resource getToplevelResource(Resource resource) {
		Objects.requireNonNull(resource);
		while (!resource.isTopLevel()) {
			resource = resource.getParent();
		}
		return resource;
	}
	
	/**
	 * Check whether parent is a (higher-level) parent of child
	 * @param child
	 *      not null
	 * @param parent
	 *      not null
	 * @param allowReferences
	 * 		if false, only parents in the direct tree above child will be checked, otherwise references are considered as well 
	 * @return
	 * @throws SecurityException
	 * 		if the caller does not have the read permission for one of the parent resources of child 
	 */
	public static boolean isRecursiveParent(Resource child, Resource parent, boolean allowReferences) {
		Objects.requireNonNull(child);
		Objects.requireNonNull(parent);
		child = child.getParent();
		boolean found;
		while (child != null) {
			if (allowReferences)
				found = parent.equalsLocation(child);
			else
				found = parent.equals(child);
			if (found)
				return true;
			child = child.getParent();
		}
		return false;
	}

	/**
	 * Returns the first parent of the specified type above the input resource, or null, if none is found.
	 * If the input resource itself is of the specified type, it is returned.
	 * Note: this does not localize the input resource, it parses the parents by path.
	 * @param resource
	 * @param type
	 *      not null
	 * @return
	 * @throws SecurityException
	 * 		if the caller does not have the read permission for one of the parent resources 
	 * 		that is accessed in the method 
	 */
	@SuppressWarnings("unchecked")
	public static <R extends Resource> R getFirstParentOfType(Resource resource, Class<? extends R> type) {
		Objects.requireNonNull(type);
		Resource parent = resource;
		while (parent != null && !type.isAssignableFrom(parent.getResourceType())) {
			parent = parent.getParent();
		}
		return (R) parent;
	}

	/**
	 * Copy the source resource to target, including all subresources, with default settings 
	 * (schedule values are copied, the active state is copied, logging is not activated for the
	 * new resources)
	 * @param source
	 * @param target
	 * 		may be virtual
	 * @param ra
	 * @throws ResourceOperationException if something goes wrong...
	 * @throws SecurityException if the caller does not have the read permission for any
	 * of the input resources, or the write permission for any of the target resources
	 * @throws NullPointerException if any of source, target or ra is null
	 */
	public static void copy(Resource source, Resource target, ResourceAccess ra) {
		new CopyHelper(null, source, target, ra).commit();
	}
	
	/**
	 * Copy the source resource to target, including all subresources.
	 * @param source
	 * @param target
	 * 		may be virtual
	 * @param ra
	 * @param config
	 * @throws ResourceOperationException if something goes wrong...
	 * @throws SecurityException if the caller does not have the read permission for any
	 * of the input resources, or the write permission for any of the target resources
	 * @throws NullPointerException if any of source, target or ra is null
	 */
	public static void copy(Resource source, Resource target, ResourceAccess ra, ResourceCopyConfiguration config) {
		new CopyHelper(config, source, target, ra).commit();
	}
	
	/**
	 * Get a resource list of the specified element type at the given path, creating it if does not exist yet.
	 * @param appMan
	 * @param path
	 * @param elementType
	 * @return
	 * @throws ClassCastException if a resource at the given path exists but is not of type ResourceList, or the element type does not match
	 * @throws SecurityException if the resource exists and the caller does not have the read permission, or the 
	 * 		resource does not exist and the caller does not have the write permission
	 * @throws NullPointerException if any of the passed arguments is null
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Resource> ResourceList<T> getOrCreateResourceList(ApplicationManager appMan, String path, Class<T> elementType) {
		Objects.requireNonNull(appMan);
		Objects.requireNonNull(path);
		Objects.requireNonNull(elementType);
		ResourceList<T> list = appMan.getResourceAccess().getResource(path);
		if (list == null)
			list = appMan.getResourceManagement().createResource(path, ResourceList.class);
		Class<? extends Resource> type = list.getElementType();
		if (type == null)
			list.setElementType(elementType);
		else if (type != elementType)
			throw new ClassCastException("Resource list exists with different element type " + type + " instead of " + elementType);
		return list;
	}
	
}
