package eu.h2020.symbiote.security;

import eu.h2020.symbiote.security.certificate.Certificate;
import eu.h2020.symbiote.security.enums.ValidationStatus;
import eu.h2020.symbiote.security.exceptions.custom.SecurityHandlerException;
import eu.h2020.symbiote.security.session.AAM;
import eu.h2020.symbiote.security.token.Token;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.security.SignedObject;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Security Handler interface proposed for Release 3 of SymbIoTe.
 *
 * @author Daniele Caldarola (CNIT)
 * @author Mikołaj Dobski (PSNC)
 * @author Nemanja Ignjatov (UNIVIE)
 * @author Pietro Tedeschi (CNIT)
 */
public interface ISecurityHandler {

    /**
     * @return list of all currently available security entrypoints to symbiote (getCertificate, login, token
     * validation)
     * @throws SecurityHandlerException on operation error
     */
    List<AAM> getAvailableAAMs() throws SecurityHandlerException;

    /**
     * Retrieves your home token from the given AAM you have account in.
     *
     * @param homeAAM      to request the token from
     * @param loginRequest containing the username and client id signed with your private Key
     * @return home token
     * @throws SecurityHandlerException on operation error
     */
    Token login(AAM homeAAM, SignedObject loginRequest) throws SecurityHandlerException;

    /**
     * Login to foreign AAMs (you don't have account in) using home token.
     *
     * @param foreignAAMs to get the Tokens from
     * @param homeToken   to use as login credentialsWallet
     * @param certificate if the operation is in an intranet environment, then the user needs to provide the
     *                    certificate matching the one from the homeToken
     * @return map of the foreign tokens that were acquired using a given home token
     * @throws SecurityHandlerException on operation error
     */
    Map<AAM, Token> login(List<AAM> foreignAAMs, Token homeToken, Optional<Certificate> certificate)
            throws SecurityHandlerException;

    /**
     * @param aam Authentication and Authorization Manager to request guest token from
     * @return guest token that allows access to all public resources in symbIoTe
     */
    Token loginAsGuest(AAM aam);

    /**
     * Removes all the acquired tokens from memory
     */
    void clearCachedTokens();

    /**
     * Used to acquire a certificate for this client from the home AAM
     *
     * @param username  of the user in the home AAM
     * @param password  of the user in the home AAM
     * @param clientId  that will be bound with the user and this client
     * @param clientCSR Certificate Signing Request required to issue a certificate for this client, it should be
     *                  Base64 encoded String of @{@link PKCS10CertificationRequest#getEncoded()}
     * @return certificate used by this client for challenge-response operations
     * @throws SecurityHandlerException on operation error
     */
    Certificate getCertificate(AAM homeAAM,
                               String username,
                               String password,
                               String clientId,
                               String clientCSR)
            throws SecurityHandlerException;

    /**
     * @param validationAuthority where the token should be validated (ideally it should be the token issuer authority)
     * @param token               to be validated
     * @param certificate         if the operation is in an intranet environment, then the user needs to provide the
     *                            certificate matching the one from the homeToken
     * @return validation status of the given token
     */
    ValidationStatus validate(AAM validationAuthority, String token, Optional<Certificate> certificate);
}
