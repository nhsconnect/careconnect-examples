package uk.nhs.careconnect.hapiclient.fhir;


import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.ContactPoint;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Organization;
import uk.nhs.careconnect.core.dstu2.CareConnectProfile;
import uk.nhs.careconnect.core.dstu2.CareConnectSystem;

/**
 * Created by kevinmayfield on 07/07/2017.
 */
public class CareConnectOrganisation {
    public static Organization buildCareConnectOrganisation(String ODSCode, String organisationName, String phone, String addressLine1, String addressLine2, String city, String postCode, String type)
    {
        Organization organization = new Organization();

       // List<IdDt> profiles = new ArrayList<IdDt>();
       // profiles.add(new IdDt(CareConnectSystem.ProfileOrganization));
       /// ResourceMetadataKeyEnum.PROFILES.put(organization, profiles);

        organization.setMeta(new Meta().addProfile(CareConnectProfile.Organization_1));


        organization.addIdentifier()
                .setSystem(CareConnectSystem.ODSOrganisationCode)
                .setValue(ODSCode);

        switch (type) {
            case "prov":
                organization.getType().addCoding()
                    .setSystem(CareConnectSystem.OrganisationType)
                    .setCode("prov")
                    .setDisplay("Healthcare Provider");
                break;
        }
        organization.setName(organisationName);

        organization.addTelecom()
                .setUse(ContactPoint.ContactPointUse.WORK)
                .setValue(phone)
                .setSystem(ContactPoint.ContactPointSystem.PHONE);

        organization.addAddress()
                .setUse(Address.AddressUse.WORK)
                .addLine(addressLine1)
                .addLine(addressLine2)
                .setCity(city)
                .setPostalCode(postCode);
        /*
        switch (type) {
            case "CSC":
                organization.getType().addCoding()
                        .setSystem("http://hl7.org/fhir/ValueSet/v3-ServiceDeliveryLocationRoleType")
                        .setCode(type)
                        .setDisplay("Community Service Centre");
                break;
        }
        */
        return organization;
    }
}

