package org.ogema.webresourcemanager.impl.internal.dao;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WebresourceManagerDAO implements  Serializable{

    private static final String FILE_LOCATION = "config" + File.separator + "WebresourceManager.xml";
    private List<WebresourceManagerDO> webresourceManagerDO = new ArrayList<>();
    private static final long serialVersionUID = 3574779384959388617L;
    private static List<WebresourceManagerDO> list = new ArrayList<>();

    public WebresourceManagerDAO() {
    }

    public List<WebresourceManagerDO> readConfig() {

        if (WebresourceManagerDAO.list.isEmpty() == false) {
            return WebresourceManagerDAO.list;
        }
        File file = new File(FILE_LOCATION);

        try {
            WebresourceManagerDAO c = JAXB.unmarshal(file, WebresourceManagerDAO.class);
            WebresourceManagerDAO.list = c.getWebresourceManagerDO();        
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public List<WebresourceManagerDO> getWebresourceManagerDO() {
        return webresourceManagerDO;
    }

    public void setWebresourceManagerDO(List<WebresourceManagerDO> webresourceManagerDO) {
        this.webresourceManagerDO = webresourceManagerDO;
    }

//    public static void main(String[] args) {
//        WebresourceManagerDAO dao = new WebresourceManagerDAO();
//        dao.writeConfig();
//        List<WebresourceManagerDO> myList = dao.readConfig();
//
//        System.out.println("wm: " + myList.size());
//        for (WebresourceManagerDO wm : myList) {
//            System.out.println("wm: " + wm.getUsername());
//        }
//    }
//
//    private void writeConfig() {
//        File file = new File(FILE_LOCATION);
//        WebresourceManagerDAO d = new WebresourceManagerDAO();
//        d.getWebresourceManagerDO().add(new WebresourceManagerDO("bemi0001", "P53PwNb1", "ADMIN", "Zentralbemi-User"));
//        d.getWebresourceManagerDO().add(new WebresourceManagerDO("bemi0002", "P53PwNb2", "USER", "Zentralbemi-User"));
//        try {
//            JAXB.marshal(d, file);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println("writeConfig: " + file.getAbsolutePath());
//    }
}
