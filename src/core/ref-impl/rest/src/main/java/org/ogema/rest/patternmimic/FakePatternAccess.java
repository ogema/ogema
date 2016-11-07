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
package org.ogema.rest.patternmimic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.rest.servlet.RestApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakePatternAccess {

	private final ApplicationManager am;
	private final Logger logger;
	
	public FakePatternAccess(ApplicationManager am) {
		this.am=am;
		this.logger = am.getLogger();
	}
	
	@SuppressWarnings("unchecked")
	public PatternMatch create(FakePattern pattern, Resource parent, String name) throws ClassNotFoundException {
		Resource demandedModel;
		if (parent != null)
			demandedModel = parent.addDecorator(name, pattern.modelClass);
		else
			demandedModel = am.getResourceManagement().createResource(name, pattern.modelClass);
		PatternMatch match = new PatternMatch();
		match.patternType = pattern;
		match.demandedModel = new ResourceProxy(demandedModel, demandedModel);
//		match.demandedModel = demandedModel;
		match.fields = new HashMap<>();
		int lastopenresources = pattern.resourceFields.size()+1;
		int openresources = pattern.resourceFields.size();
		while (openresources > 0 && openresources < lastopenresources) {
			lastopenresources = openresources;
			openresources = 0;
			for (ResourceProxy rp: pattern.resourceFields) {
				Resource sub;
				if (!rp.optional)
					sub = addSubResource(demandedModel,(Class<? extends Resource>) Class.forName(rp.type),rp.relativePath);
				else
					sub = getSubresource(demandedModel, rp.relativePath);
				if (!rp.optional && sub == null) { // this can happen if we try to create a sub-sub resource before its parent (a subresource) 
					openresources++;
					continue;
				}
				if (sub == null) // means it is optional
					continue;
				String value = rp.value;
				if (value != null && (sub instanceof SingleValueResource)) {
					try {
						setValue((SingleValueResource) sub, value);
					} catch (NumberFormatException e) {
						LoggerFactory.getLogger(FakePattern.class).error("Error setting resource value",e);
					}
				}
				AccessMode am = rp.accessMode;
				sub.requestAccessMode(am, AccessPriority.PRIO_LOWEST); // FIXME priority; required?
				match.fields.put(rp.name, new ResourceProxy(demandedModel,sub));
	 		}
		}
		return match;
	}
	
//	@Deprecated
//	public List<PatternMatch> getMatches(FakePattern pattern, Resource parent, boolean recursive) {
//		if (logger.isDebugEnabled())
//			logger.debug("Processing pattern request for demanded model {}",pattern.modelClass.getName());
//		Class<? extends Resource> cl = pattern.modelClass;
//		List<? extends Resource> topMatches;
//		List<PatternMatch> matches = new ArrayList<>();
//		if (parent == null && recursive)
//			topMatches = am.getResourceAccess().getResources(cl);
//		else if (parent == null)
//			topMatches = am.getResourceAccess().getToplevelResources(cl);
//		else 
//			topMatches = parent.getSubResources(cl, recursive);
//		// TODO filter according to level if recursive == true
//		for (Resource tm : topMatches) {
//			if (logger.isTraceEnabled())
//				logger.trace("Checking resource {}",tm);
//			if (!tm.isActive())
//				continue;
//			PatternMatch match = matches(pattern, tm);
//			if (match != null)
//				matches.add(match);
//			if (logger.isTraceEnabled())
//				logger.trace("Resource {} matches: {}",tm,(match != null));
//		}
//		return matches;
//	}
	
	public PatternMatchList getMatches(FakePattern pattern, Resource parent, boolean recursive, int maxHits, int from) {
		if (logger.isDebugEnabled())
			logger.debug("Processing pattern request for demanded model {}",pattern.modelClass.getName());
		
		long upperLimit = ((long) maxHits) + from; // prevents overflow
		
		Class<? extends Resource> cl = pattern.modelClass;
		List<? extends Resource> topMatches;
		List<PatternMatch> matches = new ArrayList<>();
		if (parent == null && recursive)
			topMatches = am.getResourceAccess().getResources(cl);
		else if (parent == null)
			topMatches = am.getResourceAccess().getToplevelResources(cl);
		else 
			topMatches = parent.getSubResources(cl, recursive);
		int counter = 0;
		for (Resource tm : topMatches) {
			if (!tm.isActive())
				continue;
			PatternMatch match = matches(pattern, tm);
			if (match == null)
				continue;
			if (counter >= from && counter < upperLimit) // we cannot break, since we need the total nr of matches
				matches.add(match);
			counter++;
		}
		PatternMatchList pml = new PatternMatchList();
		pml.setMatches(matches);
		pml.nrMatches = counter; 
		if (matches.size() > 0) {
			pml.matchesStart = from;
			pml.matchesEnd = (int) Math.min(upperLimit, (long) counter) - 1;
		}
		return pml;
	}
	
	/**
	 * 
	 * @param type
	 * @param demandedModel
	 * @return
	 *  	null, if this is not a match, the PatternMatch object corresponding to demandedModel otherwise
	 */
	@SuppressWarnings("unchecked")
	private static final PatternMatch matches(FakePattern type, Resource demandedModel) {
		PatternMatch match = new PatternMatch();
		match.demandedModel = new ResourceProxy(demandedModel,demandedModel);
		match.patternType = type;
		match.fields = new HashMap<>();
		for (ResourceProxy proxy : type.resourceFields) {
			// TODO return false if the field does not match
			String path = proxy.relativePath;
			Objects.requireNonNull(path);
			Resource entry = getSubresource(demandedModel, path);
			boolean optional = proxy.optional;
			if ((entry == null || !entry.isActive()) && !optional)
				return null;
			else if (entry == null)
				continue;
			String loc = proxy.location;
			if (loc != null && !loc.trim().isEmpty() && !loc.trim().replace('.', '/').equals(entry.getLocation())) {
				return null;
			}
//				continue;
			AccessMode am = proxy.accessMode;
			if (am != null && !entry.requestAccessMode(am, AccessPriority.PRIO_LOWEST)) { // FIXME priority
				return null;
			}
			Class<? extends Resource> tp;
			try {
				tp = (Class<? extends Resource>) Class.forName(proxy.type);
			} catch (ClassNotFoundException e) {
				LoggerFactory.getLogger(RestApp.class).error("Could not resolve class " + proxy.type);
				return null;
			} catch (NullPointerException e) {
				LoggerFactory.getLogger(RestApp.class).error("Null pointer in class resolution for type " + proxy.type);
				return null;
			}
			if (!tp.isAssignableFrom(entry.getResourceType()))
				return null;
			String value = proxy.value;
			if (value != null) {
				if (!compareValues(value,entry))
					return null;
			}
			match.fields.put(proxy.name, new ResourceProxy(demandedModel,entry));
		}
		return match;
	}
	
	private static final Resource getSubresource(Resource base, String path) {
		String[] components = path.split("\\.|/");
		Resource result = base;
		for (String cmp : components) { 
			if (cmp.isEmpty())
				continue;
			result = result.getSubResource(cmp);
			if (result == null || !result.exists()) 
				return null;
		}
		return result;
	}
	
	private static final Resource addSubResource(Resource base, Class<? extends Resource> type, String path) {
		String[] components = path.split("\\.|/");
		if (components.length == 0 && components[0].isEmpty())
			throw new IllegalArgumentException("Illegal path " + path);
		Resource result = base;
		for (int i=0;i<components.length - 1;i++) {
			if (components[i].isEmpty())
				continue;
			result = result.getSubResource(components[i]);
			if (result == null) { // virtual is ok
				return null;
			}
		}
		return result.create().addDecorator(components[components.length-1], type);
	}
	
	static boolean compareValues(String value,Resource resource) {
		if (resource instanceof StringResource) {
			return ((StringResource) resource).getValue().equals(value);
		}
		else if (resource instanceof FloatResource) {	// FIXME ok?
			try {
				float val1 = Float.parseFloat(value);
				float val2 = ((FloatResource) resource).getValue();
				float epsilon = Math.max(Math.abs(val1), Math.abs(val2)) / 10000;
				return Math.abs(val1- val2) < epsilon; 
			} catch (NumberFormatException e) {
				return false;
			}
		}
		else if (resource instanceof IntegerResource || resource instanceof TimeResource) {
			try {
				long val1 = Long.parseLong(value);
				
				long val2;
				if (resource instanceof IntegerResource)
					val2 = ((IntegerResource) resource).getValue();
				else
					val2 = ((TimeResource) resource).getValue();
				return val1 == val2;
			} catch (NumberFormatException e) {
				return false;
			}
		}
		else if (resource instanceof BooleanResource) {
			try {
				boolean val1 = Boolean.parseBoolean(value);
				boolean val2 = ((BooleanResource) resource).getValue();
				return (val1 == val2);
			} catch (NumberFormatException e) {
				return false;
			}
		}
		else
			return false;
	}
	
	static String getValue(Resource resource) {
		if (resource instanceof StringResource) {
			return ((StringResource) resource).getValue();
		}
		else if (resource instanceof FloatResource) {
			return String.valueOf(((FloatResource) resource).getValue());
		}
		else if (resource instanceof IntegerResource) {
			return String.valueOf(((IntegerResource) resource).getValue());
		}
		else if (resource instanceof BooleanResource) {
			return String.valueOf(((BooleanResource) resource).getValue());
		}
		else if (resource instanceof TimeResource) {
			return String.valueOf(((TimeResource) resource).getValue());
		}
		else
			return null;
	}
	
	static void setValue(SingleValueResource resource, String value) throws NumberFormatException {
		if (resource instanceof StringResource) {
			((StringResource) resource).setValue(value);
		}
		else if (resource instanceof FloatResource) {
			((FloatResource) resource).setValue(Float.parseFloat(value));
		}
		else if (resource instanceof IntegerResource) {
			((IntegerResource) resource).setValue(Integer.parseInt(value));
		}
		else if (resource instanceof BooleanResource) {
			((BooleanResource) resource).setValue(Boolean.parseBoolean(value));
		}
		else if (resource instanceof TimeResource) {
			((TimeResource) resource).setValue(Long.parseLong(value));
		}
	}
}
