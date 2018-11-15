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
package org.ogema.tools.resourcemanipulator.configurations;

import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;

/**
 * Configuration for a threshold-manipulator involving a source resource s, a
 * target resource t and a threshold x. The value of T is kept up-to-date
 * according to whether s exceeds x or not. Two parameters allow further
 * modification of the rule: An equality flag E allows to count the equality
 * case s=x to be effectively treated as "value exceeds the threshold", and an
 * inversion flag I allows to invert the value set into the target resource.
 * Both flags are false by default. The effective rules enforced are as
 * follows:<br>
 * E = false : t = I XOR (s&gt;x) <br>
 * E = true : t = I XOR (s&gt;=x) <br>
 *
 * If s becomes inactive, t is set inactive, too.
 * @author Timo Fischer, Fraunhofer IWES
 */
public interface Threshold extends ManipulatorConfiguration {

	/**
	 * Set the threshold rule for target such that it is true exactly if the
	 * value of source exceed the threshold. Modification of this rule can be
	 * performed before starting to enforce the rule by calling other methods
	 * like {@link #setInversion(boolean)} or
	 *
	 * @param source resource whose value shall be compared.
	 * @param threshold the threshold value that the source resource shall be
	 * compared to.
	 * @param target resource that the result of the comparison is written to..
	 */
	void setThreshold(FloatResource source, float threshold, BooleanResource target);

	/**
	 * Sets a threshold value
	 * @param threshold new threshold to use for the comparison.
	 */
	void setThreshold(float threshold);

	/**
	 * Gets the threshold value.
	 * @return currently-configured threshold value.
	 */
	float getThreshold();

	/**
	 * Sets the inversion flag, which is false by default.
	 *
	 * @param inversion new inversion flag.
	 */
	void setInversion(boolean inversion);

	/**
	 * Gets the currently-configured inversion setting.
	 *
	 * @return current state of the inversion setting.
	 */
	boolean getInversion();

	/**
	 * Sets the handing of equality of the resource and the threshold.
	 *
	 * @param equalityCountsAsExceeding new rule for the treatment of the case
	 * that the source resource equals the threshold.
	 */
	void setEqualityExceeds(boolean equalityCountsAsExceeding);

	/**
	 * Gets the information if equality is currently configured as "exceeds the
	 * threshold" or not.
	 *
	 * @return Current setting of the parameter E.
	 */
	boolean getEqualityExceeds();
}
