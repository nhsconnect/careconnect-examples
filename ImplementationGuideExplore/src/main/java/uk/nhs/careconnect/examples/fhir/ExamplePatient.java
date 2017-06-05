package uk.nhs.careconnect.examples.fhir;

import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Patient;
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
public class ExamplePatient {

    public static Patient buildCareConnectFHIRPatient()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Patient patient = new Patient();

        List<IdDt> profiles = new ArrayList<IdDt>();
        profiles.add(new IdDt(CareConnectSystem.ProfilePatient));
        ResourceMetadataKeyEnum.PROFILES.put(patient, profiles);

        CodeableConceptDt ethnicCode = new CodeableConceptDt();
        ethnicCode
                .addCoding()
                .setSystem(CareConnectSystem.SystemEthnicCategory)
                .setDisplay("British, Mixed British")
                .setCode("01");
        ExtensionDt ethnicExtension = new ExtensionDt()
                .setUrl(CareConnectSystem.ExtUrlEthnicCategory)
                .setValue(ethnicCode);
        patient.addUndeclaredExtension(ethnicExtension);

        IdentifierDt nhsNumber = patient.addIdentifier()
                .setSystem(CareConnectSystem.SystemNHSNumber)
                .setValue("9876543210");

        CodeableConceptDt verificationStatusCode = new CodeableConceptDt();
        verificationStatusCode
                .addCoding()
                .setSystem(CareConnectSystem.SystemNHSNumberVerificationStatus)
                .setDisplay("Number present and verified")
                .setCode("01");
        ExtensionDt verificationStatus = new ExtensionDt()
                .setUrl(CareConnectSystem.ExtUrlNHSNumberVerificationStatus)
                .setValue(verificationStatusCode);
        nhsNumber.addUndeclaredExtension(verificationStatus);

        patient.addIdentifier()
                .setSystem("https://fhir.jorvik.nhs.uk/PAS/Patient")
                .setValue("123345");

        patient.addName()
                .setUse(NameUseEnum.USUAL)
                .addFamily("Kanfeld")
                .addGiven("Bernie")
                .addPrefix("Miss");

        patient.addAddress()
                .setUse(AddressUseEnum.HOME)
                .addLine("10, Field Jardin")
                .addLine("Long Eaton")
                .setCity("Nottingham")
                .setPostalCode("NG10 1ZZ");

        patient.setActive(true);

        patient.setGender(AdministrativeGenderEnum.FEMALE);

        patient.setMaritalStatus(MaritalStatusCodesEnum.S);
        // HAPI doesn't add in the display text. It is mandatory in the profile
        patient.getMaritalStatus().getCoding().get(0).setDisplay("Never Married");

        Date birth;
        try {
            birth = dateFormat.parse("1998-03-19");
            patient.setBirthDate(new DateDt(birth));
        } catch (ParseException e) {
            e.printStackTrace();
        }


        patient.setManagingOrganization(new ResourceReferenceDt("https://sds.proxy.nhs.uk/Organization/C81010"));
        patient.getManagingOrganization().setDisplay("Moir Medical Centre");

        patient.addCareProvider()
                .setDisplay("Dr AA Bhatia")
                .setReference("https://sds.proxy.nhs.uk/Practitioner/G8133438");

        return patient;
    }
}
