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

import de.fhg.iee.bacnet.apdu.ApduConstants;
import de.fhg.iee.bacnet.apdu.ProtocolControlInformation;
import de.fhg.iee.bacnet.api.DeviceAddress;
import de.fhg.iee.bacnet.api.Indication;
import de.fhg.iee.bacnet.api.IndicationListener;
import de.fhg.iee.bacnet.api.Transport;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Transport base class that handles invoke IDs and IndicationListeners
 * for concrete implementations. Implementations need to perform the link level
 * I/O through
 * {@link #sendData(java.nio.ByteBuffer, de.iwes.bacnet.api.Transport.Priority, boolean, de.iwes.bacnet.api.DeviceAddress) sendData}
 * and {@link #receivedPackage(de.iwes.bacnet.api.Indication) receivedPackage}.
 *
 * @author jlapp
 */
public abstract class AbstractTransport implements Transport {

    Collection<IndicationListener<?>> listeners = new ConcurrentLinkedQueue<>();
    final PriorityQueue<PendingReply> pendingReplies = new PriorityQueue<>();
    InvokeIds invokeIds = new InvokeIds();

    final Logger logger = LoggerFactory.getLogger(getClass());
    final Thread timeoutThread;

    private long messageTimeout = 1000;
    private int messageRetries = 3;
    
    private final ThreadGroup executorThreads = new ThreadGroup("BACnet UDP transport executors");
    private final ExecutorService executor;

    public AbstractTransport() {
        timeoutThread = new Thread(timeoutHandler, getClass().getSimpleName() + " Timeout and Retry Handler");
        executor = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(executorThreads, r, AbstractTransport.this.getClass().getSimpleName() + " executor");
                return t;
            }
        });
    }

    private static class InvokeIds {

        BitSet ids = new BitSet(256);
        int lastId = 42;

        synchronized int getId() {
            if (ids.nextClearBit(0) > 255) {
                throw new IllegalStateException("out of invoke IDs");
            }
            do {
                lastId = (lastId + 1) % 256;
            } while (ids.get(lastId));
            ids.set(lastId);
            return lastId;
        }

        synchronized void release(int id) {
            ids.clear(id & 255);
        }

    }

    private class PendingReply implements Comparable<PendingReply> {

        private final int invokeId;
        private final DeviceAddress destination;
        private final ByteBuffer data;
        private final Priority prio;
        private final IndicationListener<?> listener;
        private final IndicationFuture<?> f;
        private int tryNumber = 1;
        private volatile long expiryTime;

        public PendingReply(int invokeId, DeviceAddress destination, ByteBuffer data, Priority prio, IndicationListener<?> listener, IndicationFuture<?> f) {
            this.invokeId = invokeId;
            this.destination = destination;
            this.data = data.duplicate();
            this.prio = prio;
            this.listener = listener;
            this.f = f;
            expiryTime = System.currentTimeMillis() + messageTimeout;
        }

        public int getInvokeId() {
            return invokeId;
        }

        public IndicationListener<?> getListener() {
            return listener;
        }

        public int getTryNumber() {
            return tryNumber;
        }

        public long getExpiryTime() {
            return expiryTime;
        }

        public void setTryNumber(int tryNumber) {
            this.tryNumber = tryNumber;
        }

        @Override
        public int compareTo(PendingReply o) {
            int cmpTimes = Long.compare(expiryTime, o.expiryTime);
            return cmpTimes;
        }

    }

    private final Runnable timeoutHandler = new Runnable() {

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                synchronized (pendingReplies) {
                    if (pendingReplies.isEmpty()) {
                        try {
                            pendingReplies.wait();
                        } catch (InterruptedException ie) {
                            //shutdown...
                            break;
                        }
                        continue;
                    }
                    PendingReply next = pendingReplies.peek();
                    long now = System.currentTimeMillis();
                    if (now >= next.expiryTime) {
                        pendingReplies.remove();
                        if (next.tryNumber >= messageRetries) {
                            //TODO: notify listener
                            logger.warn("no reply from {} for invoke ID {}", next.destination, next.invokeId);
                        } else {
                            next.tryNumber++;
                            next.expiryTime = now + messageTimeout;
                            pendingReplies.offer(next);
                            try {
                                logger.trace("resending message to {} for invoke ID {}", next.destination, next.invokeId);
                                //TODO: blocking i/o ?
                                next.data.rewind();
                                sendData(next.data, next.prio, true, next.destination);
                            } catch (IOException ex) {
                                //TODO: logging
                                logger.warn("resending failed", ex);
                            }
                        }
                    }
                    try {
                        long wait = next.expiryTime - now;
                        if (wait > 0) {
                            pendingReplies.wait(wait);
                        }
                    } catch (InterruptedException ie) {
                        //shutdown...
                        break;
                    }
                }
            }
            logger.debug("Message timeout handler shutting down.");
        }
    };

    protected final boolean hasLocalInvokeId(ProtocolControlInformation pci) {
        int pduType = pci.getPduType();
        return pduType == ApduConstants.TYPE_COMPLEX_ACK || pduType == ApduConstants.TYPE_SIMPLE_ACK || pduType == ApduConstants.TYPE_SEGMENT_ACK || pduType == ApduConstants.TYPE_ERROR || pduType == ApduConstants.TYPE_REJECT || pduType == ApduConstants.TYPE_ABORT;
    }

    private void executeListener(final PendingReply r, final Indication i) {
        if (r.getListener() == null) {
            return;
        }
        Runnable listenerCall = new Runnable() {
            @Override
            public void run() {
                try {
                    Object v = r.getListener().event(i);
                    r.f.resolve(v, null);
                } catch (Throwable t) {
                    //logger.warn("exception in event listener", t);
                    r.f.resolve(null, t);
                }
            }
        };
        executor.execute(listenerCall);
    }

    protected final void receivedPackage(Indication indication) {
        boolean indicationHandled = false;
        ProtocolControlInformation pci = indication.getProtocolControlInfo();
        if (hasLocalInvokeId(pci)) {
            int invokeId = pci.getInvokeId();
            invokeIds.release(invokeId);
            logger.trace("received message from {} for invoke ID {}", indication.getSource(), invokeId);
            synchronized (pendingReplies) {
                Iterator<PendingReply> it = pendingReplies.iterator();
                while (it.hasNext()) {
                    PendingReply r = it.next();
                    if (r.getInvokeId() == invokeId) {
                        it.remove();
                        indicationHandled = true;
                        Indication i = new DefaultIndication(indication);
                        executeListener(r, i);
                        break;
                    }
                }
                pendingReplies.notifyAll();
            }
        }
        if (!indicationHandled) {
            for (IndicationListener l : listeners) {
                //TODO: needs executor
                Indication i = new DefaultIndication(indication);
                l.event(i);
            }
        }
    }

    @Override
    public final void addListener(IndicationListener l) {
        listeners.add(l);
    }

    @Override
    public final void removeListener(IndicationListener l) {
        Iterator<IndicationListener<?>> it = listeners.iterator();
        while (it.hasNext()) {
            if (it.next() == l) {
                it.remove();
            }
        }
    }

    @Override
    public abstract DeviceAddress getLocalAddress();

    @Override
    public abstract DeviceAddress getBroadcastAddress();

    @Override
    public final <V> Future<V> request(DeviceAddress destination, ByteBuffer data, Priority prio, boolean expectingReply, IndicationListener<V> l) throws IOException {
        ProtocolControlInformation pci = new ProtocolControlInformation(data);
        IndicationFuture<V> f = new IndicationFuture<>();
        if (pci.getPduType() == ApduConstants.TYPE_CONFIRMED_REQ) {
            int invokeId = invokeIds.getId();
            
            PendingReply r = new PendingReply(invokeId, destination, data, prio, l, f);
            synchronized (pendingReplies) {
                pendingReplies.add(r);
                pendingReplies.notifyAll();
            }
            pci = pci.withInvokeId(invokeId);
            data.rewind();
            pci.write(data);
        	logger.trace("Schedulding message {} bytes to {}, expReply:{} invoke:{}",data.limit(), destination, expectingReply,
        			invokeId);
        } else {
        	logger.trace("Schedulding unconfirmed message {} bytes to {}, expReply:{}",data.limit(), destination, expectingReply);
        }
        data.rewind();
        //sendData(data, prio, expectingReply, destination);
        executeSend(data, prio, expectingReply, destination);
        return f;
    }
    
    private class IndicationFuture<V> implements Future<V> {
        
        final AtomicBoolean cancelled = new AtomicBoolean(false);
        final CountDownLatch done = new CountDownLatch(1);
        volatile V result;
        volatile Throwable t;

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            cancelled.set(true);
            return result == null;
        }

        @Override
        public boolean isCancelled() {
            return cancelled.get();
        }

        @Override
        public boolean isDone() {
            return result != null;
        }
        
        private void resolve(Object result, Throwable t) {
            this.result = (V) result;
            this.t = t;
            done.countDown();
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            done.await();
            if (t != null) {
                throw new ExecutionException(t);
            }
            return result;
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            done.await(timeout, unit);
            if (t != null) {
                throw new ExecutionException(t);
            }
            return result;
        }
        
    }

    private void executeSend(final ByteBuffer data, Priority prio, boolean expectingReply, DeviceAddress destination) {
        Runnable send = new Runnable() {
            @Override
            public void run() {
                try {
                	logger.trace("Sending out message {} bytes to {}, expReply:{}",data.limit(), destination, expectingReply);
                    sendData(data, prio, expectingReply, destination);
                } catch (IOException ex) {
                    logger.error("send failed for destination {}", destination, ex);
                }
            }
        };
        executor.execute(send);
    }

    /**
     * @param apdu a BACnet apdu, i.e. the application protocol control
     * information plus the actual service data.
     * @param prio BACnet message priority
     * @param expectingReply
     * @param destination
     * @throws java.io.IOException
     */
    protected abstract void sendData(ByteBuffer apdu, Priority prio, boolean expectingReply, DeviceAddress destination) throws IOException;

    @Override
    public final void close() throws IOException {
        timeoutThread.interrupt();
        executor.shutdown();
        doClose();
    }

    protected abstract void doClose() throws IOException;

    @Override
    public final AbstractTransport start() {
        timeoutThread.start();
        doStart();
        return this;
    }

    protected abstract void doStart();

}
