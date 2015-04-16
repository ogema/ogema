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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.ogema.core.application.AppID;

public class RedirectionURLConnection extends URLConnection {

	enum Snippet {
		SNIPPET0, USERNAME, SNIPPET2, PASSWORD, SNIPPET4, NATIVERESOURCE, SNIPPET6, EOF
	};

	static final byte[] snippet0 = "<HTML><HEAD><meta charset=\"utf-8\"></HEAD>\n\n<BODY>\n<SCRIPT type=\"application/javascript\">\nvar otusr=\""
			.getBytes();
	byte[] username;
	static final byte[] snippet2 = "\";\nvar otpwd=\"".getBytes();
	byte[] otp;
	static final byte[] snippet4 = "\";\n</SCRIPT>\n</BODY>\n".getBytes();
	static final byte[] snippet6 = "\n</HTML>".getBytes();

	static final int len0 = snippet0.length;
	int len1; // username
	static final int len2 = snippet2.length;
	int len3; // otp
	static final int len4 = snippet4.length;
	int len5; // native resource
	static final int len6 = snippet6.length;

	Snippet currentPart;
	int readPtr;

	RedirectStream is;

	protected String name;
	private InputStream nativeStream;
	private int available;

	protected RedirectionURLConnection(URL url, String name, AppID app, String pw) {
		super(url); // call constructor from super class URLConnection
		this.name = name;
		this.otp = pw.getBytes();
		this.username = app.getIDString().getBytes();
		len1 = username.length;
		len3 = otp.length;
		try {
			this.nativeStream = app.getApplication().getClass().getResource(name).openConnection().getInputStream();
			len5 = nativeStream.available();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.is = new RedirectStream();
		try {
			connect();
		} catch (IOException e) {
		}
	}

	public void connect() throws IOException {
		currentPart = Snippet.SNIPPET0;
		readPtr = 0;
		available = len0 + len1 + len2 + len3 + len4 + len5 + len6;
	}

	public InputStream getInputStream() throws IOException {
		return is;
	}

	public int getContentLength() {
		return available;
	}

	public long getContentLengthLong() {
		return available;
	}

	@SuppressWarnings("fallthrough")
	class RedirectStream extends InputStream {

		@Override
		public int available() {
			return available;
		}

		int readPart(byte[] ba, int doff, int len) {
			int toRead = len;
			int read = doff;
			byte[] currentArr;
			switch (currentPart) {
			case SNIPPET0:
				currentArr = snippet0;
				// How many bytes could be read in the current array yet?
				int currentBytes = currentArr.length - readPtr;
				if (toRead <= currentBytes) {
					System.arraycopy(currentArr, readPtr, ba, read, toRead);
					readPtr += toRead;
					read += toRead;
					if (readPtr == len0)
						readPtr = 0;
					available -= toRead;
					return read - doff;
				}
				else {
					System.arraycopy(currentArr, readPtr, ba, read, currentBytes);
					readPtr = 0;
					toRead -= currentBytes;
					read += currentBytes;
					currentPart = Snippet.USERNAME;
					available -= currentBytes;
					if ((toRead <= 0))
						return read - doff;
				}
			case USERNAME:
				currentArr = username;
				currentBytes = currentArr.length;
				if (toRead <= currentBytes) {
					return read - doff;
				}
				else {
					System.arraycopy(currentArr, readPtr, ba, read, currentBytes);
					toRead -= currentBytes;
					read += currentBytes;
					currentPart = Snippet.SNIPPET2;
					available -= currentBytes;
					if ((toRead <= 0))
						return read - doff;
				}
			case SNIPPET2:
				currentArr = snippet2;
				currentBytes = currentArr.length - readPtr;
				if (toRead <= currentBytes) {
					System.arraycopy(currentArr, readPtr, ba, read, toRead);
					readPtr += toRead;
					read += toRead;
					if (readPtr == len2)
						readPtr = 0;
					available -= toRead;
					return read - doff;
				}
				else {
					System.arraycopy(currentArr, readPtr, ba, read, currentBytes);
					readPtr = 0;
					toRead -= currentBytes;
					read += currentBytes;
					currentPart = Snippet.PASSWORD;
					available -= currentBytes;
					if ((toRead <= 0))
						return read - doff;
				}
			case PASSWORD:
				currentArr = otp;
				currentBytes = currentArr.length;
				if (toRead <= currentBytes) {
					return read - doff;
				}
				else {
					System.arraycopy(currentArr, readPtr, ba, read, currentBytes);
					toRead -= currentBytes;
					read += currentBytes;
					currentPart = Snippet.SNIPPET4;
					available -= currentBytes;
					if ((toRead <= 0))
						return read - doff;
				}
			case SNIPPET4:
				currentArr = snippet4;
				currentBytes = currentArr.length - readPtr;
				if (toRead <= currentBytes) {
					System.arraycopy(currentArr, readPtr, ba, read, toRead);
					readPtr += toRead;
					read += toRead;
					if (readPtr == len4)
						readPtr = 0;
					available -= toRead;
					return read - doff;
				}
				else {
					System.arraycopy(currentArr, readPtr, ba, read, currentBytes);
					readPtr = 0;
					toRead -= currentBytes;
					read += currentBytes;
					currentPart = Snippet.NATIVERESOURCE;
					available -= currentBytes;
					if ((toRead <= 0))
						return read - doff;
				}
			case NATIVERESOURCE:
				int nativeRead = 0;
				try {
					while (toRead > 0) {
						nativeRead = nativeStream.read(ba, read, toRead);
						if (nativeRead != -1) {
							read += nativeRead;
							toRead -= nativeRead;
							available -= nativeRead;
						}
						else {
							currentPart = Snippet.SNIPPET6;
							break;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				if ((toRead <= 0))
					return read - doff;
			case SNIPPET6:
				currentArr = snippet6;
				currentBytes = currentArr.length - readPtr;
				if (toRead <= currentBytes) {
					System.arraycopy(currentArr, readPtr, ba, read, toRead);
					readPtr += toRead;
					read += toRead;
					if (readPtr == len6)
						readPtr = 0;
					available -= toRead;
					return read - doff;
				}
				else {
					System.arraycopy(currentArr, readPtr, ba, read, currentBytes);
					readPtr = 0;
					toRead -= currentBytes;
					read += currentBytes;
					currentPart = Snippet.EOF;
					available -= currentBytes;
					return read - doff;
				}
			case EOF:
			default:
				return -1;
			}
		}

		int read1() {
			int result = -1;
			switch (currentPart) {
			case SNIPPET0:
				result = snippet0[readPtr++];
				if (readPtr == len0) {
					readPtr = 0;
					currentPart = Snippet.USERNAME;
				}
				available--;
				return result;
			case USERNAME:
				result = username[readPtr++];
				if (readPtr == len1) {
					readPtr = 0;
					currentPart = Snippet.SNIPPET2;
				}
				available--;
				return result;
			case SNIPPET2:
				result = snippet2[readPtr++];
				if (readPtr == len2) {
					readPtr = 0;
					currentPart = Snippet.PASSWORD;
				}
				available--;
				return result;
			case PASSWORD:
				result = otp[readPtr++];
				if (readPtr == len3) {
					readPtr = 0;
					currentPart = Snippet.SNIPPET4;
				}
				available--;
				return result;
			case SNIPPET4:
				result = snippet4[readPtr++];
				if (readPtr == len4) {
					readPtr = 0;
					currentPart = Snippet.NATIVERESOURCE;
				}
				available--;
				return result;
			case NATIVERESOURCE:
				try {
					result = nativeStream.read();
					if (result != -1) {
						available--;
					}
					else {
						readPtr = 0;
						currentPart = Snippet.SNIPPET6;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return result;
			case SNIPPET6:
				result = snippet6[readPtr++];
				if (readPtr == len6) {
					readPtr = 0;
					currentPart = Snippet.NATIVERESOURCE;
				}
				available--;
				return result;
			case EOF:
			default:
				// break;
				return -1;
			}
			// return result;
		}

		@Override
		public int read(byte[] ba) {
			return readPart(ba, 0, ba.length);
		}

		@Override
		public int read(byte[] ba, int off, int len) {
			return readPart(ba, off, len);
		}

		@Override
		public int read() throws IOException {
			return read1();
		}

	}
}
