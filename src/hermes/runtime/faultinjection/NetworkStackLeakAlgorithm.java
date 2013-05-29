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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class NetworkStackLeakAlgorithm {// extends FaultAlgorithm {    

    public final static int SERIAL_ID = 0x800005;
    public final static int MAX_LEAK_SIZE = 102400;
    
    NetworkStackLeakFaultDescription m_description;
    List<byte[]> m_leakList = new ArrayList<>();
    

    public static void runOnce(NetworkStackLeakFaultDescription description) {
        NetworkStackLeakAlgorithm alg = new NetworkStackLeakAlgorithm(description);
        alg.run();
        alg.close();
    }

    public NetworkStackLeakAlgorithm(NetworkStackLeakFaultDescription description) {
        m_description = description;
    }

    //Close or real leak will happen
    public void close(){
        m_leakList.clear();
    }
    
    public void run(){        
        if(runLeak()){
            int leakSize = randomGenerator.nextInt()%MAX_LEAK_SIZE;
            byte[] leak =new byte[leakSize];
            randomGenerator.nextBytes(leak);
            m_leakList.add(leak);
        }
    }
    
    protected boolean runLeak() {
        return (getRandomPer() <= m_description.getLeakPercentage());
    }

    protected int getRandomPer() {
        return randomGenerator.nextInt() % 100;
    }
    protected static Random randomGenerator = null;

    static {
        randomGenerator = new Random(System.currentTimeMillis());
    }
}
