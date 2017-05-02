package eu.h2020.symbiote.core.model.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.h2020.symbiote.core.model.Location;

import java.util.List;

/**
 * Represents CIM-defined Mobile Sensor class.
 *
 * Created by Mael on 28/03/2017.
 */
public class MobileSensor extends Resource {

    @JsonProperty("locatedAt")
    private Location locatedAt;
    @JsonProperty("observesProperty")
    private List<String> observesProperty;

    public MobileSensor() {
    }

    public Location getLocatedAt() {
        return locatedAt;
    }

    public void setLocatedAt(Location locatedAt) {
        this.locatedAt = locatedAt;
    }

    public List<String> getObservesProperty() {
        return observesProperty;
    }

    public void setObservesProperty(List<String> observesProperty) {
        this.observesProperty = observesProperty;
    }
}
