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
package hermes.network;

import hermes.HermesConfig;
import hermes.network.packets.HermesAbstractPacket;
import hermes.network.packets.HermesAbstractPacketHeader;
import hermes.network.packets.PacketEnumeration;
import hermes.network.packets.ReadPacketContext;
import hermes.network.packets.WritePacketContext;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.WritePendingException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public abstract class HermesAsynchronousSocketChannel {

    protected ReentrantLock m_lock = new ReentrantLock();
    protected boolean m_closed = false;
    protected AsynchronousSocketChannel m_channel;
    protected String m_id = null;
    ByteBuffer m_byteBuffer = null;
    AtomicInteger m_sequencer = new AtomicInteger(0);
    HermesWriteCompletionHandler m_writeHandler;
    HermesReadCompletionHandler m_readHandler;
    protected List<PacketEnumeration> m_packetsEnum = null;

    public AsynchronousSocketChannel getChannel() {
        return m_channel;
    }

    public void setChannel(AsynchronousSocketChannel channel) {
        m_channel = channel;
        m_writeHandler = new HermesWriteCompletionHandler(this);
        m_readHandler = new HermesReadCompletionHandler(this);
    }

    public HermesAsynchronousSocketChannel() {//AsynchronousSocketChannel channel) {
        //m_channel = channel;
        m_byteBuffer = ByteBuffer.allocate(5012).order(ByteOrder.LITTLE_ENDIAN);
    }

    public HermesAsynchronousSocketChannel(AsynchronousSocketChannel channel) {
        //m_channel = channel;
        m_byteBuffer = ByteBuffer.allocate(5012).order(ByteOrder.LITTLE_ENDIAN);
        setChannel(channel);
    }

    public boolean isClosed() {
        try {
            m_lock.lock();
            return m_closed;
        } finally {
            m_lock.unlock();
        }
    }

    public void close() throws IOException {
        if (HermesConfig.DEBUG) {
            Logger.getLogger(HermesAsynchronousSocketChannel.class.getName()).
                    log(Level.SEVERE, "close");
        }
        try {
            m_lock.lock();
            if (m_closed) {
                return;
            }
            m_closed = true;
            try {
                m_channel.shutdownOutput();
                m_channel.close();
            } catch (Exception innerEx) {
            }
            onClose();
        } catch (Exception ex) {
            if (HermesConfig.DEBUG) {
                Logger.getLogger(HermesAsynchronousSocketChannel.class.getName()).
                        log(Level.SEVERE, "close ex:" + ex.toString());
            }
            throw ex;
        } finally {
            m_lock.unlock();
            if (HermesConfig.DEBUG) {
                Logger.getLogger(HermesAsynchronousSocketChannel.class.getName()).
                        log(Level.SEVERE, "unclock");
            }
        }
    }

    public String getID() {
        return m_id;
    }

    public void setID(String id) {
        m_id = id;
    }

    public void sendPacket(HermesAbstractPacket packet) throws Exception {
        //System.out.println("sendPacket");

        try {
            m_lock.lock();
//        if (HermesConfig.DEBUG) {
//            Logger.getLogger(HermesAsynchronousSocketChannel.class.getName()).
//                    log(Level.FINEST, "sendPacket op={0}", packet.getHeader().getOpcode());
//        }
            ByteBuffer byteBuffer = ByteBuffer.allocate(5012).order(ByteOrder.LITTLE_ENDIAN);
            packet.getHeader().setSequenceNo(m_sequencer.getAndIncrement());
            packet.serializable(byteBuffer);
            byteBuffer.flip();
//        for(int i=0; i < byteBuffer.array().length; i++){
//            System.out.print(byteBuffer.array()[i]+",");
//        }
            boolean send = false;
            while (!send) {
                try {
                    m_channel.write(byteBuffer, new WritePacketContext(packet, byteBuffer), m_writeHandler);
                    send = true;
                } catch (WritePendingException ex) {
                    Logger.getLogger(HermesAsynchronousSocketChannel.class.getName()).
                            log(Level.FINEST, "sendPacket: EX()");
                }
            }
        } finally {
            //System.out.println("sendPacket");
            m_lock.unlock();
        }
    }

    public void activateRead() {
        //System.out.println("\n\nactivateRead\n\n");
        try {
            m_lock.lock();
            ReadPacketContext rc = new ReadPacketContext(m_byteBuffer);
            System.out.println("");
            m_channel.read(m_byteBuffer, rc, m_readHandler);
        } finally {
            m_lock.unlock();
        }
    }

    //boolean: true complete, 0 otherwise
    public boolean readPacket() throws Exception {
        if (HermesConfig.DEBUG) {
            Logger.getLogger(HermesAsynchronousSocketChannel.class.getName()).
                    log(Level.FINE, "readPacket ()");
        }
        try {
            if (m_byteBuffer.remaining() < HermesAbstractPacketHeader.HEADER_SIZE) {
                System.out.println("readPacket (1)");
                return false;
            }
            HermesAbstractPacketHeader header = new HermesAbstractPacketHeader();
            header.deserializable(m_byteBuffer);
            if (m_byteBuffer.remaining() < header.getBodySize()) {
                System.out.println("readPacket (2) :" + m_byteBuffer.remaining() + " " + header.getBodySize());
                return false;
            }

            //System.out.println("readPacket (3) :"+m_byteBuffer.remaining()+" "+header.getBodySize());
            Class packetClass = getPacketClass(header.getOpcode());
            if (packetClass == null) {
                throw new Exception("Unknown packet type");
            }

            HermesAbstractPacket packet = null;
            packet = (HermesAbstractPacket) packetClass.newInstance();
            packet.setHeader(header);
            packet.deserializeBody(m_byteBuffer);
            this.onPacketReceived(packet);
            return true;
        } catch (Exception ex) {
            System.out.println("readPacket Exception=" + ex.getLocalizedMessage());
            ex.printStackTrace();
            m_byteBuffer.flip();
            try {
                close();
            } catch (IOException ex1) {
                Logger.getLogger(HermesAsynchronousSocketChannel.class.getName()).log(Level.SEVERE, null, ex1);
            }
            throw ex;
        }


    }

    protected Class getPacketClass(int opcode) {
        for (PacketEnumeration e : m_packetsEnum) {
            //System.out.println("e.opcode="+e.getOpcode()+" opcode="+opcode);
            if (e.equals(opcode)) {
                return e.getPacketClass();
            }
        }
        return null;
    }

    public abstract void onPacketReceived(HermesAbstractPacket packet);

    public abstract void onPacketWrite(HermesAbstractPacket packet);

    public abstract void onClose();
}
