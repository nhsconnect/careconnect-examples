package uk.nhs.careconnect.fhirStarter.entities;


import java.util.Collection;
import java.util.LinkedHashSet;

public class Address  {

    private String city;

    private String county;

    private org.hl7.fhir.dstu3.model.Address.AddressUse use;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }


    private String country;


    private String postcode;


    private Collection<String> lines = new LinkedHashSet<>();

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getCity() { return this.city;}
    public void setCity(String city) { this.city = city; }

    public String getCounty() { return this.county; }
    public void setCounty(String county) { this.county = county; }

    public org.hl7.fhir.dstu3.model.Address.AddressUse getUse() {
        return use;
    }

    public void setUse(org.hl7.fhir.dstu3.model.Address.AddressUse use) {
        this.use = use;
    }

    public Collection<String> getLines() {
        return lines;
    }

    public void setLines(Collection<String> lines) {
        this.lines = lines;
    }
}
