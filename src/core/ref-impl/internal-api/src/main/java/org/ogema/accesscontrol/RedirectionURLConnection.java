/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ogema.accesscontrol;

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

	enum BeforeHeadStatus {
		WAIT4BRACKET_OPEN, WAIT4MINUS3, BRACKET_OPEN, BRACKET_CLOSE, EXCLAM, MINUS1, MINUS2, MINUS3, MINUS4, H, E, A, D, WAIT4BRACKET_CLOSE, WAIT4HEADBRACKET_CLOSE
	};

	static final byte[] snippet0 = "\n<script type=\"application/javascript\">\nvar otusr=\"".getBytes();
	byte[] username;
	static final byte[] snippet2 = "\";\nvar otpwd=\"".getBytes();
	byte[] otp;
	static final byte[] snippet4 = "\";\nvar otp_uri_ext=\"user=\"+otusr+\"&pw=\"+otpwd;\n</script>\n".getBytes();

	static final int len0 = snippet0.length;
	int len1; // username
	static final int len2 = snippet2.length;
	int len3; // otp
	static final int len4 = snippet4.length;
	int len5; // native resource

	Snippet currentPart;
	private BeforeHeadStatus beforeHeadState;
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
		beforeHeadState = BeforeHeadStatus.WAIT4BRACKET_OPEN;
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
			int index = 0;
			switch (currentPart) {
			case SNIPPET_UP_TO_HEAD: // read byte wise until the head tag
				int nativeRead = 0;
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
							currentPart = Snippet.SNIPPET0;
							break;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				if ((read - doff == 0)) {
					if (available > 0)
						return fillAvailables(ba, index, toRead);
					else
						return -1;
				}
				return read - doff;
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
				if (read - doff == 0) {
					if (available > 0)
						return fillAvailables(ba, index, toRead);
					else
						return -1;
				}
				return read - doff;
			case EOF:
			default: {
				if (available > 0)
					return fillAvailables(ba, index, toRead);
				else
					return -1;
			}
			}
		}

		private int fillAvailables(byte[] ba, int index, int len) {
			int result = 0;
			while (available > 0 && len > 0) {
				ba[index++] = ' ';
				available--;
				len--;
				result++;
			}
			return result;
		}

		private boolean checkBeginOfHead(int c) {
			switch (beforeHeadState) {
			case WAIT4BRACKET_OPEN:
				// skip whitespaces
				if (c == '<')
					beforeHeadState = BeforeHeadStatus.BRACKET_OPEN;//
				break;
			case BRACKET_OPEN:
				switch (c) {
				case '!':
					beforeHeadState = BeforeHeadStatus.EXCLAM;//
					break;
				case 'h':
				case 'H':
					beforeHeadState = BeforeHeadStatus.H;//
					break;
				default:
					beforeHeadState = BeforeHeadStatus.WAIT4BRACKET_CLOSE;//
					break;
				}
				break;
			case EXCLAM:
				switch (c) {
				case '-':
					beforeHeadState = BeforeHeadStatus.MINUS1;//
					break;
				default:
					beforeHeadState = BeforeHeadStatus.WAIT4BRACKET_CLOSE;
					break;
				}
				break;
			case MINUS1:
				switch (c) {
				case '-':
					beforeHeadState = BeforeHeadStatus.MINUS2;
					break;
				default:
					beforeHeadState = BeforeHeadStatus.WAIT4BRACKET_CLOSE;
					break;
				}
				break;
			case MINUS2:
				switch (c) {
				case '-':
					beforeHeadState = BeforeHeadStatus.MINUS3;
					break;
				default:
					beforeHeadState = BeforeHeadStatus.WAIT4MINUS3;
					break;
				}
				break;
			case WAIT4MINUS3:
				switch (c) {
				case '-':
					beforeHeadState = BeforeHeadStatus.MINUS3;
					break;
				default:
					break;
				}
				break;
			case MINUS3:
				switch (c) {
				case '-':
					beforeHeadState = BeforeHeadStatus.MINUS4;
					break;
				default:
					beforeHeadState = BeforeHeadStatus.WAIT4MINUS3;
					break;
				}
				break;
			case MINUS4:
				switch (c) {
				case '>':
					head = false;
					beforeHeadState = BeforeHeadStatus.WAIT4BRACKET_OPEN;
					break;
				default:
					beforeHeadState = BeforeHeadStatus.WAIT4MINUS3;
					break;
				}
				break;
			case H:
				switch (c) {
				case 'e':
				case 'E':
					beforeHeadState = BeforeHeadStatus.E;//
					break;
				case '>':
					head = false;
					beforeHeadState = BeforeHeadStatus.WAIT4BRACKET_OPEN;
					break;
				default:
					beforeHeadState = BeforeHeadStatus.WAIT4BRACKET_CLOSE;
					break;
				}
				break;
			case E:
				switch (c) {
				case 'a':
				case 'A':
					beforeHeadState = BeforeHeadStatus.A;//
					break;
				case '>':
					head = false;
					beforeHeadState = BeforeHeadStatus.WAIT4BRACKET_OPEN;
					break;
				default:
					beforeHeadState = BeforeHeadStatus.WAIT4BRACKET_CLOSE;
					break;
				}
				break;
			case A:
				switch (c) {
				case 'd':
				case 'D':
					beforeHeadState = BeforeHeadStatus.D;//
					break;
				case '>':
					head = false;
					beforeHeadState = BeforeHeadStatus.WAIT4BRACKET_OPEN;
					break;
				default:
					beforeHeadState = BeforeHeadStatus.WAIT4BRACKET_CLOSE;
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
						beforeHeadState = BeforeHeadStatus.WAIT4HEADBRACKET_CLOSE;
					break;
				}
				break;
			case WAIT4BRACKET_CLOSE:
				switch (c) {
				case '>':
					beforeHeadState = BeforeHeadStatus.WAIT4BRACKET_OPEN;
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
			case SNIPPET_UP_TO_HEAD: // read byte wise until the head tag
				try {
					int c = nativeStream.read();
					if (c != -1) {
						available--;
						if (checkBeginOfHead(c)) { // Check if the begin head is reached
							currentPart = Snippet.SNIPPET0;
						}
					}
					else {
						currentPart = Snippet.NATIVERESOURCE;
						if (available > 0) {
							available--;
							return ' ';
						}
						else
							return -1;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
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
			default: {
				if (available > 0) {
					available--;
					return ' ';
				}
				else
					return -1;
			}
			}
		}

		// int read1() {
		// int result = -1;
		// switch (currentPart) {
		// case SNIPPET0:
		// result = snippet0[readPtr++];
		// if (readPtr == len0) {
		// readPtr = 0;
		// currentPart = Snippet.USERNAME;
		// }
		// available--;
		// return result;
		// case USERNAME:
		// result = username[readPtr++];
		// if (readPtr == len1) {
		// readPtr = 0;
		// currentPart = Snippet.SNIPPET2;
		// }
		// available--;
		// return result;
		// case SNIPPET2:
		// result = snippet2[readPtr++];
		// if (readPtr == len2) {
		// readPtr = 0;
		// currentPart = Snippet.PASSWORD;
		// }
		// available--;
		// return result;
		// case PASSWORD:
		// result = otp[readPtr++];
		// if (readPtr == len3) {
		// readPtr = 0;
		// currentPart = Snippet.SNIPPET4;
		// }
		// available--;
		// return result;
		// case SNIPPET4:
		// result = snippet4[readPtr++];
		// if (readPtr == len4) {
		// readPtr = 0;
		// currentPart = Snippet.NATIVERESOURCE;
		// }
		// available--;
		// return result;
		// case NATIVERESOURCE:
		// try {
		// result = nativeStream.read();
		// if (result != -1) {
		// available--;
		// }
		// else {
		// readPtr = 0;
		// currentPart = Snippet.EOF;
		// }
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// return result;
		// case EOF:
		// default: {
		// if (available > 0) {
		// available--;
		// return ' ';
		// }
		// else
		// return -1;
		// }
		// }
		// }

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
