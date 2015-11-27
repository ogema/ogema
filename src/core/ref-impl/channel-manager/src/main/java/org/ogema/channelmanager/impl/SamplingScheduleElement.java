package org.ogema.channelmanager.impl;

import java.util.List;

public class SamplingScheduleElement {

	private final long samplingtimestamp;
	private final List<Channel> channels;

	public SamplingScheduleElement(long samplingtimestamp, List<Channel> channels) {
		this.samplingtimestamp = samplingtimestamp;
		this.channels = channels;
	}

	public long getSamplingTimestamp() {
		return this.samplingtimestamp;
	}

	public List<Channel> getChannels() {
		return this.channels;
	}
}
