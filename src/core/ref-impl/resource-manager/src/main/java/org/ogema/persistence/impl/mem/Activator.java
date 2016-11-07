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
