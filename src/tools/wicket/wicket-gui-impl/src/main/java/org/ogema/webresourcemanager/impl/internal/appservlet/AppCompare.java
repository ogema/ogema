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
package org.ogema.webresourcemanager.impl.internal.appservlet;

import java.util.Comparator;

/**
 *
 * @author tgries
 */
public class AppCompare implements Comparator<AppsJsonGet> {

	@Override
	public int compare(AppsJsonGet o1, AppsJsonGet o2) {
		return o1.getName().compareToIgnoreCase(o2.getName());
	}

}
