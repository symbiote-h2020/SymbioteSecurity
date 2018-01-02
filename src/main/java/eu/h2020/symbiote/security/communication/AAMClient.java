package eu.h2020.symbiote.security.communication;

import eu.h2020.symbiote.security.commons.SecurityConstants;
import eu.h2020.symbiote.security.commons.credentials.HomeCredentials;
import eu.h2020.symbiote.security.commons.enums.ManagementStatus;
import eu.h2020.symbiote.security.commons.enums.ValidationStatus;
import eu.h2020.symbiote.security.commons.exceptions.custom.*;
import eu.h2020.symbiote.security.communication.interfaces.IFeignAAMClient;
import eu.h2020.symbiote.security.communication.payloads.*;
import feign.Feign;
import feign.FeignException;
import feign.Response;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

import java.util.Optional;

/**
 * Crude RMI-like client's implementation to the AAM module that communicates with it over REST.
 *
 * @author Dariusz Krajewski (PSNC)
 */
public class AAMClient implements IAAMClient {

    private static final String AAM_COMMS_ERROR_MESSAGE = "Failed to communicate with the AAM: ";
    private String serverAddress;
    private IFeignAAMClient feignClient;

    /**
     * @param serverAddress of the AAM server the client wants to interact with.
     */
    public AAMClient(String serverAddress) {
        this.serverAddress = serverAddress;
        this.feignClient = getJsonClient();
    }

    /**
     * @return Instance of feign client with all necessary parameters set
     */
    private IFeignAAMClient getJsonClient() {
        return Feign.builder().encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
                .target(IFeignAAMClient.class, serverAddress);
    }


    /**
     * @param componentIdentifier component identifier or {@link SecurityConstants#AAM_COMPONENT_NAME} for AAM CA certificate
     * @param platformIdentifier  for a platform component or {@link SecurityConstants#CORE_AAM_INSTANCE_ID} for Symbiote core components
     * @return symbiote component Certificate of the component in PEM format
     */
    @Override
    public String getComponentCertificate(String componentIdentifier,
                                          String platformIdentifier) throws
            AAMException {
        Response response;
        try {
            response = feignClient.getComponentCertificate(componentIdentifier, platformIdentifier);
        } catch (FeignException fe) {
            throw new AAMException(AAM_COMMS_ERROR_MESSAGE + fe.getMessage());
        }
        if (response.status() == 500)
            throw new AAMException(response.body().toString());
        if (response.status() == 404)
            throw new AAMException("Certificate not found");
        return response.body().toString();

    }

    /**
     * Allows the user to acquire their client's certificate.
     *
     * @param certificateRequest required to issue a certificate for given (username, clientId) tupple.
     * @return the signed certificate from the provided CSR in PEM format
     */
    @Override
    public String signCertificateRequest(CertificateRequest certificateRequest) throws
            NotExistingUserException,
            ValidationException,
            InvalidArgumentsException,
            AAMException {
        Response response;
        try {
            response = feignClient.signCertificateRequest(certificateRequest);
        } catch (FeignException fe) {
            throw new AAMException(AAM_COMMS_ERROR_MESSAGE + fe.getMessage());
        }
        switch (response.status()) {
            case 400:
                if (response.body().toString().contains("INVALID_ARGUMENTS"))
                    throw new InvalidArgumentsException(response.body().toString());
                throw new NotExistingUserException(response.body().toString());
            case 401:
                //TODO: Find a way to differentiate ValidationException from WrongCredentialsException since response's body is empty on error
                throw new ValidationException("Could not validate - Invalid certificate / credentials");
            default:
                return response.body().toString();
        }

    }

    /**
     * Allows the user to revoke their credentials
     *
     * @param revocationRequest required to revoke a certificate or token.
     * @return the signed certificate from the provided CSR in PEM format
     */
    @Override
    public String revokeCredentials(RevocationRequest revocationRequest) throws
            InvalidArgumentsException,
            WrongCredentialsException,
            AAMException {
        Response response;
        try {
            response = feignClient.revokeCredentials(revocationRequest);
        } catch (FeignException fe) {
            throw new AAMException(AAM_COMMS_ERROR_MESSAGE + fe.getMessage());
        }
        switch (response.status()) {
            case 400:
                throw new InvalidArgumentsException(response.body().toString());
            case 401:
                throw new WrongCredentialsException();
            default:
                return response.body().toString();
        }

    }

    /**
     * Sends information about event to Anomaly Detection Module
     *
     * @param eventLogRequest contains information about event
     * @return true/false depending on event report status
     */
    @Override
    public String logAnomalyEvent(EventLogRequest eventLogRequest) {
        if(eventLogRequest == null)
            return "";
        Response response = feignClient.logAnomalyEvent(eventLogRequest);
        return response.body().toString();
    }

    /**
     * @return GUEST token used to access public resources offered in SymbIoTe
     */
    @Override
    public String getGuestToken() throws
            JWTCreationException,
            AAMException {
        Response response;
        try {
            response = feignClient.getGuestToken();
        } catch (FeignException fe) {
            throw new AAMException(AAM_COMMS_ERROR_MESSAGE + fe.getMessage());
        }
        if (response.status() == 500)
            throw new JWTCreationException("Server failed to create a guest token");
        return response.headers().get(SecurityConstants.TOKEN_HEADER_NAME).toString();
    }

    /**
     * @param loginRequest JWS build in accordance to @{@link eu.h2020.symbiote.security.helpers.CryptoHelper#buildHomeTokenAcquisitionRequest(HomeCredentials)}
     *                     and http://www.smarteremc2.eu/colab/display/SYM/Home+Authorization+Token+acquisition+%28home+login%29+request
     * @return HOME token used to access restricted resources offered in SymbIoTe
     */
    @Override
    public String getHomeToken(String loginRequest) throws
            WrongCredentialsException,
            JWTCreationException,
            MalformedJWTException,
            AAMException {
        Response response;
        try {
            response = feignClient.getHomeToken(loginRequest);
        } catch (FeignException fe) {
            throw new AAMException(AAM_COMMS_ERROR_MESSAGE + fe.getMessage());
        }
        switch (response.status()) {
            case 400:
                throw new MalformedJWTException("Unable to read malformed token");
            case 401:
                throw new WrongCredentialsException("Could not validate token with incorrect credentials");
            case 500:
                throw new JWTCreationException("Server failed to create a home token");
            default:
                return response.headers().get(SecurityConstants.TOKEN_HEADER_NAME).toArray()[0].toString();
        }
    }

    /**
     * @param remoteHomeToken   that an actor wants to exchange in this AAM for a FOREIGN token
     * @param clientCertificate in PEM with key matching the SPK claim in the provided token in 'offline' (intranet) scenarios
     * @param aamCertificate    in PEM with key matching the IPK claim in the provided token in 'offline' (intranet) scenarios
     * @return FOREIGN token used to access restricted resources offered in SymbIoTe federations
     */
    @Override
    public String getForeignToken(String remoteHomeToken,
                                  Optional<String> clientCertificate,
                                  Optional<String> aamCertificate) throws
            ValidationException,
            JWTCreationException,
            AAMException {
        Response response;
        try {
            response = feignClient.getForeignToken(
                    remoteHomeToken,
                    clientCertificate.orElse("").replace("\n", "").replace("\r", ""),
                    aamCertificate.orElse("").replace("\n", "").replace("\r", ""));
        } catch (FeignException fe) {
            throw new AAMException(AAM_COMMS_ERROR_MESSAGE + fe.getMessage());
        }
        switch (response.status()) {
            case 401:
                throw new ValidationException("Failed to validate homeToken");
            case 500:
                throw new JWTCreationException("Server failed to create a foreign token");
            default:
                return response.headers().get(SecurityConstants.TOKEN_HEADER_NAME).toArray()[0].toString();
        }

    }

    /**
     * @return collection of AAMs available in the SymbIoTe ecosystem
     */
    @Override
    public AvailableAAMsCollection getAvailableAAMs() throws AAMException {
        try {
            return feignClient.getAvailableAAMs();
        } catch (FeignException fe) {
            throw new AAMException(AAM_COMMS_ERROR_MESSAGE + fe.getMessage());
        }
    }

    /**
     * @return collection of AAMs available in the SymbIoTe ecosystem
     */
    @Override
    public AvailableAAMsCollection getAAMsInternally() throws AAMException {
        try {
            return feignClient.getAAMsInternally();
        } catch (FeignException fe) {
            throw new AAMException(AAM_COMMS_ERROR_MESSAGE + fe.getMessage());
        }
    }

    /**
     * @param token                                  that is to be validated
     * @param clientCertificate                      in PEM with key matching the SPK claim in the provided token in 'offline' (intranet) scenarios
     * @param clientCertificateSigningAAMCertificate in PEM being the AAM that signed the clientCertificate  in 'offline' (intranet) scenarios
     * @param foreignTokenIssuingAAMCertificate      in PEM with key matching the IPK claim in the provided FOREIGN token in 'offline' (intranet) scenarios
     * @return validation status
     */
    @Override
    public ValidationStatus validateCredentials(String token,
                                                Optional<String> clientCertificate,
                                                Optional<String> clientCertificateSigningAAMCertificate,
                                                Optional<String> foreignTokenIssuingAAMCertificate) throws
            AAMException {
        try {
            return feignClient.validateCredentials(
                    token,
                    clientCertificate.orElse("").replace("\n", "").replace("\r", ""),
                    clientCertificateSigningAAMCertificate.orElse("").replace("\n", "").replace("\r", ""),
                    foreignTokenIssuingAAMCertificate.orElse("").replace("\n", "").replace("\r", ""));
        } catch (FeignException fe) {
            throw new AAMException(AAM_COMMS_ERROR_MESSAGE + fe.getMessage());
        }
    }

    /**
     * @param platformManagementRequest related to associated platforms' management operation.
     * @return Management Status informing about a result of completing requested management operation
     */
    @Override
    public ManagementStatus managePlatform(PlatformManagementRequest platformManagementRequest) throws
            AAMException {
        try {
            return feignClient.managePlatform(platformManagementRequest).getRegistrationStatus();
        } catch (FeignException fe) {
            throw new AAMException(AAM_COMMS_ERROR_MESSAGE + fe.getMessage());
        } catch (Exception e) {
            throw new AAMException("Internal Platform Management Error");
        }
    }

    /**
     * @param userManagementRequest related to associated users' management operation.
     * @return Management Status informing about a result of completing requested management operation
     */
    @Override
    public ManagementStatus manageUser(UserManagementRequest userManagementRequest) throws
            AAMException {
        try {
            return feignClient.manageUser(userManagementRequest);
        } catch (FeignException fe) {
            throw new AAMException(AAM_COMMS_ERROR_MESSAGE + fe.getMessage());
        } catch (Exception e) {
            throw new AAMException("Internal User Management Error");
        }
    }

    /**
     * @param credentials of a user whose details should be returned
     * @return details of requested user
     */
    @Override
    public UserDetails getUserDetails(Credentials credentials) throws
            UserManagementException,
            AAMException {
        try {
            return feignClient.getUserDetails(credentials);
        } catch (FeignException exc) {
            switch (exc.status()) {
                case 400: //Bad Request, Requested user does not exist
                    throw new UserManagementException("Requested user is not in database");
                case 401: //Error 401 - Unauthorized, Wrong password was provided for existing user
                    throw new UserManagementException("Wrong password was provided");
                default:
                    throw new AAMException(AAM_COMMS_ERROR_MESSAGE + exc.getMessage());
            }
        }
    }

}
