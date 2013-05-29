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

import hermes.orchestration.remote.AddAspectRemoteAction;
import hermes.orchestration.remote.AddAspectRemoteActionResult;
import hermes.orchestration.remote.GetStateActionResult;
import hermes.orchestration.remote.GetStateRemoteAction;
import hermes.orchestration.remote.RemoteAction;
import hermes.orchestration.remote.RemoteActionResult;

/**
 *
 * @author Rolando Martins Carnegie Mellon University
 */
public class DefaultRemoteActionHandler extends RemoteActionHandler {

    HermesRuntime m_runtime;

    public DefaultRemoteActionHandler(HermesRuntime runtime) {
        m_runtime = runtime;
    }

    @Override
    public RemoteActionResult remoteAction(RemoteAction remoteAction) {
        System.out.println("DefaultRemoteActionHandler: onRemoteAction() id="+remoteAction.getSerialID());
        switch (remoteAction.getSerialID()) {
            case GetStateRemoteAction.SERIAL_ID: {
                //TODO!
                GetStateActionResult result = new GetStateActionResult(1001);
                System.out.println("DefaultRemoteActionHandler: onRemoteAction(): getstate");
                return result;
            }
            case AddAspectRemoteAction.SERIAL_ID:{
                System.out.println("DefaultRemoteActionHandler: AddAspectRemoteAction()");
                AddAspectRemoteAction addAspectAction = (AddAspectRemoteAction)remoteAction;
                addAspectAction.getFault().setRuntime(m_runtime);
                System.out.println("DefaultRemoteActionHandler: AddAspectRemoteAction() faultid="+
                        addAspectAction.getFault().getFaultID());
                m_runtime.getFaultManager().addFault(addAspectAction.getFault());
                AddAspectRemoteActionResult result = new AddAspectRemoteActionResult(true);
                return result;
            }
        }
        System.out.println("DefaultRemoteActionHandler: onRemoteAction(): null");
        return null;
    }    
    
    
}
