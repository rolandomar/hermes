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
package hermes.deployment.network;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class ServerWriteCompletionHandler implements
  CompletionHandler<Integer, ServerChannelState> {

  private AsynchronousSocketChannel socketChannel;

  public ServerWriteCompletionHandler(
    AsynchronousSocketChannel socketChannel) {
    this.socketChannel = socketChannel;
  }

  @Override
  public void completed(
    Integer bytesWritten, ServerChannelState attachment) {
    try {
      socketChannel.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void failed(Throwable exc, ServerChannelState attachment) {
   try {
      socketChannel.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}


/*public class ProactorInitiator {
  static int ASYNC_SERVER_PORT = 4333;

  public void initiateProactiveServer(int port)
    throws IOException {

    final AsynchronousServerSocketChannel listener =
      AsynchronousServerSocketChannel.open().bind(
        new InetSocketAddress(port));
     AcceptCompletionHandler acceptCompletionHandler =
       new AcceptCompletionHandler(listener);

     SessionState state = new SessionState();
     listener.accept(state, acceptCompletionHandler);
  }

  public static void main(String[] args) {
    try {
       System.out.println("Async server listening on port : " +
         ASYNC_SERVER_PORT);
       new ProactorInitiator().initiateProactiveServer(
         ASYNC_SERVER_PORT);
    } catch (IOException e) {
     e.printStackTrace();
    }

    // Sleep indefinitely since otherwise the JVM would terminate
    while (true) {
      try {
        Thread.sleep(Long.MAX_VALUE);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}

public class AcceptCompletionHandler
  implements
    CompletionHandler<AsynchronousSocketChannel, SessionState> {

  private AsynchronousServerSocketChannel listener;

  public AcceptCompletionHandler(
    AsynchronousServerSocketChannel listener) {
    this.listener = listener;
  }

  @Override
  public void completed(AsynchronousSocketChannel socketChannel,
    SessionState sessionState) {
   // accept the next connection
   SessionState newSessionState = new SessionState();
   listener.accept(newSessionState, this);

   // handle this connection
   ByteBuffer inputBuffer = ByteBuffer.allocate(2048);
   ReadCompletionHandler readCompletionHandler =
     new ReadCompletionHandler(socketChannel, inputBuffer);
   socketChannel.read(
     inputBuffer, sessionState, readCompletionHandler);
  }

  @Override
  public void failed(Throwable exc, SessionState sessionState) {
   // Handle connection failure...
  }

}

public class ReadCompletionHandler implements
  CompletionHandler<Integer, SessionState> {

   private AsynchronousSocketChannel socketChannel;
   private ByteBuffer inputBuffer;

   public ReadCompletionHandler(
     AsynchronousSocketChannel socketChannel,
     ByteBuffer inputBuffer) {
     this.socketChannel = socketChannel;
     this.inputBuffer = inputBuffer;
   }

   @Override
   public void completed(
     Integer bytesRead, SessionState sessionState) {

     byte[] buffer = new byte[bytesRead];
     inputBuffer.rewind();
     // Rewind the input buffer to read from the beginning

     inputBuffer.get(buffer);
     String message = new String(buffer);

     System.out.println("Received message from client : " +
       message);

     // Echo the message back to client
     WriteCompletionHandler writeCompletionHandler =
       new WriteCompletionHandler(socketChannel);

     ByteBuffer outputBuffer = ByteBuffer.wrap(buffer);

     socketChannel.write(
       outputBuffer, sessionState, writeCompletionHandler);
  }

  @Override
  public void failed(Throwable exc, SessionState attachment) {
    //Handle read failure.....
   }

}

public class WriteCompletionHandler implements
  CompletionHandler<Integer, SessionState> {

  private AsynchronousSocketChannel socketChannel;

  public WriteCompletionHandler(
    AsynchronousSocketChannel socketChannel) {
    this.socketChannel = socketChannel;
  }

  @Override
  public void completed(
    Integer bytesWritten, SessionState attachment) {
    try {
      socketChannel.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void failed(Throwable exc, SessionState attachment) {
   // Handle write failure.....
  }

}

public class SessionState {

  private Map<String, String> sessionProps =
    new ConcurrentHashMap<String, String>();

   public String getProperty(String key) {
     return sessionProps.get(key);
   }

   public void setProperty(String key, String value) {
     sessionProps.put(key, value);
   }

}

*/
