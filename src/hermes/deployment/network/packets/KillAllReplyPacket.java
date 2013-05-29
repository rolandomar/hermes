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

import hermes.network.packets.HermesAbstractPacketHeader;
import hermes.network.packets.rpc.TwoWayRequest;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class KillAllReplyPacket extends TwoWayRequest {

    public static final int SERIAL_ID = 0x000011;    

    public KillAllReplyPacket() {
    }

    public KillAllReplyPacket(HermesAbstractPacketHeader header) {
        super(header);
    }

    public KillAllReplyPacket(int requestNo) {
        super(requestNo);
        m_header.setOpcode(SERIAL_ID);        
    }
    

    @Override
    protected void serializeTwoWayRequestBody(ByteBuffer buf) throws Exception{

    }

    @Override
    protected void deserializeTwoWayRequestBody(ByteBuffer buf) throws Exception {

    }
}
