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

}
