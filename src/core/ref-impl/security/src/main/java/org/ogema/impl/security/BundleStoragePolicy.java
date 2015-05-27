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
package org.ogema.impl.security;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.osgi.framework.Bundle;

class BundleStoragePolicy {
	static final int DEFAULT_FS_MEMORY = 1024 * 1024 * 10; // 10M
	int availableMemory;
	private Bundle bundle;

	static HashMap<Integer, BundleStoragePolicy> policies;

	public BundleStoragePolicy(Bundle b) {
		this.bundle = b;
		availableMemory = refresh();
		// create refresher thread
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					Set<Entry<Integer, BundleStoragePolicy>> policySet = policies.entrySet();
					for (Entry<Integer, BundleStoragePolicy> entry : policySet) {
						entry.getValue().refresh();
					}
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
	}

	static BundleStoragePolicy getStoragePolicy(Bundle b) {
		int id = (int) b.getBundleId();
		BundleStoragePolicy bsp = policies.get(id);
		if (bsp == null) {
			bsp = new BundleStoragePolicy(b);
			policies.put(id, bsp);
		}
		return bsp;
	}

	int refresh() {
		// re-calculate the available memory by rescanning bundles storage area
		// and return it.
		return availableMemory;
	}

	int consume(int bytes) {
		availableMemory -= bytes;
		if (availableMemory < 0)
			availableMemory = refresh();
		return availableMemory;
	}

	static {
		policies = new HashMap<Integer, BundleStoragePolicy>();
	}
}
