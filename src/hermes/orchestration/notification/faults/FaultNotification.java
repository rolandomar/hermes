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
package hermes.orchestration.notification.faults;

import hermes.orchestration.notification.Notification;
import hermes.serialization.HermesSerializableHelper;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins Carnegie Mellon University
 */
public class FaultNotification extends Notification {

    public final static int FAULTNOTIFICATION_SERIALID = 242386281;
    //FaultDescription m_cpuFaultDescription = null;
    String m_id = null;    

    public FaultNotification() {
    }

    public FaultNotification(String id) {
        m_id = id;        
    }
   

    @Override
    public int getSerialID() {
        return FAULTNOTIFICATION_SERIALID;
    }

    @Override
    protected void serializeNotification(ByteBuffer buf) throws Exception {
        //m_cpuFaultDescription.serializable(buf);
        HermesSerializableHelper.serializeString(buf, m_id);        
    }

    @Override
    protected void deserializableNotification(ByteBuffer buf) throws Exception {
        m_id = HermesSerializableHelper.deserializeString(buf);        
        //int serialID = buf.getInt(buf.position());
        //m_cpuFaultDescription = FaultDescriptionFactory.getInstance().getFaultDescriptionImpl(serialID);
        //m_cpuFaultDescription.deserializable(buf);
    }
    
    @Override
    public String toString(){
        return "FaultNotification@"+m_id;
    }
}
