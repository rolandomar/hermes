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
package hermes.deployment.network;

import hermes.network.packets.HermesAbstractPacket;
import hermes.deployment.DeploymentDaemon;
import hermes.deployment.network.packets.InitPacket;
import hermes.deployment.network.packets.InitReplyPacket;
import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class ServerChannelState {

    enum State {

        NOT_INITIALIZED,
        INITIALIZED,
        CLOSED
    }
    State m_currentstate = State.NOT_INITIALIZED;
    DeploymentDaemon m_nodeDaemon;
    AtomicInteger m_sequencer = new AtomicInteger(0);

    public ServerChannelState(DeploymentDaemon nodeDaemon) {
        m_nodeDaemon = nodeDaemon;
    }

    void onPacketReceived(AsynchronousSocketChannel channel, HermesAbstractPacket packet) {
        switch (m_currentstate) {
            case NOT_INITIALIZED: {
                if(packet.getHeader().getOpcode()!=InitPacket.INITPACKET_OPCODE){
                    try {
                        channel.close();
                    } catch (IOException ex) {
                        Logger.getLogger(ServerChannelState.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }else{
                    String retValue = "";
                    InitReplyPacket replyPacket = new InitReplyPacket(
                            packet.getHeader().getSequenceNo(),
                            InitReplyPacket.RETCODE_OK,
                            retValue
                            );
                }
                break;
            }
            case INITIALIZED: {
                break;
            }
            case CLOSED: {
                break;
            }

        }
    }
}
