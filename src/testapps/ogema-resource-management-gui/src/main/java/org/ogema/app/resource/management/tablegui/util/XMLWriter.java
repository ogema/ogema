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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.ogema.core.application.ApplicationManager;

/**
 * 
 * @author cnoelle
 * 
 */
public class XMLWriter {

	public XMLWriter(ApplicationManager am) {
	}

	public File createFile(List<String[]> data, File file) {
		List<ResourceXML> list = new ArrayList<ResourceXML>();
		for (String[] item : data) {

			String path = item[0];
			String type = item[1];
			// returns false unless the String equals true (case insensitive)
			boolean active = Boolean.parseBoolean(item[2]);
			String location = item[3];
			String value = item[5];
			ResourceXML object = new ResourceXML();
			object.setValue(value);
			object.setPath(path);
			object.setType(type);
			object.setActive(active);
			object.setLocation(location);
			list.add(object);
		}
		Resources res = new Resources();
		res.setRes(list);

		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(Resources.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			JAXB.marshal(res, file);
		} catch (JAXBException e) {
			e.printStackTrace();
			return null;
		}
		return file;
	}

}
