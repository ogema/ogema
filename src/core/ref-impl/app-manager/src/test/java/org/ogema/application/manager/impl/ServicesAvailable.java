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
package org.ogema.application.manager.impl;

import org.junit.Test;

import static org.junit.Assert.*;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.hardwaremanager.HardwareManager;

import org.ogema.core.security.WebAccessManager;
import org.ogema.exam.OsgiAppTestBase;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * Checks that the application manager returns the references to the required services, not null.
 */
@ExamReactorStrategy(PerClass.class)
public class ServicesAvailable extends OsgiAppTestBase {

	@Test
	public void getWebAccessManager() {
		final ApplicationManager appMan = getApplicationManager();
		assertNotNull(appMan);
		final WebAccessManager webAcc = appMan.getWebAccessManager();
		assertNotNull(webAcc);
	}

	@Test
	public void getHardwareManager() {
		final ApplicationManager appMan = getApplicationManager();
		assertNotNull(appMan);
		final HardwareManager hwMan = appMan.getHardwareManager();
		assertNotNull(hwMan);
	}

}
