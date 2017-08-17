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
package org.ogema.core.resourcemanager.transaction;

import java.util.Collection;

import org.ogema.core.channelmanager.measurements.SampledValue;
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
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceAccessException;
import org.ogema.core.resourcemanager.ResourceOperationException;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;

/**
 * A transaction on the resource database, consisting of multiple individual read and/or write operations and structure operations. 
 * Transactions are atomic, in the sense that no intermediate states where only parts of the operations have been executed
 * will ever be seen by an application. No parallel write access to the database is possible, and no parallel read access either,
 * if this transaction contains any write operations. (FIXME not yet satisfied)
 * <br>
 * If any of the operations in a transaction fails, a rollback will be performed, undoing all previous operations,
 * so that the resource graph is not left in an inconsistent state. 
 * In particular, missing permissions to access or manipulate the given resources will lead to the transaction being aborted. 
 * <br>
 * Several methods offered by transactions have a different default behaviour from their {@link Resource}-counterparts, 
 * regarding the handling of virtual or inactive resources. Most importantly, the resource write operations by 
 * default create the target resource if it does not exist, and they activate it if it is found inactive. It is possible to specify the
 * behaviour, though. For examples, see {@link #setFloat(FloatResource, float)} with default behaviour, and 
 * the variant {@link #setFloat(FloatResource, float, WriteConfiguration)} with a configuration parameter.
 * <br>
 * Transactions do not provide explicit methods for adding of subresources. If you need to add a subresource in the transaction
 * (either as decorator or optional element), first retrieve the subresource as a virtual element, using for instance 
 * {@link Resource#getSubResource(String, Class)}, then in the transaction either create it explicitly
 * using {@link #create(Resource)}, or use any of the methods that implicitly create the resource passed, such as
 * {@link #setFloat(FloatResource, float)} or {@link #activate(Resource)}.
 * <br>
 * It is not possible to create top-level resources in a transaction. Since creation without activation of a top-level resource does not
 * cause any listener callbacks, this should not cause any problems; create the resource outside the transaction, then add subresources,
 * set their values and activate them in a transaction.
 */
public interface ResourceTransaction {
	
	/**
	 * Execute the operations that have previously been added to the transaction, including all 
	 * resource write operations (see e.g. {@link #setFloat(FloatResource, float)}), resource read
	 * operations (see e.g. {@link #getFloat(FloatResource)}), and structure operations 
	 * (see e.g. {@link #create(Resource)}, {@link #activate(Resource)} or {@link #setAsReference(Resource, Resource)}).
	 * <br>
	 * If an exception occurs during execution of the transaction, a rollback operation will
	 * be performed, undoing all operations of this transaction that have been executed hitherto, and
	 * the exception is rethrown, wrapped in a {@link ResourceOperationException}. If an exception occurs 
	 * in the rollback operation as well, it is caught and written to the log; 
	 * the transaction will still try to execute the pending rollbacks. 
	 *  
	 * @throws ResourceOperationException
	 * 		If an exception occurs in any of the transaction operations.
	 */
	void commit() throws ResourceOperationException;
	
	/**
	 * Sets the value for the resource that shall be written in the transaction. 
	 * The resource will be created and activated if it does not exist yet 
	 * (is virtual), respectively is inactive. 
	 * <br>
	 * This is equivalent to {@link #setFloat(FloatResource, float, WriteConfiguration)}
	 * with the last arguments equal to {@link WriteConfiguration#CREATE_AND_ACTIVATE}.
	 * @param resource
	 * 		The resource whose value will be written
	 * @param value
	 */
	void setFloat(FloatResource resource, float value);
	
	/** 
	 * Sets the value for the resource that shall be written in the the transaction.
	 * @param resource
	 * 		The resource whose value will be written
	 * @param value
	 * @param configuration
	 * 		Specifies how to deal with inactive or virtual resources. 
	 */
	void setFloat(FloatResource resource, float value, WriteConfiguration configuration);

	/**
	 * Read a resource value in the transaction.<br>
	 * This is equivalent to {@link #getFloat(FloatResource, ReadConfiguration)} with the last arguments equal to 
	 * {@link ReadConfiguration#FAIL}, i.e., in case the resource does not exist or is inactive, the transaction will be aborted.
	 * 
	 * @see #getFloat(FloatResource, ReadConfiguration)
	 * @param resource
	 * @return
	 */
	TransactionFuture<Float> getFloat(FloatResource resource);
	
	/**
	 * Read a resource value in the transaction.<br>
	 * @param resource
	 * 		The resource whose value is going to be read.
	 * @return A transaction future. After the transaction has been executed, the resource value can
	 * be obtained by calling {@link TransactionFuture#getValue()}. If getValue() is called before
	 * the execution of the transaction, or if an exception has occured during execution of the 
	 * transaction (causing a rollback) and the getValue() method is called nevertheless, then an exception
	 * will be thrown.
	 * @param configuration
	 * 		Specifies how to deal with inactive and virtual resources.
	 */
	TransactionFuture<Float> getFloat(FloatResource resource, ReadConfiguration configuration);

	/**
	 * See {@link #setFloat(org.ogema.core.model.simple.FloatResource, float)}
	 */
	void setInteger(IntegerResource resource, int value);
	
	/**
	 * See {@link #setFloat(FloatResource, float, WriteConfiguration)}
	 */
	void setInteger(IntegerResource resource, int value, WriteConfiguration configuration);

	/**
	 * See {@link #getFloat(org.ogema.core.model.simple.FloatResource)}
	 */
	TransactionFuture<Integer> getInteger(IntegerResource resource);
	
	/**
	 * See {@link #getFloat(FloatResource, ReadConfiguration)}
	 */
	TransactionFuture<Integer> getInteger(IntegerResource resource, ReadConfiguration configuration);

	/**
	 * See {@link #setFloat(org.ogema.core.model.simple.FloatResource, float)}
	 */
	void setBoolean(BooleanResource resource, boolean value);
	
	/**
	 * See {@link #setFloat(FloatResource, float, WriteConfiguration)}
	 */
	void setBoolean(BooleanResource resource, boolean value, WriteConfiguration configuration);

	/**
	 * See {@link #getFloat(org.ogema.core.model.simple.FloatResource)}
	 */
	TransactionFuture<Boolean> getBoolean(BooleanResource resource);	
	
	/**
	 * See {@link #getFloat(FloatResource, ReadConfiguration)}
	 */
	TransactionFuture<Boolean> getBoolean(BooleanResource resource, ReadConfiguration configuration);

	/**
	 * See {@link #setFloat(org.ogema.core.model.simple.FloatResource, float)}
	 */
	void setString(StringResource resource, String value);
	
	/**
	 * See {@link #setFloat(FloatResource, float, WriteConfiguration)}
	 */
	void setString(StringResource resource, String value, WriteConfiguration configuration);

	/**
	 * See {@link #getFloat(org.ogema.core.model.simple.FloatResource)}
	 */
	TransactionFuture<String> getString(StringResource resource);
	
	/**
	 * See {@link #getFloat(FloatResource, ReadConfiguration)}
	 */
	TransactionFuture<String> getString(StringResource resource, ReadConfiguration configuration);

	/**
	 * See {@link #setFloat(org.ogema.core.model.simple.FloatResource, float)}
	 */
	void setTime(TimeResource resource, long value);
	
	/**
	 * See {@link #setFloat(FloatResource, float, WriteConfiguration)}
	 */
	void setTime(TimeResource resource, long value, WriteConfiguration configuration);

	/**
	 * See {@link #getFloat(org.ogema.core.model.simple.FloatResource)}
	 */
	TransactionFuture<Long> getTime(TimeResource resource);
	
	/**
	 * See {@link #getFloat(FloatResource, ReadConfiguration)}
	 */
	TransactionFuture<Long> getTime(TimeResource resource, ReadConfiguration configuration);

	/**
	 * See {@link #setFloat(org.ogema.core.model.simple.FloatResource, float)}
	 */
	void setByteArray(ByteArrayResource resource, byte[] value);
	
	/**
	 * See {@link #setFloat(FloatResource, float, WriteConfiguration)}
	 */
	void setByteArray(ByteArrayResource resource, byte[] value, WriteConfiguration configuration);

	/**
	 * See {@link #getFloat(org.ogema.core.model.simple.FloatResource)}
	 */
	TransactionFuture<byte[]> getByteArray(ByteArrayResource resource);
	
	/**
	 * See {@link #getFloat(FloatResource, ReadConfiguration)}
	 */
	TransactionFuture<byte[]> getByteArray(ByteArrayResource resource, ReadConfiguration configuration);

	/**
	 * See {@link #setFloat(org.ogema.core.model.simple.FloatResource, float)}
	 */
	void setIntegerArray(IntegerArrayResource resource, int[] value);
	
	/**
	 * See {@link #setFloat(FloatResource, float, WriteConfiguration)}
	 */
	void setIntegerArray(IntegerArrayResource resource, int[] value, WriteConfiguration configuration);

	/**
	 * See {@link #getFloat(org.ogema.core.model.simple.FloatResource)}
	 */
	TransactionFuture<int[]> getIntegerArray(IntegerArrayResource resource);
	
	/**
	 * See {@link #getFloat(FloatResource, ReadConfiguration)}
	 */
	TransactionFuture<int[]> getIntegerArray(IntegerArrayResource resource, ReadConfiguration configuration);

	/**
	 * See {@link #setFloat(org.ogema.core.model.simple.FloatResource, float)}
	 */
	void setBooleanArray(BooleanArrayResource resource, boolean[] value);
	
	/**
	 * See {@link #setFloat(FloatResource, float, WriteConfiguration)}
	 */
	void setBooleanArray(BooleanArrayResource resource, boolean[] value, WriteConfiguration configuration);

	/**
	 * See {@link #getFloat(org.ogema.core.model.simple.FloatResource)}
	 */
	TransactionFuture<boolean[]> getBooleanArray(BooleanArrayResource resource);
	
	/**
	 * See {@link #getFloat(FloatResource, ReadConfiguration)}
	 */
	TransactionFuture<boolean[]> getBooleanArray(BooleanArrayResource resource, ReadConfiguration configuration);

	/**
	 * See {@link #setFloat(org.ogema.core.model.simple.FloatResource, float)}
	 */
	void setFloatArray(FloatArrayResource resource, float[] value);
	
	/**
	 * See {@link #setFloat(FloatResource, float, WriteConfiguration)}
	 */
	void setFloatArray(FloatArrayResource resource, float[] value, WriteConfiguration configuration);

	/**
	 * See {@link #getFloat(org.ogema.core.model.simple.FloatResource)}
	 */
	TransactionFuture<float[]> getFloatArray(FloatArrayResource resource);
	
	/**
	 * See {@link #getFloat(FloatResource, ReadConfiguration)}
	 */
	TransactionFuture<float[]> getFloatArray(FloatArrayResource resource, ReadConfiguration configuration);

	/**
	 * See {@link #setFloat(org.ogema.core.model.simple.FloatResource, float)}
	 */
	void setStringArray(StringArrayResource resource, String[] value);
	
	/**
	 * See {@link #setFloat(FloatResource, float, WriteConfiguration)}
	 */
	void setStringArray(StringArrayResource resource, String[] value, WriteConfiguration configuration);

	/**
	 * See {@link #getFloat(org.ogema.core.model.simple.FloatResource)}
	 */
	TransactionFuture<String[]> getStringArray(StringArrayResource resource);
	
	/**
	 * See {@link #getFloat(FloatResource, ReadConfiguration)}
	 */
	TransactionFuture<String[]> getStringArray(StringArrayResource resource, ReadConfiguration configuration);

	/**
	 * See {@link #setFloat(org.ogema.core.model.simple.FloatResource, float)}
	 */
	void setTimeArray(TimeArrayResource resource, long[] value);
	
	/**
	 * See {@link #setFloat(FloatResource, float, WriteConfiguration)}
	 */
	void setTimeArray(TimeArrayResource resource, long[] value, WriteConfiguration configuration);

	/**
	 * See {@link #getFloat(org.ogema.core.model.simple.FloatResource)}
	 */
	TransactionFuture<long[]> getTimeArray(TimeArrayResource resource);
	
	/**
	 * See {@link #getFloat(FloatResource, ReadConfiguration)}
	 */
	TransactionFuture<long[]> getTimeArray(TimeArrayResource resource, ReadConfiguration configuration);
	
	/**
	 * Set the schedule content to a new function, that is written in the {@link #commit()} operation. All data points previously
	 * contained in the schedule are deleted.
	 * @param schedule Target schedule to set.
	 * 		If schedule does not exist it is created in the transaction, if it is inactive it will be activated.
	 * @param function the new state for the schedule. Note that the TimeSeries interface extends ReadOnlyTimeSeries, so anything that implements a TimeSeries,
	 * such as a {@link Schedule} or a @see MemoryTimeSeries, can also be set.
	 */
	void setSchedule(Schedule schedule, ReadOnlyTimeSeries function);
	
	/**
	 * Set the schedule content to a new function, that is written in the {@link #commit()} operation.
	 * All data points previously contained in the schedule are deleted.
	 * @param schedule Target schedule to set.
	 * @param function the new state for the schedule. Note that the TimeSeries interface extends ReadOnlyTimeSeries, so anything that implements a TimeSeries,
	 * such as a {@link Schedule} or a @see MemoryTimeSeries, can also be set.
	 * @param configuration determines how to deal with a virtual or inactive resource
	 */
	void setSchedule(Schedule schedule, ReadOnlyTimeSeries function, WriteConfiguration configuration);
	
	/**
	 * Add the time series content to the schedule in the transaction. Contrary to {@link #setSchedule(Schedule, ReadOnlyTimeSeries)},
	 * existing data points in the schedule are retained.
	 * @param schedule Target schedule to set.
	 * @param function data points ot be added to the schedule. Note that the TimeSeries interface extends ReadOnlyTimeSeries, so anything that implements a TimeSeries,
	 * such as a {@link Schedule} or a @see MemoryTimeSeries, can also be added.
	 */
	void addSchedule(Schedule schedule, ReadOnlyTimeSeries function);
	
	/**
	 * Add the time series content to the schedule in the transaction. Contrary to {@link #setSchedule(Schedule, ReadOnlyTimeSeries, WriteConfiguration)},
	 * existing data points in the schedule are retained.
	 * @param schedule Target schedule to set.
	 * @param function data points ot be added to the schedule. Note that the TimeSeries interface extends ReadOnlyTimeSeries, so anything that implements a TimeSeries,
	 * such as a {@link Schedule} or a @see MemoryTimeSeries, can also be added.
	 * @param configuration determines how to deal with a virtual or inactive resource
	 */
	void addSchedule(Schedule schedule, ReadOnlyTimeSeries function, WriteConfiguration configuration);
	
	/**
	 * Add data points to the schedule.
	 * Uses the default configuration {@link WriteConfiguration#CREATE_AND_ACTIVATE} to deal with virtual
	 * and inactive resources.
	 * @see Schedule#addValues(Collection)
	 * @param schedule Target schedule to set.
	 * @param values the new values for the schedule.
	 */
	void addScheduleValues(Schedule schedule, Collection<SampledValue> values);
	
	/**
	 * Add data points to the schedule.
	 * @see Schedule#addValues(Collection)
	 * @param schedule Target schedule to set.
	 * @param values the new values for the schedule.
	 * @param configuration determines how to deal with a virtual or inactive resource
	 */
	void addScheduleValues(Schedule schedule, Collection<SampledValue> values, WriteConfiguration configuration);
	
	/**
	 * Replaces schedule values in the specified time interval. 
	 * Uses the default configuration {@link WriteConfiguration#CREATE_AND_ACTIVATE} to deal with virtual
	 * and inactive resources.
	 * @see Schedule#replaceValues(long, long, Collection)
	 * @param schedule Target schedule to set.
	 * @param starttime timestamp in ms since 1 Jan 1970
	 * @param endtime timestamp in ms since 1 Jan 1970
	 * @param values the new values for the schedule.
	 */
	void replaceScheduleValues(Schedule schedule, long starttime, long endtime, Collection<SampledValue> values);
	
	/**
	 * Replaces schedule values in the specified time interval. 
	 * @see Schedule#replaceValues(long, long, Collection)
	 * @param schedule Target schedule to set.
	 * @param starttime timestamp in ms since 1 Jan 1970
	 * @param endtime timestamp in ms since 1 Jan 1970
	 * @param values the new values for the schedule.
	 * @param configuration determines how to deal with a virtual or inactive resource
	 */
	void replaceScheduleValues(Schedule schedule, long starttime, long endtime, Collection<SampledValue> values, WriteConfiguration configuration);

	/**
	 * Gets the state of the schedule.
	 * @see #getSchedule(Schedule, ReadConfiguration)
	 * @param schedule 
	 * @return 
	 */
	TransactionFuture<ReadOnlyTimeSeries> getSchedule(Schedule schedule);
	
	/**
	 * Gets the state of the schedule.
	 * @see #getFloat(FloatResource, ReadConfiguration)
	 * @param resource
	 * @param configuration
	 */
	TransactionFuture<ReadOnlyTimeSeries> getSchedule(Schedule resource, ReadConfiguration configuration);
	
	/**
	 * Gets the schedule values in the specified time interval.
	 * Fails if the schedule resource is inactive.
	 * @see Schedule#getValues(long, long)
	 * @see #getSchedule(Schedule, long, long, ReadConfiguration)
	 * @param schedule 
	 * @param starttime timestamp in ms since 1 Jan 1970
	 * @param endtime timestamp in ms since 1 Jan 1970
	 * @return 
	 */
	TransactionFuture<ReadOnlyTimeSeries> getSchedule(Schedule schedule, long starttime, long endtime);
	
	/**
	 * Gets the schedule values in the specified time interval.
	 * @see Schedule#getValues(long, long)
	 * @see #getFloat(FloatResource, ReadConfiguration)
	 * @param schedule 
	 * @param starttime timestamp in ms since 1 Jan 1970
	 * @param endtime timestamp in ms since 1 Jan 1970
	 * @return 
	 */
	TransactionFuture<ReadOnlyTimeSeries> getSchedule(Schedule schedule, long starttime, long endtime, ReadConfiguration configuration);
	
	/**
	 * Activate the resource in the transaction.
	 * If the resource does not exist, it is created. Does not activate subresources.
	 * This is equivalent to {@link #activate(Resource, boolean, boolean)} with boolean arguments 
	 * <code>(create = true, recursive = false)</code>.
	 * @param resource
	 */
	void activate(Resource resource);
	
	/**
	 * Activate the resource in the transaction.
	 * @param resource
	 * @param create
	 * 		create resource if it does not exist? If this is false and the resource is virtual, a
	 * 		VirtualResourceException is thrown, causing the transaction to be aborted.
	 * @param recursive
	 * 		true: activate subresources as well. A recursive search includes the entire resource
	 *            tree below the calling resource, but does not extend the search going over references 
	 */
	void activate(Resource resource, boolean create, boolean recursive);
	
	/**
	 * Deactivate the resource in the transaction. Does not deactivate
	 * subresources. If the resource is virtual, it is ignored.
	 * @param resource
	 */
	void deactivate(Resource resource);
	
	/**
	 * Deactivate the resource in the transaction. If the resource is virtual, nothing happens.
	 * @param resource
	 * @param recursive
	 * 		true: deactivate subresources as well 
	 */
	void deactivate(Resource resource, boolean recursive);
	
	/**
	 * @see Resource#isActive()
	 * @param resource
	 * @return
	 */
	TransactionFuture<Boolean> isActive(Resource resource);
	
	/**
	 * Create a resoure in the transaction. 
	 * @param resource
	 */
	void create(Resource resource);
	
	/**
	 * Delete a resource in the transaction. 
	 * @param resource
	 */
	void delete(Resource resource);
	
	/**
	 * @see Resource#exists()
	 * @param resource
	 * @return
	 */
	TransactionFuture<Boolean> exists(Resource resource);
	
	/**
	 * Set a reference from resource to target (see {@link Resource#setAsReference(Resource)}). 
	 * 
	 * @param resource
	 * @param target
	 * 		Note that target must exist, it is not created automatically (contrary to resource).
	 * 		For instance, target could be created in the same transaction by 
	 * 		calling {@link #create(Resource)} with argument target, before calling this method. 
	 */
	<R extends Resource> void setAsReference(R resource, R target);
	
	/**
	 * Request the specified access mode for the given resource.
	 * 
	 * @param resource
	 * @param mode
	 * @param priority
	 * @param failOnReject
	 * 		If true, the transaction operation will throw a {@link ResourceAccessException} if the access mode is not granted
	 * 		due to another application having registered an exclusive access for the resource. This will
	 * 		then cause the transaction to fail and all its previous operations be undone. If the argument is false, the transaction 
	 * 		continues to execute the remaining actions, even if the requested mode is not granted. 
	 * @return The future will return true if the access mode has been granted, and false otherwise. Note that,
	 * 		if the parameter failOnReject is true, the operation cannot fail gracefully but will rather throw an exception
	 * 		if the access mode is not granted, so that in this case either the future returns true or the transaction fails altogether. 
	 */
	TransactionFuture<Boolean> requestAccessMode(Resource resource, AccessMode mode, AccessPriority priority, boolean failOnReject);
	
	/**
	 * @see Resource#getAccessMode()
	 * @param resource
	 * @return
	 */
	TransactionFuture<AccessMode> getAccessMode(Resource resource);
	
	/**
	 * @see Resource#getAccessPriority()
	 * @param resource
	 * @return
	 */
	TransactionFuture<AccessPriority> getAccessPriority(Resource resource);

}
