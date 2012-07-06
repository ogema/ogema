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
package org.ogema.resourcemanager.impl.model.array;

import org.ogema.core.model.array.TimeArrayResource;
import org.ogema.resourcemanager.impl.ApplicationResourceManager;
import org.ogema.resourcemanager.impl.ResourceBase;

import org.ogema.resourcemanager.virtual.VirtualTreeElement;

/**
 * 
 * @author jlapp
 */
public class DefaultTimeArrayResource extends ResourceBase implements TimeArrayResource {

	public DefaultTimeArrayResource(VirtualTreeElement el, String path, ApplicationResourceManager resMan) {
		super(el, path, resMan);
		if (el.getData().getLongArr() == null)
			el.getData().setLongArr(new long[0]);
	}

	@Override
	public long[] getValues() {
		return getEl().getData().getLongArr().clone();
	}

	@Override
	public boolean setValues(long[] value) {
		if (!exists() || !hasWriteAccess()) {
			return false;
		}
		checkWritePermission();
		getTreeElement().getData().setLongArr(value);
		getTreeElement().fireChangeEvent();
		//FIXME no change check
		handleResourceUpdate(true);
		return true;
	}

	@Override
	public long getElementValue(int index) {
		return getEl().getData().getLongArr()[index];
	}

	@Override
	public void setElementValue(long value, int index) {
		if (!exists() || !hasWriteAccess()) {
			return;
		}
		checkWritePermission();
		long[] arr = getTreeElement().getData().getLongArr();
		boolean changed = arr[index] != value;
		arr[index] = value;
		getTreeElement().fireChangeEvent();
		handleResourceUpdate(changed);
	}

	@Override
	public int size() {
		return getEl().getData().getLongArr().length;
	}

}
