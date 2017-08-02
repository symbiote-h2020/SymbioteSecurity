package eu.h2020.symbiote.security.handler;

import eu.h2020.symbiote.security.commons.Certificate;
import eu.h2020.symbiote.security.commons.enums.ValidationStatus;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import eu.h2020.symbiote.security.communication.interfaces.payloads.AAM;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;

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
     * @return map of all currently available security entrypoints to symbiote (getCertificate, login, token
     * validation)
     * @throws SecurityHandlerException on operation error
     */
    Map<String, AAM> getAvailableAAMs(AAM homeAAM) throws SecurityHandlerException;

    /**
     * Retrieves your home token from the given AAM you have account in.
     *
     * @param aam AAM instance to get a home token from
     * @param user user to get the token
     * @param clientId client id for the token
     * @return home token
     * @throws SecurityHandlerException on operation error
     */
    public String login(AAM aam, String user, String clientId) throws SecurityHandlerException;

    /**
     * Login to foreign AAMs (you don't have account in) using home token.
     *
     * @param foreignAAMs to get the Tokens from
     * @param homeToken  used to aquire foreignToken
     * @return map of the foreign tokens that were acquired using a given home token
     * @throws SecurityHandlerException on operation error
     */
    Map<AAM, String> login(List<AAM> foreignAAMs, String homeToken)
            throws SecurityHandlerException;

    /**
     * @param aam Authentication and Authorization Manager to request guest token from
     * @return guest token that allows access to all public resources in symbIoTe
     */
    String loginAsGuest(AAM aam);

    /**
     * Removes all the acquired tokens from memory
     */
    void clearCachedTokens();

    /**
     * Used to acquire a certificate(PKI) for this client from the home AAM
     * This private key will be used to sign-off the request to AAM
     *
     * @param homeAAM   the Authenticantion and Authorization Manager the user has account in
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
     * Used to validate a Token to the AAM
     * 
     * @param validationAuthority where the token should be validated (ideally it should be the token issuer authority)
     * @param token               to be validated
     * @param certificate         if the operation is in an intranet environment, then the user needs to provide the
     *                            certificate matching the one from the homeToken
     * @return validation status of the given token
     */
    ValidationStatus validate(AAM validationAuthority, String token, Optional<Certificate> certificate);
}
