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
package org.ogema.driver.homematic;

import java.util.HashMap;
import java.util.Map;

import org.ogema.driver.homematic.tools.Converter;

public class Constants {

	/*
	 * USB Device Configuration
	 */
	public static final short VENDOR_ID = 0x1b1f;
	public static final short PRODUCT_ID = (short) 0xc00f;

	public static final byte ENDPOINT_IN = (byte) 0x83;
	public static final byte ENDPOINT_OUT = (byte) 0x02;

	public static final int SIZE = 0x40;

	public static final byte CONFIGURATION = 1;
	public static final byte INTERFACE = 0;

	public static final int KEEPALIVETIME = 30000;
	public static final int USBCOM_TIMEOUT = 100000;
	public static final int CONNECT_WAIT_TIME = 30000;

	/*
	 * LowLevel Message Frames
	 */
	public static final byte[] M_K = Converter.hexStringToByteArray("4b");
	public static final byte[] M_C = Converter.hexStringToByteArray("43");
	public static final byte[] M_Y1 = Converter.hexStringToByteArray("5901");
	public static final byte[] M_Y2 = Converter.hexStringToByteArray("5902");
	public static final byte[] M_Y3 = Converter.hexStringToByteArray("5903");

	/*
	 * DeviceNames
	 */
	public static final Map<String, String> deviceNames;
	static {
		deviceNames = new HashMap<String, String>();
		deviceNames.put("00A8", "HM-WDS30-OT2-SM");
		deviceNames.put("0068", "HM-LC-Dim1TPBU-FM");
		deviceNames.put("0078", "HM-Dis-TD-T");
		deviceNames.put("002B", "HM-WS550Tech");
		deviceNames.put("0040", "HM-WDS100-C6-O");
		deviceNames.put("0050", "HM-SEC-SFA-SM");
		deviceNames.put("0000", "HM-LC-Sw1-Pl-DN-R2");
		deviceNames.put("0033", "KS550LC");
		deviceNames.put("002C", "KS550TECH");
		deviceNames.put("0038", "HM-RC-19-B");
		deviceNames.put("0088", "Schueco_263-132");
		deviceNames.put("004E", "HM-LC-DDC1-PCB");
		deviceNames.put("00DE", "HM-ES-TX-WM");
		deviceNames.put("007D", "ROTO_ZEL-STG-RM-WT-2");
		deviceNames.put("00D9", "HM-MOD-Em-8");
		deviceNames.put("005D", "HM-Sen-MDIR-O");
		deviceNames.put("0046", "HM-SWI-3-FM");
		deviceNames.put("0019", "HM-SEC-KEY");
		deviceNames.put("0096", "WDF-solar");
		deviceNames.put("004A", "HM-SEC-MDIR");
		deviceNames.put("002E", "HM-LC-DIM2L-SM");
		deviceNames.put("0028", "HM-SEC-WIN");
		deviceNames.put("0048", "IS-WDS-TH-OD-S-R3");
		deviceNames.put("0054", "DORMA_RC-H");
		deviceNames.put("0061", "HM-LC-SW4-DR");
		deviceNames.put("00B4", "HM-LC-Dim1T-Pl-3");
		deviceNames.put("00C1", "Motion Detector HM-Sen-MDIR-O-2");
		deviceNames.put("00D3", "HM-Dis-WM55");
		deviceNames.put("006D", "HM-OU-LED16");
		deviceNames.put("00DB", "HM-Sen-MDIR-WM55");
		deviceNames.put("0080", "ROTO_ZEL-STG-RM-HS-4");
		deviceNames.put("00BD", "HM-CC-RT-DN-BoM");
		deviceNames.put("001B", "HM-RC-SEC3");
		deviceNames.put("0004", "HM-LC-SW1-FM");
		deviceNames.put("003E", "Temperature Outdoor HM-WDS30-T-O");
		deviceNames.put("008C", "Schueco_263-131");
		deviceNames.put("004F", "IR Motion Detector HM-SEN-MDIR-SM");
		deviceNames.put("000B", "HM-WS550");
		deviceNames.put("0014", "HM-LC-SW1-SM-ATMEGA168");
		deviceNames.put("00BF", "HM-PB-2-FM");
		deviceNames.put("0070", "HM-LC-Dim2L-SM-644");
		deviceNames.put("0091", "Schueco_263-167");
		deviceNames.put("0008", "HM-RC-4");
		deviceNames.put("007A", "ROTO_ZEL-STG-RM-FSA");
		deviceNames.put("003F", "HM-WDS40-TH-I");
		deviceNames.put("0065", "DORMA_BRC-H");
		deviceNames.put("0059", "HM-LC-DIM1T-FM");
		deviceNames.put("0094", "IS-WDS-TH-OD-S-R3");
		deviceNames.put("00B1", "HM-SEC-SC-2");
		deviceNames.put("004B", "HM-Sec-Cen");
		deviceNames.put("006B", "HM-PB-2-WM55");
		deviceNames.put("0052", "HM-LC-SW2-PB-FM");
		deviceNames.put("0035", "HM-PB-4-WM");
		deviceNames.put("00CE", "HM-LC-Sw4-PCB-2");
		deviceNames.put("0045", "HM-SEC-WDS");
		deviceNames.put("00B6", "HM-LC-Dim1TPBU-FM-2");
		deviceNames.put("00C8", "HM-LC-Sw1-Pl-3");
		deviceNames.put("003C", "HM-WDS20-TH-O");
		deviceNames.put("003A", "HM-CC-VD");
		deviceNames.put("0071", "HM-LC-Dim1T-Pl-644");
		deviceNames.put("002F", "HM-SEC-SC");
		deviceNames.put("00D7", "HM-ES-PMSw1-Pl-DN-R1");
		deviceNames.put("00A6", "HM-RC-Key4-2");
		deviceNames.put("00D2", "HM-LC-Bl1-FM-2");
		deviceNames.put("0064", "DORMA_atent");
		deviceNames.put("00D0", "HM-LC-Sw4-DR-2");
		deviceNames.put("008B", "Schueco_263-130");
		deviceNames.put("00A9", "HM-PB-6-WM55");
		deviceNames.put("0095", "Radiator Valve HM-CC-RT-DN");
		deviceNames.put("0007", "KS550");
		deviceNames.put("00A1", "HM-LC-SW1-PL2");
		deviceNames.put("000D", "ASH550");
		deviceNames.put("008E", "Schueco_263-155");
		deviceNames.put("0015", "HM-LC-SW4-SM-ATMEGA168");
		deviceNames.put("0013", "HM-LC-DIM1L-PL");
		deviceNames.put("00CC", "HM-LC-Sw2-DR-2");
		deviceNames.put("00C2", "HM-PB-2-WM55-2");
		deviceNames.put("0000", "HM-ES-PMSw1-Pl-DN-R3");
		deviceNames.put("0073", "HM-LC-Dim1T-FM-644");
		deviceNames.put("00A3", "HM-LC-Dim1L-Pl-2");
		deviceNames.put("0087", "Schueco_263-147");
		deviceNames.put("0067", "HM-LC-Dim1PWM-CV");
		deviceNames.put("0058", "HM-LC-DIM1T-CV");
		deviceNames.put("009B", "Schueco_263-xxx");
		deviceNames.put("0044", "HM-SEN-EP");
		deviceNames.put("0037", "HM-RC-19");
		deviceNames.put("00BB", "HM-LC-Dim2T-SM-2");
		deviceNames.put("001D", "HM-RC-KEY3");
		deviceNames.put("0000", "HM-LC-Sw1-Pl-DN-R4");
		deviceNames.put("0083", "Roto_ZEL-STG-RM-FSS-UP3");
		deviceNames.put("0042", "Smoke Detector HM-SEC-SD");
		deviceNames.put("0011", "HM-LC-SW1-PL");
		deviceNames.put("007F", "ROTO_ZEL-STG-RM-FST-UP4");
		deviceNames.put("00A4", "HM-LC-Dim1T-Pl-2");
		deviceNames.put("0049", "KFM-Display");
		deviceNames.put("0002", "HM-LC-SW1-SM");
		deviceNames.put("005C", "HM-OU-CF-PL");
		deviceNames.put("00CF", "HM-LC-Sw4-WM-2");
		deviceNames.put("006E", "HM-LC-Dim1L-CV-644");
		deviceNames.put("0030", "HM-SEC-RHS");
		deviceNames.put("00B7", "HM-LC-Dim1L-CV-2");
		deviceNames.put("0093", "Schueco_263-158");
		deviceNames.put("0009", "HM-LC-SW2-FM");
		deviceNames.put("003D", "Temperature/Humidity HM-WDS10-TH-O");
		deviceNames.put("0062", "HM-LC-SW2-DR");
		deviceNames.put("0041", "HM-WDC7000");
		deviceNames.put("0076", "HM-Sys-sRP-Pl");
		deviceNames.put("00AC", "Switchbox HM-ES-PMSw1-Pl");
		deviceNames.put("0000", "HM-ES-PMSw1-Pl-DN-R5");
		deviceNames.put("004C", "HM-RC-12-SW");
		deviceNames.put("0069", "HM-LC-Sw1PBU-FM");
		deviceNames.put("0003", "HM-LC-SW4-SM");
		deviceNames.put("00C9", "HM-LC-Sw1-SM-2");
		deviceNames.put("0089", "Schueco_263-134");
		deviceNames.put("0066", "HM-LC-SW4-WM");
		deviceNames.put("0039", "HM-CC-TC");
		deviceNames.put("0086", "Schueco_263-146");
		deviceNames.put("00B9", "HM-LC-Dim1T-CV-2");
		deviceNames.put("0047", "KFM-Sensor");
		deviceNames.put("0079", "ROTO_ZEL-STG-RM-FWT");
		deviceNames.put("0090", "Schueco_263-162");
		deviceNames.put("001F", "KS888");
		deviceNames.put("001A", "HM-RC-P1");
		deviceNames.put("0072", "HM-LC-Dim1T-CV-644");
		deviceNames.put("00A5", "HM-RC-Sec4-2");
		deviceNames.put("00A0", "Remote Control HM-RC-4-2");
		deviceNames.put("0092", "Schueco_263-144");
		deviceNames.put("003B", "HM-RC-4-B");
		deviceNames.put("001C", "HM-RC-SEC3-B");
		deviceNames.put("000F", "S550IA");
		deviceNames.put("0029", "HM-RC-12");
		deviceNames.put("008D", "Schueco_263-135");
		deviceNames.put("006C", "HM-LC-SW1-BA-PCB");
		deviceNames.put("00CB", "HM-LC-Sw2-FM-2");
		deviceNames.put("00CD", "HM-LC-Sw4-SM-2");
		deviceNames.put("00A7", "HM-Sen-RD-O");
		deviceNames.put("002A", "HM-RC-12-B");
		deviceNames.put("0075", "HM-OU-CFM-PL");
		deviceNames.put("0081", "ROTO_ZEL-STG-RM-FDK");
		deviceNames.put("8001", "PS-switch");
		deviceNames.put("009F", "HM-Sen-Wa-Od");
		deviceNames.put("0034", "HM-PBI-4-FM");
		deviceNames.put("006A", "HM-LC-Bl1PBU-FM");
		deviceNames.put("0032", "HM-WS550LCW");
		deviceNames.put("007C", "ROTO_ZEL-STG-RM-FZS");
		deviceNames.put("00AF", "HM-OU-CM-PCB");
		deviceNames.put("0053", "HM-LC-BL1-PB-FM");
		deviceNames.put("002D", "HM-LC-SW4-PCB");
		deviceNames.put("007E", "ROTO_ZEL-STG-RM-DWT-10");
		deviceNames.put("00BE", "HM-MOD-Re-8");
		deviceNames.put("0001", "HM-LC-SW1-PL-OM54");
		deviceNames.put("00DA", "HM-RC-8");
		deviceNames.put("00C0", "HM-SEC-MDIR-2");
		deviceNames.put("00DD", "HM-PB-4DIS-WM-2");
		deviceNames.put("0057", "HM-LC-DIM1T-PL");
		deviceNames.put("000E", "ASH550I");
		deviceNames.put("0012", "HM-LC-DIM1L-CV");
		deviceNames.put("0000", "HM-LC-Sw1-Pl-DN-R5");
		deviceNames.put("8002", "PS-Th-Sens");
		deviceNames.put("00B8", "HM-LC-Dim2L-SM-2");
		deviceNames.put("0027", "HM-SEC-KEY-O");
		deviceNames.put("0082", "Roto_ZEL-STG-RM-FFK");
		deviceNames.put("0056", "CO2 Detector HM-CC-SCD");
		deviceNames.put("004D", "HM-RC-19-SW");
		deviceNames.put("0051", "HM-LC-SW1-PB-FM");
		deviceNames.put("00CA", "HM-LC-Sw1-FM-2");
		deviceNames.put("0043", "HM-SEC-TIS");
		deviceNames.put("0022", "WS888");
		deviceNames.put("00B2", "Water Detector HM-SEC-WDS-2");
		deviceNames.put("0016", "HM-LC-DIM2L-CV");
		deviceNames.put("0074", "HM-LC-Dim2T-SM");
		deviceNames.put("008A", "Schueco_263-133");
		deviceNames.put("00AD", "HM-TC-IT-WM-W-EU");
		deviceNames.put("00DC", "HM-Sen-DB-PCB");
		deviceNames.put("00D1", "HM-LC-Bl1-SM-2");
		deviceNames.put("008F", "Schueco_263-145");
		deviceNames.put("0018", "CMM");
		deviceNames.put("0031", "HM-WS550LCB");
		deviceNames.put("005A", "HM-LC-DIM2T-SM");
		deviceNames.put("0084", "Schueco_263-160");
		deviceNames.put("0060", "HM-PB-4DIS-WM");
		deviceNames.put("0036", "HM-PB-2-WM");
		deviceNames.put("00B5", "HM-LC-Dim1PWM-CV-2");
		deviceNames.put("006F", "HM-LC-Dim1L-Pl-644");
		deviceNames.put("000A", "HM-LC-SW2-SM");
		deviceNames.put("0000", "HM-ES-PMSw1-Pl-DN-R4");
		deviceNames.put("00BC", "HM-WDS40-TH-I-2");
		deviceNames.put("00BA", "HM-LC-Dim1T-FM-2");
		deviceNames.put("0005", "HM-LC-BL1-FM");
		deviceNames.put("00B3", "HM-LC-Dim1L-Pl-3");
		deviceNames.put("005F", "HM-SCI-3-FM");
		deviceNames.put("00AB", "HM-LC-SW4-BA-PCB");
		deviceNames.put("00A2", "ROTO_ZEL-STG-RM-FZS-2");
		deviceNames.put("00D8", "HM-LC-Sw1-Pl-DN-R1");
		deviceNames.put("0006", "HM-LC-BL1-SM");
		deviceNames.put("0026", "HM-SEC-KEY-S");
		deviceNames.put("007B", "ROTO_ZEL-STG-RM-FEP-230V");
		deviceNames.put("FFF0", "CCU-FHEM");
		deviceNames.put("0000", "HM-LC-Sw1-Pl-DN-R3");
		deviceNames.put("00C7", "HM-SEC-SCo");
		deviceNames.put("001E", "HM-RC-KEY3-B");

	}

}
