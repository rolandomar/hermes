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
import hermes.serialization.HermesSerializableHelper;
import hermes.network.packets.rpc.TwoWayRequest;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class InitPacket extends TwoWayRequest {
    public static final int INITPACKET_OPCODE = 0x000005;
    private String m_ID = null;

    
    public InitPacket() {
        super();
    }
    
    public InitPacket(HermesAbstractPacketHeader header) {
        super(header);
    }
    
    public InitPacket(int requestID,String ID) {
        
        m_header.setOpcode(INITPACKET_OPCODE);
        m_ID = ID;
    }

    public String getID() {
        return m_ID;
    }
    
    @Override
    protected void serializeTwoWayRequestBody(ByteBuffer buf) throws Exception{
        HermesSerializableHelper.serializeString(buf, m_ID);
    }

    @Override
    protected void deserializeTwoWayRequestBody(ByteBuffer buf) throws Exception {
        m_ID = HermesSerializableHelper.deserializeString(buf);
        System.out.println("deserializeTwoWayRequestBody=" +m_ID);
    }
}
