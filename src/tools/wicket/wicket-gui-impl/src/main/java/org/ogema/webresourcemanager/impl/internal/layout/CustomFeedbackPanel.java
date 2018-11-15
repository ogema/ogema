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

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

public class CustomFeedbackPanel extends FeedbackPanel {

	private static final long serialVersionUID = 7530436807003218264L;

	public CustomFeedbackPanel(String id) {
		super(id);
	}

	public CustomFeedbackPanel(String id, IFeedbackMessageFilter filter) {
		super(id, filter);
	}

	@Override
	protected String getCSSClass(FeedbackMessage message) {
		String css;
		switch (message.getLevel()) {
		case FeedbackMessage.SUCCESS:
			css = "alert success";
			break;
		case FeedbackMessage.INFO:
			css = "alert info";
			break;
		case FeedbackMessage.ERROR:
			css = "alert error";
			break;
		default:
			css = "alert";
		}

		return css;
	}
}
