package uk.nhs.careconnect.unscheduled.ccriunscheduled;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@SpringBootApplication
public class CcriUnscheduledApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CcriUnscheduledApplication.class);

    private static String yasIdentifier = "https://fhir.yas.nhs.uk/Encounter/Identifier";

    private static String yasLocationIdentifier = "https://fhir.yas.nhs.uk/Location/Identifier";

    FhirContext ctxFHIR = FhirContext.forDstu3();

    public static void main(String[] args) {
        SpringApplication.run(CcriUnscheduledApplication.class, args);
    }

    IGenericClient client = null;

    public static final String SNOMEDCT = "http://snomed.info/sct";


    DateFormat df = new SimpleDateFormat("HHmm_dd_MM_yyyy");

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && args[0].equals("exitcode")) {
            throw new Exception();
        }



        client = ctxFHIR.newRestfulGenericClient("https://data.developer.nhs.uk/ccri-fhir/STU3/");

        client.setEncoding(EncodingEnum.XML);

        Integer idno = 1;
        Integer locno = 1;

        Bundle bundle = getPatientBundle("1");

        bundle.setType(Bundle.BundleType.COLLECTION);

        Organization yas = getOrganization("RX8");

        if (yas != null) {
            bundle.addEntry().setResource(yas);
        }

        Location jimmy = new Location();



        Location ambulanceVech = new Location();
        ambulanceVech.setId("#location"+idno.toString());
        ambulanceVech.setStatus(Location.LocationStatus.ACTIVE);
        ambulanceVech.setName("Danzig");
        ambulanceVech.setDescription("Box Body Ambulance");
        ambulanceVech.getType().addCoding()
                .setSystem("http://hl7.org/fhir/v3/RoleCode")
                .setCode("AMB")
                .setDisplay("Ambulance");
        ambulanceVech.addTelecom()
                .setSystem(ContactPoint.ContactPointSystem.PHONE)
                .setValue("airwave-542329")
                .setUse(ContactPoint.ContactPointUse.MOBILE);
        ambulanceVech.addIdentifier().setSystem(yasIdentifier).setValue(idno.toString());
        ambulanceVech.getPhysicalType().addCoding()
                .setSystem( "http://hl7.org/fhir/location-physical-type")
                .setCode("ve")
                .setDisplay("Vehicle");
        ambulanceVech.getPosition()
                .setAltitude(0)
                .setLatitude(53.795387709017916)
                .setLongitude(-1.5295702591538431);
        ambulanceVech.setManagingOrganization(new Reference(yas.getId()));
        locno++;
        bundle.addEntry().setResource(ambulanceVech);

        Location patientLoc = new Location();



        Encounter encounter = new Encounter();
        encounter.setId("#encounter"+idno.toString());
        encounter.setSubject(new Reference(bundle.getEntryFirstRep().getResource().getId()));
        encounter.setStatus(Encounter.EncounterStatus.INPROGRESS);
        encounter.addIdentifier().setSystem(yasIdentifier).setValue(idno.toString());
        encounter.setServiceProvider(new Reference(yas.getId()));
        encounter.getClass_().setCode("EMER").setSystem("http://hl7.org/fhir/v3/ActCode").setDisplay("emergency");
        encounter.addType().addCoding()
                .setSystem("http://snomed.info/sct")
                .setCode("409971007")
                .setDisplay("Emergency medical services");
        idno++;
        bundle.addEntry().setResource(encounter);


        Encounter triage = new Encounter();
        triage.setId("#encounter"+idno.toString());
        triage.setSubject(new Reference(bundle.getEntryFirstRep().getResource().getId()));
        triage.setStatus(Encounter.EncounterStatus.FINISHED);
        triage.addIdentifier().setSystem(yasIdentifier).setValue(idno.toString());
        triage.setServiceProvider(new Reference(yas.getId()));
        triage.getClass_().setCode("EMER").setSystem("http://hl7.org/fhir/v3/ActCode").setDisplay("emergency");
        triage.addType().addCoding()
                .setSystem("http://snomed.info/sct")
                .setCode("245581009")
                .setDisplay("Emergency examination for triage");
        triage.setPartOf(new Reference(encounter.getId()));
        idno++;
        bundle.addEntry().setResource(triage);


        Encounter ambulance = new Encounter();
        ambulance.setId("#encounter"+idno.toString());
        ambulance.setSubject(new Reference(bundle.getEntryFirstRep().getResource().getId()));
        ambulance.setStatus(Encounter.EncounterStatus.INPROGRESS);
        ambulance.addIdentifier().setSystem(yasIdentifier).setValue(idno.toString());
        ambulance.setServiceProvider(new Reference(yas.getId()));
        ambulance.addType().addCoding()
                .setSystem("http://snomed.info/sct")
                .setCode("11424001")
                .setDisplay("Ambulance-based care");
        ambulance.setPartOf(new Reference(encounter.getId()));
        ambulance.addLocation()
                .setLocation(new Reference(ambulanceVech.getId()));
        idno++;
        bundle.addEntry().setResource(ambulance);


        System.out.println(ctxFHIR.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle));

    }

    private Bundle getPatientBundle(String patientId) {
        Bundle bundle = client
                .search()
                .forResource(Patient.class)
                .where(Patient.RES_ID.exactly().code(patientId))
                .include(Patient.INCLUDE_GENERAL_PRACTITIONER)
                .include(Patient.INCLUDE_ORGANIZATION)
                .returnBundle(Bundle.class)
                .execute();
        return bundle;
    }

    private Organization getOrganization(String sdsCode) {
        Organization organization = null;
        Bundle bundle =  client
                .search()
                .forResource(Organization.class)
                .where(Organization.IDENTIFIER.exactly().code(sdsCode))

                .returnBundle(Bundle.class)
                .execute();
        if (bundle.getEntry().size()>0) {
            if (bundle.getEntry().get(0).getResource() instanceof Organization)
                organization = (Organization) bundle.getEntry().get(0).getResource();

        }
        return organization;
    }
}
