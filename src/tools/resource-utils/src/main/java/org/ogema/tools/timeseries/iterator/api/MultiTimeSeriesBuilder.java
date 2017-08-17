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
package org.ogema.tools.timeseries.iterator.api;

import java.util.Collection;
import java.util.Objects;

import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.tools.timeseries.iterator.impl.BooleanLogicTimeSeries;
import org.ogema.tools.timeseries.iterator.impl.FunctionMultiTimeSeries;
import org.ogema.tools.timeseries.iterator.impl.GenericSumTimeSeries;

import com.google.common.base.Function;

/**
 * Create a time series that is composed of several other time series.
 * It can represent for instance the sum or average of the underlying
 * time series, or an AND or OR concatenation of boolean time series.
 * 
 * @author cnoelle
 *
 * @param <N>
 */
public class MultiTimeSeriesBuilder<N> {

	private final Collection<ReadOnlyTimeSeries> timeSeries;
	private final Class<N> type;
	private boolean ignoreGaps;
	// only relevant for float time series
	private boolean sumOrAverage;
	// only relevant for boolean time series
	private boolean andOrOr;
	private Function<Collection<N>,N> function;
	// TODO individual modes
	private InterpolationMode forcedModeConstituents;
	private InterpolationMode forcedModeResult;

	/**
	 * @param timeSeries
	 * 		The underlying time series
	 * @param type
	 * 		The type of the time series data points. Must be one of the following:
	 * 		<ul>
	 * 			<li><code>Float.class</code>
	 * 			<li><code>Boolean.class</code>
	 * 			<li><code>Integer.class</code>
	 * 			<li><code>Long.class</code>
	 * 		</ul>
	 */
	private MultiTimeSeriesBuilder(Collection<ReadOnlyTimeSeries> timeSeries, Class<N> type) {
		this.timeSeries = Objects.requireNonNull(timeSeries);
		this.type = Objects.requireNonNull(type);
		if (type != Float.class && type != Integer.class && type != Long.class && type != Boolean.class)
			throw new IllegalArgumentException("Illegal type, must be either Float.class, Boolean.class, Integer.class or Long.class, got " + type);
	}
	
	/**
	 * Create a new builder instance.
	 * @param timeSeries
	 * 		The underlying time series
	 * @param type
	 * 		The type of the time series data points. Must be one of the following:
	 * 		<ul>
	 * 			<li><code>Float.class</code>
	 * 			<li><code>Boolean.class</code>
	 * 			<li><code>Integer.class</code>
	 * 			<li><code>Long.class</code>
	 * 		</ul>
	 * @return
	 */
	public static <N> MultiTimeSeriesBuilder<N> newBuilder(Collection<ReadOnlyTimeSeries> timeSeries, Class<N> type) {
		return new MultiTimeSeriesBuilder<N>(timeSeries, type);
	}
	
	
	/**
	 * Create the multi time series based on the provided input data.
	 * No data points are copied to the multi time series, instead it evaluates
	 * the underlying time series upon demand.
	 * @return
	 * @throws IllegalStateException if the type of the time series is not equals to Boolean.class or Float.class, and 
	 * 		no function has been set yet.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ReadOnlyTimeSeries build() {
		if (type == Boolean.class) {
			return andOrOr ? new BooleanLogicTimeSeries.BooleanAndTimeSeries(timeSeries, ignoreGaps) 
					: new BooleanLogicTimeSeries.BooleanOrTimeSeries(timeSeries, ignoreGaps);
		}
		if (function != null) {
			return new FunctionMultiTimeSeries(timeSeries, ignoreGaps, function, type, forcedModeConstituents, forcedModeResult);
		}
		if (type != Float.class)
			throw new IllegalStateException("No function has been set yet, which is required for non-Float time series.");
		return new GenericSumTimeSeries(timeSeries, sumOrAverage, ignoreGaps, forcedModeConstituents, forcedModeResult);
	}
	
	/**
	 * Define how to calculate the values of the multi time series from the 
	 * values of the underlying time series. Note: in order to calculate the
	 * sum or average values for float time series, {@link #setSum())} or {@link #setAverage()}
	 * can be used instead.
	 * @param function
	 */
	public MultiTimeSeriesBuilder<N> setFunction(Function<Collection<N>,N> function) {
		if (function == null) {
			this.function = null;
			return this;
		}
		if (type == Boolean.class)
			throw new IllegalStateException("Functions not supported for boolean time series");
		this.function = function;
		return this;
	}
	
	/**
	 * For float time series. Note: this resets the function to null.
	 * @throws IllegalStateException
	 * 		if the type is not equal to Float.class
	 */
	public MultiTimeSeriesBuilder<N> setSum() {
		if (type != Float.class)
			throw new IllegalStateException("Sum and average only supported for float time series");
		this.sumOrAverage = true;
		this.function = null; 
		return this;
	}
	
	/**
	 * For float time series. Note: this resets the function to null.
	 * This is the default for float time series.
	 * @throws IllegalStateException
	 * 		if the type is not equal to Float.class
	 */
	public MultiTimeSeriesBuilder<N> setAverage() {
		if (type != Float.class)
			throw new IllegalStateException("Sum and average only supported for float time series");
		this.sumOrAverage = false;
		this.function = null; 
		return this;
	}
	
	/**
	 * For boolean time series.
	 * @throws IllegalStateException 
	 * 		if the type is not equal to Boolean.class
	 */
	public MultiTimeSeriesBuilder<N> setAnd() {
		if (type != Boolean.class) 
			throw new IllegalStateException("AND and OR only supported by boolean time series");
		this.andOrOr = true;
		return this;
	}
	
	/**
	 * For boolean time series; this is the default setting. 
	 * @throws IllegalStateException 
	 * 		if the type is not equal to Boolean.class
	 */
	public MultiTimeSeriesBuilder<N> setOr() {
		if (type != Boolean.class) 
			throw new IllegalStateException("AND and OR only supported by boolean time series");
		this.andOrOr = false;
		return this;
	}
	
	/**
	 * Define how to deal with gaps in one or more time series. 
	 * If true, the multi time series will have a good quality value wherever 
	 * at least one of the underlying time series has one. If false, all of the
	 * underlying time series must have good quality values for the multi time 
	 * series to have one.
	 * @param ignoreGaps
	 * 		default: false
	 */
	public MultiTimeSeriesBuilder<N> ignoreGaps(boolean ignoreGaps) {
		this.ignoreGaps = ignoreGaps;
		return this;
	}
	
	/**
	 * Calculate the multi time series values using the passed interpolation mode on the input time series,
	 * instead of their intrinsic interpolation modes.
	 * @param mode
	 * @return
	 * @throws UnsupportedOperationException for boolean time series
	 */
	public MultiTimeSeriesBuilder<N> setInterpolationModeForConstituents(InterpolationMode mode) {
		if (type == Boolean.class && mode != InterpolationMode.STEPS)
			throw new UnsupportedOperationException("Boolean multi time series only supports STEP mode");
		this.forcedModeConstituents = mode;
		return this;
	}
	
	/**
	 * Set the interpolation mode for the resulting multi time series. If this is not set (or set to null)
	 * then the interpolation mode will be determined heuristically from the input time series.
	 * @param mode
	 * @return
	 * @throws UnsupportedOperationException for boolean time series
	 */
	public MultiTimeSeriesBuilder<N> setInterpolationModeForResult(InterpolationMode mode) {
		if (type == Boolean.class && mode != InterpolationMode.STEPS)
			throw new UnsupportedOperationException("Boolean multi time series only supports STEP mode");
		this.forcedModeResult = mode;
		return this;
	}
	
	
}
