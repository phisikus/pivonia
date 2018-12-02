package eu.phisikus.pivonia.api.pool;

/**
 * Implement this interface in your message to provide source and target node identification.
 *
 * @param <K> type of key used for node identification
 */
public interface Envelope<K> {
    K getSenderId();
    K getRecipientId();
}
