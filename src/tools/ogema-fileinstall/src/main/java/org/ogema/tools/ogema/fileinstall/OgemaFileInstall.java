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
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceAccess;

/**
 * Utility class for loading OGEMA resource files into the resource database.
 * Implements all methods of a Felix Fileinstall ArtifactInstaller without
 * actually implementing the interface to avoid the dependency,
 * {@link OgemaArtifactInstaller} is providing an ArtifactInstaller as DS
 * component.
 */
public class OgemaFileInstall {

	private static final String[] JSON_EXTENSIONS = {".ogj", ".json"};
	private static final String[] XML_EXTENSIONS = {".ogx", ".xml"};

	private static final Object INSTALLERSYNC = new Object();

	protected OgemaLogger logger;
	protected ApplicationManager appMan;
	protected ResourceManagement resMan;
	protected ResourceAccess resAcc;

	private ResourceList<StringResource> installationTimestamps = null;

	public OgemaFileInstall() {
	}

	OgemaFileInstall(ApplicationManager appman) {
		this.appMan = appman;
		this.logger = appman.getLogger();
		this.resMan = appman.getResourceManagement();
		this.resAcc = appman.getResourceAccess();
	}

	private synchronized long getInstallationTimestamp(File file) {
		if (installationTimestamps == null) {
			List<FileInstallTimestamps> l = resAcc
					.getToplevelResources(FileInstallTimestamps.class);
			if (l.isEmpty()) {
				return -1;
			}
			installationTimestamps = l.get(0).installedFiles();
		}
		for (StringResource filename : installationTimestamps.getAllElements()) {
			if (filename.getValue().equals(file.getPath())) {
				return filename.getSubResource("timestamp", TimeResource.class)
						.getValue();
			}
		}
		return -1;
	}

	private synchronized void storeInstallationTimestamp(File file,
			long timestamp) {
		if (installationTimestamps == null) {
			String resname = "InstalledResourceFiles";
			installationTimestamps = resMan
					.createResource(resname, FileInstallTimestamps.class)
					.installedFiles().create();
		}
		for (StringResource filename : installationTimestamps.getAllElements()) {
			if (filename.getValue().equals(file.getPath())) {
				filename.getSubResource("timestamp", TimeResource.class)
						.setValue(timestamp);
				return;
			}
		}
		StringResource newFile = installationTimestamps.add().create();
		newFile.setValue(file.getPath());
		newFile.getSubResource("timestamp", TimeResource.class)
				.<TimeResource> create().setValue(timestamp);
	}

	public void install(File file) throws Exception {
		synchronized (INSTALLERSYNC) {
			if (!(file.lastModified() > getInstallationTimestamp(file))) {
				logger.debug("file {} already installed, timestamp {}", file,
						getInstallationTimestamp(file));
				return;
			}
			logger.info("installing file {}", file);
			if (isJsonFile(file)) {
				installJson(file);
			} else if (isXmlFile(file)) {
				installXml(file);
			} else {
				logger.error("installer notified with unsupported file type: ",
						file);
				return;
			}
			storeInstallationTimestamp(file, file.lastModified());
		}
	}

	private void installXml(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
                InputStreamReader in = new InputStreamReader(fis, Charset.forName("UTF-8"))) {
            appMan.getSerializationManager().createFromXml(in);
        }
    }
	private void installJson(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
                InputStreamReader in = new InputStreamReader(fis, Charset.forName("UTF-8"))) {
            appMan.getSerializationManager().createFromJson(in);
        }
    }
	public void update(File file) throws Exception {
		logger.debug("updating {}", file);
		install(file);
	}

	public void uninstall(File file) throws Exception {
		// TODO(?)
	}

	public boolean canHandle(File file) {
		logger.trace("can handle {}?", file);
		return isJsonFile(file) || isXmlFile(file);
	}

	private static boolean isJsonFile(File file) {
		final String fileLower = file.getName().toLowerCase();
		for (String suffix : JSON_EXTENSIONS) {
			if (fileLower.endsWith(suffix))
				return true;
		}
		return false;
	}

	private static boolean isXmlFile(File file) {
		final String fileLower = file.getName().toLowerCase();
		for (String suffix : XML_EXTENSIONS) {
			if (fileLower.endsWith(suffix))
				return true;
		}
		return false;
	}

}
