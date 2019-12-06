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

import org.ogema.core.model.array.FloatArrayResource;
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
public class DefaultFloatArrayResource extends ResourceBase implements FloatArrayResource {

	public DefaultFloatArrayResource(VirtualTreeElement el, String path, ApplicationResourceManager resMan) {
		super(el, path, resMan);
		if (el.getData().getFloatArr() == null)
			el.getData().setFloatArr(new float[0]);
	}

	@Override
	public float[] getValues() {
		checkReadPermission();
		return getEl().getData().getFloatArr().clone();
	}

	@Override
	public boolean setValues(float[] values) {
		resMan.lockRead();
		try {
			final VirtualTreeElement el = getElInternal();
			if (el.isVirtual() || getAccessModeInternal() == AccessMode.READ_ONLY) {
				return false;
			}
			checkWritePermission();
			el.getData().setFloatArr(values);
			//FIXME no change check!
			handleResourceUpdateInternal(true);
		} finally {
			resMan.unlockRead();
		}
		return true;
	}

	@Override
	public float getElementValue(int index) {
        	checkReadPermission();
		return getEl().getData().getFloatArr()[index];
	}

	@Override
	public void setElementValue(float value, int index) {
		if (!exists() || !hasWriteAccess()) {
			return;
		}
		checkWritePermission();
		float[] arr = getTreeElement().getData().getFloatArr();
		boolean changed = arr[index] != value;
		arr[index] = value;
		getTreeElement().fireChangeEvent();
		handleResourceUpdate(changed);
	}

	@Override
	public int size() {
		return getEl().getData().getFloatArr().length;
	}

	@Override
	public long getLastUpdateTime() {
		return super.getLastUpdateTime();
	}
	
	@Override
	public float[] getAndSet(final float[] value) throws VirtualResourceException, SecurityException, ResourceAccessException {
		if (!exists())
			throw new VirtualResourceException("Resource " + path + " is virtual, cannot set value");
		checkWriteAccess();
		resMan.lockWrite(); 
		try {
			final float[] val = getValues();
			setValues(value);
			return val;
		} finally {
			resMan.unlockWrite();
		}
	}

}
