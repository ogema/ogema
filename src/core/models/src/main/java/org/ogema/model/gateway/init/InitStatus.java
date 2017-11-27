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
package org.ogema.model.gateway.init;

import org.ogema.core.model.ModelModifiers.NonPersistent;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.model.prototypes.Data;

/** 
 * Information on the initialization process OGEMA Gateway. Note that this resource should
 * not be backuped as its element shall indicate non-persistent initialization
 */
@Deprecated
public interface InitStatus extends Data {
	
	/**
	 * Notification from framework that no app was found with unfinished start method for more than 2 seconds
	 */
	//@NonPersistent
	//BooleanResource startupFinshed();

	/**
	 * If active and true the resource shall indicate that reading resources from backup (usually in
	 * form of ogx / ogj files is finished)
	 */
	@NonPersistent
	BooleanResource replayOnClean();
}