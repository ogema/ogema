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
package org.ogema.driver.knxdriver;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceValueListener;

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
	private ResourceValueListener<Resource> resListener;
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

	public ResourceValueListener<?> getResListener() {
		return resListener;
	}

	public void setResListener(ResourceValueListener<Resource> resListener) {
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
