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
public class NetworkStackLeakFaultDescription extends FaultDescription {

    static final int SERIAL_ID = 123123985;   
    protected int m_corruptPer;
    
    @Override
    public int getSerialID() {
        return SERIAL_ID;
    }

    public NetworkStackLeakFaultDescription() {
    }

    public NetworkStackLeakFaultDescription(
            int leakPer) {
        m_corruptPer = leakPer;
    }

    public int getLeakPercentage() {
        return m_corruptPer;
    }

    @Override
    protected void serializableFaultDescriptionImpl(ByteBuffer buf) throws Exception {
        buf.putInt(m_corruptPer);        
    }

    @Override
    protected void deserializableFaultDescriptionImpl(ByteBuffer buf) throws Exception {
        m_corruptPer = HermesSerializableHelper.deserializeInt(buf);
    }
}
