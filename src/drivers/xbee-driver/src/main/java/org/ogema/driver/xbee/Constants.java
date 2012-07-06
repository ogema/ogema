/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.driver.xbee;

import java.util.Calendar;

public class Constants {

	public final static Calendar calendar = Calendar.getInstance();

	// InputHandler
	public final static byte EXPLICIT_RX_INDICATOR = (byte) 0x91;
	public final static byte AT_COMMAND_RESPONSE = (byte) 0x88;
	public final static byte REMOTE_COMMAND_RESPONSE = (byte) 0x97;
	public final static byte TX_STATUS = (byte) 0x8B;
	public final static short DEVICE_ANNCE = 0x0013; // Device announce
	public final static short ACTIVE_ENDPOINT_RESPONSE = (short) 0x8005;
	public final static short SIMPLE_DESCRIPTOR_RESPONSE = (short) 0x8004;
	public final static short NODE_DESCRIPTOR_RESPONSE = (short) 0x8002;
	public final static short COMPLEX_DESCRIPTOR_RESPONSE = (short) 0x8010;
	public final static short USER_DESCRIPTOR_RESPONSE = (short) 0x8011;
	public final static short IEEE_ADDR_RESPONSE = (short) 0x8001;
	public final static short MGMT_LQI_RESPONSE = (short) 0x8031;
	public final static short MATCH_DESCRIPTOR_REQUEST = (short) 0x0006;
	public static final short NETWORK_ADDRESS_RESPONSE = (short) 0x8000;
	public final static short ND_COMMAND = 0x4e44;
	public final static short SP_COMMAND = 0x5350;
	public final static short NI_COMMAND = 0x4E49;

	/**
	 * Reverses the byte order of a given byte array.
	 * 
	 * @param array
	 */
	public static void reverseByteArray(byte[] array) {
		if (array == null)
			return;
		int i = 0, j = array.length - 1;
		byte temp;
		while (j > i) {
			temp = array[j];
			array[j] = array[i];
			array[i] = temp;
			--j;
			++i;
		}
	}

	// Channel
	public final static String COMMAND = "COMMAND";
	public final static String ATTRIBUTE = "ATTRIBUTE";
	public final static String XBEE = "XBEE";
	public static final String MANUFACTURER_SPECIFIC = "EXT";

	// Cluster
	public final static boolean MANDATORY = true;
	public final static boolean OPTIONAL = false;
	public final static boolean READ_ONLY = true;
	public final static boolean READ_WRITE = false;
	public final static boolean ANALOG = true;
	public final static boolean DISCRETE = false;
	public static final byte READ_ATTRIBUTES = 0x00;
	public final static byte READ_ATTRIBUTES_RESPONSE = 0x01;
	public final static byte WRITE_ATTRIBUTES_RESPONSE = 0x04;
	public static final byte DEFAULT_RESPONSE = 0x0b;

	// SerialConnection
	public static final byte ESCAPE_BYTE = 0x20;
	public static final int MESSAGE_START = 3;
	public static final int MTU = 128;

	final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static String bytesToHex(Byte bt) {
		char[] hexChars = new char[2];
		int v = bt & 0xFF;
		hexChars[0] = hexArray[v >>> 4];
		hexChars[1] = hexArray[v & 0x0F];

		return new String(hexChars);
	}

	// ZigBee DataTypes (ZigBee Cluster Library Page 83 - 99)
	public final static byte NO_DATA = 0x00;
	public final static byte EIGHT_BIT_DATA = 0x08;
	public final static byte SIXTEEN_BIT_DATA = 0x09;
	public final static byte TWENTYFOUR_BIT_DATA = 0x0a;
	public final static byte THIRTYTWO_BIT_DATA = 0x0b;
	public final static byte FORTY_BIT_DATA = 0x0c;
	public final static byte FORTYEIGHT_BIT_DATA = 0x0d;
	public final static byte FIFTYSIX_BIT_DATA = 0x0e;
	public final static byte SIXTYFOUR_BIT_DATA = 0x0f;
	public final static byte BOOLEAN = 0x10;
	public final static byte EIGHT_BIT_BITMAP = 0x18;
	public final static byte SIXTEEN_BIT_BITMAP = 0x19;
	public final static byte TWENTYFOUR_BIT_BITMAP = 0x1a;
	public final static byte THIRTYTWO_BIT_BITMAP = 0x1b;
	public final static byte FORTY_BIT_BITMAP = 0x1c;
	public final static byte FORTYEIGHT_BIT_BITMAP = 0x1d;
	public final static byte FIFTYSIX_BIT_BITMAP = 0x1e;
	public final static byte SIXTYFOUR_BIT_BITMAP = 0x1f;
	public final static byte UNSIGNED_EIGHT_BIT_INTEGER = 0x20;
	public final static byte UNSIGNED_SIXTEEN_BIT_INTEGER = 0x21;
	public final static byte UNSIGNED_TWENTYFOUR_BIT_INTEGER = 0x22;
	public final static byte UNSIGNED_THIRTYTWO_BIT_INTEGER = 0x23;
	public final static byte UNSIGNED_FORTY_BIT_INTEGER = 0x24;
	public final static byte UNSIGNED_FORTYEIGHT_BIT_INTEGER = 0x25;
	public final static byte UNSIGNED_FIFTYSIX_BIT_INTEGER = 0x26;
	public final static byte UNSIGNED_SIXTYFOUR_BIT_INTEGER = 0x27;
	public final static byte SIGNED_EIGHT_BIT_INTEGER = 0x28;
	public final static byte SIGNED_SIXTEEN_BIT_INTEGER = 0x29;
	public final static byte SIGNED_TWENTYFOUR_BIT_INTEGER = 0x2a;
	public final static byte SIGNED_THIRTYTWO_BIT_INTEGER = 0x2b;
	public final static byte SIGNED_FORTY_BIT_INTEGER = 0x2c;
	public final static byte SIGNED_FORTYEIGHT_BIT_INTEGER = 0x2d;
	public final static byte SIGNED_FIFTYSIX_BIT_INTEGER = 0x2e;
	public final static byte SIGNED_SIXTYFOUR_BIT_INTEGER = 0x2f;
	public final static byte EIGHT_BIT_ENUMERATION = 0x30;
	public final static byte SIXTEEN_BIT_ENUMERATION = 0x31;
	public final static byte SEMI_PRECISION_FLOAT = 0x38;
	public final static byte SINGLE_PRECISION_FLOAT = 0x39;
	public final static byte DOUBLE_PRECISION_FLOAT = 0x3a;
	public final static byte OCTET_STRING = 0x41;
	public final static byte CHARACTER_STRING = 0x42;
	public final static byte LONG_OCTET_STRING = 0x43;
	public final static byte LONG_CHARACTER_STRING = 0x44;
	public final static byte ARRAY = 0x48;
	public final static byte STRUCTURE = 0x4c;
	public final static byte SET = 0x50;
	public final static byte BAG = 0x51;
	public final static byte TIME_OF_DAY = (byte) 0xe0;
	public final static byte DATE = (byte) 0xe1;
	public final static byte UTCTIME = (byte) 0xe2;
	public final static byte CLUSTER_ID = (byte) 0xe8;
	public final static byte ATTRIBUTE_ID = (byte) 0xe9;
	public final static byte BACNET_OID = (byte) 0xea;
	public final static byte IEEE_ADDRESS = (byte) 0xf0;
	public final static byte HUNDREDTWENTYEIGHT_BIT_SECURITY_KEY = (byte) 0xf1;
	public final static byte UNKOWN = (byte) 0xff;

	public static final long QUALITY_THRESHOLD = 60 * 1000;

	public static final byte STATUS_SUCCESS = 0x00;
	public static final byte STATUS_UNSUPPORTED_ATTRIBUTE = (byte) 0x86;
	public static final byte UNSUPPORTED_CLUSTER_COMMAND = (byte) 0x81;
	public static final byte NO_DESCRIPTOR = (byte) 0x89;

	public static final String STATIC_IF_NAME = "org.ogema.driver.xbee.portname";

	public static final byte STATUS_NOT_SUPPORTED = (byte) 0x84;

}
