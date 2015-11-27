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
package org.ogema.app.resource.management.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.ogema.app.resource.management.tablegui.ResourcesTablePanel;
import org.ogema.app.resource.management.tablegui.SchedulesTablePanel;
import org.ogema.app.webresman.ResourceTypeContentPanel;
import org.ogema.apps.wicket.ApplicationPage;
import org.ogema.apps.wicket.ApplicationPanel;
import org.ogema.apps.wicket.ComponentProvider;
import org.osgi.framework.Bundle;

@Service(ComponentProvider.class)
@Component(specVersion = "1.1", immediate = true)
public class ResManagementProvider implements ComponentProvider {
	private static final long serialVersionUID = 1L;

	@Override
    public List<ApplicationPanel> getPanel() {
        List<ApplicationPanel> panelList = new ArrayList<>();
        ResourceTypeContentPanel types = new ResourceTypeContentPanel();
        ResourcesTablePanel table = new ResourcesTablePanel();
        SchedulesTablePanel schedules = new SchedulesTablePanel();
        panelList.add(types);
        panelList.add(table);
        panelList.add(schedules);
        return panelList;
    }

	@Override
	public ApplicationPage getPage() {
		return null;
	}

	
//	@Override
//	public Bundle getTargetApp() {
//		Bundle bundle = ResManagementGuiActivator.getBundle();
//		return bundle;
//	}

	@Override
	public ResourceReference getImage() {
		ResourceReference ref = new PackageResourceReference(ResourceTypeContentPanel.class, "icon.jpg");
		return ref;
	}

	@Override
	public String getTitle() {
		return "Resource View";
	}

}
