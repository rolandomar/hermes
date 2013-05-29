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
package hermes.runtime.faultinjection;

import hermes.serialization.HermesSerializableHelper;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class ThreadDelayFaultDescription extends FaultDescription {

    static final int SERIAL_ID = 133123980;    
    protected int m_duration = 0;//ms    
    
    @Override
    public int getSerialID() {
        return SERIAL_ID;
    }

    public ThreadDelayFaultDescription() {
    }

    public ThreadDelayFaultDescription(int duration) {        
        m_duration = duration;        
    }

   
    public int getDuration() {
        return m_duration;
    }       

    @Override
    protected void serializableFaultDescriptionImpl(ByteBuffer buf) throws Exception {        
        buf.putInt(m_duration);              
    }

    @Override
    protected void deserializableFaultDescriptionImpl(ByteBuffer buf) throws Exception {        
        m_duration = HermesSerializableHelper.deserializeInt(buf);        
    }
}
