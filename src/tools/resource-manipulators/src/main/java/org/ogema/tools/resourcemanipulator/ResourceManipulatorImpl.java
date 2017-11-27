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
package org.ogema.tools.resourcemanipulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.ResourceList;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.tools.resourcemanipulator.configurations.ManipulatorConfiguration;
import org.ogema.tools.resourcemanipulator.configurations.ProgramEnforcer;
import org.ogema.tools.resourcemanipulator.configurations.ScheduleManagement;
import org.ogema.tools.resourcemanipulator.configurations.ScheduleSum;
import org.ogema.tools.resourcemanipulator.configurations.Sum;
import org.ogema.tools.resourcemanipulator.configurations.Threshold;
import org.ogema.tools.resourcemanipulator.implementation.ProgramEnforcerImpl;
import org.ogema.tools.resourcemanipulator.implementation.ScheduleManagementImpl;
import org.ogema.tools.resourcemanipulator.implementation.ScheduleSumImpl;
import org.ogema.tools.resourcemanipulator.implementation.SumImpl;
import org.ogema.tools.resourcemanipulator.implementation.ThresholdConfigurationImpl;
import org.ogema.tools.resourcemanipulator.implementation.controllers.Controller;
import org.ogema.tools.resourcemanipulator.implementation.controllers.ProgramEnforcerController;
import org.ogema.tools.resourcemanipulator.implementation.controllers.ScheduleConfiguration;
import org.ogema.tools.resourcemanipulator.implementation.controllers.ScheduleManagementController;
import org.ogema.tools.resourcemanipulator.implementation.controllers.ScheduleSumController;
import org.ogema.tools.resourcemanipulator.implementation.controllers.SumController;
import org.ogema.tools.resourcemanipulator.implementation.controllers.ThresholdController;
import org.ogema.tools.resourcemanipulator.model.CommonConfigurationNode;
import org.ogema.tools.resourcemanipulator.model.ProgramEnforcerModel;
import org.ogema.tools.resourcemanipulator.model.ResourceManipulatorModel;
import org.ogema.tools.resourcemanipulator.model.ScheduleManagementModel;
import org.ogema.tools.resourcemanipulator.model.ScheduleSumModel;
import org.ogema.tools.resourcemanipulator.model.SumModel;
import org.ogema.tools.resourcemanipulator.model.ThresholdModel;
import org.osgi.framework.Bundle;

/**
 * Implementation of the new approach for the {@link ResourceManipulator}, which
 * creates configuration objects for the applications to use.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class ResourceManipulatorImpl implements ResourceManipulator, ResourceDemandListener<ResourceManipulatorModel> {

	private final static String targetName = "RESOURCE_MANIPULATOR_CONFIGURATIONS";
    private final ApplicationManager appMan;
    private final ResourceManagement resMan;
    private final ResourceAccess resAcc;
    private final OgemaLogger logger;
    private final String appId;
    private CommonConfigurationNode commonConfigurationNode;
    // a single controller manages all schedules; it is constructed when needed for the first time
    private ScheduleManagementController scheduleManagement = null;

    private final Map<ResourceManipulatorModel, Controller> controllerMap = new HashMap<>();

    public ResourceManipulatorImpl(ApplicationManager applicationManager) {
        appMan = applicationManager;
        resMan = applicationManager.getResourceManagement();
        resAcc = applicationManager.getResourceAccess();
        logger = applicationManager.getLogger();
//        appId = applicationManager.getAppID().getIDString(); //this is not even invariant under restart with unchanged configuration
        Bundle bdl = applicationManager.getAppID().getBundle();
        appId = bdl.getSymbolicName();
    }


    @Override
    public void start() {

        // find the common root node.
        final List<CommonConfigurationNode> existingNodes = resAcc.getToplevelResources(CommonConfigurationNode.class);
        if (existingNodes.isEmpty()) {
            final String name = resMan.getUniqueResourceName(targetName); // FIXME does this really work?
            commonConfigurationNode = resMan.createResource(name, CommonConfigurationNode.class);
            // instead of activating all resource lists immediately, although we might not need them,
            // we take care to create and activate them upon their first usage; see method #activate below
//            commonConfigurationNode.thresholds().create();
//            commonConfigurationNode.programEnforcers().create();
//            commonConfigurationNode.scheduleSums().create();
//            commonConfigurationNode.sums().create();
            commonConfigurationNode.activate(true);
        } else if (existingNodes.size() == 1) {
            commonConfigurationNode = existingNodes.get(0);
        } else {
            logger.warn("Found multiple top-level instances of CommonConfigurationNode.class on startup. This should not happen. Will continue using the first one.");
            commonConfigurationNode = existingNodes.get(0);
        }

        resAcc.addResourceDemand(ResourceManipulatorModel.class, this);
    }

    @Override
    @Deprecated
    public void start(ApplicationManager applicationManager) {
        start();
    }
    
    @Override
    public void stop() {
        resAcc.removeResourceDemand(ResourceManipulatorModel.class, this);
//        Iterator<Controller> it = controllerMap.values().iterator();
//        while (it.hasNext()) {
//        	Controller ct = it.next();
//        	ct.stop();
//        }
        try {
			Thread.sleep(2000);				// there may be multiple callbacks pending from controller.delete(), 
											// which lead to unexpected behaviour if we immediately clear the controllers map
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        controllerMap.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ManipulatorConfiguration> T createConfiguration(Class<T> type) {
        if (type == Threshold.class) {
            return (T) new ThresholdConfigurationImpl(this);
        }
        if (type == ProgramEnforcer.class) {
            return (T) new ProgramEnforcerImpl(this);
        }
        if (type == ScheduleSum.class) {
            return (T) new ScheduleSumImpl(this);
        }
        if(type == Sum.class) {
        	return (T) new SumImpl(this);
        }
        if (type == ScheduleManagement.class) {
        	return (T) new ScheduleManagementImpl(this,appMan);
        }
        throw new UnsupportedOperationException("Cannot create an instance for unknown or un-implemented configuration type " + type.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ManipulatorConfiguration> List<T> getConfigurations(Class<T> type) {
    	if (commonConfigurationNode == null) {
    		final String name = resMan.getUniqueResourceName(targetName);
    		commonConfigurationNode = resAcc.getResource(name);
    		if (commonConfigurationNode == null)
    			return Collections.emptyList();
    	}
        
        // special case: Someone asked for ALL configurations
        if (type == ManipulatorConfiguration.class) {
            List<T> result = new ArrayList<>();
            for (Threshold config : getConfigurations(Threshold.class)) {
                result.add( (T) config);
            }
            for (ProgramEnforcer config : getConfigurations(ProgramEnforcer.class)) {
                result.add( (T) config);
            }
            for (ScheduleSum config : getConfigurations(ScheduleSum.class)) {
                result.add( (T) config);
            }
            for (Sum config : getConfigurations(Sum.class)) {
                result.add( (T) config);
            }
            for (ScheduleManagement config : getConfigurations(ScheduleManagement.class)) {
            	result.add((T) config); 
            }
            return result;
        }
        
        if (type == Threshold.class) {
            final List<ThresholdModel> configurations = commonConfigurationNode.thresholds()
            		.<ResourceList<ThresholdModel>> create().getAllElements();
            final List<T> result = new ArrayList<>(configurations.size());
            for (ThresholdModel configuration : configurations) {
                final String appTag = configuration.application().getValue();
                if (!appId.equals(appTag)) continue; // only care about your own configurations.
                if (!configuration.isActive()) {
                    logger.warn("Encountered inactive configuration at "+configuration.getLocation()+" while parsing the list of configurations. This should not happen (too often), since the ResourceManipulators tool assumes only active configurations. Ignoring the configuration and continuing.");                    
                    continue;
                }
                result.add((T) new ThresholdConfigurationImpl(this, configuration));
            }
            return result;
        }
        
        if (type == ProgramEnforcer.class) {
            final List<ProgramEnforcerModel> configurations = commonConfigurationNode.programEnforcers()
            		.<ResourceList<ProgramEnforcerModel>> create().getAllElements();
            final List<T> result = new ArrayList<>(configurations.size());
            for (ProgramEnforcerModel configuration : configurations) {
                final String appTag = configuration.application().getValue();
                if (!appId.equals(appTag)) continue; // only care about your own configurations.             
                if (!configuration.isActive()) {
                    logger.warn("Encountered inactive configuration at "+configuration.getLocation()+" while parsing the list of configurations. This should not happen (too ofteb), since the ResourceManipulators tool assumes only active configurations. Ignoring the configuration and continuing.");                    
                    continue;
                }
                result.add((T) new ProgramEnforcerImpl(this, configuration));
            }
            return result;
        }

        if (type == ScheduleSum.class) {
            final List<ScheduleSumModel> configurations = commonConfigurationNode.scheduleSums()
            		.<ResourceList<ScheduleSumModel>> create().getAllElements();
            final List<T> result = new ArrayList<>(configurations.size());
            for (ScheduleSumModel configuration : configurations) {
                final String appTag = configuration.application().getValue();
                if (!appId.equals(appTag)) continue; // only care about your own configurations.             
                if (!configuration.isActive()) {
                    logger.warn("Encountered inactive configuration at "+configuration.getLocation()+" while parsing the list of configurations. This should not happen (too ofteb), since the ResourceManipulators tool assumes only active configurations. Ignoring the configuration and continuing.");                    
                    continue;
                }
                result.add((T) new ScheduleSumImpl(this, configuration));
            }
            return result;
        }
        
        if (type == Sum.class) {
            final List<SumModel> configurations = commonConfigurationNode.sums()
            		.<ResourceList<SumModel>> create().getAllElements();
            final List<T> result = new ArrayList<>(configurations.size());
            for (SumModel configuration : configurations) {
                final String appTag = configuration.application().getValue();
                if (!appId.equals(appTag)) continue; // only care about your own configurations.             
                if (!configuration.isActive()) {
                    logger.warn("Encountered inactive configuration at "+configuration.getLocation()+" while parsing the list of configurations. This should not happen (too ofteb), since the ResourceManipulators tool assumes only active configurations. Ignoring the configuration and continuing.");                    
                    continue;
                }
                result.add((T) new SumImpl(this, configuration));
            }
            return result;
        }
        if (type == ScheduleManagement.class) {
            final List<ScheduleManagementModel> configurations = commonConfigurationNode.scheduleManagements()
            			.<ResourceList<ScheduleManagementModel>> create().getAllElements();
            final List<T> result = new ArrayList<>(configurations.size());
            for (ScheduleManagementModel configuration : configurations) {
                final String appTag = configuration.application().getValue();
                if (!appId.equals(appTag)) continue; // only care about your own configurations.             
                if (!configuration.isActive()) {
                    logger.warn("Encountered inactive configuration at "+ configuration.getLocation()+" while parsing the list of configurations. This should not happen (too ofteb), since the ResourceManipulators tool assumes only active configurations. Ignoring the configuration and continuing.");                    
                    continue;
                }
                result.add((T) new ScheduleManagementImpl(this, configuration,appMan));
            }
            return result;
        }
        
        throw new UnsupportedOperationException("Cannot create a configuration instance for unknown or un-implemented configiuration type " + type.getName());
    }

    /**
     * Creates an empty configuration of the given type and sets the APP-ID
     *
     * @param <T>
     * @param type Resource type of the new resource to create.
     * @return Newly-created resource instance of the given type. The field {@link ResourceManipulatorModel#application()
     * } is already created and set correctly, but no other sub-resources are
     * created.
     *
     */
    @SuppressWarnings("unchecked")
    public <T extends ResourceManipulatorModel> T createResource(Class<T> type) {
        final ResourceManipulatorModel result;
        if (type == ThresholdModel.class) {
        	activate(commonConfigurationNode.thresholds());
            result = commonConfigurationNode.thresholds().add();
        } else if (type == ProgramEnforcerModel.class) {
        	activate(commonConfigurationNode.programEnforcers());
            result = commonConfigurationNode.programEnforcers().add();
        } else if (type == ScheduleSumModel.class) {
        	activate(commonConfigurationNode.scheduleSums());
            result = commonConfigurationNode.scheduleSums().add();
        } else if(type == SumModel.class) {
        	activate(commonConfigurationNode.sums());
        	result = commonConfigurationNode.sums().add();
        } else if (type == ScheduleManagementModel.class) {
        	activate(commonConfigurationNode.scheduleManagements());
        	result = commonConfigurationNode.scheduleManagements().add();
        } else {
            throw new UnsupportedOperationException("Cannot create a resource instance for unknown or un-implemented configuration type " + type.getName());
        }
        result.application().create();
        result.application().requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
        result.application().setValue(appId);
        return (T) result;
    }
    
    private void activate(ResourceList<?> listResource) {
    	listResource.create().activate(false);
    }

    /**
     * Passes the application manager to configuration implementations which may
     * require it.
     */
    public ApplicationManager getApplicationManager() {
        return appMan;
    }

    private void newConfiguration(final ResourceManipulatorModel configuration, final boolean retry) {
    	final String id = configuration.application().getValue();
        if (!appId.equals(id)) {// belongs to a different application.
            logger.debug("Resource manipulator configuration resource at " + configuration.getLocation() + " found, but configuration belongs to a different application. Ignoring this.");
            return;
        }

        final Controller existingController = controllerMap.get(configuration);
        // originally, there should not have been an existing controller. With deactivation and reactivation possible,
        // a controller may already exist.
     /*   if (existingController != null) {
            logger.error("Caught a resource availabe callback for configuration at " + configuration.getLocation() + " but controller already exists. Will ignore the callback. Ignoring the callback. Expect that something went really wrong.");
            return;
        } */

        // Create a suitable controller instance.
        final Controller controller;
        try {
	        if (existingController != null) 
	        	controller = existingController;
	        else if (configuration instanceof ThresholdModel) {
	            controller = new ThresholdController(appMan, (ThresholdModel) configuration);
	        } else if (configuration instanceof ProgramEnforcerModel) {
	            controller = new ProgramEnforcerController(appMan, (ProgramEnforcerModel) configuration);
	        } else if (configuration instanceof ScheduleSumModel) {
	            controller = new ScheduleSumController(appMan, (ScheduleSumModel) configuration);
	        } else if (configuration instanceof SumModel) {
	        	controller = new SumController(appMan, (SumModel) configuration);
	        } else if (configuration instanceof ScheduleManagementModel) {
	        	if (scheduleManagement == null) // FIXME check if synchronization is needed; presumably not, since this is only called in one single application thread(?)
	        		scheduleManagement = new ScheduleManagementController(appMan);
	        	controller = new ScheduleConfiguration((ScheduleManagementModel) configuration, scheduleManagement, appMan);
	        } else {
	            logger.error("Got resource available callback for unknown or unsupported resource manipulator configuration at " 
	            		+ configuration.getLocation() + " which is of type " + configuration.getResourceType().getCanonicalName() + ". Ignoring the callback.");
	            return;
	        }
        } catch (IllegalStateException e) {
        	if (!retry) {
        		logger.error("Resource manipulator failed to start for configuration {}",configuration,e);
        		return;
        	}
        	appMan.createTimer(10000, new TimerListener() {
				
				@Override
				public void timerElapsed(Timer timer) {
					timer.destroy();
					newConfiguration(configuration, false);
				}
			});
        	return;
        }

        controller.start();
        controllerMap.put(configuration, controller);
        logger.debug("Started new enforcing a rule of type " + configuration.getResourceType().getCanonicalName() + " at " + configuration.getLocation());
    }
    
    @Override
    public void resourceAvailable(ResourceManipulatorModel configuration) {
        newConfiguration(configuration, true);
    }

    @Override
    public void resourceUnavailable(ResourceManipulatorModel configuration) {
    	
      /*  final String id = configuration.application().getValue();  // null
        if (!appId.equals(id)) {// belongs to a different application. 
            return;
        }*/
        final Controller existingController = controllerMap.remove(configuration);
        if (existingController != null) {
            existingController.stop();
            logger.debug("Stopped enforcing a manipulator rule of type " + configuration.getResourceType().getCanonicalName());
        } else {
//            logger.error("Caught a resource unavailabe callback for configurat ion at " + configuration.getLocation() + " but no controller was assigned to this. Ignoring the callback. Expect that something went really wrong.");
        }
    }

    @Override
    public void deleteAllConfigurations() {
        final List<ManipulatorConfiguration> allConfigurations = getConfigurations(ManipulatorConfiguration.class);
        for (ManipulatorConfiguration config : allConfigurations) config.remove();
        if (scheduleManagement != null)
        	scheduleManagement.close();
    }

}
