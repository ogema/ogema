package com.example.app.windowheatcontrol.config;

import org.ogema.core.model.units.TemperatureResource;
import org.ogema.model.locations.Room;
import org.ogema.model.prototypes.Configuration;

/** 
 * Put an instance of this resource type for each program into a ResourceList
 */
public interface RoomConfig extends Configuration {

	/**
	 * A reference to the room to which this setting applies
	 * @return
	 */
	Room targetRoom();
	
	/**
	 * Temperature to be set by room controller when a window is opened in the room.
	 * @return
	 */
	TemperatureResource windowOpenTemperature();
	
}
