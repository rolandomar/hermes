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
package hermes.orchestration.actions;

import hermes.serialization.HermesSerializable;
import hermes.serialization.HermesSerializableHelper;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class BooleanWrapperActionResult implements HermesSerializable{
    
    Boolean m_result  = null;
    
 
    public static BooleanWrapperActionResult allocate(byte[] resultBuffer) throws Exception{
        BooleanWrapperActionResult b = new BooleanWrapperActionResult();
        b.init(resultBuffer);
        return b;
    }
    
    public BooleanWrapperActionResult(){        
    }
    
    public BooleanWrapperActionResult(boolean b){
        m_result = b;
    }
    
    public void init(byte[] resultBuffer) throws Exception{        
        ByteBuffer buf = ByteBuffer.wrap(resultBuffer);
        deserializable(buf);        
    }
    
    public byte[] getBytes(){
        ByteBuffer buf = ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN);        
        try {
            serializable(buf);
        } catch (Exception ex) {
            byte b = 0;
            buf.put(0, b);
        }
        return buf.array();
    }
    
    public Boolean getValue(){
        return m_result;
    }
    
    @Override
    public void serializable(ByteBuffer buf) throws Exception {
        HermesSerializableHelper.serializeBoolean(buf, m_result);
    }

    @Override
    public void deserializable(ByteBuffer buf) throws Exception {
        m_result = HermesSerializableHelper.deserializeBoolean(buf);
    }
    
}
