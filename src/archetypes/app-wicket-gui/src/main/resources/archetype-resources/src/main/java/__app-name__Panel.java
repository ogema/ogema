/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;
import org.ogema.service.webresourcemanager.ApplicationPanel;
import org.ogema.service.webresourcemanager.JSWidget;

public class ${app-name}Panel extends ApplicationPanel {
	
	private static final long serialVersionUID = 45676583578358L;

	@Override
	public void initContent() {
		Label l = new Label("label", "Hello ${app-name}");
		add(l);
	}
	
	

	@Override
	public String getTitle() {
		return "${app-name}";
	}

	@Override
	public Map<String, JSWidget> getWidgets() {
		return null;
	}
}