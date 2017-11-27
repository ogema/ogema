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
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.Callable;
import org.ogema.resourcemanager.virtual.VirtualTreeElement;

/**
 *
 * @author jlapp
 */
public class ScheduleTreeElementFactory {

    private final Cache<String, ScheduleTreeElement> cache = CacheBuilder.newBuilder().softValues().build();
    
	// in cache.get a guava-internal class may be loaded which accesses a system property in 
    // its static initializer... bound to fail if there is an app in the call stack, hence the privileged access
	// TODO guava issue?
    public ScheduleTreeElement get(final VirtualTreeElement element) {
        try {
            final ScheduleTreeElement e = AccessController.doPrivileged(new PrivilegedExceptionAction<ScheduleTreeElement>() {

				@Override
				public ScheduleTreeElement run() throws Exception {
					return cache.get(element.getPath(), new ScheduleTreeElementCallable(element));
				}
			});    		
            if (!e.baseElement.isVirtual()) {
                e.create();
            }
            return e;
        } catch (PrivilegedActionException ee) {
            return new ScheduleTreeElement(element);
        }
    }
    
    private final static class ScheduleTreeElementCallable implements Callable<ScheduleTreeElement> {
    	
    	final VirtualTreeElement element;
    	
    	private ScheduleTreeElementCallable(VirtualTreeElement element) {
    		this.element = element;
		}
    	
    	@Override
        public ScheduleTreeElement call() throws Exception {
            return new ScheduleTreeElement(element);
        }
    }
    
}
