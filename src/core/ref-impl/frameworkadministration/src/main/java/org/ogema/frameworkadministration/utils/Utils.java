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
package org.ogema.frameworkadministration.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.ogema.frameworkadministration.json.JsonReplyMessage;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author tgries
 */
public class Utils {

	public static final String ACTION_GETUSERDATA = "getUserData";
	public static final String ACTION_BULKCHANGE = "bulkChange";
	public static final String ACTION_SINGLECHANGE = "singleChange";
	public static final String ACTION_SIZECHANGE = "sizeChange";
	public static final String[] FILTERED_PERMISSIONS = { "urps", "basic import rights", "felix property access" };
	public static final String[] FILTERED_USERAPPS = { "URP@" };

	public static final String USER_CONDITIONINFOTYPE = "org.osgi.service.condpermadmin.BundleLocationCondition";
	public static final String USER_PRECONDITIONFILE = "urp:";
	public static final String USER_SUFCONDITIONFILE = "/*";
	public static final String USER_PERMISSIONAME = "org.ogema.accesscontrol.ResourcePermission";

	public static final String USER_ALLPERMISSION = "java.security.AllPermission";

	public static final boolean DEBUG = true;

	public static void log(String message, Class<?> fromClass) {
		if (DEBUG) {
			System.out.println("DEBUG " + fromClass + ": " + message);
		}
	}

	public static boolean isValidJSON(final String json) {
		boolean valid = false;
		try {
            @SuppressWarnings("deprecation")
			final JsonParser parser = new ObjectMapper().getJsonFactory().createJsonParser(json);
			while (parser.nextToken() != null) {
			}
			valid = true;
		} catch (JsonParseException jpe) {
			jpe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return valid;
	}

	public static String createMessage(String type, String message) {

		ObjectMapper mapper = new ObjectMapper();

		String result = "{}";

		JsonReplyMessage replyMessage = new JsonReplyMessage();
		replyMessage.setType(type);
		replyMessage.setMessage(message);

		try {
			result = mapper.writeValueAsString(replyMessage);
		} catch (IOException ex) {
			Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
		}

		return result;

	}

	public static String readJsonFromRequest(HttpServletRequest req) throws IOException {

		String result = "{}";

		StringBuilder sb = new StringBuilder();
		BufferedReader reader = req.getReader();
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
		} finally {
			reader.close();
		}

		String jsonString = sb.toString();

		if (Utils.isValidJSON(jsonString)) {
			return jsonString;
		}

		return result;

	}

}
