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
package org.ogema.channelmanager.impl;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.EventType;
import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelUpdateListener;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.slf4j.Logger;

public class DeviceReaderThread extends Thread {

	private final CommDevice device;
	private final ChannelDriver driver;

	private boolean running = true;

	private boolean configurationUpdated = false;

	private boolean driverSupportsAsyncRead = false;

	private final Map<ChannelLocator, List<WeakReference<ChannelEventListener>>> updateListeners = new HashMap<ChannelLocator, List<WeakReference<ChannelEventListener>>>();

	private final Map<ChannelLocator, List<WeakReference<ChannelEventListener>>> changedListeners = new HashMap<ChannelLocator, List<WeakReference<ChannelEventListener>>>();

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

	/**
	 * Register new listener if not already available in list "channelListeners". Removes listener from list that are
	 * null.
	 * 
	 * @param newListener
	 *            new listener to be added
	 * @param channelListeners
	 *            list of all available listeners for one Channel
	 */
	private void addChannelToListenerList(ChannelEventListener newListener,
			List<WeakReference<ChannelEventListener>> channelListeners) {
		boolean foundListener = false;

		for (WeakReference<ChannelEventListener> listenerReference : channelListeners) {
			ChannelEventListener listener = listenerReference.get();

			if (listener == null) {
				channelListeners.remove(listenerReference);
				continue;
			}

			if (listener == newListener) {
				foundListener = true;
				break;
			}
		}

		if (!foundListener) {
			channelListeners.add(new WeakReference<ChannelEventListener>(newListener));
		}
	}

	public void removeChannel(Channel channel) {
		ChannelLocator locator = channel.getConfiguration().getChannelLocator();

		updateListeners.remove(locator);
		changedListeners.remove(locator);

		driver.channelRemoved(locator);
	}

	public void addChannelUpdateListener(Channel channel, ChannelEventListener newUpdateListener) {
		ChannelLocator channelLocator = channel.getConfiguration().getChannelLocator();

		List<WeakReference<ChannelEventListener>> channelListeners = updateListeners.get(channelLocator);

		if (channelListeners == null) {
			channelListeners = new LinkedList<WeakReference<ChannelEventListener>>();

			updateListeners.put(channelLocator, channelListeners);
		}

		addChannelToListenerList(newUpdateListener, channelListeners);
	}

	public void addChannelChangedListener(Channel channel, ChannelEventListener newChangedListener) {
		ChannelLocator channelLocator = channel.getConfiguration().getChannelLocator();

		List<WeakReference<ChannelEventListener>> channelListeners = changedListeners.get(channelLocator);

		if (channelListeners == null) {
			channelListeners = new LinkedList<WeakReference<ChannelEventListener>>();

			changedListeners.put(channelLocator, channelListeners);
		}

		addChannelToListenerList(newChangedListener, channelListeners);
	}

	public DeviceReaderThread(CommDevice device, ChannelDriver driver) {
		this.device = device;
		this.driver = driver;
		driverSupportsAsyncRead = false;

	}

	public void updateConfiguration() {
		logger.debug("DRC: update configuration called!");
		configurationUpdated = true;
	}

	private class SamplingScheduleElement {

		private final long waitingTime;
		private final List<Channel> channels;

		public SamplingScheduleElement(long waitingTime, List<Channel> channels) {
			this.waitingTime = waitingTime;
			this.channels = channels;
		}

		public long getWaitingTime() {
			return this.waitingTime;
		}

		public List<Channel> getChannels() {
			return this.channels;
		}
	}

	@Override
	public void run() {

		if (configurationUpdated) {
			configurationUpdated = false;
		}

		long nextSamplingTime = System.currentTimeMillis();

		while (running) {
			List<SamplingScheduleElement> samplingSchedule = constructSamplingSchedule();
			while (configurationUpdated) {
				configurationUpdated = false;
				samplingSchedule = constructSamplingSchedule();
			}

			if (samplingSchedule.size() == 0) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}

			}
			else {
				while (!configurationUpdated || samplingSchedule.size() > 0) {
					if (configurationUpdated) {
						break;
					}

					SamplingScheduleElement scheduleElement = samplingSchedule.get(0);
					nextSamplingTime = scheduleElement.getWaitingTime();

					while (System.currentTimeMillis() < nextSamplingTime) {

						if (configurationUpdated) {
							break;
						}

						try {
							int sleepTime = 1000;
							if (((nextSamplingTime - System.currentTimeMillis()) < sleepTime)) {
								Thread.sleep(nextSamplingTime - System.currentTimeMillis());
							}
							else {
								Thread.sleep(sleepTime);
							}
							Thread.sleep(1);

						} catch (InterruptedException e) {
						}
					}

					if (configurationUpdated) {
						break;
					}

					/* Create list of channels to sample and backup old data for event detection */
					List<SampledValueContainer> nextChannels = new LinkedList<SampledValueContainer>();
					List<SampledValue> oldValues = new LinkedList<SampledValue>();

					for (Channel channel : scheduleElement.getChannels()) {

						nextChannels.add(channel.getSampledValueContainer());
						oldValues.add(channel.getSampledValueContainer().getSampledValue());

					}

					if (configurationUpdated) {
						break;
					}
					for (Channel channel : scheduleElement.getChannels()) {
						if (configurationUpdated) {
							break;
						}
						long nextSample = channel.getConfiguration().getSamplingPeriod() + scheduleElement.waitingTime;
						for (int i = 0; i < samplingSchedule.size(); i++) {
							if (configurationUpdated) {
								break;
							}

							if (nextSample < samplingSchedule.get(i).waitingTime) {
								if (configurationUpdated) {
									break;
								}
								List<Channel> channels = new LinkedList<Channel>();
								channels.add(channel);
								SamplingScheduleElement element1 = new SamplingScheduleElement(nextSample, channels);
								samplingSchedule.add(i, element1);
								break;
							}
							else if (samplingSchedule.get(i).waitingTime == nextSample) {
								if (configurationUpdated) {
									break;
								}
								List<Channel> channels = samplingSchedule.get(i).channels;
								channels.add(channel);
								SamplingScheduleElement element1 = new SamplingScheduleElement(nextSample, channels);
								samplingSchedule.set(i, element1);
							}
							else if (i + 1 >= samplingSchedule.size()) {
								if (configurationUpdated) {
									break;
								}
								List<Channel> channels = new LinkedList<Channel>();
								channels.add(channel);
								SamplingScheduleElement element1 = new SamplingScheduleElement(nextSample, channels);
								samplingSchedule.add(i + 1, element1);
								break;
							}
							if (configurationUpdated) {
								break;
							}

						}
						if (configurationUpdated) {
							break;
						}

					}
					if (configurationUpdated) {
						break;
					}
					if (samplingSchedule.size() > 1) {
						samplingSchedule.remove(0);
					}
					if (configurationUpdated) {
						break;
					}
					if (nextChannels.size() > 0) {
						triggerSampling(nextChannels, oldValues);
					}

					if (configurationUpdated) {
						break;
					}
				}

			}
		}
	}

	private void triggerSampling(List<SampledValueContainer> nextChannels, List<SampledValue> oldValues) {
		if (driverSupportsAsyncRead) {
			driver.readChannels(nextChannels, new DeviceReaderChannelUpdateListener(nextChannels, oldValues));
		}
		else {
			try {
				driver.readChannels(nextChannels);

			} catch (Exception e) {
				// TODO Auto-generated catch block

				e.printStackTrace();
			}

			checkForEvents(oldValues, nextChannels);
		}
	}

	private class DeviceReaderChannelUpdateListener implements ChannelUpdateListener {

		private List<SampledValueContainer> channelValues = null;
		private List<SampledValue> oldValues = null;
		private final long requestTime;

		public DeviceReaderChannelUpdateListener(List<SampledValueContainer> channelValues, List<SampledValue> oldValues) {
			this.channelValues = channelValues;
			this.oldValues = oldValues;
			requestTime = System.currentTimeMillis();
		}

		@Override
		public void channelsUpdated(List<SampledValueContainer> channels) {
			checkForEvents(oldValues, channels);
		}

		@Override
		public void exceptionOccured(Exception e) {
			// set channel quality to invalid if last value is older then the request time;

			long currentTime = System.currentTimeMillis();

			for (SampledValueContainer channelValue : channelValues) {

				if (channelValue.getSampledValue() != null) {

					long timeStamp = channelValue.getSampledValue().getTimestamp();

					if (timeStamp < requestTime) {
						Value value = channelValue.getSampledValue().getValue();
						channelValue.setSampledValue(new SampledValue(value, currentTime, Quality.BAD));
					}
				}

			}
		}

	}

	private void checkForEvents(List<SampledValue> oldValues, List<SampledValueContainer> channels) {

		int i = 0;

		for (SampledValueContainer channelData : channels) {

			SampledValue oldValue = oldValues.get(i);

			List<SampledValueContainer> singleValueList = new LinkedList<SampledValueContainer>();

			singleValueList.add(channelData);

			ChannelLocator channelLocator = channelData.getChannelLocator();

			/* call update listeners */
			List<WeakReference<ChannelEventListener>> updateEventListeners = updateListeners.get(channelLocator);

			if (updateEventListeners != null) {
				callEventListeners(singleValueList, updateEventListeners);
			}

			/* call value changed listeners */
			SampledValue v = channelData.getSampledValue();
			if ((oldValue == null) || (v.getValue() != null && (!v.equals(oldValue.getValue())))
					|| (!v.getQuality().equals(oldValue.getQuality()))) {
				List<WeakReference<ChannelEventListener>> changedEventListeners = changedListeners.get(channelLocator);

				if (changedEventListeners != null) {
					callEventListeners(singleValueList, changedEventListeners);
				}
			}

			i++;
		}
	}

	private void callEventListeners(List<SampledValueContainer> singleValueList,
			List<WeakReference<ChannelEventListener>> listeners) {
		for (WeakReference<ChannelEventListener> listenerReference : listeners) {
			ChannelEventListener listener = listenerReference.get();

			if (listener == null) {
				listeners.remove(listenerReference);
			}
			else {
				listener.channelEvent(EventType.UPDATED, singleValueList);
			}
		}
	}

	private List<SamplingScheduleElement> constructSamplingSchedule() {

		List<Channel> deviceChannels = device.getChannels();

		List<SamplingScheduleElement> waitingList = new LinkedList<SamplingScheduleElement>();

		List<Long> samplingPeriodList = validSamplingPeriods(deviceChannels);

		if (samplingPeriodList.size() == 0) {
			return waitingList;
		}

		for (Channel channel : deviceChannels) {
			long samplingPeriod = System.currentTimeMillis() + channel.getConfiguration().getSamplingPeriod();
			if (waitingList.size() == 0) {

				List<Channel> channels = new LinkedList<Channel>();
				if (channel.getConfiguration().getSamplingPeriod() > 0) {
					channels.add(channel);
				}
				SamplingScheduleElement element1 = new SamplingScheduleElement(samplingPeriod, channels);
				waitingList.add(0, element1);

			}
			else {

				for (int i = 0; i < waitingList.size(); i++) {
					long samplingPeriod2 = System.currentTimeMillis() + channel.getConfiguration().getSamplingPeriod();
					if (samplingPeriod2 < waitingList.get(i).waitingTime) {

						List<Channel> channels = new LinkedList<Channel>();
						if (channel.getConfiguration().getSamplingPeriod() > 0) {
							channels.add(channel);
						}
						SamplingScheduleElement element1 = new SamplingScheduleElement(samplingPeriod2, channels);
						waitingList.add(i, element1);

						break;
					}
					else if (waitingList.get(i).waitingTime == samplingPeriod2) {

						List<Channel> channels = waitingList.get(i).channels;
						if (channel.getConfiguration().getSamplingPeriod() > 0) {
							channels.add(channel);
						}
						SamplingScheduleElement element1 = new SamplingScheduleElement(samplingPeriod2, channels);
						waitingList.set(i, element1);

					}
					else if (i + 1 == waitingList.size()) {

						List<Channel> channels = new LinkedList<Channel>();
						if (channel.getConfiguration().getSamplingPeriod() > 0) {
							channels.add(channel);
						}
						SamplingScheduleElement element1 = new SamplingScheduleElement(samplingPeriod2, channels);
						waitingList.add(i + 1, element1);

						break;
					}

				}

			}
		}

		return waitingList;
	}

	private boolean isPeriodicPollingChannel(Channel channel) {
		return (channel.getConfiguration().getSamplingPeriod() > 0);
	}

	private List<Long> validSamplingPeriods(List<Channel> channels) {

		List<Long> samplingPeriods = new ArrayList<Long>(channels.size());

		for (Channel channel : channels) {
			if (isPeriodicPollingChannel(channel)) {
				samplingPeriods.add(channel.getConfiguration().getSamplingPeriod());
			}
		}

		return samplingPeriods;
	}
}
