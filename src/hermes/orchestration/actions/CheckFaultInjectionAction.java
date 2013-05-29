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

import hermes.orchestration.actions.Action;
import hermes.runtime.FaultContextOLD;
import hermes.runtime.HermesFault;
import hermes.runtime.HermesFaultFactory;
import hermes.runtime.faultinjection.bft.FaultContext;
import hermes.serialization.HermesSerializable;
import hermes.serialization.HermesSerializableHelper;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Rolando Martins Carnegie Mellon University
 */
public class CheckFaultInjectionAction extends Action {

    public final static int SERIAL_ID = 5000004;
    //HermesFault m_fault = null;
    String m_faultID = null;
    FaultContext m_ctx = new FaultContext();
    
    
    @Override
    public int getSerialID() {
        return SERIAL_ID;
    }

    public CheckFaultInjectionAction(){         
    }
    
    public CheckFaultInjectionAction(String srcID,String faultID) {
        super(srcID, "");
        m_faultID = faultID;        
    }  
    
    public CheckFaultInjectionAction(String srcID,String faultID,FaultContext ctx) {
        super(srcID, "");
        m_faultID = faultID;
        m_ctx = ctx;
    } 
    
    public FaultContext getFaultContext(){
        return m_ctx;
    }
    
    public String getFaultID(){
        return m_faultID;
    }

    @Override
    protected void serializeAction(ByteBuffer buf) throws Exception {        
        HermesSerializableHelper.serializeString(buf, m_faultID);     
        //m_fault.serializable(buf);    
        m_ctx.serializable(buf);
    }

    @Override
    protected void deserializableAction(ByteBuffer buf) throws Exception {        
        m_faultID = HermesSerializableHelper.deserializeString(buf); 
        m_ctx.deserializable(buf);
        
//        int serialID = buf.getInt(buf.position());
//        m_fault =
//                (HermesFault) HermesFaultFactory.getInstance().
//                getAspectImpl(serialID);
//        m_fault.deserializable(buf);
    }
}
