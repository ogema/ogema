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
package org.ogema.tests.persistence.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceNotFoundException;
import org.ogema.impl.persistence.ResourceDBImpl;
import org.ogema.impl.persistence.TreeElementImpl;
import org.ogema.persistence.ResourceDB;
import org.ogema.resourcetree.TreeElement;
import org.ogema.tests.persistence.PersistencyTestBase;
import org.ogema.tests.persistence.testmodels.DataCollection;
import org.ogema.tests.persistence.testmodels.User;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.startlevel.FrameworkStartLevel;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class PersistenceTest extends PersistencyTestBase implements FrameworkListener {

	// @XXX Test is not applicable in this format. It needs a new framework instance for each testlet.
	public PersistenceTest() {
		super(false);
	}

	@Inject
	protected ResourceDB resDB;

	@Inject
	BundleContext ctx;

	private FrameworkStartLevel fsl;

	private int fl;

	private int oldfl;

	private static String s0;

	private static String s1;

	private static String s2;

	private static String s3;

	private static String s4;

	private String s5;

	@Ignore
	@Test
	public void prepareResourceList() throws InvalidSyntaxException, BundleException, InterruptedException {
		((ResourceDBImpl) resDB).removeFiles();
		// ((ResourceDBImpl) resDB).restart();
		ApplicationManager appMan = getApplicationManager();
		String name = newResourceName();
		ResourceManagement resMgmnt = appMan.getResourceManagement();
		ResourceAccess resAcc = appMan.getResourceAccess();

		ResourceList<StringResource> res = resMgmnt.createResource(name, ResourceList.class);
		res.setElementType(StringResource.class);
		s0 = res.add().getLocation();
		s1 = res.add().getLocation();
		res.delete();
		res = resMgmnt.createResource(name, ResourceList.class);
		res.setElementType(StringResource.class);
		s2 = res.add().getLocation();
		s3 = res.add().getLocation();
		s4 = res.add().getLocation();
		s5 = res.add().getLocation();
	}

	@Ignore
	@Test(expected = ResourceNotFoundException.class)
	public void resourceListdeleteRecreateSurviceRestart() {
		ApplicationManager appMan = getApplicationManager();
		ResourceManagement resMgmnt = appMan.getResourceManagement();
		ResourceAccess resAcc = appMan.getResourceAccess();

		try {
			Thread.sleep((long) (storagePeriod * 1.5));
		} catch (InterruptedException e) {
		}
		Bundle b = ctx.getBundle(0);
		fsl = b.adapt(FrameworkStartLevel.class);
		oldfl = fl = fsl.getStartLevel();
		fsl.setStartLevel(2, this);
		while (fl > 2)
			;
		fsl.setStartLevel(oldfl, this);
		while (fl < oldfl)
			;

		StringResource sr0 = null, sr1 = null, sr2 = null, sr3 = null, sr4 = null, sr5 = null;
		try {
			sr0 = resAcc.getResource(s0);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			sr1 = resAcc.getResource(s1);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			sr2 = resAcc.getResource(s2);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			sr3 = resAcc.getResource(s3);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			sr4 = resAcc.getResource(s4);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			sr5 = resAcc.getResource(s5);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		Assert.assertNull(sr0);
		Assert.assertNull(sr1);
		Assert.assertNotNull(sr2);
		Assert.assertNotNull(sr3);
		Assert.assertNotNull(sr4);
		Assert.assertNotNull(sr5);
	}

	@Override
	public void frameworkEvent(FrameworkEvent event) {
		System.out.println(event.getType());
		System.out.println(fsl.getStartLevel());
		fl = fsl.getStartLevel();
	}

	Object lock = new Object();

	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		System.out.println("TestProbeBuilder gets called");
		probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "*");
		probe.setHeader(Constants.EXPORT_PACKAGE, "org.ogema.tests.persistence.tests");
		probe.setHeader(Constants.EXPORT_PACKAGE, "org.ogema.tests.persistence.testmodels");
		return probe;
	}

	@Ignore
	@Test
	public void writeResourceAfterDeleteWontCausesDBIntegrity() {
		// ((ResourceDBImpl) resDB).removeFiles();
		// ((ResourceDBImpl) resDB).restart();
		final ApplicationManager appMan = getApplicationManager();
		final String name = newResourceName();
		final ResourceManagement resMgmnt = appMan.getResourceManagement();
		final ResourceAccess resAcc = appMan.getResourceAccess();

		DataCollection dc = resAcc.getResource(name);
		if (dc == null)
			dc = resMgmnt.createResource(name, DataCollection.class);
		String dcName = dc.getLocation();
		ResourceList<User> res = dc.users().create();
		res.setElementType(User.class);
		String usersName = res.getLocation();

		s0 = res.add().getLocation();
		s1 = res.add().getLocation();
		((ResourceDBImpl) resDB).doStorage();

		new Thread(new Runnable() {

			@Override
			public void run() {
				ResourceList<User> res = resAcc.getResource(name + "/users");
				s2 = res.add().getLocation();
				synchronized (lock) {
					lock.notify();
				}
				synchronized (lock) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
					}
				}
				res.setElementType(User.class);
				// res.add();
				synchronized (lock) {
					lock.notify();
				}
			}
		}).start();
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
			}
		}
		res.delete();
		synchronized (lock) {
			lock.notify();
		}
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
			}
		}
		res = dc.users().create();
		res.setElementType(User.class);
		s3 = res.add().getLocation();
		((ResourceDBImpl) resDB).doStorage();
		((ResourceDBImpl) resDB).restart();

		TreeElement te = ((ResourceDBImpl) resDB).getToplevelResource(dcName);
		assertNotNull(te);

		te = te.getChild("users");
		assertNotNull(te);

		// TreeElement tmp = te.getChild("users_0");
		// assertNotNull(tmp);
		// tmp = te.getChild("users_1");
		// assertNull(tmp);
		// tmp = te.getChild("users_2");
		// assertNull(tmp);
		// tmp = te.getChild("users_3");
		// assertNull(tmp);
		// tmp = te.getChild("users_4");
		// assertNotNull(tmp);
		// tmp = te.getChild("users_5");
		// assertNotNull(tmp);
	}
}
