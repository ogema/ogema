package org.ogema.impl.wago;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.EventType;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.wago.DigitalIn;

public class InputListener implements ChannelEventListener {

	Map<Integer, DigitalIn> bits;

	public InputListener() {
		bits = new HashMap<>();
	}

	void addBitConsumer(int index, DigitalIn din) {
		bits.put(index, din);
	}

	@Override
	public void channelEvent(EventType type, List<SampledValueContainer> channels) {
		SampledValueContainer channel = channels.get(0);
		int value = channel.getSampledValue().getValue().getIntegerValue();
		for (int i = 0; i < 16; i++) {
			DigitalIn din = bits.get(i);
			if (din != null) {
				short mask = (short) (1 << i);
				boolean b = (value & mask) != 0;
				din.setValue(b);
			}
		}
	}

}
