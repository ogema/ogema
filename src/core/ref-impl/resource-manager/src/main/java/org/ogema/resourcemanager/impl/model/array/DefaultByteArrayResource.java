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

import org.ogema.core.model.array.ByteArrayResource;
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
public class DefaultByteArrayResource extends ResourceBase implements ByteArrayResource {

	public DefaultByteArrayResource(VirtualTreeElement el, String path, ApplicationResourceManager resMan) {
		super(el, path, resMan);
		if (el.getData().getByteArr() == null)
			el.getData().setByteArr(new byte[0]);
	}

	@Override
	public byte[] getValues() {
		checkReadPermission();
		return getEl().getData().getByteArr().clone();
	}

	@Override
	public boolean setValues(byte[] values) {
		resMan.lockRead();
		try {
			final VirtualTreeElement el = getEl();
			if (el.isVirtual() || getAccessModeInternal() == AccessMode.READ_ONLY) {
				return false;
			}
			checkWritePermission();
			el.getData().setByteArr(values);
			//FIXME no change check!
			handleResourceUpdateInternal(true);
		} finally {
			resMan.unlockRead();
		}
		return true;
	}

	@Override
	public byte getElementValue(int index) {
		return getEl().getData().getByteArr()[index];
	}

	@Override
	public void setElementValue(byte value, int index) {
		if (!exists() || !hasWriteAccess()) {
			return;
		}
		checkWritePermission();
		byte[] arr = getTreeElement().getData().getByteArr();
		boolean changed = arr[index] != value;
		arr[index] = value;
		getTreeElement().fireChangeEvent();
		handleResourceUpdate(changed);
	}

	@Override
	public int size() {
		checkReadPermission();
		return getEl().getData().getByteArr().length;
	}

	@Override
	public long getLastUpdateTime() {
		return super.getLastUpdateTime();
	}
	
	@Override
	public byte[] getAndSet(final byte[] value) throws VirtualResourceException, SecurityException, ResourceAccessException {
		if (!exists())
			throw new VirtualResourceException("Resource " + path + " is virtual, cannot set value");
		checkWriteAccess();
		resMan.lockWrite(); 
		try {
			final byte[] val = getValues();
			setValues(value);
			return val;
		} finally {
			resMan.unlockWrite();
		}
	}

}
