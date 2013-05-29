/*
 *  Copyright 2012 Carnegie Mellon University  
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *   
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 */
package hermes.orchestration.network.packets;

import hermes.network.packets.PacketEnumeration;
import hermes.deployment.network.packets.InitPacket;
import hermes.deployment.network.packets.InitReplyPacket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */

public class OrchestrationPackets {

    public static List<PacketEnumeration> HermesOrchestrationPackets = new ArrayList<>(Arrays.asList(
            new PacketEnumeration(InitPacket.INITPACKET_OPCODE, InitPacket.class),
            new PacketEnumeration(InitReplyPacket.INITPACKETREPLY_OPCODE, InitReplyPacket.class),
            new PacketEnumeration(NotificationRequestPacket.NOTIFICATIONREQUESTY_OPCODE,NotificationRequestPacket.class),
            new PacketEnumeration(NotificationReplyPacket.NOTIFICATIONREPLY_OPCODE,NotificationReplyPacket.class),
            new PacketEnumeration(ActionRequestPacket.ACTIONREQUESTY_OPCODE,ActionRequestPacket.class),
            new PacketEnumeration(ActionReplyPacket.ACTIONREPLY_OPCODE,ActionReplyPacket.class),
            new PacketEnumeration(RemoteActionRequestPacket.SERIAL_ID,RemoteActionRequestPacket.class),
            new PacketEnumeration(RemoteActionReplyPacket.SERIAL_ID,RemoteActionReplyPacket.class) 
            ));
}
