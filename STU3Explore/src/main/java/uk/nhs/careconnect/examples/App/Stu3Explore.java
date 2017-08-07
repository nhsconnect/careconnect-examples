package uk.nhs.careconnect.examples.App;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.dstu3.model.Task;
import org.hl7.fhir.instance.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.instance.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.instance.hapi.validation.IValidationSupport;
import org.hl7.fhir.instance.hapi.validation.ValidationSupportChain;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.nhs.careconnect.examples.fhir.CareConnectTask;
import uk.nhs.careconnect.validation.stu3.CareConnectValidation;

@SpringBootApplication
public class Stu3Explore implements CommandLineRunner {


	FhirContext ctxFHIR;

	FhirValidator validator;

	FhirInstanceValidator instanceValidator;

	public static void main(String[] args) {
		SpringApplication.run(Stu3Explore.class, args);
	}


    @Override
	public void run(String... args) throws Exception {

		if (args.length > 0 && args[0].equals("exitcode")) {
			throw new Exception();
		}

        FhirContext ctxFHIR = FhirContext.forDstu3();



        validator = ctxFHIR.newValidator();
        instanceValidator = new FhirInstanceValidator();
        validator.registerValidatorModule(instanceValidator);

        IValidationSupport valSupport = new CareConnectValidation();
        ValidationSupportChain support = new ValidationSupportChain(new DefaultProfileValidationSupport(), valSupport);
        instanceValidator.setValidationSupport(support);

        FhirValidator validator = ctxFHIR.newValidator();
		//
        // FhirContext ctxFHIR = FhirContext.forDstu3();
        IParser parser = ctxFHIR.newXmlParser();

		String serverBase = "http://fhirtest.uhn.ca/baseDstu3/";

		IGenericClient client = ctxFHIR.newRestfulGenericClient(serverBase);

        Task task = CareConnectTask.buildFHIRTask();
        System.out.println(parser.setPrettyPrint(true).encodeResourceToString(task));
        MethodOutcome outcome = client.update().resource(task)
                .conditionalByUrl("Task?identifier="+task.getIdentifier().get(0).getSystem()+"%7C"+task.getIdentifier().get(0).getValue())
                .execute();
        task.setId(outcome.getId());
        System.out.println(outcome.getId().getValue());

        //Validate v = new Validate();
        //v.
        validate(parser.setPrettyPrint(true).encodeResourceToString(task));

    }


    private void validate(String resource)
    {
        ValidationResult result = validator.validateWithResult(resource);

        // System.out.println(result.isSuccessful()); // false

        // Show the issues
        for (SingleValidationMessage next : result.getMessages()) {
            switch (next.getSeverity())
            {
                case FATAL:
                    System.out.println(" Next issue " + (char)27 + "[31mFATAL" + (char)27 + "[0m" + " - " +  next.getLocationString() + " - " + next.getMessage());
                    break;
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
