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
package org.ogema.resourcemanager.impl.model.array;

import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.ResourceAccessException;
import org.ogema.core.resourcemanager.VirtualResourceException;
import org.ogema.resourcemanager.impl.ApplicationResourceManager;
import org.ogema.resourcemanager.impl.ResourceBase;

import org.ogema.resourcemanager.virtual.VirtualTreeElement;

/**
 * 
 * @author jlapp
 */
public class DefaultIntegerArrayResource extends ResourceBase implements IntegerArrayResource {

	public DefaultIntegerArrayResource(VirtualTreeElement el, String path, ApplicationResourceManager resMan) {
		super(el, path, resMan);
		if (el.getData().getIntArr() == null)
			el.getData().setIntArr(new int[0]);
	}

	@Override
	public int[] getValues() {
		return getEl().getData().getIntArr().clone();
	}

	@Override
	public boolean setValues(int[] values) {
		resMan.lockRead();
		try {
			final VirtualTreeElement el = getEl();
			if (el.isVirtual() || getAccessModeInternal() == AccessMode.READ_ONLY) {
				return false;
			}
			checkWritePermission();
			el.getData().setIntArr(values);
			//FIXME no change check!
			handleResourceUpdateInternal(true);
		} finally {
			resMan.unlockRead();
		}
		return true;
	}

	@Override
	public int getElementValue(int index) {
		return getEl().getData().getIntArr()[index];
	}

	@Override
	public void setElementValue(int value, int index) {
		if (!exists() || !hasWriteAccess()) {
			return;
		}
		checkWritePermission();
		int[] arr = getTreeElement().getData().getIntArr();
		boolean changed = arr[index] != value;
		arr[index] = value;
		getTreeElement().fireChangeEvent();
		handleResourceUpdate(changed);
	}

	@Override
	public int size() {
		return getEl().getData().getIntArr().length;
	}

	@Override
	public long getLastUpdateTime() {
		return super.getLastUpdateTime();
	}
	
	@Override
	public int[] getAndSet(final int[] value) throws VirtualResourceException, SecurityException, ResourceAccessException {
		if (!exists())
			throw new VirtualResourceException("Resource " + path + " is virtual, cannot set value");
		checkWriteAccess();
		resMan.lockWrite(); 
		try {
			final int[] val = getValues();
			setValues(value);
			return val;
		} finally {
			resMan.unlockWrite();
		}
	}
}
