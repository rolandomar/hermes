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

import hermes.HermesConfig;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public abstract class HermesClientChannel extends HermesAsynchronousSocketChannel {
    ExecutorService futureExecutor = Executors.newFixedThreadPool(1);
    SocketAddress m_sockaddr = null;
    //String m_id;

    public HermesClientChannel(String id) {
        m_id = id;
        //client.connect(addr);//Mserver.getLocalAddress()).get();
    }
    
    @Override
    public String getID(){
        return m_id;
    }

    public HermesFuture<Boolean> open(String ip, int port) throws IOException {
        if (m_channel != null) {
            //throw new IOException("Client already active");
            m_channel.close();
            m_channel = null;
        }
        try {
            final AsynchronousSocketChannel channel = 
                    AsynchronousSocketChannel.open(HermesConfig.MainThreadPool);
            InetAddress addr = InetAddress.getByName(ip);
            m_sockaddr = new InetSocketAddress(addr, port);
            setChannel(channel);
            
            //final FutureTask<Void> future = new FutureTask<>(connectionHandler);            
            HermesFuture<Boolean> future = new HermesFuture<>();
            ConnectionHandler connectionHandler = new ConnectionHandler(this,future,2500);
            //channel.co //= channel.connect(m_sockaddr);
            //System.out.println("bfore connect");
            channel.connect(m_sockaddr, null, connectionHandler);
            //System.out.println("after connection");
            //futureExecutor.execute(future);
            //this.activateRead();
            return future;
        } catch (IOException ex) {
            if (m_channel != null) {
                m_channel.close();
                m_channel = null;
            }
            throw ex;
        }
    }
}

class ConnectionHandler implements CompletionHandler<Void, Void>/*,Callable<Void>*/ {

    HermesClientChannel channel;
    //CountDownLatch latch = new CountDownLatch(1);
    long timeout;
    HermesFuture<Boolean> future;

    public ConnectionHandler(HermesClientChannel channel,HermesFuture<Boolean> future,long timeout) {
        this.channel = channel;
        this.timeout = timeout;
        this.future = future;
    }

    @Override
    public void completed(Void result, Void attachment) {
        //System.out.println("ConnectionHandler completed");
        //latch.countDown();
        future.put(true);
        //System.out.println("ConnectionHandler completed="+latch.getCount());
        channel.activateRead();       
    }

    @Override
    public void failed(Throwable exc, Void attachment) {
        System.out.println("ConnectionHandler failed");        
        try {
            channel.close();
        } catch (IOException ex) {
            Logger.getLogger(HermesClientChannel.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            //latch.countDown();
            future.put(false);            
        }
    }

    /*@Override
    public Void call() throws Exception {
        //System.out.println("ConnectionHandler call="+latch.getCount());
        latch.await();//timeout, TimeUnit.MILLISECONDS);
        System.out.println("ConnectionHandler after call");
        Void v = null;
        return v;
    }*/
}
