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

import hermes.serialization.HermesSerializableHelper;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class ActionResultImpl extends ActionResult {

    public static int ACTIONRESULTIMPL_SERIALID = 5000001;
    protected byte[] m_result;

    public ActionResultImpl(){        
    }
    
    public ActionResultImpl(byte[] result){
        m_result = result;
    }
    
    @Override
    public int getSerialID() {
        return ACTIONRESULTIMPL_SERIALID;
    }
   
    public byte[] getResult() {
        return m_result;
    }

    @Override
    protected void serializeActionResult(ByteBuffer buf) throws Exception {
        HermesSerializableHelper.serializeBytes(buf, m_result);
    }

    @Override
    protected void deserializableActionResult(ByteBuffer buf) throws Exception {        
        m_result = HermesSerializableHelper.deserializeBytes(buf);
    }
}
