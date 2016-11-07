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
package org.ogema.impl.persistence;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.util.Arrays;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogema.core.model.array.BooleanArrayResource;
import org.ogema.core.model.array.FloatArrayResource;
import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.array.TimeArrayResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.persistence.DBConstants;
import org.ogema.persistence.ResourceDB;
import org.ogema.resourcetree.TreeElement;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.osgi.framework.BundleContext;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
// @ExamReactorStrategy(EagerSingleStagedReactorFactory.class)
public class ResourceIOTest {

	String ogemaVersion = MavenUtils.asInProject().getVersion("org.ogema.core", "api");

	@Inject
	BundleContext ctx;
	@Inject
	ResourceDB resdb;

	static String text = "Java 2 security uses several policy files to determine the granted permission for each Java program. See Dynamic policy for the list of available policy files. The Java Development Kit provides policytool to edit these policy files. This tool is recommended for editing any policy file to verify the syntax of its contents. Syntax errors in the policy file cause an AccessControlException during application execution, including the server start. Identifying the cause of this exception is not easy because the user might not be familiar with the resource that has an access violation. Be careful when you edit these policy files.";

	@Test
	public void test1Files() {

		resdb.addOrUpdateResourceType(IntegerArrayResource.class);
		resdb.addOrUpdateResourceType(FloatArrayResource.class);
		resdb.addOrUpdateResourceType(StringArrayResource.class);
		resdb.addOrUpdateResourceType(TimeArrayResource.class);
		resdb.addOrUpdateResourceType(BooleanArrayResource.class);
		resdb.addOrUpdateResourceType(StringResource.class);

		// IntegerArrayResource iar = resdb.createResource("testArray",
		// IntegerArrayResource.class);
		int iarr[] = { 1, 11, 21, 1112, 3112, 211213 };
		TreeElement iar = null;
		if (!resdb.hasResource("testArray")) {
			iar = resdb.addResource("testArray", IntegerArrayResource.class, "app1");
			// iar.setValues(iarr);
			iar.getData().setIntArr(iarr);
		}
		else {
			iar = resdb.getToplevelResource("testArray");
			TestCase.assertTrue(Arrays.equals(iarr, iar.getData().getIntArr()));
		}
		// FloatArrayResource far = resdb.createResource("testFloatArray",
		// FloatArrayResource.class);
		// float farr[] = { 1, 11, 21, 1112, 3112, 211213 };
		// far.setValues(farr);
		float farr[] = { 1, 11, 21, 1112, 3112, 211213 };
		TreeElement far = null;
		if (!resdb.hasResource("testFloatArray")) {
			far = resdb.addResource("testFloatArray", FloatArrayResource.class, "app2");
			far.getData().setFloatArr(farr);
		}
		else {
			far = resdb.getToplevelResource("testFloatArray");
			TestCase.assertTrue(Arrays.equals(farr, far.getData().getFloatArr()));
		}

		long larr[] = { 1, 11, 21, 1112, 3112, 211213 };
		TreeElement tar = null;
		if (!resdb.hasResource("testTimeArray")) {
			tar = resdb.addResource("testTimeArray", TimeArrayResource.class, "app3");
			tar.getData().setLongArr(larr);
		}
		else {
			tar = resdb.getToplevelResource("testTimeArray");
			TestCase.assertTrue(Arrays.equals(larr, tar.getData().getLongArr()));
		}

		TreeElement zar = null;
		boolean zarr[] = { true, false };
		if (!resdb.hasResource("testBooleanArray")) {
			zar = resdb.addResource("testBooleanArray", BooleanArrayResource.class, "app4");
			zar.getData().setBooleanArr(zarr);
		}
		else {
			zar = resdb.getToplevelResource("testBooleanArray");
			TestCase.assertTrue(Arrays.equals(zarr, zar.getData().getBooleanArr()));
		}

		TreeElement txt = null;
		if (!resdb.hasResource("anyText")) {
			txt = resdb.addResource("anyText", StringResource.class, "anyApp");
			txt.getData().setString(text);

		}
		else {
			txt = resdb.getToplevelResource("anyText");
			TestCase.assertTrue(text.equals(txt.getData().getString()));
		}

		TreeElement sArr = null;
		String sarr[] = { text, null, text + text, text + text + text, null };
		if (!resdb.hasResource("YASA")) {
			sArr = resdb.addResource("YASA", StringArrayResource.class, "anyApp");
			sArr.getData().setStringArr(sarr);

		}
		else {
			sArr = resdb.getToplevelResource("YASA");
			TestCase.assertTrue(Arrays.equals(sarr, sArr.getData().getStringArr()));
		}

		int count = 10;
		while (count-- > 0) {
			txt.getData().setString(text);
			zar.getData().setBooleanArr(zarr);
			tar.getData().setLongArr(larr);
			far.getData().setFloatArr(farr);
			iar.getData().setIntArr(iarr);
			sArr.fireChangeEvent();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	@Before
	public void init() {
		// ResourceDBManager dbman = new ResourceDBManager(resdb, access);
		// ApplicationResourceManager appResMan = new
		// ApplicationResourceManager(appMan, app, dbman, access);
		// resMan = appResMan;
		// resAcc = appResMan;
	}

	@Configuration
	public Option[] configure() {
		return options(
				// excludeDefaultRepositories(),
				// repositories("file:.m2/"),
				CoreOptions.systemProperty(DBConstants.PROP_NAME_PERSISTENCE_ACTIVE)
						.value(DBConstants.PROP_VALUE_PERSISTENCE_ACTIVE),
				CoreOptions.systemProperty(DBConstants.DB_PATH_PROP).value("resourceIOTest"),
				CoreOptions.systemProperty(DBConstants.PROP_NAME_TIMEDPERSISTENCE_PERIOD).value("1000"),
				CoreOptions.systemProperty(DBConstants.PROP_NAME_PERSISTENCE_COMPACTION_START_SIZE_GARBAGE).value("64"),
				CoreOptions.systemProperty(DBConstants.PROP_NAME_PERSISTENCE_COMPACTION_START_SIZE).value("512"),
				CoreOptions.systemProperty(DBConstants.PROP_NAME_PERSISTENCE_DEBUG).value("true"), junitBundles(),
				CoreOptions.bundle("reference:file:target/classes/"), // load
				// this
				// bundle
				// directly
				// from
				// maven
				// build
				// dir
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.scr", "1.6.2").start(),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.framework.security", "2.2.0").start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "permission-admin", ogemaVersion).start(),
				CoreOptions.mavenBundle("javax.servlet", "javax.servlet-api", "3.0.1"),
				CoreOptions.mavenBundle("org.slf4j", "slf4j-api", "1.7.2"),
				CoreOptions.mavenBundle("joda-time", "joda-time", "2.2"),
				CoreOptions.mavenBundle("org.json", "json", "20160212"),
                CoreOptions.mavenBundle("com.google.guava", "guava", "19.0"),
				CoreOptions.mavenBundle("org.ogema.core", "models", ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.core", "api", ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "internal-api", ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "ogema-logger", ogemaVersion).start(),
				// CoreOptions.mavenBundle("org.ogema.ref-impl", "app-manager",
				// ogemaVersion).start(),
				// CoreOptions.mavenBundle("org.ogema.ref-impl",
				// "resource-manager", ogemaVersion).start(), // = this bundle
				CoreOptions.mavenBundle("org.ogema.ref-impl", "security", ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "resource-manager", ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "util", ogemaVersion),
				CoreOptions.mavenBundle("org.ogema.tools", "resource-utils", ogemaVersion),
				CoreOptions.mavenBundle("com.fasterxml.jackson.core", "jackson-core", "2.7.4"),
				CoreOptions.mavenBundle("com.fasterxml.jackson.core", "jackson-annotations", "2.7.4"),
				CoreOptions.mavenBundle("com.fasterxml.jackson.core", "jackson-databind", "2.7.4"),
				CoreOptions.mavenBundle("com.fasterxml.jackson.module", "jackson-module-jaxb-annotations", "2.7.4"),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.useradmin.filestore", "1.0.2").start(),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.http.api", "2.3.0").start(),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.useradmin", "1.0.3").start(),
				CoreOptions.mavenBundle("org.ogema.tools", "memory-timeseries").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.http.jetty", "3.0.2").start(),
				CoreOptions.mavenBundle("javax.servlet", "javax.servlet-api", "3.1.0"),
				CoreOptions.mavenBundle("org.eclipse.jetty", "jetty-servlets", "9.2.11.v20150529")
		// this
		// bundle
		// directly
		// from
		// maven
		// build
		// dir
		);

	}

	@Before
	public void before() throws InterruptedException {
	}

	@After
	public void after() throws InterruptedException {
	}

	@BeforeClass
	public static void beforeClass() throws InterruptedException {
		// System.setProperty(DBConstants.DB_PATH_PROP, "resourceIOTest");
	}

	@AfterClass
	public static void afterClass() throws InterruptedException {
	}
}
