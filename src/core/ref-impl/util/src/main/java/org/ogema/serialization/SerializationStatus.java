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
package org.ogema.serialization;

import java.util.HashSet;
import java.util.Set;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;

import org.ogema.core.tools.SerializationManager;

/**
 * Object for holding information about a current serialization run. On top of the serialization parameters defined on
 * the SerializationManager interface this includes tracing information about the current recursion depth (for limiting
 * the depth and the resources that were already serialized (for preventing loops).
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public final class SerializationStatus {

	private final SerializationManager manager;
	private int currentDepth = 1;
	// list of locations that were already parsed.
	private Set<String> parsedLocations = new HashSet<>();

	/**
	 * Constructs a new SerializationStatus object. Note that this constructor is package private on purpose: Wasn't it
	 * for the schedules in their separate package the whole class would be package private, since the information held
	 * by this is relevant only during the process of serialization.
	 */
	SerializationStatus(SerializationManager parameters) {
		manager = parameters;
	}
    
	public int maxDepth() {
		return manager.getMaxDepth();
	}

	public boolean followReferences() {
		return manager.getFollowReferences();
	}

	public boolean serializeSchedules() {
		return manager.getSerializeSchedules();
	}

	/**
	 * Should the current depth of recursion still be parsed?
	 */
	boolean parseCurrentDepth() {
		return currentDepth <= manager.getMaxDepth();
	}

	/**
	 * Has the location already been parsed?
	 */
	boolean locationParsed(String location) {
		return parsedLocations.contains(location);
	}

	void addParsedLocation(String location) {
		parsedLocations.add(location);
	}
    
    /* include resource only as link. */
    boolean linkResource(Resource res){
        return !parseCurrentDepth()
                || (Schedule.class.isAssignableFrom(res.getResourceType()) && !serializeSchedules())
                || (res.isReference(false) && !followReferences())
                || locationParsed(res.getLocation("/"));
    }

    public SerializationStatus increaseDepth() {
        SerializationStatus s = new SerializationStatus(manager);
        s.currentDepth = currentDepth+1;
        s.parsedLocations = parsedLocations;
        return s;
    }    
    
}
