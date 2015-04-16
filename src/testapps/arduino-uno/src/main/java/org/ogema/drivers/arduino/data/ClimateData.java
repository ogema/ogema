package org.ogema.drivers.arduino.data;

import org.json.JSONObject;

public interface ClimateData {
	JSONObject getCurrentData(int id);
}
