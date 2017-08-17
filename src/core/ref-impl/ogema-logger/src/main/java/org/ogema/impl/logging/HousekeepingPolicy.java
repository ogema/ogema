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
package org.ogema.impl.logging;

import ch.qos.logback.core.rolling.RolloverFailure;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.helper.FileFilterUtil;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.util.FileSize;
import java.io.File;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * The rolling policy for the default OGEMA logger, basically a logback {@link TimeBasedRollingPolicy} using a
 * {@link SizeAndTimeBasedFNATP} triggering policy with added housekeeping for a maximum total size of all log files.
 * 
 * @author jlapp
 */
public class HousekeepingPolicy extends TimeBasedRollingPolicy<Object> {

	protected String maxFileSizeString;
	/**
	 * Maximum size of a single logfile.
	 */
	protected FileSize maxFileSize;
	protected String maxTotalSizeString;
	/**
	 * Maximum size of all logfiles combined.
	 */
	protected FileSize maxTotalSize;
	protected final PriorityQueue<File> files = new PriorityQueue<>(50, new Comparator<File>() {
		@Override
		public int compare(File o1, File o2) {
			return Long.compare(o1.lastModified(), o2.lastModified());
		}
	});
	protected final SizeAndTimeBasedFNATP<Object> triggeringPollicy = new SizeAndTimeBasedFNATP<>();

	public HousekeepingPolicy() {
		super();
		setTimeBasedFileNamingAndTriggeringPolicy(triggeringPollicy);
		setMaxFileSize("2MB");
		setMaxTotalSize("20MB");
	}

	public final void setMaxFileSize(String size) {
		if (isStarted()) {
			checkFileSizes();
			pruneHistory();
		}
		maxFileSize = FileSize.valueOf(size);
		triggeringPollicy.setMaxFileSize(maxFileSize);
		maxFileSizeString = size;
	}

	public String getMaxFileSize() {
		return maxFileSizeString;
	}

	// must not be called "setMaxTotalSize", since logback accesses this by reflections,
	// and mixes it up with the String-arg method below
	public void setMaxTotalSizeLong(long size) {
		if (isStarted()) {
			checkFileSizes();
			pruneHistory();
		}
		maxTotalSizeString = Long.toString(size);
		maxTotalSize = FileSize.valueOf(maxTotalSizeString);
	}

	public final void setMaxTotalSize(String size) {
		maxTotalSize = FileSize.valueOf(size);
		maxTotalSizeString = size;
	}

	public String getMaxTotalSize() {
		return maxTotalSizeString;
	}

	public long getMaxTotalSizeLong() {
		return maxTotalSize.getSize();
	}

	@Override
	public void start() {
		checkFileSizes();
		super.start();
		pruneHistory();
	}

	protected void checkFileSizes() {
		if (maxTotalSize.getSize() <= (2 * maxFileSize.getSize())) {
			throw new IllegalStateException(String.format("maxTotalSize must exceed 2 times maxFileSize (%d <= 2*%d)",
					maxTotalSize.getSize(), maxFileSize.getSize()));
		}
	}

	/*
	 * check that total log file size will not exceed maxTotalSize, otherwise remove oldest log files till maxTotalSize
	 * limit is met.
	 */
	protected synchronized boolean pruneHistory() {
		files.clear();
		long currentTotal = 0;

		FileNamePattern fnp = new FileNamePattern(getFileNamePattern(), context);
		final String fregex = fnp.toRegex();
		final String stem = FileFilterUtil.afterLastSlash(fregex).replace("\\d{1,2}", "(\\d{1,})"); // hack: bug in
																									// logback, original
		// regex will only match file
		// numbers 0-99
		final String dir = fregex.substring(0, fregex.length() - stem.length());
		File[] logfiles = FileFilterUtil.filesInFolderMatchingStemRegex(new File(dir), stem);
		File activeFile = new File(getActiveFileName());
		for (File file : logfiles) {
			if (file.getName().equals(activeFile.getName())) {
				// this should not happen anyway,
				// since the new active file hasn't been started yet (?)
				continue;
			}
			files.add(file);
			currentTotal += file.length();
		}
		// do not include active logfile in calculation, trim history so
		// that max total size will not be exceeded.
		// finer control is possible by overriding isTriggeringEvent().
		long maxHistorySize = maxTotalSize.getSize() - maxFileSize.getSize();
		while (currentTotal > maxHistorySize) {
			File file = files.poll();
			if (file == null) {
				return false;
			}
			long fileSize = file.length();
			if (file.delete()) {
				currentTotal -= fileSize;
			}
		}
		return true;
	}

	/*
	 * Extension of logback.rollover(): Additionally checks the total size of all logfiles, and deletes additional old
	 * logfiles if the total size is too large.
	 */
	@Override
	public synchronized void rollover() throws RolloverFailure {
		super.rollover();
		if (!pruneHistory()) {
			throw new RolloverFailure("Could not delete enough old logfiles to meet size constraints on logfiles.");
		}
	}
}
