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
package org.ogema.apps.climatestation;

public class Constants {

	static final float RH2AH_FACTOR = 0.66f;
	static final String SERVLET_PATH = "/rcsservice";
	static final String MESSAGES_PROPERTY_FILE = "properties/messages.properties";
	static final String OUTSIDE_TEMP_SENS = "resourcePathTemperatureOutside";
	static final String OUTSIDE_RH_SENSOR = "resourcePathHumidityOutside";
	static final String INSIDE_TEMP_SENS = "resourcePathTemperatureInside";
	static final String INSIDE_RH_SENSOR = "resourcePathHumidityInside";

	static final String JSON_TEMPIN_NAME = "tempIn";
	static final String JSON_RHIN_NAME = "rhIn";
	static final String JSON_TEMPOUT_NAME = "tempOut";
	static final String JSON_RHOUT_NAME = "rhOut";
	static final String JSON_MESSAGE_NAME = "message";
	static final String JSON_MESSAGEID_NAME = "messageID";
	static final String JSON_PRIORITY_NAME = "prio";
	static final String JSON_MOTIONIN_NAME = "motionIn";
	static final String JSON_LIGHT_NAME = "light1";
	static final String JSON_LIGHT2_NAME = "light2";
	static final String JSON_AIR_NAME = "air";
	static final String JSON_WATER_NAME = "water";
	static final String JSON_SMOKE_NAME = "smoke";

	static final int Priority_1 = 1;
	static final int Priority_2 = 3;
	static final int Priority_3 = 2;
	static final int Priority_4 = 3;

	static final String[] messages = {
			/* ID_0 */"Die Sensoren liefern keine gültigen Meßwerte. Bitte prüfen Sie den Status der Raumsensoren.",
			/* ID_1 */"Der Raumluftzustand ist in Ordnung.",
			/* ID_2 */"Der CO2-Gehalt ist sehr hoch. Bitte öffnen Sie das Fenster..",
			/* ID_3 */"Die Temperatur im Innen- und Aussenbereich ist sehr hoch.",
			/* ID_4 */"Die Innentemperatur ist sehr hoch. Bitte öffnen Sie das Fenster.",
			/* ID_5 */"Die Innentemperatur ist sehr hoch. Bitte drehen Sie Heizung niedriger.",
			/* ID_6 */"Die Aussentemperatur ist sehr hoch. Schließen Sie die Fenster um eine Aufheizung der Wohnung zu vermeiden.",
			/* ID_7 */"Die Innentemperatur ist in Ordnung.",
			/* ID_8 */"Die Innentemperatur ist sehr niedrig. Bitte drehen Sie die Heizung höher.",
			/* ID_9 */"Die Luftfeuchtigkeit ist sehr hoch. Bitte öffnen Sie das Fenster.",
			/* ID_10 */"Die Luftfeuchtigkeit ist sehr hoch. Es droht Schimmelgefahr. Bitte öffnen Sie das Fenster.",
			/* ID_11 */"Die Luftfeuchtigkeit im Innen- und Aussenbereich ist sehr hoch. Bitte drehen Sie die Heizung höher.",
			/* ID_12 */"Die Luftfeuchtigkeit im Innen- und Aussenbereich ist sehr hoch. Es droht Schimmelgefahr. Bitte drehen Sie die Heizung höher.",
			/* ID_13 */"Die Luftfeuchtigkeit im Aussenbereich ist sehr hoch. Bitte schließen Sie die Fenster.",
			/* ID_14 */"Die Luftfeuchtigkeit im Aussenbereich ist sehr hoch. Es droht Schimmelgefahr. Bitte schließen Sie die Fenster.",
			/* ID_15 */"Die Luftfeuchtigkeit ist sehr niedrig.",
			/* ID_16 */"Die Luftfeuchtigkeit ist in Ordnung.",
			/* ID_17 */"Die Luftfeuchtigkeit ist hoch. Bitte öffnen Sie das Fenster.",
			/* ID_18 */"Die Luftfeuchtigkeit im Innen- und Aussenbereich ist sehr hoch.",
			/* ID_19 */"Die Luftfeuchtigkeit ist sehr hoch. Es droht Schimmelgefahr. Bitte öffnen Sie das Fenster.",
			/* ID_20 */"Stoßlüften 5 min.",
			/* ID_21 */"CO2 ist sehr hoch, wenn möglich 5-10 min Stoßlüften.",
			/* ID_22 */"und CO2-Gehalt.",
			/* ID_23 */"Die Luftfeuchtigkeit und Innentemperatur ist sehr hoch. Es droht Schimmelgefahr. Bitte öffnen Sie das Fenster.",
			/* ID_24 */"Die Innentemperatur und Luftfeuchtigkeit ist sehr hoch. Es droht Schimmelgefahr. Bitte öffnen Sie das Fenster und drehen die Heizung runter.",
			/* ID_25 */"Die Luftfeuchtigkeit ist sehr niedrig. Die Innentemperatur ist sehr hoch. Bitte öffnen Sie das Fenster.",
			/* ID_26 */"Die Luftfeuchtigkeit im Aussenbereich ist sehr hoch, die Innentemperatur sehr niedrig. Es droht Schimmelgefahr. Bitte drehen Sie die Heizung auf und schließen Sie die Fenster.",
			/* ID_27 */"Die Luftfeuchtigkeit im Aussenbereich ist sehr hoch, die Innentemperatur sehr niedrig. Bitte drehen Sie die Heizung auf und schließen Sie die Fenster.",
			/* ID_28 */"Die Luftfeuchtigkeit im Innen- und Aussenbereich ist hoch. Die Innentemperatur ist sehr niedrig. Es droht Schimmelgefahr. Bitte drehen Sie die Heizung auf.",
			/* ID_29 */"Die Luftfeuchtigkeit im Innen- und Aussenbereich ist sehr hoch. Die Innentemperatur ist sehr niedrig. Bitte drehen Sie die Heizung höher.",
			/* ID_30 */"Die Luftfeuchtigkeit und Temperatur im Aussenbereich ist sehr hoch. Bitte schließen Sie die Fenster um eine Aufheizung der Wohnung zu verhindern.",
			/* ID_31 */"Die Innentemperatur ist sehr hoch, die Luftfeuchtigkeit sehr niedrig. Bitte drehen Sie die Heizung niedriger.",
			/* ID_32 */"Die Luftfeuchtigkeit und Innentemperatur ist sehr hoch. Bitte öffnen Sie das Fenster.",
			/* ID_33 */"Die Luftfeuchtigkeit im Innen- und Aussenbereich, sowie Innentemperatur ist sehr hoch. Bitte öffnen Sie das Fenster.",
			/* ID_34 */"Die Luftfeuchtigkeit außen, sowie die Innentemperatur und Luftfeuchtigkeit ist sehr hoch. Bitte drehen sie die Heizung niedriger.",
			/* ID_35 */"Die Luftfeuchtikeit im Innen- und Aussenbreich ist sehr hoch. Die Innentemperatur sehr niedrig. Bitte drehen Sie die Heizung höher.",
			/* ID_36 */"Unbekannter Zustand." };
	public static final int DEFAULT_MESSAGEID = 36;
}
