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
