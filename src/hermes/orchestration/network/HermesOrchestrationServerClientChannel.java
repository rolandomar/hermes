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

import hermes.HermesConfig;
import hermes.network.HermesAsynchronousSocketChannel;
import hermes.network.HermesFuture;
import hermes.network.HermesServerChannel;
import hermes.network.packets.HermesAbstractPacket;
import hermes.network.packets.rpc.TwoWayRequest;
import hermes.deployment.network.packets.InitPacket;
import hermes.deployment.network.packets.InitReplyPacket;
import hermes.orchestration.OrchestrationNodeDaemon;
import hermes.orchestration.OrchestrationNodeServerClient;
import hermes.orchestration.actions.ActionResult;
import hermes.orchestration.network.packets.ActionReplyPacket;
import hermes.orchestration.network.packets.ActionRequestPacket;
import hermes.orchestration.network.packets.NotificationReplyPacket;
import hermes.orchestration.network.packets.NotificationRequestPacket;
import hermes.orchestration.network.packets.OrchestrationPackets;
import hermes.orchestration.network.packets.RemoteActionReplyPacket;
import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class HermesOrchestrationServerClientChannel extends HermesAsynchronousSocketChannel {

    OrchestrationNodeDaemon m_daemon = null;
    OrchestrationNodeServerClient m_client = null;
    HermesServerChannel m_server = null;
    Map<Integer, HermesFuture<HermesAbstractPacket>> m_requestMap;

    public HermesOrchestrationServerClientChannel(
            OrchestrationNodeDaemon daemon,
            AsynchronousSocketChannel channel,
            HermesServerChannel server) {
        super(channel);
        m_daemon = daemon;
        m_client = null;
        m_packetsEnum = OrchestrationPackets.HermesOrchestrationPackets;
        m_server = server;
        m_requestMap = new HashMap<>(1000);

    }

    public void setClient(OrchestrationNodeServerClient client) {
        m_client = client;
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
            Collection<HermesFuture<HermesAbstractPacket>> list = m_requestMap.values();
            for (HermesFuture<HermesAbstractPacket> e : list) {
                System.out.println("Canceling events");
                e.cancel(true);
            }
            m_requestMap.clear();
            super.close();
        } finally {
            m_lock.unlock();
        }
    }

    @Override
    public void onPacketReceived(final HermesAbstractPacket packet) {
        //System.out.println("HermesOrchestrationServerClientChannel:Packet received ="+packet.getHeader().getOpcode());
        switch (packet.getHeader().getOpcode()) {
            case InitPacket.INITPACKET_OPCODE: {
                InitPacket initPacket = (InitPacket) packet;
                System.out.println("Packet receved: initPacket seq=" + packet.getHeader().getSequenceNo() + " ID"
                        + initPacket.getID());
                this.setID(initPacket.getID());
                m_server.addClient(this);
                m_daemon.onNewServerClientChannel(this);
                InitReplyPacket replyPacket = new InitReplyPacket(initPacket.getRequestNo(), 0, "All done");
                try {
                    sendPacket(replyPacket);
                } catch (Exception ex) {
                    try {
                        close();
                    } catch (IOException ex1) {
                        Logger.getLogger(HermesOrchestrationServerClientChannel.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
                break;
            }
            case NotificationRequestPacket.NOTIFICATIONREQUESTY_OPCODE: {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //System.out.println("Packet receved: NotificationRequestPacket");
                        NotificationRequestPacket notificationPacket = (NotificationRequestPacket) packet;
                        m_daemon.onNotification(m_client, notificationPacket.getNotification());
                        NotificationReplyPacket replyPacket = new NotificationReplyPacket(notificationPacket.getRequestNo(),
                                NotificationReplyPacket.RETCODE_OK);
                        try {
                            sendPacket(replyPacket);
                        } catch (Exception ex) {
                            try {
                                close();
                            } catch (IOException ex1) {
                                Logger.getLogger(HermesOrchestrationServerClientChannel.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                        }
                    }
                }).start();


                return;
            }

            case ActionRequestPacket.ACTIONREQUESTY_OPCODE: {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //System.out.println("Packet receved: ActionRequestPacket");
                        ActionRequestPacket notificationPacket = (ActionRequestPacket) packet;
                        ActionResult result = m_daemon.onAction(m_client, notificationPacket.getAction());
                        ActionReplyPacket replyPacket = new ActionReplyPacket(notificationPacket.getRequestNo(),
                                result);
                        try {
                            sendPacket(replyPacket);
                        } catch (Exception ex) {
                            try {
                                close();
                            } catch (IOException ex1) {
                                Logger.getLogger(HermesOrchestrationServerClientChannel.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                        }
                    }
                }).start();
                return;
            }

            case RemoteActionReplyPacket.SERIAL_ID: {
                RemoteActionReplyPacket reply = (RemoteActionReplyPacket) packet;
                HermesFuture<HermesAbstractPacket> future = m_requestMap.remove(reply.getRequestNo());
                future.put(reply);
            }
        }

    }

    @Override
    public void onPacketWrite(HermesAbstractPacket packet) {
    }

    @Override
    public void onClose() {
        if (HermesConfig.DEBUG) {
            Logger.getLogger(HermesOrchestrationServerClientChannel.class.getName()).
                    log(Level.INFO, "OrchestrationNodeDaemon: onServerClientChannelClose():onClose");
        }
        try {
            m_lock.lock();
            m_server.onServerClientChannelClose(this);
            m_daemon.onServerClientChannelClose(this);
        } finally {
            m_lock.unlock();
            if (HermesConfig.DEBUG) {
                Logger.getLogger(HermesOrchestrationServerClientChannel.class.getName()).
                        log(Level.INFO, "OrchestrationNodeDaemon: onServerClientChannelClose():onClose after notifications");
            }
        }
        //m_daemon.onServerClientChannelClose(this);
    }
}
