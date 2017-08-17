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
package org.ogema.persistence.impl.faketree;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.ogema.resourcemanager.virtual.VirtualTreeElement;

/**
 *
 * @author jlapp
 */
public class ScheduleTreeElementFactory {

    private final Cache<String, ScheduleTreeElement> cache = CacheBuilder.newBuilder().softValues().build();
    
    public ScheduleTreeElement get(final VirtualTreeElement element) {
        try {
            ScheduleTreeElement e = cache.get(element.getPath(), new Callable<ScheduleTreeElement>() {
                @Override
                public ScheduleTreeElement call() throws Exception {
                    return new ScheduleTreeElement(element);
                }
            });
            if (!e.baseElement.isVirtual()) {
                e.create();
            }
            return e;
        } catch (ExecutionException ee) {
            return new ScheduleTreeElement(element);
        }
    }

}
