package eu.phisikus.pivonia.crypto;

/**
 * This interface represents the ability to transform bytes into encrypted form and back
 */
public interface Encryptor {
    /**
     * Encrypt contents of one array of bytes and return them in a new array
     *
     * @param cleartextData input bytes
     * @return encrypted array of bytes
     */
    byte[] encrypt(byte[] cleartextData);

    /**
     * Decrypt previously encrypted array of bytes to get the initial contents
     *
     * @param encryptedData encrypted array of bytes
     * @return decrypted array of bytes
     */
    byte[] decrypt(byte[] encryptedData);
}
