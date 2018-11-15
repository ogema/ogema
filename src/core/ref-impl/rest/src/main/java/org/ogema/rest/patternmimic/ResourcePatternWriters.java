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
package org.ogema.rest.patternmimic;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.rest.servlet.Utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public final class ResourcePatternWriters {

	private static final String PARAM_RECURSIVE = "recursive";
	private static final String PARAM_MAX_HITS = "maxHits";
	private static final String PARAM_FROM = "from";
	
	public static interface ResourcePatternWriter {
		
		void write(FakePattern pattern, Writer w) throws IOException, JAXBException;
		
		FakePattern getPattern(String in) throws JAXBException, JsonParseException, JsonMappingException, IOException;

		String contentType();
	}

	public static ResourcePatternWriter forRequest(HttpServletRequest req, ApplicationManager appMan) {
		final boolean xmlOrJson = Utils.xmlOrJson(req);
		//System.out.println("Writer for type: " + accept);
		String recursiveStr = req.getParameter(PARAM_RECURSIVE);
		boolean recursive = false;
		if (recursiveStr != null) {
			try {
				recursive = Boolean.parseBoolean(recursiveStr);
			} catch (NumberFormatException e) {}
		}
		int maxHits = getNonNegativeInt(req, PARAM_MAX_HITS, Integer.MAX_VALUE);
		int from = getNonNegativeInt(req, PARAM_FROM, 0);
		Resource parent = selectResource(req.getPathInfo(), appMan); // may be null, for toplevel
		return xmlOrJson ? createXmlWriter(appMan, parent, recursive, maxHits, from) : createJsonWriter(appMan, parent, recursive, maxHits, from);
	}

	private static ResourcePatternWriter createJsonWriter(final ApplicationManager appMan, final Resource parent, 
				final boolean recursive, final int maxHits, final int from) {
		
		final SerializationManager sman = new SerializationManager();
		
		return new ResourcePatternWriter() {

			@Override
			public void write(FakePattern pattern, Writer w) throws IOException, JAXBException {
				FakePatternAccess fpa = new FakePatternAccess(appMan);
				PatternMatchList matches = fpa.getMatches(pattern, parent, recursive, maxHits,from);
				w.write(sman.toJson(matches));
			}
			
			@Override
			public FakePattern getPattern(String in) throws JsonParseException, JsonMappingException, IOException {
				return sman.fromJson(in);
			}

			@Override
			public String contentType() {
				return "application/json; charset=utf-8";
			}
		};
	}

	private static ResourcePatternWriter createXmlWriter(final ApplicationManager appMan, final Resource parent, 
					final boolean recursive, final int maxHits, final int from) {
		
		final SerializationManager sman = new SerializationManager();
	
		return new ResourcePatternWriter() {
			
			@Override
			public void write(FakePattern pattern, Writer w) throws IOException, JAXBException {
				FakePatternAccess fpa = new FakePatternAccess(appMan);
				PatternMatchList matches = fpa.getMatches(pattern, parent, recursive, maxHits,from);
				w.write(sman.toXml(matches));
			}

			@Override
			public FakePattern getPattern(String in) throws JAXBException {
				return sman.fromXml(in);
			}
			
			@Override
			public String contentType() {
				return "application/xml; charset=utf-8";
			}
		};
	}
	
	private static final Resource selectResource(String pathInfo, ApplicationManager am) {
		if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) {
			return null;
		}
		if (pathInfo.startsWith("/")) {
			pathInfo = pathInfo.substring(1);
		}
		String[] path = pathInfo.split("/");
		if (path.length == 0 || path[0].isEmpty()) {
			return null;
		}
		Resource r = am.getResourceAccess().getResource(path[0]);
		for (int i = 1; i < path.length && r != null && r.exists(); i++) {
			r = r.getSubResource(path[i]);
		}
		if (r == null || !r.exists()) {
			return null;
		}
		return r;
	}

	private static final int getNonNegativeInt(HttpServletRequest req, String param, int defaultVal) {
		int result = defaultVal;
		String valueStr = req.getParameter(param);
		try {
			result = Integer.parseInt(valueStr);
			if (result < 0) 
				result = defaultVal;
		} catch (Exception e) {}
		return result;
	}
	
}
