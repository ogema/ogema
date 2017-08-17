package org.ogema.impl.wago;

import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.model.Resource;
import org.ogema.wago.AnalogOut;

public class AnalogOutImpl implements AnalogOut {

	int wordOffset;

	protected int dataWidth;
	public BusCoupler pi;

	@Override
	public int getWidth() {
		return dataWidth;
	}

	@Override
	public void setChannel(ChannelAccess ca, ChannelConfiguration cl, Resource res) {
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
