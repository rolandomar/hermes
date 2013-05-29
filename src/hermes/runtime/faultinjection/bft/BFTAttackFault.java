/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hermes.runtime.faultinjection.bft;

import hermes.runtime.HermesFault;
import hermes.runtime.HermesRuntime;
import java.nio.ByteBuffer;

public class BFTAttackFault extends HermesFault {

    public final static int SERIAL_ID = 0x6201066;
    //FaultContext m_ctx = new FaultContext();

    public BFTAttackFault() {
        
    }

    public BFTAttackFault(HermesRuntime runtime, String faultID,
            byte triggerType) {
        super(runtime, faultID, triggerType);
        //m_ctx = fd;
    }

    public BFTAttackFault(String faultID, 
            byte triggerType) {
        super(null, faultID, triggerType);
        //m_ctx = fd;
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
        //Thread.sleep(0, (int) (m_fd.getDelay() * 1000)); //from us to ns
    }

    @Override
    public String toString() {
        String str = "BFTDelayPacketSequence:" + this.getFaultID()
                + " sync:" + this.getSynchronizedFaul();
                //+ " delay:" + m_ctx.toString();
        return str;
    }
}
