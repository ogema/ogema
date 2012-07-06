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
package org.ogema.channelmapper.impl;

import java.io.File;

import javax.xml.bind.JAXB;

public class PersistentConfiguration {
	private final String DEFAULT_FILENAME = "config/channelmapper.config";

	private String filename = DEFAULT_FILENAME;

	private MappingConfiguration mappingConfiguration = null;

	public PersistentConfiguration() {
	}

	public PersistentConfiguration(String filename) {
		this.filename = filename;
	}

	public void read() {
		File file = new File(filename);

		if (!file.exists()) {
			mappingConfiguration = new MappingConfiguration();
		}
		else {
			mappingConfiguration = JAXB.unmarshal(file, MappingConfiguration.class);
		}
	}

	public void write() {
		File file = new File(filename);
		JAXB.marshal(mappingConfiguration, file);
	}

	public MappingConfiguration getMappingConfiguration() {
		if (mappingConfiguration == null) {
			mappingConfiguration = new MappingConfiguration();
		}

		return mappingConfiguration;
	}

}
