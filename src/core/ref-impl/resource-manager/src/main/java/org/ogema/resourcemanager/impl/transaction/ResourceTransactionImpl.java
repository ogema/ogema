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
package org.ogema.resourcemanager.impl.transaction;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.ogema.core.application.ApplicationManager;
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
import org.ogema.core.resourcemanager.ResourceOperationException;
import org.ogema.core.resourcemanager.transaction.ReadConfiguration;
import org.ogema.core.resourcemanager.transaction.ResourceTransaction;
import org.ogema.core.resourcemanager.transaction.TransactionFuture;
import org.ogema.core.resourcemanager.transaction.WriteConfiguration;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.resourcemanager.impl.ResourceDBManager;
import org.ogema.resourcemanager.impl.transaction.actions.AccessModeAction;
import org.ogema.resourcemanager.impl.transaction.actions.AccessModeReadAction;
import org.ogema.resourcemanager.impl.transaction.actions.AccessPriorityReadAction;
import org.ogema.resourcemanager.impl.transaction.actions.ActivationAction;
import org.ogema.resourcemanager.impl.transaction.actions.ActiveReadAction;
import org.ogema.resourcemanager.impl.transaction.actions.CreationAction;
import org.ogema.resourcemanager.impl.transaction.actions.DeletionAction;
import org.ogema.resourcemanager.impl.transaction.actions.ExistenceReadAction;
import org.ogema.resourcemanager.impl.transaction.actions.ReferenceAction;
import org.ogema.resourcemanager.impl.transaction.actions.ResourceReadAction;
import org.ogema.resourcemanager.impl.transaction.actions.ResourceWriteAction;
import org.ogema.resourcemanager.impl.transaction.actions.ScheduleAddAction;
import org.ogema.resourcemanager.impl.transaction.actions.ScheduleReadAction;
import org.ogema.resourcemanager.impl.transaction.actions.ScheduleReplaceAction;

public class ResourceTransactionImpl implements ResourceTransaction {
	
	private volatile boolean commited = false;
    private final ResourceDBManager dbMan;
    private final ApplicationManager appMan;
    private final Queue<AtomicAction> pending = new LinkedList<>();
    private boolean requiresStructureWriteLock = false;
    private boolean requiresCommitWriteLock = false;
    
    public ResourceTransactionImpl(ResourceDBManager dbMan, ApplicationManager appMan) {
        this.dbMan = dbMan;
        this.appMan = appMan;
    }
    
	@Override
	public void commit() throws ResourceOperationException {
		checkStatus();
		commited = true;
		AtomicAction action;
		Deque<AtomicAction> done = new ArrayDeque<>();
		final boolean writeLock = requiresCommitWriteLock;
		final boolean structureWriteLock = requiresStructureWriteLock;
		lock(structureWriteLock, writeLock);
		try {
			while ((action = pending.poll()) != null) {
				appMan.getLogger().debug("Executing action {} for resource {}",action.getType(),action.getSource());
				done.add(action); // add this immediately, so we can try to rollback this action even if it fails
				try {
					action.execute();
				} catch (Exception e) {
					// will be rethrown after rollback
					appMan.getLogger().warn("Transaction failed at action {} for resource {}",action.getType(), action.getSource());
					rollback(done,action,e);
					return;
				}
			}
		} finally {
			unlock(structureWriteLock, writeLock);
		}
	}
	
	private void rollback(final Deque<AtomicAction> actions, AtomicAction source, Exception cause) {
		/*
		 * Avoid using caller's permissions in rollback
		 */
		AccessController.doPrivileged(new PrivilegedAction<Void>() {
			
			@Override
			public Void run() {
				AtomicAction action;
				while ((action = actions.pollLast()) != null) {
					try {
						appMan.getLogger().debug("Executing rollback {} for resource {}",action.getType(),action.getSource());
						action.rollback();
					} catch (Exception ee) { // may not be so unusual
						appMan.getLogger().warn("Transaction rollback failed",ee);
						continue;
					}
				}
				return null;
			}
		});
		pending.clear();
		throw new DefaultResourceOperationException(source, cause);
	}
	
	private void checkStatus() {
		if (commited)
			throw new IllegalStateException("Transaction has been commited already.");
	}
	
	// FIXME check: is it admissible to obtain structure read lock and then commit write lock? 
	private void lock(boolean structureWrite, boolean valueWrite) {
		if (structureWrite)
			dbMan.lockStructureWrite();
		else
			dbMan.lockStructureRead();
		if (valueWrite)
			dbMan.lockWrite();
		else
			dbMan.lockRead();
	}
	
	private void unlock(boolean structureWrite, boolean valueWrite) {
		if (valueWrite)
			dbMan.unlockWrite();
		else
			dbMan.unlockRead();
		if (structureWrite)
			dbMan.unlockStructureWrite();
		else
			dbMan.unlockStructureRead();
	}
	
	
	// TODO check need for locks
	private void addAction(AtomicAction action) {
		pending.add(action);
		if (action.requiresCommitWriteLock())
			requiresCommitWriteLock = true;
		if (action.requiresStructureWriteLock())
			requiresStructureWriteLock = true;
	}
	
	@Override
	public void setFloat(FloatResource resource, float value) {
		setFloat(resource, value, WriteConfiguration.CREATE_AND_ACTIVATE);
	}

	@Override
	public void setFloat(FloatResource resource, float value, WriteConfiguration configuration) {
		checkStatus();
//		addAction(new FloatWriteAction(resource, value, configuration));
		addAction(new ResourceWriteAction<Float, FloatResource>(resource, value, configuration));
	}

	@Override
	public TransactionFuture<Float> getFloat(FloatResource resource) {
		return getFloat(resource, ReadConfiguration.FAIL); 
	}

	@Override
	public TransactionFuture<Float> getFloat(FloatResource resource, ReadConfiguration configuration) {
		checkStatus();
//		FloatReadAction action = new FloatReadAction(resource, configuration);
		ResourceReadAction<Float, FloatResource> action = new ResourceReadAction<Float, FloatResource>(resource, configuration);
		addAction(action);
		return new TransactionFutureImpl<>(action);
	}

	@Override
	public void setInteger(IntegerResource resource, int value) {
		setInteger(resource, value, WriteConfiguration.CREATE_AND_ACTIVATE);
	}

	@Override
	public void setInteger(IntegerResource resource, int value, WriteConfiguration configuration) {
		checkStatus();
		addAction(new ResourceWriteAction<Integer, IntegerResource>(resource, value, configuration));
	}

	@Override
	public TransactionFuture<Integer> getInteger(IntegerResource resource) {
		return getInteger(resource, ReadConfiguration.FAIL);
	}

	@Override
	public TransactionFuture<Integer> getInteger(IntegerResource resource, ReadConfiguration configuration) {
		checkStatus();
		ResourceReadAction<Integer, IntegerResource> action = new ResourceReadAction<Integer, IntegerResource>(resource, configuration);
		addAction(action);
		return new TransactionFutureImpl<>(action);
	}

	@Override
	public void setBoolean(BooleanResource resource, boolean value) {
		setBoolean(resource, value, WriteConfiguration.CREATE_AND_ACTIVATE);
	}

	@Override
	public void setBoolean(BooleanResource resource, boolean value, WriteConfiguration configuration) {
		checkStatus();
		addAction(new ResourceWriteAction<Boolean, BooleanResource>(resource, value, configuration));
	}

	@Override
	public TransactionFuture<Boolean> getBoolean(BooleanResource resource) {
		return getBoolean(resource, ReadConfiguration.FAIL);
	}

	@Override
	public TransactionFuture<Boolean> getBoolean(BooleanResource resource, ReadConfiguration configuration) {
		checkStatus();
		ResourceReadAction<Boolean, BooleanResource> action = new ResourceReadAction<Boolean, BooleanResource>(resource, configuration);
		addAction(action);
		return new TransactionFutureImpl<>(action);
	}

	@Override
	public void setString(StringResource resource, String value) {
		setString(resource, value, WriteConfiguration.CREATE_AND_ACTIVATE);
	}

	@Override
	public void setString(StringResource resource, String value, WriteConfiguration configuration) {
		checkStatus();
		addAction(new ResourceWriteAction<String, StringResource>(resource, value, configuration));
	}

	@Override
	public TransactionFuture<String> getString(StringResource resource) {
		return getString(resource, ReadConfiguration.FAIL);
	}

	@Override
	public TransactionFuture<String> getString(StringResource resource, ReadConfiguration configuration) {
		checkStatus();
		ResourceReadAction<String, StringResource> action = new ResourceReadAction<String, StringResource>(resource, configuration);
		addAction(action);
		return new TransactionFutureImpl<>(action);
	}

	@Override
	public void setTime(TimeResource resource, long value) {
		setTime(resource, value, WriteConfiguration.CREATE_AND_ACTIVATE);
	}

	@Override
	public void setTime(TimeResource resource, long value, WriteConfiguration configuration) {
		checkStatus();
		addAction(new ResourceWriteAction<Long, TimeResource>(resource, value, configuration));
	}

	@Override
	public TransactionFuture<Long> getTime(TimeResource resource) {
		return getTime(resource, ReadConfiguration.FAIL);
	}

	@Override
	public TransactionFuture<Long> getTime(TimeResource resource, ReadConfiguration configuration) {
		checkStatus();
		ResourceReadAction<Long, TimeResource> action = new ResourceReadAction<Long, TimeResource>(resource, configuration);
		addAction(action);
		return new TransactionFutureImpl<>(action);
	}

	@Override
	public void setByteArray(ByteArrayResource resource, byte[] value) {
		setByteArray(resource, value, WriteConfiguration.CREATE_AND_ACTIVATE);
	}

	@Override
	public void setByteArray(ByteArrayResource resource, byte[] value, WriteConfiguration configuration) {
		checkStatus();
		addAction(new ResourceWriteAction<byte[], ByteArrayResource>(resource, value, configuration));
	}

	@Override
	public TransactionFuture<byte[]> getByteArray(ByteArrayResource resource) {
		return getByteArray(resource, ReadConfiguration.FAIL);
	}

	@Override
	public TransactionFuture<byte[]> getByteArray(ByteArrayResource resource, ReadConfiguration configuration) {
		checkStatus();
		ResourceReadAction<byte[], ByteArrayResource> action = new ResourceReadAction<byte[], ByteArrayResource>(resource, configuration);
		addAction(action);
		return new TransactionFutureImpl<>(action);
	}

	@Override
	public void setIntegerArray(IntegerArrayResource resource, int[] value) {
		setIntegerArray(resource, value, WriteConfiguration.CREATE_AND_ACTIVATE);
	}

	@Override
	public void setIntegerArray(IntegerArrayResource resource, int[] value, WriteConfiguration configuration) {
		checkStatus();
		addAction(new ResourceWriteAction<int[], IntegerArrayResource>(resource, value, configuration));

	}

	@Override
	public TransactionFuture<int[]> getIntegerArray(IntegerArrayResource resource) {
		return getIntegerArray(resource, ReadConfiguration.FAIL);
	}

	@Override
	public TransactionFuture<int[]> getIntegerArray(IntegerArrayResource resource, ReadConfiguration configuration) {
		checkStatus();
		ResourceReadAction<int[], IntegerArrayResource> action = new ResourceReadAction<int[], IntegerArrayResource>(resource, configuration);
		addAction(action);
		return new TransactionFutureImpl<>(action);
	}

	@Override
	public void setBooleanArray(BooleanArrayResource resource, boolean[] value) {
		setBooleanArray(resource, value, WriteConfiguration.CREATE_AND_ACTIVATE);
	}

	@Override
	public void setBooleanArray(BooleanArrayResource resource, boolean[] value, WriteConfiguration configuration) {
		checkStatus();
		addAction(new ResourceWriteAction<boolean[], BooleanArrayResource>(resource, value, configuration));

	}

	@Override
	public TransactionFuture<boolean[]> getBooleanArray(BooleanArrayResource resource) {
		return getBooleanArray(resource, ReadConfiguration.FAIL);
	}

	@Override
	public TransactionFuture<boolean[]> getBooleanArray(BooleanArrayResource resource, ReadConfiguration configuration) {
		checkStatus();
		ResourceReadAction<boolean[], BooleanArrayResource> action = new ResourceReadAction<boolean[], BooleanArrayResource>(resource, configuration);
		addAction(action);
		return new TransactionFutureImpl<>(action);
	}

	@Override
	public void setFloatArray(FloatArrayResource resource, float[] value) {
		setFloatArray(resource, value, WriteConfiguration.CREATE_AND_ACTIVATE);
	}

	@Override
	public void setFloatArray(FloatArrayResource resource, float[] value, WriteConfiguration configuration) {
		checkStatus();
		addAction(new ResourceWriteAction<float[], FloatArrayResource>(resource, value, configuration));
	}

	@Override
	public TransactionFuture<float[]> getFloatArray(FloatArrayResource resource) {
		return getFloatArray(resource, ReadConfiguration.FAIL);
	}

	@Override
	public TransactionFuture<float[]> getFloatArray(FloatArrayResource resource, ReadConfiguration configuration) {
		checkStatus();
		ResourceReadAction<float[], FloatArrayResource> action = new ResourceReadAction<float[], FloatArrayResource>(resource, configuration);
		addAction(action);
		return new TransactionFutureImpl<>(action);
	}

	@Override
	public void setStringArray(StringArrayResource resource, String[] value) {
		setStringArray(resource, value, WriteConfiguration.CREATE_AND_ACTIVATE);
	}

	@Override
	public void setStringArray(StringArrayResource resource, String[] value, WriteConfiguration configuration) {
		checkStatus();
		addAction(new ResourceWriteAction<String[], StringArrayResource>(resource, value, configuration));
	}

	@Override
	public TransactionFuture<String[]> getStringArray(StringArrayResource resource) {
		return getStringArray(resource, ReadConfiguration.FAIL);
	}

	@Override
	public TransactionFuture<String[]> getStringArray(StringArrayResource resource, ReadConfiguration configuration) {
		checkStatus();
		ResourceReadAction<String[], StringArrayResource> action = new ResourceReadAction<String[], StringArrayResource>(resource, configuration);
		addAction(action);
		return new TransactionFutureImpl<>(action);
	}

	@Override
	public void setTimeArray(TimeArrayResource resource, long[] value) {
		setTimeArray(resource, value, WriteConfiguration.CREATE_AND_ACTIVATE);
	}

	@Override
	public void setTimeArray(TimeArrayResource resource, long[] value, WriteConfiguration configuration) {
		checkStatus();
		addAction(new ResourceWriteAction<long[], TimeArrayResource>(resource, value, configuration));
	}

	@Override
	public TransactionFuture<long[]> getTimeArray(TimeArrayResource resource) {
		return getTimeArray(resource, ReadConfiguration.FAIL);
	}

	@Override
	public TransactionFuture<long[]> getTimeArray(TimeArrayResource resource, ReadConfiguration configuration) {
		checkStatus();
		ResourceReadAction<long[], TimeArrayResource> action = new ResourceReadAction<long[], TimeArrayResource>(resource, configuration);
		addAction(action);
		return new TransactionFutureImpl<>(action);
	}

	// TODO interpolation mode
	@Override
	public void setSchedule(Schedule schedule, ReadOnlyTimeSeries function) {
		setSchedule(schedule, function, WriteConfiguration.CREATE_AND_ACTIVATE);
	}

	@Override
	public void setSchedule(Schedule schedule, ReadOnlyTimeSeries function, WriteConfiguration configuration) {
		checkStatus();
		addAction(new ResourceWriteAction<List<SampledValue>, Schedule>(schedule, function.getValues(Long.MIN_VALUE), configuration));
	}
	
	@Override
	public void addSchedule(Schedule schedule, ReadOnlyTimeSeries function) {
		addSchedule(schedule, function, WriteConfiguration.CREATE_AND_ACTIVATE);
	}

	@Override
	public void addSchedule(Schedule schedule, ReadOnlyTimeSeries function, WriteConfiguration configuration) {
		addScheduleValues(schedule, function.getValues(Long.MIN_VALUE), configuration);
	}
	
	@Override
	public void addScheduleValues(Schedule schedule, Collection<SampledValue> values) {
		addScheduleValues(schedule, values, WriteConfiguration.CREATE_AND_ACTIVATE);
	}
	
	@Override
	public void addScheduleValues(Schedule schedule, Collection<SampledValue> values, WriteConfiguration configuration) {
		checkStatus();
		addAction(new ScheduleAddAction(schedule, values, configuration));
	}
	
	@Override
	public void replaceScheduleValues(Schedule schedule, long starttime, long endtime, Collection<SampledValue> values) {
		replaceScheduleValues(schedule, starttime, endtime, values, WriteConfiguration.CREATE_AND_ACTIVATE);
	}
	
	@Override
	public void replaceScheduleValues(Schedule schedule, long starttime, long endtime, Collection<SampledValue> values,	WriteConfiguration configuration) {
		checkStatus();
		addAction(new ScheduleReplaceAction(schedule, values, configuration, starttime, endtime));
	}
	
	@Override
	public TransactionFuture<ReadOnlyTimeSeries> getSchedule(Schedule schedule) {
		return getSchedule(schedule,ReadConfiguration.FAIL);
	}

	@Override
	public TransactionFuture<ReadOnlyTimeSeries> getSchedule(Schedule resource, ReadConfiguration configuration) {
		return getSchedule(resource, Long.MIN_VALUE, Long.MAX_VALUE, ReadConfiguration.FAIL);
	}
	
	@Override
	public TransactionFuture<ReadOnlyTimeSeries> getSchedule(Schedule schedule, long starttime, long endtime) {
		return getSchedule(schedule, starttime, endtime, ReadConfiguration.FAIL);
	}
	
	@Override
	public TransactionFuture<ReadOnlyTimeSeries> getSchedule(Schedule resource, long starttime, long endtime, ReadConfiguration configuration) {
		checkStatus();
		ScheduleReadAction action = new ScheduleReadAction(resource, configuration, starttime, endtime);
		addAction(action);
		return new TransactionFutureImpl<>(action);
	}
	
	@Override
	public void activate(Resource resource) {
		activate(resource, true, false);		
	}

	@Override
	public void activate(Resource resource, boolean create, boolean recursive) {
		checkStatus();
		ActivationAction action = new ActivationAction(resource, true, recursive, create);
		addAction(action);
	}

	@Override
	public void deactivate(Resource resource) {
		deactivate(resource, false);
	}

	@Override
	public void deactivate(Resource resource, boolean recursive) {
		checkStatus();
		addAction(new ActivationAction(resource, false, recursive, false));
	}

	@Override
	public void create(Resource resource) {
		checkStatus();
		addAction(new CreationAction(resource));
	}

	@Override
	public void delete(Resource resource) {
		checkStatus();
		addAction(new DeletionAction(resource,appMan.getResourceManagement()));
	}

	@Override
	public <R extends Resource> void setAsReference(R resource, R target) {
		checkStatus();
		addAction(new ReferenceAction<R>(resource, target,appMan.getResourceManagement()));
	}

	@Override
	public TransactionFuture<Boolean> requestAccessMode(Resource resource, AccessMode mode, AccessPriority priority,boolean failOnReject) {
		checkStatus();
		AccessModeAction action  =new AccessModeAction(resource, mode, priority, failOnReject);
		addAction(action);
		return new TransactionFutureImpl<>(action);
	}
   
	@Override
	public TransactionFuture<AccessMode> getAccessMode(Resource resource) {
		checkStatus();
		AccessModeReadAction action  =new AccessModeReadAction(resource);
		addAction(action);
		return new TransactionFutureImpl<>(action);
	}
	
	@Override
	public TransactionFuture<AccessPriority> getAccessPriority(Resource resource) {
		checkStatus();
		AccessPriorityReadAction action  =new AccessPriorityReadAction(resource);
		addAction(action);
		return new TransactionFutureImpl<>(action);
	}

	@Override
	public TransactionFuture<Boolean> isActive(Resource resource) {
		checkStatus();
		ActiveReadAction action  = new ActiveReadAction(resource);
		addAction(action);
		return new TransactionFutureImpl<>(action);
	}

	@Override
	public TransactionFuture<Boolean> exists(Resource resource) {
		checkStatus();
		ExistenceReadAction action  = new ExistenceReadAction(resource);
		addAction(action);
		return new TransactionFutureImpl<>(action);
	}

	
}
