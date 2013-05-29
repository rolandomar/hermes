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
package hermes.orchestration.bft;

import hermes.HermesConfig;
import hermes.deployment.network.HermesNodeServerChannel;
import hermes.orchestration.OrchestrationNodeDaemon;
import hermes.orchestration.OrchestrationNodeServerClient;
import hermes.orchestration.actions.Action;
import hermes.orchestration.actions.ActionResult;
import hermes.orchestration.actions.ActionResultImpl;
import hermes.orchestration.actions.BooleanWrapperActionResult;
import hermes.orchestration.actions.CheckFaultInjectionAction;
import hermes.orchestration.actions.CheckFaultInjectionActionResult;
import hermes.orchestration.actions.SpeedBumpActionResult;
import hermes.orchestration.network.HermesOrchestrationServerChannel;
import hermes.orchestration.notification.Notification;
import hermes.orchestration.notification.RuntimeJoinNotification;
import hermes.orchestration.notification.RuntimeLeaveNotification;
import hermes.network.HermesFuture;
import hermes.deployment.DeploymentClient;
import hermes.deployment.network.EnvElement;
import hermes.orchestration.actions.ActionImpl;
import hermes.orchestration.network.HermesOrchestrationServerClientChannel;
import hermes.orchestration.notification.StatNotification;
import hermes.runtime.HermesFault;
import hermes.runtime.faultinjection.CPULoaderFaultAlgorithm;
import hermes.runtime.faultinjection.CPULoaderFaultDescription;
import hermes.runtime.faultinjection.CrashFault;
import hermes.runtime.faultinjection.NetworkDropperFault;
import hermes.runtime.faultinjection.NetworkDropperFaultDescription;
import hermes.runtime.faultinjection.bft.BFTAttackFault;
import hermes.runtime.faultinjection.bft.BFTDelayPacketFault;
import hermes.runtime.faultinjection.bft.BFTDelayPacketFaultDescription;
import hermes.runtime.faultinjection.bft.BFTForgePayloadFault;
import hermes.serialization.HermesSerializableHelper;
import hermes.stats.Stats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class BFTOrchestrationDaemon extends OrchestrationNodeDaemon {

    final static String HERMES_HOMES = "/home/rmartins/HermesLauncher";
    final static String HERMES_COMMAND = HERMES_HOMES + "/Hermes.jar";
    final static String BFTSMART_HOMES = "/home/rmartins/Projects/BFT-SMaRt_Hermes";
    //final static String BFTSMART_COMMAND = BFTSMART_HOMES +
    boolean m_startingNewTest = false;
    boolean m_endingTest = false;
    int m_noOfRuntimes = 0;
    int m_allowRunToStartWithNPeers = 1;
    CountDownLatch m_startLatch = new CountDownLatch(m_allowRunToStartWithNPeers);
    //CyclicBarrier barrier = new CyclicBarrier(m_allowRunToStartWithNPeers);
    HermesOrchestrationServerChannel m_serverChannel = new HermesOrchestrationServerChannel(this);
    Map<String, PendingFault> m_pendingFaults = new HashMap<>();
    //<id,pf>
    Map<String, Map<String, PendingFault>> m_pendingFaultsPerClient = new HashMap<>();
    //Node control
    Map<String, DeploymentClient> m_currentReplicas = new HashMap<>();
    DeploymentClient m_currentClient = null;
    public volatile boolean m_stopFlag = false;
    Stats m_stats = new Stats();

    public boolean isStopped() {
        return m_stopFlag;
    }

    protected boolean waitForRunStartConditions(long timeout) {
        boolean result = false;
        try {
            m_startLatch.await(timeout, TimeUnit.MILLISECONDS);
            result = true;
        } catch (InterruptedException ex) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE,
                    "P3OrchestrationDaemon: failed tLoggero start run!", ex);
        }
        return result;
    }

    /**
     *
     * @param client
     * @param action
     * @return
     */
    @Override
    public ActionResult onAction(OrchestrationNodeServerClient client, Action action) {
        switch (action.getSerialID()) {
            //example of specialized action
            case CheckFaultInjectionAction.SERIAL_ID: {
                //System.out.println("CheckFaultInjectionAction: action:"+action.toString());
                CheckFaultInjectionAction cfa = (CheckFaultInjectionAction) action;
                String faultID = cfa.getFaultID();
                PendingFault pf = null;

                try {
                    m_lock.lock();
                    pf = m_pendingFaults.get(faultID);
                } finally {
                    m_lock.unlock();
                }
                //if (HermesConfig.DEBUG) {
                //    Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                //            log(Level.INFO, "CheckFaultInjectionAction={0} from {1} latch={2}", new Object[]{faultID, client.getID(), pf.m_latch.getCount()});
                //}
                //if (pf.m_type[0] == BFTAttackFault.SERIAL_ID) {
                //System.out.println("RIN=" + cfa.getFaultContext().getRun());
                if (cfa.getFaultContext().getRun() < 500) {
                    //System.out.println("Still not inject fault");                                        
                    return new CheckFaultInjectionActionResult(false);
                }
                //}

                try {
                    boolean flag = false;

                    try {
                        m_lock.lock();
                        if (pf.m_latch.getCount() == 1) {
                            pf.m_future.put(true);
                            flag = true;
                        }
                    } finally {
                        m_lock.unlock();
                    }
                    Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                            log(Level.SEVERE, "onNotification: client:" + client.getID() + " " + pf.m_latch.getCount()
                            + " RUN3:" + cfa.getFaultContext().getRun());
                    pf.m_latch.countDown();
                    boolean ret = pf.m_latch.await(100000, TimeUnit.MILLISECONDS);
                    Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                            log(Level.SEVERE, "onNotification: client:" + client.getID() + " after latch await ret=" + ret
                            + " RUN:" + cfa.getFaultContext().getRun());
//                    if (flag) {
//                        
//                        try {
//                            m_lock.lock();
//                            pf.reset();
//                        } finally {
//                            m_lock.unlock();
//                        }
//                        //pf.reset();
//                    }
                    return new CheckFaultInjectionActionResult(ret);
                } catch (InterruptedException ex) {
                    pf.m_future.put(false);
                    if (HermesConfig.DEBUG) {
                        Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                                log(Level.SEVERE, "CheckFaultInjectionAction nok");
                    }
                    return new CheckFaultInjectionActionResult(false);
                }
            }
            //General action implementation
            case ActionImpl.ACTIONIMPL_SERIALID: {
                if (action.getAction().compareToIgnoreCase("start_run") == 0) {
                    //barrier
                    boolean result = true;
                    //check if it is the client
                    if (client.getID().compareTo("1001") != 0) {
                        result = waitForRunStartConditions(60000);
                    }

                    BooleanWrapperActionResult temp = new BooleanWrapperActionResult(result);
                    return new ActionResultImpl(temp.getBytes());
                } else {
                    if (action.getAction().compareToIgnoreCase("perform_fault:1") == 0) {
                        BooleanWrapperActionResult temp = new BooleanWrapperActionResult(true);
                        return new ActionResultImpl(temp.getBytes());
                    } else {
                        if (action.getAction().compareToIgnoreCase("speed_bump") == 0) {
                            return new SpeedBumpActionResult(50);
                        }
                    }
                    return null;
                }
            }
        }
        return null;

    }

    /**
     *
     * @param client
     * @param notification
     */
    @Override
    public void onNotification(OrchestrationNodeServerClient client, Notification notification) {
//        if (false && HermesConfig.DEBUG) {
//            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
//                    log(Level.INFO, "OrchestrationNodeDaemon: onNotification(): {0}",
//                    notification.toString());
//        }
        switch (notification.getSerialID()) {
            case RuntimeJoinNotification.RUNTIMEJOINNOTIFICATION_SERIALID: {
                if (!m_startingNewTest) {
                    m_startingNewTest = true;
                }
                m_noOfRuntimes++;
                if (client.getID().compareTo("1001") != 0) {
                    Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                            log(Level.SEVERE,
                            "P3OrchestrationDaemon: join {0} peers active, latch {1} ",
                            new Object[]{m_noOfRuntimes, m_startLatch.getCount()});
                    m_startLatch.countDown();
                } else {
                    Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                            log(Level.SEVERE,
                            "P3OrchestrationDaemon: join not client, so counting");
                }
                if (HermesConfig.DEBUG) {
                    Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                            log(Level.INFO,
                            "P3OrchestrationDaemon: join {0} peers active, latch {1} ",
                            new Object[]{m_noOfRuntimes, m_startLatch.getCount()});
                }
                break;
            }

            case RuntimeLeaveNotification.RUNTIMELEAVENOTIFICATION_SERIALID: {
                if (client.getID().compareToIgnoreCase("1001") == 0) {
                    Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                            log(Level.INFO,
                            "P3OrchestrationDaemon: CLIENT LEFT, RUN DONE!",
                            m_noOfRuntimes);
                    m_stopFlag = true;
                }
                m_noOfRuntimes--;
                if (HermesConfig.DEBUG) {
                    Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                            log(Level.INFO,
                            "P3OrchestrationDaemon: leave {0} peers active",
                            m_noOfRuntimes);
                }

                break;
            }

            case StatNotification.STATNOTIFICATION_SERIALID: {
                StatNotification sn = (StatNotification) notification;
                if (HermesConfig.DEBUG) {
                    Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                            log(Level.SEVERE,
                            "P3OrchestrationDaemon: stat {0}",
                            sn.getStatToken().toString());
                }
                m_stats.collect(sn.getStatToken());

                if (sn.getStatToken().m_i == 999) {
                    Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                    log(Level.SEVERE, "OnNotification: stopping with invocation 999");
                    stop();
                }
                break;
            }
        }

    }

    private void stop() {
        Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                    log(Level.INFO, "BFTOrchestrationDaemon: stop()");
        m_stats.end();
        m_stopFlag = true;
        //Map<String, PendingFault>
        Set<Map.Entry<String, PendingFault>> faults = m_pendingFaults.entrySet();
        Iterator<Map.Entry<String, PendingFault>> iter = faults.iterator();
        while (iter.hasNext()) {
            iter.next().getValue().cancel();
        }
    }

    /**
     *
     * @throws IOException
     */
    @Override
    public void open(String prefix, String run, String attack) throws IOException {
        if (HermesConfig.DEBUG) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                    log(Level.INFO, "BFTOrchestrationDaemon: open(): starting daemon@{0}:{1}",
                    new Object[]{HermesConfig.getOrchestrationDaemonIP(), HermesConfig.getOrchestrationDaemonPort()});
        }
        m_serverChannel.open(HermesConfig.getOrchestrationDaemonIP(),
                HermesConfig.getOrchestrationDaemonPort());
        m_stats.setPrefix(prefix, run, attack);
    }

    /**
     * Injects a fault in clients defined in
     * <code>ids[]</code>
     *
     * @param ids IDs for the clients to be injected
     * @param fault fault to be injected
     * @return a future for asynchronous notification of completion
     * @throws Exception throw if ids.length != faults.length or operation fails
     */
    public HermesFuture<Boolean> simultaneousFaultInjection(String[] ids, HermesFault fault) throws Exception {
        //Implicit 2PC
        String faultID = fault.getFaultID();
        CountDownLatch latch = new CountDownLatch(ids.length);
        HermesFuture<Boolean> future = new HermesFuture<>();
        m_pendingFaults.put(faultID, new PendingFault(faultID, fault.getSerialID(), latch, future));
        for (int i = 0; i < ids.length; i++) {
            Map<String, PendingFault> cmap = m_pendingFaultsPerClient.get(ids[i]);
            if (cmap == null) {
                cmap = new HashMap<>();
                m_pendingFaultsPerClient.put(ids[i], cmap);
            }
            cmap.put(ids[i], new PendingFault(faultID, fault.getSerialID(), latch, future));
            OrchestrationNodeServerClient client = m_clientMap.get(ids[i]);
            try {
                client.addFault(fault);
            } catch (Exception ex) {
                m_pendingFaults.remove(faultID);
                m_pendingFaultsPerClient.remove(faultID);
                throw ex;
            }
        }
        return future;
    }

    /**
     * Injects different faults on different clients, with faults[i] being
     * injected in ids[i], with i > 0 and i < n
     *
     * @param ids IDs of the clients to be inject with the faults
     * @param faults faults to be injected
     * @return a future for asynchronous notification of completion
     * @throws Exception throw if ids.length != faults.length or operation fails
     */
    public HermesFuture<Boolean> simultaneousFaultInjection(String[] ids, HermesFault[] faults) throws Exception {
        //2PC        

        CountDownLatch latch = new CountDownLatch(ids.length);
        HermesFuture<Boolean> future = new HermesFuture<>();
        for (int i = 0; i < ids.length; i++) {
            String faultID = faults[i].getFaultID();
            m_pendingFaults.put(faultID, new PendingFault(faultID, faults, latch, future));
            Map<String, PendingFault> cmap = m_pendingFaultsPerClient.get(ids[i]);
            if (cmap == null) {
                cmap = new HashMap<>();
                m_pendingFaultsPerClient.put(ids[i], cmap);
            }

            cmap.put(ids[i], new PendingFault(faultID, faults, latch, future));
            System.out.println("-------------inject client" + m_clientMap.get(ids[i]));
            OrchestrationNodeServerClient client = m_clientMap.get(ids[i]);
            System.out.println("---------------------injected client" + m_clientMap.get(ids[i]));
            try {
                client.addFault(faults[i]);
            } catch (Exception ex) {
                m_pendingFaults.remove(faultID);
                m_pendingFaultsPerClient.remove(faultID);
                throw ex;
            }
        }

        return future;
    }

    /**
     * Injects a fault in a client
     *
     * @param nodeID ID for the client being injected
     * @param fault fault to be injected
     * @return a future for asynchronous notification of completion
     * @throws Exception throw if operation fails
     */
    public HermesFuture<Boolean> injectFault(String nodeID, HermesFault fault) throws Exception {
        String faultID = fault.getFaultID();
        CountDownLatch latch = new CountDownLatch(1);
        HermesFuture<Boolean> future = new HermesFuture<>();
        m_pendingFaults.remove(faultID);
        m_pendingFaults.put(faultID, new PendingFault(faultID, fault.getSerialID(), latch, future));
        OrchestrationNodeServerClient client = m_clientMap.get(nodeID);
        System.out.println("Injecting fault " + fault + " in " + client.getID());
        try {
            Map<String, PendingFault> cmap = m_pendingFaultsPerClient.get(nodeID);
            if (cmap == null) {
                cmap = new HashMap<>();
                m_pendingFaultsPerClient.put(nodeID, cmap);
            }
            cmap.put(nodeID, new PendingFault(faultID, fault.getSerialID(), latch, future));
            client.addFault(fault);
        } catch (Exception ex) {
            m_pendingFaults.remove(faultID);
            m_pendingFaultsPerClient.remove(faultID);
            throw ex;
        }
        return future;
    }

    /**
     *
     * @param timeout
     * @return
     * @throws Exception
     */
    public String[] launchNodes(long timeout) throws Exception {
        int groupSize = HermesConfig.getGroupSize();
        m_allowRunToStartWithNPeers = groupSize;
        //latch to control start
        m_startLatch = new CountDownLatch(m_allowRunToStartWithNPeers);
        if (HermesConfig.DEBUG) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                    log(Level.INFO, "P3OrchestrationDaemon: launchNodes: latch with {0}", m_allowRunToStartWithNPeers);
        }
        //
        String[] ids = new String[groupSize];
        String[] ips = new String[groupSize];
        for (int i = 0; i < groupSize; i++) {
            String ip = HermesConfig.getNodeIP(i);
            String id = HermesConfig.getNodeID(i);
            int port = HermesConfig.getNodeDaemonPort();
            startNode(timeout, ip, port, id);
        }
        return ids;
    }

    public void launchClientNode(long timeout) throws Exception {
        String ip = HermesConfig.getNodeClientIP();
        String id = HermesConfig.getNodeClientID();
        int port = HermesConfig.getNodeDaemonPort();
        startClientNode(timeout, ip, port, id);
    }

    /**
     *
     * @param timeout
     * @param ip
     * @param port
     * @param id
     * @throws Exception
     */
    protected void startNode(long timeout, String ip, int port, String id) throws Exception {
        if (HermesConfig.DEBUG) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                    log(Level.INFO, "P3OrchestrationDaemon: startNode({0} {1} {2})",
                    new Object[]{ip, port, id});
        }
        DeploymentClient client = new DeploymentClient(ip, port, id);
        client.open(timeout);
        m_currentReplicas.put(id, client);
    }

    protected void startClientNode(long timeout, String ip, int port, String id) throws Exception {
        if (HermesConfig.DEBUG) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                    log(Level.INFO, "P3OrchestrationDaemon: startClientNode({0} {1} {2})",
                    new Object[]{ip, port, id});
        }
        DeploymentClient client = new DeploymentClient(ip, port, id);
        client.open(timeout);
        m_currentClient = client;
    }

    /**
     * Launch remotely the application
     *
     * @param clientId clientID for the node where the application is about to
     * be launched
     * @param binary The application to be launched
     * @param args The arguments to be used in the launch
     * @param env The environment variables to be set before the launch
     * @param timeout The maximum time allow to the operation to complete
     * @return
     */
    protected boolean launchApplication(String clientId,
            String binary, String[] args, EnvElement[] env, long timeout) {
        DeploymentClient client = m_currentReplicas.get(clientId);
        if (client == null) {
            return false;
        }
        return client.bootstrapApplication(binary, args, env, timeout);
    }

    /**
     * Launch the Hermes on all connected clients
     *
     * @param binary The application to be launched
     * @param args The arguments to be used in the launch
     * @param env The environment variables to be set before the launch
     * @param timeout The maximum time allow to the operation to complete
     * @return true, if success, false, otherwise
     * @throws Exception
     */
    public boolean launchHermesReplicas(String binary, String[] args,
            EnvElement[] env, long timeout) throws Exception {
        Collection<DeploymentClient> clients = m_currentReplicas.values();
        if (clients.isEmpty()) {
            return false;
        }
        /*for (DeploymentClient c : clients) {
         String[] command = new String[args.length + 1];
         command[args.length] = c.getID();
         System.arraycopy(args, 0, command, 0, args.length);
         if (!c.bootstrapApplication(binary, command, env, timeout)) {
         Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
         log(Level.SEVERE, "P3OrchestrationDaemon: launchHermes: failed");
         return false;
         }
         }*/
        int size = HermesConfig.getGroupSize();
        for (int i = 0; i < size; i++) {
            DeploymentClient c = m_currentReplicas.get(Integer.toString(i));
            String[] command = new String[args.length + 1];
            command[args.length] = c.getID();
            System.arraycopy(args, 0, command, 0, args.length);
            if (!c.bootstrapApplication(binary, command, env, timeout)) {
                Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                        log(Level.SEVERE, "P3OrchestrationDaemon: launchHermes: failed");
                return false;
            }
            Thread.sleep(500);
        }

        if (HermesConfig.DEBUG) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                    log(Level.INFO, "P3OrchestrationDaemon: launchApplicationInNodes: waiting for all nodes");
        }
        if (m_startLatch.await(timeout, TimeUnit.MILLISECONDS) == false) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                    log(Level.SEVERE, "P3OrchestrationDaemon: launchApplicationInNodes: failed");
            return false;
        }
        if (HermesConfig.DEBUG) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                    log(Level.INFO, "P3OrchestrationDaemon: launchApplicationInNodes: started");
        }

        return true;
    }

    public boolean launchHermesClient(String binary, String[] args,
            EnvElement[] env, long timeout) throws Exception {

        if (!m_currentClient.bootstrapApplication(binary, args, env, timeout)) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).
                    log(Level.SEVERE, "P3OrchestrationDaemon: launchHermesClient: failed");
            return false;
        }
        return true;
    }

    /**
     * Returns the list of connected clients
     *
     * @return list of connected clients
     */
    public Collection<OrchestrationNodeServerClient> getConnectedClients() {
        return getClientMap().values();
    }

    /**
     * Returns the list of connected clients
     *
     * @return list of connected clients
     */
    public Collection<OrchestrationNodeServerClient> getClients(String[] ids) throws Exception {
        List<OrchestrationNodeServerClient> l = new ArrayList<>();
        int i = 0;
        for (String id : ids) {
            OrchestrationNodeServerClient c = getClientMap().get(id);
            if (c == null) {
                throw new Exception("Unknown client");
            }
            l.add(c);
        }
        return l;
    }

    private void dump() {
        m_stats.dump();
    }

    private void startTimer() {
        m_stats.start();
    }

//    private void endTimer() {
//        m_stats.end();
//    }

    /**
     * Helper class for injecting faults
     *
     */
    public class PendingFault {

        public String m_id;
        public int[] m_type;
        public CountDownLatch m_latch;
        public HermesFuture<Boolean> m_future;
        long ids = 0;
//
        //public void reset() {
        //        m_latch = new CountDownLatch((int) ids);
        //      }

        public PendingFault(String id, int type, CountDownLatch latch, HermesFuture<Boolean> future) {
            m_id = id;
            m_type = new int[]{type};
            m_latch = latch;
            m_future = future;
            ids = latch.getCount();
        }

        public PendingFault(String id, HermesFault[] faults, CountDownLatch latch, HermesFuture<Boolean> future) {
            m_id = id;
            int size = faults.length;
            m_type = new int[size];
            for (int i = 0; i < faults.length; i++) {
                m_type[i] = faults[i].getSerialID();
            }
            m_latch = latch;
            m_future = future;
            ids = latch.getCount();
        }

        public PendingFault(String id, int[] type, CountDownLatch latch, HermesFuture<Boolean> future) {
            m_id = id;
            m_type = type;
            m_latch = latch;
            m_future = future;
            ids = latch.getCount();
        }

        public void cancel() {
            while (m_latch.getCount() > 0) {
                m_latch.countDown();
            }
            m_future.put(false);
        }
    }

    public void test1() {
        Collection<OrchestrationNodeServerClient> clients = getConnectedClients();
        if (clients.size() >= 2) {
            String[] ids = new String[clients.size()];
            int i = 0;
            for (OrchestrationNodeServerClient client : clients) {
                ids[i++] = client.getID();
            }
            String faultID = "2B4FA20ED54E4DA9B6B2A917D1FA723F";
            HermesFault[] faults = new HermesFault[2];
            faults[0] = new CrashFault(faultID);
            CPULoaderFaultDescription desc = new CPULoaderFaultDescription(2, 5000, 90);
            faults[1] = new CPULoaderFaultAlgorithm(faultID, desc, HermesFault.TRIGGER_ALWAYS);
            try {
                System.out.println("inject faults");
                HermesFuture<Boolean> future = simultaneousFaultInjection(ids,
                        faults);
                boolean ret = future.get(10000, TimeUnit.MILLISECONDS);
                System.out.println("injected fault with=" + ret);
            } catch (Exception ex) {
                Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void attack1(String[] m) {
        String[] ids = m;
        String faultID = "5B4FA20ED54E4DA9B6B2A917D1FA724F";
        HermesFault fault = new CrashFault(faultID);
        fault.getFaultContext().putStringValue("f", HermesSerializableHelper.stringsToString(ids));
        //HermesFault[] faults = new HermesFault[]{
        //    new CrashFault(faultID),
        //    new CrashFault(faultID)};
        //faults[1] = new CrashFault(faultID);
        //faults[1] = new CPULoaderFaultAlgorithm(faultID, desc, HermesFault.TRIGGER_ALWAYS);
        try {
            System.out.println("inject faults");
            HermesFuture<Boolean> future = simultaneousFaultInjection(ids,
                    fault);
            //faults);
            //Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Performed attack1");
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "\n\n\nlaunching client");
            String command = HermesConfig.getApplicationLaunch();
            String[] clientappargs = new String[]{"-Xmx1024m", "bftsmart.demo.counter.CounterClient", "1001", "1", "1000"};
            launchHermesClient(command, clientappargs, null, 5000);
            startTimer();
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "finished client");

            boolean ret = future.get(300, TimeUnit.SECONDS);
            System.out.println("injected fault with=" + ret);
        } catch (Exception ex) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, null, ex);
            m_stopFlag = true;
        }

    }

    public void attack2(String[] m) {
        String[] ids = m;
        String faultID = "5B4FA20ED54E4DA9B6B2A917D1FA724F";

        try {
            System.out.println("inject faults");
            //SHORT_TIMEOUT = 2000 * 5
            BFTDelayPacketFaultDescription desc = new BFTDelayPacketFaultDescription(BFTDelayPacketFaultDescription.MOD_SEQ,
                    10);
            //5);
            BFTDelayPacketFault fault = new BFTDelayPacketFault(faultID, desc, HermesFault.TRIGGER_ALWAYS);
            fault.getFaultContext().putStringValue("f", HermesSerializableHelper.stringsToString(ids));
            for (int i = 0; i < m.length; i++) {
                System.out.println("inject faults:" + m[i] + " fault:" + fault);
                injectFault(m[i], fault);
            }
            //HermesFuture<Boolean> future = simultaneousFaultInjection(ids,
            //      fault);
            //Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Performed attack1");
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "\n\n\nlaunching client");
            String command = HermesConfig.getApplicationLaunch();
            String[] clientappargs = new String[]{"bftsmart.demo.counter.CounterClient", "1001", "1", "1000"};
            launchHermesClient(command, clientappargs, null, 5000);
            startTimer();
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "finished client");

            //boolean ret = future.get(100000, TimeUnit.MILLISECONDS);
            //System.out.println("injected fault with=" + ret);
        } catch (Exception ex) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, null, ex);
            m_stopFlag = true;
        }

    }

    public void attack3(String[] m) {
        String[] ids = m;
        String faultID = "5B4FA20ED54E4DA9B6B2A917D1FA724F";

        try {
            System.out.println("inject faults");
            BFTDelayPacketFaultDescription desc = new BFTDelayPacketFaultDescription(BFTDelayPacketFaultDescription.ALL,
                    10);
            //5);
            BFTDelayPacketFault fault = new BFTDelayPacketFault(faultID, desc, HermesFault.TRIGGER_ALWAYS);
            fault.getFaultContext().putStringValue("f", HermesSerializableHelper.stringsToString(ids));
            //HermesFuture<Boolean> future = simultaneousFaultInjection(ids,
            //        fault);
            for (int i = 0; i < m.length; i++) {
                injectFault(m[i], fault);
            }

            //Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Performed attack1");
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "\n\n\nlaunching client");
            String command = HermesConfig.getApplicationLaunch();
            String[] clientappargs = new String[]{"bftsmart.demo.counter.CounterClient", "1001", "1", "1000"};
            launchHermesClient(command, clientappargs, null, 5000);
            startTimer();
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "finished client");

            //boolean ret = future.get(100000, TimeUnit.MILLISECONDS);
            //System.out.println("injected fault with=" + ret);
        } catch (Exception ex) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, null, ex);
            m_stopFlag = true;
        }

    }

    public void attack4(String[] m) {
        String[] ids = m;
        String faultID = "5B4FA20ED54E4DA9B6B2A917D1FA724F";

        try {
            System.out.println("inject faults");
            BFTForgePayloadFault fault =
                    new BFTForgePayloadFault(faultID, HermesFault.TRIGGER_ALWAYS, BFTForgePayloadFault.MAXINT);
            fault.getFaultContext().putStringValue("f", HermesSerializableHelper.stringsToString(ids));
            //HermesFuture<Boolean> future = simultaneousFaultInjection(ids,
            //        fault);
            for (int i = 0; i < m.length; i++) {
                injectFault(m[i], fault);
            }

            //Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Performed attack1");
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "\n\n\nlaunching client");
            String command = HermesConfig.getApplicationLaunch();
            String[] clientappargs = new String[]{"bftsmart.demo.counter.CounterClient", "1001", "1", "1000"};
            launchHermesClient(command, clientappargs, null, 5000);
            startTimer();
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "finished client");

            //boolean ret = future.get(100000, TimeUnit.MILLISECONDS);
            //System.out.println("injected fault with=" + ret);
        } catch (Exception ex) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, null, ex);
            m_stopFlag = true;
        }

    }

    public void attack5(String[] m) {
        String[] ids = m;
        String faultID = "5B4FA20ED54E4DA9B6B2A917D1FA724F";

        try {
            System.out.println("inject faults");
            BFTForgePayloadFault fault =
                    new BFTForgePayloadFault(faultID, HermesFault.TRIGGER_ALWAYS, BFTForgePayloadFault.NEGATIVE);
            fault.getFaultContext().putStringValue("f", HermesSerializableHelper.stringsToString(ids));
            //HermesFuture<Boolean> future = simultaneousFaultInjection(ids,
            //        fault);
            for (int i = 0; i < m.length; i++) {
                injectFault(m[i], fault);
            }

            //Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Performed attack1");
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "\n\n\nlaunching client");
            String command = HermesConfig.getApplicationLaunch();
            String[] clientappargs = new String[]{"bftsmart.demo.counter.CounterClient", "1001", "1", "1000"};
            launchHermesClient(command, clientappargs, null, 5000);
            startTimer();
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "finished client");

            //boolean ret = future.get(100000, TimeUnit.MILLISECONDS);
            //System.out.println("injected fault with=" + ret);
        } catch (Exception ex) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, null, ex);
            m_stopFlag = true;
        }

    }

    public void attack6(String[] m) {
        String[] ids = m;
        String faultID = "5B4FA20ED54E4DA9B6B2A917D1FA724F";

        try {
            System.out.println("inject faults");
            BFTForgePayloadFault fault =
                    new BFTForgePayloadFault(faultID, HermesFault.TRIGGER_ALWAYS, BFTForgePayloadFault.CORRUPT_6);
            fault.getFaultContext().putStringValue("f", HermesSerializableHelper.stringsToString(ids));
            //HermesFuture<Boolean> future = simultaneousFaultInjection(ids,
            //        fault);
            for (int i = 0; i < m.length; i++) {
                injectFault(m[i], fault);
            }

            //Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Performed attack1");
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "\n\n\nlaunching client");
            String command = HermesConfig.getApplicationLaunch();
            String[] clientappargs = new String[]{"bftsmart.demo.counter.CounterClient", "1001", "1", "1000"};
            launchHermesClient(command, clientappargs, null, 5000);
            startTimer();
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "finished client");

            //boolean ret = future.get(100000, TimeUnit.MILLISECONDS);
            //System.out.println("injected fault with=" + ret);
        } catch (Exception ex) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, null, ex);
            m_stopFlag = true;
        }

    }

    public void attack7(String[] m) {
        String[] ids = m;
        String faultID = "5B4FA20ED54E4DA9B6B2A917D1FA724F";

        try {
            System.out.println("inject faults");
            BFTForgePayloadFault fault =
                    new BFTForgePayloadFault(faultID, HermesFault.TRIGGER_ALWAYS, BFTForgePayloadFault.CORRUPT_7);
            fault.getFaultContext().putStringValue("f", HermesSerializableHelper.stringsToString(ids));
            //HermesFuture<Boolean> future = simultaneousFaultInjection(ids,
            //        fault);
            for (int i = 0; i < m.length; i++) {
                injectFault(m[i], fault);
            }

            //Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Performed attack1");
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "\n\n\nlaunching client");
            String command = HermesConfig.getApplicationLaunch();
            String[] clientappargs = new String[]{"bftsmart.demo.counter.CounterClient", "1001", "1", "1000"};
            launchHermesClient(command, clientappargs, null, 5000);
            startTimer();
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "finished client");

            //boolean ret = future.get(100000, TimeUnit.MILLISECONDS);
            //System.out.println("injected fault with=" + ret);
        } catch (Exception ex) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, null, ex);
            m_stopFlag = true;
        }

    }

    public void attack8(String[] m) {
        String[] ids = m;
        String faultID = "5B4FA20ED54E4DA9B6B2A917D1FA724F";

        try {
            System.out.println("inject faults");
            BFTForgePayloadFault fault =
                    new BFTForgePayloadFault(faultID, HermesFault.TRIGGER_ALWAYS, BFTForgePayloadFault.CORRUPT_8);
            fault.getFaultContext().putStringValue("f", HermesSerializableHelper.stringsToString(ids));
            //HermesFuture<Boolean> future = simultaneousFaultInjection(ids,
            //        fault);
            for (int i = 0; i < m.length; i++) {
                injectFault(m[i], fault);
            }

            //Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Performed attack1");
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "\n\n\nlaunching client");
            String command = HermesConfig.getApplicationLaunch();
            String[] clientappargs = new String[]{"bftsmart.demo.counter.CounterClient", "1001", "1", "1000"};
            launchHermesClient(command, clientappargs, null, 5000);
            startTimer();
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "finished client");

            //boolean ret = future.get(100000, TimeUnit.MILLISECONDS);
            //System.out.println("injected fault with=" + ret);
        } catch (Exception ex) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, null, ex);
            m_stopFlag = true;
        }

    }

    //public void attack9(String node0, String node1) {
    public void attack9(String[] m) {
        String[] ids = m;
        String faultID = "5B4FA20ED54E4DA9B6B2A917D1FA724F";

        try {
            System.out.println("inject faults");
            BFTForgePayloadFault fault =
                    new BFTForgePayloadFault(faultID, HermesFault.TRIGGER_ALWAYS, BFTForgePayloadFault.CORRUPT_9);
            fault.getFaultContext().putStringValue("f", HermesSerializableHelper.stringsToString(ids));
            //HermesFuture<Boolean> future = simultaneousFaultInjection(ids,
            //        fault);
            for (int i = 0; i < m.length; i++) {
                injectFault(m[i], fault);
            }

            //Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Performed attack1");
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "\n\n\nlaunching client");
            String command = HermesConfig.getApplicationLaunch();
            String[] clientappargs = new String[]{"bftsmart.demo.counter.CounterClient", "1001", "1", "1000"};
            launchHermesClient(command, clientappargs, null, 5000);
            startTimer();
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "finished client");

            //boolean ret = future.get(100000, TimeUnit.MILLISECONDS);
            //System.out.println("injected fault with=" + ret);
        } catch (Exception ex) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, null, ex);
            m_stopFlag = true;
        }

    }

    public void attack10(String[] m) {
        int half = m.length / 2;
        String[] ids1 = new String[half];
        String[] ids2 = new String[half];
        String faultID1 = "5B4FA20ED54E4DA9B6B2A917D1FA724F";
        String faultID2 = "5B4FA20ED54E4DA9B6B2A917D1FA724F";

        System.arraycopy(m, 0, ids1, 0, half);
        System.arraycopy(m, half, ids2, 0, half);

        try {
            System.out.println("inject faults");
            BFTForgePayloadFault fault1 =
                    new BFTForgePayloadFault(faultID1, HermesFault.TRIGGER_ALWAYS, BFTForgePayloadFault.CORRUPT_8);
            fault1.getFaultContext().putStringValue("f", HermesSerializableHelper.stringsToString(m));
            BFTForgePayloadFault fault2 =
                    new BFTForgePayloadFault(faultID2, HermesFault.TRIGGER_ALWAYS, BFTForgePayloadFault.CORRUPT_9);
            fault2.getFaultContext().putStringValue("f", HermesSerializableHelper.stringsToString(m));
            //HermesFuture<Boolean> future = simultaneousFaultInjection(ids,
            //        fault);
            for (int i = 0; i < half; i++) {
                injectFault(ids1[i], fault1);
                injectFault(ids2[i], fault2);
            }

            //Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Performed attack1");
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "\n\n\nlaunching client");
            String command = HermesConfig.getApplicationLaunch();
            String[] clientappargs = new String[]{"bftsmart.demo.counter.CounterClient", "1001", "1", "1000"};
            launchHermesClient(command, clientappargs, null, 5000);
            startTimer();
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "finished client");

            //boolean ret = future.get(100000, TimeUnit.MILLISECONDS);
            //System.out.println("injected fault with=" + ret);
        } catch (Exception ex) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, null, ex);
            m_stopFlag = true;
        }

    }

    public void attack11(String[] m) {
        int half = m.length / 2;
        String[] ids1 = new String[half];
        String[] ids2 = new String[half];
        String faultID1 = "5B4FA20ED54E4DA9B6B2A917D1FA724F";
        String faultID2 = "5B4FA20ED54E4DA9B6B2A917D1FA724F";

        System.arraycopy(m, 0, ids1, 0, half);
        System.arraycopy(m, half, ids2, 0, half);

        try {
            System.out.println("inject faults");
            BFTDelayPacketFaultDescription desc = new BFTDelayPacketFaultDescription(BFTDelayPacketFaultDescription.ALL, 5);
            BFTDelayPacketFault fault1 = new BFTDelayPacketFault(faultID1, desc, HermesFault.TRIGGER_ALWAYS);
            fault1.getFaultContext().putStringValue("f", HermesSerializableHelper.stringsToString(m));
            NetworkDropperFaultDescription desc2 = new NetworkDropperFaultDescription(50);
            NetworkDropperFault fault2 = new NetworkDropperFault(faultID2, desc2, HermesFault.TRIGGER_ALWAYS);
            fault2.getFaultContext().putStringValue("f", HermesSerializableHelper.stringsToString(m));
            //HermesFuture<Boolean> future = simultaneousFaultInjection(ids,
            //        fault);
            for (int i = 0; i < half; i++) {
                injectFault(ids1[i], fault1);
                injectFault(ids2[i], fault2);
            }

            //Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Performed attack1");
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "\n\n\nlaunching client");
            String command = HermesConfig.getApplicationLaunch();
            String[] clientappargs = new String[]{"bftsmart.demo.counter.CounterClient", "1001", "1", "1000"};
            launchHermesClient(command, clientappargs, null, 5000);
            startTimer();
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "finished client");

            //boolean ret = future.get(100000, TimeUnit.MILLISECONDS);
            //System.out.println("injected fault with=" + ret);
        } catch (Exception ex) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, null, ex);
            m_stopFlag = true;
        }

    }

    public void attack12(String[] m) {
        String[] ids = m;
        String faultID = "5B4FA20ED54E4DA9B6B2A917D1FA724F";

        try {
            System.out.println("inject faults");
            NetworkDropperFaultDescription desc = new NetworkDropperFaultDescription(1);
            NetworkDropperFault fault = new NetworkDropperFault(faultID, desc, HermesFault.TRIGGER_ALWAYS);
            fault.getFaultContext().putStringValue("f", HermesSerializableHelper.stringsToString(ids));
            //HermesFuture<Boolean> future = simultaneousFaultInjection(ids,
            //        fault);
            for (int i = 0; i < m.length; i++) {
                injectFault(m[i], fault);
            }

            //Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Performed attack1");
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "\n\n\nlaunching client");
            String command = HermesConfig.getApplicationLaunch();
            String[] clientappargs = new String[]{"bftsmart.demo.counter.CounterClient", "1001", "1", "1000"};
            launchHermesClient(command, clientappargs, null, 5000);
            startTimer();
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "finished client");

            //boolean ret = future.get(100000, TimeUnit.MILLISECONDS);
            //System.out.println("injected fault with=" + ret);
        } catch (Exception ex) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, null, ex);
            m_stopFlag = true;
        }

    }

    public void attack13(String[] m) {
        String[] ids = m;
        String faultID = "5B4FA20ED54E4DA9B6B2A917D1FA724F";

        try {
            System.out.println("inject faults");
            NetworkDropperFaultDescription desc = new NetworkDropperFaultDescription(50);
            NetworkDropperFault fault = new NetworkDropperFault(faultID, desc, HermesFault.TRIGGER_ALWAYS);
            fault.getFaultContext().putStringValue("f", HermesSerializableHelper.stringsToString(ids));
            //HermesFuture<Boolean> future = simultaneousFaultInjection(ids,
            //        fault);
            for (int i = 0; i < m.length; i++) {
                injectFault(m[i], fault);
            }

            //Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Performed attack1");
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "\n\n\nlaunching client");
            String command = HermesConfig.getApplicationLaunch();
            String[] clientappargs = new String[]{"bftsmart.demo.counter.CounterClient", "1001", "1", "1000"};
            launchHermesClient(command, clientappargs, null, 5000);
            startTimer();
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "finished client");

            //boolean ret = future.get(100000, TimeUnit.MILLISECONDS);
            //System.out.println("injected fault with=" + ret);
        } catch (Exception ex) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, null, ex);
            m_stopFlag = true;
        }

    }

    public void attack14(String[] m) {
        String[] ids = m;
        String faultID = "5B4FA20ED54E4DA9B6B2A917D1FA724F";

        try {
            System.out.println("inject faults");
            NetworkDropperFaultDescription desc = new NetworkDropperFaultDescription(75);
            NetworkDropperFault fault = new NetworkDropperFault(faultID, desc, HermesFault.TRIGGER_ALWAYS);
            fault.getFaultContext().putStringValue("f", HermesSerializableHelper.stringsToString(ids));
            //HermesFuture<Boolean> future = simultaneousFaultInjection(ids,
            //        fault);
            for (int i = 0; i < m.length; i++) {
                injectFault(m[i], fault);
            }

            //Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Performed attack1");
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "\n\n\nlaunching client");
            String command = HermesConfig.getApplicationLaunch();
            String[] clientappargs = new String[]{"bftsmart.demo.counter.CounterClient", "1001", "1", "1000"};
            launchHermesClient(command, clientappargs, null, 5000);
            startTimer();
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "finished client");

            //boolean ret = future.get(100000, TimeUnit.MILLISECONDS);
            //System.out.println("injected fault with=" + ret);
        } catch (Exception ex) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, null, ex);
            m_stopFlag = true;
        }

    }

    public void attack15(String[] m) {
        String[] ids = m;
        String faultID = "5B4FA20ED54E4DA9B6B2A917D1FA724F";

        try {
            System.out.println("inject faults");
            NetworkDropperFaultDescription desc = new NetworkDropperFaultDescription(100);
            NetworkDropperFault fault = new NetworkDropperFault(faultID, desc, HermesFault.TRIGGER_ALWAYS);
            fault.getFaultContext().putStringValue("f", HermesSerializableHelper.stringsToString(ids));
            //HermesFuture<Boolean> future = simultaneousFaultInjection(ids,
            //        fault);
            for (int i = 0; i < m.length; i++) {
                injectFault(m[i], fault);
            }

            //Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Performed attack1");
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "\n\n\nlaunching client");
            String command = HermesConfig.getApplicationLaunch();
            String[] clientappargs = new String[]{"bftsmart.demo.counter.CounterClient", "1001", "1", "1000"};
            launchHermesClient(command, clientappargs, null, 5000);
            startTimer();
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "finished client");

            //boolean ret = future.get(100000, TimeUnit.MILLISECONDS);
            //System.out.println("injected fault with=" + ret);
        } catch (Exception ex) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, null, ex);
            m_stopFlag = true;
        }

    }

    public static int getRandomPer(boolean nonZero, int max) {
        int i = randomGenerator.nextInt() % max;
        if (nonZero) {
            i++;
        }
        if (i < 0) {
            return i * (-1);
        }
        return i;
    }

    public static boolean checkIfInSet(String[] s, int n, int maxindex) {
        for (int i = 0; i < maxindex; i++) {
            if (Integer.parseInt(s[i]) == n) {
                return true;
            }
        }
        return false;
    }
    protected static Random randomGenerator = null;

    static {
        randomGenerator = new Random(System.currentTimeMillis());
    }

    public static String[] getRandomSet(boolean nonZero, int size) {
        //int groupSize = HermesConfig.getGroupSize() - 1;
        int groupSize = HermesConfig.getGroupSize();
        System.out.print("getRandomSet: groupSize=" + groupSize + " [");
        String[] s = new String[size];
        for (int i = 0; i < size; i++) {
            int n = getRandomPer(nonZero, groupSize);
            //System.out.println("random" + n);
            while (checkIfInSet(s, n, i)) {
                //System.out.println("n" + n);
                n = getRandomPer(nonZero, groupSize);
            }
            s[i] = Integer.toString(n);
            System.out.print(s[i]);
        }
        System.out.println("]");
        return s;
    }

    public static void printSet(int f, String[] s) {

        StringBuffer b = new StringBuffer();
        b.append("Configuration: " + f + "set[");
        for (int i = 0; i < s.length; i++) {
            b.append(" " + s[i]);
        }
        b.append("]\n");
        Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, b.toString());
    }

    public static void main(String[] args) {
        BFTOrchestrationDaemon daemon = new BFTOrchestrationDaemon();
        String r = args[2];
        String f = args[0];
        String a = args[1];
        System.out.println("f:" + f);
        System.out.println("attack:" + a);
        try {
            daemon.open(f, r, a);
        } catch (IOException ex) {
            Logger.getLogger(HermesNodeServerChannel.class.getName()).log(Level.SEVERE, "OOOO", ex);
        }
        long start = System.currentTimeMillis();
        int attack = Integer.parseInt(a);
        int faulty = Integer.parseInt(f);
//        boolean randomSet = false;
//        boolean onlyZero = false;
//        String[] m_1;
//        String[] m_2;
//        String[] m_3;
//        String[] m_4;
//        String[] m_8;
//
//        m_1 = new String[]{"0"};
//        m_2 = new String[]{"0", "1"};
//        m_3 = new String[]{
//            "0", "1", "2"};
//        m_4 = new String[]{
//            "0", "1", "2", "3"};
//        m_8 = new String[]{
//            "0", "1", "2", "3", "4", "5", "6", "7"};
//        if (randomSet) {
//            int fint = Integer.parseInt(f);
//            switch (fint) {
//                case 1:
//                    m_1 = getRandomSet(false, 1);
//                    break;
//                case 2:
//                    m_2 = getRandomSet(false, 2);
//                    break;
//                case 3:
//                    m_2 = getRandomSet(false, 3);
//                    break;
//                case 4:
//                    m_4 = getRandomSet(false, 4);
//                    break;
//                case 8:
//                    m_8 = getRandomSet(false, 8);
//                    break;
//            }
//        }
//        if (onlyZero) {
//            m_1 = new String[]{"0"};
//            m_2 = new String[]{"0"};
//            m_3 = new String[]{"0"};
//            m_4 = new String[]{"0"};
//            m_8 = new String[]{"0"};
//        }
//
//        printSet(2, m_2);
//        printSet(4, m_4);
//        printSet(8, m_8);

/////////////////////////////////////////////////////////////      
//0-f1_{0} 		
//1-f1_{x}
//----------------------------
//2-f2_{0}
//3-f2_{x}
//4-f2_{0,1}
//5-f2_{x,y}
//----------------------------
//6-f3_{0} 		
//7-f3_{x}
//8-f3_{0,1}	
//9-f3_{x,y} 		
//10-f3_{0,1,2}    	
//11-f3_{x,y,z}

//a1:
//crash
//
//a2:
//delay seq above timeout
//
//a3:
//delay all above timeout
//
//a4:
//max_int
//
//a5:
//min_int
//
//a6:
//corrupt all
//
//a7:              
//delay Propose messages under timeout
//
//a8:              
//delay Propose messages above timeout
//
//a9:
//corrupt DDOS
//////////////////////////////////////////////////////////////2



        Integer[] groupSizes = {
            //1f
            4, 4,
            //2f
            7, 7, 7, 7,
            //3f
            10, 10, 10, 10, 10, 10
        };
        HermesConfig.setGroupSize(groupSizes[faulty]);

        String[][] Configurations = {
            //f1
            new String[]{"0"},
            getRandomSet(false, 1),
            //f2
            new String[]{"0"},
            getRandomSet(false, 1),
            new String[]{"0", "1"},
            getRandomSet(false, 2),
            //f3
            new String[]{"0"},
            getRandomSet(false, 1),
            new String[]{"0", "1"},
            getRandomSet(false, 2),
            new String[]{"0", "1", "2"},
            getRandomSet(false, 3)
        };
        String[] m = Configurations[faulty];
        printSet(faulty, m);

        try {
            //connects to clients
            daemon.launchNodes(10000);
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "launched replicas");
            daemon.launchClientNode(10000);
            String command = HermesConfig.getApplicationLaunch();
            //String[] appargs = new String[]{"-cp", HERMES_HOMES + "/Hermes.jar", "hermes.bft.BFTNode"};
            String[] appargs = new String[]{"-Xmx1024m", "bftsmart.demo.counter.CounterServer"};
            //boostrap
            daemon.launchHermesReplicas(command, appargs, null, 5000);
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Performing attack1");
            switch (attack) {
                case 0: {
                    String[] clientappargs = new String[]{"bftsmart.demo.counter.CounterClient", "1001", "1", "1000"};
                    daemon.launchHermesClient(command, clientappargs, null, 5000);
                    daemon.startTimer();
                    Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "finished client");
                    break;
                }
                case 1: {
                    daemon.attack1(m);
                    break;
                }
                case 2: {
                    daemon.attack2(m);
                    break;
                }
                case 3: {
                    daemon.attack3(m);
                    break;
                }
                case 4: {
                    daemon.attack4(m);
                    break;
                }
                case 5: {
                    daemon.attack5(m);
                    break;
                }
                case 6: {
                    daemon.attack6(m);
                    break;
                }
                case 7: {
                    daemon.attack7(m);
                    break;
                }
                case 8: {
                    daemon.attack8(m);
                    break;
                }
                case 9: {
                    daemon.attack9(m);
                    break;
                }
                case 10: {
                    daemon.attack10(m);
                    break;
                }
                case 11: {
                    daemon.attack11(m);
                    break;
                }
                case 12: {
                    daemon.attack12(m);
                    break;
                }
                case 13: {
                    daemon.attack13(m);
                    break;
                }
                case 14: {
                    daemon.attack14(m);
                    break;
                }
                case 15: {
                    daemon.attack15(m);
                    break;
                }
            }

            //Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "\n\n\nlaunching client");
            //String[] clientappargs = new String[]{"bftsmart.demo.counter.CounterClient", "1001", "1", "1000"};
            //daemon.launchHermesClient(command, clientappargs, null, 5000);
            //Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "finished client");


        } catch (Exception ex) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, null, ex);
        }
        Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Going to loop");
        //daemon.test1();
        //Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Performed test1");

        try {
            //wait for things to finish
            float duration = 315;
            while (!daemon.m_stopFlag) {
                long end = System.currentTimeMillis() - start;
                duration = (float) end / (float) 1000;
                int l = ((int) duration) % 5;
                if (l == 0 || duration > 300) {
                    Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Run in progress for " + duration + " seconds");
                }
                if (duration > 315) {
                    break;
                }
                //Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Run in progress for " + duration + " seconds");
                Thread.sleep(1000);
            }

            Logger.getLogger(OrchestrationNodeDaemon.class.getName()).
                    log(Level.INFO, "OrchestrationNodeDaemon: stopped work!");
            if (duration > 300) {
                Logger.getLogger(OrchestrationNodeDaemon.class.getName()).
                        log(Level.SEVERE, "\n\nError Run\n\n");
            }
            daemon.dump();
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, "Shutdowning properly");
            System.exit(0);
        } catch (InterruptedException ex) {
            Logger.getLogger(BFTOrchestrationDaemon.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onServerClientChannelClose(HermesOrchestrationServerClientChannel client) {
        try {
            m_lock.lock();
            if (HermesConfig.DEBUG) {
                Logger.getLogger(OrchestrationNodeDaemon.class.getName()).
                        log(Level.INFO,
                        "BFTOrchestrationNodeDaemon: onServerClientChannelClose():Removed client {0}",
                        client.getID());
            }
            super.onServerClientChannelClose(client);
            if (HermesConfig.DEBUG) {
                Logger.getLogger(OrchestrationNodeDaemon.class.getName()).
                        log(Level.INFO,
                        "BFTOrchestrationNodeDaemon: onServerClientChannelClose():Removed client {0} after clean",
                        client.getID());
            }
            if (client.getID().compareToIgnoreCase("1001") == 0) {
                //if (HermesConfig.DEBUG) {
                    Logger.getLogger(OrchestrationNodeDaemon.class.getName()).
                            log(Level.SEVERE,
                            "BFTOrchestrationNodeDaemon: onServerClientChannelClose(): Ending computation - Removed client {0}",
                            client.getID());
               // }
                //endTimer();
                //this.m_stopFlag = true;
                stop();
                if (HermesConfig.DEBUG) {
                    Logger.getLogger(OrchestrationNodeDaemon.class.getName()).
                            log(Level.INFO,
                            "BFTOrchestrationNodeDaemon: onServerClientChannelClose(): Ending computation - Removed client {0} after setStop",
                            client.getID());
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(OrchestrationNodeDaemon.class.getName()).
                    log(Level.INFO,
                    "BFTOrchestrationNodeDaemon: onServerClientChannelClose(): EX:", ex.toString());
            ex.printStackTrace();
            if (client.getID().compareToIgnoreCase("1001") == 0) {
                //endTimer();
                //this.m_stopFlag = true;
                stop();
            }
        } finally {
            //releasing the lock so that other threads can get notifies
            m_lock.unlock();
            if (HermesConfig.DEBUG) {
                Logger.getLogger(OrchestrationNodeDaemon.class.getName()).
                        log(Level.INFO,
                        "BFTOrchestrationNodeDaemon: onServerClientChannelClose(): Ending computation - Removed client {0} After lock",
                        client.getID());
            }
        }
    }
}
