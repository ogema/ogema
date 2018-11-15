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

	private final Map<String, DefaultRecordedData> recordedData = new HashMap<>();

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

	public DefaultRecordedData getRecordedData(TreeElement el, boolean create) {
		synchronized (recordedData) {
			DefaultRecordedData d = recordedData.get(el.getLocation());
			if (d == null && create) {
				d = new DefaultRecordedData(rda, scheduler, executor, el);
				recordedData.put(el.getLocation(), d);
			}
			return d;
		}
	}

	public void close() {
		synchronized (recordedData) {
			for (Map.Entry<String, DefaultRecordedData> e : recordedData.entrySet()) {
				e.getValue().close();
			}
		}
		executor.shutdown();
	}

}
