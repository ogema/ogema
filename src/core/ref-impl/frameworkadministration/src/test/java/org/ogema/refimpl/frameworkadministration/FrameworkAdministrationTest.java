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
package org.ogema.refimpl.frameworkadministration;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.ogema.frameworkadministration.servlet.FAServletLogger;

public class FrameworkAdministrationTest {

	@BeforeClass
	public static void initClass() {

	}

	@Before
	public void init() {

	}

	@AfterClass
	public static void afterClass() {

	}

	@After
	public void tearDown() {

	}

	@Test
	@Ignore
	public void testJSON() throws IOException {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		//setMucConfig(request, "1", "WA256", "192.168.2.112", "8080", "0000");

		Mockito.when(request.getParameter("data")).thenReturn(dummyData());
		doPost(request, response);
		//doGet(request, response);
		System.out.println(response.getOutputStream().toString());
		//  Mockito.verify(request, Mockito.atLeast(1)).getParameter("username"); // only if you want to verify username was called...
	}

	private String dummyData() {
		return "{\"path\":\"data\\\\logs\",\"sizeFile\":20971520,\"sizeCache\":1048576,\"list\":[{\"name\":\"org.eclipse.jetty.security.MappedLoginService\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ogema.hardwaremanager.impl.HardwareManagerImpl\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.ogema.application.manager.impl.AppIDImpl\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ogema.application.manager.impl.ApplicationManagerImpl-org.ogema.frameworkadministration.FrameworkAdministration\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.util.ssl.SslContextFactory\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.util.security.Password\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.util.thread.Timeout\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.http.AbstractGenerator\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ogema.hardwaremanager.impl.Activator\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.ops4j.pax.wicket.internal.extender.BundleImportExtender\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.eclipse.jetty.servlet.ServletHandler\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.resource.loader.InitializerStringResourceLoader\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ogema.application.manager.impl.ApplicationManagerImpl-org.ogema.rest.servlet.RecordedDataServlet\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ops4j.pax.wicket.internal.PaxWicketPageTracker\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.ogema.apps.admin.OgemaAdmin\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"resource-manager-[5]_FrameworkAdministration@62/framework-administration-2.0-SNAPSHOT\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.markup.html.SecurePackageResourceGuard\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ogema.recordeddata.slotsdb.db.FileObjectProxy\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"/\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.ops4j.pax.wicket.internal.PageMounterTracker\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.ogema.application.manager.impl.ApplicationManagerImpl-org.ogema.webresourcemanager.impl.internal.MyWebResourceManager\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ogema.rest.servlet.RecordedDataServlet\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.ogema.rest.servlet.RestServlet\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.ops4j.pax.wicket.internal.HttpTracker\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.ogema.ref-impl.administration.2.0.0.SNAPSHOT\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.util.string.StringValue\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ops4j.pax.wicket.internal.PaxWicketApplicationFactory\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.apache.wicket.request.mapper.CompoundRequestMapper\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ogema.ref-impl.framework-administration.2.0.0.SNAPSHOT\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.ops4j.pax.wicket.internal.PaxWicketAppFactoryTracker\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.ops4j.pax.wicket.internal.BundleDelegatingPageMounter\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.apache.wicket.Application\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ogema.apps.admin.AdminServlet\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.core.request.mapper.BasicResourceReferenceMapper\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.markup.html.PackageResourceGuard\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ogema.resourcemanager.impl.ApplicationResourceManager\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.ops4j.pax.wicket.internal.filter.FilterDelegator\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.apache.wicket.resource.loader.ComponentStringResourceLoader\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ops4j.pax.wicket.internal.DelegatingClassResolver\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.ogema.resourcemanager.impl.ResourceDBManager\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.ops4j.pax.wicket.internal.BundleDelegatingClassResolver\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.apache.wicket.markup.parser.AbstractMarkupFilter\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.util.component.AbstractLifeCycle\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.markup.resolver.WicketMessageResolver\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.protocol.http.WebApplication\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.request.resource.ResourceReferenceRegistry\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.markup.resolver.WicketContainerResolver\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.util.component.AggregateLifeCycle\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ogema.application.manager.impl.ApplicationManagerImpl-org.ogema.apps.admin.OgemaAdmin\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ogema.service.ogema-gui-impl.2.0.0.SNAPSHOT\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.server.session\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.util.resource.FileResource\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ogema.ref-impl.channel-manager.2.0.0.SNAPSHOT\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.core.util.file.WebApplicationPath\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ops4j.pax.wicket.internal.filter.FilterTrackerCustomizer\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.eclipse.jetty.util.resource.URLResource\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ops4j.pax.wicket.api.InjectorHolder\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.ogema.frameworkadministration.FrameworkAdministration\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.apache.felix.http.jetty.2.3.0\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.util.component.Container\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.security.HashLoginService\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ogema.ref-impl.security.2.0.0.SNAPSHOT\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.ops4j.pax.wicket.internal.servlet.ServletDescriptor\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.eclipse.jetty.server.handler.ContextHandlerCollection\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.resource.loader.PackageStringResourceLoader\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.util.resource.Resource\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.server.Server\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.util.encoding.UrlDecoder\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.http.MimeTypes\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ops4j.pax.wicket.internal.injection.BundleDelegatingComponentInstanciationListener\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"resource-manager-[4]_OgemaAdmin@60/ogema-admin-2.0-SNAPSHOT\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.ogema.webresourcemanager.impl.internal.MyWebResourceManager\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.server.handler.AbstractHandler\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ops4j.pax.wicket.internal.servlet.ServletCallInterceptor\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.apache.felix.shell.remote.1.1.2\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.servlet.ServletHolder\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.serialize.java.JavaSerializer\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ogema.impl.security.RestAccess\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.ogema.persistence.impl.mem.MemoryResourceDB\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.ogema.rest.servlet.RestAccess\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.markup.resolver.AutoLinkResolver\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ogema.application.manager.impl.ApplicationTracker\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.apache.felix.configadmin.1.8.0\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.server.AbstractConnector\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.io.AbstractBuffer\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"resource-manager-[1]_RestServlet@56/rest-2.0-SNAPSHOT\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.ogema.application.manager.impl.ApplicationManagerImpl-org.ogema.rest.servlet.RestServlet\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.util.thread.QueuedThreadPool\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.util.StringUtil\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.request.cycle.RequestCycleListenerCollection\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.core.request.mapper.AbstractBookmarkableMapper\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.security.PropertyUserStore\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ogema.impl.security.WebAccessManagerImpl\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.RequestListenerInterface\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.apache.felix.metatype.1.0.10\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.io.nio\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.server.session.AbstractSessionIdManager\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.ogema.impl.security.OgemaHttpContext\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.apache.felix.scr.1.8.2\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.ops4j.pax.wicket.internal.injection.BundleAnalysingComponentInstantiationListener\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.ops4j.pax.wicket.internal.extender.PaxWicketBundleListener\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.ogema.recordeddata.slotsdb.SlotsDb\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.markup.html.form.AutoLabelResolver\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.servlet.Holder\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.util.io.IOUtils\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ops4j.pax.wicket.internal.Activator\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.ogema.impl.security.RestHttpContext\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.ogema.ref-impl.ogema-logger.2.0.0.SNAPSHOT\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.markup.parser.filter.RelativePathPrefixHandler\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ogema.ref-impl.rest.2.0.0.SNAPSHOT\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"resource-manager-[2]_RecordedDataServlet@56/rest-2.0-SNAPSHOT\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"resource-manager-[3]_MyWebResourceManager@59/ogema-gui-impl-2.0-SNAPSHOT\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.ops4j.pax.wicket.internal.extender.BundleDelegatingExtensionTracker\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.eclipse.jetty.server.handler.ContextHandler\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.util.security.Credential\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.http.HttpGenerator\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ogema.impl.security.AccessManagerImpl\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.ogema.resourcemanager.impl.model.ResourceFactoryASM\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.protocol.http.WicketFilter\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ops4j.pax.wicket.spi.support.DelegatingComponentInstanciationListener\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.apache.wicket.request.resource.ClassScanner\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.util.listener.ListenerCollection\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.ops4j.pax.wicket.internal.GenericContext\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.ops4j.pax.wicket.internal.injection.registry.OSGiServiceRegistryProxyTargetLocatorFactory\",\"file\":\"ERROR\",\"cache\":\"ERROR\",\"console\":\"ERROR\"},{\"name\":\"org.ogema.ref-impl.app-manager.2.0.0.SNAPSHOT\",\"file\":\"DEBUG\",\"cache\":\"TRACE\",\"console\":\"INFO\"},{\"name\":\"org.eclipse.jetty.util.log\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"},{\"name\":\"org.apache.wicket.core.util.lang.WicketObjects\",\"file\":\"INFO\",\"cache\":\"INFO\",\"console\":\"INFO\"}]}";
	}

	private HttpServletResponse doPost(HttpServletRequest request, HttpServletResponse response) {

		try {
			new FAServletLogger().doPost(request, response);
		} catch (ServletException ex) {
			Logger.getLogger(FrameworkAdministrationTest.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(FrameworkAdministrationTest.class.getName()).log(Level.SEVERE, null, ex);
		}
		return response;
	}

	@SuppressWarnings("unused")
	private void doGet(HttpServletRequest request, HttpServletResponse response) {

		try {
			new FAServletLogger().doGet(request, response);
		} catch (ServletException ex) {
			Logger.getLogger(FrameworkAdministrationTest.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(FrameworkAdministrationTest.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
