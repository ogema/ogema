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
