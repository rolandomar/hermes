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
package hermes.network.packets.rpc;

import hermes.network.packets.HermesAbstractPacket;
import hermes.serialization.HermesSerializableHelper;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public abstract class TwoWayReply extends HermesAbstractPacket {

    int m_replyNo;
    
    protected TwoWayReply(){
        super();
    }
    
    public TwoWayReply(int replyNo){
        super();
        m_replyNo = replyNo;
    }
    
    public int getReplyNo(){
        return m_replyNo;
    }
    
    public void setReplyNo(int replyNo){
        m_replyNo = replyNo;
    }

    protected abstract void serializeTwoWayReplyBody(ByteBuffer buf);

    protected abstract void deserializeTwoWayReplyBody(ByteBuffer buf) throws Exception;

    @Override
    protected void serializeBody(ByteBuffer buf) {
        buf.putInt(m_replyNo);
        serializeTwoWayReplyBody(buf);
    }

    @Override
    public void deserializeBody(ByteBuffer buf) throws Exception {        
        m_replyNo = HermesSerializableHelper.deserializeInt(buf);
        deserializeTwoWayReplyBody(buf);
    }
}
