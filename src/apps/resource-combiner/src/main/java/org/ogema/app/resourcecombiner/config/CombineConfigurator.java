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
