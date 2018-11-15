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

import java.util.HashMap;

/**
 * This class provides a list of configurations, that are supported by a type of device. The list key is the unique name
 * of the configuration. For each physical instance of the device the list is cloned and fulfilled with the data
 * provided by the physical device.
 * 
 * @author mns
 *
 */
public class HMAllConfigs extends HMDevConfI {
	static HashMap<String, ListEntryValue> supportedConfigs;

	HashMap<String, ListEntryValue> deviceConfigs;

	@SuppressWarnings("unchecked")
	public HMAllConfigs() {
		deviceConfigs = (HashMap<String, ListEntryValue>) supportedConfigs.clone();
	}

	static {
		supportedConfigs = new HashMap<String, ListEntryValue>(43);
		supportedConfigs.put("burstRx", null);
		supportedConfigs.put("pairCentral", null);
		supportedConfigs.put("backAtCharge", null);
		supportedConfigs.put("backAtMotion", null);
		supportedConfigs.put("backAtKey", null);
		supportedConfigs.put("backOnTime", null);
		supportedConfigs.put("stbyTime", null);
		supportedConfigs.put("stbyTime2", null);
		supportedConfigs.put("btnLock", null);
		supportedConfigs.put("sabotageMsg", null);
		supportedConfigs.put("cyclicInfoMsgDis", null);
		supportedConfigs.put("lowBatLimit", null);
		supportedConfigs.put("lowBatLimitBA", null);
		supportedConfigs.put("lowBatLimitBA2", null);
		supportedConfigs.put("lowBatLimitBA3", null);
		supportedConfigs.put("lowBatLimitFS", null);
		supportedConfigs.put("lowBatLimitRT", null);
		supportedConfigs.put("batDefectLimit", null);
		supportedConfigs.put("intKeyVisib", null);
		supportedConfigs.put("transmDevTryMax", null);
		supportedConfigs.put("confBtnTime", null);
		supportedConfigs.put("compMode", null);
		supportedConfigs.put("localResDis", null);
		supportedConfigs.put("globalBtnLock", null);
		supportedConfigs.put("modusBtnLock", null);
		supportedConfigs.put("paramSel", null);
		supportedConfigs.put("RS485IdleTime", null);
		supportedConfigs.put("keypressSignal", null);
		supportedConfigs.put("signal", null);
		supportedConfigs.put("signalTone", null);
		supportedConfigs.put("wakeupDefChan", null);
		supportedConfigs.put("wakeupBehavior", null);
		supportedConfigs.put("brightness", null);
		supportedConfigs.put("backlOnTime", null);
		supportedConfigs.put("backlOnMode", null);
		supportedConfigs.put("backlOnMode2", null);
		supportedConfigs.put("ledMode", null);
		supportedConfigs.put("language", null);
		supportedConfigs.put("energyOpt", null);
		supportedConfigs.put("cyclicInfoMsg", null);
		supportedConfigs.put("evtFltrPeriod", null);
		supportedConfigs.put("evtFltrNum", null);
		supportedConfigs.put("caseWidth", null);
		supportedConfigs.put("caseLength", null);
		supportedConfigs.put("meaLength", null);
		supportedConfigs.put("driveDown", null);
		supportedConfigs.put("useCustom", null);
		supportedConfigs.put("averaging", null);
		supportedConfigs.put("txMinDly", null);
		supportedConfigs.put("txThrPwr", null);
		supportedConfigs.put("txThrCur", null);
		supportedConfigs.put("txThrVlt", null);
		supportedConfigs.put("driveUp", null);
		supportedConfigs.put("txThrFrq", null);
		supportedConfigs.put("cndTxFalling", null);
		supportedConfigs.put("cndTxRising", null);
		supportedConfigs.put("cndTxCycBelow", null);
		supportedConfigs.put("cndTxCycAbove", null);
		supportedConfigs.put("cndTxDecAbove", null);
		supportedConfigs.put("cndTxDecBelow", null);
		supportedConfigs.put("cndTxThrhHi", null);
		supportedConfigs.put("txThrHiCur", null);
		supportedConfigs.put("txThrHiFrq", null);
		supportedConfigs.put("txThrHiPwr", null);
		supportedConfigs.put("txThrHiVlt", null);
		supportedConfigs.put("cndTxThrhLo", null);
		supportedConfigs.put("txThrLoCur", null);
		supportedConfigs.put("txThrLoFrq", null);
		supportedConfigs.put("txThrLoPwr", null);
		supportedConfigs.put("txThrLoVlt", null);
		supportedConfigs.put("highHoldTime", null);
		supportedConfigs.put("evntRelFltTime", null);
		supportedConfigs.put("triggerMode", null);
		supportedConfigs.put("mtrType", null);
		supportedConfigs.put("driveTurn", null);
		supportedConfigs.put("mtrConstIr", null);
		supportedConfigs.put("mtrConstGas", null);
		supportedConfigs.put("mtrConstLed", null);
		supportedConfigs.put("mtrSensIr", null);
		supportedConfigs.put("refRunCounter", null);
		supportedConfigs.put("loadErrCalib", null);
		supportedConfigs.put("minInterval", null);
		supportedConfigs.put("captInInterval", null);
		supportedConfigs.put("brightFilter", null);
		supportedConfigs.put("holdTime", null);
		supportedConfigs.put("holdPWM", null);
		supportedConfigs.put("setupDir", null);
		supportedConfigs.put("setupPosition", null);
		supportedConfigs.put("angelOpen", null);
		supportedConfigs.put("angelMax", null);
		supportedConfigs.put("angelLocked", null);
		supportedConfigs.put("pullForce", null);
		supportedConfigs.put("pushForce", null);
		supportedConfigs.put("tiltMax", null);
		supportedConfigs.put("ledFlashUnlocked", null);
		supportedConfigs.put("ledFlashLocked", null);
		supportedConfigs.put("msgScdPosD", null);
		supportedConfigs.put("msgRhsPosC", null);
		supportedConfigs.put("msgScdPosC", null);
		supportedConfigs.put("msgWdsPosC", null);
		supportedConfigs.put("msgRhsPosB", null);
		supportedConfigs.put("msgScPosB", null);
		supportedConfigs.put("msgScdPosB", null);
		supportedConfigs.put("msgWdsPosB", null);
		supportedConfigs.put("msgRhsPosA", null);
		supportedConfigs.put("msgScPosA", null);
		supportedConfigs.put("msgScdPosA", null);
		supportedConfigs.put("msgWdsPosA", null);
		supportedConfigs.put("eventDlyTime", null);
		supportedConfigs.put("ledOnTime", null);
		supportedConfigs.put("eventFilterTime", null);
		supportedConfigs.put("eventFilterTimeB", null);
		supportedConfigs.put("evtFltrTime", null);
		supportedConfigs.put("seqPulse1", null);
		supportedConfigs.put("seqPulse2", null);
		supportedConfigs.put("seqPulse3", null);
		supportedConfigs.put("seqPulse4", null);
		supportedConfigs.put("longPress", null);
		supportedConfigs.put("seqPulse5", null);
		supportedConfigs.put("seqTolerance", null);
		supportedConfigs.put("msgShowTime", null);
		supportedConfigs.put("beepAtAlarm", null);
		supportedConfigs.put("beepAtService", null);
		supportedConfigs.put("beepAtInfo", null);
		supportedConfigs.put("backlAtAlarm", null);
		supportedConfigs.put("backlAtService", null);
		supportedConfigs.put("backlAtInfo", null);
		supportedConfigs.put("transmitTryMax", null);
		supportedConfigs.put("loadAppearBehav", null);
		supportedConfigs.put("sunThresh", null);
		supportedConfigs.put("ovrTempLvl", null);
		supportedConfigs.put("fuseDelay", null);
		supportedConfigs.put("redTempLvl", null);
		supportedConfigs.put("redLvl", null);
		supportedConfigs.put("stormUpThresh", null);
		supportedConfigs.put("waterUppThr", null);
		supportedConfigs.put("localResetDis", null);
		supportedConfigs.put("stormLowThresh", null);
		supportedConfigs.put("waterlowThr", null);
		supportedConfigs.put("sign", null);
		supportedConfigs.put("powerUpAction", null);
		supportedConfigs.put("statusInfoMinDly", null);
		supportedConfigs.put("statusInfoRandom", null);
		supportedConfigs.put("characteristic", null);
		supportedConfigs.put("logicCombination", null);
		supportedConfigs.put("dblPress", null);
		supportedConfigs.put("caseDesign", null);
		supportedConfigs.put("caseHigh", null);
		supportedConfigs.put("fillLevel", null);
		supportedConfigs.put("CtRampOn", null);
		supportedConfigs.put("CtRampOff", null);
		supportedConfigs.put("ActionType", null);
		supportedConfigs.put("ActionTypeDim", null);
		supportedConfigs.put("OffTimeMode", null);
		supportedConfigs.put("OnTimeMode", null);
		supportedConfigs.put("BlJtOn", null);
		supportedConfigs.put("DimJtOn", null);
		supportedConfigs.put("KeyJtOn", null);
		supportedConfigs.put("SwJtOn", null);
		supportedConfigs.put("WinJtOn", null);
		supportedConfigs.put("ttJtOn", null);
		supportedConfigs.put("BlJtOff", null);
		supportedConfigs.put("DimJtOff", null);
		supportedConfigs.put("KeyJtOff", null);
		supportedConfigs.put("SwJtOff", null);
		supportedConfigs.put("WinJtOff", null);
		supportedConfigs.put("ttJtOff", null);
		supportedConfigs.put("BlJtDlyOn", null);
		supportedConfigs.put("DimJtDlyOn", null);
		supportedConfigs.put("SwJtDlyOn", null);
		supportedConfigs.put("BlJtDlyOff", null);
		supportedConfigs.put("DimJtDlyOff", null);
		supportedConfigs.put("SwJtDlyOff", null);
		supportedConfigs.put("BlJtRampOn", null);
		supportedConfigs.put("DimJtRampOn", null);
		supportedConfigs.put("WinJtRampOn", null);
		supportedConfigs.put("BlJtRampOff", null);
		supportedConfigs.put("DimJtRampOff", null);
		supportedConfigs.put("WinJtRampOff", null);
		supportedConfigs.put("lgMultiExec", null);
		supportedConfigs.put("OffDlyBlink", null);
		supportedConfigs.put("OnLvlPrio", null);
		supportedConfigs.put("OnDlyMode", null);
		supportedConfigs.put("OffLevel", null);
		supportedConfigs.put("OffLevelKm", null);
		supportedConfigs.put("OnMinLevel", null);
		supportedConfigs.put("OnLevel", null);
		supportedConfigs.put("OnLevelKm", null);
		supportedConfigs.put("RampSstep", null);
		supportedConfigs.put("RampOnTime", null);
		supportedConfigs.put("CtDlyOn", null);
		supportedConfigs.put("CtDlyOff", null);
		supportedConfigs.put("RampOffTime", null);
		supportedConfigs.put("DimMinLvl", null);
		supportedConfigs.put("DimMaxLvl", null);
		supportedConfigs.put("DimStep", null);
		supportedConfigs.put("OffDlyStep", null);
		supportedConfigs.put("OffDlyNewTime", null);
		supportedConfigs.put("OffDlyOldTime", null);
		supportedConfigs.put("CtRefOn", null);
		supportedConfigs.put("CtRefOff", null);
		supportedConfigs.put("MaxTimeF", null);
		supportedConfigs.put("CtOn", null);
		supportedConfigs.put("CtOff", null);
		supportedConfigs.put("BlJtRefOn", null);
		supportedConfigs.put("BlJtRefOff", null);
		supportedConfigs.put("DriveMode", null);
		supportedConfigs.put("RampOnSp", null);
		supportedConfigs.put("RampOffSp", null);
		supportedConfigs.put("ActTypeLed", null);
		supportedConfigs.put("ActTypeMp3", null);
		supportedConfigs.put("ActTypeOuCf", null);
		supportedConfigs.put("ActNum", null);
		supportedConfigs.put("DimElsActionType", null);
		supportedConfigs.put("DimElsOffTimeMd", null);
		supportedConfigs.put("DimElsOnTimeMd", null);
		supportedConfigs.put("DimElsJtOn", null);
		supportedConfigs.put("DimElsJtOff", null);
		supportedConfigs.put("CtValLo", null);
		supportedConfigs.put("DimElsJtDlyOn", null);
		supportedConfigs.put("DimElsJtDlyOff", null);
		supportedConfigs.put("DimElsJtRampOn", null);
		supportedConfigs.put("DimElsJtRampOff", null);
		supportedConfigs.put("Intense", null);
		supportedConfigs.put("TempRC", null);
		supportedConfigs.put("CtrlRc", null);
		supportedConfigs.put("CtValHi", null);
		supportedConfigs.put("OnDly", null);
		supportedConfigs.put("OnTime", null);
		supportedConfigs.put("OffDly", null);
		supportedConfigs.put("OffTime", null);
		supportedConfigs.put("peerNeedsBurst", null);
		supportedConfigs.put("expectAES", null);
		supportedConfigs.put("lcdSymb", null);
		supportedConfigs.put("lcdLvlInterp", null);
		supportedConfigs.put("fillLvlUpThr", null);
		supportedConfigs.put("fillLvlLoThr", null);
		supportedConfigs.put("displayMode", null);
		supportedConfigs.put("displayTemp", null);
		supportedConfigs.put("displayTempUnit", null);
		supportedConfigs.put("controlMode", null);
		supportedConfigs.put("decalcDay", null);
		supportedConfigs.put("valveErrorPos", null);
		supportedConfigs.put("mdTempValve", null);
		supportedConfigs.put("day-temp", null);
		supportedConfigs.put("night-temp", null);
		supportedConfigs.put("tempWinOpen", null);
		supportedConfigs.put("party-temp", null);
		supportedConfigs.put("decalMin", null);
		supportedConfigs.put("decalHr", null);
		supportedConfigs.put("valveOffset", null);
		supportedConfigs.put("partyEndHr", null);
		supportedConfigs.put("partyEndMin", null);
		supportedConfigs.put("partyEndDay", null);
		supportedConfigs.put("dayTemp", null);
		supportedConfigs.put("boostPos", null);
		supportedConfigs.put("boostPeriod", null);
		supportedConfigs.put("valveOffsetRt", null);
		supportedConfigs.put("valveMaxPos", null);
		supportedConfigs.put("valveErrPos", null);
		supportedConfigs.put("daylightSaveTime", null);
		supportedConfigs.put("regAdaptive", null);
		supportedConfigs.put("showInfo", null);
		supportedConfigs.put("winOpnBoost", null);
		supportedConfigs.put("noMinMax4Manu", null);
		supportedConfigs.put("showWeekday", null);
		supportedConfigs.put("hyst2point", null);
		supportedConfigs.put("heatCool", null);
		supportedConfigs.put("weekPrgSel", null);
		supportedConfigs.put("modePrioParty", null);
		supportedConfigs.put("modePrioManu", null);
		supportedConfigs.put("winOpnDetFall", null);
		supportedConfigs.put("winOpnMode", null);
		supportedConfigs.put("nightTemp", null);
		supportedConfigs.put("reguIntI", null);
		supportedConfigs.put("reguIntP", null);
		supportedConfigs.put("reguIntPstart", null);
		supportedConfigs.put("reguExtI", null);
		supportedConfigs.put("reguExtP", null);
		supportedConfigs.put("reguExtPstart", null);
		supportedConfigs.put("tempMin", null);
		supportedConfigs.put("tempMax", null);
		supportedConfigs.put("winOpnTemp", null);
		supportedConfigs.put("winOpnPeriod", null);
		supportedConfigs.put("decalcWeekday", null);
		supportedConfigs.put("decalcTime", null);
		supportedConfigs.put("tempOffset", null);
		supportedConfigs.put("btnNoBckLight", null);
		supportedConfigs.put("showSetTemp", null);
		supportedConfigs.put("showHumidity", null);
		supportedConfigs.put("sendWeatherData", null);
	}

	@Override
	public HashMap<String, ListEntryValue> getDevConfigs() {
		return deviceConfigs;
	}
}
