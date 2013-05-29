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
package hermes.runtime.faultinjection;

import hermes.serialization.HermesSerializable;
import hermes.serialization.HermesSerializableHelper;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public abstract class FaultDescription implements HermesSerializable{

    public abstract int getSerialID();
       
    
    public FaultDescription(){        
    }
    
    protected abstract void serializableFaultDescriptionImpl(ByteBuffer buf) throws Exception;    
    protected abstract void deserializableFaultDescriptionImpl(ByteBuffer buf) throws Exception;
    
    @Override
    public void serializable(ByteBuffer buf) throws Exception {
        buf.putInt(getSerialID());
        serializableFaultDescriptionImpl(buf);
           
    }

    @Override
    public void deserializable(ByteBuffer buf) throws Exception {
        if(HermesSerializableHelper.deserializeInt(buf) != getSerialID()){
            throw new Exception("Serializable: SERIAL_ID Mismatched!");
        }
        deserializableFaultDescriptionImpl(buf);               
    }
}
