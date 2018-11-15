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