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
package org.ogema.core.rads.change;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.rads.tools.RadFactory;
import org.ogema.core.rads.tools.ResourceFieldInfo;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.CompoundResourceEvent;
import org.ogema.core.resourcemanager.pattern.PatternChangeListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.resourcetree.listeners.ResourceLock;
import org.slf4j.Logger;

public class PatternChangeListenerRegistration implements RegisteredPatternChangeListener {
	
	private final ResourcePattern<?> pattern;
	private final ApplicationManager am;
	private final Logger logger;
	private final PatternChangeListener<?> patternListener;
	private final List<PatternStructureChangeListener> structureListeners;
	private final List<PatternValueChangeListener> valueListeners;
	
	public PatternChangeListenerRegistration(ResourcePattern<?> pattern, ApplicationManager am, Logger logger,
			PatternChangeListener<?> patternListener, final Class<? extends ResourcePattern<?>> patternType) {
		this.pattern = pattern;
		this.am = am;
		this.logger = logger;
		this.patternListener = patternListener;
		this.structureListeners = AccessController.doPrivileged(new PrivilegedAction<List<PatternStructureChangeListener>>() {

			@Override
			public List<PatternStructureChangeListener> run() {
				return initStructureListeners(patternType);
			}
		});
		this.valueListeners = AccessController.doPrivileged(new PrivilegedAction<List<PatternValueChangeListener>>() {

			@Override
			public List<PatternValueChangeListener> run() {
				return initValueListeners(patternType);
			}
		});
		
	}
	
	public void destroy() {
		for (PatternStructureChangeListener l: structureListeners) {
			l.getResource().removeStructureListener(l);
		}
		structureListeners.clear();
		for (PatternValueChangeListener l: valueListeners) {
			l.getResource().removeValueListener(l);
		}
		valueListeners.clear();
	}
	
	private List<PatternStructureChangeListener> initStructureListeners(Class<? extends ResourcePattern<?>> patternType) {
		List<PatternStructureChangeListener> list = new ArrayList<>();
		List<ResourceFieldInfo> fields = RadFactory.getResourceInfoRecursively(patternType, AccessPriority.PRIO_LOWEST); // TODO priority		
		for (ResourceFieldInfo i: fields) {
			try {
				if (i.requiresChangeListenerStructure()) {
					Field field  = i.getField();
					field.setAccessible(true);
					Resource target  = (Resource) field.get(pattern);
					if (target == null) 
						continue;
					PatternStructureChangeListener structureListener = new PatternStructureChangeListener(this, target);
					list.add(structureListener);
					target.addStructureListener(structureListener);
				}
			} catch (IllegalAccessException | IllegalArgumentException | SecurityException e) {
				logger.error("Error initializing pattern change listeners: ",e);
			}
		}
		return list;
	}
	
	private List<PatternValueChangeListener> initValueListeners(Class<? extends ResourcePattern<?>> patternType) {
		List<PatternValueChangeListener> list = new ArrayList<>();
		List<ResourceFieldInfo> fields = RadFactory.getResourceInfoRecursively(patternType, AccessPriority.PRIO_LOWEST); // TODO priority		
		for (ResourceFieldInfo i: fields) {
			try {
				if (i.requiresChangeListenerValue()) {
					Field field  = i.getField();
					field.setAccessible(true);
					Resource target  = (Resource) field.get(pattern);
					if (target == null) 
						continue;
					PatternValueChangeListener valueListener = new PatternValueChangeListener(target, i.changeListenerValueCallOnEveryUpdate(), am, this);
					list.add(valueListener);
					target.addValueListener(valueListener);
				}
			} catch (IllegalAccessException | IllegalArgumentException | SecurityException e) {
				logger.error("Error initializing pattern change listeners: ",e);
			}
		}
		return list;
	}
	
	@Override
	public ResourcePattern<?> getPatternInstance() {
		return pattern;
	}

	@Override
	public PatternChangeListener<?> getPatternListener() {
		return patternListener;
	}

	ApplicationManager getApplicationManager() {
		return am;
	}
	
	private boolean running = false;
	private final List<CompoundResourceEvent<?>> events = new ArrayList<>();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void trigger(final CompoundResourceEvent event) {
		// System.out.println("   trigger called for " + event.getSource());
		synchronized(this) {
			events.add(event);
			if (running) return;
			running = true;
		}
		Callable<Boolean> trigger = new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				if (!event.isActive())
					return false;
				List<CompoundResourceEvent<?>> eventsLocal;
                lockRead(); // ensures that no further events are added in the meantime
				try {
					synchronized(PatternChangeListenerRegistration.this) {
						running = false; 
						eventsLocal = new ArrayList<>(events);
						events.clear();
					}
				} finally {
					unlockRead();
				}
				// trigger common callback
				((PatternChangeListener) patternListener).patternChanged(pattern, eventsLocal);
				return true;
			}
		};
		am.submitEvent(trigger);
	}

	private void lockRead() {
		((ResourceLock) am.getResourceManagement()).lockRead();
	}
	
	private void unlockRead() {
		((ResourceLock) am.getResourceManagement()).unlockRead();
	}

	
}
