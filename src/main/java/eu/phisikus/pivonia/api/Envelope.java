package eu.phisikus.pivonia.api;

/**
 * Implement this interface in your message to provide source and target node identification.
 *
 * @param <K> type of key used for node identification
 */
public interface Envelope<K> {
    /**
     * Retrieves sender ID.
     *
     * @return sender ID
     */
    K getSenderId();

    /**
     * Retrieves recipient ID.
     *
     * @return recipient ID
     */
    K getRecipientId();

    /**
     * Changes envelope data like sender and recipient id.
     *
     * @param senderId    new sender id
     * @param recipientId new recipient id
     * @return some instance containing new data
     */
    Envelope<K> readdress(K senderId, K recipientId);
}
