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
