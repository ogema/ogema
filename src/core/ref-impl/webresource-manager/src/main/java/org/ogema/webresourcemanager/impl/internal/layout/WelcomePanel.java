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
import org.ogema.core.webresourcemanager.ApplicationPanel;
import org.ogema.core.webresourcemanager.JSWidget;
import org.ogema.webresourcemanager.impl.internal.info.OgemaSVGPanel;

/**
 *
 * @author skarge
 */
public class WelcomePanel extends ApplicationPanel {

	private static final long serialVersionUID = 85740243570551041L;

	@Override
	public void initContent() {
		final OgemaSVGPanel ogema20 = new OgemaSVGPanel("ogemaSVG", 0, 0, 600f, 500f, "0 0 430.98 326.127",
				"new 0 0 430.98 326.127");
		addOrReplace(ogema20);
	}

	@Override
	public Map<String, JSWidget> getWidgets() {
		return null;
	}

	@Override
	public String getTitle() {
		return "Start: OGEMA 2.0";
	}

}
