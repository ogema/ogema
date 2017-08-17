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
package org.ogema.tools.resource.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.ValueResource;
import org.ogema.core.model.array.ArrayResource;
import org.ogema.core.model.array.BooleanArrayResource;
import org.ogema.core.model.array.ByteArrayResource;
import org.ogema.core.model.array.FloatArrayResource;
import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.array.TimeArrayResource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.model.units.PhysicalUnitResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.tools.timeseries.api.FloatTimeSeries;
import org.ogema.tools.timeseries.api.MemoryTimeSeries;
import org.ogema.tools.timeseries.implementations.FloatTreeTimeSeries;
import org.ogema.tools.timeseries.implementations.TreeTimeSeries;

/**
 * Offers convenience methods for dealing with {@link ValueResource}s, such as 
 * retrieving the value of a generic {@link SingleValueResource} as a String, which can
 * be used, for instance, for display in a user interface.
 */
@SuppressWarnings("deprecation")
public class ValueResourceUtils {
	
	// no need to construct this
	private ValueResourceUtils() {}
	
	/**
	 * Set the value of a ValueResource. This is a convenience method that does not perform any
	 * type checks - if they value type does not match the expected type for 
	 * the resource passed, an exception is thrown. The correspondence between resource type and value type is
	 * <ul>
	 * 	<li>SingleValueResource: the respective primitive type (or String), e.g. float for a FloatResource
	 *  <li>ArrayResource: the respective array type, e.g. boolean[] for a BooleanResource
	 *  <li><code>Schedule: List&lt;SampledValue&gt;</code>
	 * </ul>
	 * @param resource
	 * @param value
	 * @throws ClassCastException
	 * 		If the value passed is not of the expected type, and the resource is not of SingleValueType
	 * @throws NumberFormatException
	 * 		If the resource is a SingleValueResource but the value passed is not of the expected primitive type
	 * 		
	 */
	@SuppressWarnings("unchecked")
	public static void setValue(ValueResource resource, Object value) throws ClassCastException, NumberFormatException {
		if (resource instanceof SingleValueResource) 
			setValue((SingleValueResource) resource, value.toString());
		else if (resource instanceof Schedule) {
			Collection<SampledValue> values;
			if (value instanceof ReadOnlyTimeSeries)
				values = ((ReadOnlyTimeSeries) value).getValues(Long.MIN_VALUE);
			else if (value instanceof List) 
				values = (Collection<SampledValue>) value;
			else 
				throw new IllegalArgumentException("Schedule value must be either a time series or a collection of SampledValue objects");
			((Schedule) resource).replaceValues(Long.MIN_VALUE, Long.MAX_VALUE, values);
		}
		else if (resource instanceof IntegerArrayResource) 
			((IntegerArrayResource) resource).setValues((int[]) value);
		else if (resource instanceof FloatArrayResource) 
			((FloatArrayResource) resource).setValues((float[]) value);
		else if (resource instanceof TimeArrayResource)
			((TimeArrayResource) resource).setValues((long[]) value);
		else if (resource instanceof BooleanArrayResource)
			((BooleanArrayResource) resource).setValues((boolean[]) value);
		else if (resource instanceof StringArrayResource)
			((StringArrayResource) resource).setValues((String[]) value);
		else if (resource instanceof ByteArrayResource)
			((ByteArrayResource) resource).setValues((byte[]) value);
		else if (resource instanceof org.ogema.core.model.simple.OpaqueResource)
			((org.ogema.core.model.simple.OpaqueResource) resource).setValue((byte[]) value);
	}
	
	/**
	 * Returns the value of a ValueResource as an object. 
	 * @param resource
	 * @return
	 */
	public static Object getValue(ValueResource resource) {
		if (resource instanceof FloatResource) 
			return ((FloatResource) resource).getValue();
		if (resource instanceof StringResource)
			return ((StringResource) resource).getValue();
		if (resource instanceof IntegerResource)
			return ((IntegerResource) resource).getValue();
		if (resource instanceof TimeResource)
			return ((TimeResource) resource).getValue();
		if (resource instanceof BooleanResource)
			return ((BooleanResource) resource).getValue();
		if (resource instanceof Schedule)
			return ((Schedule) resource).getValues(Long.MIN_VALUE);
		if (resource instanceof ByteArrayResource)
			return ((ByteArrayResource) resource).getValues();
		if (resource instanceof IntegerArrayResource)
			return ((IntegerArrayResource) resource).getValues();
		if (resource instanceof BooleanArrayResource)
			return ((BooleanArrayResource) resource).getValues();
		if (resource instanceof FloatArrayResource)
			return ((FloatArrayResource) resource).getValues();
		if (resource instanceof TimeArrayResource)
			return ((TimeArrayResource) resource).getValues();
		if (resource instanceof StringArrayResource)
			return ((StringArrayResource) resource).getValues();
		if (resource instanceof org.ogema.core.model.simple.OpaqueResource)
			return ((org.ogema.core.model.simple.OpaqueResource) resource).getValue();
		return null; // should not happen
	}

	/**
	 * Set the resource value. The <code>value</code> parameter must be parsable to the primitive (or String)
	 * value type of <code>resource</code>. For instance, if <code>resource</code> is a {@link FloatResource},
	 * then <code>value</code> must be parsable as float.
	 * @param resource
	 * @param value
	 * @throws NumberFormatException
	 */
	public static void setValue(SingleValueResource resource, String value) throws NumberFormatException {
		if (resource instanceof StringResource) {
			((StringResource) resource).setValue(value);
		}
		else if (resource instanceof FloatResource) {
			((FloatResource) resource).setValue(Float.parseFloat(value));
		}
		else if (resource instanceof IntegerResource) {
			((IntegerResource) resource).setValue(Integer.parseInt(value));
		}
		else if (resource instanceof BooleanResource) {
			((BooleanResource) resource).setValue(Boolean.parseBoolean(value));
		}
		else if (resource instanceof TimeResource) {
			((TimeResource) resource).setValue(Long.parseLong(value));
		}
	}

	/**
	 * Set the resource value; the passed float value is converted by the respective 
	 * canonical conversion method to the primitive (or String) value type of <code>resource</code>. 
	 * @param resource
	 * @param value
	 */
	public static void setValue(SingleValueResource resource, float value) {
		if (resource instanceof StringResource) {
			((StringResource) resource).setValue(String.valueOf(value));
		}
		else if (resource instanceof FloatResource) {
			((FloatResource) resource).setValue(value);
		}
		else if (resource instanceof IntegerResource) {
			((IntegerResource) resource).setValue((int) value);
		}
		else if (resource instanceof BooleanResource) {
			((BooleanResource) resource).setValue(value == 1 ? true : false);
		}
		else if (resource instanceof TimeResource) {
			((TimeResource) resource).setValue((long) value);
		}
	}

	/**
	 * Set the resource value; the passed integer value is converted by the respective 
	 * canonical conversion method to the primitive (or String) value type of <code>resource</code>. 
	 * @param resource
	 * @param value
	 */
	public static void setValue(SingleValueResource resource, int value) {
		if (resource instanceof StringResource) {
			((StringResource) resource).setValue(String.valueOf(value));
		}
		else if (resource instanceof FloatResource) {
			((FloatResource) resource).setValue(value);
		}
		else if (resource instanceof IntegerResource) {
			((IntegerResource) resource).setValue(value);
		}
		else if (resource instanceof BooleanResource) {
			((BooleanResource) resource).setValue(value == 1 ? true : false);
		}
		else if (resource instanceof TimeResource) {
			((TimeResource) resource).setValue(value);
		}
	}

	/**
	 * Set the resource value; the passed value is converted by the respective 
	 * method of {@link Value}. 
	 * @param resource
	 * @param value
	 */
	public static void setValue(SingleValueResource resource, Value value) {
		if (resource instanceof StringResource) {
			((StringResource) resource).setValue(value.getStringValue());
		}
		else if (resource instanceof FloatResource) {
			((FloatResource) resource).setValue(value.getFloatValue());
		}
		else if (resource instanceof IntegerResource) {
			((IntegerResource) resource).setValue(value.getIntegerValue());
		}
		else if (resource instanceof BooleanResource) {
			((BooleanResource) resource).setValue(value.getBooleanValue());
		}
		else if (resource instanceof TimeResource) {
			((TimeResource) resource).setValue(value.getLongValue());
		}
	}
	
	
	/**
	 * Returns a String representation of the value of <code>resource</code>.
	 * @param resource
	 */
	public static String getValue(SingleValueResource resource) {
		if (resource instanceof StringResource) {
			return ((StringResource) resource).getValue();
		}
		else if (resource instanceof TemperatureResource) {
			return String.valueOf(((TemperatureResource) resource).getCelsius()) + "°C";
		}
		else if (resource instanceof PhysicalUnitResource) {
			return String.valueOf(((PhysicalUnitResource) resource).getValue()) + " "
					+ ((PhysicalUnitResource) resource).getUnit();
		}
		else if (resource instanceof FloatResource) {
			return String.valueOf(((FloatResource) resource).getValue());
		}
		else if (resource instanceof IntegerResource) {
			return String.valueOf(((IntegerResource) resource).getValue());
		}
		else if (resource instanceof BooleanResource) {
			return String.valueOf(((BooleanResource) resource).getValue());
		}
		else if (resource instanceof TimeResource) {
			return String.valueOf(((TimeResource) resource).getValue());
		}
		else
			throw new RuntimeException();
	}
	
	/**
	 * Tries to retrieve a float representation of the value of <code>resource</code>. If this is 
	 * not possible, for instance, if <code>resource</code> is a {@link StringResource} and its value is
	 * not parsable as float, then a {@link NumberFormatException} is thrown. 
	 * @param resource
	 * @throws NumberFormatException
	 */
	public static float getFloatValue(SingleValueResource resource) throws NumberFormatException {
		if (resource instanceof FloatResource) {
			return ((FloatResource) resource).getValue();
		}
		else if (resource instanceof IntegerResource) {
			return ((IntegerResource) resource).getValue();
		}
		else if (resource instanceof BooleanResource) {
			return (((BooleanResource) resource).getValue() ? 1 : 0);
		}
		else if (resource instanceof TimeResource) {
			return ((TimeResource) resource).getValue();
		}
		else if (resource instanceof StringResource) {
			return Float.parseFloat(((StringResource) resource).getValue());  // throws NumberFormatException
		}
		else 
			throw new RuntimeException();
	}

	/** 
	 * Get a String representation of a {@link FloatResource} value that is suitable for human reading.
	 * This is similar to {@link #getValue(SingleValueResource)}, but allows in addition to specify a 
	 * maximum number of decimals.
	 */
	public static String getValue(FloatResource resource, int maxDecimals) {
		String format = "%." + maxDecimals + "f";
		if (resource instanceof TemperatureResource) {
			return String.format(Locale.ENGLISH,format + "°C", ((TemperatureResource) resource).getCelsius());
		}
		else if (resource instanceof PhysicalUnitResource) {
			return String.format(Locale.ENGLISH,format + " " + ((PhysicalUnitResource) resource).getUnit(), resource.getValue());
		}
		else
			return String.format(Locale.ENGLISH,format, resource.getValue());
	}
	
	
	/**
	 * Returns the size of a generic array resource, or -1 if the array is a virtual resource (does not exist)
	 * @param array
	 * @return
	 */
	public static int getSize(ArrayResource array) {
		if (!array.exists())
			return -1;
		if (array instanceof IntegerArrayResource)
			return ((IntegerArrayResource) array).size();
		else if (array instanceof FloatArrayResource)
			return ((FloatArrayResource) array).size();
		else if (array instanceof BooleanArrayResource)
			return ((BooleanArrayResource) array).size();
		else if (array instanceof TimeArrayResource)
			return ((TimeArrayResource) array).size();
		else if (array instanceof StringArrayResource)
			return ((StringArrayResource) array).size();
		else if (array instanceof ByteArrayResource)
			return ((ByteArrayResource) array).size();
		else 
			throw new IllegalArgumentException(); // cannot occur
	}
	
	/**
	 * Returns a primitive array of the value type appropriate for the ArrayResources 
	 * (i.e. it returns int[] for IntegerArrayResources, float[] for FloatArrayResource, etc.). 
	 * @param array
	 * @param idx
	 * @return
	 * 		the primitive array, or null if array is virtual
	 * @throws IndexOutOfBoundsException
	 * 		if idx &lt; 0 or idx &gt;= array size
	 */
	public static Object getValue(ArrayResource array, int idx) throws IndexOutOfBoundsException {
		if (!array.exists())
			return null;
		if (idx < 0 || idx >= getSize(array))
			throw new IndexOutOfBoundsException();
		Object obj = getValue(array);
		if (array instanceof IntegerArrayResource)
			return ((int[]) obj)[idx];
		else if (array instanceof FloatArrayResource)
			return ((float[]) obj)[idx];
		else if (array instanceof BooleanArrayResource)
			return ((boolean[]) obj)[idx];
		else if (array instanceof TimeArrayResource)
			return ((long[]) obj)[idx];
		else if (array instanceof StringArrayResource)
			return ((String[]) obj)[idx];
		else if (array instanceof ByteArrayResource)
			return ((byte[]) obj)[idx];
		else 
			throw new IllegalArgumentException(); // cannot occur
	}
	
	/**
	 * Check if the array resource contains a String. This returns false if the resource is inactive.
	 * @param array
	 * 		may not be null
	 * @param string
	 * 		may be null, in which case false is returned
	 * @return
	 */
	public static boolean contains(StringArrayResource array, String string) {
		Objects.requireNonNull(array);
		if (string == null)
			return false;
		return array.isActive() && Arrays.asList(array.getValues()).contains(string); 
	}
	
	/**
	 * Check if the array resource contains a float value. This returns false if the resource is inactive.
	 * @param array
	 * @param value
	 * @param delta
	 * 		non-negative
	 * @return
	 * @throws IllegalArgumentException
	 * 		if delta &lt; 0
	 */
	public static boolean contains(FloatArrayResource array, float value, float delta) throws IllegalArgumentException {
		Objects.requireNonNull(array);
		if (delta < 0)
			throw new IllegalArgumentException("delta < 0: " + delta);
		if (!array.isActive())
			return false;
		for (float f : array.getValues()) { 
			if (delta == 0F) {
				if (value == f)  
					return true;
			}
			else if (Math.abs(f-value) < delta)
				return true;
		}
		return false;
	}
	
	/**
	 * Check if the array resource contains an int value. This returns false if the resource is inactive.
	 * @param array
	 * @param value
	 * @return
	 */
	public static boolean contains(IntegerArrayResource array, int value) {
		Objects.requireNonNull(array);
		if (!array.isActive())
			return false;
		for (int i : array.getValues()) {
			if (i == value)
				return true;
		}
		return false;
	}
	
	/**
	 * Check if the array resource contains a long value. This returns false if the resource is inactive.
	 * @param array
	 * @param value
	 * @return
	 */
	public static boolean contains(TimeArrayResource array, long value) {
		Objects.requireNonNull(array);
		if (!array.isActive())
			return false;
		for (long i : array.getValues()) {
			if (i == value)
				return true;
		}
		return false;
	}
	
	/**
	 * Replace one value of an ArrayResource or append one value to it. 
	 * If you want to replace all values at once, use {@link #setValue(ValueResource, Object)} instead. 
	 * 
	 * @param array
	 * 		ArrayResource for which a new value is to be set. 
	 * @param idx
	 * 		if this is <code> &lt; array.size()</code>, then the respective value is replaced, if it is equal to <code>array.size()</code>, the value is appended
	 * @param object
	 * 		the value to set. Must match the array type. A String convertible to the target type is fine, too.
	 * @return
	 * 		true, if value has been successfully replaced or appended; false, if the array resource does not exist, or the index is too large (or &lt; 0).
	 */
	public static boolean setValue(ArrayResource array, int idx, Object object) {
		if (!array.exists())
			return false;
		int sz = getSize(array);
		if (sz < idx || idx < 0)
			return false;
		if (array instanceof IntegerArrayResource) {
			int[] vals = ((IntegerArrayResource) array).getValues();
			int target;
			if (object instanceof String)
				target = Integer.parseInt((String) object);
			else
				target = (int) object;
			if (idx < sz) {
				vals[idx] = target;
				((IntegerArrayResource) array).setValues(vals);
			} else {
				int[] newVals = new int[sz+1];
				System.arraycopy(vals, 0, newVals, 0, sz);
				newVals[sz] = target;
				((IntegerArrayResource) array).setValues(newVals);
			}
			return true;
		}
		else if (array instanceof FloatArrayResource) {
			float[] vals = ((FloatArrayResource) array).getValues();
			float target;
			if (object instanceof String)
				target = Float.parseFloat((String) object);
			else
				target = (float) object;
			if (idx < sz) {
				vals[idx] = target;
				((FloatArrayResource) array).setValues(vals);
			} else {
				float[] newVals = new float[sz+1];
				System.arraycopy(vals, 0, newVals, 0, sz);
				newVals[sz] = target;
				((FloatArrayResource) array).setValues(newVals);
			}
			return true;
		}
		else if (array instanceof BooleanArrayResource) {
			boolean[] vals = ((BooleanArrayResource) array).getValues();
			boolean target;
			if (object instanceof String)
				target = Boolean.parseBoolean((String) object);
			else
				target = (boolean) object;
			if (idx < sz) {
				vals[idx] = target;
				((BooleanArrayResource) array).setValues(vals);
			} else {
				boolean[] newVals = new boolean[sz+1];
				System.arraycopy(vals, 0, newVals, 0, sz);
				newVals[sz] = target;
				((BooleanArrayResource) array).setValues(newVals);
			}	
			return true;
		}
		else if (array instanceof TimeArrayResource) {
			long[] vals = ((TimeArrayResource) array).getValues();
			long target;
			if (object instanceof String)
				target = Long.parseLong((String) object);
			else
				target = (long) object;
			if (idx < sz) {
				vals[idx] = target;
				((TimeArrayResource) array).setValues(vals);
			} else {
				long[] newVals = new long[sz+1];
				System.arraycopy(vals, 0, newVals, 0, sz);
				newVals[sz] = target;
				((TimeArrayResource) array).setValues(newVals);
			}		
			return true;
		}
		else if (array instanceof StringArrayResource) {
			String[] vals = ((StringArrayResource) array).getValues();
			if (idx < sz) {
				vals[idx] = (String) object;
				((StringArrayResource) array).setValues(vals);
			} else {
				String[] newVals = new String[sz+1];
				System.arraycopy(vals, 0, newVals, 0, sz);
				newVals[sz] = (String) object;
				((StringArrayResource) array).setValues(newVals);
			}		
			return true;
		}
		else if (array instanceof ByteArrayResource) {
			byte[] vals = ((ByteArrayResource) array).getValues();
			byte target;
			if (object instanceof String)
				target = Byte.parseByte((String) object);
			else
				target = (byte) object;
			if (idx < sz) {
				vals[idx] = target;
				((ByteArrayResource) array).setValues(vals);
			} else {
				byte[] newVals = new byte[sz+1];
				System.arraycopy(vals, 0, newVals, 0, sz);
				newVals[sz] = target;
				((ByteArrayResource) array).setValues(newVals);
			}	
			return true;
		}
		else 
			throw new IllegalArgumentException(); // cannot occur
		
	}
	
	/**
	 * Append an element to the array.
	 * @param array
	 * @param object
	 * @return
	 */
	public static boolean appendValue(ArrayResource array, Object object) {
		int sz = getSize(array);
		if (sz < 0)
			return false;
		return setValue(array, sz, object);
	}
	
	/**
	 * Remove an element from an ArrayResource, at the specified index.
	 * @param array
	 * @param idx
	 * @return
	 */
	public static boolean removeElement(ArrayResource array, int idx) {
		int sz = getSize(array);
		if (idx < 0 || idx >= sz)
			return false;
		Object value = getValue(array);
		if (array instanceof IntegerArrayResource) {
			int[] values = (int[]) value;
			sz= values.length; // just to be sure it hasn't changed in the meantime
			int[] newValues = new int[sz-1]; 
			if (idx > 0)
				System.arraycopy(values, 0, newValues, 0, idx);
			if (idx < sz-1) 
				System.arraycopy(values, idx+1, newValues, idx, sz-1-idx);
			setValue(array, newValues);
		}
		else if (array instanceof FloatArrayResource) {
			float[] values = (float[]) value;
			sz= values.length; // just to be sure it hasn't changed in the meantime
			float[] newValues = new float[sz-1]; 
			if (idx > 0)
				System.arraycopy(values, 0, newValues, 0, idx);
			if (idx < sz-1) 
				System.arraycopy(values, idx+1, newValues, idx, sz-1-idx);
			setValue(array, newValues);
		}
		else if (array instanceof BooleanArrayResource){
			boolean[] values = (boolean[]) value;
			sz= values.length; // just to be sure it hasn't changed in the meantime
			boolean[] newValues = new boolean[sz-1]; 
			if (idx > 0)
				System.arraycopy(values, 0, newValues, 0, idx);
			if (idx < sz-1) 
				System.arraycopy(values, idx+1, newValues, idx, sz-1-idx);
			setValue(array, newValues);
		}
		else if (array instanceof TimeArrayResource){
			long[] values = (long[]) value;
			sz= values.length; // just to be sure it hasn't changed in the meantime
			long[] newValues = new long[sz-1]; 
			if (idx > 0)
				System.arraycopy(values, 0, newValues, 0, idx);
			if (idx < sz-1) 
				System.arraycopy(values, idx+1, newValues, idx, sz-1-idx);
			setValue(array, newValues);
		}
		else if (array instanceof StringArrayResource){
			String[] values = (String[]) value;
			sz= values.length; // just to be sure it hasn't changed in the meantime
			String[] newValues = new String[sz-1]; 
			if (idx > 0)
				System.arraycopy(values, 0, newValues, 0, idx);
			if (idx < sz-1) 
				System.arraycopy(values, idx+1, newValues, idx, sz-1-idx);
			setValue(array, newValues);
		}
		else if (array instanceof ByteArrayResource){
			byte[] values = (byte[]) value;
			sz= values.length; // just to be sure it hasn't changed in the meantime
			byte[] newValues = new byte[sz-1]; 
			if (idx > 0)
				System.arraycopy(values, 0, newValues, 0, idx);
			if (idx < sz-1) 
				System.arraycopy(values, idx+1, newValues, idx, sz-1-idx);
			setValue(array, newValues);
		}
		else 
			throw new IllegalArgumentException(); // cannot occur
		return true;
	}
	
	/**
	 * Shifts the values of a temperature schedule by 273.15 (from Kelvin to Celsius)
	 * 
	 * @param readOnlyTimeSeries
	 * @return
	 */
	public static MemoryTimeSeries getTemperatureScheduleInCelsius(ReadOnlyTimeSeries readOnlyTimeSeries) {
		return affineTransformation(readOnlyTimeSeries,1,-273.15F);
	}
	
	/**
	 * Multiplies each value of <code>schedule</code> with <code>factor</code> and adds <code>addend</code>
	 * 
	 * @param readOnlyTimeSeries
	 * @param factor
	 * @param addend
	 * @return
	 */
	public static MemoryTimeSeries affineTransformation(ReadOnlyTimeSeries readOnlyTimeSeries, float factor, float addend) {
		TreeTimeSeries tts = new TreeTimeSeries(FloatValue.class);
		tts.setInterpolationMode(readOnlyTimeSeries.getInterpolationMode());		
		final Iterator<SampledValue> it = readOnlyTimeSeries.iterator();
		SampledValue value;
		while (it.hasNext()) {
			value = it.next();
			tts.addValue(new SampledValue(new FloatValue(value.getValue().getFloatValue()*factor + addend), 
					value.getTimestamp(), value.getQuality()));
		}
		return tts;
	}
	
	/**
	 * Get a reduced set of points for the specified interval [t0,t1], obtained by downsampling
	 * the original set to a minimum time interval between adjacent points.
	 * @param schedule
	 * @param t0
	 * @param t1
	 * @param minimumInterval
	 * @return
	 */
	public static List<SampledValue> downsample(ReadOnlyTimeSeries schedule, long t0, long t1, long minimumInterval) {
		FloatTimeSeries fts = new FloatTreeTimeSeries();
		fts.readWithBoundaries(schedule, t0, t1);
		return fts.downsample(t0, t1, minimumInterval);
	}
	
	/**
	 * Integrate a time series over the specified domain, taking into account the interpolation mode. 
	 * @see FloatTimeSeries#integrate(long, long)
	 * @see FloatTimeSeries
	 * 
	 * @param schedule
	 * @param startTime
	 * @param endTime
	 * @return Integral, with time measured in ms (not in seconds).
	 * @throws UnsupportedOperationException
	 * 		if no interpolation mode is set for the schedule
	 */
	public static float integrate(ReadOnlyTimeSeries schedule, long startTime, long endTime) {
		return integrate(schedule, startTime, endTime, null);
	}
	
	/**
	 * Like {@link #integrate(ReadOnlyTimeSeries, long, long)}, but with an explicitly set interpolation mode.
	 * @param schedule
	 * @param startTime
	 * @param endTime
	 * @param mode
	 * 		Interpolation mode for integration. If null, the default schedule interpolation mode is used.
	 * @return
	 */
	public static float integrate(ReadOnlyTimeSeries schedule, long startTime, long endTime, InterpolationMode mode) {
		FloatTimeSeries fts = new FloatTreeTimeSeries();
		fts.readWithBoundaries(schedule, startTime, endTime);
		if (mode != null)
			fts.setInterpolationMode(mode);
		return fts.integrate(startTime, endTime);
	}
	

	/**
	 * Returns an average value for the time series on the specified interval; depending on the interpolation mode,
	 * this is either calculated as an integral divided by the length of the interval, or 
	 * simply as the arithmetic average of all points in the interval (if interpolation mode is NONE). <br>
	 * 
	 * If the interval does not contain any values (even by interpolation), then <code>Float.NaN</code> is returned.
	 * @param schedule
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static float getAverage(ReadOnlyTimeSeries schedule, long startTime, long endTime) {
		if (endTime == startTime) {
			SampledValue sv = schedule.getValue(startTime);
			if (sv == null || sv.getQuality() == Quality.BAD)
				return Float.NaN;
			else 
				return sv.getValue().getFloatValue();
		}
		else if (startTime > endTime)
			return getAverage(schedule, endTime, startTime);
		if (schedule.getInterpolationMode() == InterpolationMode.NONE) {
			int count = 0;
			float val = 0;
			final Iterator<SampledValue> it = schedule.iterator(startTime, endTime);
			SampledValue sv;
			while (it.hasNext()) {
				sv = it.next();
				if (sv.getQuality() != Quality.BAD) {
					count++;
					val += sv.getValue().getFloatValue();
				}
			}
			if (count == 0)
				return Float.NaN;
			else 
				return val / count; 
		}
		return integrate(schedule, startTime, endTime) / (endTime - startTime);
	}
	
	/**
	 * Like {@link ReadOnlyTimeSeries#getValues(long, long)}, except that the resulting list is guaranteed to contain 
	 * data points for the two boundary points <tt>startTime</tt> and <tt>endTime</tt>. If the time series is not defined at the boundaries,
	 * bad quality values are returned there.
	 * @param schedule
	 * @param startTime
	 * @param endTime
	 * @return
	 * 		a list with at least two entries (if <tt>endTime &gt; startTime</tt>) for the two boundary points
	 * @throws IllegalArgumentException 
	 * 		if <code>endTime &lt; startTime</code>
	 */
	public static List<SampledValue> getValuesWithBoundaries(ReadOnlyTimeSeries schedule, long startTime, long endTime) throws IllegalArgumentException {
		Objects.requireNonNull(schedule);
		if (startTime > endTime)
			throw new IllegalArgumentException("Start time smaller than end time: start time: " + startTime + ", end time: " + endTime);
		if (startTime == endTime) {
			SampledValue val = getValueSafe(schedule, startTime);
			return Collections.singletonList(val);
		}
		final List<SampledValue> values = schedule.getValues(startTime, endTime);
		if (values.isEmpty() || values.get(0).getTimestamp() > startTime) {
			values.add(getValueSafe(schedule, startTime));
			Collections.sort(values);
		}
		if (values.isEmpty() || values.get(values.size()-1).getTimestamp() < endTime) 
			values.add(getValueSafe(schedule, endTime));
		return values;
	}
	
	/**
	 * Like {@link ReadOnlyTimeSeries#getValue(long)}, but never returns null.
	 * If the schedule is not defined at <tt>time</tt>, a bad quality value is returned.
	 * @param schedule
	 * @param time
	 * @return
	 */
	public static SampledValue getValueSafe(ReadOnlyTimeSeries schedule, long time) {
		SampledValue val = schedule.getValue(time);
		if (val == null)
			val = new SampledValue(new FloatValue(Float.NaN), time, Quality.BAD);
		return val;
	}
	
	/**
	 * Get the time series value at a certain point in time, as if it had interpolation mode <tt>mode</tt>
	 * @param timeSeries
	 * @param time
	 * @param mode
	 * @return
	 */
	public static SampledValue getValueForInterpolationMode(ReadOnlyTimeSeries timeSeries, long time, InterpolationMode mode) {
		if (mode == timeSeries.getInterpolationMode())
			return timeSeries.getValue(time);
		if (!isInScheduleRange(timeSeries, time, mode)) 
			return null;
		switch (mode) {
		case NONE:
			List<SampledValue> values = timeSeries.getValues(time, time+1);
			if (values.isEmpty())
				return new SampledValue(new FloatValue(Float.NaN), time, Quality.BAD);
			return values.get(0);
		case STEPS:
			SampledValue previous = timeSeries.getPreviousValue(time);
			return new SampledValue(new FloatValue(previous.getValue().getFloatValue()), time, previous.getQuality());
		case NEAREST:
			previous = timeSeries.getPreviousValue(time);
			SampledValue next = timeSeries.getNextValue(time);
			if (previous == null)
				return next;
			if (next == null)
				return previous;
			long d1 = time-previous.getTimestamp();
			long d2 = next.getTimestamp()-time;
			SampledValue toBeCopied = (d1 <= d2 ? previous: next);
			return new SampledValue(new FloatValue(toBeCopied.getValue().getFloatValue()), time, toBeCopied.getQuality());
		case LINEAR:
			previous = timeSeries.getPreviousValue(time);
			next = timeSeries.getNextValue(time);
			if (previous == null || next  == null)
				return null;
			long t0 = time - previous.getTimestamp();
			if (t0 == 0)
				return previous;
			long t1 = next.getTimestamp() - time;
			if (t1 == 0)
				return next;
			float diff = next.getValue().getFloatValue()-previous.getValue().getFloatValue();
			Quality qual = (previous.getQuality() == Quality.GOOD && next.getQuality() == Quality.GOOD ? Quality.GOOD : Quality.BAD);
			return new SampledValue(new FloatValue(previous.getValue().getFloatValue() + diff * t0 / (t0 + t1)), time, qual);
		default: 
			throw new RuntimeException();
		}
	}
	
	/**
	 * Check whether a point in time would be in the time series range if the latter had interpolation mode <tt>mode</tt>
	 * @param timeSeries
	 * @param time
	 * @param mode
	 * @return
	 */
	public static boolean isInScheduleRange(ReadOnlyTimeSeries timeSeries, long time, InterpolationMode mode) {
		if (timeSeries.isEmpty())
			return false;
		switch (mode) {
		case NEAREST:
			return true;
		case STEPS:
			return time >= timeSeries.getNextValue(Long.MIN_VALUE).getTimestamp();
		case LINEAR:
		case NONE:
			return time >= timeSeries.getNextValue(Long.MIN_VALUE).getTimestamp()
					&& time <= timeSeries.getPreviousValue(Long.MAX_VALUE).getTimestamp();
		default:
			throw new RuntimeException();
		}
	}
	
    /**
     * Gets the value type for a schedule by looking at its parent resource.
     */
    public static Class<? extends Value> getValueType(Schedule schedule) {
        Resource parent = schedule.getParent(); // must be either a single value resource or resource list
        while (parent instanceof ResourceList)
        	parent = parent.getParent();
        if (parent == null) 
            throw new IllegalStateException("Schedule at path " + schedule.getPath() 
            	+ " does not seem to have a valid parent. Cannot determine the type of elements. OGEMA schedules must always have a simple non-array parent resource.");
        if (!(parent instanceof SingleValueResource))
        	throw new RuntimeException("Parent of schedule " + schedule.getPath() 
        	+ " is not a SingleValueResource, cannot determine its type");
        if (parent instanceof FloatResource)
            return FloatValue.class;
        else if (parent instanceof IntegerResource) 
            return IntegerValue.class;
        else if (parent instanceof BooleanResource) 
            return BooleanValue.class;
        else if (parent instanceof StringResource) 
            return StringValue.class;
        else if (parent instanceof TimeResource) 
            return LongValue.class;
        else 
        	throw new IllegalStateException("Illegal type " + parent.getResourceType().getName());
    }

    /**
     * Create a list of sampled values from the arrays of values and timestamps.
     * Both arrays must have the same size.
     * @param values
     * @param timestamps
     * @return
     */
    public static List<SampledValue> getSampledValues(float[] values, long[] timestamps) {
    	if (values.length != timestamps.length)
    		throw new IllegalArgumentException("Array length must coincide. Got length " + values.length + " and " + timestamps.length);
    	List<SampledValue> v = new ArrayList<>();
    	for (int i=0;i<values.length;i++) {
    		v.add(new SampledValue(new FloatValue(values[i]), timestamps[i], Quality.GOOD));
    	}
    	return v;
    }
    
    /**
     * @see ValueResourceUtils#getSampledValues(float[], long[])
     */
    public static List<SampledValue> getSampledValues(int[] values, long[] timestamps) {
    	if (values.length != timestamps.length)
    		throw new IllegalArgumentException("Array length must coincide. Got length " + values.length + " and " + timestamps.length);
    	List<SampledValue> v = new ArrayList<>();
    	for (int i=0;i<values.length;i++) {
    		v.add(new SampledValue(new IntegerValue(values[i]), timestamps[i], Quality.GOOD));
    	}
    	return v;
    }
    
    /**
     * @see ValueResourceUtils#getSampledValues(float[], long[])
     */
    public static List<SampledValue> getSampledValues(boolean[] values, long[] timestamps) {
    	if (values.length != timestamps.length)
    		throw new IllegalArgumentException("Array length must coincide. Got length " + values.length + " and " + timestamps.length);
    	List<SampledValue> v = new ArrayList<>();
    	for (int i=0;i<values.length;i++) {
    		v.add(new SampledValue(new BooleanValue(values[i]), timestamps[i], Quality.GOOD));
    	}
    	return v;
    }
    
    /**
     * See {@link #interpolate(SampledValue, SampledValue, long, InterpolationMode, boolean)}.
     * The default value <tt>badQualityForUndefined = false</tt> is used here.
     * @param previous
     * @param next
     * @param t
     * @param mode
     * @return
     */
    public static SampledValue interpolate(SampledValue previous, SampledValue next, long t, InterpolationMode mode) {
    	return interpolate(previous, next, t, mode, false);
    }
    
    /**
     * Interpolate between two {@link SampledValue}s, based on the provided {@link InterpolationMode}.
     * @param previous
     * 		may be null
     * @param next
     * 		may be null
     * @param t
     * 		the timestamp for which the interpolated value is requested
     * @param mode
     * 		the interpolation mode
     * @param badQualityForUndefined
     * 		if true, a sampled value with bad quality is returned in case the requested value
     * 		is undefined (e.g. one of the two sampled values is null, but an interpolation is
     * 		required). If the argument is false, null is returned in this case.
     * @return
     */
    public static SampledValue interpolate(final SampledValue previous, final SampledValue next, final long t, 
    			final InterpolationMode mode, final boolean badQualityForUndefined) {
    	if (previous == null && next == null)
    		return badQualityForUndefined ? new SampledValue(FloatValue.NAN, t, Quality.BAD) : null;
    	if (previous != null && previous.getTimestamp() == t)
    		return previous;
    	if (next != null && next.getTimestamp() == t)
    		return next;
    	switch (mode) {
		case STEPS:
			if (previous == null)
				return badQualityForUndefined ? new SampledValue(FloatValue.NAN, t, Quality.BAD) : null;
			return new SampledValue(previous.getValue(), t, previous.getQuality());
		case LINEAR:
			if (previous == null || next == null)
				return badQualityForUndefined ? new SampledValue(FloatValue.NAN, t, Quality.BAD) : null;
			final float p = previous.getValue().getFloatValue();
			final float n = next.getValue().getFloatValue();
			final long tp = previous.getTimestamp();
			final long tn = next.getTimestamp();
			float newV = p + (n-p)*(t-tp)/(tn-tp);
			return new SampledValue(new FloatValue(newV), t, 
				previous.getQuality() == Quality.GOOD && next.getQuality() == Quality.GOOD ? Quality.GOOD : Quality.BAD);
		case NEAREST:
			if (previous == null && next == null)
				return badQualityForUndefined ? new SampledValue(FloatValue.NAN, t, Quality.BAD) : null;
			final Long tp2 = (previous != null ? previous.getTimestamp() : null);
			final Long tn2 = (next != null ? next.getTimestamp() : null);
			final SampledValue sv = (tp2 == null ? next : tn2 == null ? previous : (t-tp2)<=(tn2-t) ? previous : next);
			return new SampledValue(sv.getValue(), t, sv.getQuality());
		default: // NONE and null
			return badQualityForUndefined ? new SampledValue(FloatValue.NAN, t, Quality.BAD) : null;
		}
    }
    
    
    /**
     * Find maximum value in time series
     * 
     * @param timeSeries
     * @param startTime
     * @param endTime
     * @return maximum value or null if no value is found
     */
    public static SampledValue getMax(ReadOnlyTimeSeries timeSeries, long startTime, long endTime) {
    	float max = -Float.MAX_VALUE;
    	Long t = null; 
    	final Iterator<SampledValue> it = timeSeries.iterator(startTime, endTime);
    	SampledValue val;
    	while (it.hasNext()) {
    		val = it.next();
    		if (val.getQuality() == Quality.BAD) continue;
    		float fval = val.getValue().getFloatValue();
    		if (fval > max) {
    			max = fval;
    			t = val.getTimestamp();
    		}
    	}
    	return t != null ? new SampledValue(new FloatValue(max), t, Quality.GOOD) : null;
    }
    
    /**
     * Find the minimum value in time series
     * 
     * @param timeSeries
     * @param startTime
     * @param endTime
     * @return maximum value or null if no value is found
     */
    public static SampledValue getMin(ReadOnlyTimeSeries timeSeries, long startTime, long endTime) {
    	float min = Float.MAX_VALUE;
    	Long t = null; 
    	final Iterator<SampledValue> it = timeSeries.iterator(startTime, endTime);
    	SampledValue val;
    	while (it.hasNext()) {
    		val = it.next();
    		if (val.getQuality() == Quality.BAD) 
    			continue;
    		float fval = val.getValue().getFloatValue();
    		if (fval < min) { 
    			min = fval;
    			t = val.getTimestamp();
    		}
    	}
    	return t != null ? new SampledValue(new FloatValue(min), t, Quality.GOOD) : null;
    }
    
    /**
     * Find maximum value in time series
     * 
     * @param timeSeries
     * @param startTime
     * @param endTime
     * @return maximum value or -Float.MAX_VALUE if no value is found
     * @deprecated use {@link #getMax(ReadOnlyTimeSeries, long, long)}, which also
     * provides information on the timestamp
     *  
     */
    @Deprecated
    public static float getMaximum(ReadOnlyTimeSeries timeSeries, long startTime, long endTime) {
    	float max = -Float.MAX_VALUE;
    	final Iterator<SampledValue> it = timeSeries.iterator(startTime, endTime);
    	SampledValue val;
    	while (it.hasNext()) {
    		val = it.next();
    		if (val.getQuality() == Quality.BAD) continue;
    		float fval = val.getValue().getFloatValue();
    		if (fval > max) max = fval;
    	}
    	return max;
    }
    
    /**
     * Find minimum value in time series
     * 
     * @param timeSeries
     * @param startTime
     * @param endTime
     * @return minmum value or Float.MAX_VALUE if time series is empty
     * @deprecated use {@link #getMin(ReadOnlyTimeSeries, long, long)}, which also
     * provides information on the timestamp
     */
    @Deprecated
    public static float getMinimum(ReadOnlyTimeSeries timeSeries, long startTime, long endTime) {
    	float min = Float.MAX_VALUE;
    	final Iterator<SampledValue> it = timeSeries.iterator(startTime, endTime);
    	SampledValue val;
    	while (it.hasNext()) {
    		val = it.next();
    		if (val.getQuality() == Quality.BAD) 
    			continue;
    		float fval = val.getValue().getFloatValue();
    		if (fval < min) 
    			min = fval;
    	}
    	return min;
    }
	
}
