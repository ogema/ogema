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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptUrlReferenceHeaderItem;
import org.apache.wicket.resource.JQueryResourceReference;

public class JQueryUIResourceReference extends JavaScriptUrlReferenceHeaderItem {

	private static final long serialVersionUID = 1L;

	public JQueryUIResourceReference(String URL) {
		super(URL, "jquery-ui-1.9.1", true, "utf-8", "");
	}

	@Override
    public Iterable<? extends HeaderItem> getDependencies() {
        List<HeaderItem> deps = new ArrayList<>();
        deps.add(JavaScriptHeaderItem.forReference(JQueryResourceReference.get()));
        return deps;
    }
}
