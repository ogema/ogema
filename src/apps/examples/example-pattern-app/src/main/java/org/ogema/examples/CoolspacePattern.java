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
package org.ogema.examples;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.ranges.TemperatureRange;
import org.ogema.model.devices.whitegoods.CoolingDevice;

/**
 * Resource pattern for a cooling device controlled by this applications. This contains all data points used by the controller.
 */
// Take note of the Generics parameter "CoolingDevice" here, this tells the framework that the basis model looked for is a cooling device, 
// which is stored in the field "model". Most other fields in this class refer to this field.<br>
// replace this by your desired resource type, and adapt the subresource fields below
public class CoolspacePattern extends ResourcePattern<CoolingDevice> {

	/**
	 * Device temperature reading.
	 */
	public final TemperatureResource temperature = model.temperatureSensor().reading();

	/**
	 * Control switch for the device: we require exclusive write access, so that no other application
	 * can change the value while we control it, but we do not register a particular access priority. If another application
	 * tried to access the resource with exclusive write access and higher than lowest priority, we will lose access to the pattern, 
	 * and a patternUnavailable callback will be issued. <br>
	 * The default value for Access is mode = AccessMode.SHARED.
	 */
	@Access(mode = AccessMode.EXCLUSIVE)
	public BooleanResource stateControl = model.onOffSwitch().stateControl();

	private final TemperatureRange controlLimits = model.temperatureSensor().settings().controlLimits();

	/**
	 * Maximum temperature the device may attain.
	 */
	protected FloatResource maxTemp = controlLimits.upperLimit();

	/**
	 * Minimum cooling temperature. 
	 */
	protected FloatResource minTemp = controlLimits.lowerLimit();
	
	/**
	 * Is the device controllable? We do not require this field to exist, if it does not exist but the device has a
	 * stateControl resource (which must exist in order to match our pattern definition), it is reasonable
	 * to assume the device to be controllable. Note that the default value for Existence is CreateMode.MUST_EXIST.<br>
	 * If the field exists, we want to be informed about value changes, e.g. when the device can no longer be controlled. 
	 * In this case, a renewed execution of the {@link #accept()} method is triggered. If the pattern had previously 
	 * been completed, but now accept() returns false, a resourceUnavailable() callback will be issued. The default value for
	 * ValueChangedListener is activate = false.
	 */
	@Existence(required = CreateMode.OPTIONAL)
	@ValueChangedListener(activate = true)
	protected BooleanResource isDeviceControllable = model.onOffSwitch().controllable();

	/**
	 * Constructor for the access pattern. This constructor is invoked by the framework. 
	 * It is required to exist, and must be public.
	 */
	public CoolspacePattern(Resource device) {
		super(device);
	}

	/**
	 * Here we can perform additional validity checks for the pattern. When all required subresources for the pattern are available, 
	 * the framework calls the {@link #accept()} method, and issues a {@link PatternListener#patternAvailable(ResourcePattern) patternAvailable}
	 * callback if and only if the method returns true. Otherwise, it will listen to further changes in the resource tree 
	 * (structure changes, i.e. creation/activation and deletion/deactivation of resources, and value changes for resources that 
	 * have a {@link ResourcePattern.ValueChangedListener} annotation with activate = true). 
	 * As soon as accept() returns true, resourceAvailable is called. Similarly, after issuing the 
	 * patternAvailable callback, the framework continues to listen to changes in the tree, and issues a 
	 * {@link PatternListener#patternUnavailable(ResourcePattern) patternUnavailable} callback 
	 * as soon as {@link #accept()} returns false. 
	 */
	@Override
	public boolean accept() {
		/**
		 *  we want to receive a callback if either no information on the controllable status of the device is available,
		 *  or it is definitely controllable. Otherwise, we will not try to control it.
		 */
		if (!isDeviceControllable.isActive() || isDeviceControllable.getValue()) return true;
		return false;
	}

}
