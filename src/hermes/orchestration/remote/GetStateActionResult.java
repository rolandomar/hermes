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

import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class GetStateActionResult extends RemoteActionResult {

    public static int SERIAL_ID = 5000003;
    //protected byte[] m_result;
    int m_speedBump = -1;

    public GetStateActionResult() {
    }

    public GetStateActionResult(int speedBump) {
        ///m_result = new IntegerWrapperActionResult(speed).getBytes();
        m_speedBump = speedBump;
    }
    
    /*public GetStateActionResult(byte[] result) {
        m_result = result;
    }*/

    @Override
    public int getSerialID() {
        return SERIAL_ID;
    }

    public int getSpeedBump() throws Exception {
        //return IntegerWrapperActionResult.allocate(m_result).getValue();
        return m_speedBump;

    }
    
//    @Override
//    public byte[] getResult() {
//        return m_result;
//    }

    @Override
    protected void serializeActionResult(ByteBuffer buf) throws Exception {
        buf.putInt(m_speedBump);
        //HermesSerializableHelper.serializeBytes(buf, m_result);
    }

    @Override
    protected void deserializableActionResult(ByteBuffer buf) throws Exception {
        //m_result = HermesSerializableHelper.deserializeBytes(buf);
        m_speedBump = buf.getInt();
    }
}
