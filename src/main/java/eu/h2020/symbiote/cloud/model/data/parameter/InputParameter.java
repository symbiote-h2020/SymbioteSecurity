package eu.h2020.symbiote.cloud.model.data.parameter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents CIM-defined InputParameter parameter class
 *
 * Created by Mael on 28/03/2017.
 */
public class InputParameter extends Parameter {

    @JsonProperty("name")
    private final String name;
    @JsonIgnore
    private boolean mandatory;
    @JsonIgnore
    private List<Restriction> restrictions;
    @JsonProperty("value")
    private String value;

    
    public InputParameter(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public List<Restriction> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(List<Restriction> restrictions) {
        this.restrictions = restrictions;
    }
    
    public void setParamValue(String paramValue) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
