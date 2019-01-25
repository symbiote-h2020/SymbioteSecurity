package eu.h2020.symbiote.security.communication.payloads;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.h2020.symbiote.security.commons.enums.EventType;
import eu.h2020.symbiote.security.commons.exceptions.custom.MalformedJWTException;
import eu.h2020.symbiote.security.commons.jwt.JWTClaims;
import eu.h2020.symbiote.security.commons.jwt.JWTEngine;

import java.util.Optional;

/**
 * Class that defines structure of payload needed to log event
 *
 * @author Piotr Jakubowski (PSNC)
 */
public class EventLogRequest {

    private String username = "";
    private String clientIdentifier = "";
    private String jti = "";
    private String componentId = "";
    private String platformId = "";
    private EventType eventType;
    private long timestamp = 0;
    private String tokenString = null;
    private String reason = null;
    private String sourcePlatformId = null;

    /**
     * Standard constructor for creating EventLogRequest object prepared for serializing and deserializing.
     *
     * @param username         user's name
     * @param clientIdentifier identifier of client
     * @param jti              jti
     * @param platformId       identifier of platform verifying token
     * @param eventType        type  of incoming event
     * @param timestamp        time of event in millis
     * @param tokenString      full token
     * @param reason           text reason of rejection
     */
    @JsonCreator
    public EventLogRequest(@JsonProperty("username") String username,
                           @JsonProperty("clientIdentifier") String clientIdentifier,
                           @JsonProperty("jti") String jti,
                           @JsonProperty("componentId") String componentId,
                           @JsonProperty("platformId") String platformId,
                           @JsonProperty("eventType") EventType eventType,
                           @JsonProperty("timestamp") long timestamp,
                           @JsonProperty("tokenString") String tokenString,
                           @JsonProperty("reason") String reason) {
        this.setUsername(username);
        this.setClientIdentifier(clientIdentifier);
        this.setJti(jti);
        this.setComponentId(componentId);
        this.setPlatformId(platformId);
        this.setEventType(eventType);
        this.setTimestamp(timestamp);
        this.setTokenString(tokenString);
        this.setReason(reason);
        if (this.getTokenString() != null) {
            JWTClaims claims = null;
            try {
                claims = JWTEngine.getClaimsFromToken(this.getTokenString());
            } catch (MalformedJWTException ignored) {
            }
            if (claims != null) {
                String[] subjectParts = claims.getSub().split("@");

                switch (claims.getTtyp()) {
                    case "Home":
                        this.setSourcePlatformId(this.platformId);
                        break;
                    case "Foreign":
                        if (subjectParts.length == 4)
                            this.setSourcePlatformId(subjectParts[2]);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Alternative constructor for creating EventLogRequest object. It tries to extract username, clientIdentifier and jti from token.
     *
     * @param tokenString token from which you can extract username, clientIdentified and jti
     * @param eventType   type of incoming event
     * @param timestamp   time of event in millis
     * @param reason      text reason of rejection
     */
    public EventLogRequest(String tokenString, String platformId, EventType eventType,
                           long timestamp, String reason) {
        JWTClaims claims = null;
        try {
            claims = JWTEngine.getClaimsFromToken(tokenString);
        } catch (MalformedJWTException ignored) {
        }

        if (claims != null) {
            String[] subjectParts = claims.getSub().split("@");

            switch (claims.getTtyp()) {
                case "Home":
                    this.setSourcePlatformId(this.platformId);
                    break;
                case "Foreign":
                    if (subjectParts.length == 4)
                        this.setSourcePlatformId(subjectParts[2]);
                    break;
                default:
                    break;
            }

            this.setUsername(subjectParts[0]);

            if (subjectParts.length > 1)
                this.setClientIdentifier(subjectParts[1]);
            this.setJti(claims.getJti());
        }

        this.setPlatformId(platformId);
        this.setEventType(eventType);
        this.setTimestamp(timestamp);
        this.setTokenString(tokenString);
        this.setReason(reason);
    }

    public EventLogRequest() {
    }

    public String getUsername() {
        return username;
    }

    public String getClientIdentifier() {
        return clientIdentifier;
    }

    public String getJti() {
        return jti;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTokenString() {
        return tokenString;
    }

    public void setTokenString(String tokenString) {
        this.tokenString = Optional.ofNullable(tokenString).orElse("");
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = Optional.ofNullable(reason).orElse("");
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setClientIdentifier(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSourcePlatformId() {
        return sourcePlatformId;
    }

    public void setSourcePlatformId(String sourcePlatformId) {
        this.sourcePlatformId = sourcePlatformId;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

}