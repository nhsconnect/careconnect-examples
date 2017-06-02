package uk.nhs.careconnect.examples.fhir;

import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.TimingDt;
import ca.uhn.fhir.model.dstu2.resource.MedicationStatement;
import ca.uhn.fhir.model.dstu2.valueset.MedicationStatementStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.TimingAbbreviationEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.IntegerDt;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by kevinmayfield on 26/05/2017.
 */
public class ExampleMedicationStatement {

    public static MedicationStatement buildCareConnectFHIRMedicationStatement() {

        //http://dmd.medicines.org.uk/DesktopDefault.aspx?VMP=10097211000001102&toc=nofloat

        MedicationStatement statement = new MedicationStatement();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        List<IdDt> profiles = new ArrayList<IdDt>();
        profiles.add(new IdDt("https://fhir.hl7.org.uk/StructureDefinition/CareConnect-MedicationStatement-1"));
        ResourceMetadataKeyEnum.PROFILES.put(statement, profiles);


        Date lastIssueDate;
        ExtensionDt lastIssueExtension = new ExtensionDt();
        try {
            lastIssueDate = dateFormat.parse("2017-03-27");
                lastIssueExtension
                    .setUrl("https://fhir.hl7.org.uk/StructureDefinition/Extension-CareConnect-MedicationStatementLastIssueDate-1")
                    .setValue(new DateTimeDt(lastIssueDate));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        statement.addUndeclaredExtension(lastIssueExtension);

        ExtensionDt repeatInformation = new ExtensionDt();
        ExtensionDt repeatInfReviewDate = new ExtensionDt();
        ExtensionDt repeatNumberIssues = new ExtensionDt();
        repeatInformation
                .setUrl("https://fhir.hl7.org.uk/StructureDefinition/Extension-CareConnect-MedicationRepeatInformation-1")
                .addUndeclaredExtension(repeatInfReviewDate);

        Date reviewDate;
        try {
            reviewDate = dateFormat.parse("2017-05-27");
            repeatInfReviewDate
                    .setUrl("reviewDate")
                    .setValue(new DateTimeDt(reviewDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        repeatNumberIssues
                .setUrl("numberOfRepeatsIssued")
                .setValue(new IntegerDt("3"));
        repeatInformation.addUndeclaredExtension(repeatNumberIssues);


        statement.addUndeclaredExtension(repeatInformation);


        statement.setPatient(new ResourceReferenceDt("https://pds.proxy.nhs.uk/Patient/9876543210"));
        statement.getPatient().setDisplay("Bernie Kanfeld");

        CodeableConceptDt drugCode = new CodeableConceptDt();
        drugCode.addCoding()
                .setCode("10097211000001102")
                .setSystem(CareConnectSystem.SNOMEDCT)
                .setDisplay("Insulin glulisine 100units/ml solution for injection 3ml pre-filled disposable devices");

        statement.setMedication(drugCode);

        Date assertDate;
        try {
            assertDate = dateFormat.parse("2017-05-29");
            statement.setDateAsserted(new DateTimeDt(assertDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        statement.getInformationSource()
                .setReference("https://sds.proxy.nhs.uk/Practitioner/G8040738")
                .setDisplay("Dr AD Jordan");

        statement.setStatus(MedicationStatementStatusEnum.ACTIVE);

        // This may not be workable, this list may be huge. Better option may be to query MedicationOrder for each Statement

        statement.addSupportingInformation().setReference("MedicationOrder/1710523");
       // statement.addSupportingInformation().setReference("MedicationOrder/123123");
        statement.addSupportingInformation().setReference("MedicationOrder/1232131123");

        MedicationStatement.Dosage
                dosage = statement.addDosage();

        CodeableConceptDt additionalIns = new CodeableConceptDt();

        /*
        additionalIns.addCoding()
                .setCode("1521000175104")
                .setSystem(CareConnectSystem.SNOMEDCT)
                .setDisplay("After dinner");
        dosage.set
                //setAdditionalInstructions(additionalIns);
        */
        TimingDt timing = new TimingDt();
        timing.setCode(TimingAbbreviationEnum.TID);
        dosage.setTiming(timing);
        dosage.setText("Three times a day");

        //MedicationOrder.DispenseRequest dispenseRequest = new MedicationOrder.DispenseRequest();
        //dispenseRequest.setNumberOfRepeatsAllowed(5);


        return statement;
    }
}
