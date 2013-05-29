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
import hermes.orchestration.notification.*;
import hermes.serialization.HermesSerializable;
import hermes.serialization.HermesSerializableHelper;
import java.nio.ByteBuffer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class RemoteActionImpl extends Action {
    public static int SERIAL = 6000000;
    
    @Override
    public int getSerialID(){
        return SERIAL;
    }
        
    public RemoteActionImpl(){}
        
    public RemoteActionImpl(String srcID,String action){
        super(srcID,action);    
    }

    @Override
    protected void serializeAction(ByteBuffer buf){
        
    }
    @Override
    protected void deserializableAction(ByteBuffer buf) throws Exception{
        
    }
        
}
