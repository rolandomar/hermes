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
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class CPULoaderFaultAlgorithm extends HermesFault/* implements Runnable */{ // extends FaultAlgorithm {    

    public final static int SERIAL_ID = 0x800000;
    CPULoaderFaultDescription m_faultDescription;

    public CPULoaderFaultAlgorithm() {
    }

    public CPULoaderFaultAlgorithm(HermesRuntime runtime,
            String faultID,
            CPULoaderFaultDescription description, byte triggerType) {
        super(runtime,faultID,triggerType);
        m_faultDescription = description;
    }
    
    public CPULoaderFaultAlgorithm(
            String faultID,
            CPULoaderFaultDescription description,byte triggerType) {
        super(null,faultID,triggerType);
        m_faultDescription = description;
    }

    public CPULoaderFaultDescription getFaultDescription() {
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
                (CPULoaderFaultDescription) FaultDescriptionFactory.
                getInstance().getFaultDescriptionImpl(serialID);
        m_faultDescription.deserializable(buf);
    }
    
    @Override
    public void executeImpl() throws Exception{
        SimpleCPUFaultThread[] threads = new SimpleCPUFaultThread[m_faultDescription.getNoThreads()];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new SimpleCPUFaultThread(this);
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                Logger.getLogger(CPULoaderFaultAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    } 
    
    @Override
    public String toString(){
        String str = "CPULoaderFaultAlgorithm:"+this.getFaultID()+" sync="+this.getSynchronizedFaul()+" "+
                m_faultDescription.toString();
        return str;
    }

    class SimpleCPUFaultThread extends Thread {

        CPULoaderFaultAlgorithm m_alg;

        public SimpleCPUFaultThread(CPULoaderFaultAlgorithm alg) {
            m_alg = alg;
        }

        @Override
        public void run() {
            //System.out.println("SimpleCPUFaultThread");
            long start = System.currentTimeMillis();
            double ret = 102382913892L;
            long coeficient1 = 963523231L;
            long coeficient2 = 283746133L;
            long maxDuration = m_faultDescription.getDuration();
            while ((System.currentTimeMillis() - start) < m_faultDescription.getDuration()) {
                try {
                    long period = Math.round(randomGenerator.nextDouble() * maxDuration);
                    long activePeriod = Math.round((double) period * ((double) m_faultDescription.getLoad() / (double) 100));
                    maxDuration -= period;
                    long slack = period - activePeriod;
                    long startInner = System.currentTimeMillis();
                    //System.out.println("SimpleCPUFaultThread: activePeriod=" + activePeriod + " period=" + period + " slack=" + slack);
                    while ((System.currentTimeMillis() - startInner) < activePeriod) {
                        ret = ret * coeficient1 * coeficient2;
                        ret = Math.exp(ret);
                    }
                    sleep(slack);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CPULoaderFaultAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    };
    protected static Random randomGenerator = null;

    static {
        randomGenerator = new Random(System.currentTimeMillis());
        //randomGenerator.

    }
;
}
