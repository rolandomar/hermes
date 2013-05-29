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
package hermes.runtime.faultinjection;

import hermes.orchestration.notification.NotificationFactory;
import hermes.runtime.faultinjection.bft.BFTDelayPacketFaultDescription;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Rolando Martins
 * Carnegie Mellon University
 */
public class FaultDescriptionFactory {
//protected List<NotificationFactoryElement> NotificationElements;
    protected Map<Integer, FaultDescriptionFactory.FaultDescriptionFactoryElement> FaultDescriptionElements;
    static FaultDescriptionFactory m_staticInstance = new FaultDescriptionFactory();

    public static FaultDescriptionFactory getInstance(){
        return m_staticInstance;
    }
    
    protected FaultDescriptionFactory() {
        FaultDescriptionElements = new HashMap<>();
        FaultDescriptionElements.put(
                CPULoaderFaultDescription.SERIAL_ID, 
                new FaultDescriptionFactory.FaultDescriptionFactoryElement(
                CPULoaderFaultDescription.SERIAL_ID,
                CPULoaderFaultDescription.class));
        FaultDescriptionElements.put(
                CrashFaultDescription.SERIAL_ID, 
                new FaultDescriptionFactory.FaultDescriptionFactoryElement(
                CrashFaultDescription.SERIAL_ID,
                CrashFaultDescription.class));
        FaultDescriptionElements.put(
                NetworkDropperFaultDescription.SERIAL_ID, 
                new FaultDescriptionFactory.FaultDescriptionFactoryElement(
                NetworkDropperFaultDescription.SERIAL_ID,
                NetworkDropperFaultDescription.class));
        FaultDescriptionElements.put(
                MultipleFaultDescription.SERIAL_ID, 
                new FaultDescriptionFactory.FaultDescriptionFactoryElement(
                MultipleFaultDescription.SERIAL_ID,
                MultipleFaultDescription.class));
        FaultDescriptionElements.put(
                NetworkCorrupterFaultDescription.SERIAL_ID, 
                new FaultDescriptionFactory.FaultDescriptionFactoryElement(
                NetworkCorrupterFaultDescription.SERIAL_ID,
                NetworkCorrupterFaultDescription.class));
        FaultDescriptionElements.put(
                NetworkStackLeakFaultDescription.SERIAL_ID, 
                new FaultDescriptionFactory.FaultDescriptionFactoryElement(
                NetworkStackLeakFaultDescription.SERIAL_ID,
                NetworkStackLeakFaultDescription.class));       
        FaultDescriptionElements.put(
                ThreadDelayFaultDescription.SERIAL_ID, 
                new FaultDescriptionFactory.FaultDescriptionFactoryElement(
                ThreadDelayFaultDescription.SERIAL_ID,
                ThreadDelayFaultDescription.class));                  
        
        FaultDescriptionElements.put(
                BFTDelayPacketFaultDescription.SERIAL_ID, 
                new FaultDescriptionFactory.FaultDescriptionFactoryElement(
                BFTDelayPacketFaultDescription.SERIAL_ID,
                BFTDelayPacketFaultDescription.class));                  
    }
    
    public void addNotificationElement(final int serialID, final Class notificationClass){
        FaultDescriptionFactory.FaultDescriptionFactoryElement e = new FaultDescriptionFactory.FaultDescriptionFactoryElement(serialID,notificationClass);
        m_staticInstance.FaultDescriptionElements.put(serialID,e);
    }

    public FaultDescription getFaultDescriptionImpl(int serialID) {
        try {            
            return (FaultDescription) FaultDescriptionElements.get(serialID).m_notificationClass.newInstance();        
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(NotificationFactory.class.getName()).log(Level.SEVERE, "serial="+serialID, ex);
            return null;
        }         
    }

    class FaultDescriptionFactoryElement {

        Class m_notificationClass;
        int m_serialID;

        public FaultDescriptionFactoryElement(int serialID, Class notificationClass) {
            m_serialID = serialID;
            m_notificationClass = notificationClass;
        }
    }
}
