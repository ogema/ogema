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
package org.ogema.examples;

import org.apache.wicket.markup.html.basic.Label;
import org.ogema.apps.wicket.ApplicationPanel;

public class WicketGuiExamplePanel extends ApplicationPanel {
	
	private static final long serialVersionUID = 45676583578358L;
        private final Label title;
        private static WicketGuiExamplePanel instance;

        private WicketGuiExamplePanel(){
            this.title = new Label("label", "Hello WicketGuiExample");
        }

	@Override
	public void initContent() {
		add(title);
	}
	
	@Override
	public String getTitle() {
              return "WicketGuiExample";
	}

        public static WicketGuiExamplePanel getInstance(){
            if(WicketGuiExamplePanel.instance == null){
                WicketGuiExamplePanel.instance = new  WicketGuiExamplePanel();
            }
            return WicketGuiExamplePanel.instance;
        }

	
}