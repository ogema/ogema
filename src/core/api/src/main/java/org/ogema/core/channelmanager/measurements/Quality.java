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
package org.ogema.core.channelmanager.measurements;

/**
 * Quality of a {@link SampledValue} (e.g. a measurement). A Quality.GOOD indicates
 * that the value shall be used for further processing. This means it is considered
 * correct and reliable.<br>
 * In the context of schedules and most of the OGEMA TimeSeries API Quality.BAD is
 * used to model definition gaps. If, in case of measurements reported, applications
 * provide data where Quality.BAD does not indicate a gap but just doubtful measurement
 * quality and the values marked with Quality.BAD shall still be used in a standard API
 * method set the QualityBadEffect.IGNORE_QUALITY flag of the method.
 */
public enum Quality {

	/**
	 * Value is not considered reliable. Calculations based on this value are probably
	 * not reliable, either.
	 */
	BAD(0),
	/**
	 * Value is reliable and can be used for further calculations.
	 */
	GOOD(1);

	private final int quality;

	private Quality(int quality) {
		this.quality = quality;
	}

	public int getQuality() {
		return quality;
	}

	public static Quality getQuality(int quality) {
		switch (quality) {
		case 0:
			return Quality.BAD;
		case 1:
			return Quality.GOOD;
		default:
			return null;
		}
	}
}
