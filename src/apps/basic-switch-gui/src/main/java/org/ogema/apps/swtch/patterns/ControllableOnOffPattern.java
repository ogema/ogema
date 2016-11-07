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
