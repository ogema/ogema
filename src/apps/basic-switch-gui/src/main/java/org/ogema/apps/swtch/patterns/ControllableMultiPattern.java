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
package org.ogema.apps.swtch.patterns;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.actors.MultiSwitch;
import org.ogema.model.prototypes.PhysicalElement;

@SuppressWarnings("unused")
public class ControllableMultiPattern extends ResourcePattern<MultiSwitch> {

	//	private final ApplicationManager appMan;

	public ControllableMultiPattern(Resource match) {
		super(match);
	}

	private final FloatResource stateControl = model.stateControl();

	@Existence(required = CreateMode.OPTIONAL)
	private final FloatResource stateFeedback = model.stateFeedback();

//	@Equals(value = 1)
//    @SuppressWarnings("deprecation")
//	private final BooleanResource controllable = model.controllable();

	public Resource getUppermostParent() {
		Resource res = model;
		while (res.getParent() != null)
			res = res.getParent();
		return res;
	}

	public String getName() {
		PhysicalElement parent;
		try {
			parent = (PhysicalElement) getUppermostParent();
		} catch (Exception e) {
			parent = model;
		}
		if (parent.name().isActive())
			return parent.name().getValue();
		else
			return parent.getLocation();
	}

	public String getValue() {
		if (!stateFeedback.isActive())
			return "0";
		return String.valueOf(stateFeedback.getValue());
	}

	public String getType() {
		return getUppermostParent().getResourceType().getSimpleName();
	}

	public String getRoom() {
		PhysicalElement parent;
		try {
			parent = (PhysicalElement) getUppermostParent();
		} catch (Exception e) {
			parent = model;
		}
		try {
			if (parent.location().room().name().isActive())
				return parent.location().room().name().getValue();
			else if (parent.location().room().isActive())
				return parent.location().room().getLocation();
			else
				return "";
		} catch (Exception e) {
			return "";
		}
	}

}
