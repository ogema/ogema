/**
 * Copyright 2009 - 2014
 *
 * Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Fraunhofer IIS
 * Fraunhofer ISE
 * Fraunhofer IWES
 *
 * All Rights reserved
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ogema.webresourcemanager.impl.internal.layout;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author skarge
 */
public class SubnaviagationEntryExternal implements Serializable {

	private final String alias;
	private final String name;
	public static final long serialVersionUID = 4846814157687654635L;

	public SubnaviagationEntryExternal(String alias, String name) {
		this.alias = alias;
		this.name = name;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 89 * hash + Objects.hashCode(this.alias);
		hash = 89 * hash + Objects.hashCode(this.name);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final SubnaviagationEntryExternal other = (SubnaviagationEntryExternal) obj;
		if (!Objects.equals(this.alias, other.alias)) {
			return false;
		}
		if (!Objects.equals(this.name, other.name)) {
			return false;
		}
		return true;
	}

	public String getAlias() {
		return alias;
	}

	public String getName() {
		return name;
	}

}
