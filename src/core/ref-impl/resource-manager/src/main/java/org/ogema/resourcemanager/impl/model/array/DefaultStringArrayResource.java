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
package org.ogema.resourcemanager.impl.model.array;

import org.ogema.core.model.array.StringArrayResource;
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
public class DefaultStringArrayResource extends ResourceBase implements StringArrayResource {

	public DefaultStringArrayResource(VirtualTreeElement el, String path, ApplicationResourceManager resMan) {
		super(el, path, resMan);
		if (el.getData().getStringArr() == null)
			el.getData().setStringArr(new String[0]);
	}

	@Override
	public String[] getValues() {
		checkReadPermission();
		return getEl().getData().getStringArr().clone();
	}

	@Override
	public boolean setValues(String[] values) {
		resMan.lockRead();
		try {
			final VirtualTreeElement el = getElInternal();
			if (el.isVirtual() || getAccessModeInternal() == AccessMode.READ_ONLY) {
				return false;
			}
			checkWritePermission();
			el.getData().setStringArr(values);
			//FIXME no change check!
			handleResourceUpdateInternal(true);
		} finally {
			resMan.unlockRead();
		}
		return true;
	}

	@Override
	public String getElementValue(int index) {
        	checkReadPermission();
		return getEl().getData().getStringArr()[index];
	}

	@Override
	public void setElementValue(String value, int index) {
		if (!exists() || !hasWriteAccess()) {
			return;
		}
		checkWritePermission();
		String[] arr = getTreeElement().getData().getStringArr();
		//FIXME null values
		boolean changed = !arr[index].equals(value);
		arr[index] = value;
		getTreeElement().fireChangeEvent();
		handleResourceUpdate(changed);
	}

	@Override
	public int size() {
		return getEl().getData().getStringArr().length;
	}

	@Override
	public long getLastUpdateTime() {
		return super.getLastUpdateTime();
	}
	
	@Override
	public String[] getAndSet(final String[] value) throws VirtualResourceException, SecurityException, ResourceAccessException {
		if (!exists())
			throw new VirtualResourceException("Resource " + path + " is virtual, cannot set value");
		checkWriteAccess();
		resMan.lockWrite(); 
		try {
			final String[] val = getValues();
			setValues(value);
			return val;
		} finally {
			resMan.unlockWrite();
		}
	}
}
