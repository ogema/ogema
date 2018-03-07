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
package org.ogema.resourcemanager.tests;


import org.junit.Assert;
import org.junit.Before;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.exam.latest.LatestVersionsTestBase;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;

public abstract class TestBase extends LatestVersionsTestBase {
	
	final static long PERSISTENCE_PERIOD_MS = 100;
	
	public TestBase() {
		super(true);
	}
    
	@Before
	public void deleteResources() throws InterruptedException {
		boolean found = false;
		for (Resource r : getApplicationManager().getResourceAccess().getToplevelResources(Resource.class)) {
			found = true;
			r.delete();
		}
		if (found)
			Thread.sleep(2 * PERSISTENCE_PERIOD_MS);
		Assert.assertTrue(getApplicationManager().getResourceAccess().getResources(Resource.class).isEmpty());
	}
	
	@Configuration
	@Override
	public Option[] config() {
		Option[] sup = super.config();
		Option[] newOpt = new Option[sup.length+4];
		System.arraycopy(sup, 0, newOpt, 0, sup.length);
		newOpt[sup.length] = CoreOptions.mavenBundle("org.ops4j.pax.tinybundles", "tinybundles", "3.0.0");
		newOpt[sup.length+1] = CoreOptions.mavenBundle("biz.aQute.bnd", "biz.aQute.bndlib", "3.5.0");
		newOpt[sup.length+2] = CoreOptions.systemProperty("org.ogema.persistence").value("active");
		newOpt[sup.length+3] = CoreOptions.systemProperty("org.ogema.timedpersistence.period").value(String.valueOf(PERSISTENCE_PERIOD_MS));
		return newOpt;
	}
	
	// required after a restart of the test app
	final ApplicationManager waitForAppManager() throws InterruptedException {
		for (int i=0;i<100;i++) {
			final ApplicationManager appMan = getApplicationManager();
			if (appMan != null)
				return appMan;
			Thread.sleep(50);
		}
		Assert.fail("Gave up waiting for app manager");
		return null;
	}

}
