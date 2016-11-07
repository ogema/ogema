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

import java.util.List;

import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.EventType;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.IllegalConversionException;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.NoSuchResourceException;
import org.ogema.model.locations.Room;

public class ReadChannelListener implements ChannelEventListener, ThreadControl {
	private ChannelAccess channelAccess;
	Room room;
	private int samplingRate;
	private long lastRead;
	private OgemaLogger logger;
	private ChannelLocator chloc;

	public ReadChannelListener(Room r, int rate, long timeStamp, OgemaLogger logger, ChannelLocator channelLocator) {
		this.room = r;
		this.samplingRate = rate;
		this.lastRead = timeStamp;
		this.logger = logger;
		this.chloc = channelLocator;
	}

	@Override
	public void channelEvent(EventType type, List<SampledValueContainer> channels) {
		try {
			for (SampledValueContainer container : channels) {
				if (room == null)
					return;
				ChannelLocator cl = container.getChannelLocator();
				if (!cl.equals(chloc)) {
					logger.error("Misleaded callback %s to %s", cl.toString(), chloc.toString());
					continue;
				}
				SampledValue val = container.getSampledValue();
				long value = val.getValue().getLongValue();
				int jitterV = (int) (value - lastRead) - samplingRate;
				TimeResource time = room.getSubResource("read");
				IntegerResource jitter = time.getSubResource("jitter");
				time.setValue(System.currentTimeMillis() - value);
				jitter.setValue(jitterV);
				lastRead = value;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (NoSuchResourceException e) {
			e.printStackTrace();
		} catch (IllegalConversionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {

	}
}
