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
package org.ogema.app.webresman;

import java.util.Collection;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssContentHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.ogema.app.resource.management.gui.util.Util;
import org.ogema.app.resource.management.gui.ResManagementGuiActivator;
import org.ogema.apps.wicket.ApplicationPanel;

public class ResourceTypeContentPanel extends ApplicationPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6890912559018818634L;

	@Override
	public void initContent() {
		final WebMarkupContainer wmc = new WebMarkupContainer("radmin");

		add(wmc);

		AjaxEventBehavior event = new AjaxEventBehavior("onclick") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6472566516515572238L;

			@Override
			protected void onEvent(final AjaxRequestTarget target) {
				// do stuff here
				// target.appendJavaScript("alert('onClick');");
				Collection<? extends Component> components = target.getComponents();
				for (Component c : components) {
					String ajaxRegionMarkupId = c.getAjaxRegionMarkupId();
					String markupId = c.getMarkupId();
					// System.out.println(ajaxRegionMarkupId + " " + markupId);
				}

				target.appendJavaScript("window.alert(getChilds())");

			}
		};
		wmc.add(event);

	}

	@Override
	public void renderHead(IHeaderResponse response) {
		response.render(CssContentHeaderItem.forReference(new CssResourceReference(ResourceTypeContentPanel.class,
				"style.css")));
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				ResourceTypeContentPanel.class, "d3.js")));
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				ResourceTypeContentPanel.class, "d3.layout.js")));

		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				ResourceTypeContentPanel.class, "resourceViewPanel.js")));

		// ResourceManagement rm = ResManagementGuiActivator.getRm();
		// ResourceAccess ram = ResManagementGuiActivator.getRam();
		Util util = new Util(ResManagementGuiActivator.getAppManager());
		String allResoureJson = util.getAllResoureJson();
		response.render(OnDomReadyHeaderItem.forScript("first(" + allResoureJson + ");"));

	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Resource View";
	}

}
