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

import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.ogema.apps.wicket.ApplicationPanel;

public class IFramePanel extends ApplicationPanel {

	private static final long serialVersionUID = 1L;
	private final String url;
    public IFramePanel(final String externalURL){
        super();
        this.url = externalURL;
    }

    @Override
    public void initContent() {
        RedirectPage page = new RedirectPage(url);
        InlineFrame frame = new InlineFrame("myFrame", page);
        add(frame);
    }

    @Override
    public String getTitle() {
        return "iframe";
    }

    

}
