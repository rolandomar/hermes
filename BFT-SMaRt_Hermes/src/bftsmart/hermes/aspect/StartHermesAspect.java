/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bftsmart.hermes.aspect;

/**
 *
 * @author rmartins
 */
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.JoinPoint;

import hermes.HermesConfig;
import hermes.runtime.HermesRuntime;
import hermes.runtime.bft.BFTNode;

import bftsmart.demo.counter.CounterServer;

@Aspect
public class StartHermesAspect {

    
    //@After("execution (* bftsmart.demo.counter.CounterClient.main*(..))")
    //@After("execution (* bftsmart.demo.counter.*.main*(..))")
    
    @Before("execution (* bftsmart.demo.counter.CounterServer.main*(..))")
    public void advice(JoinPoint joinPoint) {
        //public static void main(String[] args) throws IOException {
        String[] args = (String[]) joinPoint.getArgs()[0];
        String id = args[0];
        System.out.printf("StartHermesAspect.advice() called on '%s'%n", joinPoint);
        //HermesRuntime m_runtime = new HermesRuntime();
        HermesRuntime.getInstance().setID(id);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        Date date = new Date();
        dateFormat.format(date);
        String prefix = "replica";
        if(id.compareTo("1001")==0){
            System.out.println("\nCLIENT\n");
            prefix = "client";
        }else{
            System.out.println("\nREPLICA\n");
        }
        String logName = HermesConfig.getWorkingDir() + "/node_" + prefix+"_"+
        		HermesRuntime.getInstance().getRuntimeID() + "_" + dateFormat.format(date) + ".log";        
        HermesConfig.addLoggerFile(logName);        
        try {
            HermesRuntime.getInstance().open();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}