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
package hermes.deployment.network.packets;

import hermes.network.packets.PacketEnumeration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class HermesNodePackets {

    public static List<PacketEnumeration> HermesNodePackets = new ArrayList<>(Arrays.asList(
            new PacketEnumeration(InitPacket.INITPACKET_OPCODE, InitPacket.class),
            new PacketEnumeration(InitReplyPacket.INITPACKETREPLY_OPCODE, InitReplyPacket.class),
            new PacketEnumeration(BootstrapRequestPacket.BOOTSTRAPREQUESTY_OPCODE, BootstrapRequestPacket.class),
            new PacketEnumeration(BootstrapReplyPacket.BOOTSTRAPREPLY_OPCODE, BootstrapReplyPacket.class),
            new PacketEnumeration(KillAllRequestPacket.SERIAL_ID, KillAllRequestPacket.class),
            new PacketEnumeration(KillAllReplyPacket.SERIAL_ID, KillAllReplyPacket.class)
            ));
}
