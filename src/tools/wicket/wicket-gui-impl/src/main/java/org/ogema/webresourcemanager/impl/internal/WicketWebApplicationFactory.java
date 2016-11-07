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
package org.ogema.webresourcemanager.impl.internal;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.ops4j.pax.wicket.api.WebApplicationFactory;

@Component
@Properties( { @Property(name = "pax.wicket.applicationname", value = "ogema"),
		@Property(name = "pax.wicket.mountpoint", value = "wicket") })
@Service(WebApplicationFactory.class)
public class WicketWebApplicationFactory implements WebApplicationFactory<OgemaWicketApplication> {

	{
		System.err.println("### INIT " + WicketWebApplicationFactory.class.getName());
	}

	@Override
	public Class<OgemaWicketApplication> getWebApplicationClass() {
		return OgemaWicketApplication.class;
	}

	@Override
	public void onInstantiation(OgemaWicketApplication application) {
		// Nothing to do here...
		System.err.println("### INIT " + application);
	}

}
