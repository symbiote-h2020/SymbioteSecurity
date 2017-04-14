package eu.h2020.symbiote.security.messaging.restAAM;

import eu.h2020.symbiote.security.constants.SHConstants;
import eu.h2020.symbiote.security.messaging.bean.Credential;
import eu.h2020.symbiote.security.messaging.bean.Status;
import eu.h2020.symbiote.security.messaging.bean.Token;
import feign.Headers;
import feign.RequestLine;

public interface AAMRestService {
    @RequestLine("GET " + SHConstants.GET_CORE_AAM_CA_CERTIFICATE)
    @Headers("Accept: multipart/form-data")
    byte[] getRootCertificate();

    @RequestLine("POST " + SHConstants.DO_CORE_AAM_LOGIN)
    @Headers("Content-Type: application/json")
    Token login(Credential credential);

    @RequestLine("POST " + SHConstants.DO_CORE_AAM_CHECK_TOKEN_REVOCATION)
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    Status checkTokenRevocation(Token token);

    @RequestLine("POST " + SHConstants.DO_REQUEST_CORE_TOKEN)
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    Token requestCoreToken(Token token);

	@RequestLine("POST "+SHConstants.DO_REQUEST_FOREIGN_TOKEN)
	@Headers({"Content-Type: application/json", "Accept: application/json"})
    Token requestForeignToken(Token token);

}

