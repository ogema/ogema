/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ogema.impl.persistence;

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
				while ((ch = db.resourceIO.fifo.poll()) != null) {
					if (ch.status == ChangeInfo.DELETED) {
						db.resourceIO.offsetByID.remove(ch.id);
						// db.resourceIO.changes.remove(ch.id);
						fileChanged = true;
						continue;
					}
					TreeElementImpl e = db.resNodeByID.get(ch.id);
                    if (e.isNonpersistent() && ch.status == ChangeInfo.VALUE_CHANGED) {
                        assert false : "NonPersistent resource has ChangeInfo.VALUE_CHANGED";
                        continue;
                    }
					db.resourceIO.storeResource(e);
					fileChanged = true;
				}
				if (fileChanged) {
					db.resourceIO.writeEntry();
					db.resourceIO.updateDirectory();
				}
				running = false;
				db.resourceIO.changes.clear();
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
			// check if the resource with this is is already queued
			Change change = db.resourceIO.changes.get(resID);
			if (change != null) {
				// change the status of the change object only if it wasn't deleted before
				if (change.status != ChangeInfo.DELETED)
					change.status = changeInfo;
			}
			else {
				change = new Change(resID, changeInfo);
				db.resourceIO.changes.put(resID, change);
				db.resourceIO.fifo.offer(change);

			}
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
