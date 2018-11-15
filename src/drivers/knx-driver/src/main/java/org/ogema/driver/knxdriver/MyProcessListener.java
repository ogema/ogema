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

import java.util.Map;

import org.ogema.core.logging.OgemaLogger;
import org.ogema.model.sensors.MotionSensor;
import org.ogema.model.sensors.OccupancySensor;
import org.ogema.model.sensors.TouchSensor;

import tuwien.auto.calimero.DetachEvent;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.process.ProcessEvent;
import tuwien.auto.calimero.process.ProcessListener;

public class MyProcessListener implements ProcessListener {

    ConnectionInfo conn = null;
    GroupAddress main = null;

    MyProcessListener(final ConnectionInfo connInfo, final GroupAddress grpAddress,
            Map<String, KNXNetworkLinkIP> knxNetConnections, final OgemaLogger logger) {
        this.conn = connInfo;
        this.main = grpAddress;

    }

    public void detached(DetachEvent e) {
        // TODO Auto-generated
        // method stub
    }

    public void groupWrite(ProcessEvent e) {
        // TODO Auto-generated
        // method stub

        try {

            final DPTXlatorBoolean tr = new DPTXlatorBoolean(DPTXlatorBoolean.DPT_SWITCH);
            tr.setData(e.getASDU());

            if (e.getSourceAddr().toString().trim().equals(conn.getPhyaddress())
                    && e.getDestination().toString().equals(main.toString())) {

                conn.setValue(tr.getValue());

                if (tr.getValueBoolean()) {

                    writingToRessource(true);

                } else {
                    writingToRessource(false);
                }

            }

        } catch (Exception e1) {

        }

    }

    private void writingToRessource(boolean detect) {

        if (detect) {

            if (conn.getRessource().isActive()) {

                if (conn.getRessource() instanceof OccupancySensor) {

                    OccupancySensor sensor = (OccupancySensor) conn.getRessource();

                    if (!sensor.reading().getValue()) {

                        sensor.reading().setValue(true);

                    }

                }
                if (conn.getRessource() instanceof MotionSensor) {

                    MotionSensor sensor = (MotionSensor) conn.getRessource();

                    if (!sensor.reading().getValue()) {

                        sensor.reading().setValue(true);

                    }
                }

                if (conn.getRessource() instanceof TouchSensor) {

                    TouchSensor sensor = (TouchSensor) conn.getRessource();

                    if (!sensor.reading().getValue()) {

                        sensor.reading().setValue(true);

                    }
                }

            }

        }

        if (!detect) {

            if (conn.getRessource().isActive()) {

                if (conn.getRessource() instanceof OccupancySensor) {

                    OccupancySensor sensor = (OccupancySensor) conn.getRessource();

                    if (sensor.reading().getValue()) {

                        sensor.reading().setValue(false);

                    }

                }
                if (conn.getRessource() instanceof MotionSensor) {

                    MotionSensor sensor = (MotionSensor) conn.getRessource();

                    if (sensor.reading().getValue()) {

                        sensor.reading().setValue(false);

                    }
                }

                if (conn.getRessource() instanceof TouchSensor) {

                    TouchSensor sensor = (TouchSensor) conn.getRessource();

                    if (sensor.reading().getValue()) {

                        sensor.reading().setValue(false);

                    }
                }
            }
        }
    }

}
