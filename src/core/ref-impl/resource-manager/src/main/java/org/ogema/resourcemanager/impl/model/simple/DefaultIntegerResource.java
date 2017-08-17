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
package org.ogema.resourcemanager.impl.model.simple;

import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.ResourceAccessException;
import org.ogema.core.resourcemanager.VirtualResourceException;
import org.ogema.resourcemanager.impl.ApplicationResourceManager;
import org.ogema.resourcemanager.impl.model.schedule.HistoricalSchedule;
import org.ogema.resourcemanager.virtual.VirtualTreeElement;
import org.ogema.resourcetree.SimpleResourceData;

/**
 * 
 * @author jlapp
 */
public class DefaultIntegerResource extends SingleValueResourceBase implements IntegerResource {

	public DefaultIntegerResource(VirtualTreeElement el, String path, ApplicationResourceManager resMan) {
		super(el, path, resMan);
	}

	@Override
	public int getValue() {
		checkReadPermission();
		return getEl().getData().getInt();
	}

	@Override
	public boolean setValue(int value) {
		resMan.lockRead();
		try {
			final VirtualTreeElement el = getEl();
			if (el.isVirtual() || getAccessModeInternal() == AccessMode.READ_ONLY) {
				return false;
			}
			checkWritePermission();
			final SimpleResourceData data = el.getData();
			boolean changed = value != data.getInt();
			data.setInt(value);
			handleResourceUpdateInternal(changed);
		} finally {
			resMan.unlockRead();
		}
		return true;
	}

	@Override
	public RecordedData getHistoricalData() {
		checkReadPermission();
		return getResourceDB().getRecordedData(getEl());
	}

	@Override
	public AbsoluteSchedule forecast() {
		return getSubResource("forecast", AbsoluteSchedule.class);
	}

	@Override
	public AbsoluteSchedule program() {
		return getSubResource("program", AbsoluteSchedule.class);
	}

	@Override
	public AbsoluteSchedule historicalData() {
		return getSubResource(HistoricalSchedule.PATH_IDENTIFIER, AbsoluteSchedule.class);
	}

	@Override
	public int getAndSet(final int value) throws VirtualResourceException, SecurityException, ResourceAccessException {
		return getAndWriteInternal(value, false);
	}

	@Override
	public int getAndAdd(final int value) throws VirtualResourceException, SecurityException, ResourceAccessException {
		return getAndWriteInternal(value, true);
	}
	
	private final int getAndWriteInternal(final int value, final boolean addOrSet) {
		if (!exists())
			throw new VirtualResourceException("Resource " + path + " is virtual, cannot set value");
		checkWriteAccess();
		// expensive; we make use of the fact that all access to the SimpleResourceData is 
		// guarded by the resMan lock. 
		resMan.lockWrite(); 
		try {
			final int val = getValue();
			setValue(addOrSet ? (val + value) : value);
			return val;
		} finally {
			resMan.unlockWrite();
		}
	}

}
