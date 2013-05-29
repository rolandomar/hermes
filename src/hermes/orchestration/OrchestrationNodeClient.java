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
package hermes.orchestration;

import hermes.HermesConfig;
import hermes.network.HermesFuture;
import hermes.network.packets.HermesAbstractPacket;
import hermes.deployment.network.HermesNodeClientChannel;
import hermes.deployment.network.HermesNodeServerChannel;
import hermes.deployment.network.packets.InitPacket;
import hermes.orchestration.actions.Action;
import hermes.orchestration.actions.ActionResult;
import hermes.orchestration.network.HermesOrchestrationClientChannel;
import hermes.orchestration.network.packets.ActionReplyPacket;
import hermes.orchestration.network.packets.ActionRequestPacket;
import hermes.orchestration.network.packets.NotificationReplyPacket;
import hermes.orchestration.network.packets.NotificationRequestPacket;
import hermes.orchestration.notification.GeneralNotificationImpl;
import hermes.orchestration.notification.Notification;
import hermes.orchestration.remote.RemoteAction;
import hermes.orchestration.remote.RemoteActionResult;
import hermes.runtime.HermesRuntime;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class OrchestrationNodeClient {

    protected ReentrantLock m_lock = new ReentrantLock();
    HermesOrchestrationClientChannel m_channel;
    HermesRuntime m_runtime;
    AtomicInteger m_sequencer = new AtomicInteger(1);
    String m_id;

    public OrchestrationNodeClient(HermesRuntime runtime, String id) {
        m_runtime = runtime;
        m_id = id;
        m_channel = new HermesOrchestrationClientChannel(this, id);
    }

    public void notification(Notification notification, long timeout) {
        NotificationRequestPacket notificationPacket =
                new NotificationRequestPacket(m_sequencer.getAndIncrement(), notification);
        try {
            Future<HermesAbstractPacket> future = m_channel.sendRPCPacket(notificationPacket);
            HermesAbstractPacket replyAbs = future.get(timeout, TimeUnit.MILLISECONDS);
            NotificationReplyPacket reply = (NotificationReplyPacket) replyAbs;
            //System.out.println("notification acked");
            //return reply.getReturnValue();

        } catch (Exception ex) {
            Logger.getLogger(HermesNodeClientChannel.class.getName()).log(Level.SEVERE, "notification ex=", ex);
            //return null;
        }
    }

    public ActionResult action(Action notification, long timeout) throws Exception {
        ActionRequestPacket notificationPacket =
                new ActionRequestPacket(m_sequencer.getAndIncrement(), notification);
        try {
            Future<HermesAbstractPacket> future = m_channel.sendRPCPacket(notificationPacket);
            HermesAbstractPacket replyAbs = future.get(timeout, TimeUnit.MILLISECONDS);
            ActionReplyPacket reply = (ActionReplyPacket) replyAbs;
            //System.out.println("action acked");
            return reply.getActionResult();
        } catch (Exception ex) {
            Logger.getLogger(HermesNodeClientChannel.class.getName()).log(Level.SEVERE, "actionKKKKKK", ex);
            //return null;

            throw ex;
        }

    }

    public void open() throws Exception {
        
        try {
            m_lock.lock();
            System.out.println("OrchestrationNodeClient: open()");
            try {
                HermesFuture<Boolean> future = m_channel.
                        open(HermesConfig.getOrchestrationDaemonIP(),
                        HermesConfig.getOrchestrationDaemonPort());
                if (!future.get(2500, TimeUnit.MILLISECONDS)) {
                    throw new Exception("Connection refused");
                }
            } catch (IOException | InterruptedException | ExecutionException | TimeoutException ex) {
                Logger.getLogger(HermesNodeClientChannel.class.getName()).log(Level.SEVERE, "client error", ex);
                throw ex;
            }
            InitPacket packet = new InitPacket(0, m_id);
            try {
                m_channel.sendPacket(packet);
            } catch (Exception ex) {
                Logger.getLogger(HermesNodeClientChannel.class.getName()).log(Level.SEVERE, "sendPacket", ex);
                throw ex;
            }
        } finally {
            m_lock.unlock();
        }
    }

    public static void main(String[] args) {
        HermesRuntime r = new HermesRuntime();
        OrchestrationNodeClient client = new OrchestrationNodeClient(r, "AABBCCDD001122334455");
        try {
            client.open();
        } catch (Exception ex) {
            Logger.getLogger(OrchestrationNodeClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        GeneralNotificationImpl notification = new GeneralNotificationImpl("AABBCCDD00112233445566");
        client.notification(notification, 2500);
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(HermesNodeServerChannel.class.getName()).log(Level.SEVERE, "JJJJ", ex);
            }
        }
    }

    public void close() throws IOException {
        m_channel.close();
    }

    public RemoteActionResult onRemoteAction(RemoteAction remoteAction) {
        return m_runtime.onRemoteAction(remoteAction);
    }

    public void onServerClientChannelClose(HermesOrchestrationClientChannel aThis) {
        
    }
}
