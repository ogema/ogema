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
package org.ogema.app.simulation.freezer;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.resourcemanager.ResourceValueListener;

/**
 * A simulated FreezerSimulation object. Creates the respective resource and
 * simulates a freezer's behavior.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class FreezerSimulation implements TimerListener {

	/**
	 * Update rate for the currentTemp in simulated ms.
	 */
	final long UPDATE_RATE = 10000;

	final private OgemaLogger m_log;
	final private ApplicationManager m_appMan;

	// Reference to the device access pattern.
	final private FreezerPattern m_device;

	// Timer that is periodically invoked to update the temperature
	private Timer m_timer;

	// Time of the last simulation step.
	private long lastTime = 0;

	FreezerSimulation(ApplicationManager appManager, FreezerPattern fridge) {
		m_log = appManager.getLogger();
		m_appMan = appManager;
		m_device = fridge;
		m_log.debug("New object for simulated fridge was created.");
	}

	/**
	 * Create the OGEMA resource representing the device and start simulating.
	 *
	 * @return true if the simulation runs (even if it already was running),
	 * false if the simulation could not be started.
	 */
	public boolean init() {
		// Register listener and start simulation.
		m_device.stateControl.addValueListener(m_switchListener);
		m_timer = m_appMan.createTimer(UPDATE_RATE, this);
		m_log.debug("Freezer simulator initialized successfully.");
		return true;
	}

	/**
	 * Stop simulating and remove the simulated fridge from the OGEMA system.
	 */
	public void destroy() {
		m_device.stateControl.removeValueListener(m_switchListener);
		if (m_timer != null) {
			m_timer.stop();
			m_appMan.destroyTimer(m_timer);
			m_timer = null;
		}
	}

	/**
	 * Method called every so-many simulated seconds. Updates the currentTemp
	 * according to the state of the device and the time passed since the last
	 * update.
	 */
	@Override
	public void timerElapsed(Timer timer) {
		m_log.trace("timer elapsed");

		final long t = m_appMan.getFrameworkTime();
		if (t < lastTime || lastTime == 0) {
			// need to reset or first call (after a reset) -> remember t and do nothing else.
			lastTime = t;
			return;
		}

		final float dt = 0.001f * (t - lastTime);
		lastTime = t;

		final float T = m_device.currentTemp.getValue();
		final boolean state = m_device.feedback.getValue();
		m_log.debug("Current device temperature is " + T + ", state is " + state);

		final double KAPPA_CONDUCT = 1. / (20. * 3600.);
		final double KAPPA_COOL = -8. / 3600.;
		final double conduction = KAPPA_CONDUCT * (293.15f - T) * dt;
		final double cooling = (state) ? KAPPA_COOL * dt : 0.f;
		final double dT = conduction + cooling;

		m_log.trace("Setting new device temperature to " + (T + dT));
		m_device.currentTemp.setValue((float) (T + dT));
		m_device.currentPower.setValue(state ? m_device.maxPower.getValue() : 0.f);
	}

	/**
	 * Callback received when the stateControl state has been changed. Sets the
	 * feedback to the control value.
	 */
	private final ResourceValueListener<BooleanResource> m_switchListener = new ResourceValueListener<BooleanResource>() {
		@Override
		public void resourceChanged(BooleanResource stateControl) {
			m_log.debug(FreezerSimulation.this + ": Received callback that state was changed");
			final boolean state = stateControl.getValue();
			if (m_device.feedback.getValue() != state)
				m_device.feedback.setValue(state);
		}
	};

	FreezerPattern getDevice() {
		return m_device;
	}
}
