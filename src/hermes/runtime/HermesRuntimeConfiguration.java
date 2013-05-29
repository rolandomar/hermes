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
package hermes.runtime;

import hermes.serialization.HermesSerializable;
import hermes.serialization.HermesSerializableHelper;
import java.nio.ByteBuffer;

/**
 * 
 * @author Rolando Martins
 * Carnegie Mellon University
 */
public class HermesRuntimeConfiguration implements HermesSerializable{
    public static final int HERMESRUNTIMEDESCRIPTION_SERIALID = 3000000;
    
    public static int JAVA_RUNTIME = 0x1;
    public static int CPLUSPLUS_RUNTIME = 0x2;
    
    protected int m_runtimeType = 0;
    public String m_runtimeDirectory = "~/.hermes";
    
    public int getSerialID(){
        return HERMESRUNTIMEDESCRIPTION_SERIALID;
    }
    
    public HermesRuntimeConfiguration(int runtimeType){
        m_runtimeType = runtimeType;
    }

    public int getRuntimeType(){
        return m_runtimeType;
    }
    
    public boolean isCPluplus(){
        return (m_runtimeType==CPLUSPLUS_RUNTIME);
    }
    
    public boolean isJava(){
        return (m_runtimeType==JAVA_RUNTIME);
    }
    
    @Override
    public void serializable(ByteBuffer buf) throws Exception {
        buf.putInt(getSerialID());
        buf.putInt(m_runtimeType);     
        HermesSerializableHelper.serializeString(buf, m_runtimeDirectory);
    }

    @Override
    public void deserializable(ByteBuffer buf) throws Exception {
        int serialID = buf.getInt();
        if(serialID != getSerialID()){
            throw new Exception("Wrong serial");
        }
        m_runtimeType = buf.getInt();
        m_runtimeDirectory = HermesSerializableHelper.deserializeString(buf);
    }
}
