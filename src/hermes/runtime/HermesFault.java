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

import hermes.HermesConfig;
import hermes.orchestration.actions.CheckFaultInjectionAction;
import hermes.orchestration.actions.CheckFaultInjectionActionResult;
import hermes.orchestration.notification.Notification;
import hermes.orchestration.notification.faults.FaultNotification;
import hermes.runtime.bft.BFTNode;
import hermes.runtime.faultinjection.CrashFault;
import hermes.runtime.faultinjection.bft.FaultContext;
import hermes.serialization.HermesSerializable;
import hermes.serialization.HermesSerializableHelper;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 *
 */
public abstract class HermesFault implements HermesSerializable {
    //Fault triggers type

    static public final byte TRIGGER_DISABLED = 0x0;
    static public final byte TRIGGER_ALWAYS = 0x1;
    static public final byte TRIGGER_ONCE = 0x2;
    static public final byte TRIGGER_CUSTOM = 0x3;
    private final static int NOTIFICATION_TIMEOUT = 10000; //ms
    //Scope of the fault, for now defaults to 'global'
    public final static String GLOBAL_FAULT_SIGNATURE = "global";
    //Not used anymore, keep as a remainder
    //String m_signature;
    String m_faultID;
    HermesRuntime m_runtime;
    boolean m_synchronizedFault = false;
    //private boolean m_enable = true;
    private byte m_triggerType = TRIGGER_DISABLED;
    FaultCustomTriggerChecker m_customChecker = null;
    FaultContext m_ctx = new FaultContext();
    protected final ReentrantLock m_lock = new ReentrantLock();
    private final static boolean CHECK_WITH_ORCHESTRATOR_ONCE = true;
    boolean m_orchestratorFlag = false;

    public HermesFault() {
        m_runtime = HermesRuntime.getInstance();
        m_faultID = null;
        m_synchronizedFault = false;
        m_triggerType = TRIGGER_DISABLED;
    }

    public HermesFault(byte triggerType) {
        m_runtime = HermesRuntime.getInstance();
        m_faultID = null;
        m_synchronizedFault = false;
        m_triggerType = triggerType;
    }

    public HermesFault(HermesRuntime runtime, String id, byte triggerType) {
        m_runtime = runtime;
        m_faultID = id;
        m_synchronizedFault = false;
        m_triggerType = triggerType;
    }

    public HermesFault(HermesRuntime runtime, String id, boolean synchronizedFault, byte triggerType) {
        m_runtime = runtime;
        m_faultID = id;
        m_synchronizedFault = synchronizedFault;
        m_triggerType = triggerType;
    }

    public boolean getSynchronizedFaul() {
        return m_synchronizedFault;
    }

    public byte getTriggerType() {
        return m_triggerType;
    }

    /*public String getSingature() {
     return m_signature;
     }

     public void setSignature(String signature) {
     m_signature = signature;
     }*/
    public void setSynchronizedFault(boolean synch) {
        m_synchronizedFault = synch;
    }

    public String getFaultID() {
        return m_faultID;
    }

    public void setFaultID(String aspectID) {
        m_faultID = aspectID;
    }

    @Override
    public void serializable(ByteBuffer buf) throws Exception {
        buf.putInt(getSerialID());
        HermesSerializableHelper.serializeString(buf, m_faultID);
        //HermesSerializableHelper.serializeString(buf, m_signature);
        HermesSerializableHelper.serializeBoolean(buf, m_synchronizedFault);
        buf.put(m_triggerType);
        m_ctx.serializable(buf);
        serializableImpl(buf);
    }

    @Override
    public void deserializable(ByteBuffer buf) throws Exception {
        int serial = HermesSerializableHelper.deserializeInt(buf);
        if (serial != getSerialID()) {
            throw new Exception("Notification.deserializable: wrong serial");
        }
        m_faultID = HermesSerializableHelper.deserializeString(buf);
        //m_signature = HermesSerializableHelper.deserializeString(buf);
        m_synchronizedFault = HermesSerializableHelper.deserializeBoolean(buf);
        m_triggerType = HermesSerializableHelper.deserializeByte(buf);
        m_ctx.deserializable(buf);
        deserializableImpl(buf);
    }

    /**
     *
     * @return
     */
    abstract public int getSerialID();

    abstract protected void serializableImpl(ByteBuffer buf) throws Exception;

    abstract protected void deserializableImpl(ByteBuffer buf) throws Exception;

    abstract public void executeImpl() throws Exception;

    public void execute() throws Exception {
        Logger.getLogger(HermesFault.class.getName()).log(Level.SEVERE, "HermesFault: execute: " + m_faultID);
        try {
            m_lock.lock();
            if (trigger()) {
                preExecute();
                if (getSerialID() == CrashFault.SERIAL_ID) {
                    //will crash so we send the postExecute before...
                    postExecute();
                }
                executeImpl();
                //will never reach in crashes
                postExecute();
            }
        } finally {
            Logger.getLogger(HermesFault.class.getName()).log(Level.SEVERE, "HermesFault: executed: " + m_faultID);
            m_lock.unlock();
        }
    }

    public void setFaultContext(FaultContext ctx) throws Exception {
        m_ctx = ctx;
    }

    public FaultContext getFaultContext() {
        return m_ctx;
    }

    private void preExecute() throws Exception {
        if (isEnabled()) {
            if (!checkForInjectingFault(NOTIFICATION_TIMEOUT)) {
                throw new Exception("Synchronization failed");
            }
            if (m_triggerType == TRIGGER_ONCE) {
                disable();
            }
        }
    }

    private void postExecute() throws Exception {
        if (isEnabled()) {
            postExecutionNotification();
        }
    }

    protected boolean checkForInjectingFault(long timeout) throws Exception {
        Logger.getLogger(HermesFault.class.getName()).log(Level.SEVERE, "checkForInjectingFault: " + m_faultID);
        if (isEnabled()) {
            if (CHECK_WITH_ORCHESTRATOR_ONCE) {
                if (m_orchestratorFlag) {
                    return true;
                }
            }
            CheckFaultInjectionAction action =
                    new CheckFaultInjectionAction(m_runtime.getRuntimeID(), m_faultID, m_ctx);
            CheckFaultInjectionActionResult result =
                    (CheckFaultInjectionActionResult) m_runtime.action(action, timeout);
            if (CHECK_WITH_ORCHESTRATOR_ONCE) {
                if (!m_orchestratorFlag) {
                    m_orchestratorFlag = result.getCheck();
                }
            }
            //System.out.println("checkForInjectingFault: " + m_faultID + " " + result.getCheck());
            Logger.getLogger(HermesFault.class.getName()).log(Level.SEVERE, "checkForInjectingFault: result" + result.getCheck());
            return result.getCheck();
        }
        Logger.getLogger(HermesFault.class.getName()).log(Level.SEVERE, "checkForInjectingFault: false");
        return false;
    }

    public void postExecutionNotification() {
        try {
            m_lock.lock();
            if (isEnabled()) {
                Notification notification = new FaultNotification(m_faultID);
                m_runtime.notification(notification, NOTIFICATION_TIMEOUT);
                //System.out.println("postExecutionNotification:" + m_faultID);
            }
        } finally {
            m_lock.unlock();
        }
    }

    public void setRuntime(HermesRuntime runtime) {
        m_runtime = runtime;
    }

    public boolean trigger() {
        switch (m_triggerType) {
            case TRIGGER_ALWAYS:
                return true;
            case TRIGGER_ONCE:
                return true;
            case TRIGGER_CUSTOM:
                return customTriggerChecker();
            case TRIGGER_DISABLED:
            default:
                return false;
        }
    }

    public boolean isEnabled() {
        //return m_enable;
        switch (m_triggerType) {
            case TRIGGER_ALWAYS:
            case TRIGGER_ONCE:
                return true;
            case TRIGGER_CUSTOM:
                return customTriggerChecker();
            case TRIGGER_DISABLED:
            default:
                return false;
        }
    }

    public void disable() {
        m_triggerType = TRIGGER_DISABLED;
    }

    @Override
    public String toString() {
        String str = "HermesFault:" + m_faultID + " sync=" + m_synchronizedFault;
        return str;
    }

    protected boolean customTriggerChecker() {
        //double check if this fault was disabled
        if (m_triggerType == TRIGGER_DISABLED) {
            return false;
        }
        if (m_customChecker == null) {
            return true;
        } else {
            return m_customChecker.customTriggerChecker(this);
        }
    }
}
