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
package org.ogema.frameworkadministration.servlet;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ogema.core.logging.LogOutput;
import org.ogema.frameworkadministration.controller.LoggerController;
import org.ogema.frameworkadministration.json.post.LoggerJsonPost;
import org.ogema.frameworkadministration.json.post.LoggerJsonPostList;
import org.ogema.frameworkadministration.utils.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FAServletLogger extends HttpServlet {

	private static final long serialVersionUID = 41676583578358L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String action = req.getParameter("action");

		if ("logout".equals(action)) {
			req.getSession().invalidate();
			return;
		}
		LoggerController.getInstance().writeAllLoggersJSON(resp.getWriter());
		resp.setStatus(200);
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

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

			ObjectMapper objectMapper = new ObjectMapper();
			LoggerJsonPostList loggerJsonPostList = objectMapper.readValue(jsonString, LoggerJsonPostList.class);

			//System.out.println(loggerJsonPostList);

			String action = loggerJsonPostList.getAction();

			if (action.equals("sizeChange")) {
				LoggerJsonPost loggerJsonPost = loggerJsonPostList.getElements().get(0);
				String name = loggerJsonPost.getName();

				switch (name) {
				case "sizeCache":
					LoggerController.getInstance().setSizeLogger(LogOutput.CACHE, loggerJsonPost.getValue(), resp);
					break;
				case "sizeFile":
					LoggerController.getInstance().setSizeLogger(LogOutput.FILE, loggerJsonPost.getValue(), resp);
					break;
				default:
					break;
				}
			}
			else if (action.equals("singleChange") || action.equals("bulkChange")) {

				for (LoggerJsonPost loggerJsonPost : loggerJsonPostList.getElements()) {
					LoggerController.getInstance().setLoggerValues(loggerJsonPost);
				}

			}
			else if (action.equals("getCache")) {
				String result = LoggerController.getInstance().getCacheContent();
				resp.getWriter().write(result);
				resp.setStatus(200);
			}
		}

	}

}
