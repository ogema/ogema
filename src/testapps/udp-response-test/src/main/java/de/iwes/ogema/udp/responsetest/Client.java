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
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Simple client for driving the {@link UdpReceiver} / {@link UdpReplier} apps.
 * Usage: {@code java -jar <upd-response-test-jar> <host> <port> [delay=0]}
 * 
 * @author jlapp
 */
public class Client {

    static Selector sel;
    static Map<Long, Long> sendTimes = new HashMap<>();
    static long seq = 0;
    static long count = 0;
    static long totalTime = 0;
    static float avg = 0;
    
    static final String ANSI_CSI = "\u001B[";
    static final String CLEAR_LINE = ANSI_CSI + "2K";
    static final String CURSOR_PREVIOUS_LINE = ANSI_CSI + "1F";
    static final String CURSOR_COL1 = ANSI_CSI + "1G";
    static final String YELLOW_BOLD = ANSI_CSI + "33;1m";
    static final String ATTR_RESET = ANSI_CSI + "0m";

    static void process() {
        ByteBuffer buf = ByteBuffer.allocate(8);
        String msg;
        if (System.getProperty("os.name").startsWith("Win")){
            msg = "sent: %d, received: %d, avg=%2fms%n";
        } else {
            msg = CLEAR_LINE+CURSOR_COL1+"sent: %d, received: %d, avg=%2fms"+ATTR_RESET;
        }        
        while (!Thread.interrupted()) {
            try {
                if (sel.select() == 0) {
                    continue;
                }
                for (Iterator<SelectionKey> keys = sel.selectedKeys().iterator(); keys.hasNext();) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    DatagramChannel channel = (DatagramChannel) key.channel();

                    SocketAddress src = channel.receive(buf);
                    long l = buf.getLong(0);
                    Long sent = sendTimes.remove(l);
                    if (sent == null) {
                        //FIXME: probably a bug in OGEMA (lost update?)
                        System.out.printf("%nsequence number already processed: %d, unprocessed: %s%n", l, sendTimes.keySet());
                        buf.clear();
                        continue;
                    }
                    long roundTripTime = System.nanoTime() - sent;
                    totalTime += roundTripTime;
                    count++;
                    if (count % 50 == 0){
                        System.out.printf(msg, seq, count, (totalTime / (double) count) / 1000000d);
                    }

                    buf.clear();
                }
            } catch (IOException ioex) {
                System.err.println(ioex);
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        sel = Selector.open();
        DatagramChannel channel = DatagramChannel.open();
        channel.bind(new InetSocketAddress(45678));
        channel.configureBlocking(false);

        channel.register(sel, SelectionKey.OP_READ);
        
        String host = args.length == 0 ? "eebus-pi.local" : args[0];
        int port = args.length == 0 ? 4715 : Integer.parseInt(args[1]);

        SocketAddress target = new InetSocketAddress(host, port);

        //Thread server = new Thread(Client::process);
        Thread server = new Thread(new Runnable() {
            @Override
            public void run() {
                process();
            }
        });
        
        server.start();

        ByteBuffer b = ByteBuffer.allocate(8);
        seq = 0;
        long delay = 0;
        if (args.length == 3) {
            delay = Long.parseLong(args[2]);
        }
        while (System.in.available() == 0) {
            b.putLong(0, seq);
            sendTimes.put(seq++, System.nanoTime());
            channel.send(b, target);
            b.rewind();
            if (delay > 0) {
                Thread.sleep(delay);
            }
        }
        server.interrupt();
        sel.close();
        channel.close();
    }

}
