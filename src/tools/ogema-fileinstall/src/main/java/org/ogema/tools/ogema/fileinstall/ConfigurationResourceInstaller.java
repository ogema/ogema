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
package org.ogema.tools.ogema.fileinstall;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author jlapp
 */
public class ConfigurationResourceInstaller implements BundleActivator, Application {

	/**
	 * Framework or system property ({@value} ) for the timeout in seconds (default
	 * {@value #CONFIG_TIMEOUT_DEFAULT}) the bundle start method will wait for the
	 * start of the OGEMA application.
	 */
	public static final String CONFIG_TIMEOUT = "ogema.resourceinstall.timeout";
	private static final long CONFIG_TIMEOUT_DEFAULT = 0;
	/**
	 * Framework or system property ({@value} ) for the directory containing the resource
	 * configuration files (default= {@value #CONFIG_RESOURCEPATH_DEFAULT}).
	 */
	public static final String CONFIG_RESOURCEPATH = "ogema.resourceinstall.path";
	private static final String CONFIG_RESOURCEPATH_DEFAULT = "config/resources";

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final CountDownLatch startLatch = new CountDownLatch(1);
	private volatile ApplicationManager appman;
	private Path configurationResources = Paths.get(CONFIG_RESOURCEPATH_DEFAULT);
	private Thread initThread;

	@Override
	public void start(final BundleContext context) throws Exception {
		final ServiceRegistration<Application> sreg = context.registerService(Application.class, this, null);
        long timeout = CONFIG_TIMEOUT_DEFAULT;
        if (context.getProperty(CONFIG_TIMEOUT) != null) {
            try {
                timeout = Long.parseLong(context.getProperty(CONFIG_TIMEOUT));
                logger.debug("timout according to configuration property {}: {}s",
                        CONFIG_TIMEOUT, timeout);
            } catch (NumberFormatException nfe) {
                logger.warn("configuration property {} has invalid format: {}",
                        CONFIG_TIMEOUT, context.getProperty(CONFIG_TIMEOUT));
            }
        }
        if (context.getProperty(CONFIG_RESOURCEPATH) != null) {
            try {
                configurationResources = Paths.get(context.getProperty(CONFIG_RESOURCEPATH));
            } catch (SecurityException | FileSystemNotFoundException | IllegalArgumentException e) {
                logger.warn("unusable configuration value for {}: {}",
                        CONFIG_RESOURCEPATH, context.getProperty(CONFIG_RESOURCEPATH), e);
            }
        }
        if (timeout > 0) {
            logger.info("{} configured for synchronous startup and resource installation, timeout is {}s.",
                    ConfigurationResourceInstaller.class.getSimpleName(), timeout);
        }
		if (startLatch.await(timeout, TimeUnit.SECONDS)) {
			parseInitResources(sreg, context);
		} else {
            if (timeout > 0) {
                logger.warn("{} timed out, check your configuration and start levels.",
                    ConfigurationResourceInstaller.class.getSimpleName());
            }
			initThread = new Thread(new Runnable() {

				@Override
				public void run() {
					parseInitResources(sreg, context);
				}
			}, "ogema-fileinstall-initres");
			initThread.start();
		}

	}

	private void parseInitResources(final ServiceRegistration<Application> sreg, final BundleContext context) {
		long timeout = CONFIG_TIMEOUT_DEFAULT;
		ServiceReference<ConfigurationAdmin> caRef = context.getServiceReference(ConfigurationAdmin.class);
		if (caRef != null) {
			ConfigurationAdmin ca = context.getService(caRef);
			if (ca != null) {
				Configuration[] cfgs;
				try {
					cfgs = ca.listConfigurations("(service.pid=" + getClass().getCanonicalName() + ")");
				} catch (IOException | InvalidSyntaxException e) {
					throw new ComponentException(e);
				}
				if (cfgs != null && cfgs.length == 1) {
					Configuration cfg = cfgs[0];
					Object o = cfg.getProperties().get(CONFIG_RESOURCEPATH);
					if (o != null) {
						configurationResources = Paths.get(o.toString());
					}
					o = cfg.getProperties().get(CONFIG_TIMEOUT);
					if (o != null) {
						timeout = Long.parseLong(o.toString());
					}
				}
			}
		}
		try {
			if (!startLatch.await(timeout, TimeUnit.SECONDS)) {
				logger.error("Timeout, app failed to start; probably the OGEMA framework is not running.");
				return;
			}
		} catch (InterruptedException e1) {
			return;
		}

		// resolving to absolute path requires (java.util.PropertyPermission "user.dir"
		// "read")
		configurationResources = configurationResources.toAbsolutePath();
		if (!Files.exists(configurationResources)) {
			logger.warn("configured resource file path '{}' does not exist", configurationResources);
			return;
		}
		logger.debug("loading configuration resources from {}", configurationResources);
		try (final DirectoryStream<Path> dirStream = Files.newDirectoryStream(configurationResources)) {
			Iterator<Path> it = dirStream.iterator();
			OgemaFileInstall inst = new OgemaFileInstall(appman);
			List<File> files = new ArrayList<>();
			while (it.hasNext()) {
				Path p = it.next();
				if (inst.canHandle(p.toFile())) {
					files.add(p.toFile());
				}
			}
			Collections.sort(files);
			logger.info("installing {} resource configuration files", files.size());
			for (File f : files) {
				logger.debug("installing file {}", f);
				try {
					inst.install(f);
					logger.debug("installed file {}", f);
				} catch (Exception ex) {
					if (ex.getCause() != null) {
						logger.error("installing configuration resource from {} failed: {} / {}", f, ex.getMessage(),
								ex.getCause().getMessage());
					} else {
						logger.error("installing configuration resource from {} failed: {}", f, ex.getMessage());
					}
				}
			}
			logger.debug("installation complete, removing app");
		} catch (IOException e) {
			logger.error("Failed to parse resources", e);
		}
		sreg.unregister();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (initThread != null && initThread.isAlive()) {
			initThread.interrupt();
			initThread.join(500);
		}
	}

	@Override
	public void start(ApplicationManager am) {
		appman = am;
		startLatch.countDown();
	}

	@Override
	public void stop(AppStopReason asr) {

	}

}
