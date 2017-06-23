package uk.nhs.careconnect.examples.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.MedicationOrder;
import ca.uhn.fhir.model.dstu2.resource.MedicationStatement;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.BundleTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.MedicationOrderStatusEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.IGenericClient;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by kevinmayfield on 26/05/2017.
 */
public class ExampleMedicationOrderSearchDb {


    public static void medicationOrderQueryExample()
    {
        FhirContext ctx = FhirContext.forDstu2();
        IParser parser = ctx.newXmlParser();

        // Create a client and post the transaction to the server
        IGenericClient client = ctx.newRestfulGenericClient("http://127.0.0.1:8181/Dstu2/");

        // GET [baseUrl]\Patient?identifier=https://fhir.nhs.uk/Id/nhs-number|9439676165
        Bundle results = client
                .search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().systemAndCode(CareConnectSystem.SystemNHSNumber,"9439676165"))
                .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                .execute();

        if (results.getEntry().size()>0) {
            Patient patient = (Patient) results.getEntry().get(0).getResource();
            // GET [baseUrl]\MedicationStatement?patient=6a7d31db-0bb8-4afa-bf4c-c32d5d4b8487
            results = client
                    .search()
                    .forResource(MedicationStatement.class)
                    .where(MedicationStatement.PATIENT.hasId(patient.getId()))
                    .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                    .execute();

            if (results.getEntry().size() > 0) {
                MedicationStatement statement = (MedicationStatement) results.getEntry().get(0).getResource();
                CodeableConceptDt code = (CodeableConceptDt) statement.getMedication();
                // GET [baseUrl]\MedicationOrder?patient=6a7d31db-0bb8-4afa-bf4c-c32d5d4b8487&code=http://snomed.info/sct|321153009
                results = client
                        .search()
                        .forResource(MedicationStatement.class)
                        .where(MedicationOrder.PATIENT.hasId(patient.getId()))
                        .and(MedicationOrder.CODE.exactly().systemAndCode(CareConnectSystem.SNOMEDCT, code.getCoding().get(0).getCode()))
                        .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                        .execute();
            }
        }
    }

    public static MedicationOrder buildCareConnectFHIRMedicationOrderBristol() {

        FhirContext ctxFHIR = FhirContext.forDstu2();
        IParser parser = ctxFHIR.newXmlParser();

        MedicationOrder prescription = new MedicationOrder();

        prescription.setId("6bf79485-cee5-4a20-8e4a-4bf13aba33e6");

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        List<IdDt> profiles = new ArrayList<IdDt>();
        profiles.add(new IdDt(CareConnectSystem.ProfileMedicationOrder));
        ResourceMetadataKeyEnum.PROFILES.put(prescription, profiles);

        ExtensionDt supplyType = new ExtensionDt();
        supplyType.setUrl(CareConnectSystem.ExtUrlMedicationSupplyType);
        CodeableConceptDt supplyCode = new CodeableConceptDt();
        supplyCode.addCoding()
                .setCode("394823007")
                .setSystem(CareConnectSystem.SNOMEDCT)
                .setDisplay("NHS Prescription"); // Field: Private=false
        supplyType.setValue(supplyCode);
        prescription.addUndeclaredExtension(supplyType);

        prescription.setPatient(new ResourceReferenceDt("Patient/6a7d31db-0bb8-4afa-bf4c-c32d5d4b8487")); // Field: PatientId
        prescription.getPatient().setDisplay("Karen Sansom");

        Date issueDate;
        try {
            issueDate = dateFormat.parse("2017-05-25"); // Field: IssueDate
            prescription.setDateWritten(new DateTimeDt(issueDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // The drug hasn't been cancelled so regarding it as active. This is a mandatory CareConnect element
        prescription.setStatus(MedicationOrderStatusEnum.ACTIVE); // Field: Cancelled

        prescription.setPrescriber(new ResourceReferenceDt("Practitioner/651dfe43-26d6-49b3-b493-8955415912c7")); // Field: PractitionerId
        prescription.getPrescriber().setDisplay("Dr Kevin Swamp");

        CodeableConceptDt drugCode = new CodeableConceptDt();
        drugCode.addCoding()
                .setCode("321153009") // Field: Drug Code (SNOMED CT)
                .setSystem(CareConnectSystem.SNOMEDCT)
                .setDisplay("Temazepam Tablets 20 mg"); // Field: Drug Name

        prescription.setMedication(drugCode);

        MedicationOrder.DosageInstruction dosage = prescription.addDosageInstruction();
        dosage.setText("1on"); // Field: Dosage

        SimpleQuantityDt quantity = new SimpleQuantityDt();
        quantity.setValue(60); // Field: Quantity
        quantity.setSystem(CareConnectSystem.SNOMEDCT);
        quantity.setCode("428673006");
        quantity.setUnit("tablets"); // Field: Quantity Units

        dosage.setDose(quantity);

        Bundle bundle = new Bundle();
        bundle.setTotal(1);
        bundle.setType(BundleTypeEnum.SEARCH_RESULTS);
        bundle.addEntry().setFullUrl("[baseUrl]/MedicationOrder/24966").setResource(prescription);

        System.out.println(parser.setPrettyPrint(true).encodeResourceToString(bundle));
        return prescription;
    }
}
