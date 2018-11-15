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

import java.util.concurrent.atomic.AtomicReference;

import org.osgi.framework.Bundle;

class ConfigSupplierImpl {

	private final AtomicReference<HttpConfigImpl> headerConfig;
	private final Bundle bundle;
	
	public ConfigSupplierImpl(Bundle bundle) {
		this(bundle, null);
	}
	
	public ConfigSupplierImpl(Bundle bundle, HttpConfigImpl config) {
		this.bundle = bundle;
		final HttpConfigImpl initialConfig = config != null ? config : HttpConfigImpl.DEFAULT_CONFIG;
		this.headerConfig = new AtomicReference<HttpConfigImpl>(initialConfig);
	}

	public HttpConfigImpl getConfiguration() {
		return headerConfig.get();
	}
	
	void setConfig(HttpConfigImpl update) {
		headerConfig.set(update == null ? HttpConfigImpl.DEFAULT_CONFIG : update);
	}

}
