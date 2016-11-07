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
	public static String getValidResourceName(String nameIn) {
		StringBuilder sb = new StringBuilder();
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
	
	/** 
	 * Find room in resource itself or in super resource.
	 * @param device 
	 * @return
	 * 		Room in which device is located, or null if this information is not available
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
			device = device.getParent();
		}
		return null;
	}
	
	/**
	 * Check whether parent is a (higher-level) parent of child
	 * @param child
	 * @param parent
	 * @param allowReferences
	 * 		if false, only parents in the direct tree above child will be checked, otherwise references are considered as well 
	 * @return
	 */
	public static boolean isRecursiveParent(Resource child, Resource parent, boolean allowReferences) {
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

}
