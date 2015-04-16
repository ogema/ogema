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
package org.ogema.resourcemanager.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.ogema.recordeddata.DataRecorder;
import org.ogema.resourcemanager.impl.timeseries.DefaultRecordedData;
import org.ogema.resourcetree.TreeElement;
import org.ogema.timer.TimerScheduler;

/**
 * 
 * @author Jan Lapp, Fraunhofer IWES
 */
public class RecordedDataManager {

	protected final ExecutorService executor;
	protected final ResourceDBManager dbman;
	protected final DataRecorder rda;
	protected final TimerScheduler scheduler;

	private final Map<TreeElement, DefaultRecordedData> recordedData = new HashMap<>();

	public RecordedDataManager(ResourceDBManager dbman, DataRecorder rda, TimerScheduler scheduler) {
		ThreadFactory tfac = new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("RecordedData log thread (" + RecordedDataManager.this + ")");
				return t;
			}
		};
		this.executor = Executors.newSingleThreadExecutor(tfac);
		this.dbman = dbman;
		this.rda = rda;
		this.scheduler = scheduler;
	}

	public DefaultRecordedData getRecordedData(TreeElement el) {
		synchronized (recordedData) {
			DefaultRecordedData d = recordedData.get(el);
			if (d == null) {
				d = new DefaultRecordedData(rda, scheduler, executor, el);
				recordedData.put(el, d);
			}
			return d;
		}
	}

	public void close() {
		synchronized (recordedData) {
			for (Map.Entry<TreeElement, DefaultRecordedData> e : recordedData.entrySet()) {
				e.getValue().close();
			}
		}
		executor.shutdown();
	}

}
