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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.core.model.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
    
    public static String removeSubresourcesXml(String resourceXml, Class<? extends Resource> typeToBeRemoved, boolean recursive) throws SAXException, IOException, ParserConfigurationException, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
    	final Document document = getDocument(resourceXml);
    	removeSubresourcesXml(document, typeToBeRemoved, recursive);
    	return toString(document);
    }
    
    public static String removeSubresourcesXml(String resourceXml, Class<? extends Resource>[] typesToBeRemoved, String[] relativePaths, boolean recursive) throws SAXException, IOException, ParserConfigurationException, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
    	final Document document = getDocument(resourceXml);
    	removeSubresourcesXml(document, typesToBeRemoved, relativePaths, recursive);
    	return toString(document);
    }
	
    @SuppressWarnings("unchecked")
	public static void removeSubresourcesXml(Document resource, Class<? extends Resource> typeToBeRemoved, boolean recursive) {
    	removeSubresourcesXml(resource, new Class[]{typeToBeRemoved}, null, recursive);
    }
    
    public static void removeSubresourcesXml(final Node resourceXml, final Class<? extends Resource>[] typesToBeRemoved, 
    		final String[] subresourceNames, final boolean recursive) {
    	final String[] typeNames;
    	if (typesToBeRemoved != null) {
    		typeNames = new String[typesToBeRemoved.length];
    		int ind = 0;
    		for (Class<? extends Resource> type : typesToBeRemoved)
    			typeNames[ind++] = type.getName();
    	} else
    		typeNames = null;
    	final NodeList nodeList = resourceXml.getChildNodes();
    	Node node;
    	for (int i=0;i<nodeList.getLength();i++) {
    		node = nodeList.item(i);
    		final String nname = node.getNodeName();
    		if (nname == null || (!"resource".equals(nname) && !nname.startsWith("og:")))
    			continue;
    		boolean done = false;
    		if (typesToBeRemoved != null && typesToBeRemoved.length > 0) {
	    		final Node typeNode = getChildNode(node.getChildNodes(), "type");
	    		if (typeNode != null) {
		    		final String type = typeNode.getTextContent();
		    		for (String t : typeNames) {
		    			if (t.equals(type)) {
		    				resourceXml.removeChild(node);
		    				done = true;
		    				break;
		    			}
		    		}
	    		}
    		}
    		if (done)
    			continue;
    		if (subresourceNames != null && subresourceNames.length > 0) {
    			final Node nameNode = getChildNode(node.getChildNodes(), "name");
    			if (nameNode != null) {
    				final String name = nameNode.getTextContent();
		    		for (String n : subresourceNames) {
		    			if (n.equals(name)) {
		    				resourceXml.removeChild(node);
		    				done = true;
		    				break;
		    			}
		    		}
    			}
    		}
    		if (done)
        		continue;
    		if (recursive)
        		removeSubresourcesXml(node, typesToBeRemoved, subresourceNames, recursive);
    	}
    }
    
    private final static String toString(final Document document) throws TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
        final StreamResult result = new StreamResult(new StringWriter());
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document), result);
        return result.getWriter().toString();
    }
    
    private final static Document getDocument(final String xml) throws SAXException, IOException, ParserConfigurationException {
       return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }
    
    private final static Node getChildNode(final NodeList nodeList, final String id) {
    	Node node;
    	for (int i=0;i<nodeList.getLength();i++) {
    		node = nodeList.item(i);
    		if (id.equals(node.getNodeName()))
    			return node;
    	}
    	return null;
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
