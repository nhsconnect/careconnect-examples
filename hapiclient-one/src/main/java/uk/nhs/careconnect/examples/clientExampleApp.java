package uk.nhs.careconnect.examples;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Organization;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.IGenericClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by kevinmayfield on 05/06/2017.
 */
@SpringBootApplication
public class clientExampleApp implements CommandLineRunner {


    public static void main(String[] args) {
        SpringApplication.run(clientExampleApp.class, args);
    }


    public void run(String... args) throws Exception {

        if (args.length > 0 && args[0].equals("exitcode")) {
            throw new Exception();
        }

        // Create a FHIR Context
        FhirContext ctx = FhirContext.forDstu2();
        IParser parser = ctx.newXmlParser();

        // Create a client and post the transaction to the server
        IGenericClient client = ctx.newRestfulGenericClient("http://127.0.0.1:8181/Dstu2/");

        System.out.println("GET http://127.0.0.1:8181/Dstu2/Patient?identifier=https://fhir.nhs.uk/Id/nhs-number|9876543210");
        Bundle results = client
                .search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().systemAndCode("https://fhir.nhs.uk/Id/nhs-number", "9876543210"))
                .returnBundle(Bundle.class)
                .execute();
        System.out.println(parser.setPrettyPrint(true).encodeResourceToString(results));

        if (results.getEntry().size() > 0) {
            // Process first patient only.
            Patient patient = (Patient) results.getEntry().get(0).getResource();
            System.out.println("GET http://[baseUrl]/Organization24967");

            System.out.println();
            if (patient.getManagingOrganization() != null) {
                Organization surgery = client
                        .read()
                        .resource(Organization.class)
                        .withId(patient.getManagingOrganization().getReference().getValue())
                        .execute();
                System.out.println(parser.setPrettyPrint(true).encodeResourceToString(surgery));
            }
            if (patient.getCareProvider().size() > 0) {
                System.out.println("GET http://[baseUrl]/Practitoner/24965");
                Practitioner gp= client
                        .read()
                        .resource(Practitioner.class)
                        .withId(patient.getCareProvider().get(0).getReference().getValue())
                        .execute();
                System.out.println(parser.setPrettyPrint(true).encodeResourceToString(gp));
            }

        }

    }

}
