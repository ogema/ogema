package org.ogema.tools.remote.ogema.auth.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.ogema.tools.remote.ogema.auth.RemoteOgemaAuth;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.google.common.io.BaseEncoding;

@Component(
		service=RemoteOgemaAuth.class,
		configurationPid=RemoteAuthImpl.PID, // will be overwritten by full PID
		configurationPolicy=ConfigurationPolicy.REQUIRE,
		property= {
				"service.factoryPid=" + RemoteAuthImpl.PID
		}
)
@Designate(ocd=RemoteAuthImpl.Config.class)
public class RemoteAuthImpl implements RemoteOgemaAuth {
	
	public static final String PID = "org.ogema.tools.RemoteOgemaAuthImpl";
	
	@ObjectClassDefinition
	public static @interface Config {
		
		/**
		 * Note: this property is used mainly for the purpose to select the correct service instance.
		 * The auth service itself does not evaluate it.
		 * See {@link RemoteOgemaAuth#REMOTE_HOST_PROPERTY}
		 * @return
		 */
		@AttributeDefinition(description="Target remote host", defaultValue="localhost")
		String remoteHost() default "localhost";
		
		/**
		 * Note: this property is used mainly for the purpose to select the correct service instance.
		 * The auth service itself does not evaluate it.
		 * See {@link RemoteOgemaAuth#REMOTE_PORT_PROPERTY}
		 * @return
		 */
		@AttributeDefinition(description="Target remote port", defaultValue="8443")
		int remotePort() default 8443;
		
		@AttributeDefinition(description="The REST user of the remote OGEMA instance")
		String remoteUser();
		
		/**
		 * Note: this will be converted to a property ".password" (note the dot at the beginning)
		 * @return
		 */
		@AttributeDefinition(type=AttributeType.PASSWORD, description="Password of the remote REST user")
		String _remotePassword();
		
	}
	
	private Config config;
	
	@Activate
	protected void activate(Config config) {
		this.config = config;
	}

	@Override
	public CloseableHttpResponse execute(CloseableHttpClient client, HttpUriRequest request) throws ClientProtocolException, IOException {
		Objects.requireNonNull(request);
		Objects.requireNonNull(client);
		final String str = config.remoteUser() + ":" + config._remotePassword();
		// not using the built-in Base64 class for Java 7 compatibility
		request.addHeader("Authorization", "Basic " + BaseEncoding.base64().encode(str.getBytes(StandardCharsets.UTF_8)));
		try {
			return client.execute(request);
		} finally {
			request.removeHeaders("Authorization"); // -> header not visible to services using this?
		}
	}

	
	
}
