package org.ogema.driver.iec62056p21;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.openmuc.j62056.Connection;
import org.openmuc.j62056.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Iec62056p21Connection {

	private final static Logger logger = LoggerFactory.getLogger(Iec62056p21Connection.class);
	
	/**
	 * j62056 connection
	 */
	private final org.openmuc.j62056.Connection connection;

	/**
	 * Channels which are assigned to the connection
	 */
	private Map<String, ChannelLocator> channels = new HashMap<String, ChannelLocator>();

	private boolean echoHandling = false;
	
	private int baudRateChangeDelay = 0;

	/**
	 * Consturctor
	 */
	public Iec62056p21Connection(String deviceAddress, String settings) throws ConnectionException {

		logger.debug("create new Iec62056p21Connection deviceAddress: " +  deviceAddress + " Settings: " +  settings);
		
		initParameters(settings);

		connection = new Connection(deviceAddress, echoHandling, baudRateChangeDelay);
		try {
			connection.open();
		} catch (IOException e) {
			throw new ConnectionException("Unable to open local serial port: " + deviceAddress, e);
		}

		try {
			connection.read();
		} catch (IOException e) {
			connection.close();
			throw new ConnectionException("IOException trying to read meter: " + deviceAddress + ": " + e.getMessage(),
					e);
		} catch (TimeoutException e) {
			e.printStackTrace();
			throw new ConnectionException("Read timed out: " + e.getMessage());
		}

	}

	/**
	 * Interpret the settings
	 */
	private void initParameters(String settings) {
		if (!settings.equals("")) {

			// split whitespace
			String[] args = settings.split("\\s+");

			if (args.length > 2) {
				throw new IllegalArgumentException("More than two arguments in the settings are not allowed.");
			}

			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-e")) {
					echoHandling = true;
				}
				else if (args[i].equals("-d")) {
					i++;
					if (i == args.length) {
						throw new IllegalArgumentException(
								"No baudRateChangeDelay was specified after the -d parameter");
					}
					try {
						baudRateChangeDelay = Integer.parseInt(args[i]);
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("Specified baudRateChangeDelay is not an integer.");
					}
				}
				else {
					throw new IllegalArgumentException("Found unknown argument in settings: " + args[i]);
				}
			}
		}
	}

	/**
	 * Assigns the channel to this connection
	 */
	public void addChannel(ChannelLocator channel) {
		channels.put(channel.getChannelAddress(), channel);
	}

	/**
	 * Removes the channel. It's no longer assigned to this connection.
	 */
	public void removeChannel(ChannelLocator channel) {
		channels.remove(channel.getChannelAddress());

		// TODO Should the connection be closed if no channel is assigned to the connection anymore?
	}

	public void disconnect() {
		channels.clear();
		connection.close();
	}

	public List<DataSet> read() throws IOException, TimeoutException {
		return connection.read();
	}

}
