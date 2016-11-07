/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.ogema.tests.dumydriver.hld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.EventType;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.ObjectValue;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.model.locations.Room;

public class WriteTimeListener implements ChannelEventListener, ThreadControl, ResourceValueListener<TimeResource> {
	Room room;
	private boolean running;
	private ConcurrentHashMap<String, ChannelConfiguration> chconfs;
	private OgemaLogger logger;
	private DummyHighLevelDriver hld;
	private String id;
	private int sumWrite;
	private int writeCount;

	public WriteTimeListener(Room r, final ChannelAccess ca, OgemaLogger logger, DummyHighLevelDriver hld, String id) {
		this.room = r;
		this.chconfs = new ConcurrentHashMap<>();
		this.logger = logger;
		this.hld = hld;
		this.id = id;
		TimeResource write = r.getSubResource("write");
		write.addValueListener(this, true);
		final WriteTimeListener wrl = this;
		final int sr = hld.readSamplingRate;
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				running = true;
				while (running) {
					Set<Entry<String, ChannelConfiguration>> entries = chconfs.entrySet();
					for (Entry<String, ChannelConfiguration> e : entries) {
						ChannelConfiguration chConf = e.getValue();
						long TS1 = System.currentTimeMillis();
						TimeStampContainer tsc = new TimeStampContainer();
						tsc.TS1 = TS1;
						try {
							ca.setChannelValue(chConf, new ObjectValue(tsc));
						} catch (ChannelAccessException e1) {
							running = false;
							break;
						}
					}
					Thread.yield();
				}
			}
		});
		t.setName("Dummy-Driver-Writer-Thread" + id);
		t.start();
	}

	@Override
	public void channelEvent(EventType type, List<SampledValueContainer> channels) {
		for (SampledValueContainer container : channels) {
			if (room == null)
				return;
			ChannelLocator cl = container.getChannelLocator();
			String devAddr = cl.getChannelAddress();
			ChannelConfiguration chconf = chconfs.get(devAddr);
			if (!cl.equals(chconf.getChannelLocator())) {
				logger.error("Misleaded callback %s to %s", cl.toString(), chconf.toString());
				continue;
			}
			SampledValue val = container.getSampledValue();
			TimeStampContainer tsc = (TimeStampContainer) val.getValue().getObjectValue();

			long TS1 = tsc.TS1;
			long TS2 = tsc.TS2;
			long TS3 = System.currentTimeMillis();

			TimeResource time = room.getSubResource("write");
			IntegerResource jitter = time.getSubResource("jitter");
			IntegerResource ack = time.getSubResource("lldAck");
			TimeResource ts = time.getSubResource("ts");
			ts.setValue(TS3);
			int duration = (int) (TS2 - TS1);
			time.setValue(duration);
			int meanValueW;
			sumWrite += duration;
			writeCount++;
			meanValueW = sumWrite / writeCount;
			jitter.setValue((int) (duration - meanValueW));
			ack.setValue((int) (TS3 - TS1));
		}
	}

	@Override
	public void stop() {
		running = false;
	}

	@Override
	public void resourceChanged(TimeResource resource) {
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
		}
		IntegerResource lt = resource.getSubResource("loopTime");
		TimeResource ts = resource.getSubResource("ts");
		int loop;
		synchronized (this) {

			long TS4 = System.currentTimeMillis();
			long TS3 = ts.getValue();
			loop = (int) (TS4 - TS3);
		}
		lt.setValue(loop);
	}

	public void addChannel(String devAddr, ChannelConfiguration chConf) {
		chconfs.put(devAddr, chConf);
	}
}
