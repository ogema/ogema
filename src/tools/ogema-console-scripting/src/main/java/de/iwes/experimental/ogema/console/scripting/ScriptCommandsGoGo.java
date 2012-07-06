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
package de.iwes.experimental.ogema.console.scripting;

import groovy.lang.Binding;
import groovy.ui.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.service.command.Descriptor;
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;
import org.ogema.core.application.Application;
import org.ogema.core.application.Application.AppStopReason;
import org.ogema.core.application.ApplicationManager;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * <dd>the {@link ApplicationManager} for the ScriptCommands application</dd>
 * </dl>
 *
 * When started, the script engine will evaluate the bundle resource {@code /initscripts/Groovy.groovy}, which may
 * contain additional method definitions.
 *
 */
@Component(specVersion = "1.1", immediate = true, enabled = true)
@Service(Application.class)
@Properties({ @Property(name = "osgi.command.scope", value = "ogs"),
		@Property(name = "osgi.command.function", value = { "ogs", "run", "console", "init", "env" }) })
public class ScriptCommandsGoGo implements Application {

	@Reference
	protected org.apache.felix.service.command.CommandProcessor cp;

	protected ScriptEngineManager manager;
	protected ScriptEngine engine;
	protected boolean echo = false;
	protected List<String> history = new ArrayList<>();
	protected ComponentContext ctx;

	protected ApplicationManager appMan;
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Descriptor("evaluate string in script engine")
	public Object ogs(String[] input) throws ScriptException {
		StringBuilder sb = new StringBuilder();
		if (input.length > 0) {
			sb.append(input[0]);
			for (int i = 1; i < input.length; i++) {
				sb.append(" ").append(input[i]);
			}
		}
		else {
			System.out.println("no input");
			return null;
		}
		String line = sb.toString();
		return engine.eval(line);
	}

	@Descriptor("evaluate (run) file in script engine")
	public void run(String f) {
		File file = new File(f);
		InputStream is = null;
		try {
			if (file.exists()) {
				try {
					evalIs(is = new FileInputStream(file));
				} catch (FileNotFoundException fe) {
					System.out.println(fe.getMessage());
				}
			}
			else {
				is = getClass().getResourceAsStream(f);
				if (is == null) {
					System.out.println("not found: " + f);
				}
				else {
					evalIs(is);
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

	@Descriptor("initialize a new script engine (e.g. 'javascript' or 'Groovy')")
	public void init(String engineName) {
		initEngine(engineName);
	}

	@Descriptor("display Groovy console window")
	public void console() {
		Console c = new Console(getClass().getClassLoader(),
				new Binding(engine.getBindings(ScriptContext.ENGINE_SCOPE)));
		c.run();
	}

	public void env() {
		System.out.println("---GLOBAL---");
		printBindings(engine.getBindings(ScriptContext.GLOBAL_SCOPE));
		System.out.println("---ENGINE---");
		printBindings(engine.getBindings(ScriptContext.ENGINE_SCOPE));
	}

	protected void printBindings(Bindings b) {
		if (b == null) {
			return;
		}
		for (Map.Entry<String, Object> entry : b.entrySet()) {
			String string = entry.getKey();
			Object object = entry.getValue();
			System.out.printf("%s = [%s]: %s%n", string, object.getClass().getSimpleName(), object);
		}
	}

	protected void evalIs(InputStream is) {
		InputStreamReader reader = new InputStreamReader(is);
		try {
			engine.eval(reader);
		} catch (ScriptException se) {
			System.out.println(se);
		}
	}

	protected void activate(ComponentContext ctx, Map<String, Object> config) {
		this.ctx = ctx;
		manager = new ScriptEngineManager();
		ScriptEngineFactory gfac = new GroovyScriptEngineFactory();
		manager.registerEngineName("Groovy", gfac);
	}

	protected void initEngine(String scriptname) {
		engine = getEnginePrivileged(scriptname);
		if (engine == null) {
			logger.warn("could not get script engine {} from manager, using Groovy", scriptname);
			engine = new GroovyScriptEngineFactory().getScriptEngine();
		}
		engine.put("ctx", ctx.getBundleContext());
		engine.put("bundle", ctx.getBundleContext().getBundle());

		for (String ext : engine.getFactory().getExtensions()) {
			String name = "/initscripts/" + scriptname + "." + ext;
			InputStream is = getClass().getResourceAsStream(name);
			if (is != null) {
				try {
					engine.eval(new InputStreamReader(is));
					logger.debug("sourced {}", name);
				} catch (ScriptException ex) {
					logger.error("error in init script", ex);
				}
			}
			else {
				logger.debug("no init script ({})", name);
			}
		}
	}

	protected void deactivate(ComponentContext ctx, Map<String, Object> config) {
	}
    
    private ScriptEngine getEnginePrivileged(final String engineName){
        return AccessController.doPrivileged(new PrivilegedAction<ScriptEngine>() {

            @Override
            public ScriptEngine run() {
                return manager.getEngineByName(engineName);
            }
        });
    }

	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
		initEngine("Groovy");
		engine.put("manager", appManager);
	}

	@Override
	public void stop(AppStopReason whatever) {
	}

}
