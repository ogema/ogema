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
package org.ogema.tools.resource.util;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.core.model.Resource;

/**
 * Utility methods for de-/serialization of resources.
 */
public class SerializationUtils {
	
	// no need to construct this
	private SerializationUtils() {}

    /**
     * Remove subresources of a specific type from a json representation of a resource.
     * 
     * @param resourceJson
     * 		a serialized resource in json format
     * @param typeToBeRemoved
     * 		resources of this type will be removed from json
     * @param recursive
     * 		parse the resource tree recursively, to look for elements of type <code>typeToBeRemoved</code>
     * @throws JSONException
     *  	if input is not a valid json string
     */
    public static String removeSubresources(String resourceJson, Class<? extends Resource> typeToBeRemoved, boolean recursive) throws JSONException {
    	JSONObject obj = new JSONObject(resourceJson);
    	removeSubresources(obj, typeToBeRemoved, recursive);
    	return obj.toString(4);
    }
    
    public static String removeSubresources(String resourceJson, Class<? extends Resource>[] typesToBeRemoved, String[] relativePaths, boolean recursive) throws JSONException {
    	JSONObject obj = new JSONObject(resourceJson);
    	removeSubresources(obj, typesToBeRemoved, relativePaths, recursive);
    	return obj.toString(4);
    }
	
    @SuppressWarnings("unchecked")
	public static void removeSubresources(JSONObject resourceJson, Class<? extends Resource> typeToBeRemoved, boolean recursive) {
    	removeSubresources(resourceJson, new Class[]{typeToBeRemoved}, null, recursive);
    }
    
    /**
     * Remove subresources of a specific type from a json representation of a resource.
     * 
     * @param resourceJson
     * 		a serialized resource in json format
     * @param typesToBeRemoved
     * 		resources of these types will be removed from json
     * 		Note: this probably does not work for schedules and array resources yet.
     * @param subresourceNames
     * 		subresources of these names will be removed from json
     * @param recursive
     * 		parse the resource tree recursively, to look for elements of type <code>typeToBeRemoved</code>
     */
    public static void removeSubresources(JSONObject resourceJson, Class<? extends Resource>[] typesToBeRemoved, String[] subresourceNames, boolean recursive) {
    	if (!resourceJson.has("subresources"))
    		return;
    	JSONArray array = resourceJson.getJSONArray("subresources");
        Iterator<Object> it = array.iterator();
        while(it.hasNext()) {
        	JSONObject sub = (JSONObject) it.next();
        	Iterator<String> keyIt = sub.keys();
        	
//        	String identifier = getIdentifier(typeToBeRemoved);
//        	if (!sub.has(identifier)) 
//        		continue;
//        	JSONObject subsub= sub.getJSONObject(identifier);
        	
        	JSONObject subsub = sub.getJSONObject(keyIt.next()); // should have exactly one element
        	String type = subsub.getString("type");
        	boolean done = false;
        	if (typesToBeRemoved != null) {
        		for (Class<? extends Resource> t: typesToBeRemoved) {
        			if (type.equals(t.getName())) {
        				it.remove();
        				done = true;
        				break;
        			}
        		}
        	}
        	if (done)
        		continue;
        	final String name = subsub.getString("name");
        	if (subresourceNames != null) {
        		for (String subresource : subresourceNames) {
        			if (name.equals(subresource)) {
        				it.remove();
        				done = true;
        				break;
        			}
        		}
        		 
        	}
        	if (done)
        		continue;
        	if (recursive) {
        		removeSubresources(subsub, typesToBeRemoved, subresourceNames, recursive);
        	}
        }
    }
    
/*    private static String getIdentifier(Class<? extends Resource> type) {
    	if (SingleValueResource.class.isAssignableFrom(type) || ResourceList.class.isAssignableFrom(type)) {
    		if (PhysicalUnitResource.class.isAssignableFrom(type))
    			type = FloatResource.class;
    		return type.getSimpleName();
    	}
    	return "resource";
    }
*/
    
}
