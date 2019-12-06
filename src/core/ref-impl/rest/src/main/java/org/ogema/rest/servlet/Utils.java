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
package org.ogema.rest.servlet;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ogema.accesscontrol.Constants;

public class Utils {
	
	public static final String JSON = "application/json";
	public static final String XML = "application/xml";
	private static final List<String> PARAMS_NO_LOGGING  = Arrays.asList(Constants.OTPNAME, Constants.OTUNAME); 

	public static boolean xmlOrJson(final HttpServletRequest req) {
		String accept = req.getHeader("Accept");
		if (accept == null || accept.equals("*/*")) {
			String contentType = req.getHeader("Content-Type");
			if (contentType != null) {
				contentType = contentType.toLowerCase();
				if (contentType.startsWith(XML))
					return true;
				if (contentType.startsWith(JSON))
					return false;
			}
			return false; // undetermined; json as default
		}
		accept = accept.toLowerCase();
		if (accept.contains(XML))
			return true;
		return false; // default: json
	}
	
	@SuppressWarnings("unchecked")
	static StringBuilder mapParameters(final HttpServletRequest req) {
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String[]> entry: ((Map<String, String[]>) req.getParameterMap()).entrySet()) {
			if (PARAMS_NO_LOGGING.contains(entry.getKey()))
				continue;
			if (!first)
				sb.append(',').append(' ');
			first = false;
			sb.append(entry.getKey()).append(':').append(Arrays.toString(entry.getValue()));
		}
		return sb;
	}
	
}
