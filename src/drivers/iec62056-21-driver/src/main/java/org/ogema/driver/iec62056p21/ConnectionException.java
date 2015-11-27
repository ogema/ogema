package org.ogema.driver.iec62056p21;

public class ConnectionException extends Exception {

	private static final long serialVersionUID = -6482447005742984400L;

	public ConnectionException() {
		super();
	}

	public ConnectionException(String s) {
		super(s);
	}

	public ConnectionException(Throwable cause) {
		super(cause);
	}

	public ConnectionException(String s, Throwable cause) {
		super(s, cause);
	}
}
