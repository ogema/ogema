package org.ogema.tools.resource.util;

import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.model.units.PhysicalUnitResource;
import org.ogema.core.model.units.TemperatureResource;

public class ValueResourceUtils {

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

	public static String getValue(FloatResource resource, int maxDecimals) {
		String format = "%." + maxDecimals + "f";
		if (resource instanceof TemperatureResource) {
			return String.format(null, ((TemperatureResource) resource).getCelsius(), format) + "°C";
		}
		else if (resource instanceof PhysicalUnitResource) {
			return String.format(null, resource.getValue(), format) + " " + ((PhysicalUnitResource) resource).getUnit();
		}
		else
			return String.format(null, resource.getValue(), format);
	}

}
