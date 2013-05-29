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
package hermes.orchestration.network.packets;

import hermes.network.packets.HermesAbstractPacketHeader;
import hermes.network.packets.rpc.TwoWayRequest;
import hermes.orchestration.notification.Notification;
import hermes.orchestration.notification.NotificationFactory;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class NotificationRequestPacket extends TwoWayRequest {
    public static final int NOTIFICATIONREQUESTY_OPCODE = 0x001110;
    private Notification m_notification;

    
    public NotificationRequestPacket() {
        
    }
    
    public NotificationRequestPacket(HermesAbstractPacketHeader header) {
        super(header);
    }
    
    public NotificationRequestPacket(int requestNo,Notification notification) {
        super(requestNo);        
        m_header.setOpcode(NOTIFICATIONREQUESTY_OPCODE);
        m_notification = notification;
    }

    public Notification getNotification() {
        return m_notification;
    }

    @Override
    protected void serializeTwoWayRequestBody(ByteBuffer buf) throws Exception{
        m_notification.serializable(buf);
    }

    @Override
    protected void deserializeTwoWayRequestBody(ByteBuffer buf) throws Exception {        
        
        int serialID = buf.getInt(buf.position());
        m_notification = NotificationFactory.getInstance().getNotificationImpl(serialID);
        m_notification.deserializable(buf);
    }
}
