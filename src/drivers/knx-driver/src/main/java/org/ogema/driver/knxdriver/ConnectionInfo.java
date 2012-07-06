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
package org.ogema.driver.knxdriver;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceListener;

public class ConnectionInfo {

	private long id;
	private String intface;
	private String knxRouter;
	private String groupAddress;
	private String phyaddress;
	private String name;
	private String type;
	private Resource ressource;
	private long timeStep;
	private ResourceDemandListener<Resource> resDemandListener;
	private ResourceListener resListener;
	private String value;
	private boolean statusListener;
	private long lastAccess;
	private boolean updateToSend;
	private boolean isSensor;
	private boolean isListener;
	String dptStr;

	public ConnectionInfo(String intface, String knxRouter, String groupAddress, String phyaddress, String name,
			String type, int interval) {

		this.intface = intface;
		this.knxRouter = knxRouter;
		this.groupAddress = groupAddress;
		this.phyaddress = phyaddress;
		this.name = name;
		this.type = type;
		this.timeStep = interval;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getIntface() {
		return intface;
	}

	public void setIntface(String intface) {
		this.intface = intface;
	}

	public String getKnxRouter() {
		return knxRouter;
	}

	public void setKnxRouter(String knxRouter) {
		this.knxRouter = knxRouter;
	}

	public String getGroupAddress() {
		return groupAddress;
	}

	public void setGroupAddress(String groupAddress) {
		this.groupAddress = groupAddress;
	}

	public String getPhyaddress() {
		return phyaddress;
	}

	public void setPhyaddress(String phyaddress) {
		this.phyaddress = phyaddress;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Resource getRessource() {
		return ressource;
	}

	public void setRessource(Resource ressource) {
		this.ressource = ressource;
	}

	public long getTimeStep() {
		return timeStep;
	}

	public void setTimeStep(long timeStep) {
		this.timeStep = timeStep;
	}

	public ResourceDemandListener<Resource> getResDemandListener() {
		return resDemandListener;
	}

	public void setResDemandListener(ResourceDemandListener<Resource> resDemandListener) {
		this.resDemandListener = resDemandListener;
	}

	public ResourceListener getResListener() {
		return resListener;
	}

	public void setResListener(ResourceListener resListener) {
		this.resListener = resListener;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isStatusListener() {
		return statusListener;
	}

	public void setStatusListener(boolean statusListener) {
		this.statusListener = statusListener;
	}

	public long getLastAccess() {
		return lastAccess;
	}

	public void setLastAccess(long lastAccess) {
		this.lastAccess = lastAccess;
	}

	public boolean isUpdateToSend() {
		return updateToSend;
	}

	public void setUpdateToSend(boolean updateToSend) {
		this.updateToSend = updateToSend;
	}

	public boolean isSensor() {
		return isSensor;
	}

	public void setSensor(boolean isSensor) {
		this.isSensor = isSensor;
	}

	public String getDptStr() {
		return dptStr;
	}

	public void setDptStr(String dptStr) {
		this.dptStr = dptStr;
	}

	public boolean isListener() {
		return isListener;
	}

	public void setListener(boolean isListener) {
		this.isListener = isListener;
	}

}
