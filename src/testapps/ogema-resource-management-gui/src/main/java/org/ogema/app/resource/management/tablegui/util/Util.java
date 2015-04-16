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
package org.ogema.app.resource.management.tablegui.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.lang.reflect.Method; //import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.SimpleResource;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.OpaqueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.app.resource.management.gui.ResManagementGuiActivator;

/**
 * 
 * @author cnoelle
 * 
 */
@SuppressWarnings("rawtypes")
public class Util implements Serializable {

	private static final long serialVersionUID = 5011083043755073318L;
	private final ApplicationManager am;
	private final ResourceManagement rm;
	private final ResourceAccess ra;
	private final OgemaLogger logger;
	private static Util instance;
	private List<Map<Integer, Boolean>> sorting = null;
	private final static boolean LONGTYPENAMES = true;
	private final List<ClassLoader> cls = new ArrayList<>();
	private final List<String> typesLongNames = new ArrayList<>();
	private final List<String> typesShortNames = new ArrayList<>();

	private Util() {
		this.am = ResManagementGuiActivator.getAppManager();
		this.rm = am.getResourceManagement();
		this.ra = am.getResourceAccess();
		this.logger = am.getLogger();
	}

	public static Util getInstance() {
		if (instance == null) {
			instance = new Util();
		}
		return instance;
	}

	public String readXMLInput(File xmlFile) {
		String result = "No resources added";
		// note: the name is misleading, resourcesAdded is additive, i.e. it
		// stays the same if no additional resources are added by
		// parser.installConfiguration(...)
		int resourcesAdded = 0;
		int benchmark = -1;
		int counter = 0;
		while (resourcesAdded > benchmark && counter < 100) {
			try {
				counter++;
				XMLParser parser = new XMLParser(am);
				benchmark = resourcesAdded;
				resourcesAdded = parser.installConfiguration(xmlFile);
				if (counter == 1 && resourcesAdded > 0) {
					result = "Resources added";
				}
			} catch (Exception e) {
				result = e.toString();
			}
		}
		return result;
	}

	public String getSimpleValueAsString(String resourcePath) {
		try {
			Resource res = ra.getResource(resourcePath);
			Class clazz = res.getResourceType();
			switch (clazz.getSimpleName()) {
			case "StringResource":
				return ((StringResource) res).getValue();
			case "FloatResource":
				return String.valueOf(((FloatResource) res).getValue());
			case "BooleanResource":
				return String.valueOf(((BooleanResource) res).getValue());
			case "IntegerResource":
				return String.valueOf(((IntegerResource) res).getValue());
			case "OpaqueResource":
				// TODO is this sensible?
				return String.valueOf(((OpaqueResource) res).getValue());
			default:
				return "";
			}
		} catch (Exception e) {
			return "";
		}
	}

	public String getSubresourceType(String parent, String name) {
		String type = null;
		try {
			Resource parentRes = ra.getResource(parent);
			Class clazz = parentRes.getClass();
            @SuppressWarnings("unchecked")
			List<Method> elements = getOptionalElements(clazz);
			Method item = null;
			for (Method method : elements) {
				if (method.getName().equals(name)) {
					item = method;
					break;
				}
			}
			if (item == null)
				return null;
			type = item.getReturnType().getName();
		} catch (Exception e) {
			type = null;
		}
		return type;
	}

	public String setSimpleValue(String resourcePath, String value) {
		try {
			if (resourcePath == null || value == null)
				return "Could not set value: String was null.";
			Resource res = ra.getResource(resourcePath);
			Class clazz = res.getResourceType();
			switch (clazz.getSimpleName()) {
			case "StringResource":
				((StringResource) res).setValue(value);
				break;
			case "FloatResource":
				((FloatResource) res).setValue(Float.parseFloat(value));
				break;
			case "BooleanResource":
				((BooleanResource) res).setValue(Boolean.parseBoolean(value));
				break;
			case "IntegerResource":
				((IntegerResource) res).setValue(Integer.parseInt(value));
				break;
			case "OpaqueResource":
				// TODO is this sensible?
				byte bt = Byte.parseByte(value);
				byte[] btArray = { bt };
				((OpaqueResource) res).setValue(btArray);
				break;
			default:
				return "Could not set value; wrong resource type.";
			}
		} catch (Exception e) {
			return "Error, could not set value: " + e.toString();
		}
		return "Value changed.";
	}

	@SuppressWarnings("unchecked")
	public List<String> getResourcesPaths(String type) {
		Class clazz = getClass(type);
		if (clazz == null || !Resource.class.isAssignableFrom(clazz)) {
			clazz = Resource.class;
		}
		List<String> resourcePaths = new ArrayList<String>();
		List<Resource> resources = ra.getResources(clazz);
		for (Resource res : resources) {
			resourcePaths.add(res.getPath("/"));
		}
		Collections.sort(resourcePaths);
		return resourcePaths;
	}

	public List<String> getResourcesPaths() {
		List<String> resourcePaths = new ArrayList<String>();
		List<Resource> resources = ra.getResources(Resource.class);
		for (Resource res : resources) {
			resourcePaths.add(res.getPath("/"));
		}
		Collections.sort(resourcePaths);
		return resourcePaths;
	}

	public boolean toggleActivation(String resourcePath) {
		try {
			Resource res = ra.getResource(resourcePath);
			boolean active = res.isActive();
			if (active)
				res.deactivate(false);
			else
				res.activate(false);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public String getActivationTogglerLabel(String resourcePath) {
		try {
			Resource res = ra.getResource(resourcePath);
			boolean active = res.isActive();
			if (active)
				return "Deactivate resource";
			else
				return "Activate resource";
		} catch (Exception e) {
			return "Unable to determine activation status.";
		}
	}

	public String getTypeLongName(String resourcePath) {
		try {
			Resource res = ra.getResource(resourcePath);
			return res.getResourceType().getName();
		} catch (Exception e) {
			return null;
		}
	}

	public String getTypeShortName(String resourcePath) {
		try {
			Resource res = ra.getResource(resourcePath);
			return res.getResourceType().getSimpleName();
		} catch (Exception e) {
			return null;
		}
	}

	public boolean isSimpleResource(String longResourceTypeName) {
		Class clazz = null;
		for (ClassLoader cl : cls) {
			try {
				clazz = Class.forName(longResourceTypeName, true, cl);
			} catch (Exception e) {
			}
			if (clazz != null)
				break;
		}
		try {
			if (SimpleResource.class.isAssignableFrom(clazz))
				return true;
		} catch (Exception e) {
		}
		return false;
	}

	public boolean resourceExists(String path) {
		try {
			Resource res = ra.getResource(path);
			if (res != null)
				return true;
		} catch (Exception e) {
		}
		return false;
	}

	public int initializeClassLoaders() {
		// TODO add further class loaders for custom types, using
		// Instrumentation interface?
		// This way it works only for predefined OGEMA resource types.
		ClassLoader cl = Resource.class.getClassLoader();
		if (!cls.contains(cl))
			cls.add(cl);
//		cl = Institution.class.getClassLoader();
//		if (!cls.contains(cl))
//			cls.add(cl);
		cl = ClassLoader.getSystemClassLoader();
		if (!cls.contains(cl))
			cls.add(cl);
		// shouldn't normally help much
		cl = ResManagementGuiActivator.getBundle().getClass().getClassLoader();
		if (!cls.contains(cl))
			cls.add(cl);
		return cls.size();
	}

	public int initializeTypes() {
		String type = "org.ogema.core.model.simple.StringResource";
		if (!typesLongNames.contains(type)) {
			typesLongNames.add(type);
			typesShortNames.add("StringResource");
		}
		type = "org.ogema.core.model.simple.FloatResource";
		if (!typesLongNames.contains(type)) {
			typesLongNames.add(type);
			typesShortNames.add("FloatResource");
		}
		type = "org.ogema.core.model.simple.IntegerResource";
		if (!typesLongNames.contains(type)) {
			typesLongNames.add(type);
			typesShortNames.add("IntegerResource");
		}
		type = "org.ogema.core.model.simple.BooleanResource";
		if (!typesLongNames.contains(type)) {
			typesLongNames.add(type);
			typesShortNames.add("BooleanResource");
		}
		type = "org.ogema.core.model.simple.OpaqueResource";
		if (!typesLongNames.contains(type)) {
			typesLongNames.add(type);
			typesShortNames.add("OpaqueResource");
		}
		List<Resource> res = ra.getResources(Resource.class);
		for (Resource re : res) {
			type = re.getResourceType().getName();
			if (!typesLongNames.contains(type)) {
				typesLongNames.add(type);
				typesShortNames.add(re.getResourceType().getSimpleName());
			}
		}
		return typesLongNames.size();
	}

	public boolean classFound(String type) {
		if (getClass(type) == null)
			return false;
		else
			return true;
	}

	private Class getClass(String longResTypeName) {
		Class clazz = null;
		// initializeClassLoaders();
		for (ClassLoader cl : cls) {
			try {
				clazz = Class.forName(longResTypeName, true, cl);
			} catch (Exception e) {
			}
			if (clazz != null) {
				logger.debug("Initialized class " + clazz.getName() + " with class loader " + cl.toString());
				break;
			}
		}
		return clazz;
	}

	@SuppressWarnings("unchecked")
	public boolean createResource(String path, String type, String location, boolean active, String value) {
		if (path == null || type == null || path.replaceAll("\\s", "").equals("")
				|| type.replaceAll("\\s", "").equals(""))
			return false;
		Resource res = null;
		Resource parent = null;
		Resource referencedResource = null;
		boolean isReference = false;
		boolean isToplevel = true;
		String name;
		Class clazz = getClass(type);
		if (clazz == null) {
			logger.error("Resource type not found: " + type);
			return false;
		}
		else if (!Resource.class.isAssignableFrom(clazz)) {
			logger.error("Found wrong resource type: " + type);
			return false;
		}
		if (!path.equals(location))
			isReference = true;
		if (path.contains("/") || path.contains("."))
			isToplevel = false;
		if (isToplevel) {
			try {
				name = path;
				res = rm.createResource(path, clazz);
			} catch (Exception e) {
				logger.error("  Unable to create resource " + path + ": " + e.toString());
				return false;
			}
		}
		else {
			try {
				int lastOcc = Math.max(path.lastIndexOf("/"), path.lastIndexOf("."));
				String parentPath = path.substring(0, lastOcc);
				name = path.substring(lastOcc + 1);
				parent = ra.getResource(parentPath);
				if (parent == null) {
					logger.error("  Unable to obtain parent resource for " + path);
					return false;
				}
			} catch (Exception e) {
				logger.debug("  Unable to obtain parent resource for " + path);
				return false;
			}
			if (!isReference) {
				// TODO
				/**
				 * if (!isTypeCorrect(parent, name, clazz)) { logger.error(); return false; }
				 */
				try {
					res = parent.addOptionalElement(name);
				} catch (Exception e) {
					try {
						res = parent.addDecorator(name, clazz);
					} catch (Exception ee) {
						logger.error("Unable to create subresource " + path + " of type " + type);
						return false;
					}
				}
			}
			else if (isReference) {
				try {
					referencedResource = ra.getResource(location);
					if (referencedResource == null) {
						logger.debug("  Unable to obtain resource to be references at " + location);
						return false;
					}
				} catch (Exception e) {
					return false;
				}
				try {
					parent.setOptionalElement(name, referencedResource);
					res = parent.getSubResource(name);
				} catch (Exception e) {
					try {
						res = parent.addDecorator(name, referencedResource);
					} catch (Exception ee) {
						logger.error("Unable to create subresource " + path + " of type " + type);
						return false;
					}
				}
			}
			if (!res.getResourceType().equals(clazz)) {
				logger.warn("Created subresource of unexpected type: " + path + ". Expected: " + type + "; found: "
						+ clazz.getName());
			}
		}
		if (active) {
			res.activate(false);
		}
		if (value == null)
			return true;
		if (!isReference && !value.replaceAll("\\s", "").equals("")) {
			if (SimpleResource.class.isAssignableFrom(clazz)) {
				try {
					switch (clazz.getSimpleName()) {
					case "StringResource":
						((StringResource) res).setValue(value);
						break;
					case "FloatResource":
						float floatVal = Float.parseFloat(value.replaceAll("\\s", ""));
						((FloatResource) res).setValue(floatVal);
						break;
					case "BooleanResource":
						boolean boolVal = Boolean.parseBoolean(value.replaceAll("\\s", ""));
						((BooleanResource) res).setValue(boolVal);
						break;
					case "IntegerResource":
						int intVal = Integer.parseInt(value.replaceAll("\\s", ""));
						((IntegerResource) res).setValue(intVal);
						break;
					case "OpaqueResource":
						// FIXME
						byte byteVal = Byte.parseByte(value);
						byte[] byteVals = { byteVal };
						((OpaqueResource) res).setValue(byteVals);
					}
				} catch (Exception e) {
					logger.warn("Unable to assign a value to resource " + path);
				}
			}
		}
		return true;
	}

	public File generateXMLFile(List<Map<Integer, Boolean>> ordering, String downloadAddress)
			throws FileNotFoundException {
		List<String[]> data = getAllResources(ordering, LONGTYPENAMES);
		XMLWriter writer = new XMLWriter(am);
		File file = new File(downloadAddress);
		file = writer.createFile(data, file);
		return file;
	}

	public class ResourcesListComparator implements Comparator<String[]> {

		@Override
		public int compare(String[] o1, String[] o2) {
			List<Map<Integer, Boolean>> sort = getOrder();
			if (sort == null) {
				logger.error("Resource list comparator called without defined sorting.");
				return 0;
			}
			for (Map<Integer, Boolean> map : sort) {
				if (!map.keySet().iterator().hasNext()) {
					continue;
				}
				int key = map.keySet().iterator().next().intValue();
				boolean order = map.get(key).booleanValue();
				int sign = 1;
				if (!order) {
					sign = -1;
				}
				if (key == 0) {
					// calculate the depth of the resource tree to be traversed
					// to reach the two resources
					int level1 = o1[0].length() - o1[0].replaceAll("/", "").length();
					int level2 = o2[0].length() - o2[0].replaceAll("/", "").length();
					if (level1 != level2)
						return (level1 - level2) * sign;
				}
				else {
					String string1 = o1[key].toLowerCase();
					String string2 = o2[key].toLowerCase();
					if (!string1.equals(string2))
						return (string1.compareTo(string2)) * sign;
				}
			}
			return o1[0].toLowerCase().compareTo(o2[0].toLowerCase());
		}

	}

	/**
	 * 
	 * @param sortBy
	 *            defines the sorting. Each map must have only one entryset. The first integer in the list defines the
	 *            main sorting criterion, the second one defines the secondary criterion, etc. The following values
	 *            apply, in case of <code>true</code> value of the Boolean: <br>
	 *            0 : top-level resources first, then second level, etc <br>
	 *            1 : sort by Resource Type <br>
	 *            2 : active resources come first <br>
	 *            3 : non-references first <br>
	 *            If the Boolean value is <code>false</code>, the sorting is inverted.
	 * @param longTypesNames
	 * <br>
	 *            true : long Resource Types names <br>
	 *            false : simple Resource Types names
	 * @return
	 */
	// TODO ordering is buggy
	public List<String[]> getAllResources(List<Map<Integer, Boolean>> sortBy, boolean longTypesNames) {
		sorting = sortBy;
		List<String[]> list = new ArrayList<String[]>();
		List<Resource> resources = ra.getToplevelResources(Resource.class);
		for (Resource res : resources) {
			list.addAll(getNewEntries(res, longTypesNames, true));
		}
		Collections.sort(list, new ResourcesListComparator());
		// sorting = null;
		return list;
	}

	public boolean hasParentOfSameType(Resource res) {
		Class clazz = res.getResourceType();
		while (res.getParent() != null) {
			res = res.getParent();
			if (res.getResourceType().equals(clazz))
				return true;
		}
		return false;
	}

	public List<String[]> getResources(List<Map<Integer, Boolean>> sortBy, boolean longTypesNames, String longTypeName,
			boolean showSubresources) {
		sorting = sortBy;
		Class clazz = getClass(longTypeName);
		List<String[]> list = new ArrayList<String[]>();
		if (clazz == null || !Resource.class.isAssignableFrom(clazz))
			return list;
		@SuppressWarnings("unchecked")
		List<Resource> resources = ra.getResources(clazz);
		for (Resource res : resources) {
			// in order to avoid displaying a resource twice
			if (!showSubresources || !hasParentOfSameType(res)) {
				list.addAll(getNewEntries(res, longTypesNames, showSubresources));
			}
		}
		Collections.sort(list, new ResourcesListComparator());
		// sorting = null;
		return list;
	}

	/**
	 * 
	 * @param res
	 * @param longTypesNames
	 * <br>
	 *            true : long Resource Types names <br>
	 *            false : simple Resource Types names
	 * @return
	 */
	private List<String[]> getNewEntries(Resource res, boolean longTypesNames, boolean iterative) {
		List<String[]> entries = new ArrayList<String[]>();
		String path = "";
		String resourceType = "";
		String active = "";
		String location = "";
		String reference = "";
		String value = null;
		String subs = "0";
		try {
			path = res.getPath("/");
		} catch (Exception e) {
		}
		try {
			if (longTypesNames) {
				resourceType = res.getResourceType().getName();
			}
			else {
				resourceType = res.getResourceType().getSimpleName();
			}
		} catch (Exception e) {
		}
		try {
			active = String.valueOf(res.isActive());
		} catch (Exception e) {
		}
		try {
			location = res.getLocation("/");
		} catch (Exception e) {
		}
		try {
			reference = String.valueOf(res.isReference(false));
		} catch (Exception e) {
		}
		try {
			if (SimpleResource.class.isInstance(res)) {
				Class clazz = res.getResourceType();
				if (clazz.equals(IntegerResource.class)) {
					value = String.valueOf(((IntegerResource) res).getValue());
				}
				else if (clazz.equals(StringResource.class)) {
					value = ((StringResource) res).getValue();
				}
				else if (clazz.equals(FloatResource.class)) {
					value = String.valueOf(((FloatResource) res).getValue());
				}
				else if (clazz.equals(BooleanResource.class)) {
					value = String.valueOf(((BooleanResource) res).getValue());
				}
			}
		} catch (Exception e) {
		}
		try {
			subs = String.valueOf(res.getSubResources(false).size());
		} catch (Exception e) {
		}
		String[] newEntry = { path, resourceType, active, location, reference, value, subs };
		entries.add(newEntry);
		// in order to avoid infinite loops, only add sub resources of
		// non-referencing resources
		if (!res.isReference(false) && iterative) {
			List<Resource> subResources = res.getSubResources(false);
			for (Resource subRes : subResources) {
				entries.addAll(getNewEntries(subRes, longTypesNames, iterative));
			}
		}
		return entries;
	}

	public Integer getKey(Map<Integer, String> map, String value) {
		for (Entry<Integer, String> entry : map.entrySet()) {
			if (entry.getValue().equals(value)) {
				return entry.getKey();
			}
		}

		return Integer.valueOf(-1);
	}

	final static public List<String> getHeaders() {
		List<String> headers = new ArrayList<String>();
		headers.add("Path");
		headers.add("ResourceType");
		headers.add("Active");
		headers.add("Location");
		headers.add("Reference");
		headers.add("Value");
		headers.add("NrSubresources");
		return headers;
	}

	private List<Map<Integer, Boolean>> getOrder() {
		return sorting;
	}

	/**
	 * 
	 * @param length
	 * <br>
	 *            false : short resource type names <br>
	 *            true : long resource type names
	 * @return
	 */
	public List<String> getAvailableTypes(boolean length) {
		if (length)
			return typesLongNames;
		else
			return typesShortNames;
	}

	/**
	 * 
	 * @param path
	 *            : resource path
	 * @return optional elements of given resource that have not yet been created.
	 */
	public List<String> getUnavailableOptionalElements(String path) {
		Resource res = null;
		try {
			res = ra.getResource(path);
		} catch (Exception e) {
		}
		List<String> list = new ArrayList<String>();
		if (res == null) {
			return list;
		}
		Class clazz = res.getResourceType();
		//		for (Method meth : getOptionalElements(clazz)) {
		//			list.add(meth.getName());
		//		}
		List<String> auxList = new ArrayList<String>(list);

		Iterator<String> iter = auxList.iterator();
		// remove existing optional elements
		while (iter.hasNext()) {
			String item = iter.next();
			try {
				Resource subRes = res.getSubResource(item);
				if (subRes != null) {
					list.remove(item);
				}
			} catch (Exception e) {
			}
		}
		return list;
	}

    @SuppressWarnings({"unchecked", "rawtypes"})
	private List<Method> getOptionalElements(Class<? extends Resource> clazz) {
		List<Method> list = new ArrayList<Method>();
		if (clazz == null || clazz.equals(Resource.class)) {
			return list;
		}
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			list.add(method);
		}
		Class[] supResources = clazz.getInterfaces();
		for (Class supResource : supResources) {
			list.addAll(getOptionalElements(supResource));
		}
		return list;
	}

	public List<String[]> getSchedules(String timeFormat) {
		List<String[]> list = new ArrayList<String[]>();
		List<Schedule> schedules = ra.getResources(Schedule.class);
		for (Schedule schedule : schedules) {
			String loc = schedule.getLocation("/");
			String type = schedule.getResourceType().getSimpleName();
			String parentType = schedule.getParent().getResourceType().getSimpleName();
			String active = String.valueOf(schedule.isActive());
			List<SampledValue> values = schedule.getValues(0);
			String nrValues = String.valueOf(values.size());
			long startTime;
			long endTime;
			String start = "";
			String end = "";
			String currentValue = "";
			long t = am.getFrameworkTime();
			if (values.size() > 0) {
				startTime = values.get(0).getTimestamp();
				endTime = values.get(values.size() - 1).getTimestamp();
				start = String.valueOf(startTime);
				end = String.valueOf(endTime);
				if (!timeFormat.toLowerCase().startsWith("millis")) {
					start = convertToReadableTime(startTime);
					end = convertToReadableTime(endTime);
				}
				Value val = schedule.getValue(t).getValue();
				currentValue = getValue(val, parentType);
				if (currentValue == null)
					currentValue = "";
			}
			String mode = schedule.getInterpolationMode().toString();
			String[] entry = { loc, type, parentType, active, nrValues, start, end, currentValue, mode };
			list.add(entry);
		}
		Collections.sort(list, new SchedulesComparator());
		return list;
	}

	private static class SchedulesComparator implements Comparator<String[]> {

		@Override
		public int compare(String[] o1, String[] o2) {
			return o1[0].toLowerCase().compareTo(o2[0].toLowerCase());
		}

	}

	public String convertToReadableTime(long t) {
		final Calendar c = Calendar.getInstance();
		//		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.setTimeInMillis(t);
		String year = String.valueOf(c.get(Calendar.YEAR));
		String month = String.valueOf(c.get(Calendar.MONTH) + 1);
		if (month.length() == 1) {
			month = "0" + month;
		}
		String day = String.valueOf(c.get(Calendar.DATE));
		if (day.length() == 1) {
			day = "0" + day;
		}
		String h = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
		if (h.length() == 1) {
			h = "0" + h;
		}
		String m = String.valueOf(c.get(Calendar.MINUTE));
		if (m.length() == 1) {
			m = "0" + m;
		}
		String s = String.valueOf(c.get(Calendar.SECOND));
		if (s.length() == 1) {
			s = "0" + s;
		}
		String millis = String.valueOf(c.get(Calendar.MILLISECOND));
		if (millis.length() == 1) {
			millis = "00" + millis;
		}
		else if (millis.length() == 2) {
			millis = "0" + millis;
		}
		String str = year + "-" + month + "-" + day + "  " + h + ":" + m + ":" + s + "." + millis;
		return str;
	}

	public String getCurrentTime(String format) {
		long t = am.getFrameworkTime();
		String time;
		if (format.toLowerCase().startsWith("millis")) {
			time = String.valueOf(t);
		}
		else {
			time = convertToReadableTime(t);
		}
		return time;
	}

	public List<String> getTimeFormats() {
		List<String> list = new ArrayList<String>();
		list.add("milliseconds");
		list.add("human readable");
		return list;
	}

	public String getInterpolationMode(String scheduleLoc) {
		try {
			Schedule sched = (Schedule) ra.getResource(scheduleLoc);
			return sched.getInterpolationMode().name();
		} catch (Exception e) {
			return "Error obtaining interpolation mode.";
		}
	}

	public List<String> getInterpolationModes() {
		InterpolationMode[] modes = InterpolationMode.values();
		List<String> list = new ArrayList<String>();
		for (InterpolationMode mode : modes) {
			list.add(mode.name());
		}
		return list;
	}

	public String setInterpolationMode(String scheduleLoc, String mode) {
		try {
			Schedule schedule = (Schedule) ra.getResource(scheduleLoc);
			String oldMode = schedule.getInterpolationMode().name();
			if (oldMode.equals(mode))
				return "Nothing changed.";
			schedule.setInterpolationMode(InterpolationMode.valueOf(mode));
			return "New interpolation mode: " + mode;
		} catch (Exception e) {
			return "Error updating interpolation mode: " + e;
		}
	}

	public boolean isActive(String loc) {
		try {
			Resource res = ra.getResource(loc);
			return res.isActive();
		} catch (Exception e) {
		}
		return false;
	}

	public String setActive(String loc, String active) {
		boolean act = Boolean.parseBoolean(active);
		try {
			Resource res = ra.getResource(loc);
			boolean currentActive = res.isActive();
			if (act == currentActive)
				return "Nothing changed.";
			if (act) {
				res.activate(false);
			}
			else {
				res.deactivate(false);
			}
			return "Active mode changed: " + act;
		} catch (Exception e) {
			return "Error updating active mode: " + e;
		}
	}

	private String getValue(Value val, String parentType) {
		String currentValue = "";
		try {
			switch (parentType) {
			case "FloatResource":
				currentValue = String.valueOf(val.getFloatValue());
				break;
			case "BooleanResource":
				currentValue = String.valueOf(val.getBooleanValue());
				break;
			case "IntegerResource":
				currentValue = String.valueOf(val.getIntegerValue());
				break;
			case "TimeResource":
				currentValue = String.valueOf(val.getLongValue());
				break;
			case "StringResource":
				currentValue = val.getStringValue();
				break;
			default:
				currentValue = val.getObjectValue().toString();
			}
		} catch (Exception e) {
		}
		return currentValue;
	}

	private Value createValue(String val, String type) {
		Value value;
		try {
			switch (type) {
			case "FloatResource":
				value = new FloatValue(Float.parseFloat(val));
				break;
			case "BooleanResource":
				value = new BooleanValue(Boolean.parseBoolean(val));
				break;
			case "IntegerResource":
				value = new IntegerValue(Integer.parseInt(val));
				break;
			case "TimeResource":
				value = new LongValue(Long.parseLong(val));
				break;
			case "StringResource":
				value = new StringValue(val);
				break;
			default:
				// FIXME probably a bug in ObjectValue constructor
				//			value = new ObjectValue((Object) val);
				value = null;
			}
		} catch (Exception e) {
			return null;
		}
		return value;
	}

	public int getNrScheduleValues(String scheduleLoc, String startTime, String endTime) {
		long t0;
		long t1;
		try {
			t0 = Long.parseLong(startTime);
			t1 = Long.parseLong(endTime);
		} catch (Exception e) {
			return 0;
		}
		if (t1 < t0)
			return 0;
		Schedule schedule;
		try {
			schedule = (Schedule) ra.getResource(scheduleLoc);
		} catch (Exception e) {
			return 0;
		}
		if (schedule == null)
			return 0;
		List<SampledValue> list = schedule.getValues(t0, t1);
		if (list == null)
			return 0;
		return list.size();
	}

	public String deleteValues(String scheduleLoc, String startTime, String endTime) {
		long t0;
		long t1;
		try {
			t0 = Long.parseLong(startTime);
			t1 = Long.parseLong(endTime);
		} catch (Exception e) {
			return "Could not parse times.";
		}
		if (t1 < t0)
			return "End time earlier than start time.";
		Schedule schedule;
		try {
			schedule = (Schedule) ra.getResource(scheduleLoc);
		} catch (Exception e) {
			return "Error accessing schedule.";
		}
		schedule.deleteValues(t0, t1);
		return "Values deleted.";
	}

	public String addScheduleValue(String schedule, String timestamp, String value, String quality) {
		try {
			Schedule sched = (Schedule) ra.getResource(schedule);
			String type = sched.getParent().getResourceType().getSimpleName();
			long t = Long.parseLong(timestamp);
			Quality qual = Quality.valueOf(quality);
			Value val = createValue(value, type);
			if (val == null)
				return "Value could not be parsed to the required type.";
			SampledValue sv = new SampledValue(val, t, qual);
			List<SampledValue> values = new ArrayList<SampledValue>();
			values.add(sv);
			sched.addValues(values);
		} catch (Exception e) {
			return "Exception caught while trying to add value: " + e;
		}
		return "Value added";
	}

	public List<String> getSchedulesList() {
		List<Schedule> list = ra.getResources(Schedule.class);
		List<String> strings = new ArrayList<String>();
		strings.add("");
		for (Schedule schedule : list) {
			strings.add(schedule.getLocation("/"));
		}
		Collections.sort(strings, new CaseInsensitiveComparator());
		return strings;
	}

	private static class CaseInsensitiveComparator implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			return o1.toLowerCase().compareTo(o2.toLowerCase());
		}

	}

	public List<String[]> getScheduleData(String scheduleLoc, String timeFormat) {
		List<String[]> list = new ArrayList<String[]>();
		if (scheduleLoc.replaceAll("\\s", "").equals(""))
			return list;
		Schedule schedule;
		String parentType;
		try {
			schedule = (Schedule) ra.getResource(scheduleLoc);
			parentType = schedule.getParent().getResourceType().getSimpleName();
		} catch (Exception e) {
			return list;
		}
		List<SampledValue> values = schedule.getValues(0);
		for (SampledValue value : values) {
			long t = value.getTimestamp();
			String time = String.valueOf(t);
			if (!timeFormat.startsWith("millis")) {
				time = convertToReadableTime(t);
			}
			String currentVal = getValue(value.getValue(), parentType);
			String quality = value.getQuality().toString();
			String[] entry = { time, currentVal, quality };
			list.add(entry);
		}
		return list;
	}

	public String getValidName(String name) {
		if (name.isEmpty())
			return name;
		if (Character.isDigit(name.charAt(0))) {
			name = "_" + name;
		}
		name = name.replaceAll("-", "_").replaceAll("\\s", "_").replaceAll("/", "_").replaceAll("\\(", "_").replaceAll(
				"\\)", "_").replaceAll("\\[", "_").replaceAll("\\]", "_").replaceAll("\\{", "_").replaceAll("\\}", "_")
				.replaceAll("\\.", "_").replaceAll("<", "_").replaceAll(">", "_").replaceAll("&", "_").replaceAll("!",
						"_").replaceAll("§", "_").replaceAll("%", "_").replaceAll("&", "_").replaceAll(",", "_")
				.replaceAll(";", "_").replaceAll(":", "_").replaceAll(";", "_").replaceAll("\\|", "_").replaceAll("@",
						"_");
		return name;
	}

}
