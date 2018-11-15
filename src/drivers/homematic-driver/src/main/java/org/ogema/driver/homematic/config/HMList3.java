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
package org.ogema.driver.homematic.config;

public class HMList3 extends HMList {
	HMList list;

	public HMList3() {
		list = this;
		nl(3, 1, 0, "CtRampOn", 4, 0f, 5f, "lookup24", -1.0f, "", false, "Jmp on condition from rampOn");
		nl(3, 1, 4, "CtRampOff", 4, 0f, 5f, "lookup24", -1.0f, "", false, "Jmp on condition from rampOff");
		nl(3, 10, 0, "ActionType", 2, 0f, 3f, "lookup25", -1.0f, "", true, "");
		nl(3, 10, 0, "ActionTypeDim", 4, 0f, 8f, "lookup25", -1.0f, "", true, "");
		nl(3, 10, 6, "OffTimeMode", 1, 0f, 1f, "lookup26", -1.0f, "", false, "off time meant absolut or at least");
		nl(3, 10, 7, "OnTimeMode", 1, 0f, 1f, "lookup26", -1.0f, "", false, "on time meant absolut or at least");
		nl(3, 11, 0, "BlJtOn", 4, 0f, 9f, "lookup27", -1.0f, "", false, "Jump from on");
		nl(3, 11, 0, "DimJtOn", 4, 0f, 6f, "lookup28", -1.0f, "", false, "Jump from on");
		nl(3, 11, 0, "KeyJtOn", 4, 0f, 7f, "lookup29", -1.0f, "", false, "Jump from on");
		nl(3, 11, 0, "SwJtOn", 4, 0f, 6f, "lookup28", -1.0f, "", false, "Jump from on");
		nl(3, 11, 0, "WinJtOn", 4, 0f, 9f, "lookup30", -1.0f, "", false, "Jump from off");
		nl(3, 11, 0, "ttJtOn", 4, 0f, 6f, "lookup31", -1.0f, "", false, "Jump from on");
		nl(3, 11, 4, "BlJtOff", 4, 0f, 9f, "lookup27", -1.0f, "", false, "Jump from off");
		nl(3, 11, 4, "DimJtOff", 4, 0f, 6f, "lookup28", -1.0f, "", false, "Jump from off");
		nl(3, 11, 4, "KeyJtOff", 4, 0f, 7f, "lookup29", -1.0f, "", false, "Jump from off");
		nl(3, 11, 4, "SwJtOff", 4, 0f, 6f, "lookup28", -1.0f, "", false, "Jump from off");
		nl(3, 11, 4, "WinJtOff", 4, 0f, 9f, "lookup30", -1.0f, "", false, "Jump from off");
		nl(3, 11, 4, "ttJtOff", 4, 0f, 6f, "lookup31", -1.0f, "", false, "Jump from off");
		nl(3, 12, 0, "BlJtDlyOn", 4, 0f, 9f, "lookup27", -1.0f, "", false, "Jump from delayOn");
		nl(3, 12, 0, "DimJtDlyOn", 4, 0f, 6f, "lookup28", -1.0f, "", false, "Jump from delayOn");
		nl(3, 12, 0, "SwJtDlyOn", 4, 0f, 6f, "lookup28", -1.0f, "", false, "Jump from delayOn");
		nl(3, 12, 4, "BlJtDlyOff", 4, 0f, 9f, "lookup27", -1.0f, "", false, "Jump from delayOff");
		nl(3, 12, 4, "DimJtDlyOff", 4, 0f, 6f, "lookup28", -1.0f, "", false, "Jump from delayOff");
		nl(3, 12, 4, "SwJtDlyOff", 4, 0f, 6f, "lookup28", -1.0f, "", false, "Jump from delayOff");
		nl(3, 13, 0, "BlJtRampOn", 4, 0f, 9f, "lookup27", -1.0f, "", false, "Jump from rampOn");
		nl(3, 13, 0, "DimJtRampOn", 4, 0f, 6f, "lookup28", -1.0f, "", false, "Jump from rampOn");
		nl(3, 13, 0, "WinJtRampOn", 4, 0f, 9f, "lookup30", -1.0f, "", false, "Jump from off");
		nl(3, 13, 4, "BlJtRampOff", 4, 0f, 9f, "lookup27", -1.0f, "", false, "Jump from rampOff");
		nl(3, 13, 4, "DimJtRampOff", 4, 0f, 6f, "lookup28", -1.0f, "", false, "Jump from rampOff");
		nl(3, 13, 4, "WinJtRampOff", 4, 0f, 9f, "lookup30", -1.0f, "", false, "Jump from off");
		nl(3, 138, 5, "lgMultiExec", 1, 0f, 1f, "lookup1", -1.0f, "", false,
				"multiple execution per repeat of long trigger");
		nl(3, 14, 5, "OffDlyBlink", 1, 0f, 1f, "lookup1", -1.0f, "", false, "blink when in off delay");
		nl(3, 14, 6, "OnLvlPrio", 1, 0f, 1f, "lookup32", -1.0f, "", false, "");
		nl(3, 14, 7, "OnDlyMode", 1, 0f, 1f, "lookup33", -1.0f, "", false, "");
		nl(3, 15, 0, "OffLevel", 8, 0f, 100f, "", 2f, "%", false, "PowerLevel off");
		nl(3, 15, 0, "OffLevelKm", 8, 0f, 127.5f, "", 2f, "%", false, "OnLevel 127.5=locked");
		nl(3, 16, 0, "OnMinLevel", 8, 0f, 100f, "", 2f, "%", false, "minimum PowerLevel");
		nl(3, 17, 0, "OnLevel", 8, 0f, 100.5f, "", 2f, "%", true, "PowerLevel on"); // lit={oldLevel=100.5}},
		nl(3, 17, 0, "OnLevelKm", 8, 0f, 127.5f, "", 2f, "%", false, "OnLevel 127.5=locked");
		nl(3, 18, 0, "RampSstep", 8, 0f, 100f, "", 2f, "%", false, "rampStartStep");
		nl(3, 19, 0, "RampOnTime", 8, 0f, 111600f, "fltCvT", -1.0f, "s", false, "rampOnTime");
		nl(3, 2, 0, "CtDlyOn", 4, 0f, 5f, "lookup24", -1.0f, "", false, "Jmp on condition from delayOn");
		nl(3, 2, 4, "CtDlyOff", 4, 0f, 5f, "lookup24", -1.0f, "", false, "Jmp on condition from delayOff");
		nl(3, 20, 0, "RampOffTime", 8, 0f, 111600f, "fltCvT", -1.0f, "s", false, "rampOffTime");// TODO implement
		// conversion method
		nl(3, 21, 0, "DimMinLvl", 8, 0f, 100f, "", 2f, "%", false, "dimMinLevel");
		nl(3, 22, 0, "DimMaxLvl", 8, 0f, 100f, "", 2f, "%", false, "dimMaxLevel");
		nl(3, 23, 0, "DimStep", 8, 0f, 100f, "", 2f, "%", false, "dimStep");
		nl(3, 24, 0, "OffDlyStep", 8, 0.1f, 25.6f, "", 2f, "%", false, "off delay step if blink is active");
		nl(3, 25, 0, "OffDlyNewTime", 8, 0.1f, 25.6f, "", 10f, "s", false, "off delay blink time for low");
		nl(3, 26, 0, "OffDlyOldTime", 8, 0.1f, 25.6f, "", 10f, "s", false, "off delay blink time for high");
		nl(3, 28, 0, "CtRefOn", 4, 0f, 5f, "lookup24", -1.0f, "", false, "Jmp on condition from refOn");
		nl(3, 28, 4, "CtRefOff", 4, 0f, 5f, "lookup24", -1.0f, "", false, "Jmp on condition from refOff");
		nl(3, 29, 0, "MaxTimeF", 8, 0f, 25.5f, "", 10f, "s", false, "max time first direction.");// lit={unused=25.5}},
		nl(3, 3, 0, "CtOn", 4, 0f, 5f, "lookup24", -1.0f, "", false, "Jmp on condition from on");
		nl(3, 3, 4, "CtOff", 4, 0f, 5f, "lookup24", -1.0f, "", false, "Jmp on condition from off");
		nl(3, 30, 0, "BlJtRefOn", 4, 0f, 9f, "lookup27", -1.0f, "", false, "Jump from refOn");
		nl(3, 30, 4, "BlJtRefOff", 4, 0f, 9f, "lookup27", -1.0f, "", false, "Jump from refOff");
		nl(3, 31, 0, "DriveMode", 8, 0f, 3f, "lookup34", -1.0f, "", false, "");
		nl(3, 34, 0, "RampOnSp", 8, 0f, 1f, "", 200f, "s", false, "Ramp on speed");
		nl(3, 35, 0, "RampOffSp", 8, 0f, 1f, "", 200f, "s", false, "Ramp off speed");
		nl(3, 36, 0, "ActTypeLed", 8, 0f, 255f, "lookup35", -1.0f, "", false, "LED color");
		nl(3, 36, 0, "ActTypeMp3", 8, 0f, 255f, "", -1.0f, "", false, "Tone or MP3 to be played");
		nl(3, 36, 0, "ActTypeOuCf", 8, 0f, 255f, "lookup36", -1.0f, "", false, "type sound or LED");
		nl(3, 37, 0, "ActNum", 8, 1f, 255f, "", -1.0f, "", false, "Number of repetitions");
		nl(3, 38, 0, "DimElsActionType", 4, 0f, 8f, "lookup25", -1.0f, "", false, "");
		nl(3, 38, 6, "DimElsOffTimeMd", 1, 0f, 1f, "lookup26", -1.0f, "", false, "");
		nl(3, 38, 7, "DimElsOnTimeMd", 1, 0f, 1f, "lookup26", -1.0f, "", false, "");
		nl(3, 39, 0, "DimElsJtOn", 4, 0f, 6f, "lookup28", -1.0f, "", false, "else Jump from on");
		nl(3, 39, 4, "DimElsJtOff", 4, 0f, 6f, "lookup28", -1.0f, "", false, "else Jump from off");
		nl(3, 4, 0, "CtValLo", 8, 0f, 255f, "", -1.0f, "", false, "Condition value low for CT table");
		nl(3, 40, 0, "DimElsJtDlyOn", 4, 0f, 6f, "lookup28", -1.0f, "", false, "else Jump from delayOn");
		nl(3, 40, 4, "DimElsJtDlyOff", 4, 0f, 6f, "lookup28", -1.0f, "", false, "else Jump from delayOff");
		nl(3, 41, 0, "DimElsJtRampOn", 4, 0f, 6f, "lookup28", -1.0f, "", false, "else Jump from rampOn");
		nl(3, 41, 4, "DimElsJtRampOff", 4, 0f, 6f, "lookup28", -1.0f, "", false, "else Jump from rampOff");
		nl(3, 43, 0, "Intense", 8, 10f, 255f, "lookup37", -1.0f, "", false, "Volume");
		nl(3, 45, 0, "TempRC", 6, 5f, 30f, "", 2f, "C", false, "temperature if required by CtrlRc reg");
		nl(3, 46, 0, "CtrlRc", 4, 0f, 6f, "lookup38", -1.0f, "", false, "set mode and/or temperature");
		nl(3, 5, 0, "CtValHi", 8, 0f, 255f, "", -1.0f, "", false, "Condition value high for CT table");
		nl(3, 6, 0, "OnDly", 8, 0f, 111600f, "fltCvT", -1.0f, "s", false, "on delay");// TODO implement conversion
		// method
		nl(3, 7, 0, "OnTime", 8, 0f, 111600f, "fltCvT", -1.0f, "s", false, "on time");// lit={unused=111600}},// TODO
		// implement conversion method
		nl(3, 8, 0, "OffDly", 8, 0f, 111600f, "fltCvT", -1.0f, "s", false, "off delay");// TODO implement conversion
		// method
		nl(3, 9, 0, "OffTime", 8, 0f, 111600f, "fltCvT", -1.0f, "s", false, "off time");// lit={unused=111600}},// TODO
		// implement conversion method
	}
}
