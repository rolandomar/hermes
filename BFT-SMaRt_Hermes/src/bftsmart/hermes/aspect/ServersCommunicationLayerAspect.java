///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package bftsmart.hermes.aspect;
//
///**
// *
// * @author rmartins
// */
//import bftsmart.communication.SystemMessage;
//import bftsmart.paxosatwar.messages.MessageFactory;
//import bftsmart.paxosatwar.messages.PaxosMessage;
//import bftsmart.reconfiguration.util.TOMConfiguration;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.aspectj.lang.annotation.After;
//import org.aspectj.lang.JoinPoint;
//
//import hermes.HermesConfig;
//import hermes.runtime.HermesFault;
//import hermes.runtime.HermesRuntime;
//import hermes.runtime.bft.BFTNode;
//import hermes.runtime.faultinjection.CPULoaderFaultAlgorithm;
//import hermes.runtime.faultinjection.CrashFault;
//import hermes.runtime.faultinjection.NetworkCorrupterAlgorithm;
//import hermes.runtime.faultinjection.NetworkDropperFault;
//import hermes.runtime.faultinjection.ThreadDelayFault;
//import hermes.runtime.faultinjection.bft.BFTDelayPacketFault;
//import hermes.runtime.faultinjection.bft.BFTDelayPacketFaultDescription;
//import hermes.runtime.faultinjection.bft.BFTForgePayloadFault;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//
//@Aspect
//public class ServersCommunicationLayerAspect {
//
//    static TOMConfiguration config = null;
//
//    static {
//        int id = Integer.parseInt(HermesRuntime.getInstance().getRuntimeID());
//        config = new TOMConfiguration(id);
//    }
//
//    //@Before("execution (* com.aspectj.TestTarget.test*(..))")    
//    @Around("execution (* bftsmart.communication.server.ServersCommunicationLayer.send*(..))")
//    //public void advice(JoinPoint joinPoint) {        
//    public void advice(ProceedingJoinPoint joinPoint) throws Throwable {
//        //public final void send(int[] targets, SystemMessage sm, boolean useMAC) {
//        //System.out.println("KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKk");
//        //try {
//        SystemMessage sm = (SystemMessage) joinPoint.getArgs()[1];
//        PaxosMessage pm = null;
//        if (sm instanceof PaxosMessage) {
//            pm = (PaxosMessage) sm;
//            System.out.println("ROUND="+pm.getRound()+" number="+pm.getNumber());
//            //List<PaxosMessage> packets = null;            
//            //HermesRuntime.getInstance().getContext().put("RUN", pm.getNumber());            
//            //HermesRuntime.getInstance().getContext().put("TYPE", pm.getPaxosType());            
//        }
//        String faultID = "2B4FA20ED54E4DA9B6B2A917D1FA723F";
//        HermesFault fault = HermesRuntime.getInstance().getFaultManager().getFault(faultID);
//        //Logger.getLogger(BFTNode.class.getName()).log(Level.INFO, "\n\nFAILT={0}", fault);
//        if (fault != null) {
//            //System.out.println("LLLLLLLLLLLLLLLLLLLLLLLLL" + fault.isEnabled() + " " + pm);
//            if (fault.isEnabled() && pm != null) {
//                //System.out.println("UUUUUUUUUUUU");
//
//                //if(!(pm.getRound() == 0 && pm.getNumber() >= 100)){
//                //    return;
//                //}
//                fault.getFaultContext().putIntValue("RUN", pm.getNumber());
//                ///Logger.getLogger(BFTNode.class.getName()).log(Level.INFO, "\n\nFAILT={0}", fault);
//                //System.out.println("\n\nFAILT=" + fault);
//                try {
//                    switch (fault.getSerialID()) {
//                        case CPULoaderFaultAlgorithm.SERIAL_ID:
//                        case CrashFault.SERIAL_ID:
//                        case ThreadDelayFault.SERIAL_ID:
//                            try {
//                                Logger.getLogger(BFTNode.class.getName()).log(Level.INFO, "Executing FAULT={0}", fault);
//                                try {
//                                    fault.execute();
//                                    fault.disable();
//                                } catch (Exception ex) {
//                                    //return;
//                                    break;
//                                }
//                                Logger.getLogger(BFTNode.class.getName()).log(Level.INFO, "After FAULT={0}", fault);
//                            } catch (Exception ex) {
//                                Logger.getLogger(BFTNode.class.getName()).log(Level.SEVERE, null, ex);
//                            }
//                            break;
//                        case NetworkCorrupterAlgorithm.SERIAL_ID: {
//                            NetworkCorrupterAlgorithm faultImpl = (NetworkCorrupterAlgorithm) fault;
//                            byte[] packet = new byte[10];
//                            try {
//                                //TODO
//                                //faultImpl.setExecutionArgument(packet);
//                                fault.execute();
//                            } catch (Exception ex) {
//                                Logger.getLogger(BFTNode.class.getName()).log(Level.SEVERE, "injection not ready, but ok");
//                                //Logger.getLogger(BFTNode.class.getName()).log(Level.SEVERE, null, ex);
//                            }
//                            break;
//                        }
//                        case BFTDelayPacketFault.SERIAL_ID: {
//                            BFTDelayPacketFault faultImpl = (BFTDelayPacketFault) fault;
//                            int type = faultImpl.getDescription().getType();
//                            //System.out.println("BFTDelayPacketFaultDescription TYPE:" + type);
//                            switch (type) {
//                                case BFTDelayPacketFaultDescription.ALL: {
//                                    int id = Integer.parseInt(HermesRuntime.getInstance().getRuntimeID());
//                                    //System.out.println("BFTDelayPacketFaultDescription.ALL: ID:" + id + " ROUND" + pm.getNumber());
//                                    faultImpl.execute();
//                                    break;
//                                }
//                                case BFTDelayPacketFaultDescription.MOD_SEQ: {
//                                    int id = Integer.parseInt(HermesRuntime.getInstance().getRuntimeID());
//                                    int size = config.getF();
//                                    if ((pm.getNumber() % size) == id) {
//                                        //System.out.println("BFTDelayPacketFaultDescription.MOD_SEQ: ID:" + id + " ROUND" + pm.getNumber() + " MOD" + (pm.getNumber() % size));
//                                        faultImpl.execute();
//                                    }
//                                    break;
//                                }
//                                default: {
//                                    System.out.println("BFTDelayPacketFaultDescription. ERROR");
//                                    break;
//                                }
//                            }
//                            break;
//                        }
//
//                        case BFTForgePayloadFault.SERIAL_ID: {
//                            BFTForgePayloadFault faultImpl = (BFTForgePayloadFault) fault;
//                            int type = faultImpl.getType();
//                            switch (type) {
//                                case BFTForgePayloadFault.MAXINT: {
//                                    faultImpl.execute();
//                                    break;
//                                }
//                                case BFTForgePayloadFault.NEGATIVE: {
//                                    faultImpl.execute();
//                                    break;
//                                }
//                            }
//                            break;
//                        }
//                        case NetworkDropperFault.SERIAL_ID: {
//                            NetworkDropperFault faultImpl = (NetworkDropperFault) fault;
//                            faultImpl.execute();
//                            if (faultImpl.filterPacket()) {
//                                System.out.println("NetworkDropperFault:Fileting packet: "+HermesRuntime.getInstance().getRuntimeID());
//                                return;
//                            }
//                        }
//                    }
//                } catch (Exception ex) {
//                }
//            }
//
//        }
//        joinPoint.proceed();
//
//        //Thread.sleep(250);
//        //} catch (InterruptedException ex) {
//        //    Logger.getLogger(BFTNode.class.getName()).log(Level.SEVERE, null, ex);
//        //}
//    }
//}