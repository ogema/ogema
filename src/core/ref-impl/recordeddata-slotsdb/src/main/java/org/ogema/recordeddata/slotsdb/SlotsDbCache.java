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
package org.ogema.recordeddata.slotsdb;

import java.util.List;

import org.ogema.core.channelmanager.measurements.SampledValue;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

class SlotsDbCache {

	/*
	 *Map< encoded recorded data id + "/" + filename -> values> 
	 */
	private final Cache<String, List<SampledValue>> valueCache = CacheBuilder.newBuilder().softValues().build();
	
	private final void cache(final String accessToken, final List<SampledValue> values) {
		valueCache.put(accessToken, values);
	}
	
	private final void invalidate(final String accessToken) {
		valueCache.invalidate(accessToken);
	}
	
	private final List<SampledValue> getCache(final String accessToken) {
		return valueCache.getIfPresent(accessToken);
	}
	
	final RecordedDataCache getCache(String encodedRecordedData, String filename) {
		return new RecordedDataCacheImpl(this, encodedRecordedData, filename);
	}
	
	void clearCache() {
		valueCache.invalidateAll();
	}
	
	/**
	 * One instance per FileObject
	 */
	private final static class RecordedDataCacheImpl implements RecordedDataCache {
		
		private final String key;
		private final SlotsDbCache globalCache;
		
		private RecordedDataCacheImpl(SlotsDbCache cache, String recordedDataId, String file) {
			this.globalCache = cache;
			assert !recordedDataId.contains("/") : "Illegal character \"/\" in recorded data id " + recordedDataId; 
			assert !file.contains("/") : "Illegal character \"/\" in filename " + file; 
			this.key = recordedDataId + "/" + file;
		}
		
		public void cache(List<SampledValue> values) {
			globalCache.cache(key, values);
		}
		
		public void invalidate() {
			globalCache.invalidate(key);
		}
		
		public List<SampledValue> getCache() {
			return globalCache.getCache(key);
		}
		
	}
	
}
