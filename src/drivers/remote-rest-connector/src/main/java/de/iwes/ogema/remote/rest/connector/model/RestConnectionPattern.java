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
