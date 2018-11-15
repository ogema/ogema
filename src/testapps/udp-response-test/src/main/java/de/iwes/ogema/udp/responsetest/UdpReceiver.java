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
package de.iwes.ogema.udp.responsetest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.Transaction;
import org.osgi.service.component.ComponentContext;

/**
 * Receives UDP packets containing a single long value and writes the package data
 * to a {@link UdpTestData} resource.
 * 
 */
@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
public class UdpReceiver implements Application {

	/**
	 * configuration property ({@value}) for the UDP server socket port
	 */
	@Property(intValue = 4715)
	public static final String SOCKET = "port";

	protected OgemaLogger logger;
	protected ApplicationManager appMan;
	protected ResourceManagement resMan;
	protected ResourceAccess resAcc;

	int port;
	DatagramChannel channel;
	Selector sel;
	Thread server;
	UdpTestData test;

	Runnable serverImpl = new Runnable() {

		@Override
		public void run() {
			ByteBuffer buf = ByteBuffer.allocate(8);
			while (!Thread.interrupted()) {
				try {
					if (sel.select() == 0) {
						continue;
					}
					for (Iterator<SelectionKey> keys = sel.selectedKeys().iterator(); keys.hasNext();) {
						SelectionKey key = keys.next();
						keys.remove();
						DatagramChannel channel = (DatagramChannel) key.channel();

						InetSocketAddress src = (InetSocketAddress) channel.receive(buf);
						long l = buf.getLong(0);

						Transaction t = resAcc.createTransaction();
						//XXX why do i have to call addResource first?
						t.addResource(test.sequenceNumber());
						t.addResource(test.sourceHost());
						t.addResource(test.sourcePort());
						t.setTime(test.sequenceNumber(), l);
						t.setString(test.sourceHost(), src.getHostString());
						t.setInteger(test.sourcePort(), src.getPort());
						t.write();

						//test.sequenceNumber().setValue(l);
						//System.out.println(l);

						buf.rewind();
						//channel.send(buf, src);
						buf.clear();
					}
				} catch (IOException ioex) {
					appMan.getLogger().error("", ioex);
				}
			}
			appMan.getLogger().debug("server thread stopped");
		}

	};

	@Activate
	protected void activate(ComponentContext ctx) {
		port = Integer.parseInt(String.valueOf(ctx.getProperties().get(SOCKET)));
	}

	@Override
	public void start(ApplicationManager appManager) {
		// Store references to the application manager and common services for future use.
		this.appMan = appManager;
		this.logger = appManager.getLogger();
		this.resMan = appManager.getResourceManagement();
		this.resAcc = appManager.getResourceAccess();

		test = resMan.createResource(resMan.getUniqueResourceName("test"), UdpTestData.class);
		test.sequenceNumber().create();
		test.sourceHost().create();
		test.sourcePort().create();
		test.activate(true);

		try {
			channel = DatagramChannel.open();
			channel.bind(new InetSocketAddress(port));
			channel.configureBlocking(false);
			sel = Selector.open();//channel.provider().openSelector();
			channel.register(sel, SelectionKey.OP_READ);
		} catch (IOException se) {
			throw new RuntimeException(se);
		}
		server = new Thread(serverImpl);
		server.start();

		logger.debug("{} started", getClass().getName());
	}

	@Override
	public void stop(AppStopReason reason) {
		server.interrupt();
		try {
			sel.close();
			channel.close();
		} catch (IOException ioex) {
			appMan.getLogger().error("could not close UDP channel", ioex);
		}
		test.delete();
		logger.debug("{} stopped", getClass().getName());
	}

}
