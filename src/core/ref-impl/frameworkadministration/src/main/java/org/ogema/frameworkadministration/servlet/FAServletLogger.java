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
