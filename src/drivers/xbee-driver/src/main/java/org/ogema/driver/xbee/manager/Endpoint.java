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
package org.ogema.driver.xbee.manager;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.ogema.driver.xbee.manager.zcl.BasicCluster;
import org.ogema.driver.xbee.manager.zcl.Cluster;
import org.ogema.driver.xbee.manager.zcl.ColorControlCluster;
import org.ogema.driver.xbee.manager.zcl.GroupsCluster;
import org.ogema.driver.xbee.manager.zcl.IdentifyCluster;
import org.ogema.driver.xbee.manager.zcl.LevelControlCluster;
import org.ogema.driver.xbee.manager.zcl.OnOffCluster;
import org.ogema.driver.xbee.manager.zcl.ScenesCluster;
import org.ogema.driver.xbee.manager.zcl.ZllCommissioningCluster;

/**
 * A ZigBee Endpoint which is used as an OGEMA Device.
 * 
 * @author puschas
 * 
 */
public class Endpoint {
	private short profileId;
	private RemoteDevice device;
	private byte endpointId;
	private SimpleDescriptor simpleDescriptor;
	private Map<Short, Cluster> clusters = new HashMap<Short, Cluster>();

	public Endpoint(RemoteDevice device, byte endpointId) {
		this.setEndpointId(endpointId);
		this.device = device;
	}

	public short getProfileId() {
		if (profileId == (short) 0xc05e) // see ZigBee Document 11-0037-10 page 80 section 8.1.4
			return (short) 0x0104;
		else
			return profileId;
	}

	public SimpleDescriptor getSimpleDescriptor() {
		return simpleDescriptor;
	}

	public void setSimpleDescriptor(SimpleDescriptor simpleDescriptor) {
		this.simpleDescriptor = simpleDescriptor;
		profileId = simpleDescriptor.getApplicationProfileId();
	}

	/**
	 * This method goes iterates through all clusters in the SimpleDescriptor. If it is a "known" Cluster that has it's
	 * own class, a new object of that class will be created and put in the clusters map. If it is an "unknown" Cluster
	 * it will be created as a generic Cluster object.
	 */
	public void parseClusters() {
		short clusterId;
		ByteBuffer bb = ByteBuffer.wrap(simpleDescriptor.getInputClusterList());
		for (int i = 0; i < simpleDescriptor.getInputClusterCount(); ++i) {
			clusterId = bb.getShort();
			switch (clusterId) {
			case 0x0000:
				clusters.put(clusterId, new BasicCluster(this));
				break;
			case 0x0003:
				clusters.put(clusterId, new IdentifyCluster(this));
				break;
			case 0x0004:
				clusters.put(clusterId, new GroupsCluster(this));
				break;
			case 0x0005:
				clusters.put(clusterId, new ScenesCluster(this));
				break;
			case 0x0006:
				clusters.put(clusterId, new OnOffCluster(this));
				break;
			case 0x0008:
				clusters.put(clusterId, new LevelControlCluster(this));
				break;
			case 0x0300:
				clusters.put(clusterId, new ColorControlCluster(this));
				break;
			case 0x1000:
				clusters.put(clusterId, new ZllCommissioningCluster(this));
				break;
			default:
				clusters.put(clusterId, new Cluster(clusterId, this));
			}
		}
		bb = ByteBuffer.wrap(simpleDescriptor.getOutputClusterList());
		for (int i = 0; i < simpleDescriptor.getOutputClusterCount(); ++i) {
			clusterId = bb.getShort();
			switch (clusterId) {
			case 0x0000:
				clusters.put(clusterId, new BasicCluster(this));
				break;
			case 0x0003:
				clusters.put(clusterId, new IdentifyCluster(this));
				break;
			case 0x0004:
				clusters.put(clusterId, new GroupsCluster(this));
				break;
			case 0x0005:
				clusters.put(clusterId, new ScenesCluster(this));
				break;
			case 0x0006:
				clusters.put(clusterId, new OnOffCluster(this));
				break;
			case 0x0008:
				clusters.put(clusterId, new LevelControlCluster(this));
				break;
			case 0x0300:
				clusters.put(clusterId, new ColorControlCluster(this));
				break;
			default:
				clusters.put(clusterId, new Cluster(clusterId, this));
			}
		}
	}

	public RemoteDevice getDevice() {
		return device;
	}

	public byte getEndpointId() {
		return endpointId;
	}

	public void setEndpointId(byte endpointId) {
		this.endpointId = endpointId;
	}

	public Map<Short, Cluster> getClusters() {
		return clusters;
	}
}
