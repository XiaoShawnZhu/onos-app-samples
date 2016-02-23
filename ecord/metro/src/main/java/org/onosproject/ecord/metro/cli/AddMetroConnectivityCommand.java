/*
 * Copyright 2016-present Open Networking Laboratory
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
package org.onosproject.ecord.metro.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.packet.VlanId;
import org.onlab.util.Bandwidth;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.ecord.metro.api.MetroConnectivityId;
import org.onosproject.ecord.metro.api.MetroPathService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

import java.util.Optional;

@Command(scope = "onos", name = "add-metro-connectivity",
        description = "Configure metro domain connectivity")
public class AddMetroConnectivityCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "ingress", description = "Ingress connect point",
            required = true, multiValued = false)
    String ingressStr = null;

    @Argument(index = 1, name = "egress", description = "Egress connect point",
            required = true, multiValued = false)
    String egressStr = null;

    @Argument(index = 2, name = "bandwidth", description = "Bandwidth",
            required = false, multiValued = false)
    String bandwidthStr = null;

    @Argument(index = 3, name = "latency", description = "Latency",
            required = true, multiValued = false)
    String latencyStr = null;


    @Override
    protected void execute() {
        MetroPathService metroPathService = get(MetroPathService.class);

        ConnectPoint ingress = readConnectPoint(ingressStr);
        ConnectPoint egress = readConnectPoint(egressStr);
        if (ingress == null || egress == null) {
            print("Invalid connect points: %s, %s", ingressStr, egressStr);
            return;
        }

        Bandwidth bandwidth = (bandwidthStr == null || bandwidthStr.isEmpty()) ? null :
                Bandwidth.bps(Long.valueOf(bandwidthStr));

        print("Trying to setup connectivity between %s and %s.", ingress, egress);
        MetroConnectivityId id = metroPathService.setupConnectivity(ingress, egress, bandwidth, null);
        if (id == null) {
            print("Failed.");
            return;
        }
        print("Metro path ID : %s", id.value());
        Optional<VlanId> vlanId = metroPathService.getVlanId(id);
        if (vlanId.isPresent()) {
            print("  -- VLAN ID %d was assigned.", vlanId.get().toShort());
        } else {
            print("  -- No VLAN ID was assigned.");
        }
    }

    private ConnectPoint readConnectPoint(String str) {
        String[] strings = str.split("/");
        if (strings.length != 2) {
            return null;
        }

        DeviceId devId = DeviceId.deviceId(strings[0]);
        PortNumber port = PortNumber.portNumber(strings[1]);

        return new ConnectPoint(devId, port);
    }

}
