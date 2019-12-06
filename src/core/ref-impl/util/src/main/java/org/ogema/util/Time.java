package org.ogema.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Time {
	public static String getTime() {
		DateFormat formatter = new SimpleDateFormat("hh:mm:ss.SSS");

		long now = System.currentTimeMillis();

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(now);

		return formatter.format(calendar.getTime());
	}

}
