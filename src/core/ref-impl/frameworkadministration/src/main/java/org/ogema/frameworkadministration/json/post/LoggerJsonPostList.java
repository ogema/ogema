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
package org.ogema.frameworkadministration.json.post;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tgries
 */
public class LoggerJsonPostList implements Serializable {

	private static final long serialVersionUID = 6362506114491145570L;

	private String action;
	private List<LoggerJsonPost> elements = new ArrayList<LoggerJsonPost>();

	public LoggerJsonPostList(String action) {
		this.action = action;
	}

	public LoggerJsonPostList() {
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public List<LoggerJsonPost> getElements() {
		return elements;
	}

	public void setElements(List<LoggerJsonPost> elements) {
		this.elements = elements;
	}

	@Override
	public String toString() {
		return "LoggerJsonPostList{" + "action=" + action + ", elements=" + elements + '}';
	}

}
