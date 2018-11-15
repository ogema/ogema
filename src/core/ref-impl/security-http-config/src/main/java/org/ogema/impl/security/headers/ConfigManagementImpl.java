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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ogema.accesscontrol.HttpConfig;
import org.ogema.accesscontrol.HttpConfigManagement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;

@Component(service=HttpConfigManagement.class)
public class ConfigManagementImpl implements HttpConfigManagement {

	private final Set<ServiceReference<HttpConfigService>> pendingServices = 
			Collections.synchronizedSet(new HashSet<ServiceReference<HttpConfigService>>());
	private final ConcurrentMap<String, ServiceReference<HttpConfigService>> configs = new ConcurrentHashMap<>(4, 0.75f, 4);
	private final Map<String, ConfigSupplierImpl> headerConfigs = new MapMaker()
			.initialCapacity(30) // should accommodate the nr of apps with web resources 
			.concurrencyLevel(4)
			.weakValues()
			.makeMap();

	private volatile BundleContext ctx;
	
	@Reference(
			service=HttpConfigService.class,
			cardinality=ReferenceCardinality.MULTIPLE,
			policy=ReferencePolicy.DYNAMIC,
			policyOption=ReferencePolicyOption.GREEDY,
			unbind="removeHeader"
	)
	protected void addHeaderConfig(ServiceReference<HttpConfigService> configRef) {
		BundleContext ctx = this.ctx;
		if (ctx  == null) { // not activated yet
			synchronized (pendingServices) {
				ctx = this.ctx;
				if (ctx == null) {
					pendingServices.add(configRef);
					return;
				}
			}
		}
		addHeaderConfigInternal(configRef);
	}
	
	protected void removeHeader(ServiceReference<HttpConfigService> configRef) {
		final String bsn = getBundleSymbolicName(configRef);
		if (bsn == null) {
			pendingServices.remove(configRef);
			return;
		}
		synchronized (headerConfigs) {
			if (configs.remove(bsn, configRef)) {
				final ConfigSupplierImpl impl = headerConfigs.get(bsn); // do not remove; these continue to be in use by OgemaHttpContext
				if (impl != null) {
					impl.setConfig(HttpConfigImpl.DEFAULT_CONFIG);
				}
			}
		}
	}

	// must be called only after ctx has been set
	private void addHeaderConfigInternal(final ServiceReference<HttpConfigService> configRef) {
		final String bsn = getBundleSymbolicName(configRef);
		if (bsn == null)
			return;
		final ConfigSupplierImpl impl;
		synchronized (headerConfigs) {
			final ServiceReference<?> old = configs.put(bsn, configRef);
			if (old != null)
				LoggerFactory.getLogger(getClass()).warn("Duplicate header config for {}", bsn);
			impl = headerConfigs.get(bsn);
		}
		if (impl != null)
			impl.setConfig(convert(configRef));
	}
	
	
	@Activate
	protected void activate(BundleContext ctx) {
		synchronized (pendingServices) {
			this.ctx = ctx;
			for (ServiceReference<HttpConfigService> configRef : pendingServices) {
				addHeaderConfigInternal(configRef);
			}
			pendingServices.clear();
		}
	}
	
	@Override
	public HttpConfig getConfig(Bundle b) {
		return getSupplier(b).getConfiguration();
	}
	
	private ConfigSupplierImpl getSupplier(Bundle b) {
		Objects.requireNonNull(b);
		final String bsn = b.getSymbolicName();
		if (bsn == null)
			return new ConfigSupplierImpl(b);
		ConfigSupplierImpl impl = headerConfigs.get(bsn);
		if (impl == null) {
			synchronized (headerConfigs) {
				impl = headerConfigs.get(bsn);
				if (impl == null) {
					final ServiceReference<HttpConfigService> ref = configs.get(bsn);
					final HttpConfigImpl config = convert(ref);
					impl = new ConfigSupplierImpl(b, config);
					headerConfigs.put(bsn, impl);
				}
			}
		}
		return impl;
	}

	// may return null
	private String getBundleSymbolicName(final ServiceReference<HttpConfigService> configRef) {
		if (configRef == null)
			return null;
		try {
			final HttpBundleConfiguration config = ctx.getService(configRef).getConfiguration();
			try {
				final String bsn = config.bundleSymbolicName();
				return bsn.isEmpty() ? null : bsn;
			} finally {
				ctx.ungetService(configRef);
			}
		} catch (IllegalStateException | NullPointerException e) {
			return null;
		}
	}
	
	private HttpConfigImpl convert(final ServiceReference<HttpConfigService> configRef) {
		if (configRef == null)
			return HttpConfigImpl.DEFAULT_CONFIG;
		try {
			final HttpBundleConfiguration config = ctx.getService(configRef).getConfiguration();
			try {
				return new HttpConfigImpl(
						config.corsAllowedOrigin(), 
						config.corsAllowCredentials(), 
						config.corsAllowHeaders(),
						config.staticRedirects(),
						config.staticUriRedirects(),
						config.header());
			} finally {
				ctx.ungetService(configRef);
			}
		} catch (IllegalStateException | NullPointerException e) {
			// FIXME
			e.printStackTrace();
			return HttpConfigImpl.DEFAULT_CONFIG;
		}
	}
	
}
