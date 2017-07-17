package eu.h2020.symbiote.security.communication.interfaces;

import eu.h2020.symbiote.security.commons.SecurityConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Interface exposing the SymbIoTe Component's certificate required for challenge response procedure
 *
 * @author Mikołaj Dobski (PSNC)
 *         <p>
 */
public interface IGetComponentCertificate {
    /**
     * @return Certificate of the component
     */
    @GetMapping(value = SecurityConstants.AAM_PUBLIC_PATH + SecurityConstants.AAM_GET_COMPONENT_CERTIFICATE)
    ResponseEntity<String> getComponentCertificate();
}