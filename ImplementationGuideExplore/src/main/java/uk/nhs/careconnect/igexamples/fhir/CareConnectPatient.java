package uk.nhs.careconnect.igexamples.fhir;

import org.hl7.fhir.instance.model.*;
import uk.nhs.careconnect.core.dstu2.CareConnectExtension;
import uk.nhs.careconnect.core.dstu2.CareConnectProfile;
import uk.nhs.careconnect.core.dstu2.CareConnectSystem;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kevinmayfield on 26/05/2017.
 */
public class CareConnectPatient {

    public static Patient buildCareConnectPatientCSV(String csvLine, Organization practice, Practitioner gp)
    {


        String[] csvArray = csvLine.split(",");

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Patient patient = new Patient();

        patient.setMeta(new Meta().addProfile(CareConnectProfile.Patient_1));

        CodeableConcept ethnicCode = new CodeableConcept();
        ethnicCode
                .addCoding()
                .setSystem(CareConnectSystem.EthnicCategory)
                .setDisplay(csvArray[0])
                .setCode(csvArray[1]);
        Extension ethnicExtension = new Extension()
                .setUrl(CareConnectExtension.UrlEthnicCategory)
                .setValue(ethnicCode);
        patient.addExtension(ethnicExtension);

        Identifier nhsNumber = patient.addIdentifier()
                .setSystem(CareConnectSystem.NHSNumber)
                .setValue(csvArray[2]);

        CodeableConcept verificationStatusCode = new CodeableConcept();
        verificationStatusCode
                .addCoding()
                .setSystem(CareConnectSystem.NHSNumberVerificationStatus)
                .setDisplay(csvArray[3])
                .setCode(csvArray[4]);
        Extension verificationStatus = new Extension()
                .setUrl(CareConnectExtension.UrlNHSNumberVerificationStatus)
                .setValue(verificationStatusCode);
        nhsNumber.addExtension(verificationStatus);

        patient.addName()
                .setUse(HumanName.NameUse.USUAL)
                .addFamily(csvArray[5])
                .addGiven(csvArray[6])
                .addPrefix(csvArray[7]);

        patient.addAddress()
                .setUse(Address.AddressUse.HOME)
                .addLine(csvArray[8])
                .addLine(csvArray[9])
                .setCity(csvArray[10])
                .setPostalCode(csvArray[11]);

        patient.setActive(true);

        switch (csvArray[12]) {
            case "1":
                patient.setGender(Enumerations.AdministrativeGender.FEMALE);
                break;
            case "2":
                patient.setGender(Enumerations.AdministrativeGender.MALE);
                break;
            default:
                patient.setGender(Enumerations.AdministrativeGender.UNKNOWN);
        }
        CodeableConcept marital = new CodeableConcept();
        marital.addCoding().setSystem("http://hl7.org/fhir/v3/MaritalStatus").setCode("S").setDisplay("Never Married");
        patient.setMaritalStatus(marital);

        Date birth;
        try {
            birth = dateFormat.parse(csvArray[13]);
            patient.setBirthDate(birth);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        patient.setManagingOrganization(new Reference(practice.getId()));
        patient.getManagingOrganization().setDisplay(practice.getName());

        patient.addCareProvider()
                .setDisplay(gp.getName().getText())
                .setReference(gp.getId());

        return patient;
    }
}
