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
package org.ogema.recordeddata.slotsdb;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.tools.timeseries.api.FloatTimeSeries;
import org.ogema.tools.timeseries.implementations.FloatTreeTimeSeries;
import org.ogema.tools.timeseries.iterator.api.MultiTimeSeriesIterator;
import org.ogema.tools.timeseries.iterator.api.MultiTimeSeriesIteratorBuilder;

/**
 * Helper Tool to read out slotsDb files from console
 * 
 */
public class SlotsDbReader {

	private static final int FLEXIBLE_INTERVAL = -1;
	private final static String[] header = new String[] {
		"Counter", "Date&time", "Value", "Millis" 
	};
	private final static String[] dateTimeFormats = {
			"yyyy-MM-dd'T'HH:mm:ss:SSS",
			"yyyy-MM-dd'T'HH:mm:ss",
			"yyyy-MM-dd'T'HH:mm",
			"yyyy-MM-dd'T'HH",
			"yyyy-MM-dd",
			"yyyy-MM",
			"yyyy"
		};
	

	public static void main(String[] args) throws IOException, ParseException {
		checkArguments(args);
		File mainDir = new File(args[0]);
		if (!mainDir.isDirectory()) {
			System.out.println("Unexpected input; not a directory: " + mainDir);
			return;
		}
		final List<String> argsList = Arrays.asList(args);
		final boolean persistent = !argsList.contains("-c");
		long period = -1;
		long start = Long.MIN_VALUE;
		long end = Long.MAX_VALUE;
		for (int i = 1; i< args.length; i++) {
			final String a = args[i];
			if (a.equals("-i") && i < args.length-1) {
				try {
					period = Long.parseLong(args[i+1]);
				} catch (NumberFormatException e) {
					System.err.println("Argument " + args[i+1] + " is not a long value.");
					System.exit(1);
					return;
				}
			}
			if (a.equals("-s") && i < args.length-1) {
				try {
					start = Long.parseLong(args[i+1]);
				} catch (NumberFormatException e) {
					try {
						start = parseDateString(args[i+1]);
					} catch (NullPointerException ee) {
						System.err.println("Argument " + args[i+1] + " is neither a long value, nor a date string "
								+ " in the format yyyy-MM-dd'T'HH:mm:ss:SSS");
						System.exit(1);
						return;
					}
				}
 			}
			if (a.equals("-e") && i < args.length-1) {
				try {
					end = Long.parseLong(args[i+1]);
				} catch (NumberFormatException e) {
					try {
						end = parseDateString(args[i+1]);
					} catch (NullPointerException ee) {
						System.err.println("Argument " + args[i+1] + " is neither a long value, nor a date string "
								+ " in the format yyyy-MM-dd'T'HH:mm:ss:SSS");
						System.exit(1);
						return;
					}
				}
 			}
		}
		
		final FilenameFilter directoryFilter = new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return dir.isDirectory();
			}
		};
		String[] unsortedPerDayDirs = mainDir.list(directoryFilter);
		List<String> list = Arrays.asList(unsortedPerDayDirs);
		TreeSet<String> sortedPerDayDirs = new TreeSet<>(list);

		final Set<String> resourceDirs = new HashSet<>();
		for (String dir : sortedPerDayDirs) {
			final String[] subs = new File(mainDir, dir).list(directoryFilter);
			if (subs == null)
				continue;
			resourceDirs.addAll(Arrays.asList(subs));
		}
		final FileFilter slotsFilter = new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".slots");
			}
		};
		for (String resourceDirName : resourceDirs) {
			ArrayList<String> slotList = new ArrayList<>();
			for (String s : sortedPerDayDirs) {
				File dayDir = new File(mainDir, s);
				if (!dayDir.isDirectory())
					continue;
				File resourceDir = new File(dayDir, resourceDirName);
				File[] slots = resourceDir.listFiles(slotsFilter);
				if (slots == null || slots.length == 0)
					continue;
				String path = slots[0].getPath();
				slotList.add(path);
			}
			int size = slotList.size();
			String[] slotArr = new String[size];
			if (period > 0) 
				processAndInterpolateResource(slotList.toArray(slotArr), persistent ? resourceDirName + ".csv" : null, period, start, end);
			else
				processResource(slotList.toArray(slotArr), persistent ? resourceDirName + ".csv" : null, start, end);
		}
	}
	
	public static void processAndInterpolateResource(final String[] files, final String filename, final long period, final long start, final long end) throws IOException {
		int count = 0;
		if (filename != null) {
			File f = new File(filename);
			if (f.exists()) {
				System.out.println("File " + filename + " already exists, skipping this.");
				return;
			}
		}
		final String[][] tableData;

		final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS");
		formatter.setTimeZone(TimeZone.getTimeZone("CET"));
		long startTime = 0;
		final FloatTimeSeries timeseries = new FloatTreeTimeSeries();
		for (String name : files) {
			System.out.print("Process file: " + name);
			System.out.println(" -> " + filename);
			FileObject fo = createFileObject(name);
			List<SampledValue> values = fo.readFully();
			if (startTime == 0)
				startTime = values.get(0).getTimestamp();
			if (startTime >= end)
				break;
			if (values != null) {
				timeseries.addValues(values);
			}
			else {
				System.err.println("File empty.");
			}
		}
		final MultiTimeSeriesIterator it = MultiTimeSeriesIteratorBuilder.newBuilder(Collections.singletonList(timeseries.iterator()))
			.setStepSize(start, period)
			.setGlobalInterpolationMode(InterpolationMode.LINEAR)
			.build();
		final List<SampledValue> values = new ArrayList<>();
		while (it.hasNext()) {
			final SampledValue next = it.next().getElement(0);
			if (next != null && next.getQuality() == Quality.GOOD)
				values.add(next);
		}
		tableData = new String[values.size()][4];
		int cnt = 0;
		for (SampledValue sv : values) {
			final long timestamp = sv.getTimestamp();
			final Date date = new Date(timestamp);
			tableData[cnt][0] = Integer.toString(cnt);
			tableData[cnt][1] = formatter.format(date);
			tableData[cnt][2] = Double.toString(sv.getValue().getDoubleValue()); // The value
			// itself
			long difference = (timestamp - startTime);
			tableData[cnt][3] = difference + ""; // Relative time stamp
			cnt++;
		}
		final PrintStream out = filename != null ? new PrintStream(filename, "UTF-8") : System.out;
		try {
			printRow(header, out);
			printTable(tableData, out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (filename != null) {
				out.flush();
				out.close();
			}
		}

	}

	public static void processResource(final String[] files, final String filename, final long start, final long end) throws IOException {
		int count = 0;
		if (filename != null) {
			File f = new File(filename);
			if (f.exists()) {
				System.out.println("File " + filename + " already exists, skipping this.");
				return;
			}
		}
		final PrintStream out = filename != null ? new PrintStream(filename, "UTF-8") : System.out;
		try {
			printRow(header, out);
			final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS");
			formatter.setTimeZone(TimeZone.getTimeZone("CET"));
			long startTime = 0;
			for (String name : files) {
				FileObject fo = createFileObject(name);
				List<SampledValue> values = fo.readFully();
				if (startTime == 0)
					startTime = values.get(0).getTimestamp();
				if (startTime < start)
					continue;
				if (start >= end)
					break;
				System.out.print("Process file: " + name);
				System.out.println(" -> " + filename);
				if (values != null) {
					if (values.size() != 0) {
						String[][] tblData = new String[values.size()][4];

						for (int i = 0; i < values.size(); i++) {
							long timestamp = new Long(values.get(i).getTimestamp());
							Date date = new Date(timestamp);
							tblData[i][0] = Integer.toString(count++); // Number of the value
							tblData[i][1] = formatter.format(date); // Formatted time stamp
							tblData[i][2] = Double.toString(values.get(i).getValue().getDoubleValue()); // The value
																										// itself
							long difference = (timestamp - startTime);
							tblData[i][3] = difference + ""; // Relative time stamp
						}
						printTable(tblData, out);
					}
					else {
						System.err.println("File empty.");
					}
				}
				else {
					System.err.println("File empty.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static FileObject createFileObject(String arg) throws IOException {

		File file = new File(arg);
		FileObject fo = null;

		// create a ConstantIntervalFileObject as default to access data of the file
		fo = new ConstantIntervalFileObject(file, dummyCache);

		// now check the storing period and create a FlexibleIntervalFileObject if necessary
		if (fo.getStoringPeriod() == FLEXIBLE_INTERVAL) {
			fo = new FlexibleIntervalFileObject(file, dummyCache);
		}

		return fo;
	}

	private static void checkArguments(String[] args) {
		if (args.length == 0) {
			System.err.println("Filename required");
			printUsage();
			System.exit(1);
		}
	}

	private static void printUsage() {
		System.out.println("usage: java -jar SlotsDbReader.jar <filename> <filename>...");
	}

	private static void printTable(final String[][] tblData, final PrintStream out) {
		for (String[] row : tblData) {
			printRow(row, out);
		}
	}

	private static void printRow(final String[] row, final PrintStream out) {
		for (String column : row) {
			out.print(column + " ");
		}
		out.println();
	}

	private final static Long parseDateString(final String date) {
		for (String format : dateTimeFormats) {
			final SimpleDateFormat formatter = new SimpleDateFormat(format);
			formatter.setTimeZone(TimeZone.getTimeZone("CET"));
			try {
				return formatter.parse(date).getTime();
			} catch (ParseException e) {}
		}
		return null;
	}
	
	
	
	private final static RecordedDataCache dummyCache = new RecordedDataCache() {
		
		@Override
		public void invalidate() {}
		
		@Override
		public List<SampledValue> getCache() {
			return null;
		}
		
		@Override
		public void cache(List<SampledValue> values) {}
		
	};
	
}
