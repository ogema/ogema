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
package org.ogema.drivers.homematic.xmlrpc.ll.api;

import java.util.List;
import org.ogema.drivers.homematic.xmlrpc.ll.HomeMaticService;

/**
 * Receives all {@link HmEvent}s delivered to a {@link HomeMaticService}.
 * 
 * @see HomeMaticService#addEventListener
 * 
 * @author jlapp
 */
public interface HmEventListener {
    
    /**
     * Events that arrive in the same XML-RPC multicall are delivered together.
     * 
     * @param events list of new events
     */
    void event(List<HmEvent> events);
    
}
