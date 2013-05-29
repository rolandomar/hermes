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

import hermes.serialization.HermesSerializableHelper;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class SetStateNotification extends Notification {

    public static final int SETSTATENOTIFICATION_SERIALID = 20000001;
    String m_stateID;
    int m_mutationNo;
    String m_signature;//a0483ab869c2ef065e3a1153c8831882

    
    
    public SetStateNotification(){
        super();
    }
    
    public SetStateNotification(String srcID,String stateID, String signature,int mutationNo) {
        super(srcID);
        m_stateID = stateID;
        m_signature = signature;
        m_mutationNo = mutationNo;
    }
    
    public String getStateID() {
        return m_stateID;
    }

    public int getMutationNo() {
        return m_mutationNo;
    }

    @Override
    protected void serializeNotification(ByteBuffer buf) {
        HermesSerializableHelper.serializeString(buf, m_stateID);
        HermesSerializableHelper.serializeString(buf, m_signature);
        buf.putInt(m_mutationNo);
    }

    @Override
    protected void deserializableNotification(ByteBuffer buf) throws Exception {
        m_stateID = HermesSerializableHelper.deserializeString(buf);
        m_signature = HermesSerializableHelper.deserializeString(buf);
        m_mutationNo = buf.getInt();
    }

    @Override
    public final int getSerialID() {
        return SETSTATENOTIFICATION_SERIALID;
    }
    
     @Override
    public String toString(){
        return "SetStateNotification ("+m_srcID+","+m_stateID+","+m_signature+","+m_mutationNo+")";
    }
}
