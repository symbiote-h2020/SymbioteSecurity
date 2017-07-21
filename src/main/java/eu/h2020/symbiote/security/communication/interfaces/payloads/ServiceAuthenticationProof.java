package eu.h2020.symbiote.security.communication.interfaces.payloads;

/**
 * Payload sent by the service to confirm its authenticity.
 * <p>
 * TODO @Daniele
 */
public class ServiceAuthenticationProof {
    /**
     * used to encrypt the timestamp
     */
    public final String encryptionToken;
    /**
     * timestamp used to verify age of this proof signed by the service and encrypted using the attached token
     * This is basically a Base64 encoded {@link javax.crypto.SealedObject}
     */
    public final String encryptedTimestamp;

    public ServiceAuthenticationProof(String encryptionToken, String encryptedTimestamp) {
        this.encryptionToken = encryptionToken;
        this.encryptedTimestamp = encryptedTimestamp;
    }
}
