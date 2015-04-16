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
package org.ogema.driver.generic_zb;

public class Constants {

	/*
	 * Device IDs/Descriptions
	 * 
	 * @see ZigBee Home Automation Public Profile chapter 5.7
	 */
	public static final short COLOR_DIMMABLE_LIGHT = 0x0102;
	public static final String COLOR_DIMMABLE_LIGHT_STRING = "0102";
	public static final String HOME_AUTOMATION = "0104";
	public static final short MAINS_POWER_OUTLET = 0x0009;
	public static final String MAINS_POWER_OUTLET_STRING = "0009";

	// Channel address constants
	public static final String OFF_CMD_ADDRESS = "0006:COMMAND:00";
	public static final String ON_CMD_ADDRESS = "0006:COMMAND:01";
	public static final String TOGGLE_CMD_ADDRESS = "0006:COMMAND:02";
	public static final String ON_OFF_ATTR_ADDRESS = "0006:ATTRIBUTE:0000";
	public static final String MOVE_TO_LEVEL_CMD_ADDRESS = "0008:COMMAND:00";
	public static final String ONLEVEL_ATTR_ADDRESS = "0008:ATTRIBUTE:0000";
	public static final String MOVE_TO_COLOR_CMD_ADDRESS = "0300:COMMAND:07";

	public static final String DEVELCO_METER_VOLTAGE_ATTRIBUTE_ADDRESS = "0702:EXT:ATTRIBUTE:8101:1015";
	public static final String DEVELCO_METER_CURRENT_ATTRIBUTE_ADDRESS = "0702:EXT:ATTRIBUTE:8102:1015";
	public static final String DEVELCO_METER_FREQUENCY_ATTRIBUTE_ADDRESS = "0702:EXT:ATTRIBUTE:8103:1015";
	public static final String DEVELCO_ZHWR202_ID = "0001";
	public static final String DEVELCO_MANUFACTURER_PROFILE = "c0c9";

	public static final String PHILIPS_HUE_ID = "0200";
	public static final String PHILIPS_HUE_MANUFACTURER_PROFILE = "c05e";
}
