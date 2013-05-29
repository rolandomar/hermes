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
import hermes.serialization.HermesSerializableHelper;
import hermes.network.packets.rpc.TwoWayRequest;
import hermes.orchestration.actions.ActionFactory;
import hermes.orchestration.actions.ActionResult;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class ActionReplyPacket extends TwoWayRequest {
    
    public static final int ACTIONREPLY_OPCODE = 0x002001;
    
    private ActionResult m_result;

    public ActionReplyPacket() {
        
    }
    
    public ActionReplyPacket(HermesAbstractPacketHeader header) {
        super(header);
    }
    
    public ActionReplyPacket(int requestNo,ActionResult result) {
        super(requestNo);
        m_header.setOpcode(ACTIONREPLY_OPCODE);
        m_result = result;        
    }
    
    
    public ActionResult getActionResult(){
        return m_result;
    }

    @Override
    protected void serializeTwoWayRequestBody(ByteBuffer buf) throws Exception {
        m_result.serializable(buf);
    }

    @Override
    protected void deserializeTwoWayRequestBody(ByteBuffer buf) throws Exception {
        int serialID = buf.getInt(buf.position());
        m_result = ActionFactory.getInstance().getActionResultImpl(serialID);
        m_result.deserializable(buf);
    }
}
