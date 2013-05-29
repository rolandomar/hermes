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
import hermes.orchestration.network.HermesOrchestrationServerClientChannel;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public abstract class HermesServerChannel<Client extends HermesAsynchronousSocketChannel> {

    protected ReentrantLock m_lock = new ReentrantLock();
    AsynchronousServerSocketChannel m_server = null;
    //protected List<Client> m_clientList = new ArrayList<>();
    protected Map<String, Client> m_clientList = new HashMap<>();

    public HermesServerChannel() {
    }

    public void open(int port) throws IOException {
        
        try {
            m_lock.lock();
            InetAddress addr = InetAddress.getLocalHost();//InetAddress.getByName(ip);
            InetSocketAddress m_sockaddr = new InetSocketAddress(addr, port);
            m_server = AsynchronousServerSocketChannel.open(HermesConfig.MainThreadPool).bind(m_sockaddr);
            open_i();
        } finally {
            m_lock.unlock();
        }
    }

    public void open(String ip, int port) throws IOException {
        
        try {
            m_lock.lock();
            InetAddress addr = InetAddress.getByName(ip);
            InetSocketAddress m_sockaddr = new InetSocketAddress(addr, port);
            m_server = AsynchronousServerSocketChannel.open(HermesConfig.MainThreadPool).bind(m_sockaddr);
            open_i();
        } finally {
            m_lock.unlock();
        }
    }

//    public void open(int port) throws IOException {
//        InetAddress addr = InetAddress.getLocalHost();//InetAddress.getByName(ip);
//        InetSocketAddress m_sockaddr = new InetSocketAddress(addr, port); 
//        m_server = AsynchronousServerSocketChannel.open(AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), 100)).bind(m_sockaddr);
//        
//        //m_server = AsynchronousServerSocketChannel.open(HermesConfig.MainThreadPool).bind(m_sockaddr);
//        open_i();        
//    }
    protected void open_i() {
        CompletionHandler<AsynchronousSocketChannel, Void> handler =
                new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel result, Void attachment) {
                Client client = createClient(result);
                //m_clientList.put(client.getID(), client);//add(client);
                client.activateRead();
                m_server.accept(null, this);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
            }
        };
        m_server.accept(null, handler);
    }

    protected abstract Client createClient(AsynchronousSocketChannel result);

    public void addClient(Client client) {
        
        try {
            m_lock.lock();
            m_clientList.put(client.getID(), client);//add(client);
        } finally {
            m_lock.unlock();
        }
    }

    public void close() {
        
        try {
            m_lock.lock();
            if (m_server != null) {
                try {
                    m_server.close();
                } catch (IOException ex) {
                }
            }
        } finally {
            m_lock.unlock();
        }
    }

    public void onServerClientChannelClose(HermesOrchestrationServerClientChannel aThis) {
        
        try {
            m_lock.lock();
            m_clientList.remove(aThis.getID());
        } finally {
            m_lock.unlock();
        }
    }
}
