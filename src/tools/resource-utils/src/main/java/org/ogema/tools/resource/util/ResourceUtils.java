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
package org.ogema.tools.resource.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceAccess;
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
	 * Check if the given resource has a <code>name</code> subresource, and return its value, 
	 * if present. Otherwise, the resource path is returned.
	 * @param resource
	 */
	public static String getHumanReadableName(Resource resource) {
		Resource name = resource.getSubResource("name");
		if ((name != null) && (name instanceof StringResource)) {
			String val = ((StringResource) (name)).getValue();
			if (name.isActive() && (!val.trim().isEmpty()))
				return val;
		}
		return resource.getLocation();
	}
	
	/**Like getHumanReadableName, but just return Resource.getName when no other name is specified*/
	public static String getHumanReadableShortName(Resource resource) {
		Resource name = resource.getSubResource("name");
		if ((name != null) && (name instanceof StringResource)) {
			String val = ((StringResource) (name)).getValue();
			if (name.isActive() && (!val.trim().isEmpty()))
				return val;
		}
		return resource.getName();		
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
	
}
