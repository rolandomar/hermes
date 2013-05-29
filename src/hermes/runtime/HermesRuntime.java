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
package hermes.runtime;

import hermes.deployment.network.HermesNodeServerChannel;
import hermes.orchestration.OrchestrationNodeClient;
import hermes.orchestration.actions.Action;
import hermes.orchestration.actions.ActionResult;
import hermes.orchestration.notification.Notification;
import hermes.orchestration.notification.RuntimeJoinNotification;
import hermes.orchestration.notification.RuntimeLeaveNotification;
import hermes.orchestration.remote.RemoteAction;
import hermes.orchestration.remote.RemoteActionResult;
import hermes.runtime.exception.AspectException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class HermesRuntime {

    List<HermesFault> m_aspects = new ArrayList<>();
    static final int MAXRUNTIME = 300 * 1000;
    protected static boolean isActive = true;
    public static HermesRuntime hermesRuntime = new HermesRuntime();
    protected HermesFaultManager m_aspectManager = new HermesFaultManager();
    protected OrchestrationNodeClient m_client = null;
    String m_id;
    HermesRuntimeContext m_runtimeCTX = new HermesRuntimeContext();
    //Remote action handler (user must provide it)
    RemoteActionHandler m_remoteActionHandler = new DefaultRemoteActionHandler(this);

    public void setID(String runtimeID) {
        m_id = runtimeID;
    }

    public HermesRuntimeContext getContext() {
        return m_runtimeCTX;
    }

    public static HermesRuntime getInstance() {
        return hermesRuntime;
    }

    public static String getRandomUUID() {
        final String uuid = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
        return uuid;
    }

    public HermesRuntime() {
        m_id = getRandomUUID();
    }

    public HermesRuntime(String id) {
        m_id = id;
    }

    public String getRuntimeID() {
        return m_id;
    }

    public void open() throws Exception {
        m_client = new OrchestrationNodeClient(this, m_id);
        m_client.open();
        System.out.println("HermesRuntime:open() - send join notficiation");
        RuntimeJoinNotification notification = new RuntimeJoinNotification();
        m_client.notification(notification, 2500);
        System.out.println("HermesRuntime:open() -after join notficiation");
        if (m_id.compareToIgnoreCase("1001") == 0) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(MAXRUNTIME);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(HermesRuntime.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.exit(-1);
                }
            }.start();
        }
    }

    public void close() {
        RuntimeLeaveNotification notification = new RuntimeLeaveNotification();
        System.out.println("HermesRuntime:close() - send leave notficiation");
        m_client.notification(notification, 2500);
        System.out.println("HermesRuntime:close() - received leave notficiation");
        try {
            m_client.close();
        } catch (IOException ex) {
            Logger.getLogger(HermesRuntime.class.getName()).log(Level.SEVERE, null, ex);
        }
        m_client = null;
    }

    //Done
    public void notification(Notification notification, long timeout) {
        m_client.notification(notification, timeout);
    }

    public ActionResult action(Action action, long timeout) throws Exception {
        return m_client.action(action, timeout);
    }

    /**
     *
     * @param remoteActionHandler
     */
    public void setRemoteActionHandler(RemoteActionHandler remoteActionHandler) {
        m_remoteActionHandler = remoteActionHandler;
    }

    public HermesFaultManager getFaultManager() {
        return m_aspectManager;
    }

    public static void main(String[] args) {
        System.out.println(HermesRuntime.getRandomUUID());
    }

    public RemoteActionResult onRemoteAction(RemoteAction remoteAction) {
        if (m_remoteActionHandler == null) {
            return null;
        }
        return m_remoteActionHandler.remoteAction(remoteAction);
    }
}
