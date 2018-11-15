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
public final class SerializationStatus implements Cloneable {

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
    public boolean linkResource(Resource res){
        return !parseCurrentDepth()
                || (Schedule.class.isAssignableFrom(res.getResourceType()) && !serializeSchedules())
                || (res.isReference(false) && !followReferences())
                || locationParsed(res.getLocation("/"));
    }

    // why create a new one?
    public SerializationStatus increaseDepth() {
        SerializationStatus s = new SerializationStatus(manager);
        s.currentDepth = currentDepth+1;
        s.parsedLocations = parsedLocations;
        return s;
    }    
    
    /**
     * parsed locations in the new instance will not be reflected in parsed
     * locations in the old one.
     */
    @Override
	public Object clone() throws CloneNotSupportedException {
    	SerializationStatus other = new SerializationStatus(manager);
    	other.currentDepth = currentDepth;
    	other.parsedLocations = new HashSet<>(parsedLocations);
    	return other;
    }
    
}
