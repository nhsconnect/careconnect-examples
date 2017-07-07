package uk.nhs.careconnect.examples.fhir;

import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Organization;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.model.dstu2.valueset.AddressUseEnum;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.MaritalStatusCodesEnum;
import ca.uhn.fhir.model.dstu2.valueset.NameUseEnum;
import ca.uhn.fhir.model.primitive.DateDt;
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
public class CareConnectPatient {

    public static Patient buildCareConnectPatientCSV(String csvLine, Organization practice, Practitioner gp)
    {


        String[] csvArray = csvLine.split(",");

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Patient patient = new Patient();

        List<IdDt> profiles = new ArrayList<IdDt>();
        profiles.add(new IdDt(CareConnectSystem.ProfilePatient));
        ResourceMetadataKeyEnum.PROFILES.put(patient, profiles);

        CodeableConceptDt ethnicCode = new CodeableConceptDt();
        ethnicCode
                .addCoding()
                .setSystem(CareConnectSystem.SystemEthnicCategory)
                .setDisplay(csvArray[0])
                .setCode(csvArray[1]);
        ExtensionDt ethnicExtension = new ExtensionDt()
                .setUrl(CareConnectSystem.ExtUrlEthnicCategory)
                .setValue(ethnicCode);
        patient.addUndeclaredExtension(ethnicExtension);

        IdentifierDt nhsNumber = patient.addIdentifier()
                .setSystem(CareConnectSystem.SystemNHSNumber)
                .setValue(csvArray[2]);

        CodeableConceptDt verificationStatusCode = new CodeableConceptDt();
        verificationStatusCode
                .addCoding()
                .setSystem(CareConnectSystem.SystemNHSNumberVerificationStatus)
                .setDisplay(csvArray[3])
                .setCode(csvArray[4]);
        ExtensionDt verificationStatus = new ExtensionDt()
                .setUrl(CareConnectSystem.ExtUrlNHSNumberVerificationStatus)
                .setValue(verificationStatusCode);
        nhsNumber.addUndeclaredExtension(verificationStatus);

        patient.addName()
                .setUse(NameUseEnum.USUAL)
                .addFamily(csvArray[5])
                .addGiven(csvArray[6])
                .addPrefix(csvArray[7]);

        patient.addAddress()
                .setUse(AddressUseEnum.HOME)
                .addLine(csvArray[8])
                .addLine(csvArray[9])
                .setCity(csvArray[10])
                .setPostalCode(csvArray[11]);

        patient.setActive(true);

        switch (csvArray[12]) {
            case "1":
                patient.setGender(AdministrativeGenderEnum.FEMALE);
                break;
            case "2":
                patient.setGender(AdministrativeGenderEnum.MALE);
                break;
            default:
                patient.setGender(AdministrativeGenderEnum.UNKNOWN);
        }

        patient.setMaritalStatus(MaritalStatusCodesEnum.S);
        // HAPI doesn't add in the display text. It is mandatory in the profile
        patient.getMaritalStatus().getCoding().get(0).setDisplay("Never Married");

        Date birth;
        try {
            birth = dateFormat.parse(csvArray[13]);
            patient.setBirthDate(new DateDt(birth));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        patient.setManagingOrganization(new ResourceReferenceDt(practice.getId().getValue()));
        patient.getManagingOrganization().setDisplay(practice.getName());

        patient.addCareProvider()
                .setDisplay(gp.getName().getNameAsSingleString())
                .setReference(gp.getId().getValue());

        return patient;
    }
}
