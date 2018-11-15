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
