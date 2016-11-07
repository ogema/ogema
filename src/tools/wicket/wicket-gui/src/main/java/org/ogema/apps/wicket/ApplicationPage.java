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

import org.apache.wicket.markup.html.WebPage;

public abstract class ApplicationPage extends WebPage {

	private static final long serialVersionUID = -4500245251547381563L;

	/**
	 * Called by the framework after constructor and right before the Page is actually shown. Time-consuming
	 * initialization should be done here.
	 */
	public abstract void initContent();
}
