package uk.nhs.careconnect.hapiclient.App;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.dstu3.hapi.ctx.IValidationSupport;
import org.hl7.fhir.dstu3.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.dstu3.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import uk.org.hl7.fhir.validation.stu3.CareConnectProfileValidationSupport;

@SpringBootApplication
public class ClientExampleAppValidation implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ClientExampleAppValidation.class, args);
	}

    IParser XMLparser = null;

    IParser JSONparser = null;

    FhirContext ctxValidator;

    FhirValidator validator;

    FhirInstanceValidator instanceValidator;



    String HAPIServer = "http://fhirtest.uhn.ca/baseDstu2/";


    @Override
	public void run(String... args) throws Exception {

        if (args.length > 0 && args[0].equals("exitcode")) {
            throw new Exception();
        }
        FhirContext ctxFHIR = FhirContext.forDstu3();

        ctxValidator  = FhirContext.forDstu3();

        validator = ctxValidator.newValidator();
        instanceValidator = new FhirInstanceValidator();
        validator.registerValidatorModule(instanceValidator);

        IValidationSupport valSupport = new CareConnectProfileValidationSupport();
        ValidationSupportChain support = new ValidationSupportChain(new DefaultProfileValidationSupport(), valSupport);
        instanceValidator.setValidationSupport(support);

        XMLparser = ctxFHIR.newXmlParser();

        JSONparser = ctxFHIR.newJsonParser();

        FhirValidator validator = ctxFHIR.newValidator();


        // Create a client and post the transaction to the server
        IGenericClient client = ctxFHIR.newRestfulGenericClient("http://yellow.testlab.nhs.uk/careconnect-ri/STU3/");

        System.out.println("GET http://yellow.testlab.nhs.uk/careconnect-ri/STU3/Patient?identifier=https://fhir.nhs.uk/Id/nhs-number|9876543210");
        Bundle results = client
                .search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().systemAndCode("https://fhir.nhs.uk/Id/nhs-number", "9876543210"))
                .returnBundle(Bundle.class)
                .execute();
        System.out.println(JSONparser.setPrettyPrint(true).encodeResourceToString(results));

        for (Bundle.BundleEntryComponent entry : results.getEntry()) {

            if (entry.getResource() instanceof Patient) {
                Patient patient = (Patient) entry.getResource();


                System.out.println();
                validate(XMLparser.encodeResourceToString(patient));

                if (patient.getManagingOrganization() != null) {
                    Organization surgery = client
                            .read()
                            .resource(Organization.class)
                            .withId(patient.getManagingOrganization().getReference())
                            .execute();
                    System.out.println(JSONparser.setPrettyPrint(true).encodeResourceToString(surgery));
                    validate(XMLparser.encodeResourceToString(surgery));
                }
                if (patient.getGeneralPractitioner().size() > 0) {

                    Practitioner gp = client
                            .read()
                            .resource(Practitioner.class)
                            .withId(patient.getGeneralPractitioner().get(0).getReference())
                            .execute();
                    System.out.println(JSONparser.setPrettyPrint(true).encodeResourceToString(gp));
                    validate(XMLparser.encodeResourceToString(gp));
                }


            }

        }


    }

    private void validate(String resource)
    {
        ValidationResult result = validator.validateWithResult(resource);

       // System.out.println(result.isSuccessful()); // false

        // Show the issues
        for (SingleValidationMessage next : result.getMessages()) {
            switch (next.getSeverity())
            {
                case ERROR:
                    System.out.println(" Next issue " + (char)27 + "[31mERROR" + (char)27 + "[0m" + " - " +  next.getLocationString() + " - " + next.getMessage());
                    break;
                case WARNING:
                    System.out.println(" Next issue " + (char)27 + "[33mWARNING" + (char)27 + "[0m" + " - " +  next.getLocationString() + " - " + next.getMessage());
                    break;
                case INFORMATION:
                    System.out.println(" Next issue " + (char)27 + "[34mINFORMATION" + (char)27 + "[0m" + " - " +  next.getLocationString() + " - " + next.getMessage());
                    break;
                default:
                    System.out.println(" Next issue " + next.getSeverity() + " - " + next.getLocationString() + " - " + next.getMessage());
            }
        }
    }


}


