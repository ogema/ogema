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
package org.ogema.apps.graphgenerator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.apps.graphgenerator.generators.GraphGenerator;
import org.ogema.apps.graphgenerator.styles.EdgeStyle;
import org.ogema.apps.graphgenerator.styles.EdgeStyleBase;
import org.ogema.apps.graphgenerator.styles.NodeStyle;
import org.ogema.apps.graphgenerator.styles.NodeStyleBlue;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.SimpleResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.model.connections.ThermalConnection;
import org.ogema.model.prototypes.Connection;
import org.ogema.model.prototypes.PhysicalElement;

/**
 * Application that writes out the current state of the resource graph on
 * request.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
public class GeneratorApplication implements Application {

	private OgemaLogger logger;
	private ApplicationManager appMan;
	private ResourceAccess resAcc;
	private GraphwizzServlet servlet;

	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
		this.logger = appManager.getLogger();
		this.resAcc = appManager.getResourceAccess();
		this.servlet = new GraphwizzServlet(this);
		logger.debug("{} started", getClass().getName());

		appManager.getWebAccessManager().registerWebResource("/ogema/graphwizzgenerator",
				"org/ogema/app/graphwizzgenerator/gui");
		appManager.getWebAccessManager().registerWebResource("/apps/ogema/graphwizzgenerator", servlet);
	}

	@Override
	public void stop(AppStopReason reason) {
		appMan.getWebAccessManager().unregisterWebResource("/ogema/graphwizzgenerator");
		appMan.getWebAccessManager().unregisterWebResource("/apps/ogema/graphwizzgenerator");

		logger.debug("{} stopped", getClass().getName());
	}

	/**
	 * Writes out the current state of the resource graph as a Graphwizz file.
	 * TODO rewrite to use a general GraphGenerator as input (requires
	 * GraphGenerator to contain a feature to write groups).
	 */
	public synchronized void writeGraph(GraphGenerator graph) {
        logger.debug("Writing the graph.");
        final EdgeStyle defaultEdgeStyle = new EdgeStyleBase();
        final NodeStyle defaultNodeStyle = new NodeStyleBlue();
        final NodeStyle simpleNodeStyle = new NodeStyleBlue();
        simpleNodeStyle.setShape("rectangle");

        graph.init();

        // Write the groups
        logger.debug("Writing the groups and the resources");
        final List<Resource> topLevelResources = resAcc.getToplevelResources(Resource.class);
        for (Resource root : topLevelResources) {
            final List<Resource> groupElements = root.getDirectSubResources(true);
            groupElements.add(root);
            if (groupElements.size() > 2) {
                graph.addGroup(groupElements, root.getLocation("_"));
            }
        }
//        graph.writeDeviceGroups(resAcc.getToplevelResources(Resource.class), "");

        // write all resources, including their style
        final List<Resource> resources = resAcc.getResources(Resource.class);
        for (Resource resource : resources) {
            if (resource instanceof SimpleResource) {
                graph.addNode(resource, simpleNodeStyle);
            } else {
                graph.addNode(resource, defaultNodeStyle);
            }
        }

        // write the edges
        logger.debug("Writing edges.");
        for (Resource resource : resources) {
            final List<Resource> subresources = resource.getSubResources(false);
            for (Resource subres : subresources) {
                graph.addEdge(resource, subres, defaultEdgeStyle);
            }
        }
        graph.finish();

        PrintWriter writer;
        try {
            writer = new PrintWriter("graph.gwizz", "UTF-8");
            writer.println(graph.getGraph());
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            logger.error("Could not write to file.");
        }
    }

	public synchronized void writeConnections(GraphGenerator graph) {
        graph.init();
        final NodeStyle defaultNodeStyle = new NodeStyleBlue();
        final NodeStyle thermalNodeStyle = new NodeStyleBlue();
        thermalNodeStyle.setActiveColor("red");
        final EdgeStyle defaultEdgeStyle = new EdgeStyleBase();

        final List<Connection> connections = resAcc.getResources(Connection.class);
        for (Connection connection : connections) {
            final NodeStyle nodeStyle = (connection instanceof ThermalConnection) ? thermalNodeStyle : defaultNodeStyle;
            graph.addNode(connection, nodeStyle);

            final PhysicalElement in = resAcc.getResource(connection.input().getLocation());
            if (in.exists()) {
                graph.addNode(in, defaultNodeStyle);
                graph.addEdge(connection, in, defaultEdgeStyle);
            }

            final PhysicalElement out = resAcc.getResource(connection.output().getLocation());
            if (out.exists()) {
                graph.addNode(out, defaultNodeStyle);
                graph.addEdge(connection, out, defaultEdgeStyle);
            }

            final Resource directParent = connection.getParent();
            final List<Resource> parents = connection.getReferencingResources(Resource.class);
            if (directParent != null) parents.add(directParent);
            for (Resource parent : parents) {
                graph.addNode(parent, defaultNodeStyle);
                graph.addEdge(parent, connection, defaultEdgeStyle);
            }
        }

        PrintWriter writer;
        try {
            writer = new PrintWriter("graph.gwizz", "UTF-8");
            writer.println(graph);
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            logger.error("Could not write to file.");
        }
    }
}
