package org.ogema.tools.activation.impl;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ValueResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.Transaction;
import org.ogema.tools.resource.visitor.ResourceVisitor;

/**
 * Adds non-value resources to a transaction, to be finally activated or deactivated
 * ValueResources are ignored
 */
public class TransactionVisitor implements ResourceVisitor {

	private final Transaction transaction;

	public TransactionVisitor(ResourceAccess ra) {
		transaction = ra.createTransaction();
	}

	@Override
	public void visit(Resource resource) {
		if (resource instanceof ValueResource)
			return;
		transaction.addResource(resource);
	}

	public void activate(boolean activate) {
		if (activate)
			transaction.activate();
		else
			transaction.deactivate();
	}

}
