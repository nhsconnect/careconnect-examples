package uk.nhs.careconnect.examples;

import ca.uhn.fhir.context.FhirContext;

import ca.uhn.fhir.parser.IParser;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.dstu3.model.*;

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
        FhirContext ctx = FhirContext.forDstu3();
        IParser parser = ctx.newXmlParser();

        // Create a client and post the transaction to the server
        IGenericClient client = ctx.newRestfulGenericClient("https://yellow.testlab.nhs.uk/ccri-fhir/STU3/");

        System.out.println("GET https://yellow.testlab.nhs.uk/ccri-fhir/STU3/Patient?identifier=https://fhir.nhs.uk/Id/nhs-number|9876543210");
        Bundle results = client
                .search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().systemAndCode("https://fhir.nhs.uk/Id/nhs-number", "9876543210"))
                .returnBundle(Bundle.class)
                .execute();
        System.out.println(parser.setPrettyPrint(true).encodeResourceToString(results));

        for (Bundle.BundleEntryComponent entry : results.getEntry()) {

            if (entry.getResource() instanceof Patient) {
                Patient patient = (Patient) entry.getResource();


                System.out.println();
                if (patient.getManagingOrganization() != null) {
                    Organization surgery = client
                            .read()
                            .resource(Organization.class)
                            .withId(patient.getManagingOrganization().getReference())
                            .execute();
                    System.out.println(parser.setPrettyPrint(true).encodeResourceToString(surgery));
                }
                if (patient.getGeneralPractitioner().size() > 0) {

                    Practitioner gp = client
                            .read()
                            .resource(Practitioner.class)
                            .withId(patient.getGeneralPractitioner().get(0).getReference())
                            .execute();
                    System.out.println(parser.setPrettyPrint(true).encodeResourceToString(gp));
                }


                Bundle bundle = new Bundle();

                patient.setId("#1");
                Appointment appointment = new Appointment();
                appointment.addParticipant().setActor(new Reference("#1"));
                bundle.addEntry().setResource(appointment);

                bundle.addEntry().setResource(patient);

                System.out.println(parser.setPrettyPrint(true).encodeResourceToString(bundle));

                appointment = new Appointment();
                appointment.setId("1");
                appointment.addParticipant().setActor(new Reference("Patient/1"));

                System.out.println(parser.setPrettyPrint(true).encodeResourceToString(appointment));


            }

        }

    }

}
