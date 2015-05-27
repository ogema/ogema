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

package org.ogema.tools.grafana.base;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;

@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class GrafanaBaseApp implements Application {

	public static String WEB_RES_PATH;
	private ApplicationManager am;

	@Override
	public void start(ApplicationManager am) {
		this.am = am;
		String packagePath = "org/ogema/tools/grafana/base/grafana-1.9.1";
		WEB_RES_PATH = am.getWebAccessManager().registerWebResourcePath("", packagePath);
		//    	WEB_RES_PATH = am.getWebAccessManager().registerWebResource("/org/ogema/tools/grafana-base" , packagePath);      
		// System.out.println("  Grafana base resources registered under " + WEB_RES_PATH);
	}

	@Override
	public void stop(AppStopReason asr) {
		am.getWebAccessManager().unregisterWebResourcePath("");
	}

}
