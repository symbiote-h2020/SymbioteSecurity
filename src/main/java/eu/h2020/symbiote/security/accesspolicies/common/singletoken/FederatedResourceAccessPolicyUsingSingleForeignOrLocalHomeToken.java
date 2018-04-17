package eu.h2020.symbiote.security.accesspolicies.common.singletoken;

import eu.h2020.symbiote.security.accesspolicies.IAccessPolicy;
import eu.h2020.symbiote.security.commons.SecurityConstants;
import eu.h2020.symbiote.security.commons.Token;
import eu.h2020.symbiote.security.commons.exceptions.custom.InvalidArgumentsException;

import java.util.HashSet;
import java.util.Set;

/**
 * SymbIoTe Access Policy that needs to be satisfied by a single Token:
 * - issued one of the federation members and containing the federation identifier claim OR
 * - a HOME token issued by the home platform
 *
 * @author Mikołaj Dobski (PSNC)
 * @author Jakub Toczek (PSNC)
 */
public class FederatedResourceAccessPolicyUsingSingleForeignOrLocalHomeToken implements IAccessPolicy {
    private final String homePlatformIdentifier;
    private final Set<String> federationMembers;
    private final String federationIdentifier;

    /**
     * Creates a new access policy object
     *
     * @param homePlatformIdentifier so that HOME tokens are properly identified
     * @param federationIdentifier identifier of the federation
     * @param federationMembers    set containing federation members identifiers
     */
    public FederatedResourceAccessPolicyUsingSingleForeignOrLocalHomeToken(Set<String> federationMembers, String homePlatformIdentifier, String federationIdentifier) throws
            InvalidArgumentsException {
        if (federationMembers == null
                || federationMembers.isEmpty()
                || homePlatformIdentifier == null
                || homePlatformIdentifier.isEmpty()
                || federationIdentifier == null
                || federationIdentifier.isEmpty()
                || !federationMembers.contains(homePlatformIdentifier))
            throw new InvalidArgumentsException("Missing federation definition contents required to build this policy type");
        this.homePlatformIdentifier = homePlatformIdentifier;
        this.federationMembers = federationMembers;
        this.federationIdentifier = federationIdentifier;
    }


    @Override
    public Set<Token> isSatisfiedWith(Set<Token> authorizationTokens) {
        // presume that none of the tokens could satisfy the policy
        Set<Token> validTokens = new HashSet<>();
        // trying to find token satisfying this policy
        for (Token token : authorizationTokens) {
            //verify if token is HOME ttyp and if token is issued by this platform
            if (token.getType().equals(Token.Type.HOME) && token.getClaims().getIssuer().equals(homePlatformIdentifier)) {
                validTokens.add(token);
                return validTokens;
            }
            // a foreign token issued by member with the proper key should be processed for searching the federation id
            if (token.getType().equals(Token.Type.FOREIGN) && federationMembers.contains(token.getClaims().getIssuer())) {
                Set<String> federationIdentifierClaims = new HashSet<>();
                for (String claimKey : token.getClaims().keySet()) {
                    if (claimKey.startsWith(SecurityConstants.SYMBIOTE_ATTRIBUTES_PREFIX + SecurityConstants.FEDERATION_CLAIM_KEY_PREFIX))
                        federationIdentifierClaims.add(token.getClaims().get(claimKey).toString());
                }
                // checking if federation claims have our needed id
                if (federationIdentifierClaims.contains(federationIdentifier)) {
                    validTokens.add(token);
                    return validTokens;
                }
            }
        }

        return validTokens;
    }
}