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
package org.ogema.app.resourcecombiner.config;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Java DO parsing the configuration XML file and provides it in the form
 * of a Java DO.
 */
@XmlRootElement
public class CombineConfigurator {

	private List<CombinerDO> combine;

	public List<CombinerDO> readCombines() {
		File file = new File("config/resource-combiner.xml");
		return readCombines(file);
	}

	public List<CombinerDO> readCombines(File file) {
		CombineConfigurator c = JAXB.unmarshal(file, CombineConfigurator.class);
		return c.getCombine();
	}

	public List<CombinerDO> getCombine() {
		return combine;
	}

	public void setCombine(List<CombinerDO> combine) {
		this.combine = combine;
	}

}
