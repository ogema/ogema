package org.ogema.impl.wago;

import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.model.Resource;
import org.ogema.wago.AnalogIn;

public class AnalogInImpl implements AnalogIn {

	int wordOffset;

	protected int dataWidth;

	public boolean prepared;
	public BusCoupler pi;

	@Override
	public int getWidth() {
		return Wago750315.WORD;
	}

	@Override
	public void setChannel(ChannelAccess ca, ChannelLocator cl, Resource res) {
	}

	@Override
	public int getWordOffset() {
		return wordOffset;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}
}
