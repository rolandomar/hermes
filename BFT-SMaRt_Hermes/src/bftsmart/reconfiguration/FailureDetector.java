/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bftsmart.reconfiguration;

import bftsmart.communication.MessageHandler;
import java.util.logging.Level;

/**
 *
 * @author rmartins
 */
public class FailureDetector {

    static boolean TUNED = true;
    public static long SHORT_TIMEOUT = 2000;//1000;//2000//3000;        
    public static int TOTAL_ORDER_TIMEOUT = 3000;//2000;//3000//10000;    
    public static long STATE_INIT_TIMEOUT = 3000;//2000;//3000//10000
    public static long CONNECTION_TIMEOUT = 5000;//3000;//3000//10000
    public static int INVOCATION_TIMEOUT = 60;//s
    public static int TOTAL_ORDER_HIGHMARK_COUNT = 100;//100;
    public static int TOTAL_ORDER_HIGHMARK_REVIVAL_TIMEOUT = 10;//10;//s?
    public static int TOTAL_ORDER_HIGHMARK_TIMEOUT = 100;//100;
    public static int REPLAY_VERIFICATION_TIME = 0;
    public static int TOTAL_ORDER_CHECKPOINT_PERIOD = 1;//s
    public static int TOTAL_ORDER_GLOBAL_CHECKPOINT_PERIOD = 0;//s
    public static int SO_TIMEOUT = 10000; //server socket timeout (accept)
    public static int StatusReplyListener_timeout = 20;//s

    
//    public static long SHORT_TIMEOUT = 3000;
//    public static int TOTAL_ORDER_TIMEOUT = 10000;
//    public static long STATE_INIT_TIMEOUT = 10000;
//    public static long CONNECTION_TIMEOUT = 10000;
//    //public static int INVOCATION_TIMEOUT = 40;//s
//    public static int INVOCATION_TIMEOUT = 120;//s
//    public static int TOTAL_ORDER_HIGHMARK_COUNT = 100;
//    public static int TOTAL_ORDER_HIGHMARK_REVIVAL_TIMEOUT = 10;
//    public static int TOTAL_ORDER_HIGHMARK_TIMEOUT = 100;
//    public static int REPLAY_VERIFICATION_TIME = 0;
//    public static int TOTAL_ORDER_CHECKPOINT_PERIOD = 1;
//    public static int TOTAL_ORDER_GLOBAL_CHECKPOINT_PERIOD = 0;
//    public static int SO_TIMEOUT = 10000; //server socket timeout (accept)
//    public static int StatusReplyListener_timeout = 20;//s

    public static long recalculateTimeout(int lastRegency, int nextRegency, long oldTimeout) {
        if (TUNED) {
            if (nextRegency == -1) {
                System.out.println("recalculateTimeout: lastRegency:" + lastRegency
                        + " nextRegency:" + nextRegency
                        + " oldTimeout:" + oldTimeout
                        + " newTimeout:" + TOTAL_ORDER_TIMEOUT);
                return TOTAL_ORDER_TIMEOUT;
            }
            if (lastRegency == nextRegency) {
                System.out.println("recalculateTimeout: lastRegency:" + lastRegency
                        + " nextRegency:" + nextRegency
                        + " oldTimeout:" + oldTimeout
                        + " newTimeout:" + oldTimeout * 2);
                return oldTimeout * 2;
            }
            System.out.println("recalculateTimeout: lastRegency:" + lastRegency
                    + " nextRegency:" + nextRegency
                    + " oldTimeout:" + oldTimeout
                    + " newTimeout:" + TOTAL_ORDER_TIMEOUT);
            return TOTAL_ORDER_TIMEOUT;
        } else {
            java.util.logging.Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE,
            "recalculateTimeout: NOT TUNED lastRegency:" + lastRegency
                        + " nextRegency:" + nextRegency
                        + " oldTimeout:" + oldTimeout
                        + " newTimeout:" + (oldTimeout * 2));
            return oldTimeout * 2;
        }
        //return oldTimeout;
    }

    public static long getSleepAboveTimeout() {
        return SHORT_TIMEOUT * 5;
    }

    public static long getSleepBellowTimeout() {
        return (long) (SHORT_TIMEOUT * 0.90);
        //return 1;
    }
}
