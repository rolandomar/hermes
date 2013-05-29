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
package hermes.serialization;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;

/**
 *
 * @author Rolando Martins <rolandomartins@cmu.edu>
 */
public class HermesSerializableHelper {

    public static void serializeBytes(ByteBuffer buf, byte[] bytes) {        
        if(bytes == null){
            buf.putInt(0);       
        }else{
            buf.putInt(bytes.length);       
            buf.put(bytes);
        }
        
    }
                
    public static void serializeBoolean(ByteBuffer buf, Boolean b) {
        byte bb = 0;
        if (b != null) {
            if(b){
                bb = 1;
            }
        }
        buf.put(bb);        
    }
    
    public static <T extends HermesSerializable> 
            void serializeArray(ByteBuffer buf, T[] l) throws Exception {
        if (l == null || l.length==0) {
            buf.putInt(0);
            return;
        }
        int size = l.length;
        buf.putInt(size);
        for(HermesSerializable e: l){
            e.serializable(buf);
        }           
    }
    
    public static <T extends HermesSerializable> T[] deserializeArray(ByteBuffer buf,Class c) throws Exception {
        int size = deserializeInt(buf);
        if (size == 0) {
            return null;
        }
        //List<T> l = new ArrayList<>();                                        
        T[] l = (T[])Array.newInstance(c, size);        
        for (int i = 0; i < size; i++) {
            T t = (T)c.newInstance();
            t.deserializable(buf);
            l[i] = t;
        }
        return l;
    }
    
    
    
    public static void serializeString(ByteBuffer buf, String str) {
        if (str == null || str.length() == 0) {
            buf.putInt(0);
            return;
        }
        buf.putInt(str.length());
        buf.put(str.getBytes());
    }

    public static void serializeStringArray(ByteBuffer buf, String[] strArray) {
        if (strArray == null || strArray.length == 0) {
            buf.putInt(0);
            return;
        }
        buf.putInt(strArray.length);
        for (int i = 0; i < strArray.length; i++) {
            serializeString(buf, strArray[i]);
        }
    }
    
    public static byte[] deserializeBytes(ByteBuffer buf) throws Exception {
        int size = buf.getInt();
        if(size == 0){
            return null;
        }
        byte[] b = new byte[size];
        buf.get(b);
        return b;
    }

    public static int deserializeInt(ByteBuffer buf) throws Exception {
        HermesSerializableHelper.checkBufferSize(buf, Integer.SIZE / 8);
        return buf.getInt();
    }
    
    public static byte deserializeByte(ByteBuffer buf) throws Exception {
        HermesSerializableHelper.checkBufferSize(buf, Integer.SIZE / 8);
        return buf.get();
    }

    public static long deserializeLong(ByteBuffer buf) throws Exception {
        HermesSerializableHelper.checkBufferSize(buf, Long.SIZE / 8);
        return buf.getLong();
    }

    
    public static Boolean deserializeBoolean(ByteBuffer buf) throws Exception {
        byte bb = buf.get();
        return (bb!=0);        
    }
    
    public static String deserializeString(ByteBuffer buf) throws Exception {
        int size = deserializeInt(buf);
        if (size == 0) {
            return null;
        }
        HermesSerializableHelper.checkBufferSize(buf, size);
        byte[] buffer = new byte[size];
        buf.get(buffer);
        return new String(buffer);
    }

    public static String[] deserializeStringArray(ByteBuffer buf) throws Exception {
        int size = deserializeInt(buf);
        if (size == 0) {
            return null;
        }
        String[] strArray = new String[size];
        for (int i = 0; i < size; i++) {
            strArray[i] = deserializeString(buf);
        }
        return strArray;
    }

    public static void checkBufferSize(ByteBuffer buf, int elementSize) throws Exception {
        if (buf.remaining() < elementSize) {
            throw new Exception("Malformed packet: insufficient buffer data");
        }
    }

    public static short deserializeShort(ByteBuffer buf) throws Exception {
        HermesSerializableHelper.checkBufferSize(buf, Short.SIZE / 8);
        return buf.getShort();
    }
    
    public static String stringsToString(String[] values){
        String ret = "";
        for(int i=0; i < values.length; i++){
            if(i<values.length-1){
                ret += values[i]+":";
            }else{
                ret += values[i];
            }
        }
        System.out.println("Fault Nodes: "+ret);
        
        return ret;        
    }
    
    public static String[] stringToStringS(String values){
        StringTokenizer st = new StringTokenizer(values,":");
        String[] ret = new String[st.countTokens()];
        int i = 0;
        while(st.hasMoreTokens()){            
            ret[i++] = st.nextToken();            
        }
        return ret;
    }
    

    public static boolean isMalicious(int i,String[] values){
        for(String s:values){
            if(Integer.parseInt(s)==i){
                return true;
            }
        }
        return false;
    }
}
