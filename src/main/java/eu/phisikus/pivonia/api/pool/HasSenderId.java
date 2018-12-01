package eu.phisikus.pivonia.api.pool;

/**
 * Implement this interface in your message to provide sender node identification.
 *
 * @param <K> type of key used for node identification
 */
public interface HasSenderId<K> {
    K getSenderId();
}
