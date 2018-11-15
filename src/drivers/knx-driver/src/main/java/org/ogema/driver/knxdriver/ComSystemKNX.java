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
package org.ogema.driver.knxdriver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.naming.CommunicationException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.driver.knxdriver.KNXUtils.SearchResult;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.communication.CommunicationInformation;
import org.ogema.model.communication.DeviceAddress;
import org.ogema.model.communication.IPAddressV4;
import org.ogema.model.communication.KNXAddress;
import org.ogema.model.communication.PollingConfiguration;
import org.ogema.model.devices.buildingtechnology.ElectricDimmer;
import org.ogema.model.devices.connectiondevices.ThermalValve;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.sensors.ElectricPowerSensor;
import org.ogema.model.sensors.LightSensor;
import org.ogema.model.sensors.MotionSensor;
import org.ogema.model.sensors.OccupancySensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.sensors.TouchSensor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import tuwien.auto.calimero.CloseEvent;

import tuwien.auto.calimero.DetachEvent;
import tuwien.auto.calimero.FrameEvent;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.cemi.CEMI;
import tuwien.auto.calimero.cemi.CEMILData;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.datapoint.StateDP;
import tuwien.auto.calimero.dptxlator.DPT;
import tuwien.auto.calimero.dptxlator.DPTXlator2ByteFloat;
import tuwien.auto.calimero.dptxlator.DPTXlator4ByteUnsigned;
import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean;
import tuwien.auto.calimero.exception.KNXFormatException;
import tuwien.auto.calimero.exception.KNXIllegalArgumentException;
import tuwien.auto.calimero.knxnetip.servicetype.SearchResponse;
import tuwien.auto.calimero.link.KNXLinkClosedException;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.NetworkLinkListener;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;
import tuwien.auto.calimero.process.ProcessEvent;
import tuwien.auto.calimero.process.ProcessListener;

@Component(specVersion = "1.2", immediate = true)
@Service({Application.class, KNXdriverI.class})
public class ComSystemKNX implements KNXdriverI {

    private final int standardTimestep = 2;
    public static volatile boolean activated;
    Thread readingThread;
    Thread writingThread;
    protected OgemaLogger logger;
    private final String NAME_COM_INFO = "knxComInfo";
    public static ApplicationManager aManager;
    public static Bundle bundle;
    public static ComSystemKNX app;
    public static BundleContext bundleContext;
    private final KNXUtils knxUtils = new KNXUtils();
    private static int statusKNX;
    private final KNXStorage storage = KNXStorage.getInstance();
    Map<tuwien.auto.calimero.KNXAddress, IndicationHandler<?, ?>> indicationHandlers = new HashMap<>();

    Collection<SearchResult> knxInterfaces;
    Collection<Thread> runningConnections = new ConcurrentLinkedQueue<>();

    @Activate
    protected void activate(ComponentContext componentContext) {
        ComSystemKNX.bundle = componentContext.getBundleContext().getBundle();
        ComSystemKNX.app = this;
        bundleContext = componentContext.getBundleContext();
    }

    private void addNotAllowed() {

        storage.getNotAllowedDevices().add("0.0.255");

    }

    private void createReadWriteableComInfo(PhysicalElement device, boolean readable, boolean writeable) {
        final CommunicationInformation comInfo = device.addDecorator(NAME_COM_INFO, CommunicationInformation.class);
        final DeviceAddress comAddress = comInfo.comAddress();
        comAddress.create();
        comAddress.readable().create();
        comAddress.writeable().create();
        comAddress.readable().setValue(readable);
        comAddress.writeable().setValue(writeable);
    }

    public int addKNXDeviceToRessource(ConnectionInfo conInfo) {
        final ResourceManagement resMan = aManager.getResourceManagement();
        int status = 0; // Status
        try {
            if (conInfo.getType().equals(LightSensor.class.getSimpleName())) {
                LightSensor device = resMan.createResource(conInfo.getName(), LightSensor.class);
                device.reading().create();
                createReadWriteableComInfo(device, true, false);
                conInfo.setRessource(device);
                activateConnection(conInfo);
                logger.debug("added {}: {}", device.getResourceType().getSimpleName(), device.getPath());
            }

            if (conInfo.getType().equals(TemperatureSensor.class.getSimpleName())) {
                TemperatureSensor device = resMan.createResource(conInfo.getName(), TemperatureSensor.class);
                device.reading().create();
                createReadWriteableComInfo(device, true, false);
                conInfo.setRessource(device);
                activateConnection(conInfo);
                logger.debug("added {}: {}", device.getResourceType().getSimpleName(), device.getPath());
            }

            if (conInfo.getType().equals(MotionSensor.class.getSimpleName())) {
                MotionSensor device = resMan.createResource(conInfo.getName(), MotionSensor.class);
                device.reading().create();
                createReadWriteableComInfo(device, true, false);
                conInfo.setRessource(device);
                conInfo.setListener(true);
                activateConnection(conInfo);
                logger.debug("added {}: {}", device.getResourceType().getSimpleName(), device.getPath());
            }

            if (conInfo.getType().equals(OccupancySensor.class.getSimpleName())) {
                OccupancySensor device = resMan.createResource(conInfo.getName(), OccupancySensor.class);
                device.reading().create();
                createReadWriteableComInfo(device, true, false);
                conInfo.setRessource(device);
                conInfo.setListener(true);
                activateConnection(conInfo);
                logger.debug("added {}: {}", device.getResourceType().getSimpleName(), device.getPath());
            }

            if (conInfo.getType().equals(TouchSensor.class.getSimpleName())) {
                TouchSensor device = resMan.createResource(conInfo.getName(), TouchSensor.class);
                device.reading().create();
                createReadWriteableComInfo(device, true, false);
                conInfo.setRessource(device);
                conInfo.setListener(true);
                activateConnection(conInfo);
                logger.debug("added {}: {}", device.getResourceType().getSimpleName(), device.getPath());
            }

            if (conInfo.getType().equals(ElectricPowerSensor.class.getSimpleName())) {
                ElectricPowerSensor device = resMan.createResource(conInfo.getName(), ElectricPowerSensor.class);
                device.reading().create();
                createReadWriteableComInfo(device, true, false);
                conInfo.setRessource(device);
                activateConnection(conInfo);
                logger.debug("added {}: {}", device.getResourceType().getSimpleName(), device.getPath());
            }

            if (conInfo.getType().equals(OnOffSwitch.class.getSimpleName())) {
                OnOffSwitch device = resMan.createResource(conInfo.getName(), OnOffSwitch.class);
                device.stateControl().create();
                device.stateControl().addValueListener(createUpdateListener(conInfo), true);
                createReadWriteableComInfo(device, true, false);
                try {
                    device.stateFeedback().create();
                    indicationHandlers.put(GroupAddress.create(conInfo.getGroupAddress()),
                            IndicationHandler.createBooleanHandler(device.stateFeedback(), DPTXlatorBoolean.DPT_SWITCH));
                } catch (KNXFormatException ex) {
                    logger.error("invalid group address in {}: {}", conInfo.getRessource().getPath(), conInfo.getGroupAddress());
                }
                conInfo.setRessource(device);
                activateConnection(conInfo);
                logger.debug("added {}: {}", device.getResourceType().getSimpleName(), device.getPath());
            }

            if (conInfo.getType().equals(ElectricDimmer.class.getSimpleName())) {
                ElectricDimmer device = resMan.createResource(conInfo.getName(), ElectricDimmer.class);
                device.setting().create();
                device.setting().stateControl().create();
                device.setting().stateControl().addValueListener(createUpdateListener(conInfo), true);
                device.setting().stateFeedback().create();
                createReadWriteableComInfo(device, true, false);
                conInfo.setRessource(device);
                activateConnection(conInfo);
                logger.debug("added {}: {}", device.getResourceType().getSimpleName(), device.getPath());
            }

            if (conInfo.getType().equals(ThermalValve.class.getSimpleName())) {
                ThermalValve device = resMan.createResource(conInfo.getName(), ThermalValve.class);
                device.setting().create();
                createReadWriteableComInfo(device, true, false);
                conInfo.setRessource(device);
                activateConnection(conInfo);
                logger.debug("added {}: {}", device.getResourceType().getSimpleName(), device.getPath());
            }

        } catch (CommunicationException | ResourceException e) {
            logger.debug("exception while adding device", e);
            status = 4;
        }
        return status;
    }

    private void activateConnection(ConnectionInfo conInfo) throws CommunicationException {
        long timeInterval = conInfo.getTimeStep() * 1000;
        if (timeInterval == 0) {
            timeInterval = standardTimestep;
        }
        conInfo.setTimeStep(timeInterval);

        PhysicalElement el = (PhysicalElement) conInfo.getRessource();

        CommunicationInformation comInfo = el.addDecorator(NAME_COM_INFO, CommunicationInformation.class);
        comInfo.create();

        DeviceAddress comAddress = comInfo.comAddress();
        comAddress.create();

        PollingConfiguration cycle = comInfo.pollingConfiguration();
        cycle.create();

        TimeResource time = cycle.pollingInterval();
        time.create();

        time.setValue(timeInterval);

        IPAddressV4 ip4Address = comAddress.ipV4Address();
        ip4Address.create();

        ip4Address.address().create();
        ip4Address.port().create();

        KNXAddress knxAddress = comAddress.knxAddress();
        knxAddress.create();

        IPAddressV4 local = comAddress.addDecorator("localAddressIPV4", IPAddressV4.class);

        local.address().create();
        local.address().setValue(conInfo.getIntface());

        knxAddress.physicalAddress().create();
        knxAddress.groupAddress().create();

        knxAddress.physicalAddress().setValues(knxUtils.convertPhysicalAddress(conInfo.getPhyaddress()));
        knxAddress.groupAddress().setValues(knxUtils.convertGroupAddress(conInfo.getGroupAddress()));

        comInfo.comAddress().ipV4Address().address().setValue(knxUtils.getIPAddress(conInfo));
        comInfo.comAddress().ipV4Address().port().setValue(knxUtils.getPort(conInfo));

        // Hier lokale Adresse
        synchronized (storage.getDeviceConnections()) {
            if (conInfo.isListener()) {
                startListener(conInfo);
                conInfo.setStatusListener(true);
            }
            storage.getDeviceConnections().add(conInfo);
            conInfo.setId(storage.getDeviceConnections().size() - 1);
            conInfo.getRessource().activate(true);
            addListener(conInfo);
        }

    }

    private void startListener(final ConnectionInfo conn) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    KNXNetworkLinkIP netLinkIp = storage.getKnxNetConnections().get(conn.getKnxRouter());
                    ProcessCommunicator pc = new ProcessCommunicatorImpl(netLinkIp);
                    final GroupAddress main = new GroupAddress(conn.getGroupAddress());
                    Datapoint dp = new StateDP(main, "", 0, conn.getDptStr());
                    MyProcessListener process = new MyProcessListener(conn, main, storage.getKnxNetConnections(),
                            logger);
                    pc.addProcessListener(process);
                    pc.read(dp);
                    while (conn.isStatusListener() && activated) {
                        try {
                            Thread.sleep(1);
                            if (!netLinkIp.isOpen()) {
                                netLinkIp = storage.getKnxNetConnections().get(conn.getKnxRouter());
                                logger.info("KNX (Occupancy/Present-Detector) " + conn.getKnxRouter()
                                        + " no connection");
                                Thread.sleep(5000);
                                synchronized (netLinkIp) {
                                    if (netLinkIp.isOpen()) {
                                        pc = new ProcessCommunicatorImpl(netLinkIp);
                                        MyProcessListener process2 = new MyProcessListener(conn, main, storage
                                                .getKnxNetConnections(), logger);

                                        logger.info("KNX (Occupancy/Present-Detector) generate new listener");

                                        pc.addProcessListener(process2);
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            if (activated) {
                                logger.error(ex.getMessage());

                            }
                        }
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        t.start();

    }

    ResourceValueListener<Resource> createUpdateListener(final ConnectionInfo conn) {
        ResourceValueListener<Resource> l = new ResourceValueListener<Resource>() {
            @Override
            public void resourceChanged(Resource resource) {
                conn.setUpdateToSend(true);
            }
        };
        conn.setResListener(l);
        return l;
    }

    @SuppressWarnings("deprecation")
    private void addListener(final ConnectionInfo conn) {
        ResourceDemandListener<Resource> resDem = new ResourceDemandListener<Resource>() {
            @Override
            public void resourceAvailable(Resource resource) {
                // TODO Auto-generated method stub
            }

            @Override
            public void resourceUnavailable(Resource resource) {
                // TODO Auto-generated method stub
            }
        };

        conn.setResDemandListener(resDem);
        //conn.setResListener(resListen);

        //conn.getRessource().addResourceListener(resListen, true);
        //XXX why???
        aManager.getResourceAccess().addResourceDemand(conn.getRessource().getResourceType(), resDem);
    }

    @Override
    public boolean disconnectResource(String resourceName) {
        boolean OK = true; // Verarbeitung OK
        synchronized (storage.getDeviceConnections()) {
            Iterator<ConnectionInfo> it = storage.getDeviceConnections().iterator();
            ConnectionInfo conn;
            while (it.hasNext()) {
                try {
                    conn = it.next();
                    conn.setStatusListener(false);
                    if (conn.getName().equals(resourceName)) {
                        OK = unregisterResDemand(conn);
                        //TODO: unregister resource value listeners
                        int counter = 0;
                        for (ConnectionInfo conn2 : storage.getDeviceConnections()) {
                            if (knxUtils.getIPAddress(conn).equals(knxUtils.getIPAddress(conn2))) {
                                counter++;
                            }
                        }
                        // nur eigene Verbindung löschen
                        if (counter == 1) {
                            KNXNetworkLinkIP temp = storage.getKnxNetConnections().get(conn.getKnxRouter());
                            temp.close();
                            storage.getKnxNetConnections().remove(conn.getKnxRouter());
                        }
                        try {
                            aManager.getResourceManagement().deleteResource(conn.getRessource().getName());
                            it.remove();
                        } catch (ResourceException ex) {
                            ex.printStackTrace();
                        }
                    }
                } catch (Exception ex) {
                }
            }
        }

        return OK;
    }

    protected TimerListener timerListener = new TimerListener() {
        @Override
        public void timerElapsed(Timer timer) {
            // TODO Auto-generated method stub

            if (!readingThread.isAlive()) {
                logger.error("reading thread interrupted; start new");
                readFromSensor();
            }
            if (!writingThread.isAlive()) {
                logger.error("writing thread interrupted; start new");
                writingToActor();
            }

        }
    };

    private void readFromSensor() {

        readingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted() && activated) {
                    final long curr = System.currentTimeMillis();
                    try {
                        synchronized (storage.getDeviceConnections()) {
                            for (final ConnectionInfo conn : storage.getDeviceConnections()) {
                                if (conn.getLastAccess() + conn.getTimeStep() <= curr && conn.getTimeStep() >= 0
                                        && !conn.isListener()) {
                                    updateConnection(conn, curr);
                                    conn.setLastAccess(curr);
                                }
                            }
                        }
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            // e.printStackTrace();
                        }
                    } catch (Exception ex) {
                        logger.error("exception in reading thread", ex);
                    }
                }
                logger.debug("{} terminated", Thread.currentThread().getName());
            }
        });
        readingThread.setName("KNX reading thread");
        readingThread.start();
    }

    private void writingToActor() {
        writingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub

                while (!Thread.interrupted() && activated) {
                    final long curr = System.currentTimeMillis();
                    try {
                        synchronized (storage.getDeviceConnections()) {
                            for (final ConnectionInfo conn : storage.getDeviceConnections()) {
                                if (!conn.isSensor()) {
                                    if (conn.isUpdateToSend()) {
                                        Thread t = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                updateConnection(conn, curr);
                                                conn.setLastAccess(curr);
                                                conn.setUpdateToSend(false);
                                            }
                                        });
                                        t.start();
                                    }
                                }
                            }
                        }
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            // e.printStackTrace();
                        }
                    } catch (Exception ex) {
                        logger.error(ex.getMessage());
                    }
                }
                logger.debug("{} terminated", Thread.currentThread().getName());
            }
        });
        writingThread.setName("KNX writing thread");
        writingThread.start();

    }

    private void updateConnection(final ConnectionInfo conn, Long curTime) {
        String value; // Wert Float
        if (conn.isSensor()) // KNX-READ
        {
            if (conn.getRessource() instanceof LightSensor) {
                value = getGroupValue(conn);
                if (!value.equals("error")) {
                    LightSensor light = (LightSensor) conn.getRessource();
                    light.reading().setValue(Float.parseFloat(value.replace("lx", "")));
                }
            }
            if (conn.getRessource() instanceof ElectricPowerSensor) {
                value = getGroupValue(conn);
                if (!value.equals("error")) {
                    try {
                        int value2 = Integer.parseInt(value.replace(",", ".")
                                .replaceAll("[^0-9][^\\.][^,][^0-9].*", ""));
                        float value3 = Float.intBitsToFloat(value2);
                        if (value3 != Float.NaN && value3 != Float.NEGATIVE_INFINITY
                                && value3 != Float.POSITIVE_INFINITY) {
                            ElectricPowerSensor pow = (ElectricPowerSensor) conn.getRessource();
                            pow.reading().setValue(value3);
                        }
                    } catch (Exception ex) {
                    }
                }
            }
            if (conn.getRessource() instanceof TemperatureSensor) {
                value = getGroupValue(conn);
                if (!value.equals("error")) {
                    Float value2 = Float.parseFloat(value.replaceAll("[^0-9\\.0-9].*", ""));
                    value2 = value2 + 273.15f;
                    TemperatureSensor tmp = (TemperatureSensor) conn.getRessource();
                    tmp.reading().setValue(value2);
                }
            }
        } else {
            if (conn.isUpdateToSend()) {
                synchronized (conn) {
                    if (conn.getRessource() instanceof ElectricDimmer) {
                        // float value2 = apiService.resAdmin.getFloatValue(
                        // conn.resId, applicationId);

                        ElectricDimmer dimmer = (ElectricDimmer) conn.getRessource();
                        // rescale: OGEMA uses range [0;1], KNX uses range
                        // [0;100].
                        float value2 = dimmer.setting().stateControl().getValue() * 100.f;

                        if (value2 >= 0 && value2 <= 100) {
                            setGroupValue(conn, String.valueOf((int) value2));
                        }
                    }
                    if (conn.getRessource() instanceof OnOffSwitch) {
                        OnOffSwitch swtch = (OnOffSwitch) conn.getRessource();
                        boolean value2 = swtch.stateControl().getValue();
                        if (value2) {
                            setGroupValue(conn, "on");
                        } else {
                            setGroupValue(conn, "off");
                        }
                    }
                    if (conn.getRessource() instanceof ThermalValve) {
                        ThermalValve valve = (ThermalValve) conn.getRessource();
                        float value2 = valve.setting().stateControl().getValue() * 100.f;
                        if (value2 >= 0 && value2 <= 100) {
                            setGroupValue(conn, String.valueOf((int) value2));
                        }
                    }
                }
            }
        }
    }

    private void setGroupValue(ConnectionInfo conn, String value) {
        // TODO Auto-generated method stub
        KNXNetworkLinkIP netLinkIp = null;

        ProcessCommunicator pc = null;
        try {

            netLinkIp = storage.getKnxNetConnections().get(conn.getKnxRouter());
            if (netLinkIp == null) {
                logger.warn(" Value to " + conn.getKnxRouter() + "/" + conn.getGroupAddress()
                        + " could not be set => no connection");
                return;
            }

            synchronized (netLinkIp) {
                pc = new ProcessCommunicatorImpl(netLinkIp);
                GroupAddress main = null;
                main = new GroupAddress(conn.getGroupAddress());
                Datapoint dp = new StateDP(main, "", 0, conn.getDptStr());
                knxUtils.writeAndDetachPrivileged(pc, dp, value);
                logger.debug("wrote {}, {}", dp, value);
            }
        } catch (PrivilegedActionException | KNXFormatException | KNXLinkClosedException ex) {
            logger.info("Value to " + conn.getKnxRouter() + "/" + conn.getGroupAddress() + " could not be set => "
                    + ex.getMessage());
            if (pc != null) {
                pc.detach();
            }
        }
    }

    private String getGroupValue(final ConnectionInfo conn) {
        // TODO Auto-generated method stub

        KNXNetworkLinkIP netLinkIp = null;
        try {

            netLinkIp = storage.getKnxNetConnections().get(conn.getKnxRouter());
            synchronized (netLinkIp) {
                if (!netLinkIp.isOpen()) {

                    logger.info("Value from " + conn.getKnxRouter() + "/" + conn.getGroupAddress()
                            + " could not be read => no connection");

                    return "error";
                }
                final ProcessCommunicator pc = new ProcessCommunicatorImpl(netLinkIp);
                final GroupAddress main = new GroupAddress(conn.getGroupAddress());
                Datapoint dp = new StateDP(main, "", 0, conn.getDptStr());
                if (conn.getDptStr().equals("9.004")) {
                    pc.addProcessListener(new ProcessListener() {
                        @Override
                        public void detached(DetachEvent e) {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void groupWrite(ProcessEvent e) {
                            try {
                                if (e.getSourceAddr().toString().trim().equals(conn.getPhyaddress())
                                        && e.getDestination().toString().equals(main.toString())) {

                                    DPT tmp = new DPT("9.004", "lux", "0", "670760");
                                    DPTXlator2ByteFloat tr = new DPTXlator2ByteFloat(tmp);

                                    tr.setData(e.getASDU());
                                    conn.setValue(tr.getValue());
                                }
                            } catch (Exception e1) {
                                logger.debug("groupWrite failed", e1);
                                pc.detach();
                            }
                        }
                    });
                    try {
                        knxUtils.readAndDetachPrivileged(pc, dp);
                    } catch (Exception ex) {
                        pc.detach();
                        logger.info(conn.getGroupAddress() + " " + ex.getMessage());
                    }
                }
                if (conn.getDptStr().equals("9.001")) {
                    pc.addProcessListener(new ProcessListener() {
                        @Override
                        public void detached(DetachEvent e) {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void groupWrite(ProcessEvent e) {
                            try {
                                if (e.getSourceAddr().toString().trim().equals(conn.getPhyaddress())
                                        && e.getDestination().toString().equals(main.toString())) {
                                    DPTXlator2ByteFloat tr = new DPTXlator2ByteFloat(
                                            DPTXlator2ByteFloat.DPT_TEMPERATURE);
                                    tr.setData(e.getASDU());
                                    conn.setValue(tr.getValue());
                                }
                            } catch (Exception e1) {
                                logger.debug("groupWrite failed", e1);
                                pc.detach();
                            }
                        }
                    });
                    try {
                        pc.read(dp);
                        pc.detach();
                    } catch (Exception ex) {
                        pc.detach();
                        logger.info(conn.getGroupAddress() + " " + ex.getMessage());
                    }
                }

                if (conn.getDptStr().equals("12.001")) {
                    pc.addProcessListener(new ProcessListener() {
                        @Override
                        public void detached(DetachEvent e) {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void groupWrite(ProcessEvent e) {
                            try {
                                if (e.getSourceAddr().toString().trim().equals(conn.getPhyaddress())
                                        && e.getDestination().toString().equals(main.toString())) {
                                    DPT tmp = DPTXlator4ByteUnsigned.DPT_VALUE_4_UCOUNT;
                                    DPTXlator4ByteUnsigned tr = new DPTXlator4ByteUnsigned(tmp);
                                    tr.setData(e.getASDU());
                                    conn.setValue(tr.getValue());
                                }
                            } catch (Exception e1) {
                                logger.debug("groupWrite failed", e1);
                                pc.detach();
                            }
                        }
                    });
                    try {
                        knxUtils.readAndDetachPrivileged(pc, dp);
                    } catch (Exception ex) {
                        pc.detach();
                        logger.info(conn.getGroupAddress() + " " + ex.getMessage());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return conn.getValue();
    }

    private boolean unregisterResDemand(ConnectionInfo conn) {

        if (conn.getResDemandListener() != null) {

            aManager.getResourceAccess().removeResourceDemand(conn.getRessource().getResourceType(),
                    conn.getResDemandListener());

            return true;
        }
        return false;
    }

    @Override
    public int addConnection(final String connectionString) {
        logger.debug("adding connection: {}", connectionString);

        final ConnectionInfo conInfo = knxUtils.createConnectionInfo(connectionString);

        createConnectionToGW(conInfo);

        if (conInfo.isSensor()) {

            statusKNX = searchSensor(conInfo);

        } else {

            conInfo.setDptStr(knxUtils.getDpt(conInfo));

            statusKNX = addKNXDeviceToRessource(conInfo);
        }

        return statusKNX;

    }

    private InetAddress getLocalAddressForKnxInterface(InetAddress knxInterface) {
        for (SearchResult res : knxInterfaces) {
            for (SearchResponse r : res.responses) {
                if (r.getControlEndpoint().getAddress().equals(knxInterface)) {
                    return res.address;
                }
            }
        }
        return null;
    }

    private void createConnectionToGW(final ConnectionInfo conInfo) {
        if (!storage.getKnxNetConnections().containsKey(conInfo.getKnxRouter())) {
            logger.info("creating gateway connection to {}", conInfo.getKnxRouter());
            try {
                final String address = knxUtils.getIPAddress(conInfo);
                InetAddress remoteAddress = InetAddress.getByName(address);

                final int port = knxUtils.getPort(conInfo);

                InetAddress localAddress = getLocalAddressForKnxInterface(remoteAddress);
                if (localAddress != null) {
                    conInfo.setIntface(localAddress.getHostAddress());
                }

                final KNXNetworkLinkIP netLinkIp = knxUtils.getNetLinkIpPrivileged(conInfo.getIntface(), address, port);
                storage.getKnxNetConnections().put(address + ":" + port, netLinkIp);

                netLinkIp.addLinkListener(createIndicationListener());

                GatewayConnector connector = new GatewayConnector(netLinkIp, conInfo, port, address, logger);
                Thread t1 = new Thread(connector);
                t1.start();
                runningConnections.add(t1);
            } catch (PrivilegedActionException | UnknownHostException e) {
                logger.error("could not create gateway connection", e);
            }
        }
    }
    
    private NetworkLinkListener createIndicationListener() {
        return new NetworkLinkListener() {
            @Override
            public void confirmation(FrameEvent e) {
                logger.debug("confirmation {}", e);
            }

            @Override
            public void indication(FrameEvent e) {
                logger.trace("indication {}", e);
                CEMI cemi = e.getFrame();
                if (cemi instanceof CEMILData) {
                    CEMILData ldata = (CEMILData) cemi;
                    IndicationHandler<?, ?> handler = indicationHandlers.get(ldata.getDestination());
                    if (handler != null) {
                        logger.debug("calling inidcation handler for {}", ldata.getDestination());
                        handler.indication(ldata);
                    } else {
                        logger.trace("no handler for indication {}", ldata);
                    }
                }

            }

            @Override
            public void linkClosed(CloseEvent e) {
                logger.debug("linkClosed {}", e);
            }
        };
    }

    private int searchSensor(final ConnectionInfo conInfo) {
        KNXNetworkLinkIP netLinkIp;
        try {
            final List<ConnectionInfo> foundDevice = new ArrayList<>();
            String dptStr = knxUtils.getDpt(conInfo);
            logger.debug("searching for {}", dptStr);
            if (dptStr != null) {
                conInfo.setDptStr(dptStr);
                netLinkIp = storage.getKnxNetConnections().get(conInfo.getKnxRouter());
                if (netLinkIp != null) {
                    final ProcessCommunicator pc = new ProcessCommunicatorImpl(netLinkIp);
                    final GroupAddress main = new GroupAddress(conInfo.getGroupAddress());
                    final Datapoint dp = new StateDP(main, "", 0, dptStr);
                    pc.addProcessListener(new ProcessListener() {
                        @Override
                        public void detached(DetachEvent e) {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void groupWrite(ProcessEvent e) {
                            // TODO Auto-generated method stub
                            if (e.getDestination().toString().equals(main.toString())
                                    && !storage.getNotAllowedDevices().contains((e.getSourceAddr().toString()))
                                    && e.getSourceAddr().toString().equals(conInfo.getPhyaddress())) {
                                synchronized (foundDevice) {
                                    foundDevice.add(conInfo);
                                    foundDevice.notifyAll();
                                }
                            }
                        }
                    });
                    logger.debug("reading {}", dp);
                    knxUtils.readAndDetachPrivileged(pc, dp);

                    long start = System.currentTimeMillis();
                    int timeout = 7000;
                    while (foundDevice.isEmpty() && (start + timeout > System.currentTimeMillis())) {
                        synchronized (foundDevice) {
                            foundDevice.wait(timeout + timeout - System.currentTimeMillis());
                        }
                    }
                    if (!foundDevice.isEmpty()) {
                        logger.debug("adding device for {}", dptStr);
                        return addKNXDeviceToRessource(foundDevice.get(0));
                    } else {
                        logger.debug("{} not found", dptStr);
                        return 2;
                    }
                } else {
                    logger.debug("no connection for {}", conInfo.getKnxRouter());
                    return 1;
                }
            } else {
                logger.debug("missing DPT string");
                return 3;
            }
        } catch (KNXIllegalArgumentException ex) {
            logger.debug("", ex);
            return 3;
        } catch (PrivilegedActionException ex) {
            logger.debug("", ex.getCause());
            return 2;
        } catch (InterruptedException | KNXFormatException | KNXLinkClosedException ex) {
            logger.debug("", ex);
            return 1;
        }
    }

    @Override
    public List<ConnectionInfo> getConnectionSorted() {

        return storage.getDeviceConnections();

    }

    @Override
    public void start(ApplicationManager appManager) {
        logger = appManager.getLogger();

        knxInterfaces = new KNXUtils().searchKnxInterfaces();

        aManager = appManager;

        final List<KNXAddress> resourcesKNX = appManager.getResourceAccess().getResources(KNXAddress.class);

        addNotAllowed();

        activated = true;

        // Information aus persistenten Speicher besorgen nach HT mappen
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> tmp2 = new ArrayList<>();
                Iterator<KNXAddress> en = resourcesKNX.iterator();
                while (en != null && en.hasNext()) {
                    try {
                        KNXAddress knxAddress = en.next();
                        PhysicalElement top = (PhysicalElement) knxAddress.getParent().getParent().getParent();
                        final CommunicationInformation comInfo = top.addDecorator(NAME_COM_INFO,
                                CommunicationInformation.class);
                        IPAddressV4 local = comInfo.comAddress().getSubResource("localAddressIPV4");
                        String localStr = local.address().getValue();
                        final IPAddressV4 ip = comInfo.comAddress().ipV4Address();
                        final PollingConfiguration comCycle = comInfo.pollingConfiguration();
                        String url = top.getResourceType().getSimpleName() + "," + top.getName() + ","
                                + (ip.address().getValue() + ":" + ip.port().getValue()) + ","
                                + knxAddress.groupAddress().getValues()[0] + "/"
                                + knxAddress.groupAddress().getValues()[1] + "/"
                                + knxAddress.groupAddress().getValues()[2] + ","
                                + knxAddress.physicalAddress().getValues()[0] + "."
                                + knxAddress.physicalAddress().getValues()[1] + "."
                                + knxAddress.physicalAddress().getValues()[2] + ","
                                + ((int) (comCycle.pollingInterval().getValue() / 1000)) + "," + localStr;
                        int status = addConnection(url);
                        logger.info("KNX add device to Ogema: " + url);
                        if (status != 0) {
                            logger.info("No response from: " + url + " try again next round");
                            tmp2.add(url);
                        } else {
                            logger.info("sucess add " + url + " to Ogema");
                        }
                    } catch (Exception ex) {
                    }
                }
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                logger.info("next round");
                Iterator<String> it = tmp2.iterator();
                while (it.hasNext()) {
                    String next = it.next();
                    int status = addConnection(next);
                    logger.info("KNX add device: " + next);
                    if (status != 0) {
                        logger.info("KNX could not find device: " + next);
                    } else {
                        logger.info("successfull add: " + next);
                    }
                }

            }
        });
        t.start();
        appManager.createTimer(3000, timerListener);
        readFromSensor();
        writingToActor();
    }

    @Override
    public void stop(AppStopReason reason) {
        activated = false;
        Iterator<KNXNetworkLinkIP> it = storage.getKnxNetConnections().values().iterator();
        while (it.hasNext()) {
            KNXNetworkLinkIP el = it.next();
            el.close();
        }
        for (Thread t: runningConnections) {
            t.interrupt();
        }
        readingThread.interrupt();
        writingThread.interrupt();
        Iterator<ConnectionInfo> it2 = storage.getDeviceConnections().iterator();
        ConnectionInfo conn = null;
        while (it.hasNext()) {
            conn = it2.next();
            if (!unregisterResDemand(conn)) {
                logger.error("Error for unregister " + "ressource: " + conn.getRessource().getName());
                it2.remove();
            }
        }
    }

    public Map<String, String> getInterfaces() {
        return Collections.unmodifiableMap(storage.getAllInterface());
    }

    @Override
    public void searchInterface() {
        try {
            storage.getAllInterface().putAll(knxUtils.searchInterface());
        } catch (PrivilegedActionException e) {
            logger.error("Error while searching for interfaces!", e);
        }
    }

}
