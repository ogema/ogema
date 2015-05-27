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
package org.ogema.serialization.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import static org.ogema.serialization.JaxbResource.NS_OGEMA_REST;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IntegerArrayResource", propOrder = { "values" })
@XmlRootElement(name = "integerArray", namespace = NS_OGEMA_REST)
public class IntegerArrayResource extends Resource {

	@XmlList
	@XmlElement(required = true)
	protected List<Integer> values;

	public List<Integer> getValues() {
        if (values == null) {
            values = new ArrayList<>();
        }
        return this.values;
    }
}
