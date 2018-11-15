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
package org.ogema.driver.hmhl;

public class Constants {

	/*
	 * Device IDs/Descriptions
	 */
	public static final String HM_TH_RES_NAME = "_THSensor_";
	public static final String HM_DOOR_CONTACT_RES_NAME = "_Door_Window_Contact_";	
	public static final String HM_WATER_DETECTOR_RES_NAME = "_Water_Detector_";
	public static final String HM_POWER_RES_NAME = "_PowerMeter_";
	public static final String HM_SWITCHPLUG_RES_NAME = "_Switch_";
	public static final String HM_CO2_RES_NAME = "_CO2_";
	public static final String HM_VALVE_RES_NAME = "_RadiatorValve_";
	public static final String HM_MOTION_RES_NAME = "_Motion_";
	public static final String HM_REMOTE_RES_NAME = "_Remote_";
	public static final String HM_SMOKE_RES_NAME = "_Smoke_";
	
	// Remote control button names
	public static final String BUTTON_OFF_SHORT = "shortPress_down_";
	public static final String BUTTON_OFF_LONG = "longPress_down_";
	public static final String BUTTON_ON_SHORT = "shortPress_up_";
	public static final String BUTTON_ON_LONG = "longPress_up_";

	/*
	 * Time Constants
	 */
	public static final int DEVICE_SCAN_WAITING_TIME = 23000;
}
