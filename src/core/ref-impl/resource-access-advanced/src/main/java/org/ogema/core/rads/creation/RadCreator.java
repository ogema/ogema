/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.core.rads.creation;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.rads.tools.ResourceFieldInfo;
import org.ogema.core.rads.tools.RadFactory;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.CreateMode;

/**
 * Creates an object according to a RAD and returns an instance of the completed
 * RAD.
 *
 * @author Timo Fischer, Fraunhofer IWES
 * @param <T> type of demanded resource
 * @param <P> type of pattern
 */
public class RadCreator<T extends Resource, P extends ResourcePattern<T>> {

	public static final boolean DEBUG = false;
	final ApplicationManager m_appMan;
	final OgemaLogger m_logger;
	final ResourceManagement m_resMan;
	final String m_name;
	final RadFactory<T, P> m_factory;
	P m_result;

	public RadCreator(ApplicationManager appMan, Class<P> type, String name) {
		m_appMan = appMan;
		m_logger = appMan.getLogger();
		m_resMan = appMan.getResourceManagement();
		m_name = name;
		m_factory = new RadFactory<>(type, AccessPriority.PRIO_LOWEST);
	}

	public void create() {
		final Class<T> model = m_factory.getDemandedModel();
		final T seed;
		try {
			seed = m_resMan.createResource(m_name, model);
		} catch (Exception e) {
			m_logger.error("Error creating ResourceAccessDeclaration: Could not create demanded model with name "
					+ m_name + " and type " + model.getCanonicalName() + "\n\t Reason: " + e.getMessage());
			return;
		}
		m_result = m_factory.create(seed);
		assert m_result != null;
		for (ResourceFieldInfo info : m_factory.getResourceFieldInfos()) {
			if (info.getCreateMode() == CreateMode.OPTIONAL) {
				m_logger.debug("RAD field " + info.getField().getName() + " is optional. Will not be created.");
				continue;
			}

			final Resource resource = RadFactory.getResource(info.getField(), m_result);
			if (resource == null) {
				if (DEBUG)
					m_logger.debug("Field with name " + info.getField().getName()
							+ " was not initialized by user: Ignoring it.");
				continue; // do not care about fields that were not initialized by user.
			}
			if (resource.exists()) {
				if (DEBUG)
					m_logger.debug("Resource at " + resource.getPath() + " already exists. Skipping it.");
				continue;
			}

			final boolean created;
			try {
				if (DEBUG)
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
