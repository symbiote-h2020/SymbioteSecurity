package eu.h2020.symbiote.security.helpers;

import eu.h2020.symbiote.security.commons.Certificate;
import eu.h2020.symbiote.security.commons.Token;
import eu.h2020.symbiote.security.commons.credentials.AuthorizationCredentials;
import eu.h2020.symbiote.security.commons.exceptions.custom.MalformedJWTException;
import eu.h2020.symbiote.security.commons.jwt.JWTEngine;
import eu.h2020.symbiote.security.communication.payloads.ApplicationChallenge;
import eu.h2020.symbiote.security.communication.payloads.ServiceResponsePayload;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;


/**
 * Provides helper methods to handle client-service authentication procedure.
 * <p>
 *
 * @author Daniele Caldarola (CNIT)
 * @author Mikołaj Dobski (PSNC)
 */
public class MutualAuthenticationHelper {

    private static final Long THRESHOLD = new Long(3600000); // XXX: has to be reduced and put on a proper container or in the boostrap

    /**
     * Utility class to hash a string with SHA-256
     *
     * @param stringToHash certificate of the service host used to encrypt the challenge
     * @return the hexadecimal hashed string
     */
    public static String hashSHA256(String stringToHash) throws
            NoSuchAlgorithmException {

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] byteHash = messageDigest.digest(stringToHash.getBytes(StandardCharsets.UTF_8));
        String hexHash = new String(Hex.encode(byteHash)); // byte to hex converter to get the hashed value in hexadecimal
        messageDigest.reset();

        return hexHash;
    }


    /**
     * Used by the application to generate the challenge to be attached to the business query
     * so that the service can confirm that the client should posses provided tokens
     *
     * @param serviceCertificate       certificate of the service host used to encrypt the challenge
     * @param authorizationCredentials matching the set of tokens used in the business query
     * @return the required payload (the "challenge" in the challenge-response procedure)
     */
    public static ApplicationChallenge getApplicationChallenge(Certificate serviceCertificate,
                                                               Set<AuthorizationCredentials> authorizationCredentials) throws
            NoSuchAlgorithmException,
            InvalidKeyException,
            IOException,
            SignatureException {

        Long timestamp1 = ZonedDateTime.now().toInstant().toEpochMilli(); // the number of milliseconds since the epoch of 1970-01-01T00:00:00Z
        Iterator<AuthorizationCredentials> iteratorAC = authorizationCredentials.iterator();
        Set<SignedObject> signedHashesSet = new LinkedHashSet<SignedObject>();

        Signature signature = Signature.getInstance("SHA256withECDSA");

        while (iteratorAC.hasNext()) {
            AuthorizationCredentials authorizationCredentialsSetElement = iteratorAC.next();
            String hashString = authorizationCredentialsSetElement.authorizationToken.toString() + timestamp1.toString();
            String hexHash = hashSHA256(hashString);
            signedHashesSet.add(new SignedObject(hexHash, authorizationCredentialsSetElement.homeCredentials.privateKey, signature));
        }

        return new ApplicationChallenge(signedHashesSet, timestamp1);
    }


    /**
     * Used by the service to handle the challenge verification
     *
     * @param authorizationTokens  attached to the business query
     * @param applicationChallenge to be decrypted with Ppv,p containing the signatures set and timestamp1, attached
     *                             to the business query
     * @return true if the client should be in possession of the given tokens
     */
    public static boolean isApplicationChallengeVerified(Set<Token> authorizationTokens,
                                                         ApplicationChallenge applicationChallenge) throws
            NoSuchAlgorithmException,
            MalformedJWTException,
            IOException,
            SignatureException,
            InvalidKeyException,
            ClassNotFoundException {

        Long timestamp2 = ZonedDateTime.now().toInstant().toEpochMilli();

        Signature signature = Signature.getInstance("SHA256withECDSA");

        Set<SignedObject> signedHashesSet = applicationChallenge.getSignedHashesSet();
        Long timestamp1 = applicationChallenge.getTimestamp1();

        Iterator<Token> iteratorT = authorizationTokens.iterator();
        Iterator<SignedObject> iteratorSHS = signedHashesSet.iterator();

        while (iteratorT.hasNext() && iteratorSHS.hasNext()) {
            Token authorizationTokensElement = iteratorT.next();
            SignedObject signedHashesSetElement = iteratorSHS.next();

            String applicationPublicKeyPEM = JWTEngine.getClaimsFromToken(authorizationTokensElement.getToken()).getSpk();
            PublicKey applicationPublicKey = CryptoHelper.convertPEMToPublicKey(applicationPublicKeyPEM);

            signedHashesSetElement.verify(applicationPublicKey, signature);

            String challengeHash = (String) signedHashesSetElement.getObject();
            String calculatedHash = hashSHA256(authorizationTokensElement.toString() + timestamp1.toString());
            Long deltaT = timestamp2 - timestamp1;

            if (!Objects.equals(calculatedHash, challengeHash) || (deltaT >= THRESHOLD)) {
                return false;
            }
        }

        return true;
    }


    /**
     * Used by the service to generate the response payload to be encapsulated in a {@link SignedObject} required by
     * the application to confirm the service authenticity.
     *
     * @param servicePrivateKey used the sign the payload
     * @param timestamp2        used in the response payload
     * @return the required payload
     */
    public static SignedObject getServiceResponse(PrivateKey servicePrivateKey,
                                                  Long timestamp2) throws
            NoSuchAlgorithmException,
            InvalidKeyException,
            IOException,
            SignatureException {

        Signature signature = Signature.getInstance("SHA256withECDSA");
        String hashedTimestamp2 = hashSHA256(timestamp2.toString());
        ServiceResponsePayload serviceResponsePayload = new ServiceResponsePayload(hashedTimestamp2, timestamp2);

        return new SignedObject((Serializable) serviceResponsePayload, servicePrivateKey, signature);
    }

    /**
     * Used by the client to handle the {@link ServiceResponsePayload encapsulated in a {@link SignedObject}.
     *
     * @param serviceResponse       that should prove the service's authenticity
     * @param serviceCertificate    used verify the payload signature
     * @param applicationPrivateKey used to decrypt the payload
     * @return true if the service is genuine
     */
    public static boolean isServiceResponseVerified(SignedObject serviceResponse,
                                                    Certificate serviceCertificate,
                                                    PrivateKey applicationPrivateKey) throws
            NoSuchAlgorithmException,
            CertificateException,
            SignatureException,
            InvalidKeyException,
            IOException,
            ClassNotFoundException {

        Long timestamp3 = ZonedDateTime.now().toInstant().toEpochMilli();

        Signature signature = Signature.getInstance("SHA256withECDSA");

        serviceResponse.verify(serviceCertificate.getX509().getPublicKey(), signature);
        ServiceResponsePayload serviceResponsePayload = (ServiceResponsePayload) serviceResponse.getObject();

        Long timestamp2 = serviceResponsePayload.getTimestamp2();
        String hashedTimestamp2 = serviceResponsePayload.getHashedTimestamp2();

        String calculatedHash = hashSHA256(timestamp2.toString());
        Long deltaT = timestamp3 - timestamp2;

        return Objects.equals(calculatedHash, hashedTimestamp2) && deltaT < THRESHOLD;
    }

}
