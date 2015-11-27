package org.ogema.channelmanager.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.slf4j.Logger;

public class SamplingSchedule {

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

	Map<Long, List<Channel>> schedule;
	Long currentTimestamp;

	/**
	 * Creates a sampling schedule. For each channel the (theoretical) last sampling timestamp is calculated. This is
	 * the timestamp the channel should have been sampled the last time based on the current time and todays midnight.
	 * <p>
	 * Example: Constructor is called at 12:25:23,234 for a channel with a sampling period of 2000 ms. The constructor
	 * calculates the ms passed from todays midnight 00:00:00,000. Afterwards it calculates the number of samplings
	 * which should have occurred since midnight. The before mentioned (theoretical) last sampling timestamp is:
	 * samplingsToday * samplingPeriod + todaysMidnightTimestamp = 12:25:22,000. (nice and tidy timestamp for further
	 * sampling)
	 * <p>
	 * This approach ensures that all channels, which may have different sampling periods, have the same time base.
	 * Through this it is possible to sample channels with exact overlapping sampling periods together. Example: Channel
	 * A is sampled every 2 and Channel B every 3 seconds. After 6 seconds both channels are sampled together.
	 */
	// TODO: Creating a new schedule at midnight or just before midnight might be a problem since midnight is the
	// reference time for the schedule. Might have side effects, Not tested yet.
	public SamplingSchedule(List<Channel> channels) {

		schedule = new TreeMap<Long, List<Channel>>();

		long todaysMidnightTimestamp = getTodaysMidnight().getTimeInMillis();
		long millisecondsPassedToday = System.currentTimeMillis() - todaysMidnightTimestamp;

		for (Channel channel : channels) {
			long samplingPeriod = channel.getConfiguration().getSamplingPeriod();
			long samplingsToday = millisecondsPassedToday / samplingPeriod;
			long lastSamplingTimestamp = samplingsToday * samplingPeriod + todaysMidnightTimestamp;

			addEntry(lastSamplingTimestamp, channel);
		}

		printSchedule();
	}

	/**
	 * Returns a calender from today at 00:00:00,000 o'clock
	 */
	private GregorianCalendar getTodaysMidnight() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	/**
	 * Adds channel to the schedule
	 * 
	 * @param lastSamplingTime
	 *            Last time the channel was sampled
	 * @param channel
	 *            Channel to add to the schedule
	 */
	private void addEntry(long lastSamplingTime, Channel channel) {

		// calculate the new sampling timestamp for this channel
		long samplingTimestamp = lastSamplingTime + channel.getConfiguration().getSamplingPeriod();

		if (schedule.containsKey(samplingTimestamp)) {
			// add channel to existing schedule entry
			schedule.get(samplingTimestamp).add(channel);
		}
		else {
			// create a new schedule entry for this channel
			List<Channel> newList = new ArrayList<Channel>();
			newList.add(channel);
			schedule.put(samplingTimestamp, newList);
		}
	}

	/**
	 * @return the first element of the schedule.
	 */
	public SamplingScheduleElement getFirstElement() {
		Entry<Long, List<Channel>> entry = schedule.entrySet().iterator().next();
		currentTimestamp = entry.getKey();
		return new SamplingScheduleElement(entry.getKey(), entry.getValue());
	}

	/**
	 * Updates the schedule. Adds new schedule entries for all channels of the last schedule event. afterwards the last
	 * schedule event is deleted.
	 */
	public void update() {
		for (Channel channel : schedule.get(currentTimestamp)) {
			addEntry(currentTimestamp.longValue(), channel);
		}

		schedule.remove(currentTimestamp);

		printSchedule();
	}

	/**
	 * Prints the current schedule for debugging purpose
	 */
	public void printSchedule() {

		if (logger.isTraceEnabled()) {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:    ss    .SSS");

			StringBuilder sb = new StringBuilder();
			sb.append("\nSCHEDULE \n**************************** \n");

			for (Map.Entry<Long, List<Channel>> entry : schedule.entrySet()) {
				sb.append(sdf.format(new Date(entry.getKey())) + "\n");
				for (Channel channel : entry.getValue()) {
					sb.append("      " + channel.getConfiguration().getChannelLocator().getChannelAddress() + " "
							+ channel.getConfiguration().getSamplingPeriod() + "\n");
				}
			}
			sb.append("-------------------------------\n");
			logger.trace(sb.toString());
		}
	}
}
