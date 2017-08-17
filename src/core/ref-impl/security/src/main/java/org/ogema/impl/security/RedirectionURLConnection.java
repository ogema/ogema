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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.ogema.core.application.AppID;

public class RedirectionURLConnection extends URLConnection {

	enum Snippet {

		SNIPPET_UP_TO_HEAD, SNIPPET0, USERNAME, SNIPPET2, PASSWORD, SNIPPET4, NATIVERESOURCE, EOF
	};

	enum BeforeBodyStatus {
		WAIT4BRACKET_OPEN, WAIT4MINUS3, BRACKET_OPEN, BRACKET_CLOSE, EXCLAM, MINUS1, MINUS2, MINUS3, MINUS4, H, E, A, D, WAIT4BRACKET_CLOSE, WAIT4HEADBRACKET_CLOSE
	};

	static final byte[] snippet0 = "\n<script type=\"application/javascript\">var otusr=\"".getBytes();
	byte[] username;
	static final byte[] snippet2 = "\";var otpwd=\"".getBytes();
	byte[] otp;
	static final byte[] snippet4 = "\";</script>\n".getBytes();

	static final int len0 = snippet0.length;
	int len1; // username
	static final int len2 = snippet2.length;
	int len3; // otp
	static final int len4 = snippet4.length;
	int len5; // native resource

	Snippet currentPart;
	private BeforeBodyStatus beforeHeadState;
	int readPtr;

	InsertionStream is;

	protected String name;
	private InputStream nativeStream;
	private int available;
	private final AppID app;

	protected RedirectionURLConnection(URL url, String name, AppID app, String pw) {
		super(url); // call constructor from super class URLConnection
		this.name = name;
		this.otp = pw.getBytes();
		this.username = app.getIDString().getBytes();
		len1 = username.length;
		len3 = otp.length;
		this.app = app;
	}

	@Override
	public void connect() throws IOException {
		if (is != null) {
			return;
		}
		URL url = app.getApplication().getClass().getResource(name);
		if (url == null) {
			throw new FileNotFoundException(name);
		}
		this.nativeStream = url.openConnection().getInputStream();
		len5 = nativeStream.available();
		this.is = new InsertionStream();
		currentPart = Snippet.SNIPPET_UP_TO_HEAD;
		beforeHeadState = BeforeBodyStatus.WAIT4BRACKET_OPEN;
		readPtr = 0;
		available = len0 + len1 + len2 + len3 + len4 + len5;// + len6;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if (is == null) {
			connect();
		}
		return is;
	}

	@Override
	public int getContentLength() {
		return available;
	}

	@Override
	public long getContentLengthLong() {
		return available;
	}

	@SuppressWarnings("fallthrough")
	class InsertionStream extends InputStream {

		private boolean head;

		@Override
		public int available() {
			return available;
		}

		int readPart(byte[] ba, int doff, int len) {
			int toRead = len;
			int read = doff;
			byte[] currentArr;
			switch (currentPart) {
			case SNIPPET_UP_TO_HEAD: // read byte wise until the head tag
				int nativeRead = 0;
				int index = 0;
				try {
					while (toRead > 0) {

						int c = nativeStream.read();
						if (c != -1) {
							nativeRead = 1;
							read += nativeRead;
							toRead -= nativeRead;
							available -= nativeRead;
							ba[index++] = (byte) c;
						}
						else {
							currentPart = Snippet.NATIVERESOURCE;
							break;
						}
						if (checkBeginOfHead(c)) { // Check if the begin head is reached
							currentPart = Snippet.NATIVERESOURCE;
							break;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				if ((toRead <= 0)) {
					return read - doff;
				}
			case SNIPPET0:
				currentArr = snippet0;
				// How many bytes could be read in the current array yet?
				int currentBytes = currentArr.length - readPtr;
				if (toRead <= currentBytes) {
					System.arraycopy(currentArr, readPtr, ba, read, toRead);
					readPtr += toRead;
					read += toRead;
					if (readPtr == len0) {
						readPtr = 0;
					}
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
					if ((toRead <= 0)) {
						return read - doff;
					}
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
					if ((toRead <= 0)) {
						return read - doff;
					}
				}
			case SNIPPET2:
				currentArr = snippet2;
				currentBytes = currentArr.length - readPtr;
				if (toRead <= currentBytes) {
					System.arraycopy(currentArr, readPtr, ba, read, toRead);
					readPtr += toRead;
					read += toRead;
					if (readPtr == len2) {
						readPtr = 0;
					}
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
					if ((toRead <= 0)) {
						return read - doff;
					}
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
					if ((toRead <= 0)) {
						return read - doff;
					}
				}
			case SNIPPET4:
				currentArr = snippet4;
				currentBytes = currentArr.length - readPtr;
				if (toRead <= currentBytes) {
					System.arraycopy(currentArr, readPtr, ba, read, toRead);
					readPtr += toRead;
					read += toRead;
					if (readPtr == len4) {
						readPtr = 0;
					}
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
					if ((toRead <= 0)) {
						return read - doff;
					}
				}
			case NATIVERESOURCE:
				nativeRead = 0;
				try {
					while (toRead > 0) {
						nativeRead = nativeStream.read(ba, read, toRead);
						if (nativeRead != -1) {
							read += nativeRead;
							toRead -= nativeRead;
							available -= nativeRead;
						}
						else {
							currentPart = Snippet.EOF;
							break;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return read - doff;
			case EOF:
			default:
				return -1;
			}
		}

		private boolean checkBeginOfHead(int c) {
			switch (beforeHeadState) {
			case WAIT4BRACKET_OPEN:
				// skip whitespaces
				if (c == '<')
					beforeHeadState = BeforeBodyStatus.BRACKET_OPEN;//
				break;
			case BRACKET_OPEN:
				switch (c) {
				case '!':
					beforeHeadState = BeforeBodyStatus.EXCLAM;//
					break;
				case 'h':
				case 'H':
					beforeHeadState = BeforeBodyStatus.H;//
					break;
				default:
					beforeHeadState = BeforeBodyStatus.WAIT4BRACKET_CLOSE;//
					break;
				}
				break;
			case EXCLAM:
				switch (c) {
				case '-':
					beforeHeadState = BeforeBodyStatus.MINUS1;//
					break;
				default:
					beforeHeadState = BeforeBodyStatus.WAIT4BRACKET_CLOSE;
					break;
				}
				break;
			case MINUS1:
				switch (c) {
				case '-':
					beforeHeadState = BeforeBodyStatus.MINUS2;
					break;
				default:
					beforeHeadState = BeforeBodyStatus.WAIT4BRACKET_CLOSE;
					break;
				}
				break;
			case MINUS2:
				switch (c) {
				case '-':
					beforeHeadState = BeforeBodyStatus.MINUS3;
					break;
				default:
					beforeHeadState = BeforeBodyStatus.WAIT4MINUS3;
					break;
				}
				break;
			case WAIT4MINUS3:
				switch (c) {
				case '-':
					beforeHeadState = BeforeBodyStatus.MINUS3;
					break;
				default:
					break;
				}
				break;
			case MINUS3:
				switch (c) {
				case '-':
					beforeHeadState = BeforeBodyStatus.MINUS4;
					break;
				default:
					beforeHeadState = BeforeBodyStatus.WAIT4MINUS3;
					break;
				}
				break;
			case MINUS4:
				switch (c) {
				case '>':
					head = false;
					beforeHeadState = BeforeBodyStatus.WAIT4BRACKET_OPEN;
					break;
				default:
					beforeHeadState = BeforeBodyStatus.WAIT4MINUS3;
					break;
				}
				break;
			case H:
				switch (c) {
				case 'e':
				case 'E':
					beforeHeadState = BeforeBodyStatus.E;//
					break;
				case '>':
					head = false;
					beforeHeadState = BeforeBodyStatus.WAIT4BRACKET_OPEN;
					break;
				default:
					beforeHeadState = BeforeBodyStatus.WAIT4BRACKET_CLOSE;
					break;
				}
				break;
			case E:
				switch (c) {
				case 'a':
				case 'A':
					beforeHeadState = BeforeBodyStatus.A;//
					break;
				case '>':
					head = false;
					beforeHeadState = BeforeBodyStatus.WAIT4BRACKET_OPEN;
					break;
				default:
					beforeHeadState = BeforeBodyStatus.WAIT4BRACKET_CLOSE;
					break;
				}
				break;
			case A:
				switch (c) {
				case 'd':
				case 'D':
					beforeHeadState = BeforeBodyStatus.D;//
					break;
				case '>':
					head = false;
					beforeHeadState = BeforeBodyStatus.WAIT4BRACKET_OPEN;
					break;
				default:
					beforeHeadState = BeforeBodyStatus.WAIT4BRACKET_CLOSE;
					break;
				}
				break;
			case D:
				switch (c) {
				case ' ':
				case '\t':
				case '\n':
				case '\r':
					head = true;
					break;
				case '>':
					return true;
				default:
					if (head)
						beforeHeadState = BeforeBodyStatus.WAIT4HEADBRACKET_CLOSE;
					break;
				}
				break;
			case WAIT4BRACKET_CLOSE:
				switch (c) {
				case '>':
					beforeHeadState = BeforeBodyStatus.WAIT4BRACKET_OPEN;
					break;
				default:
					break;
				}
				break;
			case WAIT4HEADBRACKET_CLOSE:
				switch (c) {
				case '>':
					return true;
				default:
					break;
				}
				break;
			default:
				break;

			}
			return false;
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
						currentPart = Snippet.EOF;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return result;
			case EOF:
			default:
				return -1;
			}
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
