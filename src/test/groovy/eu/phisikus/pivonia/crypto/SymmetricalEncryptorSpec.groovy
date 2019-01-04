package eu.phisikus.pivonia.crypto

import eu.phisikus.pivonia.test.CryptoUtils
import org.apache.tools.ant.util.FileUtils
import spock.lang.Specification
import spock.lang.Subject

class SymmetricalEncryptorSpec extends Specification {

    @Subject
    def encryptor
    def testKeyFilename

    void setup() {
        testKeyFilename = CryptoUtils.buildRandomKeyset()
        def testKeyContent = CryptoUtils.getKeysetContent(testKeyFilename)
        encryptor = new SymmetricalEncryptor(testKeyContent)
    }

    void cleanup() {
        FileUtils.delete(new File(testKeyFilename))
    }


    def "Object can be and encrypted and decrypted"() {

        given: "there is a set of random input bytes"
        def expectedBytes = CryptoUtils.getRandomBytes()

        when: "encryption and decryption is performed"
        def encryptedBytes = encryptor.encrypt(expectedBytes)
        def actualBytes = encryptor.decrypt(encryptedBytes)

        then: "decrypted object is equal to the one that was serialized"
        actualBytes == expectedBytes

    }
}
