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
package org.ogema.tools.memoryschedules.tools;

import org.ogema.tools.memoryschedules.tools.IndexInterval;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.ogema.core.channelmanager.measurements.SampledValue;

/**
 * Sorted list of SampledValues with SortedSet-like access methods. Optimized
 * for appending values at the end whose timestamp is larger than those of the
 * existing values (e.g. for logging). As in TimeSeries, it is expected that 
 * at most one SampledValue exists for any given time t. This class is not
 * thread-safe. Synchronization has to be performed in the class using this.
 * 
 * TODO implementation of this is not nice, so far. Needs overhaul.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class SampledValueSortedList {

    public static final int NO_SUCH_INDEX = -1;
    private final List<SampledValue> m_values = new ArrayList<>();

    /**
     * Creates an empty list.
     */
    public SampledValueSortedList() {
    }

    /**
     * Gets a copy of the list.
     */
    public List<SampledValue> getCopy() {
        return new ArrayList<>(m_values);
    }

    /**
     * Sorts the values.
     */
    private void sort() {
        Collections.sort(m_values);
    }

    /**
     * Empties the list.
     */
    public void clear() {
        m_values.clear();
    }

    /**
     * Returns the size of the list.
     */
    public int size() {
        return m_values.size();
    }

    public boolean isEmpty() {
        return m_values.isEmpty();
    }

    /**
     * Does the insertion of the new value at the end require re-sorting the array?
     */
    private boolean newValueRequiresSorting(final SampledValue value) {
        if (m_values.isEmpty()) return false;
        final int L = m_values.size();
        return m_values.get(L - 1).getTimestamp() > value.getTimestamp();
    }

    /**
     * Adds copies of the values to this.
     */
    public void addValuesCopies(List<SampledValue> values) {
        boolean needsSort = false;
        for (SampledValue value : values) {
            final SampledValue newValue = new SampledValue(value);
            needsSort |= newValueRequiresSorting(value);
            m_values.add(newValue);
        }
        if (needsSort) sort();
    }


    private void getLowerBoundaryExclusive(long time, IndexInterval result, IndexInterval searchInterval) {
        // sanity check: subdivision of intervals can never result in interval of size zero.
        assert searchInterval.size() > 0;

        if (searchInterval.size() == 1) {
            result.setMin(searchInterval.getMin());
            return;
        }

        final int imid = searchInterval.mid();
        final SampledValue pivot = m_values.get(imid);
        final IndexInterval subInterval = (pivot.getTimestamp() > time) ? searchInterval.lowerHalf() : searchInterval.upperHalf();
        getLowerBoundaryExclusive(time, result, subInterval);
    }

    /**
     * Sets the lower limit of the result such that it equals the smallest index whose associated
     * value has a timestamp that is at least equal to time. Sets it to size() if no such index
     * exists.
     */
    private void getLowerBoundaryInclusive(long time, IndexInterval result, IndexInterval searchInterval) {
        // sanity check: subdivision of intervals can never result in interval of size zero.
        assert searchInterval.size() > 0;

        final int imid = searchInterval.mid();
        final SampledValue pivot = m_values.get(imid);
        final long tPivot = pivot.getTimestamp();
        if (searchInterval.size() == 1) {
            final int idx = (tPivot>=time) ? searchInterval.getMin() : searchInterval.getMin()+1;
            result.setMin(idx);
            return;
        }

        if (tPivot<time) {
            getLowerBoundaryInclusive(time, result, searchInterval.upperHalf());
        } else {
            getLowerBoundaryInclusive(time, result, searchInterval.lowerHalf());
        }
    }

    /**
     * Sets the upper limit in result such that the timestamp of the value at the upper
     * limit exceeds time. If no such value exists, the upper limit of result is set to
     * this.size().
     * 
     * @param searchInterval Interval to search in. It is assumed that at start of the recursion
     * the search interval is sufficiently large.
     */
    private void getUpperBoundary(long time, IndexInterval result, IndexInterval searchInterval) {
        // sanity check: subdivision of intervals can never result in interval of size zero.
        assert searchInterval.size() > 0;

        if (searchInterval.size() == 1) {
            result.setMax(searchInterval.getMax());
            return;
        }

        final int imid = searchInterval.mid();
        final SampledValue pivot = m_values.get(imid);
        final long tPivot = pivot.getTimestamp();
        if (tPivot <= time) {
            getUpperBoundary(time, result, searchInterval.upperHalf());
        } else {
            getUpperBoundary(time, result, searchInterval.lowerHalf());
        }
    }

    /**
     * Gets the index of the entry with the largest index whose timestamp is at most time.
     */
    public int getIndexBelow(long time) {
        if (m_values.isEmpty()) return NO_SUCH_INDEX;
        if (m_values.get(0).getTimestamp() > time) return NO_SUCH_INDEX;
        final IndexInterval result = new IndexInterval(m_values);
        final IndexInterval search = new IndexInterval(m_values);
        getLowerBoundaryExclusive(time, result, search);
        return result.getMin();
    }

    /**
     * Gets the index for the value with time t.
     * @return Index of the entry corresponding to time. If no such entry exists,
     * NO_SUCH_INDEX is returned.
     */
    public int getValueIndex(long time) {
        final int idx = getIndexBelow(time);
        if (idx == NO_SUCH_INDEX || idx >= m_values.size())
            return NO_SUCH_INDEX;
        final long tElement = m_values.get(idx).getTimestamp();
        return (tElement == time) ? idx : NO_SUCH_INDEX;
    }

    public  SampledValue getNextValue(long time) {
        final int idx = getIndexBelow(time);
        if (idx >= m_values.size() - 1) return null;
        return new SampledValue(m_values.get(idx + 1));
    }
    
    public SampledValue getPreviousValue(long time) {
        final int idx = getIndexBelow(time);
        if (idx < 0)
        	return null;
        return new SampledValue(m_values.get(idx));
    }

    public void addValue(SampledValue value) {
        final SampledValue newValue = new SampledValue(value);
        final long time = value.getTimestamp();
        final int oldIndex = getValueIndex(time);
        if (oldIndex != NO_SUCH_INDEX) {
            m_values.set(oldIndex, newValue);
        } else {
            final boolean doSort = newValueRequiresSorting(newValue);
            m_values.add(newValue);
            if (doSort) sort();
        }
    }

    /**
     * Gets the subset of entries in the time interval [t0;t1)
     * 
     * @param t0
     *            left border of the interval
     * @param t1
     *            right border of the interval - non-inclusive
     * @return Set to all elements in the interval.
     */
    private List<SampledValue> getSublist(long t0, long t1) {
        // TODO re-implement
        if (t1 <= t0)
            return new ArrayList<>();

        final int size = m_values.size();

        int lower = getIndexBelow(t0);
        if (lower == -1) lower = 0;
        while (lower < size && m_values.get(lower).getTimestamp() < t0) ++lower;
        if (lower == size) return new ArrayList<>();

        int upper = getIndexBelow(t1);
        if (upper == -1)
        	return new ArrayList<>();
        while (upper < size && m_values.get(upper).getTimestamp() < t1)
            ++upper;

        return m_values.subList(lower, upper);
    }

    /**
     * Returns a copy of the section [i0;i1).
     */
    public List<SampledValue> getCopy(int i0, int i1) {
        final int size = m_values.size();
        if (i0 < 0) i0 = 0;
        if (i1 > size) i1 = size;
        if (i0 >= size || i1 <= 0) return new ArrayList<>();

        final List<SampledValue> result = new ArrayList<>(i1 - i0);
        for (int i = i0; i < i1; ++i)
            result.add(new SampledValue(m_values.get(i)));
        return result;
    }

    /**
     * Gets the list of values. Note: This does not return a copy of the values
     * but the actual list held by this class. Should be used with care, only.
     */
    public List<SampledValue> getValues() {
        return m_values;
    }

    public void deleteValues(long startTime, long endTime) {
        final List<SampledValue> subInterval = getSublist(startTime, endTime);
        m_values.removeAll(subInterval);
    }

    /**
     * Gets the i'th element.
     */
    public SampledValue get(int i) {
        return m_values.get(i);
    }

    public List<SampledValue> getValues(long startTime) {
        if (m_values.isEmpty()) return new ArrayList<>();
        final int size = m_values.size();

        int idx = getIndexBelow(startTime);
        if (idx < 0) idx = 0;

        while (idx < size && m_values.get(idx).getTimestamp() < startTime)
            ++idx;

        if (idx >= size) return new ArrayList<>();
        List<SampledValue> subList = m_values.subList(idx, size);
        final List<SampledValue> result = new ArrayList<>(subList.size());
        for (SampledValue value : subList)
            result.add(new SampledValue(value));
        return result;
    }

    public List<SampledValue> getValues(long startTime, long endTime) {
        final List<SampledValue> sublist = getSublist(startTime, endTime);
        final List<SampledValue> result = new ArrayList<>(sublist.size());
        for (SampledValue value : sublist)
            result.add(new SampledValue(value));
        return result;
    }
    
    public boolean isEmpty(long startInclusive, long endInclusive) {
    	if (m_values.isEmpty())
    		return true;
    	int i = getIndexBelow(endInclusive);
    	return (i == NO_SUCH_INDEX || m_values.get(i).getTimestamp() < startInclusive);
    }
    
    public int size(long startInclusive, long endInclusive) {
    	return getSublist(startInclusive, endInclusive).size();
    }
    
    public Iterator<SampledValue> iterator() {
    	return m_values.iterator();
    }
    
    public Iterator<SampledValue> iterator(long startInclusive, long endInclusive) {
    	return getSublist(startInclusive, endInclusive).iterator();
    }
    
}
