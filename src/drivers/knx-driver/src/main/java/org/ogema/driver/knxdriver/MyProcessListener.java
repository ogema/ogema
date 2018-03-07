/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
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
