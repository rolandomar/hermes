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
import hermes.network.HermesAsynchronousSocketChannel;
import hermes.network.HermesServerChannel;
import hermes.network.packets.HermesAbstractPacket;
import hermes.deployment.DeploymentDaemon;
import hermes.deployment.network.packets.BootstrapReplyPacket;
import hermes.deployment.network.packets.BootstrapRequestPacket;
import hermes.deployment.network.packets.HermesNodePackets;
import hermes.deployment.network.packets.InitPacket;
import hermes.deployment.network.packets.InitReplyPacket;
import hermes.deployment.network.packets.KillAllReplyPacket;
import hermes.deployment.network.packets.KillAllRequestPacket;
import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class HermesNodeServerClientChannel extends HermesAsynchronousSocketChannel {

    DeploymentDaemon m_daemon = null;
    HermesServerChannel m_server;

    public HermesNodeServerClientChannel(DeploymentDaemon daemon, AsynchronousSocketChannel channel,
            HermesServerChannel server) {
        super(channel);
        m_daemon = daemon;
        m_packetsEnum = HermesNodePackets.HermesNodePackets;
        m_server = server;
    }

    @Override
    public void onPacketReceived(final HermesAbstractPacket packet) {
        if (HermesConfig.DEBUG) {
            Logger.getLogger(HermesNodeServerClientChannel.class.getName()).
                    log(Level.FINEST, "Packet received");
        }
        switch (packet.getHeader().getOpcode()) {
            case InitPacket.INITPACKET_OPCODE: {
                InitPacket initPacket = (InitPacket) packet;
                System.out.println("Packet receved: initPacket ID=" + initPacket.getID());
                this.setID(initPacket.getID());
                m_server.addClient(this);
                InitReplyPacket replyPacket = new InitReplyPacket(initPacket.getRequestNo(), 0, "All done");
                try {
                    sendPacket(replyPacket);
                } catch (Exception ex) {
                    try {
                        Logger.getLogger(HermesNodeServerClientChannel.class.getName()).log(Level.SEVERE, null, ex);
                        close();
                    } catch (IOException ex1) {
                        Logger.getLogger(HermesNodeServerClientChannel.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
                break;
            }
            case BootstrapRequestPacket.BOOTSTRAPREQUESTY_OPCODE: {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (HermesConfig.DEBUG) {
                            Logger.getLogger(HermesNodeServerClientChannel.class.getName()).
                                    log(Level.FINEST, "Packet receved: BOOTSTRAPREQUESTY_OPCODE");
                        }
                        BootstrapRequestPacket bootstrapPacket = (BootstrapRequestPacket) packet;
                        boolean retValue = m_daemon.boostrapApplication(
                                bootstrapPacket.getBinary(),
                                bootstrapPacket.getArgs(),
                                bootstrapPacket.getEnv());
                        BootstrapReplyPacket replyPacket =
                                new BootstrapReplyPacket(bootstrapPacket.getRequestNo(),
                                retValue);
                        try {
                            if (HermesConfig.DEBUG) {
                                Logger.getLogger(HermesNodeServerClientChannel.class.getName()).
                                        log(Level.FINEST, "Packet receved: BOOTSTRAPREQUESTY_OPCODE sending reply");
                            }
                            sendPacket(replyPacket);
                        } catch (Exception ex) {
                            try {
                                close();
                            } catch (IOException ex1) {
                                Logger.getLogger(HermesNodeServerClientChannel.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                        }

                    }
                }).start();

                return;
            }
            case KillAllRequestPacket.SERIAL_ID: {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        KillAllRequestPacket killAllPacket = (KillAllRequestPacket) packet;
                        m_daemon.killAllProcesses();
                        KillAllReplyPacket replyPacket =
                                new KillAllReplyPacket(killAllPacket.getRequestNo());
                        try {
                            sendPacket(replyPacket);
                        } catch (Exception ex) {
                            try {
                                close();
                            } catch (IOException ex1) {
                                Logger.getLogger(HermesNodeServerClientChannel.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                        }
                    }
                }).start();
                return;
            }
        }

    }

    @Override
    public void onPacketWrite(HermesAbstractPacket packet) {
    }

    @Override
    public void onClose() {
    }
}
