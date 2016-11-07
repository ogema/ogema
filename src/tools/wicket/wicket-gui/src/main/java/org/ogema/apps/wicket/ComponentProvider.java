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
