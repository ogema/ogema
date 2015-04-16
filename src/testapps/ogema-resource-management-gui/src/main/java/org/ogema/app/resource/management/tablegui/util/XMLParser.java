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
package org.ogema.app.resource.management.tablegui.util;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXB;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;

/**
 * 
 * @author cnoelle
 * 
 */
public class XMLParser {
	private final OgemaLogger logger;
	int successCounter = 0;
	int failureCounter = 0;

	public XMLParser(ApplicationManager am) {
		this.logger = am.getLogger();
	}

	public int installConfiguration(File file) {
		Resources res = JAXB.unmarshal(file, Resources.class);
		List<ResourceXML> list = res.getRes();
		for (ResourceXML resourceObject : list) {
			if (createResource(resourceObject)) {
				successCounter++;
			}
			else {
				failureCounter++;
			}
		}
		return successCounter;
	}

	private boolean createResource(ResourceXML object) {
		String path = object.getPath();
		String type = object.getType();
		String location = object.getLocation();
		boolean active = object.getActive();
		String value = object.getValue();
		if (path == null || type == null) {
			logger.warn("Path or type found null. Unable to create resource.");
			return false;
		}
		if (Util.getInstance().createResource(path, type, location, active, value)) {
			return true;
		}
		else {
			return false;
		}
	}

}
