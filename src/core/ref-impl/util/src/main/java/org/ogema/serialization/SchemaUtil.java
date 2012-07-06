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
package org.ogema.serialization;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.SAXException;

/**
 * 
 * @author jlapp
 */
public class SchemaUtil {

	private static class SchemaInstance {

		static final Schema INSTANCE = loadSchema();
		static SAXException ex;

		static Schema loadSchema() {
			try {
				SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				Schema schema = sf.newSchema(SchemaUtil.class.getResource("/Ogema.xsd"));
				return schema;
			} catch (SAXException e) {
				ex = e;
				return null;
			}
		}
	}

	public static Schema getSchema() throws SAXException {
		if (SchemaInstance.INSTANCE == null) {
			throw SchemaInstance.ex;
		}
		return SchemaInstance.INSTANCE;
	}

}
