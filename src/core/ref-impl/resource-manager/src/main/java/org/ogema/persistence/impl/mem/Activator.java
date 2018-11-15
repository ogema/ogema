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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ogema.persistence.impl.mem;

import java.util.Dictionary;
import java.util.Hashtable;
import org.ogema.persistence.ResourceDB;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author jlapp
 */
public class Activator implements BundleActivator {

	/**
	 * System property ({@value} ) that must be set to 'true' to enable the MemoryResourceDB service.
	 */
	public static final String ACTIVATE_MEM_DB = "org.ogema.persistence.memoryresourcedb.activate";

	private ServiceRegistration<ResourceDB> reg;

	@Override
	public synchronized void start(BundleContext bc) throws Exception {
		if (Boolean.getBoolean(ACTIVATE_MEM_DB)) {
			MemoryResourceDB db = new MemoryResourceDB();
			Dictionary<String, Object> serviceProps = new Hashtable<>();
			serviceProps.put(Constants.SERVICE_RANKING, Integer.MIN_VALUE);
			reg = bc.registerService(ResourceDB.class, db, serviceProps);
            LoggerFactory.getLogger(getClass()).info("activated in memory non-persistent resource DB");
		}
	}

	@Override
	public synchronized void stop(BundleContext bc) throws Exception {
		if (reg != null) {
			reg.unregister();
		}
		reg = null;
	}

}
