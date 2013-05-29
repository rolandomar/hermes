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
public class CrashFault extends HermesFault {// extends FaultAlgorithm {    

    public final static int SERIAL_ID = 0x800001;

    public CrashFault() {
    }

    public CrashFault(String faultID, boolean synch) {
        super(HermesFault.TRIGGER_ONCE);
    }

    public CrashFault(HermesRuntime runtime, String faultID) {
        super(runtime, faultID,HermesFault.TRIGGER_ONCE);
    }

    public CrashFault(String faultID) {
        super(null, faultID,HermesFault.TRIGGER_ONCE);
    }

    @Override
    public int getSerialID() {
        return SERIAL_ID;
    }

    @Override
    protected void serializableImpl(ByteBuffer buf) throws Exception {
    }

    @Override
    protected void deserializableImpl(ByteBuffer buf) throws Exception {
    }

    @Override
    public void executeImpl() throws Exception {
        System.out.println("CrashFaultAlgorithm: crashing node!");
        System.exit(0);
    }   

    public String toString() {
        String str = "CrashFaultAlgorithm:" + this.getFaultID() + " sync=" + this.getSynchronizedFaul();
        return str;

    }
}
