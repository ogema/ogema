/**
 * Copyright 2009 - 2014
 *
 * Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Fraunhofer IIS
 * Fraunhofer ISE
 * Fraunhofer IWES
 *
 * All Rights reserved
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ogema.webresourcemanager.impl.internal.layout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.ogema.core.webresourcemanager.ApplicationPage;
import org.ogema.core.webresourcemanager.ApplicationPanel;
import org.ogema.webresourcemanager.impl.internal.WebResourceManagerImpl;
import org.osgi.framework.Bundle;

/**
 *
 * @author skarge
 */
public class Util {

	public static String getJsID(String name) {
        final HashSet<String> set = new HashSet<>();
        set.add(" ");
        set.add("-");
        set.add("_");
        set.add("/");
        set.add("\\");
        set.add("^");
        set.add("°");
        set.add("!");
        set.add("");
        set.add("§");
        set.add("$");
        set.add("%");
        set.add("&");
        set.add("");
        set.add("");

        for (final String s : set) {
            name = name.replace(s, "");
        }

        name = name.toLowerCase();
        return name;
    }

	public static boolean isUserpermitted(final Bundle bundle) {
		// FIXME        
		//		PermissionManager p;
		//		try {
		//			p = MyWebResourceManager.getInstance().getPermissionManager();
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//			return false;
		//		}
		//        final String user = OgemaAuthentificatedWebsession.get().getUsername();
		// FIXME this is a hack. Re-imlement security when permission manager works again
		//        boolean result = true;
		// boolean result = p.getAccessManager().isAppPermitted(user, bundle);
		return true;
	}

	public static List<SubnaviagationEntryExternal> getExternals() {
        final List<SubnaviagationEntryExternal> list = new ArrayList<>();
        for (final Map.Entry<String, String> entry : WebResourceManagerImpl.servletMap.entrySet()) {
            list.add(new SubnaviagationEntryExternal(entry.getKey(), entry.getValue()));
        }

        return list;
    }

	public static SubnavigationEntry getExternalEntry(){
        List<ApplicationPanel> list = new ArrayList<>();
        for (final Map.Entry<String, String> entry : WebResourceManagerImpl.servletMap.entrySet()) {
            list.add(new IFramePanel(entry.getKey(), entry.getValue())); 
        }
        
        Bundle bundle = null;
        ResourceReference image = new PackageResourceReference(SubNavigation.class, "logo3.png");
        ApplicationPage page = null;
        SubnavigationEntry entry = new SubnavigationEntry(bundle, image, "Sites in OGEMA", page, list);
        return entry;
    }
}
