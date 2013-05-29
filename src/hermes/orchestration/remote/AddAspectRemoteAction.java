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

import hermes.runtime.HermesFault;
import hermes.runtime.HermesFaultFactory;
import java.nio.ByteBuffer;

/**
 * 
 * @author Rolando Martins
 * Carnegie Mellon University
 */
public class AddAspectRemoteAction extends RemoteAction{
    public static final int SERIAL_ID = 6000010;
    //public String m_aspectID = null;
    protected HermesFault m_fault = null;
    
    @Override
    public final int getSerialID(){
        return SERIAL_ID;
    }
        
    public AddAspectRemoteAction(){}
        
    public AddAspectRemoteAction(HermesFault fault){
        //super(srcID,"get_aspect");    
        //m_aspectID = aspectID;
        m_fault = fault;
    }

    public HermesFault getFault(){
        return  m_fault;
    }
    
    @Override
    protected void serializeAction(ByteBuffer buf) throws Exception{
        m_fault.serializable(buf);
        //HermesSerializableHelper.serializeString(buf, m_aspectID);
    }
    @Override
    protected void deserializableAction(ByteBuffer buf) throws Exception{                
        int serialID = buf.getInt(buf.position());
        m_fault = HermesFaultFactory.
                getInstance().getAspectImpl(serialID);
        m_fault.deserializable(buf);
    }
}
