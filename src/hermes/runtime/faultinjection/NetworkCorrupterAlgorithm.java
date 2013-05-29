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
import java.nio.ByteBuffer;
import java.util.Random;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class NetworkCorrupterAlgorithm extends HermesFault{// extends FaultAlgorithm {    

    public final static int SERIAL_ID = 0x800003;
    NetworkCorrupterFaultDescription m_faultDescription;
    byte[] m_arg = null;

    /*public static void runOnce(NetworkCorrupterFaultDescription description,byte[] packetBytes) {
        NetworkCorrupterAlgorithm alg = new NetworkCorrupterAlgorithm(description);
        alg.run(packetBytes);
    }*/

    public NetworkCorrupterAlgorithm(NetworkCorrupterFaultDescription description) {
        m_faultDescription = description;
    }

    
    /*public void run(byte[] packetBytes){
        if(packetBytes == null || packetBytes.length==0){
            return;
        }
        if(filterPacket()){
            randomGenerator.nextBytes(packetBytes);
        }
    }*/
    
    protected boolean filterPacket() {
        return (getRandomPer() <= m_faultDescription.getCorruptionPercentage());
    }

    protected int getRandomPer() {
        return randomGenerator.nextInt() % 100;
    }
    protected static Random randomGenerator = null;

    static {
        randomGenerator = new Random(System.currentTimeMillis());
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
                (NetworkCorrupterFaultDescription) FaultDescriptionFactory.
                getInstance().getFaultDescriptionImpl(serialID);
        m_faultDescription.deserializable(buf);
    }

    @Override
    //public void execute(byte[] packetBytes) throws Exception{
    public void executeImpl() throws Exception{
        byte[] packetBytes = m_arg;
        if(packetBytes == null || packetBytes.length==0){
            return;
        }
        if(filterPacket()){
            randomGenerator.nextBytes(packetBytes);
        }
    }    
}
