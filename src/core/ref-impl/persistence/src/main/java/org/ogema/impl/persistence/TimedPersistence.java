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
package org.ogema.impl.persistence;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.ogema.persistence.DBConstants;
import org.ogema.persistence.PersistencePolicy;

public class TimedPersistence implements PersistencePolicy {

	static final int DEFAULT_STOREPERIOD = 10 * 1000; // milliseconds
	Timer timer;
	int storePeriod;

	private ResourceDBImpl db;

	DBResourceIO resIO;

	boolean inTX;

	public TimedPersistence(ResourceDBImpl db) {
		resIO = db.resourceIO;
		this.db = db;
		// check if the property to activate persistence debugging is set
		String period = System.getProperty(DBConstants.PROP_NAME_TIMEDPERSISTENCE_PERIOD, null);
		if (period == null)
			storePeriod = DEFAULT_STOREPERIOD;
		else
			try {
				storePeriod = Integer.valueOf(period);
			} catch (NumberFormatException e) {
				storePeriod = DEFAULT_STOREPERIOD;
			}
		this.timer = new Timer("Storage-" + db.name);
	}

	TimerTask task = new TimerTask() {

		@Override
		synchronized public void run() {
			/*
			 * If the previous storage not yet finished or resource management has reported a transaction, no storage
			 * mustn't be triggered.
			 */
			if (running || inTX || resIO.changes.size() <= 0)
				return;
			running = true;
			try {
				/*
				 * The policy for the compaction of the data archive file decides if a compaction is required.
				 */
				if (resIO.compactionRequired()) {
					resIO.resDataFiles.updateNextOut();
					assert resIO.resDataFiles.out.size() == 0;
					resIO.currentDataFileName = resIO.resDataFiles.fileNew.getName();
					resIO.dbFileInitialOffset = 0;
					resIO.compact();
					running = false;
					return;
				}
				boolean fileChanged = false;
				Change ch = null;
				Set<Entry<Integer, Change>> tlrs = resIO.changes.entrySet();
				for (Map.Entry<Integer, Change> entry : tlrs) {

					ch = entry.getValue();
					TreeElementImpl e = db.resNodeByID.get(ch.id);
					if (ch.status == ChangeInfo.DELETED) {
						resIO.offsetByID.remove(ch.id);
						continue;
					}
					// Update persistent data in the archive file...
					if (stop) {
						timer.cancel();
						fileChanged = false;
						break;
					}
					resIO.storeResource(e);
					// ...and remove the changed info.
					resIO.changes.remove(ch.id);
					fileChanged = true;
				}
				if (fileChanged) {
					resIO.writeEntry();
					resIO.updateDirectory();
				}
				running = false;
			} catch (Throwable e) {
				e.printStackTrace();
				running = false;
			}
		}
	};
	private boolean running;
	private boolean stop;

	public int getStorePeriod() {
		return storePeriod;
	}

	public void setStorePeriod(int storePeriod) {
		this.storePeriod = storePeriod;
	}

	@Override
	public void store(int resID, org.ogema.persistence.PersistencePolicy.ChangeInfo changeInfo) {
		synchronized (task) {
			Change ch = resIO.changes.put(resID, new Change(resID, changeInfo));
			if (DBResourceIO.DEBUG && ch != null) {
				try {
					System.out.println(db.resNodeByID.get(resID).path + " storage status changed to "
							+ ch.status.name());
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void finishTransaction(int toplevel) {
		this.inTX = false;
	}

	@Override
	public void startTransaction(int toplevel) {
		this.inTX = true;
	}

	class Change {
		public Change(int resID, org.ogema.persistence.PersistencePolicy.ChangeInfo changeInfo) {
			id = resID;
			status = changeInfo;
		}

		int id;
		org.ogema.persistence.PersistencePolicy.ChangeInfo status;
	}

	@Override
	public void startStorage() {
		stop = false;
		timer.schedule(task, storePeriod, storePeriod);
	}

	@Override
	public void stopStorage() {
		stop = true;
		while (running) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
