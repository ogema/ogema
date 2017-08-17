package org.ogema.recordeddata.slotsdb;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeSet;

import org.ogema.core.channelmanager.measurements.SampledValue;

/**
 * Helper Tool to read out slotsDb files from console
 * 
 */
public class SlotsDbReader {

	private static final int FLEXIBLE_INTERVAL = -1;
	static SimpleDateFormat formatter;
	private static PrintStream out;

	public static void main(String[] args) throws IOException {
		File mainDir = new File(args[0]);
		if (!mainDir.isDirectory())
			return;
		String[] unsortedPerDayDirs = mainDir.list();
		List<String> list = Arrays.asList(unsortedPerDayDirs);
		TreeSet<String> sortedPerDayDirs = new TreeSet<>(list);

		File firstDayDir = new File(mainDir, sortedPerDayDirs.first());
		// URL url = firstDayDir.toURI().toURL();
		// Object content = url.getContent();
		// Files.list(firstDayDir.toPath());
		String[] resourceDirs = firstDayDir.list();
		list = Arrays.asList(resourceDirs);

		for (String resourceDirName : resourceDirs) {
			// System.out.println(resourceDirName);
			ArrayList<String> slotList = new ArrayList<>();
			for (String s : sortedPerDayDirs) {
				// System.out.println(s);
				File dayDir = new File(mainDir, s);
				if (dayDir.isFile())
					continue;
				File resourceDir = new File(dayDir, resourceDirName);
				File[] slots = resourceDir
						.listFiles(/*
									 * new FileFilter() {
									 * 
									 * @Override public boolean accept(File pathname) { System.out.println(pathname);
									 * return pathname.isFile() && pathname.getName().endsWith(".slots"); } }
									 */);
				// if (slots != null)
				String path = slots[0].getPath();
				// System.out.println(path);
				slotList.add(path);
			}
			int size = slotList.size();
			String[] slotArr = new String[size];
			processResource(slotList.toArray(slotArr), URLDecoder.decode(resourceDirName));
		}
	}

	public static void processResource(String[] args, String filename) throws IOException {
		int count = 0;
		File f = new File(filename);
		if (f.exists())
			return;
		out = new PrintStream(filename);
		try {
			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
			formatter.setTimeZone(TimeZone.getTimeZone("CET"));

			checkArguments(args);
			long startTime = 0;
			for (String name : args) {
				System.out.print("Process file: " + name);
				System.out.println(" -> " + filename);
				FileObject fo = createFileObject(name);
				List<SampledValue> values = fo.readFully();

				if (startTime == 0)
					startTime = values.get(0).getTimestamp();
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
						printTable(tblData);
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
		fo = new ConstantIntervalFileObject(file);

		// now check the storing period and create a FlexibleIntervalFileObject if necessary
		if (fo.getStoringPeriod() == FLEXIBLE_INTERVAL) {
			fo = new FlexibleIntervalFileObject(file);
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

	private static void printTable(String[][] tblData) {
		for (String[] row : tblData) {
			printRow(row);
		}
	}

	private static void printRow(String[] row) {
		for (String column : row) {
			out.print(column + " ");
		}
		out.println();
	}

}
