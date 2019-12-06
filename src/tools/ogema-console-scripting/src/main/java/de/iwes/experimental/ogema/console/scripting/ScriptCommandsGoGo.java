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
package de.iwes.experimental.ogema.console.scripting;

import groovy.lang.Binding;
import groovy.ui.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferencePolicyOption;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.Parameter;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iwes.experimental.ogema.console.scripting.api.ScriptEngingExtension;

/**
 * Starts a Groovy script engine that is accessible via the equinox OSGi console. Type {@code 'help'} on the OSGi
 * console for a list off new commands, generally command added by this component start with {@code 'ogs'}. <br>
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
@Component(specVersion = "1.2", immediate = true, enabled = true)
@Service(ScriptCommandsGoGo.class)
@Properties({ @Property(name = "osgi.command.scope", value = "ogs"),
		@Property(name = "osgi.command.function", value = { "ogs", "run", "console", "init", "env", "listEngines" }) })
@References({
	@Reference(
			cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
			policy=ReferencePolicy.DYNAMIC,
			policyOption=ReferencePolicyOption.GREEDY,
			referenceInterface = ScriptEngineFactory.class, 
			bind = "addFactory", 
			unbind = "removeFactory"
		),
	@Reference(
			cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
			policy=ReferencePolicy.DYNAMIC,
			policyOption=ReferencePolicyOption.GREEDY,
			referenceInterface = ScriptEngingExtension.class, 
			bind = "addExtension", 
			unbind = "removeExtension"
		)
})
public class ScriptCommandsGoGo {

	@Reference
	protected org.apache.felix.service.command.CommandProcessor cp;

	// synchronized on itself
	private final List<ScriptEngingExtension> extensions = new ArrayList<ScriptEngingExtension>(4);
    
	protected volatile ScriptEngineManager manager = new ScriptEngineManager();
	protected volatile ScriptEngine engine;
	protected boolean echo = false;
	protected final List<String> history = new ArrayList<>();
	protected ComponentContext ctx;
	/**
	 * Map engine factory -&gt; service ranking
	 */
	// synchronized on this
    protected final Map<ScriptEngineFactory, Integer> addedFactories = new HashMap<>();
    private List<ServiceReference<ScriptEngineFactory>> pendingFactories;
    
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected void addExtension(ScriptEngingExtension extension) {
		synchronized (extensions) {
			extensions.add(extension);
			if (engine != null)
				extension.newScriptEnging(engine);
		}
	}
	
	protected void removeExtension(ScriptEngingExtension extension) {
		synchronized (extensions) {
			extensions.remove(extension);
			if (engine != null) {
				extension.engineGone(engine);
			}
		}
	}
	
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
    
    private ScriptEngine selectEngine(String filename) {
        ScriptEngine fileEngine = engine;
        int idxExt = filename.lastIndexOf('.');
        String extension = idxExt != -1 && idxExt < filename.length() - 1 ?
                filename.substring(idxExt + 1) : filename;
        if (!fileEngine.getFactory().getExtensions().contains(extension)) {
            fileEngine = manager.getEngineByExtension(extension);
            setupBindings(fileEngine);
        }
        logger.trace("engine selected for file {}: {}", filename, fileEngine.getFactory().getEngineName());
        return fileEngine;
    }

	@Descriptor("evaluate (run) file in script engine")
	public Object run(String filename) {
		File file = new File(filename);
        ScriptEngine fileEngine = selectEngine(filename);
		InputStream is = null;
		try {
			if (file.exists()) {
				try {
					return evalIs(fileEngine, is = new FileInputStream(file));
				} catch (FileNotFoundException fe) {
					System.out.println(fe.getMessage());
				}
			}
			else {
				is = getClass().getResourceAsStream(filename);
				if (is == null) {
					System.out.println("not found: " + filename);
				}
				else {
					return evalIs(fileEngine, is);
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
        return null;
	}

	// TODO handle case that multiple engines for the same language exist
	@Descriptor("initialize a new script engine (e.g. 'javascript' or 'Groovy')")
	public void init(String engineName) {
		synchronized (this) {
			engine = initEngine(engineName);
		}
	}

	@Descriptor("display Groovy console window")
	public void console() {
		Console c = new Console(getClass().getClassLoader(),
				new Binding(engine.getBindings(ScriptContext.ENGINE_SCOPE)));
		c.run();
	}
	
	@Descriptor("List available script engines")
	public Collection<String> listEngines(
			@Parameter(names= {"-f", "--fullname"}, absentValue="false", presentValue="true")
			@Descriptor("Print the full engine name instead of the corresponding language?")
			final boolean fullName
			) {
		final List<String> languages = new ArrayList<>();
		synchronized (this) {
			for (ScriptEngineFactory f : addedFactories.keySet()) {
				languages.add(fullName ? f.toString() : f.getLanguageName());
			}
			for (ScriptEngineFactory f : manager.getEngineFactories()) {
				languages.add(fullName ? f.toString() : f.getLanguageName());
			}
		}
		return languages;
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
			System.out.printf("%s = [%s]: %s%n", string, object == null ? "null" : object.getClass().getSimpleName(), object);
		}
	}

	protected Object evalIs(ScriptEngine engine, InputStream is) {
		InputStreamReader reader = new InputStreamReader(is);
		try {
			return engine.eval(reader);
		} catch (ScriptException se) {
			System.out.println(se);
            return se;
		}
	}
	
	private void addFactoryInternal(ScriptEngineFactory fac, int rank) {
		 logger.debug("adding factory for {}", fac.getLanguageName());
		 synchronized (this) {
	        for (String mimeType : fac.getMimeTypes()) {
	        	boolean ok = true;
	        	for (Map.Entry<ScriptEngineFactory, Integer> existing: addedFactories.entrySet()) {
	        		if (existing.getKey().getMimeTypes().contains(mimeType) && existing.getValue() > rank) {
	        			ok = false;
	        			break;
	        		}
	        	}
	        	if (ok)
	            	manager.registerEngineMimeType(mimeType, fac);
	        }
	        {
	        	final String lang = fac.getLanguageName();
	        	boolean ok = true;
	        	for (Map.Entry<ScriptEngineFactory, Integer> existing: addedFactories.entrySet()) {
	        		if (lang.equals(existing.getKey().getLanguageName()) && existing.getValue() > rank) {
	        			ok = false;
	        			break;
	        		}
	        	}
	        	if (ok)
	        		manager.registerEngineName(lang, fac);
	        }
	        for (String ext : fac.getExtensions()) {
	        	boolean ok = true;
	        	for (Map.Entry<ScriptEngineFactory, Integer> existing: addedFactories.entrySet()) {
	        		if (existing.getKey().getExtensions().contains(ext) && existing.getValue() > rank) {
	        			ok = false;
	        			break;
	        		}
	        	}
	        	if (ok)
	        		manager.registerEngineExtension(ext, fac);
	        }
	        addedFactories.put(fac, rank);
		}
	}
    
    protected void addFactory(ServiceReference<ScriptEngineFactory> facRef) {
    	final ComponentContext ctx;
    	synchronized (this) {
    		ctx = this.ctx;
    		if (ctx == null) {
    			if (pendingFactories == null)
    				pendingFactories = new ArrayList<>(4);
    			pendingFactories.add(facRef);
    			return;
    		}
		}
    	final ScriptEngineFactory fac = ctx.getBundleContext().getService(facRef);
    	if (fac == null)
    		return;
    	Object rankProp = facRef.getProperty(Constants.SERVICE_RANKING);
        final int rank = rankProp instanceof Integer ? ((Integer) rankProp) : 0;
        addFactoryInternal(fac, rank);
    }
    
    protected void removeFactory(ServiceReference<ScriptEngineFactory> facRef) {
    	final ComponentContext ctx = this.ctx;
    	if (ctx == null)
    		return;
   		final ScriptEngineFactory fac = ctx.getBundleContext().getService(facRef);
    	try {
    		synchronized (this) {
	    		if (addedFactories.remove(fac) != null)
	    			ctx.getBundleContext().ungetService(facRef);
	    		if (manager == null)
	    			return;
	    		AccessController.doPrivileged(new PrivilegedAction<Void>() {
	
					@Override
					public Void run() {
						for (String mimeType : fac.getMimeTypes()) {
							try {
				    			if (unregisterMime(mimeType, fac)) {
				    				registerNewMime(mimeType);
				    			}
							} catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
								logger.warn("Error removing script engine by mime type {}",fac,e);
							}
			    	    }
						for (String extension: fac.getExtensions()) {
							try {
				    			if (unregisterExtension(extension, fac)) {
				    				registerNewExtension(extension);
				    			}
							} catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
								logger.warn("Error removing script engine by extension {}",fac,e);
							}
						}
						final String name = fac.getLanguageName();
						try {
			    			if (unregisterName(name, fac)) {
			    				registerNewName(name);
			    			}
						} catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
							logger.warn("Error removing script engine by name {}",fac,e);
						}
						return null;
					}
				});
    		}
    	} finally {
    		ctx.getBundleContext().ungetService(facRef);
    	}
    }
    
    // requires external sync on this
    private void registerNewMime(final String mime) {
		ScriptEngineFactory next = null;
		int rank = Integer.MIN_VALUE;
		for (Map.Entry<ScriptEngineFactory, Integer> entry : addedFactories.entrySet()) {
			if (entry.getKey().getMimeTypes().contains(mime) && entry.getValue() >= rank) {
				rank = entry.getValue();
				next = entry.getKey();
			}
		}
		if (next != null) {
			manager.registerEngineMimeType(mime, next);
		}
    }
    
 // requires external sync on this
    private void registerNewExtension(final String extension) {
		ScriptEngineFactory next = null;
		int rank = Integer.MIN_VALUE;
		for (Map.Entry<ScriptEngineFactory, Integer> entry : addedFactories.entrySet()) {
			if (entry.getKey().getExtensions().contains(extension) && entry.getValue() >= rank) {
				rank = entry.getValue();
				next = entry.getKey();
			}
		}
		if (next != null) {
			manager.registerEngineExtension(extension, next);
		}
    }
    
 // requires external sync on this
    private void registerNewName(final String name) {
		ScriptEngineFactory next = null;
		int rank = Integer.MIN_VALUE;
		for (Map.Entry<ScriptEngineFactory, Integer> entry : addedFactories.entrySet()) {
			if (entry.getKey().getNames().contains(name) && entry.getValue() >= rank) {
				rank = entry.getValue();
				next = entry.getKey();
			}
		}
		if (next != null) {
			manager.registerEngineName(name, next);
		}
    }
    
    // must be called in privileged block
    private boolean unregisterMime(final String mime, final ScriptEngineFactory fac) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	return unregister(mime, fac, "mimeTypeAssociations");
    }
    
    private boolean unregisterExtension(final String extension, final ScriptEngineFactory fac) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	return unregister(extension, fac, "extensionAssociations");
    }
    
    private boolean unregisterName(final String name, final ScriptEngineFactory fac) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	return unregister(name, fac, "nameAssociations");
    }
    
    // requires external sync on this
    private boolean unregister(final String id, final ScriptEngineFactory fac, final String fieldName) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	final Field f = ScriptEngineManager.class.getDeclaredField(fieldName);
    	f.setAccessible(true);
        @SuppressWarnings("unchecked")
    	final Map<String, ScriptEngineFactory> map = (Map<String, ScriptEngineFactory>) f.get(manager);
    	// the maps in the manager class are protected by sync on this
   		if (map.get(id) != fac)
   			return false;
		map.remove(id);
   		return true;
    }

	protected synchronized void activate(ComponentContext ctx, Map<String, Object> config) {
		this.ctx = ctx;
		if (pendingFactories != null) {
			for (ServiceReference<ScriptEngineFactory> facRef : pendingFactories) {
				addFactory(facRef);
			}
			pendingFactories = null;
		}
        try {
            @SuppressWarnings("unchecked")
            Class<ScriptEngineFactory> c = (Class<ScriptEngineFactory>) Class.forName("org.codehaus.groovy.jsr223.GroovyScriptEngineFactory");
            Constructor<ScriptEngineFactory> constr = c.getConstructor((Class<?>[]) null);
            ScriptEngineFactory f = constr.newInstance((Object[]) null);
            addFactoryInternal(f, 0);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            logger.debug("Groovy not available ({})", e.getMessage());
        }
        for (ScriptEngineFactory sef: manager.getEngineFactories()) {
            logger.debug("engine discovered: '{}', {}, {}", sef.getEngineName(), sef.getLanguageName(), sef.getExtensions());
        }
        for (ScriptEngineFactory sef: addedFactories.keySet()) {
            logger.debug("engine added: '{}', {}, {}", sef.getEngineName(), sef.getLanguageName(), sef.getExtensions());
        }
        if (manager.getEngineByName("Groovy") != null) {
            engine = initEngine("Groovy");
        } else {
            engine = initEngine("ECMAScript");
        }
        logger.debug("initialized {} engine", engine.getFactory().getLanguageName());
	}
    
    protected void setupBindings(ScriptEngine e) {
        e.put("ctx", ctx.getBundleContext());
		e.put("bundle", ctx.getBundleContext().getBundle());
		synchronized (extensions) {
			for (ScriptEngingExtension ext : extensions) {
				ext.newScriptEnging(e);
			}
		}
    }

	// requires external sync on this
	protected ScriptEngine initEngine(String scriptname) {
		ScriptEngine newEngine = getEnginePrivileged(scriptname);
		if (newEngine == null) {
			logger.warn("could not get script engine {} from manager, using ECMAScript", scriptname);
			newEngine = manager.getEngineByExtension("js");
		}
		setupBindings(newEngine);

		for (String ext : newEngine.getFactory().getExtensions()) {
			String name = "/initscripts/" + scriptname + "." + ext;
			InputStream is = getClass().getResourceAsStream(name);
			if (is != null) {
				try {
					newEngine.eval(new InputStreamReader(is));
					logger.debug("sourced {}", name);
				} catch (ScriptException ex) {
					logger.error("error in init script", ex);
				}
			}
			else {
				logger.debug("no init script ({})", name);
			}
		}
                return newEngine;
	}

	protected synchronized void deactivate(ComponentContext ctx, Map<String, Object> config) {
		synchronized (extensions) {
			for (ScriptEngingExtension ext: extensions) {
				ext.engineGone(engine);
			}
		}
		engine = null;
		addedFactories.clear();
		history.clear();
		manager = null;
	}
    
    private ScriptEngine getEnginePrivileged(final String engineName){
        return AccessController.doPrivileged(new PrivilegedAction<ScriptEngine>() {

            @Override
            public ScriptEngine run() {
                ScriptEngine take1 = manager.getEngineByName(engineName);
                return take1 != null ? take1 : manager.getEngineByExtension(engineName);
            }
        });
    }

}
