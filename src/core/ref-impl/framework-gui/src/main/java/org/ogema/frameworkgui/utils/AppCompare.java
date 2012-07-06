/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ogema.frameworkgui.utils;

import java.util.Comparator;
import org.ogema.frameworkgui.json.AppsJsonGet;

/**
 *
 * @author tgries
 */
public class AppCompare implements Comparator<AppsJsonGet> {

	@Override
	public int compare(AppsJsonGet o1, AppsJsonGet o2) {
		return o1.getName().compareToIgnoreCase(o2.getName());
	}

}
