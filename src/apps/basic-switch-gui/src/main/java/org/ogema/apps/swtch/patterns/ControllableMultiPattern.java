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
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.Equals;
import org.ogema.model.actors.MultiSwitch;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.prototypes.PhysicalElement;

@SuppressWarnings("unused")
public class ControllableMultiPattern extends ResourcePattern<MultiSwitch> {

	//	private final ApplicationManager appMan;

	public ControllableMultiPattern(Resource match) {
		super(match);
	}

	@Existence(required = CreateMode.OPTIONAL)
	private FloatResource stateControl = model.stateControl();

	@Existence(required = CreateMode.OPTIONAL)
	private FloatResource stateFeedback = model.stateFeedback();

	@Equals(value = 1)
	private BooleanResource controllable = model.controllable();

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
