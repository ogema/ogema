package org.ogema.recordeddata.slotsdb;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.recordeddata.slotsdb.ConstantIntervalFileObject;
import org.ogema.recordeddata.slotsdb.FileObject;
import org.ogema.recordeddata.slotsdb.FlexibleIntervalFileObject;

/**
 * Helper Tool to read out slotsDb files from console
 * 
 */
public class SlotsDbReader {

	private static final int FLEXIBLE_INTERVAL = -1;
	static SimpleDateFormat formatter;

	public static void main(String[] args) throws IOException {
		int count = 0;

		try {
			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
			formatter.setTimeZone(TimeZone.getTimeZone("CET"));

			checkArguments(args);
			for (String name : args) {
				FileObject fo = createFileObject(name);
				List<SampledValue> values = fo.readFully();

				if (values != null) {
					if (values.size() != 0) {
						String[][] tblData = new String[values.size()][3];

						for (int i = 0; i < values.size(); i++) {
							Long timestamp = new Long(values.get(i).getTimestamp());
							Date date = new Date(timestamp);
							tblData[i][0] = Integer.toString(count++);
							tblData[i][1] = formatter.format(date);
							tblData[i][2] = Double.toString(values.get(i).getValue().getDoubleValue());
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
			System.out.print(column + " ");
		}
		System.out.println();
	}

}
