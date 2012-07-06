/**
 * Copyright 2009 - 2014
 *
 * Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Fraunhofer IIS
 * Fraunhofer ISE
 * Fraunhofer IWES
 *
 * All Rights reserved
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ogema.webresourcemanager.impl.internal.layout;

import java.util.Map;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.ogema.core.webresourcemanager.ApplicationPanel;
import org.ogema.core.webresourcemanager.JSWidget;

/**
 *
 * @author skarge
 */
public class IFramePanel extends ApplicationPanel {

	private final String alias;
	private final String title;

	public IFramePanel(final String alias, final String title) {
		this.alias = alias;
		this.title = title;
	}

	@Override
	public void initContent() {
		WebMarkupContainer container = new WebMarkupContainer("myFrame");
		container.add(new AttributeAppender("src", Model.of(alias)));
		addOrReplace(container);
	}

	@Override
	public Map<String, JSWidget> getWidgets() {
		return null;
	}

	@Override
	public String getTitle() {
		return title;
	}

}
