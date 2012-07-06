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
