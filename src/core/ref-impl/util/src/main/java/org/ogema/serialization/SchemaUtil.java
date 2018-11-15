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
