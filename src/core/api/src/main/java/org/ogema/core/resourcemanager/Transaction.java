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
package org.ogema.core.resourcemanager;

import java.util.Collection;
import org.ogema.core.model.Resource;
import org.ogema.core.model.array.BooleanArrayResource;
import org.ogema.core.model.array.ByteArrayResource;
import org.ogema.core.model.array.FloatArrayResource;
import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.array.TimeArrayResource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.OpaqueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;

/**
 * Defines a resource transaction. All resources added to this are read and write
 * in a single step with no other resource writes taking place at the same time.
 * Also supports to activate and deactivate resources.
 */
public interface Transaction {

	/**
	 * Adds a resource and all of its sub-resources to the transaction.
	 * @param rootResource root of the tree to add.
	 * @param addReferencedSubresources if true, referenced sub-resources are added. If false, only direct sub-resources are added.
	 */
	void addTree(Resource rootResource, boolean addReferencedSubresources);

	/**
	 * Adds a resource to the set of resources to be read/written in a single
	 * transaction.
	 * @param resource Reference to the resource to be added.
	 */
	void addResource(Resource resource);

	/**
	 * Adds resources to the set of resources to be read/written in a single 
	 * transaction.
	 * @param resources Set of resources to be added.
	 */
	void addResources(Collection<Resource> resources);

	/**
	 * Gets the set of all resources to be read/written in a single transaction.
	 * @return Copy of the set of all connected resources.     
	 */
	Collection<Resource> getResources();

	/**
	 * Returns a copy of all resources in this transaction that are of the given type.
	 */
	<T extends Resource> Collection<T> getResources(Class<T> resourceType);

	/**
	 * Removes resources from the set of resources contained in this transaction.
	 * A connected resource is assumed to be equal to an element in the resources
	 * parameter if their paths are equal. To get the weaker equality of location-identity
	 * use {@link #removeResourcesByLocation(java.util.Collection) removeResourcesByLocation}.
	 */
	void removeResources(Collection<? extends Resource> resources);

	/**
	 * Removes connected resources when their locations equals that of a resource
	 * passed to this method.
	 * @see #removeResources(java.util.Collection) 
	 */
	void removeResourcesByLocation(Collection<? extends Resource> resources);

	/**
	 * Sets the value for the resource that shall be written in the next
	 * write transaction.
	 * @throws NoSuchResourceException if the resource passed had not been added
	 * to the transaction previously.
	 */
	void setFloat(FloatResource resource, float value) throws NoSuchResourceException;

	/**
	 * Gets the last value read from r during last read() transactions.
	 * @return If the resource has been a non-virtual resource during the last
	 * read action this returns the value that was read. If the resource had
	 * been virtual this returns null. If not read action had been performed
	 * at all, this also returns null.
	 * @throws NoSuchResourceException if the resource passed had not been added
	 * to the transaction previously.
	 */
	Float getFloat(FloatResource resource) throws NoSuchResourceException;

	/**
	 * @see #setFloat(org.ogema.core.model.simple.FloatResource, float) 
	 */
	void setInteger(IntegerResource resource, int value) throws NoSuchResourceException;

	/**
	 * @see #getFloat(org.ogema.core.model.simple.FloatResource) 
	 */
	Integer getInteger(IntegerResource resource) throws NoSuchResourceException;

	/**
	 * @see #setFloat(org.ogema.core.model.simple.FloatResource, float) 
	 */
	void setBoolean(BooleanResource resource, boolean value) throws NoSuchResourceException;

	/**
	 * @see #getFloat(org.ogema.core.model.simple.FloatResource) 
	 */
	Boolean getBoolean(BooleanResource resource) throws NoSuchResourceException;

	/**
	 * @see #setFloat(org.ogema.core.model.simple.FloatResource, float) 
	 */
	void setString(StringResource resource, String value) throws NoSuchResourceException;

	/**
	 * @see #getFloat(org.ogema.core.model.simple.FloatResource) 
	 */
	String getString(StringResource resource) throws NoSuchResourceException;

	/**
	 * @see #setFloat(org.ogema.core.model.simple.FloatResource, float) 
	 */
	void setTime(TimeResource resource, long value) throws NoSuchResourceException;

	/**
	 * @see #getFloat(org.ogema.core.model.simple.FloatResource) 
	 */
	Long getTime(TimeResource resource) throws NoSuchResourceException;

	/**
	 * @see #setFloat(org.ogema.core.model.simple.FloatResource, float) 
	 * @deprecated Type {@link OpaqueResource} is deprecated. Use {@link ByteArrayResource} instead.
	 */
	void setByteArray(OpaqueResource resource, byte[] values) throws NoSuchResourceException;

	/**
	 * @see #getFloat(org.ogema.core.model.simple.FloatResource) 
	 * @deprecated Type {@link OpaqueResource} is deprecated. Use {@link ByteArrayResource} instead.       
	 */
	byte[] getByteArray(OpaqueResource resource) throws NoSuchResourceException;

	/**
	 * @see #setFloat(org.ogema.core.model.simple.FloatResource, float) 
	 */
	void setByteArray(ByteArrayResource resource, byte[] values) throws NoSuchResourceException;

	/**
	 * @see #getFloat(org.ogema.core.model.simple.FloatResource) 
	 */
	byte[] getByteArray(ByteArrayResource resource) throws NoSuchResourceException;

	/**
	 * @see #setFloat(org.ogema.core.model.simple.FloatResource, float) 
	 */
	void setFloatArray(FloatArrayResource resource, float[] values) throws NoSuchResourceException;

	/**
	 * @see #getFloat(org.ogema.core.model.simple.FloatResource) 
	 */
	float[] getFloatArray(FloatArrayResource resource) throws NoSuchResourceException;

	/**
	 * @see #setFloat(org.ogema.core.model.simple.FloatResource, float) 
	 */
	void setIntegerArray(IntegerArrayResource resource, int[] values) throws NoSuchResourceException;

	/**
	 * @see #getFloat(org.ogema.core.model.simple.FloatResource) 
	 */
	int[] getIntegerArray(IntegerArrayResource resource) throws NoSuchResourceException;

	/**
	 * @see #setFloat(org.ogema.core.model.simple.FloatResource, float) 
	 */
	void setBooleanArray(BooleanArrayResource resource, boolean[] values) throws NoSuchResourceException;

	/**
	 * @see #getFloat(org.ogema.core.model.simple.FloatResource) 
	 */
	boolean[] getBooleanArray(BooleanArrayResource resource) throws NoSuchResourceException;

	/**
	 * @see #setFloat(org.ogema.core.model.simple.FloatResource, float) 
	 */
	void setStringArray(StringArrayResource resource, String[] values) throws NoSuchResourceException;

	/**
	 * @see #getFloat(org.ogema.core.model.simple.FloatResource) 
	 */
	String[] getStringArray(StringArrayResource resource) throws NoSuchResourceException;

	/**
	 * @see #setFloat(org.ogema.core.model.simple.FloatResource, float) 
	 */
	void setTimeArray(TimeArrayResource resource, long[] values) throws NoSuchResourceException;

	/**
	 * @see #getFloat(org.ogema.core.model.simple.FloatResource) 
	 */
	long[] getTimeArray(TimeArrayResource resource) throws NoSuchResourceException;

	/**
	 * Set the schedule content to a new function, that is written in the {@link #write()} operation.
	 * @param schedule Target schedule to set.
	 * @param function the new state for the schedule. Note that the TimeSeries interface extends ReadOnlyTimeSeries, so anything that implements a TimeSeries can also be added.
	 * @throws NoSuchResourceException if the schedule passed had not been added
	 * to the transaction previously.
	 */
	void setSchedule(Schedule schedule, ReadOnlyTimeSeries function) throws NoSuchResourceException;

	/**
	 * Gets the state of the schedule read during the last {@link #read()} or
	 * {@link #write()} operation.
	 * @param schedule 
	 * @return Returns a copy of the last-read content. If the schedule had been virtual in
	 * the last read transaction or if no read has been performed yet, this returns null.
	 * @throws NoSuchResourceException if the resource passed had not been added
	 * to the transaction previously.         
	 */
	ReadOnlyTimeSeries getSchedule(Schedule schedule) throws NoSuchResourceException;

	/**
	 * Perform a read transaction. If a resource in the transaction does not exist
	 * (is a virtual resource), the value read by this will be set to null.
	 */
	void read();

	/**
	 * Writes the values set to this in a single write transaction.
	 * @throws VirtualResourceException if one of the resources added to this
	 * do not exist (are virtual) the write operations are not performed and
	 * this exception is thrown, instead.
	 */
	void write() throws VirtualResourceException;

	/**
	 * Activates all the resources in a single transaction.
	 */
	void activate();

	/**
	 * Deactivate all the resources in a single transaction.
	 */
	void deactivate();
}
