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
