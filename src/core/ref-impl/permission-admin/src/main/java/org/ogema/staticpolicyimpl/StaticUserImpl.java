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
package org.ogema.staticpolicyimpl;

import org.ogema.staticpolicy.StaticUser;

public class StaticUserImpl implements StaticUser {

	private String name;
	private String group;
	private boolean natural;

	StaticUserImpl(String name, String group, boolean natural) {
		this.name = name;
		this.group = group;
		this.natural = natural;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public boolean isNatural() {
		return natural;
	}

}
