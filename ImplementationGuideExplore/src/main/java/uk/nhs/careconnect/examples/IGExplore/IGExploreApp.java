package uk.nhs.careconnect.examples.IGExplore;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.OrderResponse;
import ca.uhn.fhir.parser.IParser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.nhs.careconnect.examples.fhir.ExampleOrderResponse;

@SpringBootApplication
public class IGExploreApp implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(IGExploreApp.class, args);
	}


    @Override
	public void run(String... args) throws Exception {

		if (args.length > 0 && args[0].equals("exitcode")) {
			throw new Exception();
		}

		FhirContext ctxFHIR = FhirContext.forDstu2();
        IParser parser = ctxFHIR.newXmlParser();
		/*
        Patient patient = ExamplePatientCSV.buildCareConnectFHIRPatient();
		System.out.println(parser.setPrettyPrint(true).encodeResourceToString(patient));


		Patient patient = ExamplePatient.buildCareConnectFHIRPatient();
		System.out.println(parser.setPrettyPrint(true).encodeResourceToString(patient));

		Medication medication = ExampleMedication.buildCareConnectFHIRMedication();
		System.out.println(parser.setPrettyPrint(true).encodeResourceToString(medication));

        MedicationOrder prescription = ExampleMedicationOrder.buildCareConnectFHIRMedicationOrder();
        System.out.println(parser.setPrettyPrint(true).encodeResourceToString(prescription));

        MedicationStatement
				statement = ExampleMedicationStatement.buildCareConnectFHIRMedicationStatement();
		System.out.println(parser.setPrettyPrint(true).encodeResourceToString(statement));

		Immunization
				immunisation = ExampleImmunization.buildCareConnectFHIRImmunization();
		System.out.println(parser.setPrettyPrint(true).encodeResourceToString(immunisation));
		*/
		OrderResponse
				orderResponse = ExampleOrderResponse.buildFHIROrderResponse();
		System.out.println(parser.setPrettyPrint(true).encodeResourceToString(orderResponse));


		/*
		MedicationStatement
				statement = ExampleMedicationStatementSearchDb.buildCareConnectFHIRMedicationStatement();

		MedicationOrder prescription = ExampleMedicationOrderSearchDb.buildCareConnectFHIRMedicationOrderBristol();
		*/

    }



}
