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
package org.ogema.apps.wicket;

import java.io.Serializable;
import java.util.List;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Any application wanting to display web-pages implements a ComponentProvider. From this, OGEMA gets the list of all
 * web-pages to be displayed within the framework GUI (panels), the separate page displayed in a separate browser window
 * (page) and the menu entry and icon to use in the framework navigation.
 */
// FIXME annotations?
@Service
@Reference(cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
public interface ComponentProvider extends Serializable {

	/**
	 * Register application page to be integrated into framework navigation.
	 * 
	 * @return all panels implemented by the application to be registered.
	 */
	List<ApplicationPanel> getPanel();

	/**
	 * Registers full page into the URI name space of the http service.<br>
	 * Note: Requires permission of application to display pages outside the framework navigation
	 * 
	 * @return ApplicationPage implemented by the application to be registered
	 */
	ApplicationPage getPage();

	/**
	 * 
	 * @return Image to Display in the MenuEntry
	 */
	ResourceReference getImage();

	/**
	 * 
	 * @return the name of the application: menu entry
	 */
	String getTitle();
}
