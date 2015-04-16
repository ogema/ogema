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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

/**
 * Maps the 16 bit network addresses to the 64 bit ieee addresses. If a device leaves the network, it may receive a
 * different 16 bit network address when rejoining.
 * 
 * @author puschas
 * 
 */
public final class AddressMappings {
	private final Map<Short, Long> nwkToIeeeAddr;
	private final Map<Long, Short> ieeeToNwkAddr;
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("xbee-driver");

	public AddressMappings() {
		nwkToIeeeAddr = new HashMap<Short, Long>();
		ieeeToNwkAddr = new HashMap<Long, Short>();
	}

	public long getAddress64Bit(short address16Bit) {
		Long res = nwkToIeeeAddr.get(address16Bit);
		return res == null ? 0L : res;
	}

	public short getAddress16Bit(long address64Bit) {
		Short res = ieeeToNwkAddr.get(address64Bit);
		return res == null ? 0 : res;
	}

	public boolean containsAddress(short address16Bit) {
		return nwkToIeeeAddr.containsKey(address16Bit);
	}

	public boolean containsAddress(long address64Bit) {
		return ieeeToNwkAddr.containsKey(address64Bit);
	}

	public void addAddressMapping(long address64Bit, short address16Bit) {
		if (containsAddress(address64Bit)) { // replace old 16Bit address with
			// new one
			logger.debug("########################## Remove AddressMapping 1\t" + address64Bit + ":" + address16Bit);
			nwkToIeeeAddr.remove(ieeeToNwkAddr.get(address64Bit));
		}
		logger.debug("########################## Put AddressMapping 1\t" + address64Bit + ":" + address16Bit);
		nwkToIeeeAddr.put(address16Bit, address64Bit);
		ieeeToNwkAddr.put(address64Bit, address16Bit);
	}

	public void removeAddressMapping(short address16Bit) {
		logger.debug("########################## Remove AddressMapping 2\t" + address16Bit);
		ieeeToNwkAddr.remove(nwkToIeeeAddr.get(address16Bit));
		nwkToIeeeAddr.remove(address16Bit);
	}

	public void removeAddressMapping(long address64Bit) {
		logger.debug("########################## Remove AddressMapping 3\t" + address64Bit);
		nwkToIeeeAddr.remove(ieeeToNwkAddr.get(address64Bit));
		ieeeToNwkAddr.remove(address64Bit);
	}
}
