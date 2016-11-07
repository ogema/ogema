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
package org.ogema.tests.dumydriver.hld;

import java.util.Hashtable;

import org.osgi.framework.BundleContext;

public class ShellCommands {
	private DummyHighLevelDriver app;

	public ShellCommands(BundleContext context, DummyHighLevelDriver dhld) {
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put("osgi.command.scope", "dummy");
		props.put("osgi.command.function", new String[] { "step" });
		context.registerService(this.getClass().getName(), this, props);
		this.app = dhld;
	}

	public String step(int add) {
		return app.step(add);
	}
}
