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
package hermes.runtime.faultinjection.bft;

import hermes.runtime.faultinjection.*;
import hermes.serialization.HermesSerializableHelper;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class BFTForgeDestinationIDFaultDescription extends FaultDescription {

    static final int SERIAL_ID = 52500001;   
        
    String m_dstID;
    String m_forgedID;    
    String m_signature;
    
    @Override
    public int getSerialID() {
        return SERIAL_ID;
    }

    public BFTForgeDestinationIDFaultDescription(String dstID, String forgedID,String signature) {
        m_dstID = dstID;
        m_forgedID = forgedID;
        m_signature = signature;
    }
   
    @Override
    protected void serializableFaultDescriptionImpl(ByteBuffer buf) throws Exception {
        HermesSerializableHelper.serializeString(buf, m_dstID);
        HermesSerializableHelper.serializeString(buf, m_forgedID);
        HermesSerializableHelper.serializeString(buf, m_signature);
    }

    @Override
    protected void deserializableFaultDescriptionImpl(ByteBuffer buf) throws Exception {
        m_dstID = HermesSerializableHelper.deserializeString(buf);
        m_forgedID = HermesSerializableHelper.deserializeString(buf);
        m_signature = HermesSerializableHelper.deserializeString(buf);
    }

    public String getDestinationID(){
        return m_dstID;
    }
    
    public String getForgedID(){
        return m_forgedID;
    }
}
