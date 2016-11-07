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
