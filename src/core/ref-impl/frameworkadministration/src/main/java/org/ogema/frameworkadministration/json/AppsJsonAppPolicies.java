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
package org.ogema.frameworkadministration.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tgries
 */
public class AppsJsonAppPolicies implements Serializable {

	private static final long serialVersionUID = 826125013193189749L;

	private List<AppsJsonAppPolicy> policies = new ArrayList<AppsJsonAppPolicy>();

	public List<AppsJsonAppPolicy> getPolicies() {
		return policies;
	}

	public void setPolicies(List<AppsJsonAppPolicy> policies) {
		this.policies = policies;
	}

}
