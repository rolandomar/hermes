/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hermes.orchestration.remote;

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
