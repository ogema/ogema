package org.ogema.tools.timeseriesimport.impl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.Parameter;
import org.ogema.core.administration.FrameworkClock;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.tools.timeseriesimport.api.ImportConfigurationBuilder;
import org.ogema.tools.timeseriesimport.api.TimeseriesImport;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
		service=ShellCommands.class, 
		immediate=false,
		property= {
				"osgi.command.scope=ogm",
				"osgi.command.function=importTsCsv",
				"osgi.command.function=writeTsCsv"
		}
)
public class ShellCommands {
	
	private static final String[] DATE_TIME_PATTERNS = {
		"yyy-MM-dd'T'HH:mm:ss.fff", //?
		"yyy-MM-dd'T'HH:mm:ss",
		"yyy-MM-dd'T'HH:mm",
		"yyy-MM-dd'T'HH",
		"yyy-MM-dd",
		"yyy-MM"
	};
	
	@Reference
	private FrameworkClock clock;
	@Reference
	private TimeseriesImport importer;
	
	@Descriptor("Import timeseries from CSV file")
	public ReadOnlyTimeSeries importTsCsv(
			@Descriptor("Import values lazily, i.e. only on demand. Default value: false.")
			@Parameter(names= {"-l", "--lazyimport"}, absentValue="false", presentValue="true")
			final boolean lazy,
			@Descriptor("Factor to multiply values with. Default 1.")
			@Parameter(names= {"-f", "--factor"}, absentValue="1")
			float factor,
			@Descriptor("Addend to add to values. Default 0.")
			@Parameter(names= {"-a", "--addend"}, absentValue="0")
			float addend,
			@Descriptor("Index of time column (0-based), default: 0. If no time column is present set start time (-s), interval (-i) and value index (-v) instead.")
			@Parameter(names= {"-t", "--timeidx"}, absentValue="0")
			int timeIndex,
			@Descriptor("Index of values column (0-based), default: 1")
			@Parameter(names= {"-v", "--valueidx"}, absentValue="1")
			int valueIndex,
			@Descriptor("Start time, in millis since epoch or in ISO format 'yyyy-MM-dd'T'HH:mm:ss'; UTC time zone")
			@Parameter(names= {"-s", "--start"}, absentValue="now")
			String startTime,
			@Descriptor("Time interval in ms")
			@Parameter(names= {"-i", "--interval"}, absentValue="-1")
			long millis,
			@Descriptor("Interpolation mode, e.g. \"LINEAR\", \"STEPS\", \"NEAREST\" or \"NONE\" (default).")
			@Parameter(names= {"-m", "--mode"}, absentValue="NONE")
			InterpolationMode mode,
			@Descriptor("URL, such as file:test.csv ('file:' prefix is default and can be omitted)")
			String url) throws MalformedURLException, IOException {
		final StringBuilder sb = new StringBuilder();
		if (url.indexOf(':') < 0)
			sb.append("file:");
		sb.append(url);
		final ImportConfigurationBuilder builder = ImportConfigurationBuilder.newInstance();
		if (millis > 0) {
			builder.setTimesteps(valueIndex, parseTimestamp(startTime), millis);
		} else {
			builder.setTimeAndValueIndices(timeIndex, valueIndex);
		}
		builder.setFactor(factor).setAddend(addend).setParseEagerly(!lazy).setInterpolationMode(mode);
		return importer.parseCsv(new URL(sb.toString()), builder.build());
	}
	
	// TODO separator configurable
	@Descriptor("Write timeseries to CSV file. Returns nr of values written.")
	public int writeTsCsv(
			@Descriptor("Start time, in millis since epoch or in ISO format 'yyyy-MM-dd'T'HH:mm:ss', or String \"now\"; UTC time zone. Default: Long.MIN_VALUE.")
			@Parameter(names= {"-s", "--start"}, absentValue="")
			String startTime,
			@Descriptor("End time, in millis since epoch or in ISO format 'yyyy-MM-dd'T'HH:mm:ss', or String \"now\"; UTC time zone. Default: Long.MAX_VALUE.")
			@Parameter(names= {"-e", "--end"}, absentValue="")
			String endTime,
			@Descriptor("Suppress timestamps? Default: false.")
			@Parameter(names= {"-nt", "--notime"}, absentValue="false", presentValue="true")
			final boolean suppressTime,
			@Descriptor("Time format, such as \"yyyy-MM-dd'T'HH:mm\". May be empty (default), in which case milliseconds since epoch are written")
			@Parameter(names= {"-tf", "--timeformat"}, absentValue="")
			final String timeFormat,
			@Descriptor("Append to file if it exists? Otherwise a potentially existing file is overwritten. Default: false.")
			@Parameter(names= {"-a", "--append"}, absentValue="false", presentValue="true")
			final boolean append,
			@Descriptor("The timeseries to write")
			final ReadOnlyTimeSeries timeSeries,
			@Descriptor("URL, such as file:test.csv ('file:' prefix is default and can be omitted)")
			final String url
			) throws IOException, URISyntaxException {
		Objects.requireNonNull(timeSeries);
		final StringBuilder sb = new StringBuilder();
		if (url.indexOf(':') < 0)
			sb.append("file:");
		sb.append(url);
		final long start = parseTimestamp(startTime, Long.MIN_VALUE);
		final long end = parseTimestamp(endTime, Long.MAX_VALUE);
		final SimpleDateFormat df = timeFormat.isEmpty() ? null : new SimpleDateFormat(timeFormat, Locale.ENGLISH);
		final URL urlObj = new URL(sb.toString());
		final BufferedWriter writer0;
		if ("file".equalsIgnoreCase(urlObj.getProtocol())) {
			final OpenOption[] opts = append ? new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND} :
				 new OpenOption[]{StandardOpenOption.CREATE} ;
			writer0 = Files.newBufferedWriter(Paths.get(urlObj.getPath()), StandardCharsets.UTF_8, opts);
		} else {
			final URLConnection conn = urlObj.openConnection();
			conn.setDoOutput(true);
			writer0 = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8));
		}
		try (final PrintWriter writer = new PrintWriter(writer0)) {
			final Iterator<SampledValue> it = timeSeries.iterator(start, end);
			int cnt = 0;
			while (it.hasNext()) {
				final SampledValue sv = it.next();
				final long t = sv.getTimestamp();
				final float value = sv.getQuality() == Quality.GOOD ? sv.getValue().getFloatValue() : Float.NaN;
				if (!suppressTime) {
					writer.append(df == null ? String.valueOf(t) : df.format(new Date(t))).append(';'); // TODO format
				}
				writer.append(String.valueOf(value)).append('\r').append('\n');
				cnt++;
			}
			writer.flush();
			return cnt;
		}
	}
	
	
	private long parseTimestamp(final String str, long defaultValue) {
		try {
			return parseTimestamp(str);
		} catch (IllegalArgumentException e) {
			return defaultValue;
		}
	}
	
	private long parseTimestamp(final String str) {
		if ("now".equalsIgnoreCase(str))
			return clock.getExecutionTime();
		try {
			return Long.parseLong(str);
		} catch (NumberFormatException e) {}
		for (String p : DATE_TIME_PATTERNS) {
			try {
				return new SimpleDateFormat(p, Locale.ENGLISH).parse(str).getTime();
			} catch (Exception e) {}
		}
		throw new IllegalArgumentException("Invalid datetime " + str);
	}
	

}
