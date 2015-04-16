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
package org.ogema.util;

import java.util.Dictionary;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

public class OsgiUtils {

	public String getBundleNameFromHeader(Bundle bundle) {
		Dictionary<String, ?> dic = bundle.getHeaders();
		String[] name = null;
		if (dic == null) {
			System.out.println("Could not read Admin Manifest");
			return null;
		}
		else {
			if (dic.get(Constants.BUNDLE_NAME) != null && dic.get(Constants.BUNDLE_NAME).getClass().isArray()) {
				name = (String[]) dic.get(Constants.BUNDLE_NAME);
			}
			else {
				name = new String[1];
				name[0] = (String) dic.get(Constants.BUNDLE_NAME);
			}
		}
		if (name[0] == null)
			return "Unknown bundle";
		return name[0];
	}

}
