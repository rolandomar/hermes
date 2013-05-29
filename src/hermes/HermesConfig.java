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
package hermes;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Rolando Martins Carnegie Mellon University
 */
public class HermesConfig {

    public static AsynchronousChannelGroup MainThreadPool;
    public final static boolean DEBUG = true;
    public final static Level LEVEL = Level.FINE;
    public static PropertyManager props;
    protected static ReentrantLock m_lock = new ReentrantLock();
    public static final String CONFIG_PARAMETER = "hermes.props";
    public static final String DEFAULT_CONFIG = "config/hermes.properties";
    public static int m_groupSize = 0;

    static public PropertyManager getProps() {
        return props;
    }

    static {
        Logger log = LogManager.getLogManager().getLogger("");
        for (Handler h : log.getHandlers()) {
            h.setLevel(LEVEL);
        }
        try {
            Properties p = System.getProperties();
            //p.setProperty("java.nio.channels.DefaultThreadPool.threadFactory","");
            p.setProperty("java.nio.channels.DefaultThreadPool.initialSize", "100");
            //MainThreadPool = AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), 100);
            MainThreadPool = AsynchronousChannelGroup.withThreadPool(Executors.newCachedThreadPool(Executors.defaultThreadFactory()));
            //AsynchronousChannelGroup.withFixedThreadPool(100, Executors.defaultThreadFactory());
        } catch (IOException ex) {
            Logger.getLogger(HermesConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static {
        loadProps();
    }

    public static void addLoggerFile(String logName) {
        try {
            FileHandler handler = new FileHandler(logName);//, 102400, 5);
            Logger log = LogManager.getLogManager().getLogger("");
            log.addHandler(handler);
            SimpleFormatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);
            handler.setLevel(LEVEL);
        } catch (IOException ex) {
            Logger.getLogger(HermesConfig.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(HermesConfig.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

//    public static int getGroupSize(){
//        return (int)props.getPropertyLong("hermes.daemon.replicas");
//    }
    public static void setGroupSize(int size) {
        m_groupSize = size;
    }

    public static int getGroupSize() {
        return m_groupSize;
    }

    public static String getNodeIP(int no) {
        System.out.println("hermes.node.replica" + no + "_ip");
        return props.getProperty("hermes.node.replica_" + no + "_ip");
    }

    public static String getNodeID(int no) {
        return props.getProperty("hermes.node.replica_" + no + "_id");
    }

    public static String getNodeClientIP() {
        return props.getProperty("hermes.node.client_ip");
    }

    public static String getNodeClientID() {
        return props.getProperty("hermes.node.client_id");
    }

    public static String getOrchestrationDaemonIP() {
        return props.getProperty("hermes.bft.orchestration.ip");
    }

    public static int getOrchestrationDaemonPort() {
        return (int) props.getPropertyLong("hermes.bft.orchestration.port");
    }

    public static String getNodeDaemonIP() {
        return props.getProperty("hermes.node.daemon.ip");
    }

    public static int getNodeDaemonPort() {
        return (int) props.getPropertyLong("hermes.node.daemon.port");
    }

    /**
     * Load properties from file
     */
    public static void loadProps() {

        try {
            m_lock.lock();
            if (props == null) {
                String configName = System.getProperty(CONFIG_PARAMETER,
                        DEFAULT_CONFIG);
                //System.out.println("CONFIG="+configName);
                URI URIfile = new URI(configName);
                System.out.println("CONFIG=" + configName.toString());
                props = new PropertyManager(URIfile.toString());
                //props = new PropertyManager(configName);

            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(HermesConfig.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            m_lock.unlock();
        }
    }

    public static String getWorkingDir() {
        return props.getProperty("hermes.workingdir");
    }

    public static String getApplicationLaunch() {
        return props.getProperty("hermes.node.application.name");
    }
}
