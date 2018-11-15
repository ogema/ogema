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
