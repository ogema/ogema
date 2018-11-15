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
package org.ogema.tools.memoryschedules.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;

/**
 * Calculator that takes two time series to be merged and, based on the support
 * timestamps and the time series' interpolation modes, determines the relevant
 * support timestamps and interpolation mode for the resulting merge. Bad
 * qualities are assumed to be defining definition gaps. An interval being a
 * definition gap in either input time series results in a definition gap in the
 * result.<br>
 *
 * Intended use is as support functionality within TimeSeries implementations.
 *
 * TODO several sensible cases are still missing. Shall be implemented as the
 * need arises.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class TimeSeriesMerger {

    private final List<Long> m_timestamps = new ArrayList<>();
    private InterpolationMode m_mode;

    /**
     * Creates the merger information from two TimeSeries. TimeSeries passed are
     * not changed and not stored for subsequent calculations. Creation of this
     * is not thread safe, changes in the arguments may cause problems.
     */
    public TimeSeriesMerger(final ReadOnlyTimeSeries f1, final ReadOnlyTimeSeries f2) {
        // extract the relevant information, then process the merge
        final InterpolationMode m1 = f1.getInterpolationMode();
        final InterpolationMode m2 = f2.getInterpolationMode();

        // case: Either interpolation mode is NONE => Result has IM NONE.
        if (m1 == InterpolationMode.NONE) {
            processNone(f1, f2);
            return;
        }
        if (m2 == InterpolationMode.NONE) {
            processNone(f2, f1);
            return;
        }

        // case: Both time series are interpolated linearly.
        if (m1 == InterpolationMode.LINEAR) {
            if (m2 == InterpolationMode.LINEAR) {
                processLinearLinear(f1, f2);
                return;
            }
            if (m2 == InterpolationMode.STEPS) {
                processLinSteps(f1, f2);
                return;
            }
        }

        // case: Both time series are interpolated by steps.
        if (m1 == InterpolationMode.STEPS) {
            if (m2 == InterpolationMode.STEPS) {
                processStepStep(f1, f2);
                return;
            }
            if (m2 == InterpolationMode.LINEAR) {
                processLinSteps(f2, f1);
                return;
            }
        }

        throw new IllegalArgumentException("Cannot create a TimeSeriesMerger for interpolation modes " + m1 + " and " + m2 + ". If this is a sensible combination, please recommend an implementation.");
    }

    /**
     * Gets the relevant timestamps for the resulting function.
     *
     * @return reference to the calculated timestamps.
     */
    public List<Long> getTimestamps() {
        return m_timestamps;
    }

    /**
     * Gets the resulting interpolation model.
     */
    public InterpolationMode getInterpolationMode() {
        return m_mode;
    }

    /*
     * Internal calculations invoked in the constructor.
     */
    /**
     * Process the case where the interpolation mode of f1 is NONE.
     */
    private void processNone(final ReadOnlyTimeSeries f1, final ReadOnlyTimeSeries f2) {
        m_mode = InterpolationMode.NONE;
        for (SampledValue value : f1.getValues(Long.MIN_VALUE)) {
            if (value.getQuality() == Quality.BAD) continue;
            final long t = value.getTimestamp();
            final SampledValue v2 = f2.getValue(t);
            if (v2 == null) continue;
            final Quality q2 = v2.getQuality();
            if (q2 == Quality.BAD) continue;
            m_timestamps.add(t);
        }
    }

    /**
     * Process the case that both time series are linear.
     */
    private void processLinearLinear(final ReadOnlyTimeSeries f1, final ReadOnlyTimeSeries f2) {
        m_mode = InterpolationMode.LINEAR;
        m_timestamps.addAll(getTimestamps(f1));
        final List<Long> t2 = getTimestamps(f2);
        m_timestamps.removeAll(t2); // to avoid duplicates.
        m_timestamps.addAll(t2);
        // could perform a reduction here: A timestamp t(i) or which t(i-1) and t(i+1) are bad (or non-existing) can be removed.
        Collections.sort(m_timestamps);
    }

    /**
     * Process the case that both time series are steps.
     */
    private void processStepStep(final ReadOnlyTimeSeries f1, final ReadOnlyTimeSeries f2) {
        m_mode = InterpolationMode.STEPS;
        m_timestamps.addAll(getTimestamps(f1));
        final List<Long> t2 = getTimestamps(f2);
        m_timestamps.removeAll(t2); // to avoid duplicates.
        m_timestamps.addAll(t2);
        // could perform a reduction here: A BAD timestamp t(i) for which t(i-1) is BAD can be removed.
        Collections.sort(m_timestamps);
    }

    /**
     * Gets the timestamps of the values defining the time series f.
     */
    private List<Long> getTimestamps(final ReadOnlyTimeSeries f) {
        final List<SampledValue> values = f.getValues(Long.MIN_VALUE);
        final List<Long> result = new ArrayList<>(values.size());
        for (SampledValue value : values) {
            result.add(value.getTimestamp());
        }
        return result;
    }

    /**
     * Case: f1 is linearly interpolated, f2 is step-wise interpolated.
     *
     * @param f1
     * @param f2
     */
    private void processLinSteps(ReadOnlyTimeSeries f1, ReadOnlyTimeSeries f2) {
        m_mode = InterpolationMode.LINEAR;
        final List<Long> t1 = getTimestamps(f1);
        final List<Long> t2 = getTimestamps(f2);
        final List<Long> extraTime = new ArrayList<>(t2.size());

        long tMin = Long.MAX_VALUE;
        boolean first = true;
        Collections.sort(t2);
        for (Long t : t2) {
            if (first) { // do not add an extra entry for the first entry in steps.
                first = false;
                continue;
            }
            final long before = t-1;
            if (before<tMin) tMin = before;
            extraTime.add(before);
        }
        extraTime.removeAll(t2); // no need for extra time if already in t2.
        
        m_timestamps.addAll(t1);
        m_timestamps.removeAll(t2);
        m_timestamps.removeAll(extraTime);        
        m_timestamps.addAll(t2);
        m_timestamps.addAll(extraTime);
        Collections.sort(m_timestamps);
    }
}
