package eu.h2020.symbiote.security.helpers;

import eu.h2020.symbiote.security.accesspolicies.IAccessPolicy;
import eu.h2020.symbiote.security.commons.Token;
import eu.h2020.symbiote.security.commons.exceptions.custom.MalformedJWTException;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import eu.h2020.symbiote.security.commons.exceptions.custom.ValidationException;
import eu.h2020.symbiote.security.communication.payloads.ABACResolverResponse;
import eu.h2020.symbiote.security.communication.payloads.SecurityCredentials;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;

import java.util.*;

/**
 * Created by Nemanja on 18.08.2017.
 * <p>
 * Utility class that provides access policy validation functioncality for required set of resources
 *
 * @author Mikołaj Dobski (PSNC)
 * @author Nemanja Ignjatov (UNIVIE)
 */
public class ABACPolicyHelper {

    /**
     * @param accessPolicies  of the resources that need to be checked against the tokens
     * @param securityRequest container for tokens and user credentials which will be checked against access policies
     * @return set of resources (their identifiers) whose access policies are satisfied with the given credentials
     */
    public static ABACResolverResponse checkRequestedOperationAccess(String deploymentId, Map<String, IAccessPolicy> accessPolicies,
                                                                     SecurityRequest securityRequest) throws SecurityHandlerException {
        ABACResolverResponse abacResp = new ABACResolverResponse();
        // extracting credentials from the security request
        Map<Token, SecurityCredentials> authzCredentials = new HashMap<Token, SecurityCredentials>(securityRequest.getSecurityCredentials().size());
        Set<String> authorizedResourcesIdentifiers = new HashSet<String>();
        for (SecurityCredentials securityCredentials : securityRequest.getSecurityCredentials()) {
            try {
                authzCredentials.put(new Token(securityCredentials.getToken()), securityCredentials);
            } catch (ValidationException e) {
                e.printStackTrace();
                throw new SecurityHandlerException("Failed to recreate tokens for ABAC resolution, they got expired or corrupted: " + e.getMessage());
            }
        }

        if (accessPolicies != null) {
            for (Map.Entry<String, IAccessPolicy> resource : accessPolicies.entrySet()) {
                if (resource.getValue() != null) {
                    Token validToken = resource.getValue().isSatisfiedWith(deploymentId, authzCredentials.keySet());
                    //Check if any valid token is found for the access policy
                    if (validToken != null) {
                        abacResp.getAvailableResources().add(resource.getKey());
                        abacResp.getValidCredentials().add(authzCredentials.get(validToken));
                    }
                } else {
                    abacResp.getAvailableResources().add(resource.getKey());
                    abacResp.getValidCredentials().addAll(authzCredentials.values());
                }
            }
        }
        return abacResp;

    }
}
