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

import hermes.orchestration.notification.*;
import hermes.runtime.faultinjection.CPULoaderFaultAlgorithm;
import hermes.runtime.faultinjection.CrashFault;
import hermes.runtime.faultinjection.NetworkCorrupterAlgorithm;
import hermes.runtime.faultinjection.NetworkDropperFault;
import hermes.runtime.faultinjection.ThreadDelayFault;
import hermes.runtime.faultinjection.bft.BFTDelayPacketFault;
import hermes.runtime.faultinjection.bft.BFTForgePayloadFault;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class HermesFaultFactory {

    //protected List<NotificationFactoryElement> NotificationElements;
    protected Map<Integer, AspectFactoryElement> NotificationElements;
    static HermesFaultFactory m_staticInstance = new HermesFaultFactory();

    public static HermesFaultFactory getInstance(){
        return m_staticInstance;
    }
    
    protected HermesFaultFactory() {
        NotificationElements = new HashMap<>();
        NotificationElements.put(
                CPULoaderFaultAlgorithm.SERIAL_ID, 
                new AspectFactoryElement(
                CPULoaderFaultAlgorithm.SERIAL_ID,
                CPULoaderFaultAlgorithm.class)); 
        NotificationElements.put(
                CrashFault.SERIAL_ID, 
                new AspectFactoryElement(
                CrashFault.SERIAL_ID,
                CrashFault.class));     
        NotificationElements.put(
                NetworkCorrupterAlgorithm.SERIAL_ID, 
                new AspectFactoryElement(
                NetworkCorrupterAlgorithm.SERIAL_ID,
                NetworkCorrupterAlgorithm.class));     
        NotificationElements.put(
                ThreadDelayFault.SERIAL_ID, 
                new AspectFactoryElement(
                ThreadDelayFault.SERIAL_ID,
                ThreadDelayFault.class));     
        
        NotificationElements.put(
                NetworkDropperFault.SERIAL_ID, 
                new AspectFactoryElement(
                NetworkDropperFault.SERIAL_ID,
                NetworkDropperFault.class));     
        NotificationElements.put(
                BFTDelayPacketFault.SERIAL_ID, 
                new AspectFactoryElement(
                BFTDelayPacketFault.SERIAL_ID,
                BFTDelayPacketFault.class));     
        NotificationElements.put(
                BFTForgePayloadFault.SERIAL_ID, 
                new AspectFactoryElement(
                BFTForgePayloadFault.SERIAL_ID,
                BFTForgePayloadFault.class));     
        
    }
    
    public void addAspectElement(final int serialID, final Class notificationClass){
        AspectFactoryElement e = new AspectFactoryElement(serialID,notificationClass);
        m_staticInstance.NotificationElements.put(serialID,e);
    }

    public HermesFault getAspectImpl(int serialID) {        
        try {            
            return (HermesFault) NotificationElements.get(serialID).m_notificationClass.newInstance();           
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(NotificationFactory.class.getName()).log(Level.SEVERE, "serial="+serialID, ex);
            return null;
        }         
    }

    class AspectFactoryElement {

        Class m_notificationClass;
        int m_serialID;

        public AspectFactoryElement(int serialID, Class notificationClass) {
            m_serialID = serialID;
            m_notificationClass = notificationClass;
        }
    }
}
