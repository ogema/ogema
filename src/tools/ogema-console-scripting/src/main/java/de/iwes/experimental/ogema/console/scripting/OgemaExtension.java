package de.iwes.experimental.ogema.console.scripting;

import java.util.concurrent.ForkJoinPool;

import javax.script.ScriptEngine;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import de.iwes.experimental.ogema.console.scripting.api.ScriptEngingExtension;

@Service(Application.class)
@Component
public class OgemaExtension implements ScriptEngingExtension, Application {

	private BundleContext ctx;
	private ServiceRegistration<ScriptEngingExtension> ownRef;
	private volatile ApplicationManager appMan;
	
	@Activate
	protected void activate(BundleContext ctx) {
		this.ctx = ctx;
	}
	
	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
		ownRef = ctx.registerService(ScriptEngingExtension.class, this, null);
	}
	
	@Override
	public void stop(AppStopReason reason) {
		final Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					ownRef.unregister();
				} catch (Exception e) {}
			}
		});
		t.run();
		try {
			t.join(3000);
		} catch (InterruptedException e) {
			try {
				Thread.currentThread().interrupt();
			} catch (SecurityException ignore) {}
		}
		this.appMan = null;
	}
	
	@Override
	public void newScriptEnging(ScriptEngine engine) {
		final ApplicationManager appMan = this.appMan;
		if (appMan != null)
			engine.put("manager", appMan);
	}
	
	@Override
	public void engineGone(ScriptEngine engine) {
		engine.put("manager", null);
	}
	
}
