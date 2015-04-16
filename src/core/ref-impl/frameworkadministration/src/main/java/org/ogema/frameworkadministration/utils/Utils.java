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
package org.ogema.frameworkadministration.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.ogema.frameworkadministration.json.JsonReplyMessage;

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
	public static final String USER_PRECONDITIONFILE = "file:./ogema/users/";
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

		return "{}";

	}

}
