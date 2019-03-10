/**
 * Copyright 2011-2019 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
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
package org.ogema.drivers.homematic.xmlrpc.hl.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ogema.drivers.homematic.xmlrpc.hl.HmConnection;

public class UdpDiscovery implements AutoCloseable {
	
	private static final byte UDP_SEPARATOR = 0x00;
	private static final byte UDP_END = 0x49;
    private static final byte[] DEVICE_TYPE = "eQ3-*".getBytes(StandardCharsets.UTF_8);
    public static final String DEFAULT_SERIAL_NR = "*";
    public static final long LISTEN_TIMEOUT = 5000;
    
    private final List<Future<Collection<Ccu>>> results = Collections.synchronizedList(new ArrayList<Future<Collection<Ccu>>>());
//	private final InetAddress address;
	private final ExecutorService exec  = Executors.newFixedThreadPool(4);
	
	public void submit(final InetAddress broadcastAddress, final NetworkInterface nif, final String serialNumber) {
		try {
			results.add(exec.submit(new Callable<Collection<Ccu>>() {
	
				@Override
				public Collection<Ccu> call() throws Exception {
					return sendBroadcast(broadcastAddress, nif, serialNumber);
				}
				
			}));
		} catch (RejectedExecutionException ok) {}
	}

	@Override
	public void close() {
		exec.shutdownNow();
	}
	
	/**
	 * @return null if no Ccus have been found yet, the collection of found ccus otherwise
	 */
	public Collection<Ccu> getIntermediateResults(long waitTimeMillis) {
		final long start = System.currentTimeMillis();
		for (Future<Collection<Ccu>> f : results) {
			waitTimeMillis = waitTimeMillis <= 0 ? waitTimeMillis : waitTimeMillis - (System.currentTimeMillis() - start);
			if (!f.isDone() && waitTimeMillis <= 0)
				return null;
			try {
				final Collection<Ccu> ccus = f.get(waitTimeMillis <= 0 ? 1 : waitTimeMillis, TimeUnit.MILLISECONDS);
				if (!ccus.isEmpty())
					return ccus;
			} catch (InterruptedException e) {
				try {
					Thread.currentThread().interrupt();
				} catch (SecurityException ee) {}
				return Collections.emptySet();
			} catch (TimeoutException e) {
				return null;
			} catch (Exception e) {
				continue;
			}
		}
		return null;
	}
	
    private Collection<Ccu> sendBroadcast(InetAddress address, final NetworkInterface nif, String serialNumber) throws IOException {
    	HmConnection.logger.trace("Starting Homematic CCU discovery scan for {}", address);
        final MulticastSocket socket = new MulticastSocket();
        socket.setBroadcast(true);
        socket.setTimeToLive(5);
        socket.setSoTimeout(1000);
        socket.setNetworkInterface(nif);
        final byte[] sender = send(socket, address, serialNumber);
        return Collections.unmodifiableCollection(listen(sender, socket));
    }

    private static byte[] send(MulticastSocket socket, InetAddress broadcastAddress, String serialNumber) throws IOException {
    	final byte[] sender = new byte[3];
    	new Random().nextBytes(sender);
    	final ByteBuffer bb = ByteBuffer.allocate(256);
    	bb.put((byte) 0x02);
    	bb.put(sender)
    		.put(UDP_SEPARATOR)
    		.put(DEVICE_TYPE)
    		.put(UDP_SEPARATOR)
    		.put(serialNumber.getBytes(StandardCharsets.UTF_8))
    		.put(UDP_SEPARATOR)
    		.put(UDP_END);
    	final int pos = bb.position();
    	bb.rewind();
        DatagramPacket packet = new DatagramPacket(bb.array(), pos, broadcastAddress, 43439);
        socket.send(packet);
        return sender;
    }

    /**
     * Listens to CCU responses
     */
    private static Collection<Ccu> listen(byte[] sender, MulticastSocket socket) throws IOException {
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        final Set<Ccu> responses = new HashSet<>(); 
        while (currentTime - startTime < LISTEN_TIMEOUT) {
            final Ccu ccu = waitForCcuResponse(sender, socket);
            if (ccu != null)
            	responses.add(ccu);
            currentTime = System.currentTimeMillis();
        }
        HmConnection.logger.trace("CCU responses {}", responses);
        socket.close();
        return responses;
    }

    /**
     * Extract the CCU data from the UDP response
     */
    private static Ccu waitForCcuResponse(final byte[] sender, MulticastSocket socket) throws IOException {
        try {
        	final byte[] buffer = new byte[256];
        	final DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
            socket.receive(dp);
            final byte[] result = dp.getData();
            final ByteBuffer bb = ByteBuffer.wrap(result);
            if (!bb.hasRemaining() || bb.get() != 0x02)
            	return null;
            for (int i = 0; i < sender.length; i++) {
            	if (!bb.hasRemaining() || bb.get() != sender[i])
            		return null;
            }
            final String type = parseNextString(bb);
            final String serialNr = parseNextString(bb);
            return new Ccu(dp.getAddress(), socket.getNetworkInterface(), type, serialNr);
        } catch (SocketTimeoutException ex) {
        	return null;
        }
    }
    
    private static String parseNextString(final ByteBuffer bb) {
    	 final StringBuilder sb = new StringBuilder();
    	 boolean started = false;
    	 while (bb.hasRemaining()) {
    		 byte b = bb.get();
    		 if (b == 0x00) {
    			 if (started)
    				 break;
    			 else
    				 continue;
    		 }
    		 if (b != 0x00) {
    			 sb.append((char) b);
    			 started = true;
    		 }
    	 }
    	 return sb.toString();
    }

}
