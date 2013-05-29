/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hermes.runtime.bft;

/**
 *
 * @author rmartins
 */
public interface BFTPacketInterface {
    /**
     * Forge the destination node ID in this packet
     * @param realID
     * @param forgedID
     */
    public void forgeDestinationNode(String realID, String forgedID);
    /**
     * Forge the source node ID in this packet
     * @param realID
     * @param forgedID
     */
    public void forgeSourceNode(String realID, String forgedID);
    
    /**
     * Forge the payload and certificate from nodeID
     * @param nodeID
     * @param newPayload
     * @param certificate
     */
    public void forgePayload(String nodeID, byte[] newPayload, byte[] certificate);
}
