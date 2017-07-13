package uk.nhs.careconnect.examples.App;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import org.hl7.fhir.dstu3.model.Task;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.nhs.careconnect.examples.fhir.CareConnectTask;

@SpringBootApplication
public class Stu3Explore implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(Stu3Explore.class, args);
	}


    @Override
	public void run(String... args) throws Exception {

		if (args.length > 0 && args[0].equals("exitcode")) {
			throw new Exception();
		}

		FhirContext ctxFHIR = FhirContext.forDstu3();
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


    }



}
