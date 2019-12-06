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
package org.ogema.pattern.debugger;

import java.io.IOException;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.administration.PatternCondition;
import org.ogema.core.administration.RegisteredPatternListener;
import org.ogema.core.application.AppID;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.*;

public class PatternDebuggerServlet extends HttpServlet {

	private static final long serialVersionUID = -623919478854332527L;
	private final ApplicationManager am;
	private final AdministrationManager admin;
	private final OgemaLogger logger;
	private final ResourceAccess ra;

	public PatternDebuggerServlet(ApplicationManager am) {
		this.admin = am.getAdministrationManager();
		this.am = am;
		this.ra = am.getResourceAccess();
		this.logger = am.getLogger();
	}

	// 
//	@Override
//	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//
//		StringBuilder sb = new StringBuilder();
//		BufferedReader reader = req.getReader();
//
//		try {
//			String line;
//			while ((line = reader.readLine()) != null) {
//				sb.append(line).append('\n');
//			}
//		} finally {
//			reader.close();
//		}
//		String request = sb.toString();
//		
//		resp.getWriter().write(request);
//		resp.setStatus(HttpServletResponse.SC_OK);
//
//	}

	// FIXME instead of bad request, return an empty list of results?
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String target = req.getParameter("target");
		if (target == null) {
			setBadRequest(resp);
			return;
		}
		switch (target) {
		case "listeners":
			AdminApplication adminApp = getApp(req);
			if (adminApp ==null) {
				setBadRequest(resp);
				return;
			}
			List<RegisteredPatternListener> listeners = adminApp.getPatternListeners();
			final Set<String> listenersResult = new HashSet<>(listeners.size());
			for (RegisteredPatternListener lst: listeners) {
				String patternType = lst.getDemandedPatternType().getName();
				listenersResult.add(patternType);
			}
			tryLoadInternalPatternClasses(adminApp, listenersResult);
			writeResultToResponse(resp, new JSONArray(listenersResult).toString());
			return;
		case "patterns":
			RegisteredPatternListener targetListener = getPatternListener(req);
			List<ResourcePattern<?>> completePatterns;
			List<ResourcePattern<?>> incompletePatterns;
			String demandedModel = "null";
			if (targetListener == null) {
				final Class<? extends ResourcePattern<?>> cl = tryLoadInternalPatternClass(getApp(req), req.getParameter("listener"));
				if (cl == null) {
					setBadRequest(resp);
					return;
				}
				completePatterns = new ArrayList<>();
				incompletePatterns = new ArrayList<>();
				demandedModel = getGenericClassParameter(cl, ResourcePattern.class).getSimpleName();
				try {
					final ResourcePatternAccess rpa = getApp(req).getAppManager().getResourcePatternAccess();
					final List<? extends ResourcePattern<?>> patterns = rpa.getPatterns(cl, AccessPriority.PRIO_LOWEST);
					for (ResourcePattern<?> p : patterns) {
						boolean satisfied = false;
						try {
							satisfied = rpa.isSatisfied((ResourcePattern) p, (Class) cl);
						} catch (Exception e) {}
						if (satisfied)
							completePatterns.add(p);
						else
							incompletePatterns.add(p);
					}
				} catch (Exception e) {}
			} else {
				completePatterns = (List<ResourcePattern<?>>) targetListener.getCompletedPatterns();			
				incompletePatterns = (List<ResourcePattern<?>>) targetListener.getIncompletePatterns();
				demandedModel = targetListener.getPatternDemandedModelType().getSimpleName();
				
			}
			JSONObject result = new JSONObject();
			JSONArray arr = new JSONArray();
			for (ResourcePattern<?> pattern: completePatterns) {
				JSONObject obj = new JSONObject();
				obj.put("path", pattern.model.getPath());
				obj.put("type", pattern.model.getResourceType().getSimpleName());
				obj.put("satisfied", true);
				arr.put(obj);
			}
			for (ResourcePattern<?> pattern: incompletePatterns) {
				JSONObject obj = new JSONObject();
				obj.put("path", pattern.model.getPath());
				obj.put("type", pattern.model.getResourceType().getSimpleName());
				obj.put("satisfied", false);
				arr.put(obj);
			}
			result.put("patterns", arr);
			result.put("demandedModel", demandedModel);
			writeResultToResponse(resp, result.toString());
			return;
		case "patternInfo":
			targetListener = getPatternListener(req);
			String pattern = req.getParameter("pattern");
			if (pattern == null) {
				setBadRequest(resp);
				return;
			}
			ResourcePattern<?> selectedPattern = null;
			List<PatternCondition> conditions = null;
			Class<? extends ResourcePattern<?>> modelType = null;
			if (targetListener == null) {
				final Class<? extends ResourcePattern<?>> cl = tryLoadInternalPatternClass(getApp(req), req.getParameter("listener"));
				if (cl != null) {
					try {
						final Resource r = getApp(req).getAppManager().getResourceAccess().getResource(pattern);
						if (r != null)
							selectedPattern = cl.getConstructor(Resource.class).newInstance(r);
					} catch (Exception e) {
					}
				}
				modelType = cl;
				conditions = Condition.getResourceInfoRecursively(cl, selectedPattern);
				
			} else {
				incompletePatterns =(List<ResourcePattern<?>>) targetListener.getIncompletePatterns();
				for (ResourcePattern<?> pt: incompletePatterns) {
					if (pt.model.getPath().equals(pattern)) {
						selectedPattern = pt;
						break;
					}
				}
				if (selectedPattern == null) {
					incompletePatterns =(List<ResourcePattern<?>>) targetListener.getCompletedPatterns();
					for (ResourcePattern<?> pt: incompletePatterns) {
						if (pt.model.getPath().equals(pattern)) {
							selectedPattern = pt;
							break;
						}
					}
				}
				if (selectedPattern != null) {
					modelType =	targetListener.getDemandedPatternType();
					conditions = targetListener.getConditions(selectedPattern);
				}
			}
			if (selectedPattern == null) {
				setBadRequest(resp);
				return;
			}
//			List<? extends Resource> missingConditions = targetListener.getMissingConditions(selectedPattern);
			result = new JSONObject();
			JSONObject patterns = new JSONObject();
			for (PatternCondition res: conditions) {
			
//			for (Resource res: missingConditions) {
				JSONObject obj = new JSONObject();
				obj.put("satisfied", res.isOptional() || res.isSatisfied());
				String am = "n.a.";
				String value = "n.a.";
				Object reference = "n.a.";
				if (res.exists()) {
					am = res.getAccessMode().name();
					if (SingleValueResource.class.isAssignableFrom(res.getResourceType())) 
//						value = (ValueResourceUtils.getValue((SingleValueResource) res)).replace("°", "&#176;");
						value = StringEscapeUtils.escapeHtml4(res.getValue().toString());
					if (res.isReference())	
						reference = res.getLocation();
					else 
						reference = false;
				}
				obj.put("exists", res.exists());
				obj.put("active", res.isActive());
				obj.put("accessMode", am);
				obj.put("value", value);
				obj.put("reference", reference);
				obj.put("optional", res.isOptional());
				obj.put("type", res.getResourceType().getSimpleName());
				obj.put("fieldName", res.getFieldName());
				patterns.put(res.getPath(), obj); //TODO display field name
			}

			result.put("patterns", patterns);
			result.put("demandedModel", pattern);
			result.put("type", modelType.getName());
			writeResultToResponse(resp, result.toString());
			return;
		default:
			setBadRequest(resp);
		}
	}
	
	// by side effect
	private static void tryLoadInternalPatternClasses(final AdminApplication app, final Set<String> knownPatterns) {
		try {
			final BundleWiring wiring = app.getBundleRef().adapt(BundleWiring.class);
			final ClassLoader loader = wiring.getClassLoader();
			final Collection<String> resources = wiring.listResources("/", "*", BundleWiring.LISTRESOURCES_LOCAL | BundleWiring.LISTRESOURCES_RECURSE);
			for (String r : resources) {
				if (!r.contains(".class"))
					continue;
				final int classDollar = r.indexOf('$');
				final String name = r.substring(0, classDollar > 0 ? classDollar : r.length()-6).replace('/', '.');
				try {
					if (!ResourcePattern.class.isAssignableFrom(loader.loadClass(name)))
						continue;
				} catch (ClassNotFoundException e) {
					continue;
				}
				knownPatterns.add(name);
			}
		} catch (Exception e) {}
	}
	
	private static Class<? extends ResourcePattern<?>> tryLoadInternalPatternClass(final AdminApplication app, final String clazz) {
		try {
			final BundleWiring wiring = app.getBundleRef().adapt(BundleWiring.class);
			final ClassLoader loader = wiring.getClassLoader();
			return (Class<? extends ResourcePattern<?>>) loader.loadClass(clazz);
		} catch (Exception e) {
			return null;
		}
	}
	
	// use this only with JSON strings!
	private static void writeResultToResponse(HttpServletResponse resp,String result) throws IOException {
		resp.getWriter().write(result);
		resp.setStatus(HttpServletResponse.SC_OK);
//		resp.setContentType("application/json"); // FIXME does not accept arrays?
		resp.flushBuffer();
	}
	
	private void setBadRequest(HttpServletResponse resp) throws IOException {
		resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		resp.flushBuffer();
	}
	
	private RegisteredPatternListener getPatternListener(HttpServletRequest req) {
		AdminApplication adminApp = getApp(req);
		if (adminApp == null)
			return null;
		String listener = req.getParameter("listener");
		if (listener ==null) 
			return null;
		List<RegisteredPatternListener> listeners = adminApp.getPatternListeners();
		RegisteredPatternListener targetListener = null;
		for (RegisteredPatternListener lst : listeners) {
			if (lst.getDemandedPatternType().getName().equals(listener)) {
				targetListener = lst;
				break;
			}
		}
		return targetListener;
	}
	
	private AdminApplication getApp(HttpServletRequest req) {
		long id = -1;
		String app = req.getParameter("app"); // must specify the bundle id
		try {
			id = Long.parseLong(app);
		} catch (Exception e) {}
		if (id < 0) 
			return null;
		BundleContext ctx = FrameworkUtil.getBundle(getClass()).getBundleContext();
		Bundle bdl = ctx.getBundle(id);
		if (bdl ==null)
			return null;
		AppID appID = admin.getAppByBundle(bdl);
		return admin.getAppById(appID.getIDString());
	}
	
	// https://stackoverflow.com/questions/18707582/get-actual-type-of-generic-type-argument-on-abstract-superclass
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final Class<?> getGenericClassParameter(final Class<?> parameterizedSubClass, final Class<?> genericSuperClass) {
	    // a mapping from type variables to actual values (classes)
	    final Map<TypeVariable<?>, Class<?>> mapping = new HashMap<>();

	    Class<?> klass = parameterizedSubClass;
	    while (klass != null) {
	        final Type type = klass.getGenericSuperclass();
	        if (type instanceof ParameterizedType) {
	            final ParameterizedType parType = (ParameterizedType) type;
	            final Type rawType = parType.getRawType();
	            if (rawType == genericSuperClass) {
	                // found
	                final Type t = parType.getActualTypeArguments()[0];
	                if (t instanceof Class<?>) {
	                    return (Class<?>) t;
	                } else {
	                    return mapping.get((TypeVariable<?>)t);
	                }
	            }
	            // resolve
	            final Type[] vars = ((GenericDeclaration)(parType.getRawType())).getTypeParameters();
	            final Type[] args = parType.getActualTypeArguments();
	            for (int i = 0; i < vars.length; i++) {
	                if (args[i] instanceof Class<?>) {
	                    mapping.put((TypeVariable)vars[i], (Class<?>)args[i]);
	                } else {
	                    mapping.put((TypeVariable)vars[i], mapping.get((TypeVariable<?>)(args[i])));
	                }
	            }
	            klass = (Class<?>) rawType;
	        } else {
	            klass = klass.getSuperclass();
	        }
	    }
	    throw new IllegalArgumentException("no generic supertype for " + parameterizedSubClass + " of type " + genericSuperClass);
	}
}
