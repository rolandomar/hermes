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
package hermes.deployment.network;

import hermes.HermesConfig;
import hermes.network.HermesClientChannel;
import hermes.network.HermesFuture;
import hermes.network.packets.HermesAbstractPacket;
import hermes.network.packets.rpc.TwoWayRequest;
import hermes.deployment.DeploymentClient;
import hermes.deployment.network.packets.BootstrapReplyPacket;
import hermes.deployment.network.packets.BootstrapRequestPacket;
import hermes.deployment.network.packets.HermesNodePackets;
import hermes.deployment.network.packets.InitPacket;
import hermes.deployment.network.packets.KillAllReplyPacket;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
//deployment client for coordinator
public class HermesNodeClientChannel extends HermesClientChannel {

    Map<Integer, HermesFuture<HermesAbstractPacket>> m_requestMap;
    protected final DeploymentClient m_client;

    public HermesNodeClientChannel(DeploymentClient client, String id) {
        super(id);
        m_packetsEnum = HermesNodePackets.HermesNodePackets;
        m_requestMap = new HashMap<>(1000);
        m_client = client;
    }

    public Future<HermesAbstractPacket> sendRPCPacket(TwoWayRequest packet) throws Exception {
        //System.out.println("sendRPCPacket");
        
        try {
            m_lock.lock();
            HermesFuture<HermesAbstractPacket> future = new HermesFuture<>();
            m_requestMap.put(packet.getRequestNo(), future);
            this.sendPacket(packet);
            return future;
        } finally {
            m_lock.unlock();
        }
    }

    @Override
    public void onPacketReceived(HermesAbstractPacket packet) {
        //System.out.println("HermesNodeClientChannel:onPacketReceived");
        if (packet.getHeader().getOpcode() == BootstrapReplyPacket.BOOTSTRAPREPLY_OPCODE) {
            BootstrapReplyPacket reply = (BootstrapReplyPacket) packet;
            HermesFuture<HermesAbstractPacket> future = m_requestMap.get(reply.getRequestNo());
            future.put(reply);
            //return;
            //System.out.println("onPacketReceived: BOOTSTRAPREPLY_OPCODE="+reply.getReturnValue());
            return;
        }
        if (packet.getHeader().getOpcode() == KillAllReplyPacket.SERIAL_ID) {
            KillAllReplyPacket reply = (KillAllReplyPacket) packet;
            HermesFuture<HermesAbstractPacket> future = m_requestMap.get(reply.getRequestNo());
            future.put(reply);
            return;
        }
    }

    @Override
    public void onPacketWrite(HermesAbstractPacket packet) {
        System.out.println("onPacketWrite");
    }

    @Override
    public void onClose() {
        System.out.println("onClose");
        if (m_client != null) {
            m_client.onChannelClose();
        }
    }

    @Override
    public void close() throws IOException {
        
        
        try {
            m_lock.lock();
            super.close();
            Collection<HermesFuture<HermesAbstractPacket>> list = m_requestMap.values();
            for (HermesFuture<HermesAbstractPacket> e : list) {
                e.cancel(true);
            }
            m_requestMap.clear();

        } finally {
            m_lock.unlock();
        }
    }

    public static void main(String[] args) {
        HermesNodeClientChannel client = new HermesNodeClientChannel(null, "AABB001122334455667788");
        try {
            HermesFuture<Boolean> future = client.open(HermesConfig.getNodeDaemonIP(), HermesConfig.getNodeDaemonPort());
            if (!future.get(1000, TimeUnit.MILLISECONDS)) {
                System.exit(-1);
            }
            System.out.println("Waiting for connection");
        } catch (IOException | InterruptedException | ExecutionException | TimeoutException ex) {
            Logger.getLogger(HermesNodeClientChannel.class.getName()).log(Level.SEVERE, "client error", ex);
        }
        InitPacket packet = new InitPacket(0, "AABB00000000000010");
        String binary = "superpeer.sh";
        String[] appargs = new String[]{"--id " + "'BBCC00000000000010'"};
        //String env = "";
        EnvElement[] env = new EnvElement[]{new EnvElement("home", "/home/rmartins")};
        BootstrapRequestPacket bootAppPacket = new BootstrapRequestPacket(1, binary, appargs, env);
        try {
            client.sendPacket(packet);
            client.sendPacket(bootAppPacket);
        } catch (Exception ex) {
            Logger.getLogger(HermesNodeClientChannel.class.getName()).log(Level.SEVERE, "mainKKKKKK", ex);
        }
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(HermesNodeServerChannel.class.getName()).log(Level.SEVERE, "JJJJ", ex);
            }
        }

    }
}
