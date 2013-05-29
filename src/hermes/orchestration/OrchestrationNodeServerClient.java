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

import hermes.network.packets.HermesAbstractPacket;
import hermes.deployment.network.HermesNodeClientChannel;
import hermes.orchestration.network.HermesOrchestrationServerClientChannel;
import hermes.orchestration.network.packets.RemoteActionReplyPacket;
import hermes.orchestration.network.packets.RemoteActionRequestPacket;
import hermes.orchestration.remote.AddAspectRemoteAction;
import hermes.orchestration.remote.AddAspectRemoteActionResult;
import hermes.orchestration.remote.GetStateActionResult;
import hermes.orchestration.remote.GetStateRemoteAction;
import hermes.orchestration.remote.RemoteAction;
import hermes.orchestration.remote.RemoteActionResult;
import hermes.runtime.HermesFault;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolando Martins Carnegie Mellon University
 */
public class OrchestrationNodeServerClient {

    HermesOrchestrationServerClientChannel m_clientChannel;
    AtomicInteger m_requestSequencer = new AtomicInteger(1);
    OrchestrationNodeDaemon m_daemon;

    public OrchestrationNodeServerClient(
            OrchestrationNodeDaemon daemon,
            HermesOrchestrationServerClientChannel clientChannel) {
        m_daemon = daemon;
        m_clientChannel = clientChannel;
    }

    public int getState(String stateID) throws Exception {

        GetStateRemoteAction ra = new GetStateRemoteAction();//m_daemon.getID());
        GetStateActionResult result = (GetStateActionResult) remoteAction(ra, 2500);
        return result.getSpeedBump();

    }

    public boolean addFault(HermesFault fault) throws Exception {
        Logger.getLogger(OrchestrationNodeServerClient.class.getName()).log(Level.INFO,
                "OrchestrationNodeServerClient:addFault() : addFault "+this.getID()+" ...");
        AddAspectRemoteAction ra = new AddAspectRemoteAction(fault);
        AddAspectRemoteActionResult result = (AddAspectRemoteActionResult) remoteAction(ra, 10000);
        System.out.println("addFault after");
        Logger.getLogger(OrchestrationNodeServerClient.class.getName()).log(Level.INFO,
                "OrchestrationNodeServerClient:addFault() : addFault after");
        return result.getResult();
    }

    public RemoteActionResult remoteAction(RemoteAction notification, long timeout) throws Exception {
        Logger.getLogger(OrchestrationNodeServerClient.class.getName()).log(Level.INFO, "OrchestrationNodeServerClient:remoteAction() : before send remoteAction");
        RemoteActionRequestPacket notificationPacket =
                new RemoteActionRequestPacket(m_requestSequencer.getAndIncrement(), notification);
        try {
            Logger.getLogger(OrchestrationNodeServerClient.class.getName()).log(Level.INFO, "OrchestrationNodeServerClient:remoteAction() : before send remoteAction");
            System.out.println("OrchestrationNodeServerClient:remoteAction() before send remoteAction");
            Future<HermesAbstractPacket> future = m_clientChannel.sendRPCPacket(notificationPacket);
            HermesAbstractPacket replyAbs = future.get(timeout, TimeUnit.MILLISECONDS);
            if (replyAbs == null) {
                Logger.getLogger(OrchestrationNodeServerClient.class.getName()).log(Level.SEVERE, "OrchestrationNodeServerClient:remoteAction() emote action FAILED");
                throw new Exception("Request failed");
            }
            RemoteActionReplyPacket reply = (RemoteActionReplyPacket) replyAbs;
            Logger.getLogger(OrchestrationNodeServerClient.class.getName()).log(Level.INFO, "OrchestrationNodeServerClient:remoteAction() : remote action acked");
            return reply.getRemoteActionResult();
        } catch (Exception ex) {
            Logger.getLogger(HermesNodeClientChannel.class.getName()).log(Level.SEVERE, "OrchestrationNodeServerClient:remoteAction() : remoteAction Ex=", ex);
            throw ex;
        }

    }

    public String getID() {
        return m_clientChannel.getID();
    }
}
