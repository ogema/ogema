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
package org.ogema.tests.security.testbase.servlet;

import java.io.InputStream;
import java.io.StringReader;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.input.ReaderInputStream;
import org.ogema.core.security.WebAccessManager;
import org.ogema.tests.security.testbase.app.impl.TestApp;

/**
 * Mocks a static web page (html document)
 */
public class TestWebresource implements AutoCloseable {

	private final static String WEB_RESOURCE = "<html><head></head><body></body></html>";
	private final static AtomicInteger cnt = new AtomicInteger(0);
	private final String path;
	private final WebAccessManager wam;
	
	public TestWebresource(WebAccessManager webManager) {
		this.wam = webManager;
		this.path = webManager.registerWebResource("/securitytestresource" + cnt.getAndIncrement(), TestApp.WEBRESOURCE_PATH);
	}

	public String getPath() {
		return path + "/index.html";
	}
	
	@Override
	public void close() {
		try {
			wam.unregisterWebResource(path);
		} catch (Exception e) {/*ignore*/}
	}
	
	public static InputStream getWebResource() {
		return new ReaderInputStream(new StringReader(WEB_RESOURCE), "UTF-8");
	}
	
}
