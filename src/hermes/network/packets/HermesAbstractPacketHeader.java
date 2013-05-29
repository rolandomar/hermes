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
package hermes.network.packets;

import hermes.serialization.HermesSerializableHelper;
import hermes.serialization.HermesSerializable;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class HermesAbstractPacketHeader implements HermesSerializable{
    public static int MAGIC = 0x00aacc;
    public static int VERSION = 1;
    public static int HEADER_SIZE = 5 * Integer.SIZE / 8;
    public static int DEFAULT_BODY_SIZE = 2048;
    
    public int m_opcode = -1;
    public int m_sequenceNo = -1;
    protected int m_bodySize = 0;
    
    
    public HermesAbstractPacketHeader(){        
    }
    
    @Override
    public void serializable(ByteBuffer buf) throws Exception {        
        buf.putInt(MAGIC);
        //System.out.println("PUT MAGIC"+buf.getInt(0));
        buf.putInt(VERSION);        
        if(m_opcode == -1){
            throw new Exception("Malformed packet: bad opcode number (-1)");
        }
        buf.putInt(m_opcode);
        if(m_sequenceNo == -1){
            throw new Exception("Malformed packet: bad sequence number (-1)");
        }
        buf.putInt(m_sequenceNo);
        buf.putInt(m_bodySize);
        //System.out.println("m_sequenceNo="+m_sequenceNo+" m_bodySize="+m_bodySize);
    }

    @Override
    public void deserializable(ByteBuffer buf) throws Exception{        
        //buf.flip();
        //System.out.println("HEader SIZE="+buf.remaining());
        if (buf.remaining() < HEADER_SIZE) {
            throw new Exception("Malformed packet: insufficient buffer for header");
        }
        if (buf.getInt() != MAGIC) {
            System.out.println(buf.getInt()+ " "+MAGIC);
            throw new Exception("Malformed packet: bad magic");
        }
        if (buf.getInt() != VERSION) {
            throw new Exception("Malformed packet: bad version");
        }
        m_opcode = HermesSerializableHelper.deserializeInt(buf);        
        m_sequenceNo = HermesSerializableHelper.deserializeInt(buf);        
        //bodySize
        m_bodySize = HermesSerializableHelper.deserializeInt(buf);
    }

    
    public int getSequenceNo(){
        return m_sequenceNo;
    }
    
    public void setSequenceNo(int seqNo){
        m_sequenceNo = seqNo;
    }
    
    public int getOpcode(){
        return m_opcode;
    }
    
    public void setOpcode(int opcode){
        m_opcode = opcode;
    }
    
    public int getBodySize(){
        return m_bodySize;
    }
    
    public void setBodySize(int bodySize){
        m_bodySize =bodySize;
    }
    
    
}
