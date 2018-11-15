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
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.transaction.ResourceTransaction;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;

/**
 * Defines a resource transaction. All resources added to this are read and written
 * in a single step with no other resource writes taking place at the same time.
 * Also supports to activate and deactivate resources.
 * 
 * @deprecated use {@link ResourceTransaction} instead
 */

@Deprecated 
public interface Transaction {

	/**
	 * Adds a resource and all of its currently-existing sub-resources to the transaction.
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
	 * Gets the last value read the during last read() transaction or the value
	 * contained in the resource after the last successful write() transaction
	 * (whichever is more recent).
	 * @return If the resource has been a non-virtual resource during the last
	 * read action this returns the value that was read. If the resource had
	 * been virtual this returns null. If not read action had been performed
	 * at all, this also returns null.
	 * @throws NoSuchResourceException if the resource passed had not been added
	 * to the transaction previously.
	 */
	Float getFloat(FloatResource resource) throws NoSuchResourceException;

	/**
	 * See {@link #setFloat(org.ogema.core.model.simple.FloatResource, float)}
	 */
	void setInteger(IntegerResource resource, int value) throws NoSuchResourceException;

	/**
	 * See {@link #getFloat(org.ogema.core.model.simple.FloatResource)}
	 */
	Integer getInteger(IntegerResource resource) throws NoSuchResourceException;

	/**
	 * See {@link #setFloat(org.ogema.core.model.simple.FloatResource, float)}
	 */
	void setBoolean(BooleanResource resource, boolean value) throws NoSuchResourceException;

	/**
	 * See {@link #getFloat(org.ogema.core.model.simple.FloatResource)}
	 */
	Boolean getBoolean(BooleanResource resource) throws NoSuchResourceException;

	/**
	 * See {@link #setFloat(org.ogema.core.model.simple.FloatResource, float)}
	 */
	void setString(StringResource resource, String value) throws NoSuchResourceException;

	/**
	 * See {@link #getFloat(org.ogema.core.model.simple.FloatResource)}
	 */
	String getString(StringResource resource) throws NoSuchResourceException;

	/**
	 * @see #setFloat(org.ogema.core.model.simple.FloatResource, float) 
	 */
	void setTime(TimeResource resource, long value) throws NoSuchResourceException;

	/**
	 * See {@link #getFloat(org.ogema.core.model.simple.FloatResource)}
	 */
	Long getTime(TimeResource resource) throws NoSuchResourceException;

	/**
	 * @see #setFloat(org.ogema.core.model.simple.FloatResource, float) 
	 * @deprecated Type {@link org.ogema.core.model.simple.OpaqueResource} is deprecated. Use {@link ByteArrayResource} instead.
	 */
	@Deprecated
	void setByteArray(org.ogema.core.model.simple.OpaqueResource resource, byte[] values)
			throws NoSuchResourceException;

	/**
	 * @see #getFloat(org.ogema.core.model.simple.FloatResource) 
	 * @deprecated Type {@link org.ogema.core.model.simple.OpaqueResource} is deprecated. Use {@link ByteArrayResource} instead.       
	 */
	@Deprecated
	byte[] getByteArray(org.ogema.core.model.simple.OpaqueResource resource) throws NoSuchResourceException;

	/**
	 * See {@link #setFloat(org.ogema.core.model.simple.FloatResource, float)}
	 */
	void setByteArray(ByteArrayResource resource, byte[] values) throws NoSuchResourceException;

	/**
	 * See {@link #getFloat(org.ogema.core.model.simple.FloatResource)}
	 */
	byte[] getByteArray(ByteArrayResource resource) throws NoSuchResourceException;

	/**
	 * See {@link #setFloat(org.ogema.core.model.simple.FloatResource, float)}
	 */
	void setFloatArray(FloatArrayResource resource, float[] values) throws NoSuchResourceException;

	/**
	 * See {@link #getFloat(org.ogema.core.model.simple.FloatResource)}
	 */
	float[] getFloatArray(FloatArrayResource resource) throws NoSuchResourceException;

	/**
	 * See {@link #setFloat(org.ogema.core.model.simple.FloatResource, float)}
	 */
	void setIntegerArray(IntegerArrayResource resource, int[] values) throws NoSuchResourceException;

	/**
	 * See {@link #getFloat(org.ogema.core.model.simple.FloatResource)}
	 */
	int[] getIntegerArray(IntegerArrayResource resource) throws NoSuchResourceException;

	/**
	 * See {@link #setFloat(org.ogema.core.model.simple.FloatResource, float)}
	 */
	void setBooleanArray(BooleanArrayResource resource, boolean[] values) throws NoSuchResourceException;

	/**
	 * See {@link #getFloat(org.ogema.core.model.simple.FloatResource)}
	 */
	boolean[] getBooleanArray(BooleanArrayResource resource) throws NoSuchResourceException;

	/**
	 * See {@link #setFloat(org.ogema.core.model.simple.FloatResource, float)}
	 */
	void setStringArray(StringArrayResource resource, String[] values) throws NoSuchResourceException;

	/**
	 * See {@link #getFloat(org.ogema.core.model.simple.FloatResource)}
	 */
	String[] getStringArray(StringArrayResource resource) throws NoSuchResourceException;

	/**
	 * See {@link #setFloat(org.ogema.core.model.simple.FloatResource, float)}
	 */
	void setTimeArray(TimeArrayResource resource, long[] values) throws NoSuchResourceException;

	/**
	 * See {@link #getFloat(org.ogema.core.model.simple.FloatResource)}
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

	/**
	 * Sets the active state of a resource.
	 * @param target the resource to modify.
	 * @param recursive activate resursively.
	 * @see Resource#activate(boolean) 
	 */
	//void activate(Resource target, boolean recursive);

	/**
	 * Sets the active state of a resource.
	 * @param target the resource to modify.
	 * @param recursive deactivate resursively.
	 * @see Resource#deactivate(boolean) 
	 */
	//void deactivate(Resource target, boolean recursive);

}
