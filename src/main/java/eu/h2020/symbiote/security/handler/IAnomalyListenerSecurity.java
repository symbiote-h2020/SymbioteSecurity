package eu.h2020.symbiote.security.handler;

import eu.h2020.symbiote.security.communication.payloads.AnomalyDetectionVerbosityLevel;
import eu.h2020.symbiote.security.commons.enums.EventType;
import eu.h2020.symbiote.security.communication.payloads.EventLogRequest;
import eu.h2020.symbiote.security.communication.payloads.HandleAnomalyRequest;

/**
 * Used to manage blocked users due anomalies detection.
 *
 * @author Piotr Jakubowski (PSNC)
 */
public interface IAnomalyListenerSecurity {
    /**
     * Used to insert entry connected with detected anomaly.
     *
     * @param handleAnomalyRequest request with data about detected anomaly
     * @return true if entry successfully inserted
     */
    Boolean insertBlockedActionEntry(HandleAnomalyRequest handleAnomalyRequest);

    /**
     * Used to check if user is blocked for specified event type
     *
     * @param username
     * @param eventType
     * @return true if user is blocked for specified event type
     */
    Boolean isBlocked(String username, EventType eventType);


    AnomalyDetectionVerbosityLevel getVerbosityLevel();

    EventLogRequest prepareEventLogRequest(EventLogRequest eventLogRequest);
}