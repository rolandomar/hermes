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

import hermes.serialization.HermesSerializable;
import hermes.serialization.HermesSerializableHelper;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class MultipleFaultDescription extends FaultDescription{
    static final int SERIAL_ID = 123123983;
            
    
    protected int m_noThreads = 0;       
    protected int m_duration = 0;//ms
    //cpu load
    protected int m_load;
    //io
    protected int m_ioBandwidth;
    //memory
    protected int m_memory;

    @Override
    public int getSerialID(){
        return SERIAL_ID;
    }
       
    
    public MultipleFaultDescription(){        
    }
    
    public MultipleFaultDescription(int noThreads, int duration,
            int load, int ioBandwidth, int memory,
            int algorithmID){
        m_noThreads= noThreads;           
        m_duration = duration;        
        m_load = load;
        m_ioBandwidth = ioBandwidth;
        m_memory = memory;        
    }
        
    
    public int getNoThreads() {
        return m_noThreads;
    }

    public int getDuration() {
        return m_duration;
    }   
    
    public int getLoad(){
        return m_load;
    }
    
    public int getIOBandwidth(){
        return m_ioBandwidth;
    }
    
    public int getMemory(){
        return m_memory;
    }
      
    @Override
    public void serializableFaultDescriptionImpl(ByteBuffer buf) throws Exception {        
        buf.putInt(m_noThreads);
        buf.putInt(m_duration);
        buf.putInt(m_load);
        buf.putInt(m_ioBandwidth);
        buf.putInt(m_memory);
    }

    @Override
    public void deserializableFaultDescriptionImpl(ByteBuffer buf) throws Exception {
        m_noThreads = HermesSerializableHelper.deserializeInt(buf);        
        m_duration = HermesSerializableHelper.deserializeInt(buf);
        m_load = HermesSerializableHelper.deserializeInt(buf);
        m_ioBandwidth = HermesSerializableHelper.deserializeInt(buf);
        m_memory = HermesSerializableHelper.deserializeInt(buf);
    }
}
