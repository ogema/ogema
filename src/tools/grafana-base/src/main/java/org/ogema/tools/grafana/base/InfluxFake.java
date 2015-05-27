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
package org.ogema.tools.grafana.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.SimpleResource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.recordeddata.ReductionMode;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.model.actors.Actor;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.sensors.Sensor;
import org.ogema.model.sensors.TemperatureSensor;

/**
 * Instances of this class can serve as backend for a Grafana web page (http://grafana.org). <br>
 * For this purpose, it emulates a small part of the influxDB API (http://influxdb.com/docs/v0.8/api/reading_and_writing_data.html). 
 * Hence, only a small part of Grafana's features are supported.
 * <br><br>
 * Graphs can be generated from either {@link Schedule}s, log data of {@link SimpleResource}s, or {@link Sensor}s and {@link Actor}s. 
 * In the latter cases log data of the relevant simple subresources is displayed, e.g. {@link Sensor#reading() Sensor.reading()} or
 * {@link Actor#stateFeedback() Actor.stateFeedback()}. A list of the desired resource types must be passed to the constructor 
 * (variable {@link #panels}: keys = row names, values = list of resource types (full resource type names) = list of panels per row).
 * <br><br>
 * Note: the servlet URL SERVLET_URL must end in "/series"; the URL registered with Grafana must be stripped of the ending "/series". 
 * Indicate servlet URL in config.js and app/dashboards/sripted_async.js
 * <br>
 * jQuery API: get list of row names and corresponding panels (resource types), as well as updateInterval in ms:
		$.ajax({
	        type: "GET",
	        url: SERVLET_URL + "?parameters=",
	        contentType: "application/json"
	    })
	    .done(function (text, textStatus) {
	        var result = JSON.parse(text).parameters;
	        Object.keys(result.panels).forEach(function(rowName) {
	        	console.log("Resource types/panels in row " + rowName + ": ", result.panels[rowName]);
	        }              
	        var updateInterval = result.updateInterval;
	        console.log("Update interval ", updateInterval);
	    });
 * 
 * @author cnoelle
 *
 */
public class InfluxFake extends HttpServlet {

	private static final long serialVersionUID = 1L;
    private final List<ClassLoader> cls = new ArrayList<>();
	protected OgemaLogger logger;
    protected ApplicationManager am;
    protected ResourceManagement rm;
    protected ResourceAccess ra; 
    protected int MAX_SAMPLES; // maximum nr of samples to be sent; if nr is beyond, the set is downsampled. Default: 250
	protected long updateInterval; // update interval in ms. Disabled if <= 0. Default: -1 (disabled) 
	/**
	 * keys: row names; values: resource types (long names), corresponding to panels. 
	 * Example: for a single panel, just one map entry (key: row name) and one list entry (resource type) is needed 
	 * The full type name must be given, e.g. org.ogema.core.model.simple.FloatResource
	 */
	protected Map<String,Map> panels;
	// the field below is used for a hack to filter the resources shown by the resource type of their parent (typically applied to schedules)
	protected Map<String,Map<String,Class<? extends Resource>>> restrictions;
	protected boolean strictMode = false;
	protected DataType dataType = DataType.LOG_DATA; 
	protected List<String> loggedResources;
	
	public enum DataType {
		
		LOG_DATA(0),
		FORECAST(1),
		PROGRAM(2),
		ALL_SCHEDULES(3);	
		
		private int type;
		
		private DataType(int type) {
			this.type=type;
		}
		
		public int getDataType() {
			return type;
		}
		
		public static DataType getDataType(int type) {
			switch (type) {
			case 0: 
				return DataType.LOG_DATA;
			case 1:
				return DataType.FORECAST;
			case 2:
				return DataType.PROGRAM;
			case 3:
				return DataType.ALL_SCHEDULES;
			default:
				return null;					
			}
		}
		
	}

	/**************** Constructors ****************/
    
    public InfluxFake(ApplicationManager am, Map<String,Map> panels) { 
    	this(am,panels,-1,250);
    }
    
    public InfluxFake(ApplicationManager am, Map<String,Map> panels, int MAX_SAMPLES) {
    	this(am,panels,-1,MAX_SAMPLES);
    }
    
    public InfluxFake(ApplicationManager am, Map<String,Map> panels, long updateInterval) {
    	this(am,panels,updateInterval,250);
    }
    
    public InfluxFake(ApplicationManager am, Map<String,Map> panels, long updateInterval, int MAX_SAMPLES) {
    	this(am,panels,updateInterval,MAX_SAMPLES,DataType.LOG_DATA);
    }
    
    /**
     * 
     * @param panels: in general a Map<String,Map>, where the String provides a row ID, and the 
     * second Map sets the columns within one row (typically, the second map has only one entry).<br>
     * Allowed types for the second map: <br>
     * <ul>
     *  <li> Map<String,Class<? extends SingleValueResource>>, in which case log data for all resources of the given type is plotted
     *  <li> Map<String,Class<? extends Actor>>, in which case log data for the relevant value subresource is plotted
     *  <li> Map<String,Class<? extends Sensor>>, like Actor
     *  <li> Map<String,SingleValueResource>>, in which case only log data of the resources provided are plotted
     *  <li> Map<String,Schedule>>, in which case only the schedules provided are plotted
     * </ul>
     * @param updateInterval initial update intervla in ms; can be changed via UI
     * @param MAX_SAMPLES: maximum number of samples to display, per resource; if exceeded, the time series is downsampled
     * @param dataType: <br>
     * <ul>
     *   <li>0: plot log data (default)
     *   <li>1: plot attached forecast schedule
     *   <li>2: plot attached program schedule
     *   <li>3: plot all attached schedules 
     * </ul>
     */
    public InfluxFake(ApplicationManager am, Map<String,Map> panels, long updateInterval, int MAX_SAMPLES, DataType dataType) {
    	 this.am = am;
         this.logger = am.getLogger();
         this.rm = am.getResourceManagement();
         this.ra = am.getResourceAccess();
         this.panels = panels;
         this.updateInterval = updateInterval;
         this.MAX_SAMPLES = MAX_SAMPLES;        
         initializeClassLoaders();
         this.loggedResources = new ArrayList<String>();
         this.dataType = dataType;
         this.restrictions = new HashMap<String,Map<String,Class<? extends Resource>>>();
         //System.out.println("Created new InfluxFake!!!");
     }
    
    /**************** Methods to be overridden  **********/
    
    /**
     * Override this method in order to display an alternative name for the graph corresponding to resource;<br>
     * otherwise a default name is displayed <br>
     * (either resource location, or name of the device's physical location if resource is an instance of PhysicalElement 
     * and its location can be deduced from the resource tree)
     */
    protected String getAlternativeDisplayName(Resource resource) {
    	return null;
    }
    
    /**
     * Override this method in order to display an alternative name for the panel corresponding to resourceType;<br>
     * otherwise the name of the resource type is displayed 
     * TODO not implemented yet
     */
//    protected String getAlternativeDisplayName(Class<? extends Resource> resourceType) {
//    	return null;
//    }

    /**************** Public methods ****************/
    
    public void setStrictMode(boolean strictMode) {
    	this.strictMode = strictMode;
    }
    
    public int getMAX_SAMPLES() {
		return MAX_SAMPLES;
	}

	public void setMAX_SAMPLES(int mAX_SAMPLES) {
		MAX_SAMPLES = mAX_SAMPLES;
	}

	public long getUpdateInterval() {
		return updateInterval;
	}

	/**
	 * Set graphs' update interval
	 *  note: in order for this to have any effect, it must be ensured that the HTML page is updated accordingly
	 */
	public void setUpdateInterval(long updateInterval) {   
		this.updateInterval = updateInterval;
	}
	
    public Map<String, Map> getPanels() {
		return panels;
	}

    /**
     * Set panels to be displayed (keys: row names; list entries: panel names)
     *  note: in order for this to have any effect, it must be ensured that the HTML page is updated accordingly
     */
	public void setPanels(Map<String, Map> panels) {  
		this.panels = panels;
	}
	
	public List<String> getLoggedResources() {
		return new ArrayList<String>(loggedResources); // read only
	}

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public Map<String, Map<String,Class<? extends Resource>>> getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(Map<String, Map<String,Class<? extends Resource>>> restrictions) {
		this.restrictions = restrictions;
	}



    /**************** Servlet methods ****************/
    
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException { // TODO GET only changed entries and parameters (and accept a parameter 'full', to send all)
		Map<String,String[]> params = req.getParameterMap();
		//System.out.println("GET request to FakeInflux" + params.toString()); // FIXME
		JSONArray results = new JSONArray();
		JSONObject obj1 = new JSONObject();
		long startTime = 0;  // default: 1970
		long endTime =  4100000000000l;  // default: > 2100
		String query = null;
		String name = null;
		if (params.containsKey("q")) {
			String[] queryAux = params.get("q");
			query = queryAux[0];
			long[] interval = getQueryInterval(query);	
			startTime = interval[0];
			endTime = interval[1];
			if (query.contains("select") && query.contains("from")) {
				int idx = query.indexOf("from");
				String subStr1 = query.substring(idx+6);
				
				int idx1 = subStr1.indexOf("\"");
				name = subStr1.substring(0, idx1);
			}
			else {
				logger.debug("Received invalid query.");
				resp.getWriter().write(results.toString());
				resp.setStatus(500);
				return;
			}
			Resource res = ra.getResource(name);
			if (res==null) {
				logger.debug("Resource " + name + " not available");
				resp.getWriter().write(results.toString());
				resp.setStatus(200);   // important: do not return a server error in this case, since other Grafana graphs will collapse as well
				return;
			}
			ReadOnlyTimeSeries ts = getTimeseries(res, params);
			if (ts == null) {
				logger.debug("Received data request for non-loggable resource.");
				resp.getWriter().write(results.toString());
				resp.setStatus(200);   // important: do not return a server error in this case, since other Grafana graphs will collapse as well
				return;
			}
			Map<Long,Float> values = getValues(ts,startTime,endTime);
			Iterator<Entry<Long,Float>> it = values.entrySet().iterator();
			JSONArray pointsArray = new JSONArray();
			int counter = 0;
			while (it.hasNext()) {
				Entry<Long,Float> entry = it.next();
				JSONArray newPoint = new JSONArray();
				newPoint.put(entry.getKey());
				//newPoint.put(counter);   // sequence_number; required?
				newPoint.put(entry.getValue());
				pointsArray.put(newPoint);
				counter++;
			}			
			JSONArray columnsArray = new JSONArray();
			columnsArray.put("time");
			//columnsArray.put("sequence_number"); // required?
			columnsArray.put(name);
			String displayName = name;
			try {
				displayName =  getDeviceLocation(name);				
			} catch (Exception e) {}
			String alternativeName = getAlternativeDisplayName(res);
			if (alternativeName != null) {
				displayName = alternativeName;
			}
			obj1.put("columns", columnsArray);
			obj1.put("points", pointsArray);
			obj1.put("name", displayName);
				
		}
		else if (params.containsKey("resourceType")) {  
			// return a list of resources of that type
			String type = params.get("resourceType")[0].replaceFirst("interface ", "");  // note: this needs to be the full resource type name, e.g. org.ogema.core.model.simple.FloatResource
			Class clazz = getClass(type);
			if (clazz == null || !Resource.class.isAssignableFrom(clazz)) {
				logger.warn("Got a request for resources of type " + type + ", but could not access corresponding class");
				resp.getWriter().write(results.toString());
				resp.setStatus(500);
				return;
			}
			List<? extends Resource> ress = ra.getResources(clazz);
			
			Class<? extends Resource> parentClazz = null;
			if (params.containsKey("restrictions")) {
				String clzz = params.get("restrictions")[0];
				parentClazz = getClass(clzz);
			}
			loggedResources.clear();
			JSONArray resources = new JSONArray();
			for (Resource res: ress) {
				if (!isResourceLogged(res,params)) continue; 
				if (parentClazz != null && (res.isTopLevel() || !res.getParent().getResourceType().equals(parentClazz))) continue;
				if (strictMode) {
					Class <? extends Resource > clazz2 = res.getResourceType();
					if (!clazz.equals(clazz2)) continue;
				}
				resources.put(res.getLocation());
				loggedResources.add(res.getLocation());
			}
			obj1.put("loggedResources",resources);
		}
		else if (params.containsKey("row") && params.containsKey("panel")) {   
			//System.out.println("    Request for resources only " +params.toString());
			JSONArray resources = new JSONArray();
			String row = params.get("row")[0];
			String pl = params.get("panel")[0];
			Map<String,Object> list = panels.get(row);
			if (list == null) {
				logger.warn("Received request for non-existent row");
				resp.getWriter().write(results.toString());
				resp.setStatus(200);
				return;
			}
			Object obj = list.get(pl);
			List<? extends Resource> ress = (List<? extends Resource>) obj;
			//Class<? extends Resource> parentClass = restrictions.get(pl);
			for (Resource res: ress) {
				if (!isResourceLogged(res,params)) continue; 
				//if (parentClass != null && (res.isTopLevel() ||  !res.getParent().getResourceType().equals(parentClass))) continue; 
				resources.put(res.getLocation());
				loggedResources.add(res.getLocation());
			}
			obj1.put("loggedResources",resources);
		}
		if (params.containsKey("parameters")) {  // should only be sent in conjunction with resourceType request, not with actual data query
			JSONObject pr = new JSONObject();
			pr.put("updateInterval", updateInterval);
			//System.out.println("  Panels " + panels.toString());
			pr.put("panels", panels);		
			if (!restrictions.isEmpty()) {
				pr.put("restrictions",getStringRestrictionsMap());
			}
			obj1.put("parameters",pr);
		}
		results.put(obj1);	
		//System.out.println("GET response " + results.toString());  // FIXME
		resp.getWriter().write(results.toString());
		resp.setStatus(200);
	}
	
	private Map<Long,Float> getValues(ReadOnlyTimeSeries ts,long startTime,long endTime) {
		Map<Long,Float> vals = new LinkedHashMap<Long, Float>();
		List<SampledValue> svals = ts.getValues(startTime, endTime);
		int nrVals = svals.size();
		if (nrVals > MAX_SAMPLES && (ts instanceof RecordedData)) {  // FIXME why is this not implemented on ReadOnlyTimeSeries level?
			long indivStart = Math.max(svals.get(0).getTimestamp(),startTime);
			long indivEnd = Math.min(svals.get(svals.size()-1).getTimestamp(),endTime);			
			long stepSize = (long) Math.floor((double) (indivEnd - indivStart)/MAX_SAMPLES);
			svals = ((RecordedData) ts).getValues(indivStart, indivEnd, stepSize, ReductionMode.AVERAGE);
		}
		for (SampledValue sv : svals) {
			if (sv.getQuality() == Quality.BAD) {
				continue;
			}
			vals.put(sv.getTimestamp(), sv.getValue().getFloatValue());
		}
		return vals;
	}
	
	private boolean isResourceLogged(Resource res,Map<String,String[]> params) {
		ReadOnlyTimeSeries ts =  getTimeseries(res,params);
		//System.out.println("isResourceLogged(" + res.getLocation() + ") = " +ts);
		if (ts==null) return false;		
		if ((ts instanceof RecordedData) && ((RecordedData) ts).getConfiguration() != null) return true;
		if (ts instanceof Schedule && ((Schedule) ts).exists()) return true;
		return false;
	}
	
	/**
	 * 
	 * @param res can be either: 
	 * 		- a Schedule, in which case the Schedule is returned unchanged
	 * 		- a loggable SimpleResource, in which case the logged data is returned;
	 * 	 	- a Sensor, in which case log data for Sensor.reading is returned
	 * 		- an Actor, in which case log data for Actor.stateControl or Actor.stateFeedback (default) is returned, depending on the parameter "actorType", if present
	 * @return null, if res is not any of the above
	 */
	private ReadOnlyTimeSeries getTimeseries(Resource res,Map<String,String[]> params) {
		if (res instanceof Sensor) {
			Sensor sensor = (Sensor) res;
			res = sensor.reading();  // change res!
		}
		else if (res instanceof Actor) {  // two SimpleResources that might be logged
			if (params.containsKey("actorType")) {
				String type = params.get("actorType")[0];
				switch (type) {
				case "stateControl":
					res = ((Actor) res).stateControl();
					break;
				default:
					res = ((Actor) res).stateFeedback();
				}			
			} 
			else {
				res = ((Actor) res).stateFeedback();
			}
		}
		ReadOnlyTimeSeries ts = null;
		if (res instanceof FloatResource) {
			FloatResource fl = (FloatResource) res;
			switch (dataType.getDataType()) {
			case 0: 
				ts = fl.getHistoricalData();
				 break;
			case 1:
				ts = fl.forecast();
				break;
			case 2: 
				ts = fl.program();
				break;	
			}
			
		}
		else if (res instanceof IntegerResource) {
			IntegerResource in = (IntegerResource) res;
			switch (dataType.getDataType()) {
			case 0: 
				ts = in.getHistoricalData();
				 break;
			case 1:
				ts = in.forecast();
				break;
			case 2: 
				ts = in.program();
				break;	
			}
		}
		else if (res instanceof BooleanResource) {
			BooleanResource bo = (BooleanResource) res;
			switch (dataType.getDataType()) {
			case 0: 
				ts = bo.getHistoricalData();
				 break;
			case 1:
				ts = bo.forecast();
				break;
			case 2: 
				ts = bo.program();
				break;	
			}
		}
		else if (res instanceof ReadOnlyTimeSeries) {   // e.g. Schedules
			ts = (ReadOnlyTimeSeries) res;
		}
		return ts;
	}
		
	private long[] getQueryInterval(String query) {
//		System.out.println("Determining query interval from: " + query);
		long st=0l;
		long end=am.getFrameworkTime();
		try {
			 int idxStart = query.indexOf(" time > ");
			 int idxEnd = query.indexOf(" time < ");
			 if (idxStart > -1) {
				 try{
					 st = getTimestamp(query.substring(idxStart + 8),st);
				 } catch (Exception ee) {
					 logger.warn("Could not determine queried time interval, " + ee);
				 }
			 }
			 if (idxEnd > -1) {
				 end = getEndTimestamp(query.substring(idxEnd + 8),end);
			 }
			
		} catch (Exception e) {
			logger.warn("Could not determine queried time interval, " + e);
		}
		long[] interval = {st,end};
//		System.out.println("Query interval: [" + String.valueOf(interval[0]) + ", " +String.valueOf(interval[1]) + "]");
		return interval;
	}
	
	private long getTimestamp(String subquery, long defaultVal) {
		if (subquery == null || subquery.length() == 0) return defaultVal;
		if (subquery.length() > 7 && subquery.substring(0, 8).equals("now() - ")) {
			String timeStr = subquery.substring(8);
			Scanner scanner = new Scanner(timeStr);
			Scanner scan = scanner.useDelimiter("\\D");  // "not a number"
			int number = 0;
			try {
				number = scan.nextInt();
			} catch (Exception e) {
				scan.close();
				scanner.close();
				logger.error("Exception trying to parse query time from " + subquery + ", "  + e);
				return defaultVal;
			}
			scan.close();
			scanner.close();
			int idx = String.valueOf(number).length();
			char ch = timeStr.charAt(idx);
			long factor;
			switch (ch) {
			case 's':
				factor = 1000l; 
				break;
			case 'm': 
				factor = 1000l*60l;
				break;
			case 'h':
				factor = 1000l*60l*60l;
				break;
			case 'd':
				factor = 1000l*60l*60l*24l;
				break;
			default:
				return defaultVal;			
			}
			long ts = am.getFrameworkTime() - factor*((long) number);
			return ts;
		}
		else if (subquery.length() > 7 && subquery.substring(0, 8).equals("now() + ")) {
			String timeStr = subquery.substring(8);
			Scanner scanner = new Scanner(timeStr);
			Scanner scan = scanner.useDelimiter("\\D");  // "not a number"
			int number = 0;
			try {
				number = scan.nextInt();
			} catch (Exception e) {
				scan.close();
				scanner.close();
				logger.error("Exception trying to parse query time from " + subquery + ", "  + e);
				return defaultVal;
			}
			scan.close();
			scanner.close();
			int idx = String.valueOf(number).length();
			char ch = timeStr.charAt(idx);
			long factor;
			switch (ch) {
			case 's':
				factor = 1000l; 
				break;
			case 'm': 
				factor = 1000l*60l;
				break;
			case 'h':
				factor = 1000l*60l*60l;
				break;
			case 'd':
				factor = 1000l*60l*60l*24l;
				break;
			default:
				return defaultVal;			
			}
			long ts = am.getFrameworkTime() + factor*((long) number);
			return ts;
		}
		else if (subquery.substring(0,1).matches("\\d")) {
			Scanner scanner = new Scanner(subquery);
			Scanner scan = scanner.useDelimiter("\\D");  // "not a number"
			long number = 0;
			try {
				number = scan.nextLong();
			} catch (Exception e) {
				scan.close();
				scanner.close();
				logger.error("Exception trying to parse query time from " + subquery + ", "  + e);
				return defaultVal;
			}
			scan.close();
			scanner.close();
			// assume time in s
			//System.out.println("Calculated timestamp " + String.valueOf(number*1000l) + " from query " + subquery); // FIXME
			return number * 1000l;
		}
		else if (subquery.length() > 4 && subquery.substring(0, 5).equals("now()")) {
			return am.getFrameworkTime();
		}	
		return defaultVal; //FIXME
	}
	
	private long getEndTimestamp(String subquery, long defaultVal) {
		if (subquery == null || subquery.length() == 0) return defaultVal;
		if (subquery.length() > 7 && subquery.substring(0, 8).equals("now() - ")) {
			return getTimestamp(subquery, defaultVal);
		}
		else if (subquery.length() > 6 && subquery.substring(0,6).matches("\\d+")) {
			return getTimestamp(subquery, defaultVal);
		}
		else if (subquery.substring(0,1).matches("\\d")) {
			Scanner scanner = new Scanner(subquery);
			Scanner scan = scanner.useDelimiter("\\D");  // "not a number"
			int number = 0;
			try {
				number = scan.nextInt();
			} catch (Exception e) {
				scan.close();
				scanner.close();
				logger.error("Exception trying to parse query time from " + subquery + ", "  + e);
				return defaultVal;
			}
			scan.close();
			scanner.close();
			int idx = String.valueOf(number).length();
			char ch = subquery.charAt(idx);
			long factor;
			switch (ch) {
			case 's':
				factor = 1000l; 
				break;
			case 'm': 
				factor = 1000l*60l;
				break;
			case 'h':
				factor = 1000l*60l*60l;
				break;
			case 'd':
				factor = 1000l*60l*60l*24l;
				break;
			default:
				return defaultVal;			
			}
			long ts = am.getFrameworkTime() + factor*((long) number);
			return ts;
		}	
		return defaultVal; //FIXME
	}
	
	
	private int initializeClassLoaders() {
		// TODO add further class loaders for custom types
		// TODO: check: sufficient?
		// This way it works only for predefined OGEMA resource types.
		ClassLoader cl = Resource.class.getClassLoader();
		if (!cls.contains(cl)) cls.add(cl);
		cl = TemperatureSensor.class.getClassLoader();
		if (!cls.contains(cl)) cls.add(cl);
		cl = SimpleResource.class.getClassLoader();
		if (!cls.contains(cl)) cls.add(cl);
		cl = FloatResource.class.getClassLoader();
		if (!cls.contains(cl)) cls.add(cl);
		cl = Schedule.class.getClassLoader();
		if (!cls.contains(cl)) cls.add(cl);
		cl = ClassLoader.getSystemClassLoader();
		if (!cls.contains(cl)) cls.add(cl);
		return cls.size();
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
		//System.out.println("   returning " + clazz);
		return clazz;
	}
	
	private Map<String,Map<String,String>> getStringRestrictionsMap() {
		Map<String,Map<String,String>> map = new HashMap<>();		
		Iterator<Entry<String, Map<String,Class<? extends Resource>>>> it = restrictions.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String,Map<String,Class<? extends Resource>>> entr = it.next();
			Map<String,Class<? extends Resource>> submap = entr.getValue();
			Map<String,String> subStrMap = new HashMap<String, String>();
			Iterator<Entry<String, Class<? extends Resource>>> subit = submap.entrySet().iterator();
			while(subit.hasNext()) {
				Entry<String,Class<? extends Resource>> entry = subit.next();
				Class<? extends Resource> clzz = entry.getValue();
				subStrMap.put(entry.getKey(), entry.getValue().getName());		
			}
			map.put(entr.getKey(), subStrMap);
		}
		return map;
	}
	
	private String getDeviceLocation(String resLoc) {
		Resource res = ra.getResource(resLoc);
		if (res instanceof PhysicalElement) {
			PhysicalElement device = (PhysicalElement) res;
			// two possible ways to determine location of a device
			if (device.location().exists() && device.location().room().exists() && device.location().room().name().exists()) {
				return device.location().room().name().getValue();			
			}
			Resource parent = device.getParent();
			if (parent != null && parent.getSubResource("name") != null && (parent.getSubResource("name") instanceof StringResource) ) {
				return ((StringResource) parent.getSubResource("name")).getValue();
			}
		}
		else if (res instanceof SimpleResource) {   // check whether parent has a location
			if (!res.isTopLevel()) {
				return getDeviceLocation(res.getParent().getLocation());
			}
		}
		return null; // FIXME
	}
	
		

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			System.out.println("POST request to FakeInflux... unexpected!");
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
			String request = sb.toString();		
			resp.getWriter().write(request);
			resp.setStatus(200);
	} 

    


}
