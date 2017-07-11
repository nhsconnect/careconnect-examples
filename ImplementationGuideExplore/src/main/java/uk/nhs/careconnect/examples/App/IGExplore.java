package uk.nhs.careconnect.examples.App;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.nhs.careconnect.examples.fhir.*;

@SpringBootApplication
public class IGExplore implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(IGExplore.class, args);
	}


    @Override
	public void run(String... args) throws Exception {

		if (args.length > 0 && args[0].equals("exitcode")) {
			throw new Exception();
		}

		FhirContext ctxFHIR = FhirContext.forDstu2();
        IParser parser = ctxFHIR.newXmlParser();
		// This is to base HAPI server not the CareConnectAPI
		String serverBase = "http://127.0.0.1:8080/FHIRServer/DSTU2/";

		IGenericClient client = ctxFHIR.newRestfulGenericClient(serverBase);

        Organization organisation = CareConnectOrganisation.buildCareConnectOrganisation(
                "RTG",
                "Derby Teaching Hospitals NHS Foundation Trust",
                "01332 340131",
                "Uttoxeter Road",
                "",
                    "Derby",
                    "DE22 3NE"
                    , "prov"
                );
        System.out.println(parser.setPrettyPrint(true).encodeResourceToString(organisation));
        MethodOutcome outcome = client.update().resource(organisation)
                .conditionalByUrl("Organization?identifier="+organisation.getIdentifier().get(0).getSystem()+"%7C"+organisation.getIdentifier().get(0).getValue())
                .execute();
        organisation.setId(outcome.getId());
        System.out.println(outcome.getId().getValue());

        // GP Practice
        Organization practice = CareConnectOrganisation.buildCareConnectOrganisation(
                "C81010",
                "The Moir Medical Centre",
                "0115 9737320",
                "Regent Street",
                "Long Eaton",
                "Nottingham",
                "NG10 1QQ"
                , "prov"
        );
        System.out.println(parser.setPrettyPrint(true).encodeResourceToString(practice));
        outcome = client.update().resource(practice)
                .conditionalByUrl("Organization?identifier="+practice.getIdentifier().get(0).getSystem()+"%7C"+practice.getIdentifier().get(0).getValue())
                .execute();
        practice.setId(outcome.getId());
        System.out.println(outcome.getId().getValue());


        Practitioner gp = CareConnectPractitioner.buildCareConnectPractitioner(
                "G8133438",
                "Bhatia",
                "AA",
                "Dr.",
                AdministrativeGenderEnum.MALE,
                "0115 9737320",
                "Regent Street",
                "Long Eaton",
                "Nottingham",
                "NG10 1QQ",
                practice,
                "R0260",
                "General Medical Practitioner"
        );
        System.out.println(parser.setPrettyPrint(true).encodeResourceToString(gp));
        outcome = client.update().resource(gp)
                .conditionalByUrl("Practitioner?identifier="+gp.getIdentifier().get(0).getSystem()+"%7C"+gp.getIdentifier().get(0).getValue())
                .execute();
        gp.setId(outcome.getId());
        System.out.println(outcome.getId().getValue());

        Practitioner gp2 = CareConnectPractitioner.buildCareConnectPractitioner(
                "G8650149",
                "Swamp",
                "Karren",
                "Dr.",
                AdministrativeGenderEnum.MALE,
                "0115 9737320",
                "Regent Street",
                "Long Eaton",
                "Nottingham",
                "NG10 1QQ",
                practice,
                "R0260",
                "General Medical Practitioner"
        );
        System.out.println(parser.setPrettyPrint(true).encodeResourceToString(gp2));
        outcome = client.update().resource(gp2)
                .conditionalByUrl("Practitioner?identifier="+gp2.getIdentifier().get(0).getSystem()+"%7C"+gp2.getIdentifier().get(0).getValue())
                .execute();
        gp2.setId(outcome.getId());
        System.out.println(outcome.getId().getValue());


        Patient patient = CareConnectPatient.buildCareConnectPatientCSV("British - Mixed British,01,9876543210,Number present and verified,01,Kanfeld,Bernie,Miss,10 Field Jardin,Long Eaton,Nottingham,NG10 1ZZ,1,1998-03-19"
                ,practice
                ,gp );

		System.out.println(parser.setPrettyPrint(true).encodeResourceToString(patient));
		outcome = client.update().resource(patient)
                            .conditionalByUrl("Patient?identifier="+patient.getIdentifier().get(0).getSystem()+"%7C"+patient.getIdentifier().get(0).getValue())
                            .execute();
        patient.setId(outcome.getId());
        System.out.println(outcome.getId().getValue());


        MedicationOrder prescription = CareConnectMedicationOrder.buildCareConnectMedicationOrder(patient,gp);
        System.out.println(parser.setPrettyPrint(true).encodeResourceToString(prescription));
        outcome = client.update().resource(prescription)
                .conditionalByUrl("MedicationOrder?identifier="+prescription.getIdentifier().get(0).getSystem()+"%7C"+prescription.getIdentifier().get(0).getValue())
                .execute();
        prescription.setId(outcome.getId());
        System.out.println(outcome.getId().getValue());

        MedicationStatement medicationSummary = CareConnectMedicationStatement.buildCareConnectMedicationStatement(patient,gp2);
        System.out.println(parser.setPrettyPrint(true).encodeResourceToString(medicationSummary));
        outcome = client.update().resource(medicationSummary)
                .conditionalByUrl("MedicationStatement?identifier="+medicationSummary.getIdentifier().get(0).getSystem()+"%7C"+medicationSummary.getIdentifier().get(0).getValue())
                .execute();
        medicationSummary.setId(outcome.getId());
        System.out.println(outcome.getId().getValue());


        Immunization
                immunisation = CareConnectImmunization.buildCareConnectImmunization(patient, gp);
        System.out.println(parser.setPrettyPrint(true).encodeResourceToString(immunisation));
        outcome = client.update().resource(immunisation)
                .conditionalByUrl("Immunization?identifier="+immunisation.getIdentifier().get(0).getSystem()+"%7C"+immunisation.getIdentifier().get(0).getValue())
                .execute();
        immunisation.setId(outcome.getId());
        System.out.println(outcome.getId().getValue());

        /*


		Medication medication = ExampleMedication.buildCareConnectFHIRMedication();
		System.out.println(parser.setPrettyPrint(true).encodeResourceToString(medication));

		OrderResponse
				orderResponse = ExampleOrderResponse.buildFHIROrderResponse();
		System.out.println(parser.setPrettyPrint(true).encodeResourceToString(orderResponse));

		*/

    }



}
