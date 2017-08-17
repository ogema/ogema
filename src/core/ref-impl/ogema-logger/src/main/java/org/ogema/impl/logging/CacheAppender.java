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

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.util.FileSize;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Queue;

/**
 * Appender that implements a FIFO buffer with a maximum total size for buffered log messages. The buffer can be written
 * to disk by calling {@link #saveCache()}.
 * 
 * @author jlapp
 */
//public class CacheAppender<E extends ILoggingEvent> extends AppenderBase<E> {
public class CacheAppender<E extends ILoggingEvent> extends UnsynchronizedAppenderBase<E> {

	protected final FileSize SIZELIMIT = FileSize.valueOf("10MB");
	protected volatile FileSize maxSize = FileSize.valueOf("2MB");
	// synchronized on buffer
	protected int currentSize = 0;
	protected final Queue<String> buffer = new ArrayDeque<>(2000);
	protected final PatternLayoutBase<ILoggingEvent> layout = new PatternLayout();
	protected volatile String fileNamePattern = "cachedump_%d{yyyy-MM-dd-HH-mm.ss.SSS}.log";

	@Override
	protected void append(E eventObject) {
		// must be processed outside synchronized block, for risk of deadlock; calls arguments' toString method
		eventObject.prepareForDeferredProcessing();
		synchronized (buffer) {
//			eventObject.prepareForDeferredProcessing();
			String s = layout.doLayout(eventObject);
			int addedSize = sizeOf(s);
			if (addedSize > maxSize.getSize()) {
				return;
			}
			if (currentSize + addedSize > maxSize.getSize()) {
				pruneCache(maxSize.getSize() - addedSize);
			}
			buffer.offer(s);
			currentSize += addedSize;
		}
	}

	/*
	 * remove old entries until currentSize <= newSize.
	 */
	protected void pruneCache(long newSize) {
		synchronized (buffer) {
			while (currentSize > newSize) {
				String head = buffer.remove();
				currentSize -= sizeOf(head);
			}
		}
	}

	public void saveCache() throws IOException {
		synchronized (buffer) {
			FileNamePattern fnp = new FileNamePattern(fileNamePattern, getContext());
			String outputName = fnp.convertMultipleArguments(new Date());
			File outputFile = new File(outputName);
			if (!outputFile.getParentFile().exists()) {
				outputFile.getParentFile().mkdirs();
			}
			try (FileOutputStream fos = new FileOutputStream(outputFile, false);
					OutputStreamWriter w = new OutputStreamWriter(fos, "UTF-8");
					BufferedWriter bw = new BufferedWriter(w);) {
				for (String s : buffer) {
					bw.write(s);
				}
				bw.flush();
			}
		}
	}
    
    public List<String> getCache(){
        return Arrays.asList(buffer.toArray(new String[0]));
    }
    
	@Override
	public void start() {
		layout.setContext(getContext());
		layout.start();
		super.start();
	}

	@Override
	public void stop() {
		layout.stop();
		super.stop();
	}

	public void setSize(String size) {
		FileSize newMax = FileSize.valueOf(size);
		if (newMax.getSize() < 0 || newMax.getSize() > SIZELIMIT.getSize()) {
			// ignore silently
			return;
		}
		this.maxSize = newMax;
		pruneCache(maxSize.getSize());
	}

	public String getSize() {
		return Long.toString(maxSize.getSize());
	}

	public long getSizeLong() {
		return maxSize.getSize();
	}

	public void setPattern(String pattern) {
		layout.setPattern(pattern);
	}

	public String getPattern() {
		return layout.getPattern();
	}

	public void setFilename(String filename) {
		fileNamePattern = filename;
	}

	public String getFilename() {
		return fileNamePattern;
	}

	protected int sizeOf(String s) {
		return 2 * s.length();
	}
}
