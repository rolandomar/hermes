/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hermes.runtime.bft;

import hermes.runtime.FaultContextOLD;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class BFTFaultContext extends FaultContextOLD{    
    static protected long DEFAULT_INIT = -1;
    static public String ROUND_KEY = "ROUND_KEY";
    static public String GROUP_SIZE_KEY = "GROUP_SIZE_KEY";
    static public String F_KEY = "F_KEY";    
    //TODO: Talk to Diego and improve this further
    static public String CURRENT_PACKET_KEY = "CURRENT_PACKET_KEY";
    
    public BFTFaultContext(long instanceID) {
        super(instanceID);
        m_contextMap.put(ROUND_KEY,DEFAULT_INIT); 
        m_contextMap.put(GROUP_SIZE_KEY,DEFAULT_INIT); 
        m_contextMap.put(F_KEY,1); 
        m_contextMap.put(CURRENT_PACKET_KEY,null); 
    }
    
    public void setRound(Long round) {
        m_contextMap.put(ROUND_KEY,round);        
    }
       
    public Long getRound() {
        return (Long)m_contextMap.get(ROUND_KEY);        
    }
            
    public Long incrRound(){
        Long cur = getRound() + 1;
        setRound(cur);
        return cur;
    }
    
    public void setCurrentPacket(BFTPacketInterface packet){
        m_contextMap.put(CURRENT_PACKET_KEY,packet); 
    }
    
    public BFTPacketInterface getCurrentPacket(){
        return (BFTPacketInterface)m_contextMap.get(CURRENT_PACKET_KEY); 
    }
    

}
