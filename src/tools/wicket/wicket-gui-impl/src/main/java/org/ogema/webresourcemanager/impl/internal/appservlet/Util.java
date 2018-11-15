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
package org.ogema.webresourcemanager.impl.internal.appservlet;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ogema.core.administration.AdminApplication;
import org.ogema.webresourcemanager.impl.internal.MyWebResourceManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author skarge
 */
public class Util {

    private static Util instance;
    private static List<SubnavigationAppEntry> servletApps;

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


    private List<SubnavigationAppEntry> initAppEntrys() {
        List<SubnavigationAppEntry> result = new ArrayList<>();
        if (MyWebResourceManager.getInstance() == null) {
            return result;
        }
        List<AppsJsonGet> list2 = new ArrayList<>();
        try {
            list2 = Util.getInstance().appsList2JSON();
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (AppsJsonGet app : list2) {
            if(app.isHasWebResources()){
                result.add(new SubnavigationAppEntry(app));
            }
        }

        return result;
    }

    public  List<AppsJsonGet> appsList2JSON() throws IOException {

        List<AdminApplication> apps = (ArrayList<AdminApplication>) MyWebResourceManager.getInstance().getAdministrationManager().getAllApps();

        List<AppsJsonGet> list = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();


        for (AdminApplication entry : apps) {
            String name = entry.getID().getBundle().getSymbolicName();
            String fileName = entry.getID().getBundle().getLocation();
            int lastSeperator = fileName.lastIndexOf("/");
            fileName = fileName.substring(lastSeperator + 1, fileName.length());

            boolean needFilter = false;
            for (String filter : AppStoreUtils.FILTERED_APPS) {
                if (name.contains(filter) && !name.contains("framework-gui")
                        && !name.contains("framework-administration")) {
                    needFilter = true;
                    break;
                }
            }
            if (needFilter) {
                continue;
            }

            long id = entry.getBundleRef().getBundleId();
            Map<String, String> metainfo = new HashMap<>();
            metainfo.put("File_Name", fileName);

            Bundle bundle = entry.getBundleRef();

            Dictionary<String, String> bundleDictionary = bundle.getHeaders();
            Enumeration<String> dictionaryEnums = bundleDictionary.keys();
            while (dictionaryEnums.hasMoreElements()) {
                String key = dictionaryEnums.nextElement();
                String element = bundleDictionary.get(key);

                if (!("Import-Package".equals(key) || "Export-Package".equals(key))) {
                    String formattedKey = key.replace('-', '_');
                    metainfo.put(formattedKey, element);
                }
            }

            AppsJsonGet singleApp = new AppsJsonGet();
            singleApp.setName(name);
            singleApp.setId(id);
            singleApp.setMetainfo(metainfo);

            StringBuffer jsonBuffer = Util.getInstance().webResourceTree2JSON((int) id, "#", null);
            String jsonString = jsonBuffer.toString();
            List<AppsJsonWebResource> webResourcesApp = new ArrayList<>();

            webResourcesApp = mapper.readValue(jsonString, mapper.getTypeFactory().constructCollectionType(
                    List.class, AppsJsonWebResource.class));

            if (webResourcesApp.isEmpty()) {
                singleApp.setHasWebResources(false);
            } else {
                singleApp.setHasWebResources(true);
                for (AppsJsonWebResource singleWebResource : webResourcesApp) {
                    String path = singleWebResource.getAlias();
                    String index = "/index.html";
                    singleApp.getWebResourcePaths().add(path + index);
                }
            }

            list.add(singleApp);
        }

        Collections.sort(list, new AppCompare());
        return list;
    }

    private StringBuffer webResourceTree2JSON(int id, String path, String alias) {
        // JSONObject permObj = new JSONObject();
        // JSONArray permsArray = new JSONArray();
        int index = 0;
        StringBuffer sb;
        sb = new StringBuffer();
        BundleContext bundleContext = MyWebResourceManager.getInstance().getBundleContext();
        if (path.equals("#")) {
//            PermissionManager permissionManager = MyWebResourceManager.getInstance().getPermissionManager();
//            AppID appid = MyWebResourceManager.getInstance().getAdministrationManager().getAppByBundle(bundleContext.getBundle(id));
            Map<String, String> entries = MyWebResourceManager.getInstance().getWebManager().getRegisteredResources();
            if (entries == null) {
                sb.append("[]");
                return sb;
            }
            Set<Map.Entry<String, String>> entrySet = entries.entrySet();
            sb.append('[');
            for (Map.Entry<String, String> e : entrySet) {
                String key = e.getKey();
                String name = e.getValue();
                if (index++ != 0) {
                    sb.append(',');
                }
                sb.append("{\"text\":\"").append(key).append('"').append(',');
                sb.append("\"id\":\"").append(name).append('"').append(',');
                sb.append("\"alias\":\"").append(key).append('"').append(',');
                sb.append("\"children\":true").append('}');
            }
            sb.append(']');
            return sb;
        }

        // path = "/";
        Bundle b = bundleContext.getBundle(id);
        Enumeration<URL> entries = b.findEntries(path, null, false);
//        String replace = path + "/";
        if (entries != null) {
            // permObj.put("webResources", permsArray);
            sb.append('[');
            while (entries.hasMoreElements()) {
                URL url = entries.nextElement();
                // String query ;//= url.getQuery();
                String file = url.getFile();
                boolean isDir = true;
                if (b.findEntries(file, null, false) == null) // if (query != null)
                {
                    isDir = false;
                }

                if (index++ != 0) {
                    sb.append(',');
                }
                sb.append("{\"text\":\"").append(file.replaceFirst(path, alias)).append('"').append(',');
                sb.append("\"id\":\"").append(file).append('"').append(',');
                sb.append("\"alias\":\"").append(file.replaceFirst(path, alias)).append('"');
                sb.append(',');
                if (isDir) {
                    sb.append("\"children\":true");
                } else {
                    sb.append("\"children\":false");
                }
                sb.append('}');
            }
            sb.append(']');
            return sb;
        } else {
            return null;
        }
    }

    public static Util getInstance() {
        if (Util.instance == null) {
            Util.instance = new Util();
        }
        return Util.instance;
    }

    public List<SubnavigationAppEntry> getServletApps() {
        if(Util.servletApps == null){
            Util.servletApps = initAppEntrys();
        }
        return Util.servletApps;
    }
    
    
}
