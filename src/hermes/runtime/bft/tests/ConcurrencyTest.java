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
package hermes.runtime.bft.tests;

import hermes.runtime.bft.BFTNodeOld;
import hermes.orchestration.actions.Action;
import hermes.orchestration.actions.ActionImpl;
import hermes.orchestration.actions.ActionResultImpl;
import hermes.orchestration.actions.BooleanWrapperActionResult;
import hermes.runtime.HermesRuntime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class ConcurrencyTest {

    static public Thread test(final BFTNodeOld node) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    try {
                        //fault example
                        //System.out.println("\nbefore boolPerformFault()" + i);
                        //if(i%10==0){
                            node.checkSpeedBump();
                        //}
                        /*if (node.boolPerformFault()) {
                            System.out.println("perform fault! " + i);
                        } else {
                            System.out.println("perform fault failed! " + i);
                        }*/
                    } catch (Exception ex) {
                        Logger.getLogger(BFTNodeOld.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    static public Thread forkNode(final int id) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                BFTNodeOld node = new BFTNodeOld();
                try {
                    node.open(HermesRuntime.getRandomUUID());
                } catch (Exception ex) {
                    Logger.getLogger(ConcurrencyTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Node " + id + " start");
                //node.joinCell(HermesRuntime.getRandomUUID());
                //node.doNotification();
                node.onSetState();
                //node.leaveCell();
                int size = 10;
                Thread[] t = new Thread[size];
                for (int i = 0; i < size; i++) {
                    t[i] = test(node);
                    t[i].start();
                }
                Action action = new ActionImpl(HermesRuntime.getRandomUUID(), "start_run");
                try {
                    ActionResultImpl absresult = (ActionResultImpl)node.getRuntime().action(action, 2500);
                    BooleanWrapperActionResult result = BooleanWrapperActionResult.allocate(absresult.getResult());
                    System.out.println("ActionResult=" + result.getValue());
                } catch (Exception ex) {
                    Logger.getLogger(BFTNodeOld.class.getName()).log(Level.SEVERE, null, ex);
                }
                for (int i = 0; i < size; i++) {
                    try {
                        t[i].join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ConcurrencyTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BFTNodeOld.class.getName()).log(Level.SEVERE, null, ex);
                }
                node.close();
                System.out.println("Node " + id + " ended");
//                try {
//                    HermesRuntime.hermesRuntime.close();
//                } catch (Exception ex) {
//                    Logger.getLogger(P3Node.class.getName()).log(Level.SEVERE, null, ex);
//                }

            }
        });
    }

    public static void main(String[] args) {
        int size = 2;
        Thread[] t = new Thread[size];
        for (int i = 0; i < size; i++) {
            t[i] = forkNode(i);
            t[i].start();
        }
        for (int i = 0; i < size; i++) {
            try {
                t[i].join();
            } catch (InterruptedException ex) {
                Logger.getLogger(ConcurrencyTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
