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
package org.ogema.application.manager.impl;

import java.util.ArrayList;
import java.util.List;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.RegisteredTimer;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;

/**
 *
 * @author jlapp
 */
public class DefaultRegisteredTimer implements RegisteredTimer {

	final Timer timer;
	final ApplicationManager app;

	public DefaultRegisteredTimer(Timer timer, ApplicationManager app) {
		this.timer = timer;
		this.app = app;
	}

	static List<RegisteredTimer> asRegisteredTimers(ApplicationManager app, List<Timer> timers){
        List<RegisteredTimer> rval = new ArrayList<>(timers.size());
        for (Timer t: timers){
            rval.add(new DefaultRegisteredTimer(t, app));
        }
        return rval;
    }

	@Override
	public AdminApplication getApplication() {
		return app.getAdministrationManager().getAppById(app.getAppID().getIDString());
	}

	@Override
	public Timer getTimer() {
		return timer;
	}

	@Override
	public List<TimerListener> getListeners() {
		return timer.getListeners();
	}

}
