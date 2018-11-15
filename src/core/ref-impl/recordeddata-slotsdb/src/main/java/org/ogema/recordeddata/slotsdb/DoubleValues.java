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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.ExecutionException;
import org.ogema.core.channelmanager.measurements.DoubleValue;

/**
 *
 * @author jlapp
 */
public class DoubleValues {

    private final static LoadingCache<Double, DoubleValue> CACHE
            = CacheBuilder.newBuilder().maximumSize(4096).build(new CacheLoader<Double, DoubleValue>() {
                @Override
                public DoubleValue load(Double key) throws Exception {
                    return new DoubleValue(key);
                }
            });

    public static DoubleValue of(float f) {
        try {
            return CACHE.get((double) f);
        } catch (ExecutionException ex) {
            return new DoubleValue(f);
        }
    }

    public static DoubleValue of(Double d) {
        try {
            return CACHE.get(d);
        } catch (ExecutionException ex) {
            return new DoubleValue(d);
        }
    }

}
