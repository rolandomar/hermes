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
package hermes.orchestration.notification;

import hermes.serialization.HermesSerializable;
import hermes.serialization.HermesSerializableHelper;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public abstract class Notification implements HermesSerializable {
    protected String m_srcID;
    //protected String m_notification;
    
    public abstract int getSerialID();
    
    public String getSourceID(){
        return m_srcID;
    }
    
    public Notification(){}
    
    //public String getNotification(){
    //    return m_notification;
    //}
    
    public Notification(String srcID){//,String notification){
        m_srcID = srcID;
        //m_notification = notification;        
    }

    protected abstract void serializeNotification(ByteBuffer buf) throws Exception;
    protected abstract void deserializableNotification(ByteBuffer buf) throws Exception;
    
    @Override
    public void serializable(ByteBuffer buf) throws Exception{
            buf.putInt(getSerialID());
            HermesSerializableHelper.serializeString(buf, m_srcID);
            //HermesSerializableHelper.serializeString(buf, m_notification);
            serializeNotification(buf);
    }

    @Override
    public void deserializable(ByteBuffer buf) throws Exception {
            int serial = HermesSerializableHelper.deserializeInt(buf);
            if(serial != getSerialID()){
                throw new Exception("Notification.deserializable: wrong serial");
            }
            m_srcID = HermesSerializableHelper.deserializeString(buf);
            //m_notification = HermesSerializableHelper.deserializeString(buf);
            deserializableNotification(buf);
    }

    
}
