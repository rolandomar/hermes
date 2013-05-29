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

import hermes.orchestration.actions.Action;
import java.nio.ByteBuffer;

/**
 * 
 * @author Rolando Martins
 * Carnegie Mellon University
 */
public class SpeedBumpAction extends Action{
public static int SERIAL_ID = 5000002;
    
    @Override
    public int getSerialID(){
        return SERIAL_ID;
    }
        
    public SpeedBumpAction(){}
        
    public SpeedBumpAction(String srcID){
        super(srcID,"speed_bump");    
    }

    @Override
    protected void serializeAction(ByteBuffer buf){
        
    }
    @Override
    protected void deserializableAction(ByteBuffer buf) throws Exception{
        
    }
}
