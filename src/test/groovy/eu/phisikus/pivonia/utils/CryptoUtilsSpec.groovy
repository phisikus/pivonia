package eu.phisikus.pivonia.utils

import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.JsonKeysetReader
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class CryptoUtilsSpec extends Specification {

    def "Should build and get contents of random keyset"() {
        given: "there is a random file name"
        def filename = UUID.randomUUID().toString()
        def filePath = Path.of(filename)

        when: "keyset is created and saved in that file"
        CryptoUtils.buildKeyset(filename)

        then: "its contents can be retrieved"
        def keyContent = CryptoUtils.getKeysetContent(filename)

        and: "they can be correctly interpreted as keyset"
        def keysetReader = JsonKeysetReader.withBytes(keyContent)
        CleartextKeysetHandle.read(keysetReader)
        noExceptionThrown()

        cleanup: "Remove key file"
        Files.delete(filePath)

    }
}
