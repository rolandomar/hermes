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
import hermes.orchestration.actions.Action;
import hermes.orchestration.actions.ActionResult;
import hermes.orchestration.network.HermesOrchestrationServerChannel;
import hermes.orchestration.network.HermesOrchestrationServerClientChannel;
import hermes.orchestration.notification.Notification;
import hermes.runtime.HermesRuntime;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public abstract class OrchestrationNodeDaemon {

    protected String m_id = HermesRuntime.getRandomUUID();
    protected Map<String, OrchestrationNodeServerClient> m_clientMap = new HashMap<>();
    protected final ReentrantLock m_lock = new ReentrantLock();

    public Map<String, OrchestrationNodeServerClient> getClientMap() {
        return m_clientMap;
    }

    public String getID() {
        return m_id;
    }

    abstract public void onNotification(OrchestrationNodeServerClient client, Notification notification);

    abstract public ActionResult onAction(OrchestrationNodeServerClient client, Action action);
    HermesOrchestrationServerChannel serverChannel = new HermesOrchestrationServerChannel(this);

    abstract public void open(String prefix, String run, String attack) throws IOException;

    public void onNewServerClientChannel(HermesOrchestrationServerClientChannel clientChannel) {
        try {
            m_lock.lock();
            if (HermesConfig.DEBUG) {
                Logger.getLogger(OrchestrationNodeDaemon.class.getName()).
                        log(Level.INFO, "OrchestrationNodeDaemon: onNewServerClientChannel(): add client ->{0}<-", clientChannel.getID());
            }
            OrchestrationNodeServerClient serverClient = new OrchestrationNodeServerClient(this, clientChannel);
            clientChannel.setClient(serverClient);
            m_clientMap.put(serverClient.getID(), serverClient);
        } finally {
            m_lock.unlock();
        }
    }

    public void onServerClientChannelClose(HermesOrchestrationServerClientChannel client) {
        if (HermesConfig.DEBUG) {
            Logger.getLogger(OrchestrationNodeDaemon.class.getName()).
                    log(Level.INFO, "OrchestrationNodeDaemon: onServerClientChannelClose(): Removing client {0}", client.getID());
        }
        try {
            m_lock.lock();
            m_clientMap.remove(client.getID());


        } catch (Exception ex) {
            Logger.getLogger(OrchestrationNodeDaemon.class.getName()).
                    log(Level.INFO, "OrchestrationNodeDaemon: onServerClientChannelClose(): EX:", ex.toString());
            ex.printStackTrace();
        } finally {
            m_lock.unlock();
            if (HermesConfig.DEBUG) {
                Logger.getLogger(OrchestrationNodeDaemon.class.getName()).
                        log(Level.INFO, "OrchestrationNodeDaemon: onServerClientChannelClose(): Removed client {0}", client.getID());
            }
        }
    }
}
