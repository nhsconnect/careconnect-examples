package uk.nhs.careconnect.examples.fhir;

import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Immunization;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by kevinmayfield on 26/05/2017.
 */
public class CareConnectImmunization {

    public static Immunization buildCareConnectImmunization(Patient patient, Practitioner gp)
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Immunization immunisation = new Immunization();

        List<IdDt> profiles = new ArrayList<IdDt>();
        profiles.add(new IdDt(CareConnectSystem.ProfileImmunization));
        ResourceMetadataKeyEnum.PROFILES.put(immunisation, profiles);


        ExtensionDt dateRecorded = new ExtensionDt();
        Date recordedDate;
        try {
            recordedDate = dateFormat.parse("2017-05-27");
            dateRecorded
                    .setUrl(CareConnectSystem.ExtUrlDateRecorded)
                    .setValue(new DateTimeDt(recordedDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        immunisation.addUndeclaredExtension(dateRecorded);

        immunisation.addIdentifier()
                .setSystem("https://fhir.bristolccg.nhs.uk/DW/Immunization")
                .setValue("6bf79485-cdd5-4a20-8e4a-4bf13aba33e6");

        immunisation.setStatus("in-progress");
        Date immDate;
        try {
            immDate = dateFormat.parse("2016-03-01");
            immunisation.setDate(new DateTimeDt(immDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        CodeableConceptDt drugCode = new CodeableConceptDt();
        drugCode.addCoding()
                .setCode("396429000")
                .setSystem(CareConnectSystem.SNOMEDCT)
                .setDisplay("Measles, mumps and rubella vaccine (substance)");

        immunisation.setVaccineCode(drugCode);

        immunisation.setPatient(new ResourceReferenceDt(patient.getId().getValue()));
        immunisation.getPatient().setDisplay(patient.getName().get(0).getNameAsSingleString());

        immunisation.setWasNotGiven(false);
        immunisation.setReported(false);

        immunisation.setPerformer(new ResourceReferenceDt(gp.getId().getValue()));
        immunisation.getPerformer().setDisplay(gp.getName().getNameAsSingleString());

        immunisation.setLotNumber("63259874");
        Date expirationDate;
        try {
            expirationDate = dateFormat.parse("2020-01-01");
            immunisation.setExpirationDate(new DateDt(expirationDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Immunization.VaccinationProtocol protocol = immunisation.addVaccinationProtocol();

        protocol.setDoseSequence(1);

        protocol.addTargetDisease()
                .addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode("14189004");
        protocol.addTargetDisease()
                .addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode("36653000");
        protocol.addTargetDisease()
                .addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode("36989005");
        CodeableConceptDt doseStatus = new CodeableConceptDt();
        doseStatus.addCoding()
                .setCode("count")
                .setSystem("http://hl7.org/fhir/vaccination-protocol-dose-status")
                .setDisplay("Counts");
        protocol.setDoseStatus(doseStatus);

        return immunisation;
    }
}
