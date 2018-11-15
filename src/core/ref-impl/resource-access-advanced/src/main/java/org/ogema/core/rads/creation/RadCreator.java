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
package org.ogema.core.rads.creation;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.rads.tools.ContainerTool;
import org.ogema.core.rads.tools.ResourceFieldInfo;
import org.ogema.core.rads.tools.RadFactory;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.CreateMode;
import org.ogema.core.resourcemanager.pattern.ContextSensitivePattern;
import org.slf4j.Logger;

/**
 * Creates an object according to a RAD and returns an instance of the completed
 * RAD.
 *
 * @author Timo Fischer, Fraunhofer IWES
 * @param <T> type of demanded resource
 * @param <P> type of pattern
 */
public class RadCreator<T extends Resource, P extends ResourcePattern<T>> {

	final ApplicationManager m_appMan;
	final Logger m_logger;
	final ResourceManagement m_resMan;
	final RadFactory<T, P> m_factory;
	private final Object container;
	private final Class<P> type;
	P m_result;

	public RadCreator(ApplicationManager appMan, Logger logger, Class<P> type, PatternFactory<P> factory, Object container) {
		m_appMan = appMan;
		m_logger = logger;
		m_resMan = appMan.getResourceManagement();
		m_factory = new RadFactory<>(type, AccessPriority.PRIO_LOWEST, factory);
		this.container = container;
		this.type = type;
	}

	/**
	 * Creates the pattern by first creating a top-level resource with a given name which serves as the root resource and then subsequently adding the other fields in the pattern.
	 */
	public void create(String name) {
		final Class<T> model = m_factory.getDemandedModel();
		T seed;
		try {
			try {
				seed = m_resMan.createResource(name, model);
			} catch (ResourceAlreadyExistsException e) {
				seed = m_appMan.getResourceAccess().getResource(name);
			}
		} catch (Exception e) {
			m_logger.error("Error creating ResourceAccessDeclaration: Could not create demanded model with name "
					+ name + " and type " + model.getCanonicalName() + "\n\t Reason: " + e.getMessage());
			return;
		}
		createSubresources(seed);
		setContainer();
	}

	public void addDecorator(Resource parent, String name) {
		final Class<T> model = m_factory.getDemandedModel();
		T seed;
		try {
			try {
				seed = parent.addDecorator(name, model);
			} catch (ResourceAlreadyExistsException e) {
				seed = parent.getSubResource(name);
			}
		} catch (Exception e) {
			m_logger.error("Error creating ResourceAccessDeclaration: Could not create demanded model with name "
					+ name + " and type " + model.getCanonicalName() + "\n\t Reason: " + e.getMessage());
			return;
		}
		createSubresources(seed);
		setContainer();
	}

	/**
	 * Creates the pattern with a given resource object being the root resource.
	 */
	public void create(T seed) {
		if (!seed.exists())
			seed.create();
		createSubresources(seed);
		setContainer();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setContainer() {
		if (container != null && ContextSensitivePattern.class.isAssignableFrom(type) && m_result != null) {
			ContextSensitivePattern pattern = (ContextSensitivePattern) m_result;
//			pattern.setContainer(container);
			try {
				ContainerTool.setContainer(pattern, container);
			} catch (NoSuchFieldException | IllegalAccessException | RuntimeException e) {
				m_logger.error("Internal error: could not set pattern container: " + e);
			}
			pattern.init();
		}
	}

	private void createSubresources(T seed) {
		m_result = m_factory.create(seed);
		assert m_result != null;
		for (ResourceFieldInfo info : m_factory.getResourceFieldInfos()) {
			if (info.getCreateMode() == CreateMode.OPTIONAL) {
				m_logger.debug("RAD field " + info.getField().getName() + " is optional. Will not be created.");
				continue;
			}

			final Resource resource = RadFactory.getResource(info.getField(), m_result);
			if (resource == null) {
				if (m_logger.isDebugEnabled())
					m_logger.debug("Field with name " + info.getField().getName()
							+ " was not initialized by user: Ignoring it.");
				continue; // do not care about fields that were not initialized by user.
			}
			if (resource.exists()) {
				if (m_logger.isDebugEnabled())
					m_logger.debug("Resource at " + resource.getPath() + " already exists. Skipping it.");
				continue;
			}

			final boolean created;
			try {
				if (m_logger.isDebugEnabled())
					m_logger.debug("Creating resource at " + resource.getPath());
				created = resource.create().exists();
			} catch (Exception e) {
				throw new RuntimeException("Could not create resource " + resource + " at location "
						+ resource.getLocation(), e);
			}
			if (!created) {
				throw new RuntimeException("Error during creation of a RAD-object: Could not create subresource at "
						+ resource.getLocation());
			}
		}
	}

	public P getRad() {
		return m_result;
	}
}
