package org.ogema.impl.wago;

import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.wago.DigitalIn;

public class DigitalInImpl implements DigitalIn {
	int wordOffset;
	int bitOffset;
	short pmask, nmask;
	private ChannelLocator channel;
	private ChannelAccess channelAccess;

	public BusCoupler pi;
	private BooleanResource resource;
	private InputListener il;

	@Override
	public void setChannel(ChannelAccess ca, ChannelLocator cl, Resource res) {
		if (!(res instanceof BooleanResource)) {
			throw new RuntimeException("Wrong resource type assigned to DigitalOut device");
		}
		this.channel = cl;
		this.channelAccess = ca;
		this.resource = (BooleanResource) res;
		il.addBitConsumer(bitOffset, this);
	}

	@Override
	public int getWordOffset() {
		return wordOffset;
	}

	@Override
	public void setValue(boolean b) {
		this.resource.setValue(b);
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}
}
