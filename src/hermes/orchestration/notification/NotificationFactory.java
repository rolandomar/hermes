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
package hermes.orchestration.notification;

import hermes.orchestration.notification.faults.FaultNotification;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class NotificationFactory {

    //protected List<NotificationFactoryElement> NotificationElements;
    protected Map<Integer, NotificationFactoryElement> NotificationElements;
    static NotificationFactory m_staticInstance = new NotificationFactory();

    public static NotificationFactory getInstance(){
        return m_staticInstance;
    }
    
    protected NotificationFactory() {
        NotificationElements = new HashMap<>();
        NotificationElements.put(GeneralNotificationImpl.GENERALNOTIFICATIONIMPL_SERIALID, new NotificationFactoryElement(GeneralNotificationImpl.GENERALNOTIFICATIONIMPL_SERIALID,
                GeneralNotificationImpl.class));
        NotificationElements.put(RuntimeJoinNotification.RUNTIMEJOINNOTIFICATION_SERIALID, new NotificationFactoryElement(RuntimeJoinNotification.RUNTIMEJOINNOTIFICATION_SERIALID,
                RuntimeJoinNotification.class));
        NotificationElements.put(RuntimeLeaveNotification.RUNTIMELEAVENOTIFICATION_SERIALID, new NotificationFactoryElement(RuntimeLeaveNotification.RUNTIMELEAVENOTIFICATION_SERIALID,
                RuntimeLeaveNotification.class));
        NotificationElements.put(SetStateNotification.SETSTATENOTIFICATION_SERIALID, new NotificationFactoryElement(SetStateNotification.SETSTATENOTIFICATION_SERIALID,
                SetStateNotification.class));
        NotificationElements.put(FaultNotification.FAULTNOTIFICATION_SERIALID, 
                new NotificationFactoryElement(
                FaultNotification.FAULTNOTIFICATION_SERIALID,
                FaultNotification.class));
        
        NotificationElements.put(StatNotification.STATNOTIFICATION_SERIALID, 
                new NotificationFactoryElement(
                StatNotification.STATNOTIFICATION_SERIALID,
                StatNotification.class));
        

    }
    
    public void addNotificationElemen(final int serialID, final Class notificationClass){
        NotificationFactoryElement e = new NotificationFactoryElement(serialID,notificationClass);
        m_staticInstance.NotificationElements.put(serialID,e);
    }

    public Notification getNotificationImpl(int serialID) {
        try {            
            return (Notification) NotificationElements.get(serialID).m_notificationClass.newInstance();        
    //        switch (serialID) {
    //            case GeneralNotificationImpl.GENERALNOTIFICATIONIMPL_SERIALID: {
    //                return new GeneralNotificationImpl();
    //            }
    //            case RuntimeJoinNotification.RUNTIMEJOINNOTIFICATION_SERIALID: {
    //                return new RuntimeJoinNotification();
    //            }
    //            case RuntimeLeaveNotification.RUNTIMELEAVENOTIFICATION_SERIALID: {
    //                return new RuntimeLeaveNotification();
    //            }
    //            default:
    //        }
    //        }
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(NotificationFactory.class.getName()).log(Level.SEVERE, "serial="+serialID, ex);
            return null;
        }         
    }

    class NotificationFactoryElement {

        Class m_notificationClass;
        int m_serialID;

        public NotificationFactoryElement(int serialID, Class notificationClass) {
            m_serialID = serialID;
            m_notificationClass = notificationClass;
        }
    }
}
