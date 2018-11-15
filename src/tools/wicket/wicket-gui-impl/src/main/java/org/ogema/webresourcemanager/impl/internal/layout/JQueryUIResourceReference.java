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
