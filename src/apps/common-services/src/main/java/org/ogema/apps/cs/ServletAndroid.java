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
package org.ogema.apps.cs;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.resourcemanager.ResourceAccess;

public class ServletAndroid extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private OutputStream bout;
	private ResourceAccess ra;

	public ServletAndroid(ResourceAccess access) {
		this.ra = access;
	}

	synchronized public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		String pi = request.getPathInfo();
		System.out.println(request.getPathInfo());
		System.out.println(request.getPathTranslated());
		System.out.println(request.getRequestURI());
		System.out.println(request.getRequestURL());
		response.setContentType("text/script");
		bout = response.getOutputStream();

		switch (pi) {
		case "/read":
			bout.write(getStateFeedback(request.getParameter("resource")).getBytes());
			break;
		case "/write":
			try {
				String states = putStateControl(request.getParameter("resource"), request.getParameter("body"));
				bout.write(states.getBytes());
			} catch (Throwable e) {
				e.printStackTrace();
			}
			break;
		}

	}

	synchronized public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		response.setContentType("text/script");
	}

	// **Android*****************************************************************************************************************************************************************************

	static final String FEEDBACK_TRUE = "{\"value\":true}";
	static final String FEEDBACK_FALSE = "{\"value\":false}";
	static final String FEEDBACK_NONE = "{}";

	static final String fbutton1 = "HM_ES_PMSw1_Pl_PowerMeter_274155/onOffSwitch/stateFeedback";
	static final String fbutton2 = "Light_00212effff003dc6_0b/onOffSwitch/stateFeedback";
	static final String fbutton3 = "Develco_Smart_Plug/onOffSwitch/stateFeedback";
	static final String fbutton4 = "ZWave_Switch_Box_2/onOffSwitch/stateFeedback";

	static final String cbutton1 = "HM_ES_PMSw1_Pl_PowerMeter_274155/onOffSwitch/stateControl";
	static final String cbutton2 = "Light_00212effff003dc6_0b/onOffSwitch/stateControl";
	static final String cbutton3 = "Develco_Smart_Plug/onOffSwitch/stateControl";
	static final String cbutton4 = "ZWave_Switch_Box_2/onOffSwitch/stateControl";

	BooleanResource boolButton1, boolButton2, boolButton3, boolButton4;
	BooleanResource boolControl1, boolControl2, boolControl3, boolControl4;

	private BooleanResource getButtonControlResource(String resource) {
		if (boolControl1 == null) {
			Resource res = ra.getResource(cbutton1);
			boolControl1 = (BooleanResource) res;
		}
		if (boolControl2 == null) {
			Resource res = ra.getResource(cbutton2);
			boolControl2 = (BooleanResource) res;
		}
		if (boolControl3 == null) {
			Resource res = ra.getResource(cbutton3);
			boolControl3 = (BooleanResource) res;
		}
		if (boolControl4 == null) {
			Resource res = ra.getResource(cbutton4);
			boolControl4 = (BooleanResource) res;
		}
		switch (resource) {
		case cbutton1:
			return boolControl1;
		case cbutton2:
			return boolControl2;
		case cbutton3:
			return boolControl3;
		case cbutton4:
			return boolControl4;
		default:
			return null;
		}
	}

	public String getStateFeedback(String resource) {
		resource = resource.replace('.', '/');
		System.out.println("GetFeedback for " + resource);
		Resource res = ra.getResource(resource);
		if (!(res instanceof BooleanResource))
			return FEEDBACK_NONE;
		boolean value = ((BooleanResource) res).getValue();
		if (value)
			return FEEDBACK_TRUE;
		else
			return FEEDBACK_FALSE;
	}

	public String putStateControl(String resource, String body) {
		resource = resource.replace('.', '/');
		System.out.print("SetControl for " + resource);
		System.out.println(" to " + body);
		BooleanResource boolres = getButtonControlResource(resource);
		switch (body) {
		case "true":
			boolres.setValue(true);
			break;
		case "false":
			boolres.setValue(false);
			break;
		default:
			break;
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		String response = getButtonStates();
		return response;
	}

	private String getButtonStates() {
		if (boolButton1 == null) {
			Resource res = ra.getResource(cbutton1);
			boolButton1 = (BooleanResource) res;
		}
		if (boolButton2 == null) {
			Resource res = ra.getResource(cbutton2);
			boolButton2 = (BooleanResource) res;
		}
		if (boolButton3 == null) {
			Resource res = ra.getResource(cbutton3);
			boolButton3 = (BooleanResource) res;
		}
		if (boolButton4 == null) {
			Resource res = ra.getResource(cbutton4);
			boolButton4 = (BooleanResource) res;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("{\"values\":[");
		if (boolControl1 != null && boolControl1.getValue())
			sb.append("true,");
		else
			sb.append("false,");
		if (boolControl2 != null && boolControl2.getValue())
			sb.append("true,");
		else
			sb.append("false,");
		if (boolControl3 != null && boolControl3.getValue())
			sb.append("true,");
		else
			sb.append("false,");
		if (boolControl4 != null && boolControl4.getValue())
			sb.append("true,");
		else
			sb.append("false,");
		sb.append("true]}");
		return sb.toString();
	}
}
