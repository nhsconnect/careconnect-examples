package uk.nhs.careconnect.examples.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.MedicationStatement;
import ca.uhn.fhir.model.dstu2.valueset.BundleTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.MedicationStatementStatusEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.parser.IParser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by kevinmayfield on 26/05/2017.
 */
public class ExampleMedicationStatementSearchDb {

    public static MedicationStatement buildCareConnectFHIRMedicationStatement() {

        FhirContext ctxFHIR = FhirContext.forDstu2();
        IParser parser = ctxFHIR.newXmlParser();

        Bundle bundle = new Bundle();

        MedicationStatement statement = new MedicationStatement();

        statement.setId("6b980ed2-2dc6-48e4-886d-745862fc9529");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        List<IdDt> profiles = new ArrayList<IdDt>();
        profiles.add(new IdDt(CareConnectSystem.ProfileMedicationStatement));
        ResourceMetadataKeyEnum.PROFILES.put(statement, profiles);


        Date lastIssueDate;
        ExtensionDt lastIssueExtension = new ExtensionDt();
        try {
            lastIssueDate = dateFormat.parse("2001-08-20");
                lastIssueExtension
                    .setUrl(CareConnectSystem.ExtUrlMedicationStatementLastIssueDate)
                    .setValue(new DateTimeDt(lastIssueDate));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        statement.addUndeclaredExtension(lastIssueExtension);

        ExtensionDt repeatInformation = new ExtensionDt();
        ExtensionDt repeatInfReviewDate = new ExtensionDt();
        ExtensionDt repeatNumberIssues = new ExtensionDt();
        repeatInformation
                .setUrl(CareConnectSystem.ExtUrlMedicationRepeatInformation)
                .addUndeclaredExtension(repeatInfReviewDate);



        statement.setPatient(new ResourceReferenceDt("Patient/6a7d31db-0bb8-4afa-bf4c-c32d5d4b8487"));
        statement.getPatient().setDisplay("Karen Sansom");

        CodeableConceptDt drugCode = new CodeableConceptDt();
        drugCode.addCoding()
                .setCode("321153009")
                .setSystem(CareConnectSystem.SNOMEDCT)
                .setDisplay("Temazepam Tablets 20 mg");

        statement.setMedication(drugCode);

        Date assertDate;
        try {
            assertDate = dateFormat.parse("2017-05-29");
            statement.setDateAsserted(new DateTimeDt(assertDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // This is not the practitioner who prescribed the drug
        statement.getInformationSource()
                .setReference("Practitioner/651dfe43-26d6-49b3-b493-8955415912c7")
                .setDisplay("Dr Kevin Swamp");

        statement.setStatus(MedicationStatementStatusEnum.COMPLETED);

        PeriodDt period = new PeriodDt();
        Date startDate;
        Date endDate;
        try {
            startDate = dateFormat.parse("1995-09-02");
            endDate = dateFormat.parse("2001-08-20");
            period.setStart(new DateTimeDt(startDate));
            period.setEnd(new DateTimeDt(endDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        statement.setEffective(period);

        MedicationStatement.Dosage
                dosage = statement.addDosage();
        dosage.setText("1on"); // Field: Dosage

        SimpleQuantityDt quantity = new SimpleQuantityDt();
        quantity.setValue(60); // Field: Quantity
        quantity.setSystem(CareConnectSystem.SNOMEDCT);
        quantity.setCode("428673006");
        quantity.setUnit("tablets"); // Field: Quantity Units

        dosage.setQuantity(quantity);

        CodeableConceptDt additionalIns = new CodeableConceptDt();
        bundle.setTotal(1);
        bundle.setType(BundleTypeEnum.SEARCH_RESULTS);
        bundle.addEntry().setFullUrl("[baseUrl]/MedicationStatement/24964").setResource(statement);

        System.out.println(parser.setPrettyPrint(true).encodeResourceToString(bundle));
        return statement;
    }
}
