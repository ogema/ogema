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
package org.ogema.resourcemanager.impl.model.schedule;

import java.util.Collection;
import java.util.List;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.DefinitionSchedule;
import org.ogema.core.model.schedule.ForecastSchedule;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.persistence.impl.faketree.ScheduleTreeElement;
import org.ogema.resourcemanager.impl.ApplicationResourceManager;
import org.ogema.resourcemanager.impl.ResourceBase;
import org.ogema.resourcemanager.virtual.VirtualTreeElement;

/**
 * Resource object representing a schedule. Extends ResourceBase and utilizes
 * the schedule functionality from the underlying ScheduleTreeElement.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class DefaultSchedule extends ResourceBase implements DefinitionSchedule, ForecastSchedule {

	private final ApplicationManager m_appMan;
	private final ScheduleTreeElement m_scheduleElement;

	public DefaultSchedule(VirtualTreeElement el, ScheduleTreeElement scheduleElement, String path,
			ApplicationResourceManager resMan, ApplicationManager appMan) {
		super(el, path, resMan);
		m_appMan = appMan;
		m_scheduleElement = scheduleElement;
	}

	final ScheduleTreeElement getSchedule() {
		return m_scheduleElement;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Resource> T create() {
		DefaultSchedule s = super.create();
		m_scheduleElement.create();
		return (T) s;
	}

	@Override
	public boolean addValue(long timestamp, Value value) {
		if (!exists() || !hasWriteAccess()) {
			return false;
		}
		checkWritePermission();
		getSchedule().addValue(timestamp, value);
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
		getSchedule().addValues(values);
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
		getSchedule().replaceValuesFixedStep(startTime, values, stepSize);
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
		getSchedule().addValue(timestamp, value, timeOfCalculation);
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
		getSchedule().addValues(values, timeOfCalculation);
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
		getSchedule().replaceValuesFixedStep(startTime, values, stepSize, timeOfCalculation);
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
		getSchedule().deleteValues();
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
		getSchedule().deleteValues(endTime);
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
		getSchedule().deleteValues(startTime, endTime);
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
		getSchedule().replaceValues(startTime, endTime, values);
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

}
