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
package org.ogema.resourcemanager.impl.transaction;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceOperationException;

public class DefaultResourceOperationException extends ResourceOperationException {

	private static final long serialVersionUID = 1L;
	private final AtomicAction action;
	
	public DefaultResourceOperationException(AtomicAction action, Exception e) {
		super("Resource operation failed on resource " + action.getSource(), e);
		this.action = action;
	}

	@Override
	public Type getOperationType() {
		return action.getType();
	}

	@Override
	public Resource getSource() {
		return action.getSource();
	}

}
