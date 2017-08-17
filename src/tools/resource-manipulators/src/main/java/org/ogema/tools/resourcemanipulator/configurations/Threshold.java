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
