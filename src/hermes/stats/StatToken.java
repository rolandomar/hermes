/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hermes.stats;

import hermes.serialization.HermesSerializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;

/**
 *
 * @author rmartins
 */
public class StatToken implements HermesSerializable{

    public long m_duration = -1;
    public long m_i = -1;

    public StatToken() {        
    }
    
    public StatToken(int i, long duration) {
        m_duration = duration;
        m_i = i;
    }

    @Override
    public void serializable(ByteBuffer buf) throws Exception {
        buf.putLong(m_duration);
        buf.putLong(m_i);
    }

    @Override
    public void deserializable(ByteBuffer buf) throws Exception {
        m_duration = buf.getLong();
        m_i = buf.getLong();
        
    }
    
    public void dump(StringBuffer bf){
        float duration = ((float)m_duration/(float)1000000);
        BigDecimal bd = new BigDecimal(duration).setScale(2, RoundingMode.HALF_UP);
        bf.append(bd.floatValue()+","+m_i+"\n");
    }
    
    @Override
    public String toString(){
        return "Stat("+((float)m_duration/(float)1000000)+","+m_i+")";
    }
}