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
