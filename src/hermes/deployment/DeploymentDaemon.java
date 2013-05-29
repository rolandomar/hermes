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
import hermes.deployment.network.EnvElement;
import hermes.deployment.network.HermesNodeServerChannel;
import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class DeploymentDaemon {

    static private int QUIRK_SLEEP_TIME = 250; //ms
    protected HermesNodeServerChannel serverChannel = new HermesNodeServerChannel(this);
    protected List<Process> m_listOfProcesses = new ArrayList<>();

    void open(String ip, int port) throws IOException {
        Logger.getLogger(DeploymentDaemon.class.getName()).
                log(Level.INFO, "DeploymentDaemon: open():{0}", new Object[]{port});
        if (ip == null) {
            serverChannel.open(port);
        } else {
            serverChannel.open(ip, port);
        }
    }

    public static void main(String[] args) {
        DeploymentDaemon daemon = new DeploymentDaemon();
        try {
            int replicaID = Integer.parseInt(args[0]);
            daemon.open(HermesConfig.getNodeIP(replicaID), HermesConfig.getNodeDaemonPort());
            //daemon.open(HermesConfig.getNodeDaemonIP(), HermesConfig.getNodeDaemonPort());
            System.out.println("After Init server");
        } catch (IOException ex) {
            Logger.getLogger(DeploymentDaemon.class.getName()).log(Level.SEVERE, "DeploymentDaemon: main(): failed to open server", ex);
        }
        while (true) {
            try {
                //Loop forever, until it is killed
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(DeploymentDaemon.class.getName()).log(Level.SEVERE, "HermesNodeDaemon: main(): failed to sleep", ex);
            }
        }

    }

    /**
     * Kills all the launched applications
     */
    public void killAllProcesses() {
        for (Process p : m_listOfProcesses) {
            if (HermesConfig.DEBUG) {
                Logger.getLogger(DeploymentDaemon.class.getName()).
                        log(Level.INFO, "DeploymentDaemon: killAllProcesses(): killing process {0}", p.toString());
            }
            p.destroy();
        }
        m_listOfProcesses.clear();
        if (HermesConfig.DEBUG) {
            Logger.getLogger(DeploymentDaemon.class.getName()).
                    log(Level.INFO, "DeploymentDaemon: killAllProcesses(): ended");
        }
    }

    /**
     * Bootstrap an application in this node
     *
     * @param binary Binary to launch
     * @param args Arguments to used in the launch
     * @param envElements Environment variables to be set
     * @return true, if successful, false, otherwise
     */
    public boolean boostrapApplication(String binary, String[] args, EnvElement[] envElements) {
        if (HermesConfig.DEBUG) {
            /*Logger.getLogger(DeploymentDaemon.class.getName()).
             log(Level.INFO,
             "DeploymentDaemon: boostrapApplication(): {0} {1} {2} {3} {4}", 
             new Object[]{binary, args[0], args[1], args[2], args[3]});*/            
            String s = "";
            for(String arg:args){
                s+=" "+arg;                
            }              
            Logger.getLogger(DeploymentDaemon.class.getName()).
             log(Level.INFO,
             "DeploymentDaemon: boostrapApplication(): "+binary+" "+s);
        }
        String[] command = null;
        if (args == null || args.length == 0) {
            command = new String[]{binary};
        } else {
            command = new String[args.length + 1];
            command[0] = binary;
            System.arraycopy(args, 0, command, 1, args.length);
        }
        final ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        //pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        Logger.getLogger(DeploymentDaemon.class.getName()).
             log(Level.INFO,
             "DeploymentDaemon: boostrapApplication(): WorkingDir:"+HermesConfig.getWorkingDir());
        pb.directory(new File(HermesConfig.getWorkingDir()));


        Map<String, String> env = pb.environment();
        if (envElements != null) {
            for (int i = 0; i < envElements.length; i++) {
                env.put(envElements[i].getVar(), envElements[i].getValue());
            }
        }
        try {
            final Process p = pb.start();
            System.out.println("Start1");
            new Thread() {
                @Override
                public void run() {
                    System.out.println("Start2");
                    String s;
                    BufferedReader stdout = new BufferedReader(
                            new InputStreamReader(p.getInputStream()));
                    try {
                        while ((s = stdout.readLine()) != null) {
                            System.out.println(s);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(HermesNodeServerChannel.class.getName()).
                            log(Level.SEVERE,
                            "DeploymentDaemon: boostrapApplication(): EXIT THREAD");
                    }


                }
            }.start();
            try {
                //Quirk mode
                //sleep to check if application bootstrap correctly                
                Thread.sleep(QUIRK_SLEEP_TIME);
            } catch (InterruptedException ex) {
            }
            try {
                int exitValue = p.exitValue();
                if (exitValue != 0) {
                    Logger.getLogger(HermesNodeServerChannel.class.getName()).
                            log(Level.SEVERE,
                            "DeploymentDaemon: boostrapApplication(): TERMINATED {0}", new Object[]{binary});

                    return false;
                }
            } catch (IllegalThreadStateException pEx) {
            }
            m_listOfProcesses.add(p);
            if (HermesConfig.DEBUG) {
                Logger.getLogger(DeploymentDaemon.class.getName()).log(Level.INFO, "DeploymentDaemon: boostrapApplication sucessful");
            }
            return true;
        } catch (IOException ex) {
            Logger.getLogger(DeploymentDaemon.class.getName()).log(Level.SEVERE, "DeploymentDaemon: boostrapApplication(): failed to start application");
            return false;
        }
    }
}
//            final BufferedReader stdin = new BufferedReader(new InputStreamReader(p.getInputStream()));
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    while (true) {
//                        try {
//                            //byte[] b = new byte[10000];
//                            //p.getInputStream().read(b);
//                            //System.out.println(new String(b)); 
//                            System.out.println(stdin.readLine());
//                        } catch (IOException ex) {
//                            Logger.getLogger(HermesNodeDaemon.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
//                }
//            }).start();
