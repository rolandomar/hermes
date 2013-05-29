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
package hermes.network.packets;

import hermes.serialization.HermesSerializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public abstract class HermesAbstractPacket implements HermesSerializable {

    protected HermesAbstractPacketHeader m_header = null;

    public void setHeader(HermesAbstractPacketHeader header){
        m_header = header;
    }
    
    public HermesAbstractPacket() {
        m_header = new HermesAbstractPacketHeader();
    }

    public HermesAbstractPacket(HermesAbstractPacketHeader header) {
        m_header = header;
    }

    public HermesAbstractPacketHeader getHeader() {
        return m_header;
    }

    protected abstract void serializeBody(ByteBuffer buf) throws Exception;

    public abstract void deserializeBody(ByteBuffer buf) throws Exception;

    @Override
    public void serializable(ByteBuffer buf) throws Exception {
        ByteBuffer bufferBody = ByteBuffer.allocate(HermesAbstractPacketHeader.DEFAULT_BODY_SIZE).order(ByteOrder.LITTLE_ENDIAN);
        serializeBody(bufferBody);
        //System.out.println("bufferBody SIZE="+bufferBody.position());
        m_header.setBodySize(bufferBody.position());
        m_header.serializable(buf);
        bufferBody.flip();
        buf.put(bufferBody);
        //System.out.println("PACKET SIZE="+buf.position());
    }

    @Override
    public void deserializable(ByteBuffer buf) throws Exception {
        //System.out.println("PACKET SIZE="+buf.position());
        //if (m_header == null) {
        //m_header = new HermesAbstractPacketHeader();
        m_header.deserializable(buf);
        //}
        if (buf.remaining() < m_header.getBodySize()) {
            throw new Exception("Malformed packet: insufficient buffer for body");
        }
        deserializeBody(buf);
    }
}
