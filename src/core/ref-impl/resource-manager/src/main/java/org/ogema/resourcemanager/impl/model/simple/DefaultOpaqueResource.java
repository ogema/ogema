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
package org.ogema.resourcemanager.impl.model.simple;

import org.ogema.core.resourcemanager.ResourceAccessException;
import org.ogema.core.resourcemanager.VirtualResourceException;
import org.ogema.resourcemanager.impl.ApplicationResourceManager;

import org.ogema.resourcemanager.virtual.VirtualTreeElement;

/**
 * 
 * @author jlapp
 */
@SuppressWarnings("deprecation")
public class DefaultOpaqueResource extends SingleValueResourceBase implements org.ogema.core.model.simple.OpaqueResource {

	public DefaultOpaqueResource(VirtualTreeElement el, String path, ApplicationResourceManager resMan) {
		super(el, path, resMan);
		if (el.getData().getByteArr() == null)
			el.getData().setByteArr(new byte[0]);
	}

	@Override
	public byte[] getValue() {
		checkReadPermission();
		return getEl().getData().getByteArr().clone();
	}

	@Override
	public boolean setValue(byte[] value) {
		if (!exists() || !hasWriteAccess()) {
			return false;
		}
		checkWritePermission();
		getTreeElement().getData().setByteArr(value);
		//FIXME no change check
		handleResourceUpdate(true);
		return true;
	}
	
	@Override
	public byte[]getAndSet(final byte[]value) throws VirtualResourceException, SecurityException, ResourceAccessException {
		if (!exists())
			throw new VirtualResourceException("Resource " + path + " is virtual, cannot set value");
		checkWriteAccess();
		resMan.lockWrite(); 
		try {
			final byte[]val = getValue();
			setValue(value);
			return val;
		} finally {
			resMan.unlockWrite();
		}
	}
	

}
