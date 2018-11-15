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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ogema.frameworkgui.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tgries
 */
public class AppsJsonGet implements Serializable {

	private static final long serialVersionUID = 2201446440921362926L;

	private String name;
	private long id;
	private Map<String, String> metainfo;
	private boolean hasWebResources;
	private List<String> webResourcePaths = new ArrayList<String>();

	public AppsJsonGet() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Map<String, String> getMetainfo() {
		return metainfo;
	}

	public void setMetainfo(Map<String, String> metainfo) {
		this.metainfo = metainfo;
	}

	public List<String> getWebResourcePaths() {
		return webResourcePaths;
	}

	public void setWebResourcePaths(List<String> webResourcePaths) {
		this.webResourcePaths = webResourcePaths;
	}

	public boolean isHasWebResources() {
		return hasWebResources;
	}

	public void setHasWebResources(boolean hasWebResources) {
		this.hasWebResources = hasWebResources;
	}

}
