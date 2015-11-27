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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.tools.resourcemanipulator.configurations.ManipulatorConfiguration;
import org.ogema.tools.resourcemanipulator.configurations.ProgramEnforcer;
import org.ogema.tools.resourcemanipulator.configurations.ScheduleSum;
import org.ogema.tools.resourcemanipulator.configurations.Sum;
import org.ogema.tools.resourcemanipulator.configurations.Threshold;
import org.ogema.tools.resourcemanipulator.implementation.ProgramEnforcerImpl;
import org.ogema.tools.resourcemanipulator.implementation.ScheduleSumImpl;
import org.ogema.tools.resourcemanipulator.implementation.SumImpl;
import org.ogema.tools.resourcemanipulator.implementation.ThresholdConfigurationImpl;
import org.ogema.tools.resourcemanipulator.implementation.controllers.Controller;
import org.ogema.tools.resourcemanipulator.implementation.controllers.ProgramEnforcerController;
import org.ogema.tools.resourcemanipulator.implementation.controllers.ScheduleSumController;
import org.ogema.tools.resourcemanipulator.implementation.controllers.SumController;
import org.ogema.tools.resourcemanipulator.implementation.controllers.ThresholdController;
import org.ogema.tools.resourcemanipulator.model.CommonConfigurationNode;
import org.ogema.tools.resourcemanipulator.model.ProgramEnforcerModel;
import org.ogema.tools.resourcemanipulator.model.ResourceManipulatorModel;
import org.ogema.tools.resourcemanipulator.model.ScheduleSumModel;
import org.ogema.tools.resourcemanipulator.model.SumModel;
import org.ogema.tools.resourcemanipulator.model.ThresholdModel;

/**
 * Implementation of the new approach for the {@link ResourceManipulator}, which
 * creates configuration objects for the applications to use.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class ResourceManipulatorImpl implements ResourceManipulator, ResourceDemandListener<ResourceManipulatorModel> {

    private final ApplicationManager appMan;
    private final ResourceManagement resMan;
    private final ResourceAccess resAcc;
    private final OgemaLogger logger;
    private final String appId;
    private CommonConfigurationNode commonConfigurationNode;

    private final Map<ResourceManipulatorModel, Controller> controllerMap = new HashMap<>();

    public ResourceManipulatorImpl(ApplicationManager applicationManager) {
        appMan = applicationManager;
        resMan = applicationManager.getResourceManagement();
        resAcc = applicationManager.getResourceAccess();
        logger = applicationManager.getLogger();
        appId = applicationManager.getAppID().getIDString();
    }


    @Override
    public void start() {

        // find the common root node.
        final List<CommonConfigurationNode> existingNodes = resAcc.getToplevelResources(CommonConfigurationNode.class);
        if (existingNodes.isEmpty()) {
            final String targetName = "RESOURCE_MANIPULATOR_CONFIGURATIONS";
            final String name = resMan.getUniqueResourceName(targetName);
            commonConfigurationNode = resMan.createResource(name, CommonConfigurationNode.class);
            commonConfigurationNode.thresholds().create();
            commonConfigurationNode.programEnforcers().create();
            commonConfigurationNode.scheduleSums().create();
            commonConfigurationNode.sums().create();
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
        throw new UnsupportedOperationException("Cannot create an instance for unknown or un-implemented configuration type " + type.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ManipulatorConfiguration> List<T> getConfigurations(Class<T> type) {
        
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
            for (ScheduleSum config : getConfigurations(ScheduleSum.class)) {
                result.add( (T) config);
            }
            return result;
        }
        
        if (type == Threshold.class) {
            final List<ThresholdModel> configurations = commonConfigurationNode.thresholds().getAllElements();
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
            final List<ProgramEnforcerModel> configurations = commonConfigurationNode.programEnforcers().getAllElements();
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
            final List<ScheduleSumModel> configurations = commonConfigurationNode.scheduleSums().getAllElements();
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
            final List<SumModel> configurations = commonConfigurationNode.sums().getAllElements();
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
        
        throw new UnsupportedOperationException("Cannot create a configuration instance for unknown or un-implemented configiuration type " + type.getName());
    }

    /**
     * Creates an empty configuration of the given type and sets the APP-ID
     *
     * @param <T>
     * @param type Resource type of the new resource to create.
     * @return Newly-created resource instance of the given type. The field {@link ResourceManipulatorConfiguration#application()
     * } is already created and set correctly, but no other sub-resources are
     * created.
     *
     */
    @SuppressWarnings("unchecked")
    public <T extends ResourceManipulatorModel> T createResource(Class<T> type) {
        final ResourceManipulatorModel result;
        if (type == ThresholdModel.class) {
            result = commonConfigurationNode.thresholds().add();
        } else if (type == ProgramEnforcerModel.class) {
            result = commonConfigurationNode.programEnforcers().add();
        } else if (type == ScheduleSumModel.class) {
            result = commonConfigurationNode.scheduleSums().add();
        } else if(type == SumModel.class) {
        	result = commonConfigurationNode.sums().add();
        } else {
            throw new UnsupportedOperationException("Cannot create a resource instance for unknown or un-implemented configiuration type " + type.getName());
        }
        result.application().create();
        result.application().setValue(appId);
        return (T) result;
    }

    /**
     * Passes the application manager to configuration implementations which may
     * require it.
     */
    public ApplicationManager getApplicationManager() {
        return appMan;
    }

    @Override
    public void resourceAvailable(ResourceManipulatorModel configuration) {
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
        } else {
            logger.error("Got resource available callback for unknown or unsupported resource manipulator configuration at " + configuration.getLocation() + " which is of type " + configuration.getResourceType().getCanonicalName() + ". Ignoring the callback.");
            return;
        }

        controller.start();
        controllerMap.put(configuration, controller);
        logger.debug("Started new enforcing a rule of type " + configuration.getResourceType().getCanonicalName() + " at " + configuration.getLocation());
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
//            logger.error("Caught a resource unavailabe callback for configuration at " + configuration.getLocation() + " but no controller was assigned to this. Ignoring the callback. Expect that something went really wrong.");
        }
    }

    @Override
    public void deleteAllConfigurations() {
        final List<ManipulatorConfiguration> allConfigurations = getConfigurations(ManipulatorConfiguration.class);
        for (ManipulatorConfiguration config : allConfigurations) config.remove();
    }

}
