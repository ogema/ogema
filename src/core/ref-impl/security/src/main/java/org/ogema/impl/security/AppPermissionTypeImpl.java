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
package org.ogema.impl.security;

import java.util.HashSet;

import org.ogema.core.security.AppPermissionType;
import org.osgi.service.condpermadmin.ConditionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.permissionadmin.PermissionInfo;

public class AppPermissionTypeImpl implements AppPermissionType, Cloneable {

	@SuppressWarnings("unused")
	private static final String BUNDLE_LOCATION_CONDITION_NAME = "org.osgi.service.condpermadmin.BundleLocationCondition";

	HashSet<PermissionInfo> perms;
	HashSet<ConditionInfo> conds;
	/*
	 * mode is set by the creator of PermissionType in this case by AppPermission. Thats why this is not gives as
	 * argument neither in constructor nor in the register methods.
	 */
	boolean mode;
	String name;
	ConditionalPermissionInfo pInfo;
	private ConditionalPermissionAdmin cpa;
	boolean inited;

	AppPermissionTypeImpl(AppPermissionImpl appPerm) {
		this.cpa = appPerm.cpa;
		this.perms = new HashSet<>();
		this.conds = new HashSet<>();
		inited = false;
	}

	public AppPermissionTypeImpl() {
		this.perms = new HashSet<>();
		this.conds = new HashSet<>();
		inited = false;
	}

	public void registerPermission(String permName, String filter, String actions, ConditionInfo cond) {
		perms.add(new PermissionInfo(permName, filter, actions));
		if (cond != null)
			conds.add(cond);

		// If ConditionalPermissionInfo is already set, it is to be invalidate
		// here.
		pInfo = null;
	}

	public void registerPermission(PermissionInfo pi, ConditionInfo cond) {
		perms.add(pi);
		if (cond != null)
			conds.add(cond);

		// If ConditionalPermissionInfo is already set, it is to be invalidate
		// here.
		pInfo = null;
	}

	public void registerPermission(PermissionInfo[] pia, ConditionInfo[] cia) {
		for (PermissionInfo pi : pia) {
			perms.add(pi);
		}
		for (ConditionInfo ci : cia) {
			if (!ci.getType().equals(AppPermissionImpl.BUNDLE_LOCATION_CONDITION_NAME))
				conds.add(ci);
		}
	}

	@Override
	public String getDeclarationString() {
		// Check if this type was already applied. In this case the declaration
		// can be obtained by CPA.
		if (pInfo != null)
			return pInfo.getEncoded();

		ConditionInfo conds[] = new ConditionInfo[this.conds.size()];
		PermissionInfo perms[] = new PermissionInfo[this.perms.size()];
		String access;
		if (mode)
			access = ConditionalPermissionInfo.ALLOW;
		else
			access = ConditionalPermissionInfo.DENY;

		ConditionalPermissionInfo cpi = cpa.newConditionalPermissionInfo(name, this.conds.toArray(conds), (this.perms
				.toArray(perms)), access);
		pInfo = cpi;
		return pInfo.getEncoded();
	}

	@Override
	public ConditionalPermissionInfo getDeclarationInfo() {
		// Check if this type was already applied. In this case the declaration
		// can be obtained by CPA.
		if (pInfo != null)
			return pInfo;

		ConditionInfo conds[] = new ConditionInfo[this.conds.size()];
		PermissionInfo perms[] = new PermissionInfo[this.perms.size()];
		String access;
		if (mode)
			access = ConditionalPermissionInfo.ALLOW;
		else
			access = ConditionalPermissionInfo.DENY;

		ConditionalPermissionInfo cpi = cpa.newConditionalPermissionInfo(name, this.conds.toArray(conds), (this.perms
				.toArray(perms)), access);
		pInfo = cpi;
		return pInfo;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getRiskDescription(String language) {
		return null;
	}

	@Override
	public boolean getPermissionMode() {
		return mode;
	}

	@Override
	public void addCondition(ConditionInfo info) {
		conds.add(info);
		// If ConditionalPermissionInfo is already set, it is to be invalidate
		// here.
		pInfo = null;
	}

	void postInit(AppPermissionImpl appp) {
		if (!inited) {
			this.name = appp.newTypeName(hashCode());
			this.cpa = appp.cpa;

			if (appp.blcInfo != null)
				this.conds.add(appp.blcInfo);

			inited = true;
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
