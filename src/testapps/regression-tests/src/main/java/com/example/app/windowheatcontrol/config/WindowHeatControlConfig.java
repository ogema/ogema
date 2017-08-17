package com.example.app.windowheatcontrol.config;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.model.prototypes.Configuration;

/** 
 * The global configuration resource type for this app.
 */
public interface WindowHeatControlConfig extends Configuration {

	/**
	 * Temperature to be set by room controller when a window is opened, 
	 * if no special temperature is configured for a room.
	 * @return
	 */
	TemperatureResource defaultWindowOpenTemperature();
	
	/**
	 * List of per-room settings.
	 * @return
	 */
	ResourceList<RoomConfig> roomConfigurations();
	
}
