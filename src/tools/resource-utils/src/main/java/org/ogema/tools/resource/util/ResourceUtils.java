package org.ogema.tools.resource.util;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.locations.Room;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.tools.activation.impl.TransactionVisitor;
import org.ogema.tools.resource.visitor.PatternProxy;
import org.ogema.tools.resource.visitor.ResourceProxy;

/**
 * Generic utility methods for resources. See also {@link ValueResourceUtils}.
 */
public class ResourceUtils {

	/**
	 * Activates a resource and recursively all of its complex subresources, but not
	 * the primitive subresources (primitive resources meaning {@see ValueResource}s here). References resources
	 * and their subresources are not activated.<br>
	 * Use {@link Resource#activate(boolean)} or {@link Resource#deactivate(boolean)} with argument 
	 * <code>true</code> instead if you want to activate or deactivate all subresources, including primitive ones.
	 * @param resource
	 * 		start resource
	 * @param activate
	 * 		activate or deactivate; for most application scenarios only the activation should be relevant,
	 * 		it happens rarely that one wants to deactivate all but the value resources. 
	 */
	public static void activateComplexResources(Resource resource, boolean activate, ResourceAccess ra) {
		TransactionVisitor visitor = new TransactionVisitor(ra);
		ResourceProxy proxy = new ResourceProxy(resource);
		proxy.depthFirstSearch(visitor, false);
		visitor.activate(activate);
	}

	/**
	 * Activates all existing resource fields of a pattern, except the {@see ValueResource}s.
	 * Use {@see ResourcePatternAccess#activatePattern(ResourcePattern)} and 
	 * {@see ResourcePatternAccess#deactivatePattern(ResourcePattern)} to activate or deactivate all
	 * resources of a pattern, including value resources.
	 * @param pattern
	 * @param activate
	 * 		activate or deactivate; for most application scenarios only the activation should be relevant,
	 * 		it happens rarely that one wants to deactivate all but the value resources. 
	 * @param ra
	 */
	public static void activateComplexResources(ResourcePattern<?> pattern, boolean activate, ResourceAccess ra) {
		TransactionVisitor visitor = new TransactionVisitor(ra);
		PatternProxy proxy = new PatternProxy(pattern);
		proxy.traversePattern(visitor);
		visitor.activate(activate);
	}

	/**
	 * Returns a valid resource name for an arbitrary String, by replacing invalid
	 * characters by an underscore, and prepending an underscore if the first character
	 * is not allowed as start character. Valid resource name are precisely the 
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
		if (name != null && name.isActive() && name instanceof StringResource)
			return ((StringResource) (name)).getValue();
		else
			return resource.getPath();
	}

	/** 
	 * Find room in resource itself or in super resource. 
	 * @return
	 * 		Room in which device is located, or null if this information is not available
	 */
	public static Room getDeviceRoom(PhysicalElement device) {
		while (device != null) {
			if (!(device instanceof PhysicalElement)) {
				device = device.getParent();
				continue;
			}
			Room room = ((PhysicalElement) device).location().room();
			if (room.isActive()) {
				return room;
			}
			device = device.getParent();
		}
		return null;
	}

}
