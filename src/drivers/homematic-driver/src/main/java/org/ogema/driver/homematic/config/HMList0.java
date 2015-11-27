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

public class HMList0 extends HMList {
	HMList list;

	public HMList0() {
		// ListEntry nl(int list, int register, int offsetBits, String name, int sizeB, float min, float max,String
		// conversion, float factor, String unit, boolean inReading, String help)
		nl(0, 1, 0, "burstRx", 8, 0, 255, "lookup1", -1.0f, "", true, "device reacts on Burst");
		nl(0, 10, 0, "pairCentral", 24, 0, 16777215, "hex", -1.0f, "", true, "pairing to central");
		nl(0, 13, 5, "backAtCharge", 1, 0, 1, "lookup1", -1.0f, "", true, "Backlight at Charge");
		nl(0, 13, 6, "backAtMotion", 1, 0, 1, "lookup1", -1.0f, "", true, "Backlight at motion");
		nl(0, 13, 7, "backAtKey", 1, 0, 1, "lookup1", -1.0f, "", true, "Backlight at keystroke");
		nl(0, 14, 0, "backOnTime", 8, 0, 255, "", -1.0f, "s", true, "Backlight On Time");
		nl(0, 14, 0, "stbyTime", 8, 1, 99, "", -1.0f, "s", true, "Standby Time");
		nl(0, 14, 0, "stbyTime2", 8, 1, 120, "", -1.0f, "s", true, "Standby Time");
		nl(0, 15, 0, "btnLock", 8, 0, 1, "lookup1", -1.0f, "", false, "Button Lock");
		nl(0, 16, 0, "sabotageMsg", 8, 0, 1, "lookup1", -1.0f, "", true, "enable sabotage message");
		nl(0, 17, 0, "cyclicInfoMsgDis", 8, 0, 255, "", -1.0f, "", true, "cyclic message");
		nl(0, 18, 0, "lowBatLimit", 8, 10, 12, "", 10f, "V", true, "low batterie limit,step .1V");
		nl(0, 18, 0, "lowBatLimitBA", 8, 5, 15, "", 10f, "V", false, "low batterie limit,step .1V");
		nl(0, 18, 0, "lowBatLimitBA2", 8, 0, 15, "", 10f, "V", false, "low batterie limit,step .1V");
		nl(0, 18, 0, "lowBatLimitBA3", 8, 0, 12, "", 10f, "V", false, "low batterie limit,step .1V");
		nl(0, 18, 0, "lowBatLimitFS", 8, 2, 3, "", 10f, "V", false, "low batterie limit,step .1V");
		nl(0, 18, 0, "lowBatLimitRT", 8, 2f, 2.5f, "", 10f, "V", false, "low batterie limit,step .1V");
		nl(0, 19, 0, "batDefectLimit", 8, 0.1f, 2f, "", 100f, "Ohm", true, "batterie defect detection");
		nl(0, 2, 7, "intKeyVisib", 1, 0, 1, "lookup2", -1.0f, "", false, "visibility of internal channel");
		nl(0, 20, 0, "transmDevTryMax", 8, 1, 10, "", -1.0f, "", false, "max message re-transmit");
		nl(0, 21, 0, "confBtnTime", 8, 1, 255, "lookup3", -1.0f, "min", false, "255=permanent");
		nl(0, 23, 0, "compMode", 1, 0, 1, "lookup1", -1.0f, "", true, "compatibility moden");
		nl(0, 24, 0, "localResDis", 8, 1, 1, "lookup4", -1.0f, "", false, "local reset disable");
		nl(0, 25, 0, "globalBtnLock", 8, 1, 255, "lookup4", -1.0f, "", false, "global button lock");
		nl(0, 26, 0, "modusBtnLock", 8, 1, 255, "lookup4", -1.0f, "", false, "mode button lock");
		nl(0, 27, 0, "paramSel", 8, 0, 4, "lookup5", -1.0f, "", true, "data transfered to peer");
		nl(0, 29, 0, "RS485IdleTime", 8, 0, 255, "", -1.0f, "s", false, "Idle Time");
		nl(0, 3, 0, "keypressSignal", 1, 0, 1, "lookup1", -1.0f, "", false, "Keypress beep");
		nl(0, 3, 4, "signal", 1, 0, 1, "lookup1", -1.0f, "", false, "Confirmation beep");
		nl(0, 3, 6, "signalTone", 2, 0, 3, "lookup6", -1.0f, "", false, "");
		nl(0, 32, 0, "wakeupDefChan", 8, 0, 20, "", -1.0f, "", false, "wakeup default channel");
		nl(0, 33, 0, "wakeupBehavior", 1, 0, 20, "lookup1", -1.0f, "", false, "wakeup behavior");
		nl(0, 4, 0, "brightness", 4, 0, 15, "", -1.0f, "", true, "Display brightness");
		nl(0, 5, 0, "backlOnTime", 6, 0, 5, "lookup57", 5.0f, "", false, "Backlight ontime[s]");// !!lit={0=0,5=1,10=2,15=3,20=4,25=5}},
		nl(0, 5, 6, "backlOnMode", 2, 0, 2, "lookup1", -1.0f, "", false, "Backlight mode");
		nl(0, 5, 6, "backlOnMode2", 2, 0, 1, "lookup1", -1.0f, "", false, "Backlight mode");
		nl(0, 5, 6, "ledMode", 2, 0, 1, "lookup1", -1.0f, "", false, "LED mode");
		nl(0, 7, 0, "language", 8, 0, 1, "lookup7", -1.0f, "", true, "Language");
		nl(0, 8, 0, "energyOpt", 8, 0, 127, "lookup8", 1f, "s", true, "energy Option: Duration of ilumination");
		nl(0, 9, 0, "cyclicInfoMsg", 8, 0, 1, "lookup9", -1.0f, "", true, "cyclic message");
	}
}
