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
package org.ogema.app.resourcecombiner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.NoSuchResourceException;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.core.resourcemanager.ResourceGraphException;
import org.ogema.core.resourcemanager.ResourceManagement;

/**
 * A combine request. An instance of this class is a request to map a resource into a particular subresource of another
 * resource.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class CombineRequest {

	private final String m_sourcename, m_fullTargetname, m_targetstart, m_subname;
	private final boolean m_create;
	private final List<String> m_paths = new ArrayList<>();
	private Resource m_source, m_target, m_subres;
	OgemaLogger m_logger;
	ApplicationManager m_appMan;
	ResourceManagement m_resMan;
	ResourceAccess m_resAcc;

	public CombineRequest(ApplicationManager appManager, String sourcename, String targetname, boolean createPaths) {
		Objects.requireNonNull(appManager, "ApplicationManager must not be null.");
		Objects.requireNonNull(sourcename, "Name for source resource may not be null");
		Objects.requireNonNull(targetname, "Name for target resource may not be null");

		m_appMan = appManager;
		m_logger = appManager.getLogger();
		m_resAcc = appManager.getResourceAccess();
		m_resMan = appManager.getResourceManagement();

		m_sourcename = sourcename;
		m_fullTargetname = targetname;
		String[] paths = targetname.split("/");
		m_targetstart = paths[0];
		for (int i = 1; i < paths.length - 1; ++i) {
			m_paths.add(paths[i]);
		}
		m_subname = paths[paths.length - 1];
		m_create = createPaths;
	}

	/**
	 * Tries to connect the requested resources.
	 * 
	 * @return true if the connection was successful, false if not. XXX should probably return a suitable enum to give
	 *         advanced information about what went wrong.
	 */
	public boolean connect() {

		// 1) find source
		m_source = m_resAcc.getResource(m_sourcename);
		if (m_source == null || !m_source.isActive()) {
			m_logger.debug("Cannot map " + m_sourcename + " into " + m_fullTargetname
					+ ": Source not found or isn't active.");
			return false;
		}

		// 2) Find parent of target.
		Resource resource = m_resAcc.getResource(m_targetstart);
		if (resource == null) {
			m_logger.debug("Cannot find start " + m_targetstart
					+ " of target resource. Will not connect resources. Continue.");
			return false;
		}
		for (String subres : m_paths) {
			Resource subresource = resource.getSubResource(subres);
			if (subresource == null || !subresource.exists()) {
				if (m_create) {
					resource.addOptionalElement(subres);
					subresource = resource.getSubResource(subres);
					if (subresource == null) {
						m_logger.warn("Cannot connect source " + m_sourcename + " to " + m_fullTargetname
								+ ": subresource " + subres
								+ " does not exist in path and cannot be created. Retry later");
						return false;
					}
				}
				else {
					m_logger.warn("Cannot connect source " + m_sourcename + " to " + m_fullTargetname
							+ ": subresource " + subres + " does not exist in path. Retry later");
					return false;
				}
			}
			resource = subresource;
		}

		Resource target = resource.getSubResource(m_subname);
		if (target != null && target.exists()) {
			m_logger.info("Cannot create a reference to " + m_sourcename + " in " + m_fullTargetname
					+ ": Field already exists. Discarding the referencing process for this connection.");
			return true;
		}

		// 3) Set the reference (first try as optional element, then as decorator)
		try {
			resource.setOptionalElement(m_subname, m_source);
		} catch (NoSuchResourceException e) {
			resource.addDecorator(m_subname, m_source);
		} catch (ResourceException e) {
			m_logger.error("Caught ResourceException trying to create the reference: The type of the referenced element is not assignable to the optional element's type.", e);
		} catch (ResourceGraphException e) {
			m_logger.error("Caught ResourceTreeException trying to create the reference.", e);
		}

                final Resource subres = resource.getSubResource(m_subname);
		if (subres == null || !subres.exists()) {
			m_logger.error("Could not set a reference from " + resource.getPath("/") + " to " + m_source.getPath("/")
					+ ". Reason unknonwn. Continue and hope for the best.");
			return false;
		}
		m_logger.info("Added reference to " + m_source.getPath("/") + " as subresource " + m_subname + " to resource "
				+ resource.getPath("/"));
		return true;
	}
}
