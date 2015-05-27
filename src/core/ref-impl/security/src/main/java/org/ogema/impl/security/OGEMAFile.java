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
package org.ogema.impl.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;

import org.ogema.core.application.AppID;
import org.osgi.framework.BundleContext;

public class OGEMAFile {
	private BundleContext osgi;
	private AppID app;
	private BundleStoragePolicy storage;

	/**
	 * ApplicationManager creates each app a such instance and provides the app
	 * with the reference.
	 * 
	 * @param app
	 */
	protected OGEMAFile(AppID app) {
		this.app = app;
		this.osgi = app.getBundle().getBundleContext();
		this.storage = BundleStoragePolicy.getStoragePolicy(app.getBundle());
	}

	private File getFileStorageLocation(String path) {
		// Be sure that the path is relative
		return osgi.getDataFile(path);
	}

	/*
	 * Output objects: an instance of only one of the output objects can exists
	 * at the same time.
	 */
	public FileOutputStream createFileOutputStream(String path) {
		final File f = getFileStorageLocation(path);
		FileOutputStream fos = AccessController.doPrivileged(new PrivilegedAction<FileOutputStream>() {
			public FileOutputStream run() {
				try {
					return new FOS(f);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}
		});
		return fos;
	}

	class FOS extends FileOutputStream {

		public FOS(File f) throws FileNotFoundException {
			super(f);
		}

		public void write(byte[] b) throws IOException {
			int available = storage.consume(b.length);
			if (available >= 0)
				super.write(b);
		}

		public void write(byte[] b, int off, int len) throws IOException {
			int available = storage.consume(len);
			if (available >= 0)
				super.write(b, off, len);
		}

		public void write(int b) throws IOException {
			int available = storage.consume(1);
			if (available >= 0)
				super.write(b);
		}

	}

	public FileWriter createFileWriter(String path, final boolean append) {
		final File f = getFileStorageLocation(path);
		FileWriter fw = AccessController.doPrivileged(new PrivilegedAction<FileWriter>() {
			public FileWriter run() {
				try {
					return new FW(f, append);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}
		});
		return fw;
	}

	class FW extends FileWriter {

		public FW(File f, boolean append) throws IOException {
			super(f, append);
		}

		public Writer append(char c) throws IOException {
			int available = storage.consume(2);
			if (available >= 0)
				return super.append(c);
			else
				return this;
		}

		public Writer append(CharSequence csq) throws IOException {
			int available = storage.consume(csq.length());
			if (available >= 0)
				return super.append(csq);
			else
				return this;
		}

		public Writer append(CharSequence csq, int start, int end) throws IOException {
			int available = storage.consume((end - start) << 1);
			if (available >= 0)
				return super.append(csq, start, end);
			else
				return this;
		}

		public void write(char[] cbuf, int off, int len) throws IOException {
			int available = storage.consume(len << 1);
			if (available >= 0)
				super.write(cbuf, off, len);
		}

		public void write(int c) throws IOException {
			int available = storage.consume(2);
			if (available >= 0)
				super.write(c);
		}

		public void write(String str, int off, int len) throws IOException {
			int available = storage.consume(len << 1);
			if (available >= 0)
				super.write(str, off, len);
		}

		public void write(char[] cbuf) throws IOException {
			int available = storage.consume(cbuf.length << 1);
			if (available >= 0)
				super.write(cbuf);
		}

		public void write(String str) throws IOException {
			int available = storage.consume(str.length() << 1);
			if (available >= 0)
				super.write(str);
		}
	}

	public PrintStream createPrintStream(String path, final String cs) {
		final File f = getFileStorageLocation(path);
		PrintStream ps = AccessController
				.doPrivileged(new PrivilegedAction<PrintStream>() {
					public PrintStream run() {
						try {
							if (cs != null)
								return new PS(f, cs);
							else
								return new PS(f);
						} catch (FileNotFoundException
								| UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return null;
						}
					}
				});
		return ps;
	}

	class PS extends PrintStream {

		public PS(File f, String csn) throws FileNotFoundException, UnsupportedEncodingException {
			super(f, csn);
			// TODO Auto-generated constructor stub
		}

		public PS(File f) throws FileNotFoundException, UnsupportedEncodingException {
			super(f);
			// TODO Auto-generated constructor stub
		}

		public PrintStream append(char c) {
			return this;
		}

		public PrintStream append(CharSequence csq) {
			return this;
		}

		public PrintStream append(CharSequence csq, int start, int end) {
			return this;
		}

		public void print(boolean b) {
		}

		public void print(char c) {
		}

		public void print(char[] s) {
		}

		public void print(double d) {
		}

		public void print(float f) {
		}

		public void print(int i) {
		}

		public void print(long l) {
		}

		public void print(Object obj) {
		}

		public void print(String s) {
		}

		public PrintStream printf(Locale l, String format, Object... args) {
			return this;
		}

		public PrintStream printf(String format, Object... args) {
			return this;
		}

		public void println() {
		}

		public void println(boolean x) {
		}

		public void println(char x) {
		}

		public void println(char[] x) {
		}

		public void println(double x) {
		}

		public void println(float x) {
		}

		public void println(int x) {
		}

		public void println(long x) {
		}

		public void println(Object x) {
		}

		public void println(String x) {
		}

		public void write(byte[] buf, int off, int len) {
		}

		public void write(int b) {
		}
	}

	public PrintWriter createPrintWriter(String path, final String cs) {
		final File f = getFileStorageLocation(path);
		PrintWriter pw = AccessController
				.doPrivileged(new PrivilegedAction<PrintWriter>() {
					public PrintWriter run() {
						try {
							if (cs != null)
								return new PW(f, cs);
							else
								return new PW(f);
						} catch (FileNotFoundException
								| UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return null;
						}
					}
				});
		return pw;
	}

	class PW extends PrintWriter {

		public PW(File f, String csn) throws FileNotFoundException, UnsupportedEncodingException {
			super(f, csn);
			// TODO Auto-generated constructor stub
		}

		public PW(File f) throws FileNotFoundException, UnsupportedEncodingException {
			super(f);
			// TODO Auto-generated constructor stub
		}

		@Override
		public PrintWriter append(char c) {
			return this;
		}

		@Override
		public PrintWriter append(CharSequence csq) {
			return this;
		}

		@Override
		public PrintWriter append(CharSequence csq, int start, int end) {
			return this;
		}

		@Override
		public void print(boolean b) {
		}

		@Override
		public void print(char c) {
		}

		@Override
		public void print(char[] s) {
		}

		@Override
		public void print(double d) {
		}

		@Override
		public void print(float f) {
		}

		@Override
		public void print(int i) {
		}

		@Override
		public void print(long l) {
		}

		@Override
		public void print(Object obj) {
		}

		@Override
		public void print(String s) {
		}

		@Override
		public PrintWriter printf(Locale l, String format, Object... args) {
			return this;
		}

		@Override
		public PrintWriter printf(String format, Object... args) {
			return this;
		}

		@Override
		public void println() {
		}

		@Override
		public void println(boolean x) {
		}

		@Override
		public void println(char x) {
		}

		@Override
		public void println(char[] x) {
		}

		@Override
		public void println(double x) {
		}

		@Override
		public void println(float x) {
		}

		@Override
		public void println(int x) {
		}

		@Override
		public void println(long x) {
		}

		@Override
		public void println(Object x) {
		}

		@Override
		public void println(String x) {
		}

		@Override
		public void write(char[] buf) {
		}

		@Override
		public void write(char[] buf, int off, int len) {
		}

		@Override
		public void write(String s) {
		}

		@Override
		public void write(String s, int off, int len) {
		}

		@Override
		public void write(int b) {
		}
	}

	/*
	 * Input Objects: multiple input objects can exist at the same time
	 */
	public FileInputStream createFileInputStream(String path) {
		final File f = getFileStorageLocation(path);
		FileInputStream fis = AccessController.doPrivileged(new PrivilegedAction<FileInputStream>() {
			public FileInputStream run() {
				try {
					return new FIS(f);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}
		});
		return fis;
	}

	class FIS extends FileInputStream {
		public FIS(File f) throws FileNotFoundException {
			super(f);
		}
	}

	public FileReader createFileReader(String path) {
		final File f = getFileStorageLocation(path);
		FileReader fr = AccessController.doPrivileged(new PrivilegedAction<FileReader>() {
			public FileReader run() {
				try {
					return new FR(f);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}
		});
		return fr;

	}

	class FR extends FileReader {
		public FR(File f) throws IOException {
			super(f);
		}
	}
}
