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
package org.ogema.resourcemanager.impl;

import org.ogema.core.model.ValueResource;
import org.ogema.resourcemanager.virtual.VirtualTreeElement;

/**
 *
 * @author jlapp
 */
public class ValueResourceBase extends ResourceBase implements ValueResource {

    public ValueResourceBase(VirtualTreeElement el, String path, ApplicationResourceManager resMan) {
        super(el, path, resMan);
    }

    @Override
    public long getLastUpdateTime() {
        return super.getLastUpdateTime();
    }    
    
}
