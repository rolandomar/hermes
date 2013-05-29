/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hermes.orchestration.bft;

import hermes.deployment.network.HermesNodeServerChannel;
import hermes.runtime.HermesFault;
import hermes.runtime.faultinjection.CrashFault;
import hermes.runtime.faultinjection.bft.BFTDelayPacketFault;
import hermes.runtime.faultinjection.bft.BFTDelayPacketFaultDescription;
import hermes.runtime.faultinjection.bft.BFTForgePayloadFault;
import hermes.serialization.HermesSerializableHelper;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rmartins
 */
public class TestDaemon {

    public static void main(String[] args) {
        BFTOrchestrationDaemon daemon = new BFTOrchestrationDaemon();
        String r = "0";
        String f = "1";
        String a = "1";
        System.out.println("f:" + f);
        System.out.println("attack:" + a);
        try {
            daemon.open(f, r, a);
        } catch (IOException ex) {
            Logger.getLogger(HermesNodeServerChannel.class.getName()).log(Level.SEVERE, "OOOO", ex);
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(TestDaemon.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            String faultID = "asjh21323";
            //BFTDelayPacketFaultDescription desc = new BFTDelayPacketFaultDescription(BFTDelayPacketFaultDescription.MOD_SEQ, 5);
            //BFTDelayPacketFault fault = new BFTDelayPacketFault(faultID, desc, HermesFault.TRIGGER_ALWAYS);
            //daemon.injectFault("0", fault);
            ////
            //daemon.injectFault("0", new CrashFault("asjh21323"));
            ////
            String[] ids = {"0","1","2"};
            BFTForgePayloadFault fault =
                    new BFTForgePayloadFault(faultID, HermesFault.TRIGGER_ALWAYS, BFTForgePayloadFault.NEGATIVE);
            fault.getFaultContext().putStringValue("f", HermesSerializableHelper.stringsToString(ids));
            daemon.injectFault("0", fault);
        } catch (Exception ex) {
            Logger.getLogger(TestDaemon.class.getName()).log(Level.SEVERE, null, ex);
        }
        while (true) {
            try {                
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(TestDaemon.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
