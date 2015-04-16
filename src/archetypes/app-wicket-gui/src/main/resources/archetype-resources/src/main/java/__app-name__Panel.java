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