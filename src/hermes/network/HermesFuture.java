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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.*;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class HermesFuture<T> implements Future<T> {

    private T contents = null;
    private Lock aLock = new ReentrantLock();
    private boolean cancel = false;
    private Condition condVar = aLock.newCondition();

    @Override
    public T get() {
        try {
            aLock.lock();
            System.out.println("HermesFuture: get:"+this);
            while (contents == null && !cancel) {
                try {
                    condVar.await();
                } catch (InterruptedException e) {
                }
            }
            System.out.println("HermesFuture: after await:"+this);            
            //System.out.println("HermesFuture: get signall");
            //condVar.signalAll();
        } finally {
            aLock.unlock();
        }
        return contents;
    }

    public void put(T value) {
        try {
            aLock.lock();            
            contents = value;            
            System.out.println("HermesFuture: after put:"+this);            
            condVar.signalAll();
        } finally {
            aLock.unlock();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        aLock.lock();
        try {
            if (cancel) {
                return true;
            }
            cancel = true;

            condVar.signalAll();
        } finally {
            aLock.unlock();
        }
        return true;
    }

    @Override
    public boolean isCancelled() {
        return (cancel);
    }

    @Override
    public boolean isDone() {
        return (contents != null);
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            aLock.lock();
            System.out.println("HermesFuture: before timeawait:"+this);    
            while (contents == null) {
                try {
                    condVar.await(timeout, unit);
                } catch (InterruptedException e) {
                }
            }
            System.out.println("HermesFuture: after timeawait:"+this);    
            //System.out.format("Consumer %d got: %d%n", who, contents);
            //condVar.signalAll();
        } finally {
            aLock.unlock();
        }
        return contents;
    }
}
