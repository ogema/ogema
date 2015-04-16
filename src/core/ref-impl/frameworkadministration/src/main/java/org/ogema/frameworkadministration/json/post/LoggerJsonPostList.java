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
