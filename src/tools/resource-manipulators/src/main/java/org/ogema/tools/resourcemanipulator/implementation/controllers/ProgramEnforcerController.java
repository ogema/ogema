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
package org.ogema.tools.resourcemanipulator.implementation.controllers;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.DefinitionSchedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessModeListener;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.tools.resourcemanipulator.model.ProgramEnforcerModel;

/**
 * Enforces that a FloatResource always has the value configured in its program
 * or, in case of linear interpolation of the program, at least updates its
 * value to the one dictated by the program periodically.
 *
 * FIXME must react to changes of the target resource.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class ProgramEnforcerController implements Controller, ResourceValueListener<DefinitionSchedule>,
		ResourceStructureListener, AccessModeListener, TimerListener {

	private final ApplicationManager appMan;
	private final OgemaLogger logger;
	private final Resource target;
	private final Class<? extends Resource> resourceType;
	private final DefinitionSchedule program;
	private final long updateInterval;
	private final boolean exclusiveAccess;
	private final AccessPriority priority;
	private boolean requiredWriteAccessGranted;

	private Timer timer;

	public ProgramEnforcerController(ApplicationManager applicationManager, ProgramEnforcerModel configuration) {
		this.appMan = applicationManager;
		this.logger = applicationManager.getLogger();
		this.target = configuration.targetResource();
		final Class<? extends Resource> clazz = configuration.targetResource().getResourceType();
		if (BooleanResource.class.isAssignableFrom(clazz)) {
			resourceType = BooleanResource.class;
			program = ((BooleanResource) target).program();
		}
		else if (IntegerResource.class.isAssignableFrom(clazz)) {
			resourceType = IntegerResource.class;
			program = ((IntegerResource) target).program();
		}
		else if (TimeResource.class.isAssignableFrom(clazz)) {
			resourceType = TimeResource.class;
			program = ((TimeResource) target).program();
		}
		else if (FloatResource.class.isAssignableFrom(clazz)) {
			resourceType = FloatResource.class;
			program = ((FloatResource) target).program();
		}
		else if (StringResource.class.isAssignableFrom(clazz)) {
			resourceType = StringResource.class;
			program = ((StringResource) target).program();
		}
		else {
			resourceType = null; // not a valid resource type.
			program = null;
		}
		this.updateInterval = configuration.updateInterval().getValue();
		this.exclusiveAccess = configuration.exclusiveAccessRequired().getValue();
		this.priority = AccessPriority.valueOf(configuration.priority().getValue());
	}

	@Override
	public void start() {
		if (resourceType == null || program == null) {
			logger.error("Cannot start enforcing the program on resource " + target.getLocation()
					+ " which is of type " + target.getResourceType().getSimpleName()
					+ ". Probably not a suitable resource type. Start of controller will be ignored.");
			return;
		}
		// request write access to the target resource.
		requiredWriteAccessGranted = false;
		target.addStructureListener(this);
		target.addAccessModeListener(this);
		if (exclusiveAccess) {
			target.requestAccessMode(AccessMode.EXCLUSIVE, priority);
		}
		else {
			target.requestAccessMode(AccessMode.SHARED, priority);
		}

		// keep informed about changes in the program (incl. structure events)
		program.addStructureListener(this);
		program.addValueListener(this);

		// Create a timer an initialize the target value for the first time.
		timer = appMan.createTimer(10 * 60 * 1000l, this); // this is just a random timer value, timer is re-scheduled after every invocation.
		timerElapsed(timer); // check if there is something to do now.
	}

	@Override
	public void stop() {
		if (timer != null) { // meaning: if the controller had been started.
			timer.destroy();
			program.removeValueListener(this);
			program.removeStructureListener(this);
			target.removeAccessModeListener(this);
			target.removeStructureListener(this);
		}
	}

	/**
	 * Note: this is not only called because of the timer having elapsed. It is
	 * also called explicitly by other methods in this controller.
	 */
	@Override
	public void timerElapsed(Timer timer) {
		timer.stop();

		// only do something if you actually can actually write to the target.
		final boolean targetWriteable = requiredWriteAccessGranted && target.exists();
		if (!targetWriteable) {
			return;
		}

		final long t0 = appMan.getFrameworkTime();
		final SampledValue currentProgramValue = (program.isActive()) ? program.getValue(t0) : null;
		final boolean hasGoodProgramValue = (currentProgramValue != null)
				&& (currentProgramValue.getQuality() == Quality.GOOD);
		if (hasGoodProgramValue) {
			setProgramValue(currentProgramValue);
			target.activate(false);
		}
		else { // no good program: de-activate.
			target.deactivate(false);
		}

		// if there is a valid program, estimate the next required call to this.
		if (program.isActive()) {
			restartTimer(t0);
		}
	}

	/**
	 * Set the timer to the next expected change event and re-start it.
	 *
	 * @param t0 time that shall be considered "now" for the purpose of
	 * re-scheduling (i.e. the time of the entry last written).
	 */
	@SuppressWarnings("fallthrough")
	private void restartTimer(long t0) {
		final InterpolationMode interpolation = program.getInterpolationMode();
		switch (interpolation) {
		case LINEAR: {
			timer.setTimingInterval(updateInterval);
			timer.resume();
			return;
		}
		case STEPS: {
			final SampledValue nextProgramValue = program.getNextValue(t0 + 1);
			if (nextProgramValue != null) {
				final long t1 = nextProgramValue.getTimestamp();
				timer.setTimingInterval(t1 - t0);
				timer.resume();
			}
			// note: timer may not have resumed here because schedule ended. An update of the schedule will re-create the timing sequence.
			return;
		}
		case NEAREST: {
			final SampledValue nextProgramValue = program.getNextValue(t0 + 1);
			if (nextProgramValue == null) {
				return;
			}
			final long t1 = nextProgramValue.getTimestamp();
			final SampledValue nextToNextProgramValue = program.getNextValue(t1 + 1);
			if (nextToNextProgramValue == null) {
				return;
			}
			final long t2 = nextToNextProgramValue.getTimestamp();
			final long tMid = ((t1 + t2) / 2) + ((t1 + t2) % 2);
			timer.setTimingInterval(tMid - t0);
			timer.resume();
		}
		case NONE: {
			final String resLocation = target.getLocation();
			logger.warn("Resource at " + resLocation
					+ " has been configured for automatic program enforcement, but interpolation mode "
					+ interpolation.toString() + " is not suitable. Will ignore this.");
		}
		default:
			throw new UnsupportedOperationException("Encountered unknown/unexpected interpolation mode "
					+ interpolation);
		}
	}

	@Override
	public void resourceChanged(DefinitionSchedule resource) {
		timerElapsed(timer);
	}

	/**
	 * Structure of the schedule has been changed.
	 *
	 * @param eventType
	 */
	@SuppressWarnings("fallthrough")
	private void scheduleStructureChanged(ResourceStructureEvent.EventType eventType) {
		switch (eventType) { // react to changes that may effect the target resource.
		case RESOURCE_ACTIVATED:
		case RESOURCE_DEACTIVATED:
		case RESOURCE_CREATED:
		case RESOURCE_DELETED:
			timerElapsed(timer);
		default: // no need to react to anything else.
		}
	}

	/**
	 * Structure of the target resource has been changed.
	 */
	@SuppressWarnings("fallthrough")
	private void targetStructureChanged(ResourceStructureEvent.EventType eventType) {
		switch (eventType) { // react to changes that may effect the target resource.
		case RESOURCE_CREATED:
		case RESOURCE_DELETED:
			timerElapsed(timer);
		default: // no need to react to anything else (activation and de-activation likely caused by myself, anyways).
		}
	}

	@Override
	public void resourceStructureChanged(ResourceStructureEvent event) {
		if (event.getSource().equalsLocation(program)) {
			scheduleStructureChanged(event.getType());
			return;
		}
		if (event.getSource().equalsLocation(target)) {
			targetStructureChanged(event.getType());
			return;
		}
		logger.error("Got structure event for resource at " + event.getSource().getLocation()
				+ " which is not listened to. Ignoring the callback.");
	}

	/**
	 * Listener invoked whenever the required access mode to the target resource
	 * changes.
	 */
	@Override
	public void accessModeChanged(Resource resource) {
		final boolean previousAccess = requiredWriteAccessGranted;

		final AccessMode access = resource.getAccessMode();
		if (exclusiveAccess) {
			requiredWriteAccessGranted = access == AccessMode.EXCLUSIVE;
		}
		else {
			requiredWriteAccessGranted = (access == AccessMode.EXCLUSIVE) || (access == AccessMode.SHARED);
		}

		// Re-calculate everything relevant if the effective access mode changed.
		if (requiredWriteAccessGranted != previousAccess)
			timerElapsed(timer);
	}

	/**
	 * Sets the value of the target to the program's value if the program value
	 * differs from the current value. Checks for the program quality and the
	 * activity of the schedule are assumed to have passed successfully at this
	 * point.
	 */
	private void setProgramValue(SampledValue newValue) {
		if (resourceType == BooleanResource.class) {
			final BooleanResource resource = (BooleanResource) target;
			final boolean currentValue = resource.getValue();
			final boolean targetValue = newValue.getValue().getBooleanValue();
			if (currentValue != targetValue) {
				resource.setValue(targetValue);
			}

		}
		else if (resourceType == IntegerResource.class) {
			final IntegerResource resource = (IntegerResource) target;
			final int currentValue = resource.getValue();
			final int targetValue = newValue.getValue().getIntegerValue();
			if (currentValue != targetValue) {
				resource.setValue(targetValue);
			}
		}
		else if (resourceType == TimeResource.class) {
			final TimeResource resource = (TimeResource) target;
			final long currentValue = resource.getValue();
			final long targetValue = newValue.getValue().getLongValue();
			if (currentValue != targetValue) {
				resource.setValue(targetValue);
			}
		}
		else if (resourceType == FloatResource.class) {
			final FloatResource resource = (FloatResource) target;
			final float currentValue = resource.getValue();
			final float targetValue = newValue.getValue().getFloatValue();
			if (currentValue != targetValue) {
				resource.setValue(targetValue);
			}
		}
		else if (resourceType == StringResource.class) {
			final StringResource resource = (StringResource) target;
			final String currentValue = resource.getValue();
			final String targetValue = newValue.getValue().getStringValue();
			if (currentValue != targetValue) {
				resource.setValue(targetValue);
			}
		}
		else {
			throw new UnsupportedOperationException("Cannot set the value for unsupported resource type "
					+ resourceType.getCanonicalName()
					+ ". You should never see this message. Please report this to the OGEMA developers.");
		}
	}
}
