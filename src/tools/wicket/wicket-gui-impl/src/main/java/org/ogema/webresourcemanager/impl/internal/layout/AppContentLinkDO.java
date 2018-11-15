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
