package eu.h2020.symbiote.security.helpers;

import eu.h2020.symbiote.security.commons.credentials.HomeCredentials;
import eu.h2020.symbiote.security.commons.enums.ValidationStatus;
import eu.h2020.symbiote.security.commons.exceptions.custom.JWTCreationException;
import eu.h2020.symbiote.security.commons.exceptions.custom.MalformedJWTException;
import eu.h2020.symbiote.security.commons.exceptions.custom.ValidationException;
import eu.h2020.symbiote.security.commons.jwt.JWTClaims;
import eu.h2020.symbiote.security.commons.jwt.JWTEngine;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCSException;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;

import static org.junit.Assert.*;

/**
 * Created by Jakub on 20.07.2017.
 */

public class CryptoHelperTest {

    private final String username = "testusername";
    private final String clientId = "testclientid";
    private static final String CERTIFICATE_ALIAS = "core-2";
    private static final String CERTIFICATE_LOCATION = "./src/test/resources/platform_1.p12";
    private static final String CERTIFICATE_PASSWORD = "1234567";

    @Before
    public void setUp() throws Exception {
        ECDSAHelper.enableECDSAProvider();
    }

    @Test
    public void buildLoginRequestTest() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, JWTCreationException, MalformedJWTException, ValidationException, CertificateException {
        KeyPair keyPair = CryptoHelper.createKeyPair();
        HomeCredentials homeCredentials = new HomeCredentials(null, username, clientId, null, keyPair.getPrivate());
        String loginRequest = CryptoHelper.buildHomeTokenAcquisitionRequest(homeCredentials);
        JWTClaims claims = JWTEngine.getClaimsFromToken(loginRequest);
        assertEquals(homeCredentials.username, claims.getIss());
        assertEquals(homeCredentials.clientIdentifier, claims.getSub());
        assertEquals(ValidationStatus.VALID, JWTEngine.validateTokenString(loginRequest, keyPair.getPublic()));
    }

    @Test
    public void buildCertificateSigningRequestTest() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, KeyStoreException, IOException, CertificateException, OperatorCreationException, PKCSException {
        KeyPair keyPair = CryptoHelper.createKeyPair();
        KeyStore ks = KeyStore.getInstance("PKCS12", "BC");
        ks.load(new FileInputStream(CERTIFICATE_LOCATION), CERTIFICATE_PASSWORD.toCharArray());
        X509Certificate certificate = (X509Certificate) ks.getCertificate(CERTIFICATE_ALIAS);
        String csr = CryptoHelper.buildCertificateSigningRequest(certificate, username, clientId, keyPair);
        assertNotNull(csr);
        byte[] bytes = Base64.getDecoder().decode(csr);
        PKCS10CertificationRequest req = new PKCS10CertificationRequest(bytes);
        assertEquals(username + "@" + clientId + "@" + certificate.getSubjectX500Principal().getName().split("CN=")[1].split(",")[0], req.getSubject().toString().split("CN=")[1]);
        assertTrue(req.isSignatureValid(new JcaContentVerifierProviderBuilder().setProvider("BC").build(keyPair.getPublic())));
    }
}