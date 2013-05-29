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

import hermes.network.packets.HermesAbstractPacket;
import hermes.runtime.HermesFault;
import hermes.runtime.HermesRuntime;
import static hermes.runtime.faultinjection.ThreadDelayFault.SERIAL_ID;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class NetworkDropperFault extends HermesFault {

    public final static int SERIAL_ID = 0x800004;
    NetworkDropperFaultDescription m_fd;

    
    public NetworkDropperFaultDescription getDescription(){
        return m_fd;
    }
    
    
    public NetworkDropperFault() {
        
    }
        

    public NetworkDropperFault(HermesRuntime runtime, String faultID,
            NetworkDropperFaultDescription fd, byte triggerType) {
        super(runtime, faultID, triggerType);
        m_fd = fd;
    }

    public NetworkDropperFault(String faultID, NetworkDropperFaultDescription fd,
            byte triggerType) {
        super(null, faultID, triggerType);
        m_fd = fd;
    }

    @Override
    public int getSerialID() {
        return SERIAL_ID;
    }

    @Override
    protected void serializableImpl(ByteBuffer buf) throws Exception {
        m_fd.serializable(buf);
    }

    @Override
    protected void deserializableImpl(ByteBuffer buf) throws Exception {
        int serialID = buf.getInt(buf.position());
        m_fd =
                (NetworkDropperFaultDescription) FaultDescriptionFactory.
                getInstance().getFaultDescriptionImpl(serialID);
        m_fd.deserializable(buf);
    }

    @Override
    public void executeImpl() throws Exception {
        
    }

    @Override
    public String toString() {
        String str = "BFTDelayPacketSequence:" + this.getFaultID()
                + " sync:" + this.getSynchronizedFaul()
                + " percentage drop:" + m_fd.getDropPercentage();
        return str;
    }

    /**
     * True to discard packet, false otherwise
     *
     * @param packet
     * @return
     */
    public boolean filterPacket() {
        int per = getRandomPer();        
        System.out.println("filterPacket: "+per+" "+
               m_fd.getDropPercentage()+" "+(per <= m_fd.getDropPercentage()));
        
        return (per <= m_fd.getDropPercentage());
    }

    protected int getRandomPer() {
        int i = randomGenerator.nextInt() % 100;
        if(i <0){
            return i*(-1);
        }
        return i;
    }
    protected static Random randomGenerator = null;

    static {
        randomGenerator = new Random(System.currentTimeMillis());
    }
}
