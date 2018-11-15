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
package org.ogema.tools.activation.impl;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ValueResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.transaction.ResourceTransaction;
import org.ogema.tools.resource.visitor.ResourceVisitor;

/**
 * Adds non-value resources to a transaction, to be finally activated or deactivated
 * ValueResources are ignored
 */
public class ActivationVisitor implements ResourceVisitor {

	private final ResourceTransaction transaction;
	private final boolean activate;

	public ActivationVisitor(ResourceAccess ra, boolean activate) {
		this.activate = activate; 
		transaction = ra.createResourceTransaction();
	}

	@Override
	public void visit(Resource resource) {
		if (resource instanceof ValueResource)
			return;
		if (activate)
			transaction.activate(resource);
		else
			transaction.deactivate(resource);
	}

	public void commit() {
		transaction.commit();
	}

}
