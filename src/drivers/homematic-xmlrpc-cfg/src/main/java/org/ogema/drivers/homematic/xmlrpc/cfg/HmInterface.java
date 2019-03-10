/**
 * Copyright 2011-2019 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
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
package org.ogema.drivers.homematic.xmlrpc.cfg;

import java.net.URL;
import java.util.Objects;

import org.json.JSONObject;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HomeMatic;
import org.slf4j.LoggerFactory;

class HmInterface {
	
	static String WIRED = "wired";
	static String BID_COS = "BidCos";
	static String IP = "IP";

	private final HomeMatic hm;
	private final String type;
	private final URL url;
	private final String id;
	
	public HmInterface(HomeMatic hm, URL url) {
		this.hm = Objects.requireNonNull(hm);
		this.url = Objects.requireNonNull(url);
		this.id = url.toString();
		final int port = url.getPort();
		switch (port) {
		case 2001:
		case 42001:
			type = HmInterface.BID_COS;
			break;
		case 2010:
		case 42010:
			type = HmInterface.IP;
			break;
		case 2000:
		case 42000:
			type = HmInterface.WIRED;
			break;
		default:
			LoggerFactory.getLogger(getClass()).warn("Unknown port in Homematic server URL: {}", url);
			type = "unknown";
			break;
		}
	}
	
	public HmInterface(HomeMatic hm) {
		this.hm = Objects.requireNonNull(hm);
		this.type = hm.hashCode() + ""; // not very nice...
		this.id = type;
		this.url = null;
	}

	public HomeMatic getHm() {
		return hm;
	}
	
	public String getType() {
		return type;
	}
	
	/**
	 * may be null
	 * @return
	 */
	public URL getUrl() {
		return url;
	}
	
	public String getId() {
		return id;
	}
	
	public String toJson() {
		final StringBuilder sb = new StringBuilder();
		sb.append("{\"type\":")
			.append(JSONObject.quote(type))
			.append(",\"id\":")
			.append(JSONObject.quote(id));
		if (url != null)
			sb.append(",\"url\":").append(JSONObject.quote(url.toString()));
		sb.append('}');
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return "HmInterface: " + toJson();
	}
	
}
