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
package hermes.runtime.bft;

import hermes.HermesConfig;
import hermes.orchestration.actions.Action;
import hermes.orchestration.actions.SpeedBumpAction;
import hermes.orchestration.actions.SpeedBumpActionResult;
import hermes.orchestration.notification.Notification;
import hermes.orchestration.notification.SetStateNotification;
import hermes.runtime.HermesFault;
import hermes.runtime.HermesRuntime;
import hermes.runtime.faultinjection.CPULoaderFaultAlgorithm;
import hermes.runtime.faultinjection.CrashFault;
import hermes.runtime.faultinjection.NetworkCorrupterAlgorithm;
import hermes.runtime.faultinjection.ThreadDelayFault;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class BFTNodeOld {

    String m_cellID = null;
    HermesRuntime m_runtime = null;

    public BFTNodeOld() {
        m_runtime = new HermesRuntime();        
        Logger.getLogger(BFTNodeOld.class.getName()).log(Level.SEVERE, "BFTNode()");
    }

    /**
     *
     * @param id
     * @throws Exception
     */
    public void open(String id) throws Exception {
        Logger.getLogger(BFTNodeOld.class.getName()).log(Level.SEVERE, "BFTNode:open()");
        m_runtime.setID(id);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        Date date = new Date();
        dateFormat.format(date);
        String logName = HermesConfig.getWorkingDir() + "/node_" + m_runtime.getRuntimeID() + "_" + dateFormat.format(date) + ".log";
        HermesConfig.addLoggerFile(logName);
        m_runtime.open();
    }

    
    /**
     *
     * @return The current runtime
     */
    public HermesRuntime getRuntime() {
        return m_runtime;
    }

    
    /**
     *
     */
    public void onSetState() {
        Notification notification = new SetStateNotification(HermesRuntime.getRandomUUID(),
                "djhskjsaasjkdh", "a0483ab869c2ef065e3a1153c8831882", 1);
        m_runtime.notification(notification, 2500);
    }
  
    /**
     *
     * @throws Exception
     */
    public void checkSpeedBump() throws Exception {
        Action action = new SpeedBumpAction(HermesRuntime.getRandomUUID());
        SpeedBumpActionResult result = (SpeedBumpActionResult) m_runtime.action(action, 2500);
        int speed = result.getSpeedBump();
        if (speed > 0) {
            System.out.println("\nSpeedBump " + speed + " !");
            Thread.sleep(speed);
        }
    }

    public static void main(String[] args) {
        Logger.getLogger(BFTNodeOld.class.getName()).log(Level.SEVERE, "BFTNode:main()");
        final BFTNodeOld node = new BFTNodeOld();
        try {

            String id = null;
            System.out.println(args.length);
            if (args.length == 0) {
                id = HermesRuntime.getRandomUUID();
                Logger.getLogger(BFTNodeOld.class.getName()).log(Level.INFO, "BFTNode with random id");
            } else {
                id = args[0];
                Logger.getLogger(BFTNodeOld.class.getName()).log(Level.INFO, "BFTNode with assinged id");
            }
            node.open(id);
            Logger.getLogger(BFTNodeOld.class.getName()).log(Level.INFO, "starting BFTNode id={0}", id);
            //node.doNotification();
            //node.onSetState();
            //onde.leaveCell();
            /*try {
             //fault example
             if (node.boolPerformFault()) {
             System.out.println("\nperform fault!");
             }
             } catch (Exception ex) {
             Logger.getLogger(BFTNode.class.getName()).log(Level.SEVERE, null, ex);
             }


             Action action = new ActionImpl(HermesRuntime.getRandomUUID(), "start_run");
             try {
             ActionResultImpl absresult = (ActionResultImpl) node.getRuntime().action(action, 2500);
             BooleanWrapperActionResult result = BooleanWrapperActionResult.allocate(absresult.getResult());
             System.out.println("ActionResult=" + result.getValue());
             } catch (Exception ex) {
             Logger.getLogger(BFTNode.class.getName()).log(Level.SEVERE, null, ex);
             }*/
        } catch (Exception ex) {
            Logger.getLogger(BFTNodeOld.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }

        final String faultID = "2B4FA20ED54E4DA9B6B2A917D1FA723F";

        while (true) {
            try {
                Logger.getLogger(BFTNodeOld.class.getName()).log(Level.INFO, "BFTNode: resuming");
                HermesFault fault = node.getRuntime().getFaultManager().getFault(faultID);
                if (fault != null && fault.isEnabled()) {
                    switch (fault.getSerialID()) {
                        case CPULoaderFaultAlgorithm.SERIAL_ID:
                        case CrashFault.SERIAL_ID:
                        case ThreadDelayFault.SERIAL_ID:
                            try {
                                Logger.getLogger(BFTNodeOld.class.getName()).log(Level.SEVERE, "Executing FAULT={0}", fault);
                                fault.execute();
                                fault.disable();
                                Logger.getLogger(BFTNodeOld.class.getName()).log(Level.SEVERE, "After FAULT={0}", fault);
                            } catch (Exception ex) {
                                Logger.getLogger(BFTNodeOld.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            break;
                        case NetworkCorrupterAlgorithm.SERIAL_ID: {
                            NetworkCorrupterAlgorithm faultImpl = (NetworkCorrupterAlgorithm) fault;
                            byte[] packet = new byte[10];
                            try {
                                //TODO
                                //faultImpl.setExecutionArgument(packet);
                                fault.execute();
                            } catch (Exception ex) {
                                Logger.getLogger(BFTNodeOld.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
                Logger.getLogger(BFTNodeOld.class.getName()).log(Level.INFO, "BFTNode: sleeping");
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(BFTNodeOld.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
//        try {
//            m_runtime.close();
//        } catch (Exception ex) {
//            Logger.getLogger(P3Node.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    /**
     *
     */
    public void close() {
        m_runtime.close();
    }
}
