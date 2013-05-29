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
package hermes.orchestration.network;

import hermes.deployment.network.HermesNodeClientChannel;
import hermes.deployment.network.HermesNodeServerChannel;
import hermes.deployment.network.EnvElement;
import hermes.HermesConfig;
import hermes.network.HermesClientChannel;
import hermes.network.HermesFuture;
import hermes.network.packets.HermesAbstractPacket;
import hermes.network.packets.rpc.TwoWayRequest;
import hermes.deployment.network.packets.BootstrapRequestPacket;
import hermes.deployment.network.packets.InitPacket;
import hermes.deployment.network.packets.InitReplyPacket;
import hermes.orchestration.OrchestrationNodeClient;
import hermes.orchestration.network.packets.ActionReplyPacket;
import hermes.orchestration.network.packets.ActionRequestPacket;
import hermes.orchestration.network.packets.NotificationReplyPacket;
import hermes.orchestration.network.packets.OrchestrationPackets;
import hermes.orchestration.network.packets.RemoteActionReplyPacket;
import hermes.orchestration.network.packets.RemoteActionRequestPacket;
import hermes.orchestration.remote.RemoteActionResult;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class HermesOrchestrationClientChannel extends HermesClientChannel {

    Map<Integer, HermesFuture<HermesAbstractPacket>> m_requestMap;
    OrchestrationNodeClient m_client;
    ReentrantLock m_lock = new ReentrantLock();
    boolean m_closed = false;

    public HermesOrchestrationClientChannel(OrchestrationNodeClient client, String id) {
        super(id);
        m_packetsEnum = OrchestrationPackets.HermesOrchestrationPackets;
        m_client = client;
        //for(PacketEnumeration e: m_packetsEnum){
        //    System.out.println("PacketEnumeration: "+e.getOpcode());
        //}        
        m_requestMap = new HashMap<>(1000);
    }

    public Future<HermesAbstractPacket> sendRPCPacket(TwoWayRequest packet) throws Exception {
        
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

    @Override
    public void onPacketReceived(final HermesAbstractPacket packet) {
        //System.out.println("onPacketReceived: opcode="+packet.getHeader().getOpcode());
        switch (packet.getHeader().getOpcode()) {
            case ActionRequestPacket.ACTIONREQUESTY_OPCODE: {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //System.out.println("Packet receved: ActionRequestPacket");
                        RemoteActionRequestPacket notificationPacket = (RemoteActionRequestPacket) packet;
                        RemoteActionResult result = m_client.onRemoteAction(notificationPacket.getRemoteAction());
                        RemoteActionReplyPacket replyPacket = new RemoteActionReplyPacket(notificationPacket.getRequestNo(),
                                result);
                        try {
                            sendPacket(replyPacket);
                        } catch (Exception ex) {
                            try {
                                close();
                            } catch (IOException ex1) {
                                Logger.getLogger(HermesOrchestrationClientChannel.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                        }
                    }
                }).start();
                return;
            }
            case NotificationReplyPacket.NOTIFICATIONREPLY_OPCODE: {
                NotificationReplyPacket reply = (NotificationReplyPacket) packet;
                HermesFuture<HermesAbstractPacket> future = m_requestMap.remove(reply.getRequestNo());
                future.put(reply);
                ///System.out.println("onPacketReceived: NotificationReplyPacket");
                return;

            }
            case ActionReplyPacket.ACTIONREPLY_OPCODE: {
                ActionReplyPacket reply = (ActionReplyPacket) packet;
                // System.out.println("onPacketReceived: ActionReplyPacket:"+packet.getHeader().getSequenceNo());
                HermesFuture<HermesAbstractPacket> future = m_requestMap.remove(reply.getRequestNo());
                //if(future != null){
                future.put(reply);
                //}else{
                //  System.out.println("\n\nonPacketReceived: ActionReplyPacket NULL REQUEST");
                //}

                //System.out.println("onPacketReceived: NotificationReplyPacket");
                return;

            }

            case RemoteActionRequestPacket.SERIAL_ID: {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //System.out.println("onPacketReceived: RemoteActionRequestPacket");
                        RemoteActionRequestPacket request = (RemoteActionRequestPacket) packet;
                        RemoteActionResult r = m_client.onRemoteAction(request.getRemoteAction());
                        RemoteActionReplyPacket reply = new RemoteActionReplyPacket(request.getRequestNo(), r);
                        try {
                            //System.out.println("onPacketReceived: RemoteActionRequestPacket: before packet");
                            sendPacket(reply);
                            //System.out.println("onPacketReceived: RemoteActionRequestPacket: after packet");
                        } catch (Exception ex) {
                            Logger.getLogger(HermesOrchestrationClientChannel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }).start();
                return;
            }
            case InitReplyPacket.INITPACKETREPLY_OPCODE: {
                //check reply
                return;
            }

        }

        System.out.println("onPacketReceived not notification=" + packet.getHeader().getOpcode()
                + " seq=" + packet.getHeader().getSequenceNo());
    }

    @Override
    public void onPacketWrite(HermesAbstractPacket packet) {
        // System.out.println("onPacketWrite");
    }

    @Override
    public void onClose() {
        
        try {
            m_lock.lock();
            m_client.onServerClientChannelClose(this);
        } finally {
            m_lock.unlock();
        }
    }

    public static void main(String[] args) {
        HermesOrchestrationClientChannel client =
                new HermesOrchestrationClientChannel(null, "AABBCCDD001122334455");
        try {
            System.out.println("Connectioing");
            HermesFuture<Boolean> future = client.open(HermesConfig.getNodeDaemonIP(), HermesConfig.getNodeDaemonPort());
            future.get(2500, TimeUnit.MILLISECONDS);
            if (!future.get()) {
                System.exit(-1);
            }

            System.out.println("Waiting for connection");
        } catch (IOException | InterruptedException | ExecutionException | TimeoutException ex) {
            Logger.getLogger(HermesNodeClientChannel.class.getName()).log(Level.SEVERE, "client error", ex);
            System.exit(-1);
        }
        InitPacket packet = new InitPacket(0, "AABB00000000000010");
        String binary = "superpeer.sh";
        String[] appargs = new String[]{"--id " + "'BBCC00000000000010'"};
        EnvElement[] env = new EnvElement[]{new EnvElement("home", "/home/rmartins")};
        BootstrapRequestPacket bootAppPacket = new BootstrapRequestPacket(1, binary, appargs, env);
        try {
            client.sendPacket(packet);
            client.sendPacket(bootAppPacket);
        } catch (Exception ex) {
            Logger.getLogger(HermesNodeClientChannel.class.getName()).log(Level.SEVERE, "KKKKKK", ex);
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
