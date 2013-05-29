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
public class InitReplyPacket extends TwoWayRequest {
    
    public static final int INITPACKETREPLY_OPCODE = 0x000006;
    
    public static int RETCODE_OK = 0;
    public static int RETCODE_NOK = -1;
    private int m_retcode;
    private String m_retValue;    

    public InitReplyPacket() {
        
    }
    
    public InitReplyPacket(HermesAbstractPacketHeader header) {
        super(header);
    }
    
    public InitReplyPacket(int requestNo,int retcode,String retValue) {
        super(requestNo);
        m_header.setOpcode(INITPACKETREPLY_OPCODE);
        m_retcode = retcode;
        m_retValue = retValue;
    }

    public String getReturnValue() {
        return m_retValue;
    }
    
    public int getRetCode(){
        return m_retcode;
    }

    @Override
    protected void serializeTwoWayRequestBody(ByteBuffer buf) {
        buf.putInt(m_retcode);
        HermesSerializableHelper.serializeString(buf, m_retValue);
    }

    @Override
    protected void deserializeTwoWayRequestBody(ByteBuffer buf) throws Exception {
        m_retcode = HermesSerializableHelper.deserializeInt(buf);
        m_retValue = HermesSerializableHelper.deserializeString(buf);
    }
}
