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
package org.ogema.channelmapperv2.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.service.command.Descriptor;
import org.ogema.channelmapperv2.ChannelMapper;

@Component(specVersion = "1.2", immediate = true)
@Properties( { @Property(name = "osgi.command.scope", value = "ChannelMapper"),
		@Property(name = "osgi.command.function", value = { "listConfigs" }) })
@Service(ShellCommands.class)
@Descriptor("OGEMA channel mapper commands")
public class ShellCommands {

	@Reference
	private ChannelMapper channelMapper;
	
	@Descriptor("list channel mapper configurations")
	public void listConfigs() {
		System.out.println("Channel mapper configurations");
		for (ChannelController cc: ((ChannelMapperImpl) channelMapper).resourceMappings.values()) {
			printChannelMapping(cc);
		}
		System.out.println();
	}
	
	private static void printChannelMapping(final ChannelController cc) {
		System.out.println("  Configuration " + cc.pattern.model.getLocation());
		System.out.println("    Resource: " + cc.pattern.target.getLocationResource());
		System.out.println("    Channel: " + cc.channelLocator);
		System.out.println("    Initialisation successful: " + (cc.pattern.registrationSuccessful.isActive() && cc.pattern.registrationSuccessful.getValue()));
	}
	
}
