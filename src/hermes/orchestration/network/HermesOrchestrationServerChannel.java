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
package hermes.orchestration.network;

import hermes.network.HermesAsynchronousSocketChannel;
import hermes.network.HermesServerChannel;
import hermes.orchestration.OrchestrationNodeDaemon;
import java.nio.channels.AsynchronousSocketChannel;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class HermesOrchestrationServerChannel extends HermesServerChannel{
    OrchestrationNodeDaemon m_daemon;
    public HermesOrchestrationServerChannel(OrchestrationNodeDaemon daemon){
        m_daemon = daemon;
    }
    
    @Override
    protected HermesAsynchronousSocketChannel createClient(AsynchronousSocketChannel result) {
        HermesOrchestrationServerClientChannel client = 
                new HermesOrchestrationServerClientChannel(m_daemon,result,this);
        //m_daemon.onNewServerClientChannel(client);
        return client;
    }        
}
