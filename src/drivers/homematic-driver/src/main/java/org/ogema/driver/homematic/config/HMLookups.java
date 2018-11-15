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
import java.util.Map.Entry;
import java.util.Set;

public class HMLookups {
	static final String[] lookup1 = { "off", "on", "auto" }; // lit={off=0,on=1}}
	static final String[] lookup2 = { "invisible", "visible" };// lit={invisib=0,visib=1}}
	static final HashMap<Integer, String> lookup3 = new HashMap<Integer, String>();
	static final HashMap<Integer, String> lookup4 = new HashMap<Integer, String>();
	static final String[] lookup5 = { "off", "T1", "T2", "T1_T2", "T2_T1" };// lit={off=0,T1=1,T2=2,T1_T2=3,T2_T1=4}},
	static final String[] lookup6 = { "low", "mid", "high", "veryHigh" };// lit={low=0,mid=1,high=2,veryHigh=3}},
	static final String[] lookup7 = { "English", "German" };// lit={English=0,German=1}},
	static final String[] lookup8 = { "permanent" };// lit={permanent=0}}
	static final HashMap<Integer, String> lookup9 = new HashMap<Integer, String>();
	static final String[] lookup10 = { "unused" };// lit={unused=0}}
	static final HashMap<Integer, String> lookup11 = new HashMap<Integer, String>();
	static final HashMap<Integer, String> lookup12 = new HashMap<Integer, String>();
	static final HashMap<Integer, String> lookup13 = new HashMap<Integer, String>();
	static final String[] lookup14 = { "right", "left" };// lit={right=0,left=1}}
	static final String[] lookup15 = { "noMsg", "lvlNormal", "lvlAddStrong", "lvlAdd" };// lit={noMsg=0,lvlNormal=1,lvlAddStrong=2,lvlAdd=3}}
	static final String[] lookup16 = { "noMsg", "closed", "open", "tilted" };// lit={noMsg=0,closed=1,open=2,tilted=3}}
	static final String[] lookup17 = { "noMsg", "dry", "water", "wet" };// lit={noMsg=0,dry=1,water=2,wet=3}}
	static final String[] lookup18 = { "none", "tone1", "tone2", "tone3" };// lit={none=0,tone1=1,tone2=2,tone3=3}}
	static final String[] lookup19 = { "off", "on", "blinkSlow", "blinkFast" };// lit={off=0,on=1,blinkSlow=2,blinkFast=3}}
	static final String[] lookup20 = { "off", "last", "btnPress", "btnPressIfWasOn" };// lit={off=0,last=1,btnPress=2,btnPressIfWasOn=3}}
	static final String[] lookup21 = { "linear", "square" };// lit={linear=0,square=1}}
	static final String[] lookup22 = { "inactive", "or", "and", "xor", "nor", "nand", "orinv", "andinv", "plus",
			"minus", "mul", "plusinv", "minusinv", "mulinv", "invPlus", "invMinus", "invMul" };// lit={inactive=0,or=1,and=2,xor=3,nor=4,nand=5,orinv=6,andinv=7,plus=8,minus=9,mul=10,plusinv=11,minusinv=12,mulinv=13,invPlus=14,invMinus=15,invMul=16}}
	static final String[] lookup23 = { "", "verticalBarrel", "horizBarrel", "rectangle" };// lit={verticalBarrel=1,horizBarrel=2,rectangle=3}}
	static final String[] lookup24 = { "geLo", "geHi", "ltLo", "ltHi", "between", "outside" };// lit={geLo=0,geHi=1,ltLo=2,ltHi=3,between=4,outside=5}}
	static final String[] lookup25 = { "off", "jmpToTarget", "toggleToCnt", "toggleToCntInv", "upDim", "downDim",
			"toggelDim", "toggelDimToCnt", "toggelDimToCntInv" };// lit={off=0,jmpToTarget=1,toggleToCnt=2,toggleToCntInv=3,upDim=4,downDim=5,toggelDim=6,toggelDimToCnt=7,toggelDimToCntInv=8}}
	static final String[] lookup26 = { "absolut", "minimal" };// lit={absolut=0,minimal=1}}
	static final String[] lookup27 = { "no", "dlyOn", "refOn", "on", "dlyOff", "refOff", "off", "", "rampOn", "rampOff" };// lit={no=0,dlyOn=1,refOn=2,on=3,dlyOff=4,refOff=5,off=6,rampOn=8,rampOff=9}}
	static final String[] lookup28 = { "no", "dlyOn", "rampOn", "on", "dlyOff", "rampOff", "off" };// lit={no=0,dlyOn=1,rampOn=2,on=3,dlyOff=4,rampOff=5,off=6}}
	static final String[] lookup29 = { "no", "dlyUnlock", "rampUnlock", "unLock", "dlyLock", "rampLock", "lock", "",
			"open" };// lit={no=0,dlyUnlock=1,rampUnlock=2,unLock=3,dlyLock=4,rampLock=5,lock=6,open=8}}
	static final String[] lookup30 = { "no", "rampOnDly", "rampOn", "on", "rampOffDly", "rampOff", "off", "",
			"rampOnFast", "rampOffFast" };// lit={no=0,rampOnDly=1,rampOn=2,on=3,rampOffDly=4,rampOff=5,off=6,rampOnFast=8,rampOffFast=9}}
	static final String[] lookup31 = { "no", "", "on", "", "", "off" };// lit={no=0,on=2,off=5}}
	static final String[] lookup32 = { "high", "low" };// lit={high=0,low=1}}
	static final String[] lookup33 = { "setToOff", "NoChange" };// lit={setToOff=0,NoChange=1}}
	static final String[] lookup34 = { "direct", "viaUpperEnd", "viaLowerEnd", "viaNextEnd" };// lit={direct=0,viaUpperEnd=1,viaLowerEnd=2,viaNextEnd=3}}
	static final HashMap<Integer, String> lookup35 = new HashMap<Integer, String>();
	static final String[] lookup36 = { "no", "short", "long" };// lit={no=0,short=1,long=2}}
	static final HashMap<Integer, String> lookup37 = new HashMap<Integer, String>();
	static final String[] lookup38 = { "no", "tempOnly", "auto", "autoAndTemp", "manuAndTemp", "boost", "toggle" };// lit={no=0,tempOnly=1,auto=2,autoAndTemp=3,manuAndTemp=4,boost=5,toggle=6}}
	static final String[] lookup39 = { "none", "bulb", "switch", "window", "door", "blind", "scene", "phone", "bell" };// lit={"none"=0,"bulb"=1,"switch"=2,"window"=3,"door"=4,"blind"=5,"scene"=6,"phone"=7,"bell"=8}}
	static final String[] lookup40 = { "none", "light", "blind", "marquee", "door", "window" };// lit={"none"=0,"light"=1,"blind"=2,"marquee"=3,"door"=4,"window"=5}}
	static final String[] lookup41 = { "temp-only", "temp-hum" };// lit={"temp-only"=0,"temp-hum"=1}}
	static final String[] lookup42 = { "actual", "setpoint" };// lit={actual=0,setpoint=1}}
	static final String[] lookup43 = { "celsius", "fahrenheit" };// lit={celsius=0,fahrenheit=1}}
	static final String[] lookup44 = { "manual", "auto", "central", "party" };// lit={manual=0,auto=1,central=2,party=3}}
	static final String[] lookup45 = { "Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri" };// lit={Sat=0,Sun=1,Mon=2,Tue=3,Wed=4,Thu=5,Fri=6}}
	static final String[] lookup46 = { "auto", "close", "open" };// lit={auto=0,close=1,open=2}}
	static final String[] lookup47 = { "00", "30" };// lit={"00"=0,"30"=1}}
	static final String[] lookup48 = { "time", "date" };// lit={time=0,date=1}}
	static final String[] lookup49 = { "heating", "cooling" };// lit={heating=0,cooling=1}}
	static final String[] lookup50 = { "prog1", "prog2", "prog3" };// lit={prog1=0,prog2=1,prog3=2}}
	static final String[] lookup51 = { "offDefault", "offDeter", "on" };// lit={offDefault=0,offDeter=1,on=2}}
	static final String[] lookup52 = { "RT_TC_SC_SELF", "all", "RT_TC_CCU_SELF", "CCU", "self" };// lit={RT_TC_SC_SELF=0,all=1,RT_TC_CCU_SELF=2,CCU=3,self=4}}
	static final String[] lookup53 = { "off", "auto", "auto_manu", "auto_party", "on" };// lit={off=0,auto=1,auto_manu=2,auto_party=3,on=4}}
	static final String[] lookup54 = { "-3.5K", "-3.0K", "-2.5K", "-2.0K", "-1.5K", "-1.0K", "-0.5K", "0.0K", "0.5K",
			"1.0K", "1.5K", "2.0K", "2.5K", "3.0K", "3.5K" };// lit={"-3.5K"=0,"-3.0K"=1,"-2.5K"=2,"-2.0K"=3,"-1.5K"=4,"-1.0K"=5,"-0.5K"=6,"0.0K"=7,"0.5K"=8,"1.0K"=9,"1.5K"=10,"2.0K"=11,"2.5K"=12,"3.0K"=13,"3.5K"=14}}
	static final String[] lookup55 = { "actTemp", "setTemp" };// lit={actTemp=0,setTemp=1}}
	static final String[] lookup56 = { "temp", "tempHum" }; // lit={temp=0,tempHum=1}}
	static final HashMap<Integer, String> lookup57 = new HashMap<Integer, String>(); // lit={0=0,5=1,10=2,15=3,20=4,25=5,30=6}},

	static {
		lookup3.put(255, "permanent"); // lit={permanent=255}}

		lookup4.put(0, "off");
		lookup4.put(200, "on");// lit={off=0,on=200}}

		lookup9.put(0, "off");
		lookup9.put(200, "on_100");
		lookup9.put(1, "on"); // lit={off=0,on=1,on_100=200}}

		lookup11.put(0, "off");
		lookup11.put(33, "sensor");
		lookup11.put(34, "switch");
		lookup11.put(35, "button");// lit={off=0,sensor=33,switch=34,button=35}}
		lookup12.put(1, "gas");
		lookup12.put(2, "IR");
		lookup12.put(4, "LED");
		lookup12.put(255, "unknown");// lit={gas=1,IR=2,LED=4,unknown=255}}

		lookup13.put(0, "15");
		lookup13.put(1, "30");
		lookup13.put(2, "60");
		lookup13.put(3, "120");
		lookup13.put(4, "240");// lit={15=0,30=1,60=2,120=3,240=4}}

		lookup35.put(0, "no");
		lookup35.put(0x11, "redS");
		lookup35.put(0x12, "redL");
		lookup35.put(0x21, "greenS");
		lookup35.put(0x22, "greenL");
		lookup35.put(0x31, "orangeS");
		lookup35.put(0x32, "orangeL");// lit={no=0x00,redS=0x11,redL=0x12,greenS=0x21,greenL=0x22,orangeS=0x31,orangeL=0x32}}

		lookup37.put(255, "vol_100");
		lookup37.put(250, "vol_90");
		lookup37.put(246, "vol_80");
		lookup37.put(240, "vol_70");
		lookup37.put(234, "vol_60");
		lookup37.put(227, "vol_50");
		lookup37.put(218, "vol_40");
		lookup37.put(207, "vol_30");
		lookup37.put(190, "vol_20");
		lookup37.put(162, "vol_10");
		lookup37.put(10, "vol_00");// lit={vol_100=255,vol_90=250,vol_80=246,vol_70=240,vol_60=234,vol_50=227,vol_40=218,vol_30=207,vol_20=190,vol_10=162,vol_00=10}}

		lookup57.put(0, "0");
		lookup57.put(1, "5");
		lookup57.put(2, "10");
		lookup57.put(3, "15");
		lookup57.put(4, "20");
		lookup57.put(5, "25");
		lookup57.put(6, "30");// lit={0=0,5=1,10=2,15=3,20=4,25=5,30=6}},

	}

	public static String getSetting2Value(String lookup, int value) {
		String result = value + "";
		switch (lookup) {
		case "lookup1":
			result = lookup1[value];
			break;
		case "lookup2":
			result = lookup2[value];
			break;
		case "lookup3":
			result = lookup3.get(value);
			break;
		case "lookup4":
			result = lookup4.get(value);
			break;
		case "lookup5":
			result = lookup5[value];
			break;
		case "lookup6":
			result = lookup6[value];
			break;
		case "lookup7":
			result = lookup7[value];
			break;
		case "lookup8":
			result = lookup8[value];
			break;
		case "lookup9":
			result = lookup9.get(value);
			break;
		case "lookup10":
			result = lookup10[value];
			break;
		case "lookup11":
			result = lookup11.get(value);
			break;
		case "lookup12":
			result = lookup12.get(value);
			break;
		case "lookup13":
			result = lookup13.get(value);
			break;
		case "lookup14":
			result = lookup14[value];
			break;
		case "lookup15":
			result = lookup15[value];
			break;
		case "lookup16":
			result = lookup16[value];
			break;
		case "lookup17":
			result = lookup17[value];
			break;
		case "lookup18":
			result = lookup18[value];
			break;
		case "lookup19":
			result = lookup19[value];
			break;
		case "lookup20":
			result = lookup20[value];
			break;
		case "lookup21":
			result = lookup21[value];
			break;
		case "lookup22":
			result = lookup22[value];
			break;
		case "lookup23":
			result = lookup23[value];
			break;
		case "lookup24":
			result = lookup24[value];
			break;
		case "lookup25":
			result = lookup25[value];
			break;
		case "lookup26":
			result = lookup26[value];
			break;
		case "lookup27":
			result = lookup27[value];
			break;
		case "lookup28":
			result = lookup28[value];
			break;
		case "lookup29":
			result = lookup29[value];
			break;
		case "lookup30":
			result = lookup30[value];
			break;
		case "lookup31":
			result = lookup31[value];
			break;
		case "lookup32":
			result = lookup32[value];
			break;
		case "lookup33":
			result = lookup33[value];
			break;
		case "lookup34":
			result = lookup34[value];
			break;
		case "lookup35":
			result = lookup35.get(value);
			break;
		case "lookup36":
			result = lookup36[value];
			break;
		case "lookup37":
			result = lookup37.get(value);
			break;
		case "lookup38":
			result = lookup38[value];
			break;
		case "lookup39":
			result = lookup39[value];
			break;
		case "lookup40":
			result = lookup40[value];
			break;
		case "lookup41":
			result = lookup41[value];
			break;
		case "lookup43":
			result = lookup42[value];
			break;
		case "lookup44":
			result = lookup44[value];
			break;
		case "lookup45":
			result = lookup45[value];
			break;
		case "lookup46":
			result = lookup46[value];
			break;
		case "lookup47":
			result = lookup47[value];
			break;
		case "lookup48":
			result = lookup48[value];
			break;
		case "lookup49":
			result = lookup49[value];
			break;
		case "lookup50":
			result = lookup50[value];
			break;
		case "lookup51":
			result = lookup51[value];
			break;
		case "lookup52":
			result = lookup52[value];
			break;
		case "lookup53":
			result = lookup53[value];
			break;
		case "lookup54":
			result = lookup54[value];
			break;
		case "lookup55":
			result = lookup55[value];
			break;
		case "lookup56":
			result = lookup56[value];
			break;
		case "lookup57":
			result = lookup57.get(value);
			break;
		default:
			System.err.println("No specific lookup!");
		}
		return result;
	}

	/*
	 * Determine which register value maps with the value to be set
	 */
	@SuppressWarnings("unchecked")
	public static int getValue2Setting(String conv, String setting, float factor) {
		int value;
		int key;
		Object lookup = getLookup(conv);
		if (lookup == null) {
			value = getFactoredValue(setting, factor);
			return value;
		}
		else if (lookup instanceof String[]) {

			try {
				key = Integer.valueOf(setting);
				String s = ((String[]) lookup)[key];
			} catch (NumberFormatException | ArrayIndexOutOfBoundsException e1) {
				return -1;
			}
			return key;
		}
		else {
			try {
				key = Integer.valueOf(setting);
				String s = ((HashMap<Integer, String>) lookup).get(key);
				if (s != null)
					return key;
			} catch (NumberFormatException e1) {
				return -1;
			}
			return key;
		}
	}

	private static int getFactoredValue(String setting, float factor) {
		int result;
		float fValue = Float.parseFloat(setting);
		if (factor > 0.0f)
			fValue = fValue * factor;
		result = Math.round(fValue);
		return result;
	}

	// private static int getIndexOfSettingMap(HashMap<Integer, String> lookup, String setting) {
	// int result = -1;
	// Set<Entry<Integer, String>> entries = lookup.entrySet();
	// for (Entry<Integer, String> entry : entries) {
	// String value = entry.getValue();
	// if (value.equals(setting)) {
	// result = entry.getKey();
	// break;
	// }
	// }
	// return result;
	// }
	//
	// private static int getIndexOfSettingArr(String[] lookup, String setting) {
	// int index = 0;
	// for (String entry : lookup) {
	// if (entry.equals(setting))
	// break;
	// index++;
	// }
	// return index;
	// }

	public static Object getLookup(String lookup) {
		Object result = null;
		switch (lookup) {
		case "lookup1":
			result = lookup1;
			break;
		case "lookup2":
			result = lookup2;
			break;
		case "lookup3":
			result = lookup3;
			break;
		case "lookup4":
			result = lookup4;
			break;
		case "lookup5":
			result = lookup5;
			break;
		case "lookup6":
			result = lookup6;
			break;
		case "lookup7":
			result = lookup7;
			break;
		case "lookup8":
			result = lookup8;
			break;
		case "lookup9":
			result = lookup9;
			break;
		case "lookup10":
			result = lookup10;
			break;
		case "lookup11":
			result = lookup11;
			break;
		case "lookup12":
			result = lookup12;
			break;
		case "lookup13":
			result = lookup13;
			break;
		case "lookup14":
			result = lookup14;
			break;
		case "lookup15":
			result = lookup15;
			break;
		case "lookup16":
			result = lookup16;
			break;
		case "lookup17":
			result = lookup17;
			break;
		case "lookup18":
			result = lookup18;
			break;
		case "lookup19":
			result = lookup19;
			break;
		case "lookup20":
			result = lookup20;
			break;
		case "lookup21":
			result = lookup21;
			break;
		case "lookup22":
			result = lookup22;
			break;
		case "lookup23":
			result = lookup23;
			break;
		case "lookup24":
			result = lookup24;
			break;
		case "lookup25":
			result = lookup25;
			break;
		case "lookup26":
			result = lookup26;
			break;
		case "lookup27":
			result = lookup27;
			break;
		case "lookup28":
			result = lookup28;
			break;
		case "lookup29":
			result = lookup29;
			break;
		case "lookup30":
			result = lookup30;
			break;
		case "lookup31":
			result = lookup31;
			break;
		case "lookup32":
			result = lookup32;
			break;
		case "lookup33":
			result = lookup33;
			break;
		case "lookup34":
			result = lookup34;
			break;
		case "lookup35":
			result = lookup35;
			break;
		case "lookup36":
			result = lookup36;
			break;
		case "lookup37":
			result = lookup37;
			break;
		case "lookup38":
			result = lookup38;
			break;
		case "lookup39":
			result = lookup39;
			break;
		case "lookup40":
			result = lookup40;
			break;
		case "lookup41":
			result = lookup41;
			break;
		case "lookup43":
			result = lookup42;
			break;
		case "lookup44":
			result = lookup44;
			break;
		case "lookup45":
			result = lookup45;
			break;
		case "lookup46":
			result = lookup46;
			break;
		case "lookup47":
			result = lookup47;
			break;
		case "lookup48":
			result = lookup48;
			break;
		case "lookup49":
			result = lookup49;
			break;
		case "lookup50":
			result = lookup50;
			break;
		case "lookup51":
			result = lookup51;
			break;
		case "lookup52":
			result = lookup52;
			break;
		case "lookup53":
			result = lookup53;
			break;
		case "lookup54":
			result = lookup54;
			break;
		case "lookup55":
			result = lookup55;
			break;
		case "lookup56":
			result = lookup56;
			break;
		case "lookup57":
			result = lookup57;
			break;
		default:
			System.err.println("No specific lookup!");
			break;
		}
		return result;
	}
}
