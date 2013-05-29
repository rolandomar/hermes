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

import hermes.stats.StatToken;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class StatNotification extends Notification{
    
    public static final int STATNOTIFICATION_SERIALID = 822386210;
            
    StatToken m_st = new StatToken();
    public StatNotification(){
     super();   
    }
    
    public StatNotification(String srcID,StatToken st){
        super(srcID);
        m_st = st;
    }

    @Override
    protected void serializeNotification(ByteBuffer buf) throws Exception{            
        m_st.serializable(buf);
    }

    @Override
    protected void deserializableNotification(ByteBuffer buf) throws Exception {
        m_st.deserializable(buf);
    }

    @Override
    public final int getSerialID() {
        return STATNOTIFICATION_SERIALID;
    }
    
    public StatToken getStatToken(){
        return m_st;
    }
    
}
