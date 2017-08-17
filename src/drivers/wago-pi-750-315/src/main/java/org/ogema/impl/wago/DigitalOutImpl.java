package org.ogema.impl.wago;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.driverspi.ValueContainer;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.wago.DigitalOut;

public class DigitalOutImpl implements DigitalOut {
	int wordOffset;
	int bitOffset;
	short pmask, nmask;
	private ChannelConfiguration channel;
	private ChannelAccess channelAccess;
	public BusCoupler pi;
	private BooleanResource resource;
	private List<ValueContainer> channels = new ArrayList<>();
	ValueContainer svc;

	@Override
	public void setChannel(ChannelAccess ca, final ChannelConfiguration cfg, Resource res) {
		if (!(res instanceof BooleanResource)) {
			throw new RuntimeException("Wrong resource type assigned to DigitalOut device");
		}
		this.channel = cfg;
		this.channelAccess = ca;
		this.resource = (BooleanResource) res;

		svc = new ValueContainer(cfg.getChannelLocator(), new IntegerValue(0));
		this.channels.add(svc);

		ResourceValueListener<BooleanResource> rvl = new ResourceValueListener<BooleanResource>() {

			@Override
			public void resourceChanged(BooleanResource resource) {
				boolean val = resource.getValue();
				setValue(val);
			}
		};
		res.addValueListener(rvl);
		setValue(this.resource.getValue());
	}

	private void setValue(boolean b) {
		/*
		 * Get the current status stored in BusCoupler before
		 */
		int word = pi.getOutWord(wordOffset);

		if (b)
			word |= pmask;
		else
			word &= nmask;
		svc.setValue(new IntegerValue(word));
		try {
			channelAccess.writeUnconfiguredChannels(channels);
		} catch (ChannelAccessException e) {
			e.printStackTrace();
		}
		pi.setOutWord(wordOffset, word);
	}

	@Override
	public int getWordOffset() {
		return wordOffset;
	}

	@Override
	public void shutdown() {
		resource.setValue(false);
		channelAccess.deleteChannel(channel);
	}
}
