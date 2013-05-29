/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hermes.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class HermesRuntimeContext {

    
    
    protected Map<String, Object> m_contextMap = new HashMap<>();

    public HermesRuntimeContext() {        
    }
    
    
    public Object getObject(String key){
        return m_contextMap.get(key);
    }
    
    public void put(String key,Object value){
        m_contextMap.put(key, value);
    }
    
    public Set<String> getKeys(){
        return m_contextMap.keySet();
    }
    
    public boolean constainsKey(String key){
        return m_contextMap.containsKey(key);
    }
    
    public boolean constainsObject(Object obj){
        return m_contextMap.containsValue(obj);
    }
            
    
}
