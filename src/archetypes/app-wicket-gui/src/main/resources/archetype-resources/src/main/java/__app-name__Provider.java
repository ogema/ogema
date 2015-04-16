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
package ${package};

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.ogema.apps.wicket.ApplicationPage;
import org.ogema.apps.wicket.ApplicationPanel;
import org.ogema.apps.wicket.ComponentProvider;
import org.osgi.framework.Bundle;

@Service({ ComponentProvider.class })
@Component(specVersion = "1.2", immediate = true)
public class ${app-name}Provider implements ComponentProvider {

	private static final long serialVersionUID = 8449440527940958750L;
        private final ResourceReference image;
        private final List<ApplicationPanel> list;

        public ${app-name}Provider(){
            this.image = new PackageResourceReference(this.getClass(), "logo.jpg");
            this.list = new ArrayList<ApplicationPanel>();
            list.add(${app-name}Panel.getInstance());            
        }

	@Override
	public ResourceReference getImage() {		
		return image;
	}

	@Override
	public ApplicationPage getPage() {		
            return null;
	}

	@Override
	public List<ApplicationPanel> getPanel() {
            return list;
	}

	@Override
	public String getTitle() {
            return "${app-name}";
	}
}
