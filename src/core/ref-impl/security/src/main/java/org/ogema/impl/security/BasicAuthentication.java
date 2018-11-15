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
package org.ogema.impl.security;

import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.Authenticator;
import org.ogema.core.administration.CredentialStore;

import com.google.common.io.BaseEncoding;

@Service(Authenticator.class)
@Component
@Property(name=Authenticator.AUTHENTICATOR_ID, value=Authenticator.DEFAULT_BASIC_AUTH)
public class BasicAuthentication implements Authenticator {

	@Reference
	private CredentialStore credentials;
	
	@Override
	public String authenticate(HttpServletRequest req) {
		final String header = req.getHeader("Authorization");
		if (header == null || !header.startsWith("Basic "))
			return null;
		// Java 8 has this built in, for Java 7 support we use guava
		final String decoded = new String(BaseEncoding.base64().decode(header.substring("Basic ".length())), StandardCharsets.UTF_8);
		final String[] split = decoded.split(":");
		if (split.length != 2)
			return null;
		if (credentials.login(split[0], split[1]))
			return split[0];
		else
			return null;
	}

}
