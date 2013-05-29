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
package hermes.deployment.network.packets;

import hermes.network.packets.HermesAbstractPacketHeader;
import hermes.serialization.HermesSerializableHelper;
import hermes.network.packets.rpc.TwoWayRequest;
import hermes.deployment.network.EnvElement;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class BootstrapRequestPacket extends TwoWayRequest {
    public static final int BOOTSTRAPREQUESTY_OPCODE = 0x000003;
    private String m_binary;
    private EnvElement[] m_env;
    private String[] m_args;

    
    public BootstrapRequestPacket() {
        
    }
    
    public BootstrapRequestPacket(HermesAbstractPacketHeader header) {
        super(header);
    }
    
    public BootstrapRequestPacket(int requestNo,String binary, String[] args, EnvElement[] env) {
        super(requestNo);        
        m_header.setOpcode(BOOTSTRAPREQUESTY_OPCODE);
        m_binary = binary;
        m_env = env;
        m_args = args;
    }

    public String getBinary() {
        return m_binary;
    }

    public EnvElement[] getEnv() {
        return m_env;
    }

    public String[] getArgs() {
        return m_args;
    }

    @Override
    protected void serializeTwoWayRequestBody(ByteBuffer buf) throws Exception{
        HermesSerializableHelper.serializeString(buf, m_binary);        
        HermesSerializableHelper.serializeStringArray(buf, m_args);
        //HermesSerializableHelper.serializeString(buf, m_env);
        HermesSerializableHelper.serializeArray(buf, m_env);
        //System.out.println("serializeTwoWayRequestBody() finsihed");
    }

    @Override
    protected void deserializeTwoWayRequestBody(ByteBuffer buf) throws Exception {
        m_binary = HermesSerializableHelper.deserializeString(buf);        
        m_args = HermesSerializableHelper.deserializeStringArray(buf);
        m_env = HermesSerializableHelper.deserializeArray(buf, EnvElement.class);
        //System.out.println("deerializeTwoWayRequestBody() finsihed");
        //m_env = HermesSerializableHelper.deserializeString(buf);
    }
}
