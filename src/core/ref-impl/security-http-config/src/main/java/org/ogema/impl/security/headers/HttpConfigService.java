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
package org.ogema.impl.security.headers;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

/**
 * We'll get one instance of this registered as a service per configuration 
 * with PID "org.ogema.impl.security.HeaderConfig~SOMETHING".
 */
/*
@Component(
		configurationPid=HttpConfigService.HEADER_CONFIG_PID,
		service=HttpConfigService.class,
		property= {
				"service.factoryPid=" + HttpConfigService.HEADER_CONFIG_PID
		},
		configurationPolicy=ConfigurationPolicy.REQUIRE
)
*/
public class HttpConfigService {

	public static final String HEADER_CONFIG_PID = "org.ogema.impl.security.HttpConfig";

	private HttpBundleConfiguration config;
	
	@Activate
	protected void activate(HttpBundleConfiguration config) {
		this.config = config;
	}
	
	public HttpBundleConfiguration getConfiguration() {
		return config;
	}
	
}
