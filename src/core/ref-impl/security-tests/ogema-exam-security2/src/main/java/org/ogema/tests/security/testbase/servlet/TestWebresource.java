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
public class TestWebresource {

	private final static String WEB_RESOURCE = "<html><head></head><body></body></html>";
	private final static AtomicInteger cnt = new AtomicInteger(0);
	private final String path;
	
	public TestWebresource(WebAccessManager webManager) {
		this.path = webManager.registerWebResource("/securitytestresource" + cnt.getAndIncrement(), TestApp.WEBRESOURCE_PATH);
	}

	public String getPath() {
		return path + "/index.html";
	}
	
	public static InputStream getWebResource() {
		return new ReaderInputStream(new StringReader(WEB_RESOURCE), "UTF-8");
	}
	
}
