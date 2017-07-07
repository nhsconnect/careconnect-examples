package uk.nhs.careconnect.examples.fhir;

import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Organization;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.model.dstu2.valueset.AddressUseEnum;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.ContactPointSystemEnum;
import ca.uhn.fhir.model.dstu2.valueset.ContactPointUseEnum;
import ca.uhn.fhir.model.primitive.IdDt;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevinmayfield on 07/07/2017.
 */
public class CareConnectPractitioner {
    public static Practitioner buildCareConnectPractitioner(String SDSUserId, String familyName, String givenName, String prefix, AdministrativeGenderEnum gender
            , String phone, String addressLine1, String addressLine2, String city, String postCode
            , Organization organistion, String SDSRole, String SDSRoleName)
    {
        Practitioner practitioner = new Practitioner();

        List<IdDt> profiles = new ArrayList<IdDt>();
        profiles.add(new IdDt(CareConnectSystem.ProfilePractitioner));
        ResourceMetadataKeyEnum.PROFILES.put(practitioner, profiles);

        practitioner.addIdentifier()
                .setSystem(CareConnectSystem.SystemSDSUserId)
                .setValue(SDSUserId);

        practitioner.getName()
                .addFamily(familyName)
                .addGiven(givenName)
                .addPrefix(prefix);

        practitioner.addTelecom()
                .setUse(ContactPointUseEnum.WORK)
                .setValue(phone)
                .setSystem(ContactPointSystemEnum.PHONE);

        practitioner.addAddress()
                .setUse(AddressUseEnum.WORK)
                .addLine(addressLine1)
                .addLine(addressLine2)
                .setCity(city)
                .setPostalCode(postCode);
        practitioner.setGender(gender);

        Practitioner.PractitionerRole role = practitioner.addPractitionerRole();
        role.setManagingOrganization(new ResourceReferenceDt(organistion.getId().getValue()))
                //.getManagingOrganization().setDisplay(organistion.getName()
            .getRole()
                .addCoding()
                    .setDisplay(SDSRoleName)
                    .setCode(SDSRole)
                    .setSystem(CareConnectSystem.SystemSDSJobRoleName);
        role.getManagingOrganization().setDisplay(organistion.getName());
        return practitioner;
    }
}

