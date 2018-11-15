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

import org.apache.wicket.markup.html.basic.Label;
import org.ogema.apps.wicket.ApplicationPanel;

public class ${app-name}Panel extends ApplicationPanel {
	
	private static final long serialVersionUID = 45676583578358L;
        private final Label title;
        private static ${app-name}Panel instance;

        private ${app-name}Panel(){
            this.title = new Label("label", "Hello ${app-name}");
        }

	@Override
	public void initContent() {
		add(title);
	}
	
	@Override
	public String getTitle() {
              return "${app-name}";
	}

        public static ${app-name}Panel getInstance(){
            if(${app-name}Panel.instance == null){
                ${app-name}Panel.instance = new  ${app-name}Panel();
            }
            return ${app-name}Panel.instance;
        }

	
}