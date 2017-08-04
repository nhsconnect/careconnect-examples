package uk.nhs.careconnect.igexamples.fhir;

import org.hl7.fhir.instance.model.*;
import uk.nhs.careconnect.core.dstu2.CareConnectExtension;
import uk.nhs.careconnect.core.dstu2.CareConnectSystem;
import uk.nhs.careconnect.core.dstu2.CareConnectProfile;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kevinmayfield on 26/05/2017.
 */
public class CareConnectImmunization {

    public static Immunization buildCareConnectImmunization(Patient patient, Practitioner gp)
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Immunization immunisation = new Immunization();


        immunisation.setMeta(new Meta().addProfile(CareConnectProfile.Immunization_1));


        Extension dateRecorded = new Extension();
        Date recordedDate;
        try {
            recordedDate = dateFormat.parse("2017-05-27");
            dateRecorded
                    .setUrl(CareConnectExtension.UrlDateRecorded)
                    .setValue(new DateTimeType(recordedDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        immunisation.addExtension(dateRecorded);

        immunisation.addIdentifier()
                .setSystem("https://fhir.bristolccg.nhs.uk/DW/Immunization")
                .setValue("6bf79485-cdd5-4a20-8e4a-4bf13aba33e6");

        immunisation.setStatus("in-progress");
        Date immDate;
        try {
            immDate = dateFormat.parse("2016-03-01");
            immunisation.setDate(immDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        CodeableConcept drugCode = new CodeableConcept();
        drugCode.addCoding()
                .setCode("396429000")
                .setSystem(CareConnectSystem.SNOMEDCT)
                .setDisplay("Measles, mumps and rubella vaccine (substance)");

        immunisation.setVaccineCode(drugCode);

        immunisation.setPatient(new Reference(patient.getId()));
        immunisation.getPatient().setDisplay(patient.getName().get(0).getText());

        immunisation.setWasNotGiven(false);
        immunisation.setReported(false);

        immunisation.setPerformer(new Reference(gp.getId()));
        immunisation.getPerformer().setDisplay(gp.getName().getText());

        immunisation.setLotNumber("63259874");
        Date expirationDate;
        try {
            expirationDate = dateFormat.parse("2020-01-01");
            immunisation.setExpirationDate(expirationDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Immunization.ImmunizationVaccinationProtocolComponent protocol = immunisation.addVaccinationProtocol();

        protocol.setDoseSequence(1);

        protocol.addTargetDisease()
                .addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode("14189004");
        protocol.addTargetDisease()
                .addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode("36653000");
        protocol.addTargetDisease()
                .addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode("36989005");
        CodeableConcept doseStatus = new CodeableConcept();
        doseStatus.addCoding()
                .setCode("count")
                .setSystem("http://hl7.org/fhir/vaccination-protocol-dose-status")
                .setDisplay("Counts");
        protocol.setDoseStatus(doseStatus);

        return immunisation;
    }
}
