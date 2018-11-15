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
package org.ogema.resourcemanager.impl.model.schedule;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.schedule.RelativeSchedule;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.persistence.impl.faketree.ScheduleTreeElement;
import org.ogema.persistence.impl.faketree.ScheduleTreeElementFactory;
import org.ogema.resourcemanager.impl.ApplicationResourceManager;
import org.ogema.resourcemanager.impl.ResourceBase;
import org.ogema.resourcemanager.impl.ResourceDBManager;
import org.ogema.resourcemanager.virtual.VirtualTreeElement;

/**
 * Resource object representing a schedule. Extends ResourceBase and utilizes
 * the schedule functionality from the underlying ScheduleTreeElement.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
@SuppressWarnings("deprecation")
public class DefaultSchedule extends ResourceBase implements org.ogema.core.model.schedule.DefinitionSchedule,
		org.ogema.core.model.schedule.ForecastSchedule, AbsoluteSchedule, RelativeSchedule {

	private final ApplicationManager m_appMan;
	// this needs to change when a reference is set or replaced; need a ScheduleTreeElementRegistry
//	private final ScheduleTreeElement m_scheduleElement;
	private final ResourceDBManager m_dbMan;
	private final ScheduleTreeElementFactory m_treeElementFactory;

	public DefaultSchedule(VirtualTreeElement el, String path,
			ApplicationResourceManager resMan, ApplicationManager appMan, ResourceDBManager dbMan) {
		super(el, path, resMan);
		m_appMan = appMan;
//		m_scheduleElement = scheduleElement;
		m_dbMan = dbMan;
		m_treeElementFactory = m_dbMan.getScheduleTreeElementFactory();

	}

	final ScheduleTreeElement getSchedule() {
		resMan.lockRead();
		VirtualTreeElement e;
		try {
			e = getEl();
			while (e.isReference())
				e = (VirtualTreeElement) e.getReference();
		} finally {
			resMan.unlockRead();
		}
		return m_treeElementFactory.get(e);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Resource> T create() {
		getResourceDB().lockStructureWrite();
		m_dbMan.startTransaction();
		try {
			DefaultSchedule s = super.create();
			getSchedule().create();
			return (T) s;
		} finally {
			m_dbMan.finishTransaction();
			getResourceDB().unlockStructureWrite();
		}
	}

    @Override
    protected void deleteTreeElement() {
    	m_dbMan.startTransaction();
		try {
			if (!isReference(false)) {
				final ScheduleTreeElement ste = getSchedule();
		        ste.deleteValues();
		        ste.setLastModified(-1);
		        ste.getScheduleElement().delete();
			} else {
				// TODO this is not working!
			}
	        super.deleteTreeElement();
		} finally {
			m_dbMan.finishTransaction();
		}
		
    }

	@Override
	public boolean addValue(long timestamp, Value value) {
		if (!exists() || !hasWriteAccess()) {
			return false;
		}
		checkWritePermission();
		m_dbMan.startTransaction();
		try {
			getSchedule().addValue(timestamp, value);
		} finally {
			m_dbMan.finishTransaction();
		}
		getSchedule().setLastUpdateTime(m_appMan.getFrameworkTime());
		handleResourceUpdate(true);
		return true;
	}

	@Override
	public boolean addValues(Collection<SampledValue> values) {
		if (!exists() || !hasWriteAccess()) {
			return false;
		}
		checkWritePermission();
		m_dbMan.startTransaction();
		try {
			getSchedule().addValues(values);
		} finally {
			m_dbMan.finishTransaction();
		}
		getSchedule().setLastUpdateTime(m_appMan.getFrameworkTime());
		handleResourceUpdate(true);
		return true;
	}

	@Override
	@Deprecated
	public final boolean addValueSchedule(long startTime, long stepSize, List<Value> values) {
		return replaceValuesFixedStep(startTime, values, stepSize);
	}

	@Override
	public boolean replaceValuesFixedStep(long startTime, List<Value> values, long stepSize) {
		if (!exists() || !hasWriteAccess()) {
			return false;
		}
		checkWritePermission();
		m_dbMan.startTransaction();
		try {
			getSchedule().replaceValuesFixedStep(startTime, values, stepSize);
		} finally {
			m_dbMan.finishTransaction();
		}
		getSchedule().setLastUpdateTime(m_appMan.getFrameworkTime());
		handleResourceUpdate(true);
		return true;
	}

	@Override
	public boolean addValue(long timestamp, Value value, long timeOfCalculation) {
		if (!exists() || !hasWriteAccess()) {
			return false;
		}
		checkWritePermission();
		m_dbMan.startTransaction();
		try {
			getSchedule().addValue(timestamp, value, timeOfCalculation);
		} finally {
			m_dbMan.finishTransaction();
		}
		getSchedule().setLastUpdateTime(m_appMan.getFrameworkTime());
		handleResourceUpdate(true);
		return true;
	}

	@Override
	public boolean addValues(Collection<SampledValue> values, long timeOfCalculation) {
		if (!exists() || !hasWriteAccess()) {
			return false;
		}
		checkWritePermission();
		m_dbMan.startTransaction();
		try {
			getSchedule().addValues(values, timeOfCalculation);
		} finally {
			m_dbMan.finishTransaction();
		}
		getSchedule().setLastUpdateTime(m_appMan.getFrameworkTime());
		handleResourceUpdate(true);
		return true;
	}

	@Override
	@Deprecated
	public final boolean addValueSchedule(long startTime, long stepSize, List<Value> values, long timeOfCalculation) {
		return replaceValuesFixedStep(startTime, values, stepSize, timeOfCalculation);
	}

	@Override
	public boolean replaceValuesFixedStep(long startTime, List<Value> values, long stepSize, long timeOfCalculation) {
		if (!exists() || !hasWriteAccess()) {
			return false;
		}
		checkWritePermission();
		m_dbMan.startTransaction();
		try {
			getSchedule().replaceValuesFixedStep(startTime, values, stepSize, timeOfCalculation);
		} finally {
			m_dbMan.finishTransaction();
		}
		getSchedule().setLastUpdateTime(m_appMan.getFrameworkTime());
		handleResourceUpdate(true);
		return true;
	}

	@Override
	public Long getLastCalculationTime() {
		return getSchedule().getLastCalculationTime();
	}

	@Override
	public boolean deleteValues() {
		if (!exists() || !hasWriteAccess()) {
			return false;
		}
		checkWritePermission();
		m_dbMan.startTransaction();
		try {
			getSchedule().deleteValues();
		} finally {
			m_dbMan.finishTransaction();
		}
		getSchedule().setLastUpdateTime(m_appMan.getFrameworkTime());
		handleResourceUpdate(true);
		return true;
	}

	@Override
	public boolean deleteValues(long endTime) {
		if (!exists() || !hasWriteAccess()) {
			return false;
		}
		checkWritePermission();
		m_dbMan.startTransaction();
		try {
			getSchedule().deleteValues(endTime);
		} finally {
			m_dbMan.finishTransaction();
		}
		getSchedule().setLastUpdateTime(m_appMan.getFrameworkTime());
		handleResourceUpdate(true);
		return true;
	}

	@Override
	public boolean deleteValues(long startTime, long endTime) {
		if (!exists() || !hasWriteAccess()) {
			return false;
		}
		checkWritePermission();
		m_dbMan.startTransaction();
		try {
			getSchedule().deleteValues(startTime, endTime);
		} finally {
			m_dbMan.finishTransaction();
		}
		getSchedule().setLastUpdateTime(m_appMan.getFrameworkTime());
		handleResourceUpdate(true);
		return true;
	}

	@Override
	public boolean replaceValues(long startTime, long endTime, Collection<SampledValue> values) {
		if (!exists() || !hasWriteAccess()) {
			return false;
		}
		checkWritePermission();
		m_dbMan.startTransaction();
		try {
			getSchedule().replaceValues(startTime, endTime, values);
		} finally {
			m_dbMan.finishTransaction();
		}
		getSchedule().setLastUpdateTime(m_appMan.getFrameworkTime());
		handleResourceUpdate(true);
		return true;
	}

	@Override
	public boolean setInterpolationMode(InterpolationMode mode) {
		if (!exists() || !hasWriteAccess()) {
			return false;
		}
		checkWritePermission();
		getSchedule().setInterpolationMode(mode);
		handleResourceUpdate(true);
		return true;
	}

	@Override
	public SampledValue getValue(long time) {
		return getSchedule().getValue(time);
	}

	@Override
	public SampledValue getNextValue(long time) {
		return getSchedule().getNextValue(time);
	}

	@Override
	public List<SampledValue> getValues(long startTime) {
		return getSchedule().getValues(startTime);
	}

	@Override
	public List<SampledValue> getValues(long startTime, long endTime) {
		return getSchedule().getValues(startTime, endTime);
	}

	@Override
	public InterpolationMode getInterpolationMode() {
		return getSchedule().getInterpolationMode();
	}

	@Override
	@Deprecated
	public Long getTimeOfLatestEntry() {
		return getSchedule().getLastCalculationTime();
	}

	@Override
	public long getLastUpdateTime() {
		return getSchedule().getLastModified();
	}
	
	@Override
	public SampledValue getPreviousValue(long time) {
		return getSchedule().getPreviousValue(time);
	}

	@Override
	public boolean isEmpty() {
		return getSchedule().isEmpty();
	}

	@Override
	public boolean isEmpty(long startTime, long endTime) {
		return getSchedule().isEmpty(startTime, endTime);
	}

	@Override
	public int size() {
		return getSchedule().size();
	}

	@Override
	public int size(long startTime, long endTime) {
		return getSchedule().size(startTime, endTime);
	}

	@Override
	public Iterator<SampledValue> iterator() {
		return getSchedule().iterator();
	}

	@Override
	public Iterator<SampledValue> iterator(long startTime, long endTime) {
		return getSchedule().iterator(startTime, endTime);
	}

}
