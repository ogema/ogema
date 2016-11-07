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
package de.iwes.experimental.ogema.console.scripting;

import groovy.lang.Binding;
import groovy.ui.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.ogema.core.application.ApplicationManager;

/**
 * Starts a Groovy script engine that is accessible via the equinox OSGi console. Type {@code 'help'} on the OSGi
 * console for a list off new commands, generally command added by this component start with {@code 'ogs'}. <br/>
 * The following bindings are available inside the script engine:
 * <dl>
 * <dt>bundle</dt>
 * <dd>this bundle</dd>
 * <dt>ctx</dt>
 * <dd>this bundle's context</dd>
 * <dt>manager</dt>
 * <dd>the {@link ApplicationManager} for the ScriptCommandsEquinox application</dd>
 * </dl>
 *
 * When started, the script engine will evaluate the bundle resource {@code /initscripts/Groovy.groovy}, which may
 * contain additional method definitions.
 *
 */
@Component(specVersion = "1.2", immediate = true, enabled = false)
@Service(CommandProvider.class)
public class ScriptCommandsEquinox extends ScriptCommandsGoGo implements CommandProvider {

	@Override
	public String getHelp() {
		return "---OGEMA scripting---\n" + "\togs <string> - evaluate string in script engine\n"
				+ "\togs_echo - toggle input echo on/off\n" + "\togs_run <filename> - run file in script engine\n"
				+ "\togs_init <enginename> - initialize script engine (javascript or Groovy available)\n"
				+ "\togs_history - list all previous input of ogs command\n"
				+ "\togs_console - display the Groovy console window\n";
	}

	public void _ogs(CommandInterpreter ci) {
		StringBuilder line = new StringBuilder("");
		String arg;
		while ((arg = ci.nextArgument()) != null) {
			line.append(arg).append(" ");
		}
		try {
			if (echo) {
				ci.println(line.toString());
			}
			history.add(line.toString());
			Object o = engine.eval(line.toString());
			if (o != null) {
				ci.print("= ");
				ci.println(o);
			}
		} catch (ScriptException se) {
			ci.printStackTrace(se);
		}
	}

	public void _ogs_history(CommandInterpreter ci) {
		for (String line : history) {
			ci.println(line);
		}
	}

	public void _ogs_echo(CommandInterpreter ci) {
		echo = !echo;
		ci.println(echo ? "echo on" : "echo off");
	}

	public void _ogs_init(CommandInterpreter ci) {
		String arg = ci.nextArgument();
		if (arg == null) {
			ci.println(getHelp());
			return;
		}
		initEngine(arg);
	}

	// runs a script file from the file system or bundle resources (class loader)
	public void _ogs_run(CommandInterpreter ci) {
		String f = ci.nextArgument();
		if (f == null) {
			ci.println(getHelp());
			return;
		}
		File file = new File(f);
		InputStream is = null;
		try {
			if (file.exists()) {
				try {
					evalIs(is = new FileInputStream(file), ci);
				} catch (FileNotFoundException fe) {
					ci.println(fe.getMessage());
				}
			}
			else {
				is = getClass().getResourceAsStream(f);
				if (is == null) {
					ci.println("not found: " + f);
				}
				else {
					evalIs(is, ci);
				}
			}
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ex) {
					// do not care
				}
			}
		}
	}

	public void _ogs_console(CommandInterpreter ci) {
		Console c = new Console(getClass().getClassLoader(),
				new Binding(engine.getBindings(ScriptContext.ENGINE_SCOPE)));
		c.run();
	}

	protected void evalIs(InputStream is, CommandInterpreter ci) {
		InputStreamReader reader = new InputStreamReader(is);
		try {
			engine.eval(reader);
		} catch (ScriptException se) {
			ci.println(se);
		}
	}

}
