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

import hermes.network.packets.HermesAbstractPacketHeader;
import hermes.network.packets.rpc.TwoWayRequest;
import hermes.orchestration.actions.Action;
import hermes.orchestration.actions.ActionFactory;
import hermes.orchestration.remote.RemoteAction;
import hermes.orchestration.remote.RemoteActionFactory;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class RemoteActionRequestPacket extends TwoWayRequest {
    public static final int SERIAL_ID = 0x032000;
    private RemoteAction m_action;

    
    public RemoteActionRequestPacket() {
        
    }
    
    public RemoteActionRequestPacket(HermesAbstractPacketHeader header) {
        super(header);
    }
    
    public RemoteActionRequestPacket(int requestNo,RemoteAction action) {
        super(requestNo);        
        m_header.setOpcode(SERIAL_ID);
        m_action = action;
    }

    public RemoteAction getRemoteAction() {
        return m_action;
    }

    @Override
    protected void serializeTwoWayRequestBody(ByteBuffer buf) throws Exception{
        m_action.serializable(buf);
    }

    @Override
    protected void deserializeTwoWayRequestBody(ByteBuffer buf) throws Exception {        
        
        int serialID = buf.getInt(buf.position());
        m_action = RemoteActionFactory.getInstance().getRemoteActionImpl(serialID);
        m_action.deserializable(buf);
    }
}
