package uk.nhs.careconnect.examples.fhir;

import org.hl7.fhir.instance.model.*;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kevinmayfield on 26/05/2017.
 */
public class CareConnectMedicationStatement {

    public static MedicationStatement buildCareConnectMedicationStatement(Patient patient, Practitioner gp) {

        //http://dmd.medicines.org.uk/DesktopDefault.aspx?VMP=10097211000001102&toc=nofloat

        MedicationStatement statement = new MedicationStatement();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        statement.setMeta(new Meta().addProfile(CareConnectSystem.ProfileMedicationStatement));


        Date lastIssueDate;
        Extension lastIssueExtension = new Extension();
        try {
            lastIssueDate = dateFormat.parse("2017-03-27");
                lastIssueExtension
                    .setUrl(CareConnectSystem.ExtUrlMedicationStatementLastIssueDate)
                    .setValue(new DateTimeType(lastIssueDate));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        statement.addExtension(lastIssueExtension);

        Extension repeatInformation = new Extension();
        Extension repeatInfReviewDate = new Extension();
        Extension repeatNumberIssues = new Extension();
        repeatInformation
                .setUrl(CareConnectSystem.ExtUrlMedicationRepeatInformation)
                .addExtension(repeatInfReviewDate);

        Date reviewDate;
        try {
            reviewDate = dateFormat.parse("2017-05-27");
            repeatInfReviewDate
                    .setUrl("reviewDate")
                    .setValue(new DateTimeType(reviewDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        repeatNumberIssues
                .setUrl("numberOfRepeatsIssued")
                .setValue(new IntegerType("3"));
        repeatInformation.addExtension(repeatNumberIssues);

        statement.addIdentifier()
                .setSystem("https://fhir.bristolccg.nhs.uk/DW/MedicationStatement")
                .setValue("6b9c746e-4cce-4f5c-a2a7-0fd156dd57ac");
        statement.addExtension(repeatInformation);


        statement.setPatient(new Reference(patient.getId()));
        statement.getPatient().setDisplay(patient.getName().get(0).getText());

        CodeableConcept drugCode = new CodeableConcept();
        drugCode.addCoding()
                .setCode("321153009")
                .setSystem(CareConnectSystem.SNOMEDCT)
                .setDisplay("Temazepam Tablets 20 mg");

        statement.setMedication(drugCode);

        Date assertDate;
        try {
            assertDate = dateFormat.parse("2017-05-29");
            statement.setDateAsserted(assertDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        statement.getInformationSource()
                .setReference(gp.getId())
                .setDisplay(gp.getName().getText());

        statement.setStatus(MedicationStatement.MedicationStatementStatus.ACTIVE);


        statement.setMedication(drugCode);

        Period period = new Period();
        Date startDate;
        Date endDate;
        try {
            startDate = dateFormat.parse("1995-09-02");
            endDate = dateFormat.parse("2001-08-20");
            period.setStart(startDate);
            period.setEnd(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        statement.setEffective(period);



        MedicationStatement.MedicationStatementDosageComponent
                dosage = statement.addDosage();
        dosage.setText("1on"); // Field: Dosage

        SimpleQuantity quantity = new SimpleQuantity();
        quantity.setValue(new BigDecimal(60)); // Field: Quantity
        quantity.setSystem(CareConnectSystem.SNOMEDCT);
        quantity.setCode("428673006");
        quantity.setUnit("tablets"); // Field: Quantity Units

        dosage.setQuantity(quantity);

        CodeableConcept additionalIns = new CodeableConcept();


        return statement;
    }
}
