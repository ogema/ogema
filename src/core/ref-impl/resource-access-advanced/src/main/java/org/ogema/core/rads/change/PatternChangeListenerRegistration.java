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

public class PatternChangeListenerRegistration implements RegisteredPatternChangeListener {
	
	private final ResourcePattern<?> pattern;
	private final ApplicationManager am;
	private final PatternChangeListener<?> patternListener;
	private final List<PatternStructureChangeListener> structureListeners;
	private final List<PatternValueChangeListener> valueListeners;
	
	public PatternChangeListenerRegistration(ResourcePattern<?> pattern, ApplicationManager am, 
			PatternChangeListener<?> patternListener, final Class<? extends ResourcePattern<?>> patternType) {
		this.pattern = pattern;
		this.am = am;
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
				am.getLogger().error("Error initializing pattern change listeners: ",e);
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
				am.getLogger().error("Error initializing pattern change listeners: ",e);
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
	void trigger(CompoundResourceEvent event) {
		// System.out.println("   trigger called for " + event.getSource());
		synchronized(this) {
			events.add(event);
			if (running) return;
			running = true;
		}
		Callable<Boolean> trigger = new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
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
