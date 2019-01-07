package uk.nhs.careconnect.edms;


import ca.uhn.fhir.context.FhirContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;



@Component
public class daoConfig {

    @Bean
    public FhirContext getFhirContext() {
        return FhirContext.forDstu3();
    }



}
