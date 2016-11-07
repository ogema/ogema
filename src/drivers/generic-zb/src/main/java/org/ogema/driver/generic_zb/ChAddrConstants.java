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

public class ChAddrConstants {


	// Channel address constants for On/Off cluster attributes and commands
	public static final String OFF_CMD_ADDRESS = "0006:COMMAND:00";
	public static final String ON_CMD_ADDRESS = "0006:COMMAND:01";
	public static final String TOGGLE_CMD_ADDRESS = "0006:COMMAND:02";
	public static final String ON_OFF_ATTR_ADDRESS = "0006:ATTRIBUTE:0000";

	// Channel address constants for Level control cluster attributes and commands
	public static final String MOVE_TO_LEVEL_CMD_ADDRESS = "0008:COMMAND:00";
	public static final String ONLEVEL_ATTR_ADDRESS = "0008:ATTRIBUTE:0000";

	// Channel address constants for color control cluster attributes and commands
	public static final String MOVE_TO_COLOR_CMD_ADDRESS = "0300:COMMAND:07";

	// Channel address constants for manufacturer specific cluster attributes and commands
	public static final String DEVELCO_METER_VOLTAGE_ATTRIBUTE_ADDRESS = "0702:EXT:ATTRIBUTE:8101:1015";
	public static final String DEVELCO_METER_CURRENT_ATTRIBUTE_ADDRESS = "0702:EXT:ATTRIBUTE:8102:1015";
	public static final String DEVELCO_METER_FREQUENCY_ATTRIBUTE_ADDRESS = "0702:EXT:ATTRIBUTE:8103:1015";
}
