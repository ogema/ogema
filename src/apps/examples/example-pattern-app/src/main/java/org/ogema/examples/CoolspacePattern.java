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
