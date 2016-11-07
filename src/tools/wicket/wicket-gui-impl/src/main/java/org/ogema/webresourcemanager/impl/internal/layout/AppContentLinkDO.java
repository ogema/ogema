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
package org.ogema.webresourcemanager.impl.internal.layout;

import java.io.Serializable;
import org.ogema.apps.wicket.ApplicationPanel;

public class AppContentLinkDO implements Serializable {

	private static final long serialVersionUID = -595289313538519902L;
	private final ApplicationPanel a;
	private final String t;
	private final MainContentPanel m;
	final String n;

	public AppContentLinkDO(final ApplicationPanel content, final String text, final MainContentPanel mainContent,
			final String bundleName) {
		this.a = content;
		this.t = text;
		this.m = mainContent;
		this.n = bundleName;
	}

	public ApplicationPanel getContent() {
		return a;
	}

	public String getText() {
		return t;
	}

	public MainContentPanel getMainContent() {
		return m;
	}

	public String getBundleName() {
		return n;
	}

}
