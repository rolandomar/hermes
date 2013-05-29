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
package hermes.orchestration.remote;

import hermes.orchestration.actions.*;
import hermes.orchestration.notification.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class RemoteActionFactory {

    //protected List<NotificationFactoryElement> NotificationElements;
    protected Map<Integer, RemoteActionFactoryElement> actionElements;
    static RemoteActionFactory m_staticInstance = new RemoteActionFactory();

    public static RemoteActionFactory getInstance(){
        return m_staticInstance;
    }
    
    protected RemoteActionFactory() {
        actionElements = new HashMap<>();
        actionElements.put(
                GetStateRemoteAction.SERIAL_ID, 
                new RemoteActionFactoryElement(
                GetStateRemoteAction.SERIAL_ID,
                GetStateRemoteAction.class));
        actionElements.put(
                GetStateActionResult.SERIAL_ID, 
                new RemoteActionFactoryElement(
                GetStateActionResult.SERIAL_ID,
                GetStateActionResult.class));   
        actionElements.put(
                AddAspectRemoteAction.SERIAL_ID, 
                new RemoteActionFactoryElement(
                AddAspectRemoteAction.SERIAL_ID,
                AddAspectRemoteAction.class));
        actionElements.put(
                AddAspectRemoteActionResult.SERIAL_ID, 
                new RemoteActionFactoryElement(
                AddAspectRemoteActionResult.SERIAL_ID,
                AddAspectRemoteActionResult.class));   
    }
    
    public void addNotificationElemen(final int serialID, final Class notificationClass){
        RemoteActionFactoryElement e = new RemoteActionFactoryElement(serialID,notificationClass);
        m_staticInstance.actionElements.put(serialID,e);
    }

    public RemoteAction getRemoteActionImpl(int serialID) {
        try {            
            return (RemoteAction) actionElements.get(serialID).m_actionClass.newInstance();        
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(NotificationFactory.class.getName()).log(Level.SEVERE, "serial="+serialID, ex);
            return null;
        }         
    }
    
    public RemoteActionResult getRemoteActionResultImpl(int serialID) {
        try {            
            return (RemoteActionResult) actionElements.get(serialID).m_actionClass.newInstance();        
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(NotificationFactory.class.getName()).log(Level.SEVERE, "serial="+serialID, ex);
            return null;
        }         
    }

    class RemoteActionFactoryElement {

        Class m_actionClass;
        int m_serialID;

        public RemoteActionFactoryElement(int serialID, Class actionClass) {
            m_serialID = serialID;
            m_actionClass = actionClass;
        }
    }
}
