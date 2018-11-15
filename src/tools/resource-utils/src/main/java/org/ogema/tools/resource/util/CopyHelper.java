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
package org.ogema.tools.resource.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ValueResource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceOperationException;
import org.ogema.core.resourcemanager.transaction.ResourceTransaction;
import org.slf4j.LoggerFactory;

class CopyHelper {
 
	private final ResourceCopyConfiguration config;
	private final Resource source;
	private final Resource target;
	private final AtomicInteger activationCount;
	private final ResourceTransaction transaction;
	private final List<Resource> newlyCreated = new ArrayList<>();
	
	CopyHelper(ResourceCopyConfiguration config,
			Resource source,
			Resource target,
			ResourceAccess ra) {
		this.source = Objects.requireNonNull(source);
		this.target = Objects.requireNonNull(target);
		this.config = config != null ? config :
			ResourceCopyConfigurationBuilder.newInstance().build();
		this.transaction = this.config.isCopyActivationState() ? ra.createResourceTransaction() : null;
		this.activationCount =  this.config.isCopyActivationState() ? new AtomicInteger(0) : null;
	}
	
	/**
	 * @throws ResourceOperationException
	 * @throws SecurityException
	 */
	void commit() {
		try {
			copy(source, target);
			if (activationCount != null && activationCount.get() > 0)
				transaction.commit();
		} catch (Exception e) {
			Resource r = null;
			for (ListIterator<Resource> it=newlyCreated.listIterator();it.hasPrevious();r=it.previous()) {
				try {
					r.delete();
				} catch (Exception ee) {
					LoggerFactory.getLogger(CopyHelper.class).warn("Failed to rollback resource creation for {}: {}",r,ee.toString());
				}
			}
			throw e;
		}
	}
	
	void copy(final Resource source, final Resource copy) {
		if (source.isReference(false)) {
			if (config.isCopyReferences()) {
				copy.setAsReference(source.getLocationResource());
			}
			return;
		}
		if (!copy.exists()) {
			copy.create();
			newlyCreated.add(copy);
		}
		copyInternal(source, copy, "");
	}
	
	void copy(final Resource source, final Resource targetParent, final String parentRelativePath) {
		final Collection<Class<? extends Resource>> excludedTypes = config.getExcludedResourceTypes();
		if (excludedTypes != null) {
			final Class<? extends Resource> type = source.getResourceType();
			for (Class<? extends Resource> clz : excludedTypes) {
				if (clz.isAssignableFrom(type))
					return;
			}
		}
		final Collection<String> excludedPaths = config.getExcludedRelativePaths();
		final String newRelativePath = parentRelativePath.isEmpty() ? source.getName() : 
				parentRelativePath + "/" + source.getName();
		if (excludedPaths != null) {
			for (String path :excludedPaths) {
				if (path.equals(newRelativePath))
					return;
			}
		}
		if (source.isReference(false)) {
			if (config.isCopyReferences()) {
				final String name = source.getName();
				final Resource old = targetParent.getSubResource(name);
				final boolean isNew = old == null || !old.equalsLocation(source);
				final Resource ref = targetParent.addDecorator(source.getName(), source.getLocationResource());
				if (isNew)
					newlyCreated.add(ref);
			}
			return;
		}
		final String name = source.getName();
		final Resource old = targetParent.getSubResource(name);
		final boolean isNew = old == null || !old.exists();
		final Resource copy = targetParent.getSubResource(source.getName(), source.getResourceType()).create();
		if (isNew)
			newlyCreated.add(copy);
		copyInternal(source, copy, newRelativePath);
	}
	
	private final void copyInternal(final Resource source, final Resource copy, final String newRelativePath) {
		if (config.isCopyActivationState() && source.isActive()) {
			transaction.activate(copy);
			activationCount.getAndIncrement();
		}
		if (source instanceof Schedule) {
			if (config.isCopyScheduleValues()) {
				((Schedule) copy).addValues(((Schedule) source).getValues(Long.MIN_VALUE));
			}
		} else if (source instanceof ValueResource) {
			ValueResourceUtils.setValue((ValueResource) copy, ValueResourceUtils.getValue((ValueResource) source));
			if (config.isCopyLoggingState() && source instanceof SingleValueResource) {
				if (LoggingUtils.isLoggingEnabled((SingleValueResource) source)) {
					final RecordedData rd = LoggingUtils.getHistoricalData((SingleValueResource) source);
					if (rd != null) {
						final RecordedDataConfiguration cfg = rd.getConfiguration();
						final StorageType type = cfg.getStorageType();
						final long updateIntv = type == StorageType.ON_VALUE_UPDATE ? -2 :
							type == StorageType.ON_VALUE_CHANGED ? -1 :
							cfg.getFixedInterval();
						LoggingUtils.activateLogging((SingleValueResource) copy, updateIntv);
					}
				}
			}
		}
		if (!source.isReference(false)) {
			for (Resource sub: source.getSubResources(false)) {
				copy(sub, copy, newRelativePath);
			}
		}
	}
	
	
}
