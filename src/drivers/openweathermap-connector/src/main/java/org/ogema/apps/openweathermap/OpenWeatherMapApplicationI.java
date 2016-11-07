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
package org.ogema.apps.openweathermap;

import java.util.Map;

import org.ogema.core.application.Application;
import org.ogema.core.model.Resource;
/**
 *  
 *class for OSGI services.
 *@author brequardt
 */
public interface OpenWeatherMapApplicationI extends Application {

	public Resource createEnvironment(String name, String city, String country);
	
	public Map<String,Object> getEnviromentParameter(String name);
	
}
