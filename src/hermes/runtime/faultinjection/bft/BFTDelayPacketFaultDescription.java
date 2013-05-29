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
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class BFTDelayPacketFaultDescription extends FaultDescription {

    public static final int SERIAL_ID = 52511083;   
    
    long m_usDelay = 0;//delay in microseconds
    int m_type = 0;//delay in microseconds
    public final static int ALL = 1;
    public final static int MOD_SEQ = 2;
    public final static int ALL_PREPARE_MESSGAGES = 3;
    public final static int MOD_PREPARE_MESSGAGES = 4;
    public final static int ALL_WEAK_MESSGAGES = 5;
    public final static int MOD_WEAK_MESSGAGES = 6;
    public final static int ALL_STRONG_MESSGAGES = 7;
    public final static int MOD_STRONG_MESSGAGES = 8;
    public final static int ALL_DECIDE_MESSGAGES = 9;
    public final static int MOD_DECIDE_MESSGAGES = 10;
    public final static int ALL_LC_MESSGAGES = 11;
    public final static int MOD_LC_MESSGAGES = 12;
    public final static int ALL_STT_MESSGAGES = 13;
    public final static int MOD_STT_MESSGAGES = 14;
    
    
    
    @Override
    public int getSerialID() {
        return SERIAL_ID;
    }

    public BFTDelayPacketFaultDescription(){}
    
    public BFTDelayPacketFaultDescription(int type,long usDelay) {
        m_usDelay = usDelay;
        m_type = type;
    }
   
    @Override
    protected void serializableFaultDescriptionImpl(ByteBuffer buf) throws Exception {
        buf.putLong(m_usDelay);
        buf.putInt(m_type);
    }

    @Override
    protected void deserializableFaultDescriptionImpl(ByteBuffer buf) throws Exception {
        m_usDelay=buf.getLong();
        m_type=buf.getInt();
    }

    public long getDelay() {
        return m_usDelay;
    }
    
    public int getType(){
        return m_type;
    }
}
