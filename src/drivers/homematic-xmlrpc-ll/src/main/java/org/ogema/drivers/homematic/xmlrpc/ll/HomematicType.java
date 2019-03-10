package org.ogema.drivers.homematic.xmlrpc.ll;

public enum HomematicType {
	
	Wired(2000), BidCos(2001), Ip(2010);

	private final int defaultPort;
	
	private HomematicType(int port) {
		this.defaultPort = port;
	}
	
	public int getDefaultPort() {
		return defaultPort;
	}
	
	public int getTlsPort() {
		return Integer.parseInt("4" + getDefaultPort());
	}
	
	public static HomematicType forPort(int port) {
		for (HomematicType t : values()) {
			if (t.getDefaultPort() == port)
				return t;
		}
		for (HomematicType t : values()) {
			if (t.getTlsPort() == port)
				return t;
		}
		return null;
	}
	
}
