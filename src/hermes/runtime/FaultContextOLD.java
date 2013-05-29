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
public class FaultContextOLD {

    protected long m_contextID = -1;    
    
    protected Map<String, Object> m_contextMap = new HashMap<>();
//    
//    //Global 
//    static Map<Long, FaultContext> m_contexts = new HashMap<>();
//    public synchronized static FaultContext getContext(Long contextID) {
//        FaultContext ctx = m_contexts.get(contextID);
//        if (ctx == null) {
//            ctx = createContext(contextID);            
//        }
//        return ctx;
//    }

    public FaultContextOLD(long contextID) {
        m_contextID = contextID;
    }
    
    
    public Object getObject(String key){
        return m_contextMap.get(key);
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
            
    public void setContextID(long contextID) {
        m_contextID = contextID;
    }    

    public long getContextID() {
        return m_contextID;
    }
}
