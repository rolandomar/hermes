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
import hermes.network.packets.HermesAbstractPacketHeader;
import hermes.serialization.HermesSerializableHelper;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public abstract class TwoWayRequest extends HermesAbstractPacket {
    //public static int TWOWAYREQUEST_OPCODE = 0x000001;
    int m_requestNo;
    
    protected TwoWayRequest(){        
        super();
    }
    
    protected TwoWayRequest(HermesAbstractPacketHeader header){
        super(header);
    }        
    
    public TwoWayRequest(int requestNo){
        super();
        m_requestNo = requestNo;
        //m_header.setOpcode(TWOWAYREQUEST_OPCODE);
    }
    
    public int getRequestNo(){
        return m_requestNo;
    }
    
    public void setRequestNo(int requestNo){
        m_requestNo = requestNo;
    }

    protected abstract void serializeTwoWayRequestBody(ByteBuffer buf) throws Exception;

    protected abstract void deserializeTwoWayRequestBody(ByteBuffer buf) throws Exception;

    @Override
    protected void serializeBody(ByteBuffer buf) throws Exception{
        buf.putInt(m_requestNo);
        serializeTwoWayRequestBody(buf);
    }

    @Override
    public void deserializeBody(ByteBuffer buf) throws Exception {
        HermesSerializableHelper.checkBufferSize(buf,Integer.SIZE/8);
        m_requestNo = buf.getInt();
        deserializeTwoWayRequestBody(buf);
    }
}
