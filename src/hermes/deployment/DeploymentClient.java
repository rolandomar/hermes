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
package hermes.deployment;

import hermes.HermesConfig;
import hermes.network.HermesFuture;
import hermes.network.packets.HermesAbstractPacket;
import hermes.deployment.network.EnvElement;
import hermes.deployment.network.HermesNodeClientChannel;
import hermes.deployment.network.HermesNodeServerChannel;
import hermes.deployment.network.packets.BootstrapReplyPacket;
import hermes.deployment.network.packets.BootstrapRequestPacket;
import hermes.deployment.network.packets.InitPacket;
import hermes.deployment.network.packets.KillAllReplyPacket;
import hermes.deployment.network.packets.KillAllRequestPacket;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class DeploymentClient {

    HermesNodeClientChannel m_channel;
    AtomicInteger m_sequencer = new AtomicInteger(1);
    String m_daemonIP;
    int m_port;

    public DeploymentClient(String daemonIP, int port, String clientID) {
        super();
        m_channel = new HermesNodeClientChannel(this, clientID);
        m_daemonIP = daemonIP;
        m_port = port;

    }

    public String getID() {
        return m_channel.getID();
    }

    public boolean bootstrapApplication(String binary, String[] args, EnvElement[] env, long timeout) {
        BootstrapRequestPacket bootAppPacket = new BootstrapRequestPacket(m_sequencer.getAndIncrement(), binary, args, env);
        try {
            if (HermesConfig.DEBUG) {
                Logger.getLogger(DeploymentClient.class.getName()).
                        log(Level.INFO, "HermesNodeClient: bootstrapApplication(): {0}", binary);
            }
            Future<HermesAbstractPacket> future = m_channel.sendRPCPacket(bootAppPacket);
            HermesAbstractPacket replyAbs = future.get(timeout, TimeUnit.MILLISECONDS);
            BootstrapReplyPacket reply = (BootstrapReplyPacket) replyAbs;
            if (HermesConfig.DEBUG) {
                Logger.getLogger(DeploymentClient.class.getName()).
                        log(Level.INFO,
                        "HermesNodeClient: bootstrapApplication(): launched with return {0}", reply.getReturnValue());
            }
            return reply.getReturnValue();

        } catch (Exception ex) {
            Logger.getLogger(HermesNodeClientChannel.class.getName()).log(Level.SEVERE,
                    "HermesNodeClient: bootstrapApplication(): EX", ex);
            return false;
        }
    }

    public void killAllProcesses(long timeout) {
        KillAllRequestPacket bootAppPacket = new KillAllRequestPacket(m_sequencer.getAndIncrement());
        try {
            if (HermesConfig.DEBUG) {
                Logger.getLogger(DeploymentClient.class.getName()).
                        log(Level.INFO,
                        "HermesNodeClient: killAllProcesses()");
            }
            Future<HermesAbstractPacket> future = m_channel.sendRPCPacket(bootAppPacket);
            HermesAbstractPacket replyAbs = future.get(timeout, TimeUnit.MILLISECONDS);
            KillAllReplyPacket reply = (KillAllReplyPacket) replyAbs;            
        } catch (Exception ex) {
            Logger.getLogger(HermesNodeClientChannel.class.getName()).log(Level.SEVERE, 
                    "HermesNodeClient: killAllProcesses(): ex {0}", ex);

        }
    }

    public void open(long timeout) throws Exception {
        try {
            System.out.println("Connecting to node : "+m_daemonIP);
            HermesFuture<Boolean> future = m_channel.open(m_daemonIP, m_port);
            future.get(timeout, TimeUnit.MILLISECONDS);
            if (!future.get()) {
                System.out.println("Connection refused: "+m_daemonIP);
                throw new Exception("Connection refused: "+m_daemonIP);
            }
            if (HermesConfig.DEBUG) {
                Logger.getLogger(DeploymentClient.class.getName()).
                        log(Level.INFO, "HermesNodeClient: open(): connection established");
            }

        } catch (IOException | InterruptedException | ExecutionException | TimeoutException ex) {
            Logger.getLogger(HermesNodeClientChannel.class.getName()).log(Level.SEVERE,
                    "HermesNodeClient: open(): client error", ex);
            throw ex;
        }
        InitPacket packet = new InitPacket(0, m_channel.getID());
        try {
            m_channel.sendPacket(packet);
        } catch (Exception ex) {
            Logger.getLogger(HermesNodeClientChannel.class.getName()).log(Level.SEVERE,
                    "HermesNodeClient: open(): failed to send exception", ex);
            throw ex;
        }
    }

    public static void main(String[] args) {
        DeploymentClient client = new DeploymentClient(
                HermesConfig.getNodeDaemonIP(),
                HermesConfig.getNodeDaemonPort(), "AABB001122334455667788");
        System.out.println("Connecting to " + HermesConfig.getNodeDaemonIP() + ":" + HermesConfig.getNodeDaemonPort());
        try {
            client.open(5000);
        } catch (Exception ex) {
            Logger.getLogger(DeploymentClient.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        client.killAllProcesses(2500);
        EnvElement[] env = new EnvElement[]{new EnvElement("home", "/home/rmartins")};        
        client.bootstrapApplication("/bin/ls", new String[]{"-l"}, env, 2500);
        while (true) {
            try {
                Thread.sleep(1500);
                client.killAllProcesses(2500);
            } catch (InterruptedException ex) {
                Logger.getLogger(HermesNodeServerChannel.class.getName()).log(Level.SEVERE, "MainEx", ex);
            }
        }
    }

    public void onChannelClose() {
    }
}
