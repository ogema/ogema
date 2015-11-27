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
package org.ogema.driver.homematic.config;

public class HMList1 extends HMList {
	HMList list;

	public HMList1() {
		list = this;
		nl(1, 1, 0, "evtFltrPeriod", 4, 0.5f, 7.5f, "", 2f, "s", true, "event filter period");
		nl(1, 1, 4, "evtFltrNum", 4, 1f, 15f, "", -1.0f, "", true, "sensitivity - read each n-th puls");
		nl(1, 102, 0, "caseWidth", 16, 100f, 10000f, "", -1.0f, "cm", true, "case width");
		nl(1, 106, 0, "caseLength", 16, 100f, 10000f, "", -1.0f, "cm", true, "case length");
		nl(1, 108, 0, "meaLength", 16, 110f, 310f, "", -1.0f, "cm", true, "");
		nl(1, 11, 0, "driveDown", 16, 0f, 6000.0f, "", 10f, "s", true, "drive time up");
		nl(1, 110, 0, "useCustom", 8, 110f, 310f, "lookup4", -1.0f, "", true, "use custom");
		nl(1, 122, 0, "averaging", 8, 1f, 16f, "", -1.0f, "s", true, "averaging period");
		nl(1, 123, 0, "txMinDly", 7, 0f, 16f, "", -1.0f, "s", true, "min transmit delay");
		nl(1, 124, 0, "txThrPwr", 24, 0.00f, 3680f, "", 100f, "W", true, "threshold power");// lit={unused=0}}
		nl(1, 127, 0, "txThrCur", 16, 0f, 16000f, "", -1.0f, "mA", true, "threshold current");// lit={unused=0}}
		nl(1, 129, 0, "txThrVlt", 16, 0.0f, 230f, "", 10f, "V", true, "threshold voltage");// lit={unused=0}}
		nl(1, 13, 0, "driveUp", 16, 0f, 6000.0f, "", 10f, "s", true, "drive time up");
		nl(1, 131, 0, "txThrFrq", 8, 0.00f, 2.55f, "", 100f, "Hz", true, "threshold frequency");// lit={unused=0}}
		nl(1, 132, 0, "cndTxFalling", 1, 0f, 1f, "lookup1", -1.0f, "", true, "trigger if falling");
		nl(1, 132, 1, "cndTxRising", 1, 0f, 1f, "lookup1", -1.0f, "", true, "trigger if rising");
		nl(1, 132, 2, "cndTxCycBelow", 1, 0f, 1f, "lookup1", -1.0f, "", true,
				"cyclic trigger if level is below cndTxCycBelow");
		nl(1, 132, 3, "cndTxCycAbove", 1, 0f, 1f, "lookup1", -1.0f, "", true,
				"cyclic trigger if level is above cndTxDecAbove");
		nl(1, 133, 0, "cndTxDecAbove", 8, 0f, 255f, "", -1.0f, "", true, "level for cndTxCycAbove");
		nl(1, 134, 0, "cndTxDecBelow", 8, 0f, 255f, "", -1.0f, "", true, "level for cndTxCycBelow");
		nl(1, 135, 0, "cndTxThrhHi", 16, 0f, 3000f, "", -1.0f, "mV", false, "threshold high condition");
		nl(1, 135, 0, "txThrHiCur", 32, 0f, 16000f, "", -1.0f, "mA", true, "threshold low current");
		nl(1, 135, 0, "txThrHiFrq", 32, 48.72f, 51.27f, "", 100f, "Hz", true, "threshold low frequency");
		nl(1, 135, 0, "txThrHiPwr", 32, 0f, 3680f, "", 100f, "W", true, "threshold low power");
		nl(1, 135, 0, "txThrHiVlt", 32, 115f, 255f, "", 10f, "V", true, "threshold low voltage");
		nl(1, 139, 0, "cndTxThrhLo", 16, 0f, 3000f, "", -1.0f, "mV", false, "threshold high condition");
		nl(1, 139, 0, "txThrLoCur", 32, 0f, 16000f, "", -1.0f, "mA", true, "threshold high current");
		nl(1, 139, 0, "txThrLoFrq", 32, 48.72f, 51.27f, "", 100f, "Hz", true, "threshold high frequency");
		nl(1, 139, 0, "txThrLoPwr", 32, 0f, 3680f, "", 100f, "W", true, "threshold high power");
		nl(1, 139, 0, "txThrLoVlt", 32, 115f, 255f, "", 10f, "V", true, "threshold high voltage");
		nl(1, 143, 0, "highHoldTime", 8, 60f, 7620f, "fltCvT60", -1.0f, "s", false, "hold time on high state"); // TODO
		// implement
		// conversion
		// method
		nl(1, 145, 0, "evntRelFltTime", 8, 1f, 7620f, "fltCvT60", -1.0f, "s", false, "event filter release time ");// TODO
		// implement
		// conversion
		// method
		nl(1, 146, 0, "triggerMode", 8, 0f, 255f, "lookup11", -1.0f, "", false, "define type of event report ");
		nl(1, 149, 0, "mtrType", 8, 0f, 255f, "lookup12", -1.0f, "", false, "type of measurement");
		nl(1, 15, 0, "driveTurn", 8, 0.5f, 25.5f, "", 10f, "s", true,
				"engine uncharge - fhem min = 0.5s for protection. HM min= 0s (use regBulk if necessary)");
		nl(1, 150, 0, "mtrConstIr", 16, 1f, 65536f, "", -1.0f, "U/kWh", false, "constant IR");
		nl(1, 152, 0, "mtrConstGas", 16, 0001f, 65.536f, "", 1000f, "m3/I", false, "constant gas");
		nl(1, 154, 0, "mtrConstLed", 16, 1f, 65536f, "", -1.0f, "i/kWh", false, "constant led");
		nl(1, 156, 0, "mtrSensIr", 8, -99f, 99f, "", -1.0f, "%", false, "sensiblity IR");
		nl(1, 16, 0, "refRunCounter", 8, 0f, 255f, "", -1.0f, "", false, "reference run counter");
		nl(1, 18, 0, "loadErrCalib", 8, 0f, 255f, "", -1.0f, "", false, "Load Error Calibration");
		nl(1, 2, 0, "minInterval", 3, 0f, 4f, "lookup13", -1.0f, "", true, "minimum interval in sec");
		nl(1, 2, 3, "captInInterval", 1, 0f, 1f, "lookup1", -1.0f, "", true, "capture within interval");
		nl(1, 2, 4, "brightFilter", 4, 0f, 7f, "", -1.0f, "", true, "brightness filter - ignore light at night");
		nl(1, 20, 0, "holdTime", 8, 0f, 8.16f, "", 31.25f, "s", false, "Holdtime for door opening");
		nl(1, 21, 0, "holdPWM", 8, 0f, 255f, "", -1.0f, "", false, "Holdtime pulse wide modulation");
		nl(1, 22, 0, "setupDir", 1, 0f, 1f, "lookup14", -1.0f, "", false, "Rotation direction for locking");
		nl(1, 23, 0, "setupPosition", 8, 0f, 3000f, "", 0.06666f, "deg", true, "Rotation angle neutral position");
		nl(1, 24, 0, "angelOpen", 8, 0f, 3000f, "", 0.06666f, "deg", true, "Door opening angle");
		nl(1, 25, 0, "angelMax", 8, 0f, 3000f, "", 0.06666f, "deg", true, "Angle maximum");
		nl(1, 26, 0, "angelLocked", 8, 0f, 3000f, "", 0.06666f, "deg", true, "Angle Locked position");
		nl(1, 28, 0, "pullForce", 8, 0f, 100f, "", 2f, "%", true, "pull force level");
		nl(1, 29, 0, "pushForce", 8, 0f, 100f, "", 2f, "%", true, "push force level");
		nl(1, 30, 0, "tiltMax", 8, 0f, 255f, "", -1.0f, "", true, "maximum tilt level");
		nl(1, 31, 3, "ledFlashUnlocked", 1, 0f, 1f, "lookup1", -1.0f, "", false, "LED blinks when not locked");
		nl(1, 31, 6, "ledFlashLocked", 1, 0f, 1f, "lookup1", -1.0f, "", false, "LED blinks when locked");
		nl(1, 32, 0, "msgScdPosD", 2, 0f, 3f, "lookup15", -1.0f, "", false, "Message for position D");
		nl(1, 32, 2, "msgRhsPosC", 2, 0f, 3f, "lookup16", -1.0f, "", false, "Message for position C");
		nl(1, 32, 2, "msgScdPosC", 2, 0f, 3f, "lookup15", -1.0f, "", false, "Message for position C");
		nl(1, 32, 2, "msgWdsPosC", 2, 0f, 3f, "lookup17", -1.0f, "", false, "Message for position C");
		nl(1, 32, 4, "msgRhsPosB", 2, 0f, 3f, "lookup16", -1.0f, "", false, "Message for position B");
		nl(1, 32, 4, "msgScPosB", 2, 0f, 2f, "lookup17", -1.0f, "", false, "Message for position B");
		nl(1, 32, 4, "msgScdPosB", 2, 0f, 3f, "lookup15", -1.0f, "", false, "Message for position B");
		nl(1, 32, 4, "msgWdsPosB", 2, 0f, 3f, "lookup17", -1.0f, "", false, "Message for position B");
		nl(1, 32, 6, "msgRhsPosA", 2, 0f, 3f, "lookup16", -1.0f, "", false, "Message for position A");
		nl(1, 32, 6, "msgScPosA", 2, 0f, 2f, "lookup16", -1.0f, "", false, "Message for position A");
		nl(1, 32, 6, "msgScdPosA", 2, 0f, 1f, "lookup15", -1.0f, "", false, "Message for position A");
		nl(1, 32, 6, "msgWdsPosA", 2, 0f, 1f, "lookup17", -1.0f, "", false, "Message for position A");
		nl(1, 33, 0, "eventDlyTime", 8, 0f, 7620f, "fltCvT60", -1.0f, "s", true,
				"filters short events,causes reporting delay");// TODO implement conversion method
		nl(1, 34, 0, "ledOnTime", 8, 0f, 1.275f, "", 200f, "s", false, "LED ontime");
		nl(1, 35, 0, "eventFilterTime", 8, 0f, 7620f, "fltCvT60", -1.0f, "s", false, "event filter time");// TODO implement conversion method
		nl(1, 35, 0, "eventFilterTimeB", 8, 5f, 7620f, "fltCvT60", -1.0f, "s", false, "event filter time");// TODO implement conversion method
		nl(1, 35, 0, "evtFltrTime", 8, 600f, 1200f, "fltCvT", -1.0f, "s", false, "event filter time");// TODO implement conversion method
		nl(1, 36, 0, "seqPulse1", 8, 0f, 4.08f, "", 62.5f, "s", true,
				"Sequence Pulse. 0= unused,otherwise min= 0.032sec");
		nl(1, 37, 0, "seqPulse2", 8, 0f, 4.08f, "", 62.5f, "s", true,
				"Sequence Pulse. 0= unused,otherwise min= 0.032sec");
		nl(1, 38, 0, "seqPulse3", 8, 0f, 4.08f, "", 62.5f, "s", true,
				"Sequence Pulse. 0= unused,otherwise min= 0.032sec");
		nl(1, 39, 0, "seqPulse4", 8, 0f, 4.08f, "", 62.5f, "s", true,
				"Sequence Pulse. 0= unused,otherwise min= 0.032sec");
		nl(1, 4, 4, "longPress", 4, 0.3f, 1.8f, "m10s3", -1.0f, "s", false, "time to detect key long press");
		nl(1, 40, 0, "seqPulse5", 8, 0f, 4.08f, "", 62.5f, "s", true,
				"Sequence Pulse. 0= unused,otherwise min= 0.032sec");
		nl(1, 44, 0, "seqTolerance", 8, 0.016f, 4.08f, "", 62.5f, "s", true, "Sequence tolernace");
		nl(1, 45, 0, "msgShowTime", 8, 0.0f, 120f, "", 2f, "s", true, "Message show time(RC19). 0=always on");
		nl(1, 46, 0, "beepAtAlarm", 2, 0f, 3f, "lookup18", -1.0f, "", true, "Beep Alarm");
		nl(1, 46, 2, "beepAtService", 2, 0f, 3f, "lookup18", -1.0f, "", true, "Beep Service");
		nl(1, 46, 4, "beepAtInfo", 2, 0f, 3f, "lookup18", -1.0f, "", true, "Beep Info");
		nl(1, 47, 0, "backlAtAlarm", 2, 0f, 3f, "lookup19", -1.0f, "", true, "Backlight Alarm");
		nl(1, 47, 2, "backlAtService", 2, 0f, 3f, "lookup19", -1.0f, "", true, "Backlight Service");
		nl(1, 47, 4, "backlAtInfo", 2, 0f, 3f, "lookup19", -1.0f, "", true, "Backlight Info");
		nl(1, 48, 0, "transmitTryMax", 8, 1f, 10f, "", -1.0f, "", false, "max message re-transmit");
		nl(1, 49, 0, "loadAppearBehav", 2, 0f, 3f, "lookup20", -1.0f, "", true,
				"behavior on load appearence at restart");
		nl(1, 5, 0, "sunThresh", 8, 0f, 255f, "", -1.0f, "", true, "Sunshine threshold");
		nl(1, 50, 0, "ovrTempLvl", 8, 30f, 100f, "", -1.0f, "C", false, "overtemperatur level");
		nl(1, 51, 0, "fuseDelay", 8, 0f, 2.55f, "", 100f, "s", false, "fuse delay");
		nl(1, 52, 0, "redTempLvl", 8, 30f, 100f, "", -1.0f, "C", false, "reduced temperatur recover");
		nl(1, 53, 0, "redLvl", 8, 0f, 100f, "", 2f, "%", false, "reduced power level");
		nl(1, 6, 0, "stormUpThresh", 8, 0f, 255f, "", -1.0f, "", true, "Storm upper threshold");
		nl(1, 6, 0, "waterUppThr", 8, 0f, 256f, "", -1.0f, "", true, "water upper threshold");
		nl(1, 7, 0, "localResetDis", 8, 0f, 255f, "lookup4", -1.0f, "", true, "LocalReset disable");
		nl(1, 7, 0, "stormLowThresh", 8, 0f, 255f, "", -1.0f, "", true, "Storm lower threshold");
		nl(1, 7, 0, "waterlowThr", 8, 0f, 256f, "", -1.0f, "", true, "water lower threshold");
		nl(1, 8, 0, "sign", 1, 0f, 1f, "lookup1", -1.0f, "", true, "signature (AES)");
		nl(1, 86, 0, "powerUpAction", 1, 0f, 1f, "lookup1", -1.0f, "", true, "behavior on power up");
		nl(1, 87, 0, "statusInfoMinDly", 5, 0.5f, 15.5f, "", 2f, "s", false, "status message min delay");// lit={unused=0}}
		nl(1, 87, 5, "statusInfoRandom", 3, 0f, 7f, "", -1.0f, "s", false, "status message random delay");
		nl(1, 88, 0, "characteristic", 1, 0f, 1f, "lookup21", -1.0f, "", true, "");
		nl(1, 89, 0, "logicCombination", 5, 0f, 16f, "lookup22", -1.0f, "", true, "");
		nl(1, 9, 0, "dblPress", 4, 0f, 1.5f, "", 10f, "s", false, "time to detect double press");
		nl(1, 90, 0, "caseDesign", 8, 1f, 3f, "lookup23", -1.0f, "", true, "case desing");
		nl(1, 94, 0, "caseHigh", 16, 100f, 10000f, "", -1.0f, "cm", true, "case hight");
		nl(1, 98, 0, "fillLevel", 16, 100f, 300f, "", -1.0f, "cm", true, "fill level");
	}
}
