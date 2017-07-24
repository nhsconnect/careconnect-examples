package uk.nhs.careconnect.examples.fhir;


import org.hl7.fhir.instance.model.*;

/**
 * Created by kevinmayfield on 07/07/2017.
 */
public class CareConnectPractitioner {
    public static Practitioner buildCareConnectPractitioner(String SDSUserId, String familyName, String givenName, String prefix, Enumerations.AdministrativeGender gender
            , String phone, String addressLine1, String addressLine2, String city, String postCode
            , Organization organistion, String SDSRole, String SDSRoleName)
    {
        Practitioner practitioner = new Practitioner();

       // List<IdDt> profiles = new ArrayList<IdDt>();
       // profiles.add(new IdDt(CareConnectSystem.ProfilePractitioner));
       // ResourceMetadataKeyEnum.PROFILES.put(practitioner, profiles);

        practitioner.setMeta(new Meta().addProfile(CareConnectSystem.ProfilePractitioner));
        practitioner.addIdentifier()
                .setSystem(CareConnectSystem.SystemSDSUserId)
                .setValue(SDSUserId);

        practitioner.getName()
                .addFamily(familyName)
                .addGiven(givenName)
                .addPrefix(prefix);

        practitioner.addTelecom()
                .setUse(ContactPoint.ContactPointUse.WORK)
                .setValue(phone)
                .setSystem(ContactPoint.ContactPointSystem.PHONE);

        practitioner.addAddress()
                .setUse(Address.AddressUse.WORK)
                .addLine(addressLine1)
                .addLine(addressLine2)
                .setCity(city)
                .setPostalCode(postCode);
        practitioner.setGender(gender);

        Practitioner.PractitionerPractitionerRoleComponent role = practitioner.addPractitionerRole();
        role.setManagingOrganization(new Reference(organistion.getId()))
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

