package de.iwes.experimental.ogema.console.scripting.api;

import javax.script.ScriptEngine;

public interface ScriptEngingExtension {

	public void newScriptEnging(ScriptEngine engine);
	public void engineGone(ScriptEngine engine);
	
}
