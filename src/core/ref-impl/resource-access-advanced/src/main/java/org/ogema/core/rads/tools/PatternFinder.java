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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ogema.core.model.Resource;
import org.ogema.core.rads.creation.PatternFactory;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.pattern.ContextSensitivePattern;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.CreateMode;
import org.slf4j.LoggerFactory;

public class PatternFinder<P extends ResourcePattern<?>> {

	private final ResourceAccess ra;
	private final PatternFactory<P> factory;
	private final RadFactory<?, ?> radFactory;
	private final Class<? extends Resource> resType;
	private final Object container;

	public PatternFinder(ResourceAccess ra, Class<P> clazz, PatternFactory<P> factory, AccessPriority prio) {
		this(ra, clazz, factory, prio, null);
	}

	@SuppressWarnings( { "unchecked", "rawtypes" })
	public PatternFinder(ResourceAccess ra, Class<P> clazz, PatternFactory<P> factory, AccessPriority prio,
			Object container) {
		assert (container == null || ContextSensitivePattern.class.isAssignableFrom(clazz)) : "Internal error in pattern management... inconsistent PatternFinder initialization.";
		this.ra = ra;
		this.factory = factory;
		this.radFactory = new RadFactory(clazz, prio, factory);
		this.resType = radFactory.getDemandedModel();
		this.container = container;
	}

	@SuppressWarnings("unchecked")
	public List<P> getAllPatterns(Resource resource, boolean recursive) {
		List<P> list = new ArrayList<P>();
		List<Resource> modelMatches;
		if (resource == null)
			modelMatches = (List<Resource>) ra.getResources(resType);
		else 
			modelMatches = (List<Resource>) resource.getSubResources(resType, recursive); 
		// System.out.println("  Pattern finder has " + modelMatches.size() + " model matches for resource type " + resType.getSimpleName());
		Iterator<Resource> it = modelMatches.iterator();
		while (it.hasNext()) {
			Resource res = it.next();
			try {
				P pattern = factory.createNewPattern(res);
				if (checkCompletion(pattern)) {
					list.add(pattern);
				}
			} catch (Exception e) {
				continue;
			}
		}
		return list;
	}
	
	public boolean isSatisfied(P pattern) {
		try {
			return checkCompletion(pattern);
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass()).error("Error in pattern completion check " + e);
			return false;
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean checkCompletion(P pattern) {
		for (ResourceFieldInfo info : radFactory.getResourceFieldInfos()) {
			final Resource resource = RadFactory.getResource(info.getField(), pattern);
			final CreateMode mode = info.getCreateMode();
			if (resource == null || mode != CreateMode.MUST_EXIST) 
				continue;   // ignore uninitialized and optional resources
			if (!resource.isActive()) 
				return false;
			if (!resource.requestAccessMode(info.getMode(), info.getPrio())) 
				return false;
			if (info.isEqualityRequired() && !info.valueSatisfied(resource)) 
				return false;
		}
		if (container != null && pattern instanceof ContextSensitivePattern) {
			try {
				ContainerTool.setContainer((ContextSensitivePattern) pattern, container);
			} catch (NoSuchFieldException | IllegalAccessException | RuntimeException e) {
				LoggerFactory.getLogger(getClass()).error("Internal error: could not set pattern container: " + e);
			}
//			((ResourcePatternExtended) pattern).setContainer(container);
		}
		return pattern.accept(); // user implemented -> may throw Exceptions
	}
	
	public void createOptionalFields(P pattern, boolean createRequiredFields) {
		for (ResourceFieldInfo info : radFactory.getResourceFieldInfos()) {
			final Resource resource = RadFactory.getResource(info.getField(), pattern);
			final CreateMode mode = info.getCreateMode();
			if (resource == null)
				continue;
			if (!createRequiredFields && mode == CreateMode.MUST_EXIST)
				continue;
			resource.create();
		}
	}
	
}
