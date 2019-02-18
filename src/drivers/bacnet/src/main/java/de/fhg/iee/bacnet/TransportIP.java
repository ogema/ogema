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
package de.fhg.iee.bacnet;

import de.fhg.iee.bacnet.apdu.ProtocolControlInformation;
import de.fhg.iee.bacnet.api.DeviceAddress;
import de.fhg.iee.bacnet.api.Indication;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author jlapp
 */
public class TransportIP extends AbstractTransport {

    final int port;
    final DatagramSocket sock;
    InetAddress broadcast;
    Inet4Address localAddress;
    Thread receiverThread;

    /**
     * Create a new BACnet IP transport on the selected interface and port.
     * @param iface network interface
     * @param port port number, using 0 will automatically select a free port.
     */
    public TransportIP(NetworkInterface iface, int port) throws IOException {
        for (InterfaceAddress ia : iface.getInterfaceAddresses()) {
            if (ia.getAddress() instanceof Inet4Address) {
                localAddress = (Inet4Address) ia.getAddress();
                broadcast = ia.getBroadcast();
            }
        }
        this.sock = new DatagramSocket(null);
        this.sock.setReuseAddress(true);
        this.sock.setBroadcast(true);
        InetSocketAddress sockAddr = new InetSocketAddress(port);
        //this.sock.bind(port == 0 ? null : sockAddr);
        this.sock.bind(sockAddr);
        this.port = this.sock.getLocalPort();
        logger.debug("opened UDP port {}, broadcast={} ({}), reuse={}", this.port,
                sock.getBroadcast(), broadcast, sock.getReuseAddress());
        receiverThread = new Thread(receiver, getClass().getSimpleName() + " UDP receiver");
    }

    Runnable receiver = new Runnable() {

        @Override
        public void run() {
            byte[] buf = new byte[2048];
            DatagramPacket p = new DatagramPacket(buf, buf.length);
            while (!Thread.interrupted()) {
                try {
                    sock.receive(p);

                    logger.trace("received {} bytes from {}:{}", p.getLength(), p.getAddress(), p.getPort());

                    byte[] msg = new byte[p.getLength()];
                    System.arraycopy(buf, 0, msg, 0, msg.length);
                    ByteBuffer bb = ByteBuffer.wrap(msg);

                    //all virtual link control messages start with type, function & length
                    int bvlcType = Npdu.getUnsignedByte(bb);
                    int bvlcFunction = Npdu.getUnsignedByte(bb);
                    int bvlcLength = bb.order(ByteOrder.BIG_ENDIAN).getChar();
                    if (bvlcType != 0x81) {
                        logger.debug("package is not a BACnet/IP message, BVLC Type field has value {}", Integer.toHexString(bvlcType));
                        return;
                    }
                    if (bvlcFunction != 0x0a && bvlcFunction != 0x0b) {
                        logger.warn("received unsupported BVLC Function: {}", Integer.toHexString(bvlcType));
                        return;
                    }
                    Npdu npdu = new Npdu(bb);

                    BACnetIpAddress srcAddr = new BACnetIpAddress();
                    srcAddr.addr = p.getAddress();
                    srcAddr.npdu = npdu;
                    srcAddr.port = p.getPort();

                    if (npdu.isNetworkMessage()) {
                        logger.debug("ignoring network message (type {}) from {}", npdu.getMessageType(), srcAddr.addr);
                        continue;
                    }
                    ByteBuffer apdu = bb.slice().asReadOnlyBuffer();

                    ProtocolControlInformation pci = new ProtocolControlInformation(apdu);
                    int pciSize = apdu.position();
                    apdu.rewind();

                    apdu.position(pciSize);
                    apdu.mark();

                    Indication i = new DefaultIndication(srcAddr, pci, apdu.duplicate(), npdu.getPriority(), npdu.isExpectingReply(), TransportIP.this);
                    receivedPackage(i);

                    //FIXME: exception handling
                } catch (SocketException ex) {
                    if (Thread.interrupted()) {
                        logger.debug("transport closed");
                        break;
                    }
                    logger.error("exception in UDP receiver", ex);
                } catch (IOException ex) {
                    logger.error("exception in UDP receiver", ex);
                } catch (Exception ex) {
                    logger.error("exception in UDP receiver", ex);
                }
            }
        }
    };
    
    public static DeviceAddress createAddress(InetAddress ip, int port, Npdu npdu) {
        BACnetIpAddress addr = new BACnetIpAddress();
        addr.addr = ip;
        addr.port = port;
        addr.npdu = npdu;
        return addr;
    }

    static class BACnetIpAddress implements DeviceAddress {

        Npdu npdu;
        InetAddress addr;
        int port;

        @Override
        public DeviceAddress toDestinationAddress() {
            BACnetIpAddress dest = new BACnetIpAddress();
            dest.addr = addr;
            dest.port = port;
            //BACnet/IP does not use local address in NPDU
            dest.npdu = npdu.withoutSource();
            //devices reachable via bridge/router have source info
            if (npdu.hasSource()) {
                //TODO: hop count?
                dest.npdu = dest.npdu.withDestination(
                        npdu.getSourceNet(), npdu.getSourceAddress(), 255);
            }
            return dest;
        }

        @Override
        public String toString() {
            //TODO: include bacnet network info
            return addr.toString();
        }

    }
    
    public int getPort() {
        return port;
    }

    @Override
    public DeviceAddress getLocalAddress() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DeviceAddress getBroadcastAddress() {
        BACnetIpAddress addr = new BACnetIpAddress();
        addr.addr = broadcast;
        addr.port = this.port; //FIXME need target port
        addr.npdu = new Npdu().withDestination(0xFFFF, null, 255);
        return addr;
    }

    @Override
    protected void sendData(ByteBuffer data, Priority prio, boolean expectingReply, DeviceAddress destination) throws IOException {
        BACnetIpAddress addr = (BACnetIpAddress) destination;
        byte[] npduOctets = addr.npdu.withExpectingReply(expectingReply).toArray();
        int vlcSize = 4;
        int packetSize = vlcSize + npduOctets.length + data.limit();
        byte[] packetData = new byte[packetSize];
        //TODO need full VLC support(?) see spec J.2
        //write virtual link control
        packetData[0] = (byte) 0x81;
        packetData[1] = (byte) (addr.npdu.isBroadcast() ? 0x0b : 0x0a);
        packetData[2] = (byte) ((packetSize & 0xFF00) >> 8);
        packetData[3] = (byte) (packetSize & 0xFF);
        System.arraycopy(npduOctets, 0, packetData, 4, npduOctets.length);
        logger.trace("sending {} bytes to {}:{}", packetData.length, addr.addr, addr.port);
        data.get(packetData, vlcSize + npduOctets.length, data.limit());
        DatagramPacket p = new DatagramPacket(packetData, packetData.length, addr.addr, addr.port);
        sock.send(p);
    }

    @Override
    protected void doClose() throws IOException {
        receiverThread.interrupt();
        if (sock != null) {
            sock.close();
        }
    }

    @Override
    protected void doStart() {
        receiverThread.start();
    }

}
