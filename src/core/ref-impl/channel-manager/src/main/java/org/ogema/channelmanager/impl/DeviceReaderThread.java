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

import java.io.IOException;
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

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

	private static final int DEFAULT_SLEEP_TIME = 1000;

	private final CommDevice device;
	private final ChannelDriver driver;
	volatile boolean running = true;

	// FIXME: change to volatile?
	private boolean configurationUpdated = false;

	private final Map<ChannelLocator, List<WeakReference<ChannelEventListener>>> updateListeners = new HashMap<ChannelLocator, List<WeakReference<ChannelEventListener>>>();

	private final Map<ChannelLocator, List<WeakReference<ChannelEventListener>>> changedListeners = new HashMap<ChannelLocator, List<WeakReference<ChannelEventListener>>>();

	public DeviceReaderThread(CommDevice device, ChannelDriver driver) {
		this.device = device;
		this.driver = driver;
		this.setName("DeviceReader_" + device.getDeviceLocator().toString());
	}

	@Override
	public void run() {

		SamplingSchedule samplingSchedule = null;

		while (running) {

			if (doChannelsWithPeriodicSamplingExist()) {

				// check if schedule is null. it can be null on the first run or when the configuration has changed
				if (samplingSchedule == null) {
					// create a new schedule
					samplingSchedule = new SamplingSchedule(device.getChannels());
				}

				SamplingScheduleElement scheduleElement = samplingSchedule.getFirstElement();

				try {
					waitTillNextSamplingTime(scheduleElement.getSamplingTimestamp());
				} catch (UpdateConfigurationException e) {
					// config has changed
					configurationUpdated = false;
					samplingSchedule = null;
					continue;
				}

				/* Create list of channels to sample and backup old data for event detection */
				List<SampledValueContainer> nextChannels = new LinkedList<SampledValueContainer>();
				List<SampledValue> oldValues = new LinkedList<SampledValue>();

				for (Channel channel : scheduleElement.getChannels()) {
					nextChannels.add(channel.getSampledValueContainer());
					oldValues.add(channel.getSampledValueContainer().getSampledValue());
				}

				samplingSchedule.update();

				triggerSampling(nextChannels, oldValues);
			}
			else {
				// non periodic channels available. no need to create a sampling schedule.
				threadSleep(DEFAULT_SLEEP_TIME);
				continue;
			}

		}

		logger.debug("DeviceReaderThread stopped");
	}

	/**
	 * Wait until the current time has reached the next sampling time
	 * 
	 * @throws UpdateConfigurationException
	 *             Is thrown when configuration has been updated.
	 */
	private void waitTillNextSamplingTime(long nextSamplingTimestamp) throws UpdateConfigurationException {

		while (System.currentTimeMillis() < nextSamplingTimestamp) {

			// abort if configuration has changed
			if (configurationUpdated) {
				throw new UpdateConfigurationException();
			}

			// check every second if the configuration has changed or nextSamplingTime is reached
			long timeToNextSamplingMs = nextSamplingTimestamp - System.currentTimeMillis();
			if (timeToNextSamplingMs < DEFAULT_SLEEP_TIME) {
				threadSleep(timeToNextSamplingMs);
			}
			else {
				threadSleep(DEFAULT_SLEEP_TIME);
			}

		}
	}

	private void triggerSampling(List<SampledValueContainer> nextChannels, List<SampledValue> oldValues) {

		try {
			driver.readChannels(nextChannels);
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// Value is set to null here. If an other default value is needed than the driver should handle the
			// IOException itself.
			for (SampledValueContainer container : nextChannels) {
				long now = System.currentTimeMillis();
				container.setSampledValue(new SampledValue(null, now, Quality.BAD));
			}
		}

		checkForEvents(oldValues, nextChannels);

	}

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

	public void updateConfiguration() {
		logger.debug("update configuration called!");
		configurationUpdated = true;
	}

	public void close() {
		running = false;
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

	private List<Channel> getAllPeriodicSampledChannels() {
		List<Channel> sampledChannels = new ArrayList<Channel>();
		for (Channel channel : device.getChannels()) {
			if (isPeriodicallySampledChannel(channel)) {
				sampledChannels.add(channel);
			}
		}
		return sampledChannels;
	}

	/**
	 * Checks if at least on channel with an sampling period > 0 exists
	 */
	private boolean doChannelsWithPeriodicSamplingExist() {

		boolean result = false;

		for (Channel channel : device.getChannels()) {
			if (isPeriodicallySampledChannel(channel)) {
				result = true;
				break;
			}
		}

		return result;
	}

	/**
	 * A channel is periodically sampled if sampling period is > 0
	 */
	private boolean isPeriodicallySampledChannel(Channel channel) {
		if (channel.getConfiguration().getSamplingPeriod() > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Helper method to avoid error handling (try catch blocks) business logic
	 */
	private void threadSleep(long sleepTimeMs) {
		try {
			Thread.sleep(sleepTimeMs);
		} catch (InterruptedException e) {
		}
	}

	// /**
	// * Sampling Schedule contains informations about when which channel is sampled the next time.
	// */
	// private class SamplingSchedule {
	//
	// Map<Long, List<Channel>> schedule;
	// Long currentTimestamp;
	//
	// /**
	// * Creates a sampling schedule. For each channel the (theoretical) last sampling timestamp is calculated. This
	// * is the timestamp the channel should have been sampled the last time based on the current time and todays
	// * midnight.
	// * <p>
	// * Example: Constructor is called at 12:25:23,234 for a channel with a sampling period of 2000 ms. The
	// * constructor calculates the ms passed from todays midnight 00:00:00,000. Afterwards it calculates the number
	// * of samplings which should have occurred since midnight. The before mentioned (theoretical) last sampling
	// * timestamp is: samplingsToday * samplingPeriod + todaysMidnightTimestamp = 12:25:22,000. (nice and tidy
	// * timestamp for further sampling)
	// * <p>
	// * This approach ensures that all channels, which may have different sampling periods, have the same time base.
	// * Through this it is possible to sample channels with exact overlapping sampling periods together. Example:
	// * Channel A is sampled every 2 and Channel B every 3 seconds. After 6 seconds both channels are sampled
	// * together.
	// */
	// // TODO: Creating a new schedule at midnight or just before midnight might be a problem since midnight is the
	// // reference time for the schedule. Not tested yet.
	// public SamplingSchedule() {
	//
	// schedule = new TreeMap<Long, List<Channel>>();
	//
	// long todaysMidnightTimestamp = getTodaysMidnight().getTimeInMillis();
	// long millisecondsPassedToday = System.currentTimeMillis() - todaysMidnightTimestamp;
	//
	// for (Channel channel : device.getChannels()) {
	// long samplingPeriod = channel.getConfiguration().getSamplingPeriod();
	// long samplingsToday = millisecondsPassedToday / samplingPeriod;
	// long lastSamplingTimestamp = samplingsToday * samplingPeriod + todaysMidnightTimestamp;
	//
	// addEntry(lastSamplingTimestamp, channel);
	// }
	//
	// if (logger.isDebugEnabled()) {
	// debug_printSchedule();
	// }
	// }
	//
	// /**
	// * Returns a calender from today at 00:00:00,000 o'clock
	// */
	// private GregorianCalendar getTodaysMidnight() {
	// GregorianCalendar cal = new GregorianCalendar();
	// cal.setTimeInMillis(System.currentTimeMillis());
	// cal.set(Calendar.HOUR_OF_DAY, 0);
	// cal.set(Calendar.MINUTE, 0);
	// cal.set(Calendar.SECOND, 0);
	// cal.set(Calendar.MILLISECOND, 0);
	// return cal;
	// }
	//
	// /**
	// * Adds channel to the schedule
	// *
	// * @param lastSamplingTime
	// * Last time the channel was sampled
	// * @param channel
	// * Channel to add to the schedule
	// */
	// private void addEntry(long lastSamplingTime, Channel channel) {
	//
	// // calculate the new sampling timestamp for this channel
	// long samplingTimestamp = lastSamplingTime + channel.getConfiguration().getSamplingPeriod();
	//
	// if (schedule.containsKey(samplingTimestamp)) {
	// // add channel to existing schedule entry
	// schedule.get(samplingTimestamp).add(channel);
	// }
	// else {
	// // create a new schedule entry for this channel
	// List<Channel> newList = new ArrayList<Channel>();
	// newList.add(channel);
	// schedule.put(samplingTimestamp, newList);
	// }
	// }
	//
	// /**
	// * @return the first element of the schedule (which
	// */
	// public SamplingScheduleElement getNextElement() {
	// Entry<Long, List<Channel>> entry = schedule.entrySet().iterator().next();
	// currentTimestamp = entry.getKey();
	// return new SamplingScheduleElement(entry.getKey(), entry.getValue());
	// }
	//
	// /**
	// * Updates the schedule. Adds new schedule entries for all channels of the last schedule event. afterwards the
	// * last schedule event is deleted.
	// */
	// public void update() {
	// for (Channel channel : schedule.get(currentTimestamp)) {
	// addEntry(currentTimestamp.longValue(), channel);
	// }
	//
	// schedule.remove(currentTimestamp);
	// debug_printSchedule();
	// }
	//
	// /**
	// * Prints the current schedul for debugging purpose
	// */
	// public void debug_printSchedule() {
	//
	// SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:    ss    .SSS");
	//
	// StringBuilder sb = new StringBuilder();
	// sb.append("\nSchedule \n **************************");
	//
	// for (Map.Entry<Long, List<Channel>> entry : schedule.entrySet()) {
	// sb.append(sdf.format(new Date(entry.getKey())));
	// for (Channel channel : entry.getValue()) {
	// sb.append("      " + channel.getConfiguration().getChannelLocator().getChannelAddress() + " "
	// + channel.getConfiguration().getSamplingPeriod());
	// }
	// }
	// sb.append("-------------------------------");
	// logger.debug(sb.toString());
	// }
	//
	// }
	//
	// private class SamplingScheduleElement {
	//
	//
	// }

	private class UpdateConfigurationException extends Exception {

		public UpdateConfigurationException() {
			super();
		}
	}

}
