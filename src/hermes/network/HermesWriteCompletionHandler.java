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
package hermes.network;

import hermes.network.packets.WritePacketContext;
import java.io.IOException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.ShutdownChannelGroupException;
import java.nio.channels.WritePendingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
class HermesWriteCompletionHandler implements
        CompletionHandler<Integer, WritePacketContext> {

    private HermesAsynchronousSocketChannel socketChannel;

    public HermesWriteCompletionHandler(
            HermesAsynchronousSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void completed(Integer bytesWritten, WritePacketContext packet) {
        //System.out.println("Writer: completed="+bytesWritten);
        //System.out.println("Writer: buffer remain="+packet.getBuffer().remaining());
        if (bytesWritten <= 0) {
            try {
                socketChannel.close();
            } catch (IOException ex1) {
                Logger.getLogger(HermesWriteCompletionHandler.class.getName()).log(Level.SEVERE, null);
            }
            return;
        }
        if (packet.getBuffer().remaining() != 0) {
            boolean send = false;
            while (!send) {
                try {
                    socketChannel.getChannel().write(packet.getBuffer(), packet, this);
                    send = true;
                } catch (NotYetConnectedException | WritePendingException ex0) {
//                    try {
//                        socketChannel.close();
//                    } catch (IOException ex1) {
//                        Logger.getLogger(HermesWriteCompletionHandler.class.getName()).log(Level.SEVERE, null, ex1);
//                    }
                } catch  (ShutdownChannelGroupException ex2){
                    throw ex2;
                }
            }
            return;
        }
    }

    @Override
    public void failed(Throwable exc, WritePacketContext packet) {
        try {
            //System.out.println("Writer: failed");
            if (socketChannel != null) {
                this.socketChannel.close();
            }
        } catch (IOException ex) {
            //Logger.getLogger(HermesWriteCompletionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
