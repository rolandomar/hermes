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
package hermes.orchestration.actions;

import hermes.orchestration.notification.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class ActionFactory {

    //protected List<NotificationFactoryElement> NotificationElements;
    protected Map<Integer, ActionFactoryElement> actionElements;
    static ActionFactory m_staticInstance = new ActionFactory();

    public static ActionFactory getInstance(){
        return m_staticInstance;
    }
    
    protected ActionFactory() {
        actionElements = new HashMap<>();
        actionElements.put(
                ActionImpl.ACTIONIMPL_SERIALID, 
                new ActionFactoryElement(
                ActionImpl.ACTIONIMPL_SERIALID,
                ActionImpl.class));
        actionElements.put(
                ActionResultImpl.ACTIONRESULTIMPL_SERIALID, 
                new ActionFactoryElement(
                ActionResultImpl.ACTIONRESULTIMPL_SERIALID,
                ActionResultImpl.class));
        actionElements.put(
                SpeedBumpAction.SERIAL_ID, 
                new ActionFactoryElement(
                SpeedBumpAction.SERIAL_ID,
                SpeedBumpAction.class));
        actionElements.put(
                SpeedBumpActionResult.SERIAL_ID, 
                new ActionFactoryElement(
                SpeedBumpActionResult.SERIAL_ID,
                SpeedBumpActionResult.class));  
        
        actionElements.put(
                CheckFaultInjectionAction.SERIAL_ID, 
                new ActionFactoryElement(
                CheckFaultInjectionAction.SERIAL_ID,
                CheckFaultInjectionAction.class));
        actionElements.put(
                CheckFaultInjectionActionResult.SERIAL_ID, 
                new ActionFactoryElement(
                CheckFaultInjectionActionResult.SERIAL_ID,
                CheckFaultInjectionActionResult.class));                          
    }
    
    public void addNotificationElemen(final int serialID, final Class notificationClass){
        ActionFactoryElement e = new ActionFactoryElement(serialID,notificationClass);
        m_staticInstance.actionElements.put(serialID,e);
    }

    public Action getActionImpl(int serialID) {
        try {            
            return (Action) actionElements.get(serialID).m_actionClass.newInstance();        
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(NotificationFactory.class.getName()).log(Level.SEVERE, "serial="+serialID, ex);
            return null;
        }         
    }
    
    public ActionResult getActionResultImpl(int serialID) {
        try {            
            return (ActionResult) actionElements.get(serialID).m_actionClass.newInstance();        
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(NotificationFactory.class.getName()).log(Level.SEVERE, "serial="+serialID, ex);
            return null;
        }         
    }

    class ActionFactoryElement {

        Class m_actionClass;
        int m_serialID;

        public ActionFactoryElement(int serialID, Class actionClass) {
            m_serialID = serialID;
            m_actionClass = actionClass;
        }
    }
}
