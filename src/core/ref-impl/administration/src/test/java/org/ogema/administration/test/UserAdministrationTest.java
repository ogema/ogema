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
package org.ogema.administration.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.administration.UserAccount;
import org.ogema.exam.OsgiAppTestBase;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class)
public class UserAdministrationTest extends OsgiAppTestBase {
	
	@Test
	public void getUsersWorks() {
		final List<UserAccount> users = getApplicationManager().getAdministrationManager().getAllUsers();
		Assert.assertFalse("No users found", users.isEmpty());
		for (UserAccount user:  users) {
			Assert.assertNotNull("User name is null",user.getName());
			final UserAccount user2 = getApplicationManager().getAdministrationManager().getUser(user.getName());
			Assert.assertNotNull("User not found by its name",user2);
			Assert.assertEquals(user.getName(), user2.getName());
		}
	}

}
