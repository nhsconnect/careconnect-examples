package uk.nhs.careconnect.examples.fhir;

import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
import ca.uhn.fhir.model.dstu2.resource.MedicationStatement;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.model.dstu2.valueset.MedicationStatementStatusEnum;
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
public class CareConnectMedicationStatement {

    public static MedicationStatement buildCareConnectMedicationStatement(Patient patient, Practitioner gp) {

        //http://dmd.medicines.org.uk/DesktopDefault.aspx?VMP=10097211000001102&toc=nofloat

        MedicationStatement statement = new MedicationStatement();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        List<IdDt> profiles = new ArrayList<IdDt>();
        profiles.add(new IdDt(CareConnectSystem.ProfileMedicationStatement));
        ResourceMetadataKeyEnum.PROFILES.put(statement, profiles);


        Date lastIssueDate;
        ExtensionDt lastIssueExtension = new ExtensionDt();
        try {
            lastIssueDate = dateFormat.parse("2017-03-27");
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

        statement.addIdentifier()
                .setSystem("https://fhir.bristolccg.nhs.uk/DW/MedicationStatement")
                .setValue("6b9c746e-4cce-4f5c-a2a7-0fd156dd57ac");
        statement.addUndeclaredExtension(repeatInformation);


        statement.setPatient(new ResourceReferenceDt(patient.getId().getValue()));
        statement.getPatient().setDisplay(patient.getName().get(0).getNameAsSingleString());

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
        statement.getInformationSource()
                .setReference(gp.getId().getValue())
                .setDisplay(gp.getName().getNameAsSingleString());

        statement.setStatus(MedicationStatementStatusEnum.ACTIVE);


        statement.setMedication(drugCode);

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


        return statement;
    }
}
