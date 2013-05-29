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
package hermes.orchestration.remote;

import hermes.orchestration.actions.*;
import hermes.serialization.HermesSerializable;
import hermes.serialization.HermesSerializableHelper;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class IntegerWrapperActionResult implements HermesSerializable{
    
    Integer m_result  = null;    
    
 
    public static IntegerWrapperActionResult allocate(byte[] resultBuffer) throws Exception{
        IntegerWrapperActionResult b = new IntegerWrapperActionResult();
        b.init(resultBuffer);
        return b;
    }
    
    public IntegerWrapperActionResult(){        
    }
    
    public IntegerWrapperActionResult(Integer b){
        m_result = b;
    }
    
    public void init(byte[] resultBuffer) throws Exception{        
        ByteBuffer buf = ByteBuffer.wrap(resultBuffer);
        deserializable(buf);        
    }
    
    public byte[] getBytes(){
        ByteBuffer buf = ByteBuffer.allocate(Integer.SIZE).order(ByteOrder.LITTLE_ENDIAN);        
        try {
            serializable(buf);
        } catch (Exception ex) {
            byte b = 0;
            buf.put(0, b);
        }
        return buf.array();
    }
    
    public Integer getValue(){
        return m_result;
    }
    
    @Override
    public void serializable(ByteBuffer buf) throws Exception {
        buf.putInt(m_result);        
    }

    @Override
    public void deserializable(ByteBuffer buf) throws Exception {
        m_result = buf.getInt();
    }
    
}
