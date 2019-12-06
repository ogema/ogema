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
package org.ogema.logdatavisualisation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.core.timeseries.InterpolationMode;

/**
 *
 * @author tgries
 */
public class DataGen {

	private final Map<String, Object> dataSets = new HashMap<>();
	private String currentGraphType;

	/**
	 * Add all Loggdata for the given Resource and its RecordedData into this DataGen.
	 * Make shure the RecordedData Object is the one from (respectively belongs to) 
	 * the given Resource Object.
	 * Its safe for null-arguments.
	 * @param recordedData
	 * @param resource 
	 */
	public void addRecordedDataForResource(RecordedData recordedData, Resource resource) {

		//handle "nothing to add" cases
		if (recordedData == null || resource == null || recordedData.getConfiguration() == null) {
			return;
		}

		//happens if recordedData is empty, so it is of type EmptyRecordedData and a call to
		//recordedData.getInterpolationMode() can throw this exception :-D
		try {

			final InterpolationMode mode = recordedData.getInterpolationMode();
			//add a dataset for this resource with resource-location as internal id ("lines" plott parameter, see flotchart)
			this.addDataSetSingle(resource.getLocation(), mode, resource.getName());

		} catch (UnsupportedOperationException e) {

			final StorageType type = recordedData.getConfiguration().getStorageType();

			//if the storage type is on_value_change or on_value_update plot as steps
			if (type == StorageType.ON_VALUE_CHANGED || type == StorageType.ON_VALUE_UPDATE) {

				this.addDataSetSingle(resource.getLocation(), InterpolationMode.STEPS, resource.getName());
			}
			else {

				//else plott as points
				this.addDataSetSingle(resource.getLocation(), InterpolationMode.NONE, resource.getName());
			}
		}

		//add the data for the specific id
		this.addDataFor(resource.getLocation(), recordedData.getValues(0, Long.MAX_VALUE));
	}

	/**
	 * Add all values of the given Schedule ( analog to addRecordedDataForResource() )
	 * @param schedule 
	 */
	public void addSchedule(Schedule schedule) {

		//handle "nothing to add" cases
		if (schedule == null) {
			return;
		}

		//happens if recordedData is empty, so it is of type EmptyRecordedData an a call to
		//recordedData.getInterpolationMode() can throw this exception :-D
		try {

			final InterpolationMode mode = schedule.getInterpolationMode();
			//add a dataset for this resource with resource-location as internal id
			this.addDataSetSingle(schedule.getLocation(), mode, schedule.getName());

		} catch (UnsupportedOperationException e) {

			this.addDataSetSingle(schedule.getLocation(), InterpolationMode.NONE, schedule.getName());
		}

		//finaly add the data for the specific id to "this"
		this.addDataFor(schedule.getLocation(), schedule.getValues(0, Long.MAX_VALUE));
	}

	/**
	 * helpermethod, that handles the copy process
	 * @param uniqueName
	 * @param values 
	 */
	private void addDataFor(String uniqueName, List<SampledValue> values) {
		//this array contains the plottable Datapoints for the given recordedData and Resource
		final ArrayList<Object> flottChartData = this.getDataArray(uniqueName);

		for (SampledValue value : values) {
			//each Datapoint in the flott-chart consists out of two Values (x and y Axis)
			final ArrayList<Object> xyPoint = new ArrayList<>();
			//add x-axis value (Time as an formated Date)
			xyPoint.add(new Date(value.getTimestamp()));
			//add y-axis value (effective resource value) as double (brings auto convert for booleans)
			xyPoint.add(value.getValue().getDoubleValue());
			//add the datapoint to the timeseries
			flottChartData.add(xyPoint);
		}
	}

	/**
	 * adds one default dataset to the dataset map
	 * @param uniqueName an unique name that is only used once. only used for intern purposes
	 * @param mode points, bars or lines
	 * @param label the name of the label on the right side of the graph
	 */
	public void addDataSetSingle(String uniqueName, InterpolationMode mode, String label) {

		Map<String, Object> data = new HashMap<>();
		Map<String, Boolean> type = new HashMap<>();
		ArrayList<ArrayList<Object>> array = new ArrayList<>();

                //if(mode == null || InterpolationMode.NONE == mode || InterpolationMode.NEAREST == mode){
                    //DEFAULT (at first)
                    currentGraphType = "points"; 
                    //type.put("steps", false); 
                //}
                // just change if explicitly LINEAR or STEPS is chosen
                if(InterpolationMode.LINEAR == mode){
                    currentGraphType = "lines"; 
                    type.put("steps", false); 
                }
                if(InterpolationMode.STEPS == mode){
                    currentGraphType = "lines"; 
                    type.put("steps", true); 
                }
                
		type.put("show", true);
		type.put("fill", false);

		data.put("label", label);
		data.put("data", array);
		data.put(currentGraphType, type);

		dataSets.put(uniqueName, data);
	}

	/**
	 * changes a single type value for a dataset
	 * @param uniqueName an unique name that is only used once. only used for intern purposes
	 * @param key steps, show, fill
	 * @param value the fitting value for the key
	 */
    @SuppressWarnings("unchecked")
	public void changeType(String uniqueName, String key, boolean value) {
		Map<String, Object> map = (Map<String, Object>) dataSets.get(uniqueName);
		Map<String, Boolean> typeMap = (Map<String, Boolean>) map.get(currentGraphType);
		typeMap.put(key, value);
	}

	/**
	 * gets the data array to add values to it
	 * @param uniqueName an unique name that is only used once. only used for intern purposes
	 * @return arraylist with data to a given uniquename
	 */
    @SuppressWarnings("unchecked")
	public ArrayList<Object> getDataArray(String uniqueName) {
		Map<String, Object> map = (Map<String, Object>) dataSets.get(uniqueName);
		return (ArrayList<Object>) map.get("data");
	}

	/**
	 * get the whole dataset structure for json usage
	 * @return the whole dataset structure
	 */
	public Map<String, Object> getDataSetMap() {
		return dataSets;
	}

	/**
	 * get the datasets in json format using jackson
	 * @return json format of the dataset
	 * @throws IOException 
	 */
	public String generateJSON() throws IOException {
		return new ObjectMapper().writeValueAsString(dataSets);
	}

}
