/**
 * Copyright 2011_2018 Fraunhofer_Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE_2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fhg.iee.bacnet.enumerations;

import de.fhg.iee.bacnet.api.BACnetEnumeration;

/**
 *
 * @author jlapp
 */
public enum BACnetEngineeringUnit implements BACnetEnumeration {
	// Acceleration
	meters_per_second_per_second (166),
	// Area
	square_meters (0),
	square_centimeters (116),
	square_feet (1),
	square_inches (115),
	// Currency
	currency1 (105),
	currency2 (106),
	currency3 (107),
	currency4 (108),
	currency5 (109),
	currency6 (110),
	currency7 (111),
	currency8 (112),
	currency9 (113),
	currency10 (114),
	// Electrical
	milliamperes (2),
	amperes (3),
	amperes_per_meter (167),
	amperes_per_square_meter (168),
	ampere_square_meters (169),
	decibels (199),
	decibels_millivolt (200),
	decibels_volt (201),
	farads (170),
	henrys (171),
	ohms (4),
	ohm_meter_squared_per_meter (237),
	ohm_meters (172),
	milliohms (145),
	kilohms (122),
	megohms (123),
	microsiemens (190),
	millisiemens (202),
	siemens (173),
	siemens_per_meter (174),
	teslas (175),
	volts (5),
	millivolts (124),
	kilovolts (6),
	megavolts (7),
	volt_amperes (8),
	kilovolt_amperes (9),
	megavolt_amperes (10),
	volt_amperes_reactive (11),
	kilovolt_amperes_reactive (12),
	megavolt_amperes_reactive (13),
	volts_per_degree_kelvin (176),
	volts_per_meter (177),
	degrees_phase (14),
	power_factor (15),
	webers (178),
	// Energy
	ampere_seconds (238),
	volt_ampere_hours (239), // i.e. VAh
	kilovolt_ampere_hours (240),
	megavolt_ampere_hours (241),
	volt_ampere_hours_reactive (242), // i.e. varh
	kilovolt_ampere_hours_reactive (243),
	megavolt_ampere_hours_reactive (244),
	volt_square_hours (245),
	ampere_square_hours (246),
	joules (16),
	kilojoules (17),
	kilojoules_per_kilogram (125),
	megajoules (126),
	watt_hours (18),
	kilowatt_hours (19),
	megawatt_hours (146),
	watt_hours_reactive (203),
	kilowatt_hours_reactive (204),
	megawatt_hours_reactive (205),
	btus (20),
	kilo_btus (147),
	mega_btus (148),
	therms (21),
	ton_hours (22),
	// Enthalpy
	joules_per_kilogram_dry_air (23),
	kilojoules_per_kilogram_dry_air (149),
	megajoules_per_kilogram_dry_air (150),
	btus_per_pound_dry_air (24),
	btus_per_pound (117),
	// Entropy
	joules_per_degree_kelvin (127),
	kilojoules_per_degree_kelvin (151),
	megajoules_per_degree_kelvin (152),
	joules_per_kilogram_degree_kelvin (128),
	// Force
	newton (153),
	// Frequency
	cycles_per_hour (25),
	cycles_per_minute (26),
	hertz (27),
	kilohertz (129),
	megahertz (130),
	per_hour (131),
	// Humidity
	grams_of_water_per_kilogram_dry_air (28),
	percent_relative_humidity (29),
	// Length
	micrometers (194),
	millimeters (30),
	centimeters (118),
	kilometers (193),
	meters (31),
	inches (32),
	feet (33),
	// Light
	candelas (179),
	candelas_per_square_meter (180),
	watts_per_square_foot (34),
	watts_per_square_meter (35),
	lumens (36),
	luxes (37),
	foot_candles (38),
	// Mass
	milligrams (196),
	grams (195),
	kilograms (39),
	pounds_mass (40),
	tons (41),
	// Mass Flow
	grams_per_second (154),
	grams_per_minute (155),
	kilograms_per_second (42),
	kilograms_per_minute (43),
	kilograms_per_hour (44),
	pounds_mass_per_second (119),
	pounds_mass_per_minute (45),
	pounds_mass_per_hour (46),
	tons_per_hour (156),
	// Power
	milliwatts (132),
	watts (47),
	kilowatts (48),
	megawatts (49),
	btus_per_hour (50),
	kilo_btus_per_hour (157),
	joule_per_hours (247),
	horsepower (51),
	tons_refrigeration (52),
	// Pressure
	pascals (53),
	hectopascals (133),
	kilopascals (54),
	millibars (134),
	bars (55),
	pounds_force_per_square_inch (56),
	millimeters_of_water (206),
	centimeters_of_water (57),
	inches_of_water (58),
	millimeters_of_mercury (59),
	centimeters_of_mercury (60),
	inches_of_mercury (61),
	// Temperature
	degrees_celsius (62),
	degrees_kelvin (63),
	degrees_kelvin_per_hour (181),
	degrees_kelvin_per_minute (182),
	degrees_fahrenheit (64),
	degree_days_celsius (65),
	degree_days_fahrenheit (66),
	delta_degrees_fahrenheit (120),
	delta_degrees_kelvin (121),
	// Time
	years (67),
	months (68),
	weeks (69),
	days (70),
	hours (71),
	minutes (72),
	seconds (73),
	hundredths_seconds (158),
	milliseconds (159),
	// Torque
	newton_meters (160),
	// Velocity
	millimeters_per_second (161),
	millimeters_per_minute (162),
	meters_per_second (74),
	meters_per_minute (163),
	meters_per_hour (164),
	kilometers_per_hour (75),
	feet_per_second (76),
	feet_per_minute (77),
	miles_per_hour (78),
	// Volume
	cubic_feet (79),
	cubic_meters (80),
	imperial_gallons (81),
	milliliters (197),
	liters (82),
	us_gallons (83),
	// Volumetric Flow
	cubic_feet_per_second (142),
	cubic_feet_per_minute (84),
	million_standard_cubic_feet_per_minute (254),
	cubic_feet_per_hour (191),
	cubic_feet_per_day (248),
	standard_cubic_feet_per_day (47808),
	million_standard_cubic_feet_per_day (47809),
	thousand_cubic_feet_per_day (47810),
	thousand_standard_cubic_feet_per_day (47811),
	pounds_mass_per_day (47812),
	cubic_meters_per_second (85),
	cubic_meters_per_minute (165),
	cubic_meters_per_hour (135),
	cubic_meters_per_day (249),
	imperial_gallons_per_minute (86),
	milliliters_per_second (198),
	liters_per_second (87),
	liters_per_minute (88),
	liters_per_hour (136),
	us_gallons_per_minute (89),
	us_gallons_per_hour (192),
	// Other
	degrees_angular (90),
	degrees_celsius_per_hour (91),
	degrees_celsius_per_minute (92),
	degrees_fahrenheit_per_hour (93),
	degrees_fahrenheit_per_minute (94),
	joule_seconds (183),
	kilograms_per_cubic_meter (186),
	kilowatt_hours_per_square_meter (137),
	kilowatt_hours_per_square_foot (138),
	watt_hours_per_cubic_meter (250),
	joules_per_cubic_meter (251),
	megajoules_per_square_meter (139),
	megajoules_per_square_foot (140),
	mole_percent (252),
	no_units (95),
	newton_seconds (187),
	newtons_per_meter (188),
	parts_per_million (96),
	parts_per_billion (97),
	pascal_seconds (253),
	percent (98),
	percent_obscuration_per_foot (143),
	percent_obscuration_per_meter (144),
	percent_per_second (99),
	per_minute (100),
	per_second (101),
	psi_per_degree_fahrenheit (102),
	radians (103),
	radians_per_second (184),
	revolutions_per_minute (104),
	square_meters_per_newton (185),
	watts_per_meter_per_degree_kelvin (189),
	watts_per_square_meter_degree_kelvin (141),
	per_mille (207),
	grams_per_gram (208),
	kilograms_per_kilogram (209),
	grams_per_kilogram (210),
	milligrams_per_gram (211),
	milligrams_per_kilogram (212),
	grams_per_milliliter (213),
	grams_per_liter (214),
	milligrams_per_liter (215),
	micrograms_per_liter (216),
	grams_per_cubic_meter (217),
	milligrams_per_cubic_meter (218),
	micrograms_per_cubic_meter (219),
	nanograms_per_cubic_meter (220),
	grams_per_cubic_centimeter (221),
	becquerels (222),
	kilobecquerels (223),
	megabecquerels (224),
	gray (225),
	milligray (226),
	microgray (227),
	sieverts (228),
	millisieverts (229),
	microsieverts (230),
	microsieverts_per_hour (231),
	millirems (47814),
	millirems_per_hour (47815),
	decibels_a (232),
	nephelometric_turbidity_unit (233),
	pH (234),
	grams_per_square_meter (235),
	minutes_per_degree_kelvin (236),
	;

    private final int code;
    
    private BACnetEngineeringUnit(int code) {
        this.code = code;
    }
    
    public int getBACnetEnumValue() {
        return code;
    }
    
    /** Get type value
     * 
     * @param val
     * @return null if no type for the value is found. The value may correspond to a custom type.
     */
    public static BACnetEngineeringUnit forEnumValue(int val) {
        for (BACnetEngineeringUnit o: values()) {
            if (val == o.getBACnetEnumValue()){
                return o;
            }
        }
        return null;
        //throw new IllegalArgumentException("unknown enum value: " + val);
    }

}
