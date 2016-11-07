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
