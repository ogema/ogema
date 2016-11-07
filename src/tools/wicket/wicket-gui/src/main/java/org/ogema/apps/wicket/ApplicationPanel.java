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

import org.apache.wicket.markup.html.panel.Panel;

public abstract class ApplicationPanel extends Panel {

	private static final long serialVersionUID = 85740243570559041L;
	private static final String CONTENT_ID = "content";

	/**
	 * Get a new OgemaContentPanel to register with
	 * {@link org.ogema.webresourcemanager.WebResourceManager.registerWebResource}. Initialization of the panel content
	 * should be done in initContent if the page may not be called at all to improve startup performance.
	 * 
	 * @param id
	 */
	public ApplicationPanel() {
		super(CONTENT_ID);
	}

	/**
	 * Called by the framework after constructor and right before the panel is actually shown. Time-consuming
	 * initialization should be done here.
	 */
	public abstract void initContent();

        /**
	 * @return Name of the panel as shown in the application's drop down menu and the panel overview.
	 */
	public abstract String getTitle();

	/**
	 * Wicket-ID of the panel, which can be referred to in html.
	 * 
	 * @return
	 */
	public String getContentId() {
		return CONTENT_ID;
	}

}
