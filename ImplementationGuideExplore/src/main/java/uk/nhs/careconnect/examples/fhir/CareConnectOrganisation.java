package uk.nhs.careconnect.examples.fhir;

import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.dstu2.resource.Organization;
import ca.uhn.fhir.model.dstu2.valueset.AddressUseEnum;
import ca.uhn.fhir.model.dstu2.valueset.ContactPointSystemEnum;
import ca.uhn.fhir.model.dstu2.valueset.ContactPointUseEnum;
import ca.uhn.fhir.model.primitive.IdDt;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevinmayfield on 07/07/2017.
 */
public class CareConnectOrganisation {
    public static Organization buildCareConnectOrganisation(String ODSCode, String organisationName, String phone, String addressLine1, String addressLine2, String city, String postCode, String type)
    {
        Organization organization = new Organization();

        List<IdDt> profiles = new ArrayList<IdDt>();
        profiles.add(new IdDt(CareConnectSystem.ProfileOrganization));
        ResourceMetadataKeyEnum.PROFILES.put(organization, profiles);

        organization.addIdentifier()
                .setSystem(CareConnectSystem.SystemODSOrganisationCode)
                .setValue(ODSCode);

        switch (type) {
            case "prov":
                organization.getType().addCoding()
                    .setCode(CareConnectSystem.SystemOrganisationType)
                    .setCode("prov")
                    .setDisplay("Healthcare Provider");
                break;
        }
        organization.setName(organisationName);

        organization.addTelecom()
                .setUse(ContactPointUseEnum.WORK)
                .setValue(phone)
                .setSystem(ContactPointSystemEnum.PHONE);

        organization.addAddress()
                .setUse(AddressUseEnum.WORK)
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

