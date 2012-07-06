/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.driver.homematic.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.ogema.driver.homematic.manager.Messages.Message;
import org.slf4j.Logger;

/**
 * This class handles outgoing messages that need a response.
 * 
 * @author baerthbn
 * 
 */
public class MessageHandler {
	private LocalDevice localDevice;
	public long MIN_WAITING_TIME = 500;
	private volatile List<Long> sentMessageAwaitingResponse = new ArrayList<Long>(); // <Token>
	private volatile Map<String, SendThread> runningThreads = new ConcurrentHashMap<String, SendThread>(); // <Deviceaddress,
	// Thread>
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("homematic-driver");

	public MessageHandler(LocalDevice device) {
		this.localDevice = device;
	}

	public void messageReceived(StatusMessage msg) {
		RemoteDevice device = localDevice.getDevices().get(msg.source);
		device.parseMsg(msg);
		logger.debug("Received ?-token: " + msg.rtoken);
		if (msg.type == 'E') { // Must be parsed
			device.setValue(msg.msg_data, msg.msg_flag, msg.msg_type);

		}
		else { // is "R"
			logger.debug("Received R-token: " + msg.rtoken);
			if (sentMessageAwaitingResponse.contains(msg.rtoken)) {
				if (runningThreads.containsKey(msg.source) && ((msg.cond & (byte) 0x80) == 0)) {
					SendThread sendThread = runningThreads.get(msg.source);
					sentMessageAwaitingResponse.remove(msg.rtoken);
					logger.debug("sentMessageAwaitingResponse removed " + msg.rtoken);
					synchronized (sendThread) {
						sendThread.notify();
					}
					logger.debug("Thread has been notified");
				}
			}
		}
	}

	public void sendMessage(Message message) {
		SendThread sendThread;
		if (runningThreads.containsKey(message.getDest())) {
			sendThread = runningThreads.get(message.getDest());
			sendThread.addMessage(message);
		}
		else {
			sendThread = new SendThread(message.getDest());
			sendThread.addMessage(message);
			runningThreads.put(sendThread.getDest(), sendThread);
			sendThread.start();
		}
	}

	public class SendThread extends Thread {

		private String dest;
		private int tries = 1;

		private volatile Map<Long, Message> unsentMessageQueue; // Messages waiting to be sent

		public SendThread(String dest) {
			this.dest = dest;
			unsentMessageQueue = new ConcurrentHashMap<Long, Message>();
		}

		@Override
		public void run() {
			try {
				while (!this.unsentMessageQueue.isEmpty()) {
					if (tries > 1 && tries < 6)
						SendThread.sleep(2000);
					else if (tries >= 6) {
						break;
					}
					logger.debug("Try: " + tries);
					Message entry = this.unsentMessageQueue.get(getSmallestKey());
					sentMessageAwaitingResponse.add(entry.getToken());
					logger.debug("sentMessageAwaitingResponse added " + entry.getToken());
					entry.refreshMsg_num();
					localDevice.sendFrame(entry.getFrame());
					TimerThread timerThread = new TimerThread();
					timerThread.start();
					synchronized (this) {
						try {
							this.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if (sentMessageAwaitingResponse.contains(entry.getToken())) {
						logger.warn("Response took to long ...");
						tries++;
						sentMessageAwaitingResponse.remove(entry.getToken());
					}
					else {
						this.unsentMessageQueue.remove(entry.getToken());
						logger.debug("unsentMessageQueue removed " + entry.getToken());
						tries = 1;
						timerThread.success = true;
					}
				}

				logger.debug("Running Thread removed");
				runningThreads.remove(this.getDest());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public String getDest() {
			return dest;
		}

		public void addMessage(Message message) {
			this.unsentMessageQueue.put(message.getToken(), message);
			logger.debug("unsentMessageQueue added " + message.getToken());
		}

		private long getSmallestKey() {
			Set<Long> keys = this.unsentMessageQueue.keySet();
			long min = 0xffffffffL;
			for (long key : keys) {
				if (key < min)
					min = key;
			}
			return min;
		}

		public class TimerThread extends Thread {

			public volatile boolean success = false;

			public void run() {
				try {
					Thread.sleep(3000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				synchronized (SendThread.this) {
					if (!success)
						SendThread.this.notify();
				}
			}

		}
	}
}
