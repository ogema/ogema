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
		return new RecordedDataCache(encodedRecordedData, filename);
	}
	
	void clearCache() {
		valueCache.invalidateAll();
	}
	
	/**
	 * One instance per FileObject
	 */
	final class RecordedDataCache {
		
		private final String key;
		
		private RecordedDataCache(String recordedDataId, String file) {
			assert !recordedDataId.contains("/") : "Illegal character \"/\" in recorded data id " + recordedDataId; 
			assert !file.contains("/") : "Illegal character \"/\" in filename " + file; 
			this.key = recordedDataId + "/" + file;
		}
		
		void cache(List<SampledValue> values) {
			SlotsDbCache.this.cache(key, values);
		}
		
		void invalidate() {
			SlotsDbCache.this.invalidate(key);
		}
		
		List<SampledValue> getCache() {
			return SlotsDbCache.this.getCache(key);
		}
		
	}
	
}
