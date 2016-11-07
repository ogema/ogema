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
package org.ogema.apps.graphgenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ogema.apps.graphgenerator.generators.GraphGenerator;
import org.ogema.apps.graphgenerator.generators.GraphwizGenerator;
import org.ogema.apps.graphgenerator.generators.VisJsGenerator;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.tools.SerializationManager;
import org.ogema.model.sensors.TemperatureSensor;

/**
 * Servlet for the Graphwizz-Generator
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class GraphGenServlet extends HttpServlet {

	public static final boolean DEBUG = false;

	private static final long serialVersionUID = 1L;
	private final ApplicationManager am;
	private final ResourceAccess resAcc;
	private final SerializationManager serMan;
	private final OgemaLogger logger;

	public GraphGenServlet(ApplicationManager am) {
		this.am = am;
		this.resAcc = am.getResourceAccess();
		this.serMan = am.getSerializationManager();
		this.logger = am.getLogger();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		List<Resource> topLR=  resAcc.getToplevelResources(Resource.class);
//		Set<Class<? extends Resource>> types  = new LinkedHashSet<>();
		Set<String> types  = new LinkedHashSet<>();
		types.add(Resource.class.getName());
		for (Resource res: topLR) {
			types.add(res.getResourceType().getName());
		}
		JSONArray returnTypes = new JSONArray(types);
		resp.getWriter().write(returnTypes.toString());
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("application/json");
		resp.flushBuffer();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String selectedGenerator = req.getParameter("generator");
			String resourceType = req.getParameter("resourceType");
			String plotType = req.getParameter("plottype");
			String pce = req.getParameter("enable_phys_conf");
			Boolean physConfEnabled = Boolean.valueOf(pce != null ? pce : "false");

			if (DEBUG) {
				System.out.println("Selected generator: " + selectedGenerator + " plotType=" + plotType
						+ " enable physic config: " + physConfEnabled);
			}

			if (selectedGenerator == null || plotType == null) {
				resp.getWriter().write("Error: no generator or no plot type selected!");
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			final GraphGenerator generator;
			switch (selectedGenerator) {
			case "visjs":
				generator = new VisJsGenerator(logger, serMan);
				((VisJsGenerator) generator).setPhysConfigurationEnabled(physConfEnabled);
				break;
			case "graphviz":
				generator = new GraphwizGenerator(am);
				break;
			default:
				throw new IllegalArgumentException("Invalid generator selected");
			}

			Object result = null;
			Class<? extends Resource> type = getClassForName(resourceType);
			switch (plotType) {
			case "all": {
				result = generator.generateResourcesGraph(type, resAcc);
				break;
			}
			case "connections": {
				result = generator.generateConnectionsGraph(resAcc);
				break;
			}
			default: {
				resp.getWriter().write("plot type not known: " + plotType);
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			}

			if (result != null) {
				if (result instanceof File) {
					// graph file was written... let the user download that file:
					resp.setContentType("application/force-download");
					resp.setContentLength((int) ((File) result).length());
					resp.setHeader("Content-Transfer-Encoding", "binary");
					resp.addHeader("Content-Disposition", "attachment; filename=\"resources.graph\"");
					OutputStream out = resp.getOutputStream();
					FileInputStream in = new FileInputStream((File) result);
					byte[] buffer = new byte[4096];
					int length;
					while ((length = in.read(buffer)) > 0) {
						out.write(buffer, 0, length);
					}
					in.close();
					out.flush();
				}
				else {
					resp.getWriter().write(result.toString());
				}
				resp.setStatus(HttpServletResponse.SC_OK);
			}
			else {
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
    @SuppressWarnings("unchecked")
	private Class<? extends Resource> getClassForName(String resourceType) {
		Class<? extends Resource> type = Resource.class;
		try {
			type = (Class<? extends Resource>) Class.forName(resourceType);
		} catch(Exception e) {
			try {
				try {
					type = (Class<? extends Resource>) Class.forName(resourceType, false, TemperatureSensor.class.getClassLoader());  // FIXME does not work for custom types?
				} catch (Exception eee) {
					List<Resource> ress  = resAcc.getToplevelResources(Resource.class);
					boolean found = false;
					for (Resource top :ress) {
						Class<? extends Resource> type2 = top.getResourceType();
						if (type2.getName().equals(resourceType)) {
							type = type2;
							found = true;
							break;
						}
					}
					if(!found)
						logger.error("Could not retrieve class for " + resourceType,e);
				}
			} catch (Exception ee) {
			}
			
		}
		return type;
	}
}
