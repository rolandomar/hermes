/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hermes.runtime.faultinjection.bft;

import hermes.serialization.HermesSerializable;
import hermes.serialization.HermesSerializableHelper;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author rmartins
 */
public class FaultContext implements HermesSerializable{
    Map<String,String> m_ctx = new HashMap<>();
    
    public FaultContext(){        
    }
    
    public FaultContext(Map<String,String> ctx){
        m_ctx.putAll(ctx);        
    }
        
    public Long getLongValue(String key){
        String value = m_ctx.get(key);
        if(value == null){
            return new Long(-1);
        }
        return Long.parseLong(value);
    }
    
    public void putLongValue(String key,Long value){
        m_ctx.put(key,Long.toString(value));        
    }
    
    public Integer getIntValue(String key){
        String value = m_ctx.get(key);
        if(value == null){
            return new Integer(-1);
        }
        return Integer.parseInt(value);
    }
    
    public void putIntValue(String key,Integer value){
        m_ctx.put(key,Integer.toString(value));        
    }
    
    public String getString(String key){
        return m_ctx.get(key);
    }
    public void putStringValue(String key,String value){
        m_ctx.put(key,value);        
    }
    
    public boolean hasKey(String key){
        return m_ctx.containsKey(key);
    }
    
    public Long getRun(){
        if(hasKey("RUN")){
            return getLongValue("RUN");
        }
        return new Long(-1);
    }
    
    
    @Override
    public void serializable(ByteBuffer buf) throws Exception {
        if(m_ctx.isEmpty()){
            //System.out.println("FaultContext:serialize:0");
            buf.putLong(0);
            return;
        }else{
            //concurrent updates to the map by different clients,
            //we must proper do this
            Map<String,String> ctx = new HashMap<>(m_ctx);
            Set<Map.Entry<String, String>> set = ctx.entrySet();
            buf.putLong(set.size());
            //System.out.println("FaultContext:serialize:"+set.size());
            for(Map.Entry<String, String> s: set){
                //System.out.println("FaultContext:serialize: "+s.getKey()+" "+s.getValue());
                HermesSerializableHelper.serializeString(buf, s.getKey()); 
                HermesSerializableHelper.serializeString(buf, s.getValue()); 
            }
        }
    }

    @Override
    public void deserializable(ByteBuffer buf) throws Exception {
        long size = buf.getLong();
        m_ctx.clear();
        //System.out.println("FaultContext:deserialize:"+size);
        if(size == 0){
            return;
        }
        for(int i=0; i < size; i++){
            String key = HermesSerializableHelper.deserializeString(buf);
            //System.out.println("FaultContext:deserialize: k"+key);
            String value = HermesSerializableHelper.deserializeString(buf);
            //System.out.println("FaultContext:deserialize: v"+value);
            m_ctx.put(key, value);
        }
    }
    
    
    
}
