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
package org.ogema.tools.remote.ogema.auth;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;

public interface RemoteOgemaAuth {

	/**
	 * Service property that may be used to filter for an auth services
	 * target at a specific OGEMA instance.
	 */
	public static final String REMOTE_HOST_PROPERTY = "remoteHost";
	/**
	 * Service property that may be used to filter for an auth services
	 * target at a specific OGEMA instance.
	 */
	public static final String REMOTE_PORT_PROPERTY = "remotePort";
	
	/**
	 * Apply authorization to the passed request and send it, using the passed client.
	 * @param client
	 * @param request
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	CloseableHttpResponse execute(CloseableHttpClient client, HttpUriRequest request) throws ClientProtocolException, IOException;

}
