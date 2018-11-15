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
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.prototypes.PhysicalElement;

@SuppressWarnings("unused")
public class ControllableOnOffPattern extends ResourcePattern<PhysicalElement> {

	//	private final ApplicationManager appMan;

	public ControllableOnOffPattern(Resource match) {
		super(match);
	}

	private final BooleanResource stateControl = model.getSubResource("onOffSwitch", OnOffSwitch.class).stateControl(); // FIXME requires that name of the subresource is "onOffSwitch"

	@Existence(required = CreateMode.OPTIONAL)
	private final BooleanResource stateFeedback = model.getSubResource("onOffSwitch", OnOffSwitch.class).stateFeedback();

//	@Equals(value = 1)
//    @SuppressWarnings("deprecation")
//	private final BooleanResource controllable = model.getSubResource("onOffSwitch", OnOffSwitch.class).controllable();

	public String getName() {
		if (model.name().isActive())
			return model.name().getValue();
		else
			return model.getName();
	}

	public String getValue() {
		if (!stateFeedback.isActive())
			return "off";
		return stateFeedback.getValue() ? "on" : "off";
	}

	public String getType() {
		return model.getResourceType().getSimpleName();
	}

	public String getRoom() {
		try {
			if (model.location().room().name().isActive())
				return model.location().room().name().getValue();
			else if (model.location().room().isActive())
				return model.location().room().getLocation();
			else
				return "";
		} catch (Exception e) {
			return "";
		}
	}

	public boolean isReference() {
		return model.getSubResource("onOffSwitch", OnOffSwitch.class).isReference(false);
	}

}
