package org.ogema.wago;

import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.model.Resource;

public interface IO {
	public enum Direction {
		IN, OUT, INOUT
	}

	public void setChannel(ChannelAccess channelAccess, ChannelLocator cl, Resource res);

	public int getWordOffset();
	
	public void shutdown();
}
