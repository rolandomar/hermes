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

import hermes.runtime.FaultContextOLD;
import hermes.runtime.HermesFault;
import hermes.runtime.HermesRuntime;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class ThreadDelayFault extends HermesFault/* implements Runnable */{ // extends FaultAlgorithm {    

    public final static int SERIAL_ID = 0x810000;
    ThreadDelayFaultDescription m_faultDescription;

    public ThreadDelayFault() {
    }

    public ThreadDelayFault(HermesRuntime runtime,
            String faultID,
            ThreadDelayFaultDescription description, byte triggerType) {
        super(runtime, faultID, triggerType);
        m_faultDescription = description;
    }
    
     public ThreadDelayFault(
            String faultID,
            ThreadDelayFaultDescription description, byte triggerType) {
        super(null, faultID, triggerType);
        m_faultDescription = description;
    }

    public ThreadDelayFaultDescription getFaultDescription() {
        return m_faultDescription;
    }
    
    @Override    
    public int getSerialID() {
        return SERIAL_ID;
    }

    @Override
    protected void serializableImpl(ByteBuffer buf) throws Exception {
        m_faultDescription.serializable(buf);
    }

    @Override
    protected void deserializableImpl(ByteBuffer buf) throws Exception {
        int serialID = buf.getInt(buf.position());
        m_faultDescription =
                (ThreadDelayFaultDescription) FaultDescriptionFactory.
                getInstance().getFaultDescriptionImpl(serialID);
        m_faultDescription.deserializable(buf);
    }
    
    @Override
    public void executeImpl() throws Exception{
        System.out.println("ThreadDelayFaultAlgorithm before sleep of "+m_faultDescription.getDuration());
        Thread.sleep(m_faultDescription.getDuration());
        System.out.println("ThreadDelayFaultAlgorithm after sleep of "+m_faultDescription.getDuration());
    }   

}
