/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.resourcemanager.impl.model.simple;

import org.ogema.core.model.schedule.DefinitionSchedule;
import org.ogema.core.model.schedule.ForecastSchedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.resourcemanager.impl.ApplicationResourceManager;
import org.ogema.resourcemanager.impl.ResourceBase;
import org.ogema.resourcemanager.virtual.VirtualTreeElement;

/**
 * 
 * @author jlapp
 */
public class DefaultFloatResource extends ResourceBase implements FloatResource {

	public DefaultFloatResource(VirtualTreeElement el, String path, ApplicationResourceManager resMan) {
		super(el, path, resMan);
	}

	@Override
	public float getValue() {
		checkReadPermission();
		return getEl().getData().getFloat();
	}

	@Override
	public boolean setValue(float value) {
		if (!exists() || !hasWriteAccess()) {
			return false;
		}
		checkWritePermission();
		float oldValue = getValue();
		getTreeElement().getData().setFloat(value);
		getTreeElement().fireChangeEvent();
		handleResourceUpdate(oldValue != value);
		return true;
	}

	@Override
	public RecordedData getHistoricalData() {
		checkReadPermission();
		return getResourceDB().getRecordedData(getEl());
	}

	@Override
	public ForecastSchedule forecast() {
		return getSubResource("forecast", ForecastSchedule.class);
	}

	@Override
	public DefinitionSchedule program() {
		return getSubResource("program", DefinitionSchedule.class);
	}

}
