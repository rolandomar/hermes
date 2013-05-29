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

import hermes.network.packets.ReadPacketContext;
import java.io.IOException;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
class HermesReadCompletionHandler implements
        CompletionHandler<Integer, ReadPacketContext> {

    private HermesAsynchronousSocketChannel socketChannel;

    public HermesReadCompletionHandler(
            HermesAsynchronousSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void completed(Integer bytesRead, ReadPacketContext packet) {
        //System.out.println("HermesReadCompletionHandler completed=" + bytesRead);
        if (bytesRead == -1 || bytesRead==0) {
            try {
                //System.out.println("HermesReadCompletionHandler -1");
                socketChannel.close();
            } catch (IOException ex) {
                //Logger.getLogger(HermesReadCompletionHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }
        try {
            packet.getBuffer().flip();
            while(socketChannel.readPacket()){
                if(packet.getBuffer().remaining()==0){
                    break;
                }
                //System.out.println("HermesReadCompletionHandler loop");
            }
            
            //packet.getBuffer().flip();            
            packet.getBuffer().compact();
            socketChannel.getChannel().read(packet.getBuffer(), packet, this);            
        } catch (Exception ex) {
//            System.out.println("reader ex=" + ex.getMessage());
//            ex.printStackTrace();
            try {
                socketChannel.close();
            } catch (IOException ex1) {
                //Logger.getLogger(HermesReadCompletionHandler.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }

    }

    @Override
    public void failed(Throwable exc, ReadPacketContext packet) {
        System.out.println("Reader:failed socket closed");
        try {
            this.socketChannel.close();
        } catch (IOException ex) {
            Logger.getLogger(HermesReadCompletionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
