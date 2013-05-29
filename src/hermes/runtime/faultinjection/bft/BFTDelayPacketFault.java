/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hermes.runtime.faultinjection.bft;

import hermes.runtime.HermesFault;
import hermes.runtime.HermesRuntime;
import hermes.runtime.faultinjection.FaultDescriptionFactory;
import java.nio.ByteBuffer;

public class BFTDelayPacketFault extends HermesFault {

    public final static int SERIAL_ID = 0x1201000;
    BFTDelayPacketFaultDescription m_fd;

    public BFTDelayPacketFaultDescription getDescription(){
        return m_fd;
    }
    
    
    public BFTDelayPacketFault() {
        
    }
        

    public BFTDelayPacketFault(HermesRuntime runtime, String faultID,
            BFTDelayPacketFaultDescription fd, byte triggerType) {
        super(runtime, faultID, triggerType);
        m_fd = fd;
    }

    public BFTDelayPacketFault(String faultID, BFTDelayPacketFaultDescription fd,
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
                (BFTDelayPacketFaultDescription) FaultDescriptionFactory.
                getInstance().getFaultDescriptionImpl(serialID);
        m_fd.deserializable(buf);
    }

    @Override
    public void executeImpl() throws Exception {
        //Thread.sleep(0, (int) (m_fd.getDelay() * 1000));
        System.out.println("BFTDelayPacketFault:before faullt");
        Thread.sleep(m_fd.getDelay() * 1000);
        System.out.println("BFTDelayPacketFault:after faullt");
    }

    @Override
    public String toString() {
        String str = "BFTDelayPacketSequence:" + this.getFaultID()
                + " sync:" + this.getSynchronizedFaul()
                + " delay:" + m_fd.getDelay();
        return str;
    }
}
