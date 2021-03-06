package eu.h2020.symbiote.security.accesspolicies.common.attributeOriented.accessRules;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.h2020.symbiote.security.accesspolicies.common.attributeOriented.accessRules.commons.AccessRuleType;
import eu.h2020.symbiote.security.accesspolicies.common.attributeOriented.accessRules.commons.IAccessRule;
import eu.h2020.symbiote.security.commons.Token;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Allows building rules which verify a value against relational operators
 *
 * @author Nemanja Ignjatov (UNIVIE)
 */
public class CompositeAccessRule implements IAccessRule {

    private Set<IAccessRule> accessRules;
    private CompositeAccessRulesOperator operator;
    private final AccessRuleType accessRuleType = AccessRuleType.COMPOSITE;

    /**
     * @param accessRules - Set of access rules contained in composite access rule
     * @param operator    - logical operator for binding access rules
     */
    public CompositeAccessRule(Set<IAccessRule> accessRules, CompositeAccessRulesOperator operator) {
        this.accessRules = accessRules;
        this.operator = operator;
    }

    public CompositeAccessRule() {
    }

    /**
     * @param accessRuleJson - String containing JSON formatted Composite access rule
     * @throws IOException -
     */
    public CompositeAccessRule(String accessRuleJson) throws IOException {
        ObjectMapper objMapper = new ObjectMapper();
        ObjectNode objNode = objMapper.readValue(accessRuleJson, ObjectNode.class);

        this.operator = CompositeAccessRulesOperator.valueOf(objNode.get("operator").asText());
        JsonNode rulesJsonNode = objNode.get("accessRules");
        if ((rulesJsonNode != null) && !rulesJsonNode.isNull()) {
            this.accessRules = new HashSet<IAccessRule>();
            for (final JsonNode ruleNode : rulesJsonNode) {
                AccessRuleType ruleType = AccessRuleType.valueOf(ruleNode.get("accessRuleType").asText());
                switch (ruleType) {
                    case BOOLEAN:
                        this.accessRules.add(new BooleanAccessRule(ruleNode.toString()));
                        break;
                    case NUMERIC:
                        this.accessRules.add(new NumericAccessRule(ruleNode.toString()));
                        break;
                    case STRING:
                        this.accessRules.add(new StringAccessRule(ruleNode.toString()));
                        break;
                    case COMPOSITE:
                        this.accessRules.add(new CompositeAccessRule(ruleNode.toString()));
                        break;
                    default:
                        break;
                }
            }
        }

    }

    @Override
    public Set<Token> isMet(Set<Token> authorizationTokens) {
        Set<Token> validTokens = new HashSet<>();
        switch (this.operator) {
            case AND:
                validTokens = validateAndRelatedAccessRules(authorizationTokens);
                break;
            case OR:
                validTokens = validateOrRelatedAccessRules(authorizationTokens);
                break;
            case NAND:
                validTokens = validateNandRelatedAccessRules(authorizationTokens);
                break;
            case NOR:
                validTokens = validateNorRelatedAccessRules(authorizationTokens);
                break;
            default:
        }

        return validTokens;
    }

    @Override
    public AccessRuleType getAccessRuleType() {
        return this.accessRuleType;
    }

    @Override
    public String toJSONString() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    public Set<IAccessRule> getAccessRules() {
        return accessRules;
    }

    public CompositeAccessRulesOperator getOperator() {
        return operator;
    }

    private Set<Token> validateAndRelatedAccessRules(Set<Token> authorizationTokens) {
        Set<Token> validTokens = new HashSet<>();
        if (this.accessRules != null) {
            int validAccessRules = 0;
            for (IAccessRule accessRule : this.accessRules) {
                Set<Token> ruleValidTokens = accessRule.isMet(authorizationTokens);
                if (ruleValidTokens != null && ruleValidTokens.size() > 0) {
                    validAccessRules++;
                    validTokens.addAll(ruleValidTokens);
                }
            }

            if (validAccessRules < this.accessRules.size()) {
                validTokens.clear();
            }
        }
        return validTokens;
    }

    private Set<Token> validateOrRelatedAccessRules(Set<Token> authorizationTokens) {
        Set<Token> validTokens = new HashSet<>();
        if (this.accessRules != null) {
            for (IAccessRule accessRule : this.accessRules) {
                validTokens.addAll(accessRule.isMet(authorizationTokens));
            }
        }
        return validTokens;
    }

    private Set<Token> validateNandRelatedAccessRules(Set<Token> authorizationTokens) {
        Set<Token> validTokens = new HashSet<>();
        if (this.accessRules != null){
            int validAccessRules = 0;
            for (IAccessRule accessRule : this.accessRules){
                Set<Token> ruleValidTokens = accessRule.isMet(authorizationTokens);
                if (ruleValidTokens != null && ruleValidTokens.size() > 0) {
                    validAccessRules++;
                    validTokens.addAll(ruleValidTokens);
                }
            }
            if(validAccessRules >= this.accessRules.size()){
                validTokens.clear();
            }
        }
        return validTokens;
    }

    private Set<Token> validateNorRelatedAccessRules(Set<Token> authorizationTokens) {
        Set<Token> validTokens = new HashSet<>();
        if(this.accessRules != null){
            int validAccessRules = 0;
            for (IAccessRule accessRule : this.accessRules) {
                Set<Token> ruleValidTokens = accessRule.isMet(authorizationTokens);
                if(ruleValidTokens != null && ruleValidTokens.size() > 0) {
                    validAccessRules++;
                }
            }
            if(validAccessRules==0){
                return authorizationTokens;
            }
        }
        return validTokens;
    }

    /**
     * Enumeration for specifying the relation operator between access rules
     *
     * @author Nemanja Ignjatov (UNIVIE)
     */
    public enum CompositeAccessRulesOperator {
        // AND logical operator for describing relations between access rules
        AND,
        // OR logical operator for describing relations between access rules
        OR,
        // NAND logical operator for describing relations between access rules
        NAND,
        // NOR logical operator for describing relations between access rules
        NOR
    }
}
