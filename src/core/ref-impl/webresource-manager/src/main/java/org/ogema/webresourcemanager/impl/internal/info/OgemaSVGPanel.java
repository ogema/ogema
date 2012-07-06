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
package org.ogema.webresourcemanager.impl.internal.info;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class OgemaSVGPanel extends Panel {

	private static final long serialVersionUID = -6837143153127468974L;

	public OgemaSVGPanel(String id, int x, int y, float width, float height, String viewBox, String enableBackground) {
		super(id);
		WebMarkupContainer ogemaSVG = new WebMarkupContainer("myLogo");
		ogemaSVG.add(new AttributeModifier("version", "1.1"));
		ogemaSVG.add(new AttributeModifier("id", "Ebene_1"));
		ogemaSVG.add(new AttributeModifier("xmlns", "http://www.w3.org/2000/svg"));
		ogemaSVG.add(new AttributeModifier("xmlns:xlink", "http://www.w3.org/1999/xlink"));
		ogemaSVG.add(new AttributeModifier("x", x + "px"));
		ogemaSVG.add(new AttributeModifier("y", y + "px"));
		ogemaSVG.add(new AttributeModifier("width", width + "px"));
		ogemaSVG.add(new AttributeModifier("height", height + "px"));
		ogemaSVG.add(new AttributeModifier("viewBox", viewBox));
		ogemaSVG.add(new AttributeModifier("enable-background", enableBackground));
		ogemaSVG.add(new AttributeModifier("xml:space", "preserve"));

		addOrReplace(ogemaSVG);

	}

}
