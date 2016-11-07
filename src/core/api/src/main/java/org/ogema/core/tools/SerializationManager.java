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
package org.ogema.core.tools;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;

import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.recordeddata.ReductionMode;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;

/**
 * The SerializationManager provides methods to serialize Java objects,
 * particularly OGEMA-specific ones, to text strings. This also defines a format
 * for the exchange of OGEMA objects (resources, widgets, resource access
 * declarations) with external communication partners.<br>
 *
 * The message-defining XSD provides two ways in which a resource's sub-resource
 * can be added to the text message: as a full representation or as a link. The
 * actual choice partly depends on the parameters set on this
 * SerializationManager. For example, sub-resources are added as links once the
 * maximum recursion depth is reached. If the same resource is encountered more
 * than once during serialization, subsequent encounters are always included as
 * links (to avoid messages blowing up in case of loops in the resource graph).
 *
 * Some parameters defining the serialization behavior can be configured via the
 * methods
 * {@link #setMaxDepth(int) } {@link #setFollowReferences(boolean)}
 * and {@link #setSerializeSchedules(boolean)}. The defaults are:<br>
 * * setMaxDepth(X), with X much larger than one (effectively recursive) <br>
 * * setFollowReferences(true) <br>
 * * setSerializeSchedules(false) <br>
 */
public interface SerializationManager {

	/**
	 * Configures the maximum depth of subresources parsed during the
	 * serialization. Subresources with a larger depth (relative to the starting
	 * resource) are serialized as resource links, instead. Resources appearing
	 * a second time during serializations (which can occur when loops exist in
	 * the resource graph) are always parsed as resource links in the deeper
	 * level, even if the maximum depth is not reached, yet.
	 *
	 * @param value Maximum recursion depth to set. A recursion value of N means
	 * that the N+1'th depth level is serialized as resource links. The original
	 * resource counts as depth one.
	 */
	void setMaxDepth(int value);

	/**
	 * Gets the currently set maximum depth for serializing sub-resources.
	 *
	 * @return Maximum value as defined in {@link #setMaxDepth(int)}.
	 */
	int getMaxDepth();

	/**
	 * Sets the behavior with regards to following references during
	 * serialization.
	 *
	 * @param status If true, references are followed as if they were "real"
	 * subresources. If false, references are not followed but embedded into the
	 * resulting object as a reference.
	 */
	void setFollowReferences(boolean status);

	/**
	 * Gets the currently-set behavior with regards to following references in
	 * the serialization process. See {@link #setFollowReferences(boolean)}.
	 */
	boolean getFollowReferences();

	/**
	 * Defines whether sub-resources of type Schedule are serialized or not.
	 * Note that this only affects sub-resources of a Resource passed to methods
	 * of a SerializationManager. If a Resource of type Schedule is directly
	 * passed as an argument it is always serialized - even if the serialization
	 * of sub-schedules is turned off.
	 *
	 * @param status If true, schedules are written into the return string as if
	 * they were normal resources (the exact meaning of this hence also depends
	 * on the parameters recursive and followReferences). If false, schedules
	 * are always included as a resource link and not written recursively
	 * (writeSchedules==false overrides a recursive=true parameter for
	 * schedules).
	 */
	void setSerializeSchedules(boolean status);

	/**
	 * Gets the current behavior with regards to serializing sub-resource
	 * schedules. See {@link #setSerializeSchedules(boolean)}.
	 */
	boolean getSerializeSchedules();

	/**
	 * Builds a Json representation of the resource.
	 *
	 * @return Builds a Json representation of the Resource.
	 */
	String toJson(Resource resource);

	/**
	 * Parses a Java object as JSON String. If the object contains objects of
	 * type Resource they are parsed as in
	 * {@link #toJson(org.ogema.model.Resource)}.
	 */
	String toJson(Object object);

	/**
	 * Builds an Xml representation of a resource.
	 */
	String toXml(Resource resource);

	/**
	 * write a Resource object to output as Json.
	 */
	void writeJson(Writer output, Resource resource) throws IOException;

	/**
	 * writes an object to output as Json. @see
	 * {@link #toJson(java.lang.Object)}.
	 */
	void writeJson(Writer output, Object object) throws IOException;

	/**
	 * @see #writeXml(java.io.Writer, org.ogema.core.model.schedule.Schedule,
	 * long, long)
	 */
	void writeJson(Writer output, Schedule sched, long start, long end) throws IOException;

	/**
	 * @see #writeXml(java.io.Writer, org.ogema.core.model.schedule.Schedule,
	 * long, long)
	 */
	String toJson(Writer output, Schedule sched, long start, long end);

	/**
	 * writes a {@link Schedule} using only the subset of values contained in
	 * the interval {@code [start, end)}. The output will only include time
	 * series values if {@code SerializeSchedules} is set for this
	 * SerializationManager.
	 *
	 * @param output
	 * @param sched
	 * @param start
	 * @param end
	 */
	void writeXml(Writer output, Schedule sched, long start, long end) throws IOException;

	/**
	 * @see #writeXml(java.io.Writer, org.ogema.core.model.schedule.Schedule,
	 * long, long)
	 */
	String toXml(Writer output, Schedule sched, long start, long end);

	/**
	 * write a Resource object to output as Xml.
	 */
	void writeXml(Writer output, Resource resource) throws IOException;

	/**
	 * Write RecordedData values as XML
	 *
	 * @param writer the value of writer
	 * @param res the value of res
	 * @param data the value of data
	 * @param startTime the value of startTime
	 * @param endTime the value of endTime
	 * @param interval the value of interval
	 * @param mode the value of mode
	 * @see RecordedData#getValues(long, long, long,
	 * org.ogema.core.timeseries.ReductionMode)
	 */
	void writeXml(Writer writer, Resource res, RecordedData data, long startTime, long endTime, long interval,
			ReductionMode mode) throws IOException;

	/**
	 * Write RecordedData values as JSON
	 *
	 * @param writer the value of writer
	 * @param res the value of res
	 * @param data the value of data
	 * @param startTime the value of startTime
	 * @param endTime the value of endTime
	 * @param interval the value of interval
	 * @param mode the value of mode
	 * @see RecordedData#getValues(long, long, long,
	 * org.ogema.core.timeseries.ReductionMode)
	 */
	void writeJson(Writer writer, Resource res, RecordedData data, long startTime, long endTime, long interval,
			ReductionMode mode) throws IOException;

	/**
	 * Apply the Json object to a resource object: For each parameter defined in
	 * the Json message, the respective parameter in the resource object adapted
	 * to the value given. If the parameter already has the target value, the
	 * value is re-set only if forceUpdate is true (which can trigger
	 * callbacks). Sub-resources contained in the message that do not yet exist
	 * are created by this. This method is applied recursively to all
	 * sub-resources contained in the message.
	 *
	 * @param json A (possibly incomplete) resource structure in Json format
	 * that shall be applied to the parameter resource.
	 * @param resource The resource object to apply the new values to.
	 * @param forceUpdate Values are set to a new value if the old value differs
	 * from the new one or if forceUpdate is true. If forceUpdate is false and a
	 * new value equals the old one, no update is performed.
	 */
	void applyJson(String json, Resource resource, boolean forceUpdate);

	/**
	 * @see #applyJson(java.lang.String, org.ogema.core.model.Resource, boolean) 
	 */
	void applyJson(Reader jsonReader, Resource resource, boolean forceUpdate);

	/**
	 * Applies an Xml object representing a resource to a resource in the
	 * system.
	 *
	 * @see #applyJson(java.lang.String, org.ogema.core.model.Resource, boolean) 
	 */
	void applyXml(String xml, Resource resource, boolean forceUpdate);

	/**
	 * @see #applyXml(String, Resource,boolean)
	 */
	void applyXml(Reader xmlReader, Resource resource, boolean forceUpdate);

	/**
	 * Apply the Json object representing a resource or a set of resources (such
	 * as the JSON representation of a resource access declaration). Each
	 * resource object represented in the message is applied to the respective
	 * resource in the resource in the system
	 * {@link #applyJson(String, Resource, boolean)}. If for a resource
	 * represented in the message no suitable equivalent is found, e.g. because
	 * the resource path could not be extracted from the message, no action is
	 * taken for this resource. Other resources are still processed as normal,
	 * then.
	 *
	 * This is performed with the caller's access permissions.
	 */
	void applyJson(String json, boolean forceUpdate);

	/**
	 * @see #applyJson(java.lang.String, boolean) 
	 */
	void applyJson(Reader jsonReader, boolean forceUpdate);

	/**
	 * @see #applyJson(String,boolean)
	 */
	void applyXml(String xml, boolean forceUpdate);

	/**
	 * @see #applyXml(String,boolean)
	 */
	void applyXml(Reader xmlReader, boolean forceUpdate);

	/**
	 * @see #createFromXml(java.lang.String)
	 * @param xmlReader reader for loading the xml document.
	 * @return the newly created top level resource.
	 */
	<T extends Resource> T createFromXml(Reader xmlReader) throws ResourceAlreadyExistsException;

	/**
	 * Create the resource described in the XML document as a new top level
	 * resource.
	 *
	 * @param xml String containing the XML document.
	 * @throws ResourceAlreadyExistsException if a top level resource with the
	 * same name already exists.
	 * @return the newly created top level resource.
	 */
	/*XXX the create and apply methods currently have no way of reporting problems with the deserialized resources (e.g. type mismatch)
	 InvalidResourceTypeException: type mismatch for optional element or decorator.
	 NoSuchResourceException: document contains reference to non-existent resource.
	 Resource already exists: exception or behave like apply...?
	 also possible: ClassNotFoundException, invalid ogema name ( = NoSuchResourceEx? )
	 */
	<T extends Resource> T createFromXml(String xml) throws ResourceAlreadyExistsException;

	/**
	 * @see #createFromXml(java.lang.String, org.ogema.model.Resource)
	 * @param xmlReader reader for loading the xml document.
	 * @param parent resource in which to add the new resource.
	 * @return the newly created sub-resource.
	 */
	<T extends Resource> T createFromXml(Reader xmlReader, Resource parent);

	/**
	 * Add the resource described in the XML document as a new subresource of
	 * the given parent resource.
	 *
	 * @param xml String containing the XML document.
	 * @param parent resource in which to add the new resource.
	 * @return the newly created sub-resource.
	 */
	<T extends Resource> T createFromXml(String xml, Resource parent);

	/**
	 * @see #createFromXml(java.lang.String)
	 * @param json the JSON document containing the resource to be created.
	 * @return the newly created top level resource.
	 */
	<T extends Resource> T createFromJson(String json);

	/**
	 * @see #createFromXml(java.io.Reader)
	 * @param json reader containing a JSON representation of an OGEMA resource.
	 * @return the newly created top level resource.
	 */
	<T extends Resource> T createFromJson(Reader json);

	/**
	 * @see #createFromXml(java.io.Reader, org.ogema.model.Resource)
	 * @param json reader containing a JSON representation of an OGEMA resource.
	 * @param parent resource on which to create the new resource.
	 * @return the newly created sub-resource.
	 */
	<T extends Resource> T createFromJson(Reader json, Resource parent);

	/**
	 * @see #createFromXml(java.lang.String, org.ogema.model.Resource)
	 * @param json the JSON document containing the resource to be created.
	 * @param parent resource on which to create the new resource.
	 * @return the newly created sub-resource.
	 */
	<T extends Resource> T createFromJson(String json, Resource parent);
	
	
	/**
	 * Builds a Json representation of the resources.
	 * @param resources
	 * @return
	 */
	String toJson(Collection<Resource> resources);

	/**
	 *  Builds an Xml representation of the resources.
	 * @param resources
	 * @return
	 */
	String toXml(Collection<Resource> resources);
	
	/**
	 * write a collection of Resource objects to output as Json.
	 */
	void writeJson(Writer output, Collection<Resource> resources) throws IOException;
	
	/**
	 * write a collection of Resource objects to output as Xml.
	 */
	void writeXml(Writer output, Collection<Resource> resources) throws IOException;
	
	/**
	 * Add the resources described in the XML document as new toplevel resources
	 *
	 * @param xml String containing the XML document for a collection of resources. 
	 * 		Note that this differs from the format for a single resource.
	 * @return the newly created sub-resources.
	 */
	Collection<Resource> createResourcesFromXml(String xml);
	
	/**
	 * Add the resources described in the Json document as new toplevel resources
	 *
	 * @param json String containing the JSON document for a collection of resources. 
	 * 		Note that this differs from the format for a single resource.
	 * @return the newly created sub-resources.
	 */
	Collection<Resource> createResourcesFromJson(String json);
	
	/**
	 * Add the resources described in the XML document as new subresources of
	 * the given parent resource.
	 *
	 * @param xml String containing the XML document for a collection of resources. 
	 * 		Note that this differs from the format for a single resource.
	 * @param parent resource in which to add the new resource.
	 * @return the newly created sub-resources.
	 */
	Collection<Resource> createResourcesFromXml(String xml, Resource parent);
	
	/**
	 * Add the resources described in the JSON document as new subresources of
	 * the given parent resource.
	 *
	 * @param json String containing the JSON document for a collection of resources. 
	 * 		Note that this differs from the format for a single resource.
	 * @param parent resource in which to add the new resource.
	 * @return the newly created sub-resources.
	 */
	Collection<Resource> createResourcesFromJson(String json, Resource parent);

}
