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
package org.ogema.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.DefinitionSchedule;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.LengthResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.TemperatureSensor;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ResourceFlashServlet extends HttpServlet {

	private final static String PREFIX_ = "__testResource";
	final private static long serialVersionUID = 1L;
	//private final ApplicationManager am;
	final private ResourceAccess ra;
	final private ResourceManagement rm;
	final private List<ClassLoader> classLoaders;
	final private List<TestClass> objects;
	final private DefinitionSchedule schedule;
	final private int DEFAULT_RES_NUM = 1; // default nr of resources to be generated
	private final AtomicInteger counter;
	private final static String NOT_FOUND="{\"error\":\"not found\"}";
	private final static String TREE_PREFIX = "__tree_test_resources_";
	private volatile int treeCounter = 0;
	
	@SuppressWarnings("unused")
	private static class TestClass {
		
		private String test1;
		private String test2;
		public boolean isTestClass = true;
		
		public TestClass(String s1,String s2) {
			this.test1 = s1;
			this.test2 = s2;
		}
		
		public String getTest1() {
			return test1;
		}
		public void setTest1(String test1) {
			this.test1 = test1;
		}
		public String getTest2() {
			return test2;
		}
		public void setTest2(String test2) {
			this.test2 = test2;
		}
	
	}
	
	public ResourceFlashServlet(ApplicationManager am) {
		//this.am = am;
		this.ra = am.getResourceAccess();
		this.rm = am.getResourceManagement();
		this.classLoaders = new ArrayList<>();
		this.objects = new ArrayList<>();
		if (Boolean.getBoolean("org.ogema.apps.createtestresources")) {
			FloatResource fl = rm.createResource("__testFloatRes__", FloatResource.class);
			this.schedule = fl.program().create();
			schedule.activate(false);
		} else {
			this.schedule = null;
		}
		// FIXME ugly approach!
		classLoaders.add(Resource.class.getClassLoader());
		classLoaders.add(Room.class.getClassLoader());
		classLoaders.add(this.getClass().getClassLoader());
		List<TemperatureSensor> ts = ra.getToplevelResources(TemperatureSensor.class);
		for (TemperatureSensor t: ts) {
			if (!t.getName().startsWith(TREE_PREFIX))
				continue;
			try {
				int cnt = Integer.parseInt(t.getName().substring(TREE_PREFIX.length()));
				if (cnt >=treeCounter)
					treeCounter = cnt+1;
			} catch (Exception e) {}
		}
		int cnt = 0;
		for (Resource r : ra.getToplevelResources(null)) {
			final String name = r.getName();
			if (name.startsWith(PREFIX_) && name.length() > PREFIX_.length()) {
				final StringBuilder sb = new StringBuilder();
				boolean wasNr = false;
				for (char c : name.toCharArray()) {
					if (!Character.isDigit(c)) {
						if (!wasNr)
							continue;
						else 
							break;
					} else {
						sb.append(c);
						wasNr = true;
					}
				}
				if (sb.length() > 0) {
					final int nr = Integer.parseInt(sb.toString());
					if (nr >= cnt)
						cnt = nr + 1;
				}
			}
		}
		this.counter = new AtomicInteger(cnt);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final int localCounter = counter.getAndIncrement();
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = req.getReader();
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
		} finally {
			reader.close();
		}
		String data = sb.toString();		
		ObjectMapper mapper = new ObjectMapper();
		Map<String,String> map = mapper.readValue(data, Map.class);
		System.out.println("POST request: " + map.toString());		
		int nr = DEFAULT_RES_NUM; 
		if (map.containsKey("nr")) {
			try {
				nr = Integer.valueOf(map.get("nr"));
			} catch (Exception e) {
			}
		}
		if (!map.containsKey("type")) {
			resp.getWriter().write(NOT_FOUND);
			resp.setStatus(200);
			return;
		}
		String type = map.get("type");
		
		Class clazz = null;
		for (ClassLoader cl : classLoaders) {
			try {
				clazz = (Class) cl.loadClass(type);
				if (clazz != null) {
					break;
				}
			} catch (Exception ee) {
				continue;
			}			
		}
		long t0 = System.currentTimeMillis();
		if (type.equals("complextree")) {
			createTree(map, nr, false);
		}
		else if (type.equals("complextreewithlisteners")) {
			createTree(map, nr, true);
		}
		else if (Resource.class.isAssignableFrom(clazz) && !Schedule.class.isAssignableFrom(clazz)) {
			String prefix = "__testResource";
			if (map.containsKey("action") && map.get("action").equals("delete")) {
				List<? extends Resource> list = ra.getToplevelResources(clazz);
				for (Resource testRes : list) {
					try {
						if (!testRes.getName().startsWith(prefix)) continue;
						testRes.delete();
					}
					catch (Exception e) {}
				}		
				System.gc();
			}	
			else if (map.containsKey("action") && map.get("action").equals("create")) {
				String prefix2 = prefix + String.valueOf(localCounter) + "__";
				for (int i=0;i<nr;i++) {
					try {
						Resource res = rm.createResource(prefix2 + String.valueOf(i), clazz);
						res.activate(false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		else if (TestClass.class.isAssignableFrom(clazz)) { 
			if (map.containsKey("action") && map.get("action").equals("delete")) {
				objects.clear();
				System.gc();
			}
			else if (map.containsKey("action") && map.get("action").equals("create")) {
				for (int i=0;i<nr;i++) {
					objects.add(new TestClass(UUID.randomUUID().toString(), UUID.randomUUID().toString())); // two random strings
					//objects.add(new TestClass("", ""));
				}
			}
		}
		else if (DefinitionSchedule.class.isAssignableFrom(clazz)) { 
			if (map.containsKey("action") && map.get("action").equals("delete")) {
				schedule.deleteValues();
				System.gc();
			}
			else if (map.containsKey("action") && map.get("action").equals("create")) {
				Random rand = new Random();
				List<SampledValue> values = new ArrayList<>();
				for (int i=0;i<nr;i++) {
					long timestamp = rand.nextLong();
					FloatValue val = new FloatValue(rand.nextFloat());
					SampledValue sv = new SampledValue(val,timestamp,Quality.GOOD);
					values.add(sv);
				}
				schedule.addValues(values);
			}
		}
		long t1 = System.currentTimeMillis();
		String response = "{\"duration\":" + (t1-t0) + "}";
		resp.getWriter().write(response);
		resp.setStatus(200);

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {	
		try {
			// resources
			Map<String,Map<String,Object>> resMap = new HashMap<>();
			List<Resource> list = ra.getResources(Resource.class);
			for (Resource res: list) {
				if (res instanceof Schedule) continue;			
				String type = res.getResourceType().getName();
				if (!resMap.containsKey(type)) {
					Map<String,Object> subMap = new HashMap<>();
					subMap.put("short", res.getResourceType().getSimpleName());
					subMap.put("nr", 0);
					resMap.put(type, subMap);
				}
				Map<String,Object> subMap = resMap.get(type);
				subMap.put("nr",((int) subMap.get("nr"))+1);
			}
			// classes
			Map<String,Map<String,Object>> classMap = new HashMap<>();
			Map<String,Object> subMap = new HashMap<>();
			subMap.put("short",TestClass.class.getSimpleName());
			subMap.put("nr", objects.size());
			classMap.put(TestClass.class.getName(), subMap);
			// schedule values
			Map<String,Map<String,Object>> scheduleMap = new HashMap<>();
			Map<String,Object> subMap2 = new HashMap<>();
			subMap2.put("short",DefinitionSchedule.class.getSimpleName());
			subMap2.put("nr", schedule.getValues(0).size());
			scheduleMap.put(DefinitionSchedule.class.getName(), subMap2);			
			
			Map<String,Map<String,Map<String,Object>>> map = new HashMap<>();
			map.put("resourceTypes",resMap);
			map.put("classes",classMap);
			map.put("schedules",scheduleMap);
//			map.put("treeNr", getTreeNr());
			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writeValueAsString(map);
			json = json.substring(0, json.length()-1) + ",\"treeNr\":" + getTreeNr() +"}";
			resp.getWriter().write(json);
			resp.setStatus(200);
		} catch (Exception e) {
			System.out.println("Error caught in resource flash update: " + e.toString());
			e.printStackTrace();
			resp.getWriter().write(e.toString());
			resp.setStatus(200);
		}
	}
	
	private int getTreeNr() {
		int res = 0;
		List<? extends Resource> list = ra.getToplevelResources(TemperatureSensor.class);
		for (Resource testRes : list) {
			if (testRes.getName().startsWith(TREE_PREFIX)) 
				res++;
		}	
		return res;
	}
	
	private final Map<String,ResourceStructureListener> structureListeners = new ConcurrentHashMap<String, ResourceStructureListener>();
	
	private void createTree(Map<String,String> map, int nr, boolean addListeners) {
		if (map.containsKey("action") && map.get("action").equals("delete")) {
			List<? extends Resource> list = ra.getToplevelResources(TemperatureSensor.class);
			for (Resource testRes : list) {
				try {
					if (!testRes.getName().startsWith(TREE_PREFIX)) continue;
					removeListenersTransitive(testRes, true);
					testRes.delete();
				}
				catch (Exception e) {}
			}		
			System.gc();
		}	
		else if (map.containsKey("action") && map.get("action").equals("create")) {
			for ( int i=0;i<nr;i++ ) {
				createTree(addListeners);
			}
		}
	}
	
	private void createTree(boolean addListener) {
		TemperatureSensor ts= rm.createResource(TREE_PREFIX + (treeCounter++), TemperatureSensor.class);
		ts.location().room().name().<StringResource> create().setValue(TREE_PREFIX + treeCounter);
		ts.settings().alarmLimits().upperLimit().<TemperatureResource> create().setCelsius(31F);
		ts.settings().alarmLimits().lowerLimit().<TemperatureResource> create().setCelsius(16F);
		ts.physDim().height().<LengthResource> create().setValue(0.2F);
		ts.physDim().length().<LengthResource> create().setValue(0.15F);
		ts.name().<StringResource> create().setValue(TREE_PREFIX + treeCounter);
		ts.settings().setpoint().program().create();
		ts.settings().setpoint().setCelsius(21F);
		ts.settings().controlLimits().setAsReference(ts.settings().alarmLimits());
		ts.settings().targetRange().upperLimit().setAsReference(ts.settings().alarmLimits().upperLimit());
		ts.settings().targetRange().lowerLimit().setAsReference(ts.settings().alarmLimits().lowerLimit());
		ts.activate(true);
		if (addListener) {
			addListenerTransitive(ts, true);
		}
	}
	
	private void addListenerTransitive(Resource top, boolean transitive) {
		addListener(top);
		if (transitive) {
			for (Resource res: top.getSubResources(true)) {
				addListener(res);
			}
		}
	}
	
	private void addListener(Resource resource) {
		ResourceStructureListener rsl = new TestStructureListener();
		resource.addStructureListener(rsl);;
		structureListeners.put(resource.getPath(), rsl);
	}
	
	private void removeListenersTransitive(Resource top, boolean transitive) {
		ResourceStructureListener listener = structureListeners.remove(top.getPath());
		if (listener == null)
			return;
		top.removeStructureListener(listener);
		if (transitive) {
			for (Resource sub : top.getSubResources(true)) {
				removeListenersTransitive(sub,false);
			}
		}
	}

	private static final class TestStructureListener implements ResourceStructureListener {

		@Override
		public void resourceStructureChanged(ResourceStructureEvent arg0) {
		}
		
	}
	
}