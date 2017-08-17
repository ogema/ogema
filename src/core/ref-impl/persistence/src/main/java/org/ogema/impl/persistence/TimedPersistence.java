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
	final Timer timer;
	int storePeriod;

	private ResourceDBImpl db;

	volatile boolean inTX;
	volatile boolean running;

	public TimedPersistence(ResourceDBImpl db) {
		this.db = db;
		// check if the property to activate persistence debugging is set
		storePeriod = Integer.getInteger(DBConstants.PROP_NAME_TIMEDPERSISTENCE_PERIOD, DEFAULT_STOREPERIOD);
		this.timer = new Timer("Storage-TimedPersistence-" + db.name);
	}

	TimerTask storageTask = new TimerTask() {

		@Override
		synchronized public void run() {
			/*
			 * If the previous storage not yet finished or resource management has reported a transaction, no storage
			 * must be triggered.
			 */
			if (running || inTX || db.resourceIO == null || db.resourceIO.changes.size() <= 0)
				return;
			running = true;
			try {
				/*
				 * The policy for the compaction of the data archive file decides if a compaction is required.
				 */
				db.resourceIO.check4Compaction();

				boolean fileChanged = false;
				Change ch = null;
				Set<Entry<Integer, Change>> tlrs = db.resourceIO.changes.entrySet();
				for (Map.Entry<Integer, Change> entry : tlrs) {

					ch = entry.getValue();
					if (ch.status == ChangeInfo.DELETED) {
						db.resourceIO.offsetByID.remove(ch.id);
						db.resourceIO.changes.remove(ch.id);
						fileChanged = true;
						continue;
					}
					// Update persistent data in the archive file...
					// if (stop) {
					// timer.cancel();
					// fileChanged = false;
					// break;
					// }
					TreeElementImpl e = db.resNodeByID.get(ch.id);
					db.resourceIO.storeResource(e);
					// ...and remove the changed info.
					db.resourceIO.changes.remove(ch.id);
					fileChanged = true;
				}
				if (fileChanged) {
					db.resourceIO.writeEntry();
					db.resourceIO.updateDirectory();
				}
				running = false;
			} catch (Throwable e) {
				e.printStackTrace();
				running = false;
			}
		}
	};

	public int getStorePeriod() {
		return storePeriod;
	}

	public void setStorePeriod(int storePeriod) {
		this.storePeriod = storePeriod;
	}

	@Override
	public void store(int resID, org.ogema.persistence.PersistencePolicy.ChangeInfo changeInfo) {
		synchronized (storageTask) {
			db.resourceIO.changes.put(resID, new Change(resID, changeInfo));
		}
	}

	@Override
	public void finishTransaction(int toplevel) {
		this.inTX = false;
	}

	@Override
	public void startTransaction(int toplevel) {
		synchronized (storageTask) {
			this.inTX = true;
		}
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
		timer.schedule(storageTask, storePeriod, storePeriod);
	}

	@Override
	public void stopStorage() {
		timer.cancel();
		timer.purge();
	}

	@Override
	public Object getStorageLock() {
		return storageTask;
	}

	@Override
	public void triggerStorage() {
		storageTask.run();
	}
}
