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
