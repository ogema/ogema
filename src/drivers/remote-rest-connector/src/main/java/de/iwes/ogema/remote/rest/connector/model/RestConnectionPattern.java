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
package de.iwes.ogema.remote.rest.connector.model;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;

public class RestConnectionPattern extends ResourcePattern<RestConnection> {

	public RestConnectionPattern(Resource match) {
		super(match);
	}

	public final StringResource remotePath = model.remotePath();
	
	@ChangeListener(structureListener=true)
	@Existence(required=CreateMode.OPTIONAL)
	public final RestPullConfig pullConfig = model.pullConfig();

	@ChangeListener(structureListener=true)
	@Existence(required=CreateMode.OPTIONAL)
	public final RestPushConfig pushConfig = model.pushConfig();
	
	@ChangeListener(structureListener=true)
	@Existence(required=CreateMode.OPTIONAL)
	public final ResourceList<RestPullConfig> individualPullConfigs = model.individualPullConfigs();
	
	@ChangeListener(structureListener=true)
	@Existence(required=CreateMode.OPTIONAL)
	public final ResourceList<RestPushConfig> individualPushConfigs = model.individualPushConfigs();
	
	
}
