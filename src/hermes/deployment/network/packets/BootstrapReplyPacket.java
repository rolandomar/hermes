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
public class BootstrapReplyPacket extends TwoWayRequest {

    public static final int BOOTSTRAPREPLY_OPCODE = 0x000002;
    private boolean m_retValue;

    public BootstrapReplyPacket() {
    }

    public BootstrapReplyPacket(HermesAbstractPacketHeader header) {
        super(header);
    }

    public BootstrapReplyPacket(int requestNo, boolean retValue) {
        super(requestNo);
        m_header.setOpcode(BOOTSTRAPREPLY_OPCODE);
        m_retValue = retValue;
    }
    
    public boolean getReturnValue()
    {
        return m_retValue;
    }

    @Override
    protected void serializeTwoWayRequestBody(ByteBuffer buf) {
        HermesSerializableHelper.serializeBoolean(buf, m_retValue);
    }

    @Override
    protected void deserializeTwoWayRequestBody(ByteBuffer buf) throws Exception {
        m_retValue = HermesSerializableHelper.deserializeBoolean(buf);
    }
}
