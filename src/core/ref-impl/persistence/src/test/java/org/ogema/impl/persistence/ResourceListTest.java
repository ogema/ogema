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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
//import org.ogema.appstore.util.models.AppstoreDataCollection;
//import org.ogema.appstore.util.models.UserInStore;
import org.ogema.core.model.ResourceList;
import org.ogema.impl.persistence.testmodels.DataCollection;
import org.ogema.impl.persistence.testmodels.User;
import org.ogema.persistence.DBConstants;

import junit.framework.TestCase;

@RunWith(Parameterized.class)
public class ResourceListTest {

	@Parameterized.Parameters
	public static Collection<Object[]> params() {
		return Arrays.asList(new Object[][] { { 1, "resList" } });
	}

	private static int executionOrder;
	private static String path;

	public ResourceListTest(int count, String name) {
		executionOrder = count;
		path = name;
	}

	static ResourceDBImpl db;

	@BeforeClass
	public static void init() {
	}

	@Before
	public void before() throws InterruptedException {
		System.setProperty("org.ogema.persistence", "active");
		System.setProperty(DBConstants.DB_PATH_PROP, "./src/test/resources/" + path);
		System.setProperty(DBConstants.PROP_NAME_PERSISTENCE_COMPACTION_START_SIZE, "100");
		System.setProperty(DBConstants.PROP_NAME_TIMEDPERSISTENCE_PERIOD, "1000000");
		removeFiles(executionOrder, path);
		if (executionOrder != 1)
			copyFiles(executionOrder, path);
		db = new ResourceDBImpl();
		db.setName("BootupTest");
		db.restart();

	}

	private void copyFiles(int order, String path) {
		File dir = new File("./src/test/resources/test" + order);
		Path targetDir = FileSystems.getDefault().getPath("./src", "test", "resources", path);
		try {
			Files.createDirectories(targetDir);
			String files[] = dir.list();
			for (String file : files) {
				Path source = FileSystems.getDefault().getPath("./src", "test", "resources", "test" + order, file);
				if (!Files.isDirectory(source, LinkOption.NOFOLLOW_LINKS)) {
					Path targetFile = FileSystems.getDefault().getPath("./src", "test", "resources", path, file);
					Files.copy(source, targetFile, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void removeFiles(int order, String path) {
		File dir = new File("./src/test/resources/" + path);
		if (!dir.exists())
			dir.mkdir();
		String files[] = dir.list();
		for (String file : files) {
			new File(dir, file).delete();
		}
	}

	@After
	public void after() throws InterruptedException {

	}

	@Test
	public void checkPersistenceFilesAfterRestart() {
		String top = "AppstoreDataCollection";
		String list = "users";
		String s0 = "users_0";

		String s1 = "users_1";

		String s2 = "users_2";

		String s3 = "users_3";

		String s4 = "users_4";

		String s5 = "users_5";

		TreeElementImpl topNode = (TreeElementImpl) db.addResource(top, DataCollection.class,
				"persistence_test_resourceListTest");
		TreeElementImpl users = (TreeElementImpl) topNode.addChild(list, ResourceList.class, false);
		users.setResourceListType(User.class);
		users.addChild(s0, User.class, false);
		users.addChild(s1, User.class, false);

		db.deleteResource(topNode);

		topNode = (TreeElementImpl) db.addResource(top, DataCollection.class, "persistence_test_resourceListTest");
		users = (TreeElementImpl) topNode.addChild(list, ResourceList.class, false);
		users.setResourceListType(User.class);
		users.addChild(s0, User.class, false);
		users.addChild(s1, User.class, false);
		users.addChild(s2, User.class, false);
		users.addChild(s3, User.class, false);
		users.addChild(s4, User.class, false);
		users.addChild(s5, User.class, false);
		db.doStorage();
		db.stopStorage();
		db.restart();

		TreeElementImpl tmptop = (TreeElementImpl) db.getToplevelResource(top);
		Assert.assertNotNull(tmptop);

		tmptop = (TreeElementImpl) tmptop.getChild(list);
		Assert.assertNotNull(tmptop);

		TreeElementImpl tmp = (TreeElementImpl) tmptop.getChild(s0);
		Assert.assertNotNull(tmp);

		tmp = (TreeElementImpl) tmptop.getChild(s1);
		Assert.assertNotNull(tmp);

		tmp = (TreeElementImpl) tmptop.getChild(s2);
		Assert.assertNotNull(tmp);

		tmp = (TreeElementImpl) tmptop.getChild(s3);
		Assert.assertNotNull(tmp);

		tmp = (TreeElementImpl) tmptop.getChild(s4);
		Assert.assertNotNull(tmp);

		tmp = (TreeElementImpl) tmptop.getChild(s5);
		Assert.assertNotNull(tmp);
	}
}
