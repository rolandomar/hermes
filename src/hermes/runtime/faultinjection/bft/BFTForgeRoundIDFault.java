/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hermes.runtime.faultinjection.bft;

import hermes.runtime.FaultContextOLD;
import hermes.runtime.HermesFault;
import hermes.runtime.HermesRuntime;
import java.nio.ByteBuffer;


public class BFTForgeRoundIDFault extends HermesFault {
    public final static int SERIAL_ID = 0x1201004;

    public BFTForgeRoundIDFault() {
    }

    public BFTForgeRoundIDFault(HermesRuntime runtime, String faultID,byte triggerType) {
        super(runtime, faultID,triggerType);
    }

    public BFTForgeRoundIDFault(String faultID,byte triggerType) {
        super(null,faultID,triggerType);
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
        int serialID = buf.getInt(buf.position());        
    }
    
    @Override
    public void executeImpl() throws Exception{
    }

    @Override
    public String toString(){
        String str = "BFTForgeRoundIDFault:"+this.getFaultID()+" sync="+this.getSynchronizedFaul();    
        return str;
    }
    
}
