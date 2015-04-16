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
package org.ogema.persistence.impl.mem;

import java.util.List;

import org.ogema.core.model.Resource;
import org.ogema.resourcetree.SimpleResourceData;
import org.ogema.resourcetree.TreeElement;

/**
 * TreeElement that is only a reference to another TreeElement. A reference element has its own ID and name and its
 * parent is the TreeElement containing the reference (not the TreeElement containing {@link #getReference()}!).
 * 
 * @author jlapp
 */
public class ReferenceElement extends MemoryTreeElement {

	private volatile TreeElement delegate;
	private final boolean decorating;

	public ReferenceElement(TreeElement reference, String name, TreeElement parent, boolean decorating) {
		super(name, reference.getType(), parent);
		this.delegate = reference;
		this.decorating = decorating;
	}

	void setReference(TreeElement ref) {
		delegate = ref;
	}

	@Override
	public String getAppID() {
		return delegate.getAppID();
	}

	@Override
	public void setAppID(String appID) {
		delegate.setAppID(appID);
	}

	@Override
	public Object getResRef() {
		return delegate.getResRef();
	}

	@Override
	public void setResRef(Object resRef) {
		delegate.setResRef(resRef);
	}

	@Override
	public boolean isActive() {
		return delegate.isActive();
	}

	@Override
	public void setActive(boolean active) {
		delegate.setActive(active);
	}

	@Override
	public TreeElement getParent() {
		return super.getParent();
	}

	@Override
	public int getResID() {
		return super.getResID();
	}

	@Override
	public int getTypeKey() {
		return delegate.getTypeKey();
	}

	@Override
	public String getName() {
		return super.getName();
	}

	@Override
	public Class<? extends Resource> getType() {
		return delegate.getType();
	}

	@Override
	public boolean isNonpersistent() {
		return delegate.isNonpersistent();
	}

	@Override
	public boolean isDecorator() {
		return decorating;
	}

	@Override
	public boolean isToplevel() {
		return false;
	}

	@Override
	public boolean isReference() {
		return true;
	}

	@Override
	public TreeElement getReference() {
		return delegate;
	}

	@Override
	public TreeElement addChild(String name, Class<? extends Resource> type, boolean isDecorating) {
		return delegate.addChild(name, type, isDecorating);
	}

	@Override
	public TreeElement addReference(TreeElement ref, String name, boolean isDecorating) {
		return delegate.addReference(ref, name, isDecorating);
	}

	@Override
	public List<TreeElement> getChildren() {
		return delegate.getChildren();
	}

	@Override
	public TreeElement getChild(String childName) {
		return delegate.getChild(childName);
	}

	@Override
	public SimpleResourceData getData() {
		return delegate.getData();
	}

}
