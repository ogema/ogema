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
