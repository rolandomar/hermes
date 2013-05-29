/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hermes.runtime.faultinjection.bft;

import hermes.runtime.FaultContextOLD;
import hermes.runtime.HermesFault;
import hermes.runtime.HermesRuntime;
import java.nio.ByteBuffer;

public class BFTForgePayloadFault extends HermesFault {

    public final static int SERIAL_ID = 0x1201003;
    public static final int MAXINT = 1;
    public static final int NEGATIVE = 2;
    public static final int CORRUPT_6 = 3;
    public static final int CORRUPT_7 = 4;
    public static final int CORRUPT_8 = 5;
    public static final int CORRUPT_9 = 6;
    public static final int CORRUPT_10 = 7;
    int m_type = 0;

    public BFTForgePayloadFault() {
    }

    public BFTForgePayloadFault(HermesRuntime runtime, String faultID, byte triggerType, int type) {
        super(runtime, faultID, triggerType);
        m_type = type;
    }

    public BFTForgePayloadFault(String faultID, byte triggerType, int type) {
        super(null, faultID, triggerType);
        m_type = type;
    }

    public int getType() {
        return m_type;
    }

    @Override
    public int getSerialID() {
        return SERIAL_ID;
    }

    @Override
    protected void serializableImpl(ByteBuffer buf) throws Exception {
        buf.putInt(m_type);
    }

    @Override
    protected void deserializableImpl(ByteBuffer buf) throws Exception {
        //int serialID = buf.getInt(buf.position());        
        m_type = buf.getInt();
    }
    
    
    @Override
    public void executeImpl() throws Exception {
//        public static final int PROPOSE = 44781;
//    public static final int WEAK    = 44782;
//    public static final int STRONG  = 44783;
//    public static final int DECIDE  = 44784;
//    public static final int FREEZE  = 44785;
//    public static final int COLLECT = 44786;
        switch (m_type) {
            case MAXINT: {
                HermesRuntime.getInstance().getContext().put("attack", new Integer(4));
                break;
            }
            case NEGATIVE: {
                HermesRuntime.getInstance().getContext().put("attack", new Integer(5));
                break;
            }
            case CORRUPT_6: {
                HermesRuntime.getInstance().getContext().put("attack", new Integer(6));
                break;
            }
            case CORRUPT_7: {
                 HermesRuntime.getInstance().getContext().put("attack", new Integer(7));
                /*Integer type = (Integer)HermesRuntime.getInstance().getContext().getObject("TYPE");
                if(type!=null && type == 44782){
                    System.out.println("SET 7");
                    HermesRuntime.getInstance().getContext().put("attack", new Integer(7));
                }else{
                    System.out.println("RESET 7");
                    HermesRuntime.getInstance().getContext().put("attack", new Integer(1000));
                }*/
                break;
            }
            case CORRUPT_8: {
                 HermesRuntime.getInstance().getContext().put("attack", new Integer(8));
                /*Integer type = (Integer)HermesRuntime.getInstance().getContext().getObject("TYPE");
                if(type!=null && type == 44783){
                    HermesRuntime.getInstance().getContext().put("attack", new Integer(8));
                }else{
                    HermesRuntime.getInstance().getContext().put("attack", new Integer(1000));
                }*/
                break;
            }
            case CORRUPT_9: {
                HermesRuntime.getInstance().getContext().put("attack", new Integer(9));
                break;
            }
            case CORRUPT_10: {
                HermesRuntime.getInstance().getContext().put("attack", new Integer(10));
                break;
            }
        }
        
    }

    @Override
    public String toString() {
        String str = "BFTForgePayloadFault:" + this.getFaultID() + " sync=" + this.getSynchronizedFaul();
        return str;
    }
}
