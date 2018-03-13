package uk.nhs.careconnect.nosql;


import ca.uhn.fhir.context.FhirContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;



@Component
public class daoConfig {

    @Bean
    public FhirContext getFhirContext() {
        return FhirContext.forDstu3();
    }

    /*
    @Bean
    public EntityManagerFactory entityManagerFactory() {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory(
                "ogm-jpa-tutorial");

        return emf;
    }
*/

}
