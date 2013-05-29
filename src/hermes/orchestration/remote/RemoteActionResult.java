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

import hermes.orchestration.actions.*;
import hermes.serialization.HermesSerializable;
import hermes.serialization.HermesSerializableHelper;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public abstract class RemoteActionResult implements HermesSerializable {

    public abstract int getSerialID();

    protected abstract void serializeActionResult(ByteBuffer buf) throws Exception;
    protected abstract void deserializableActionResult(ByteBuffer buf) throws Exception;
    
    //public abstract byte[] getResult();

    @Override
    public void serializable(ByteBuffer buf) throws Exception{
        buf.putInt(getSerialID());
        serializeActionResult(buf);
    }

    @Override
    public void deserializable(ByteBuffer buf) throws Exception {
        int serial = HermesSerializableHelper.deserializeInt(buf);
        if (serial != getSerialID()) {
            throw new Exception("Notification.deserializable: wrong serial");
        }
        deserializableActionResult(buf);
    }
}
