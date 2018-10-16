package uk.nhs.careconnect.unscheduled.ccriunscheduled;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SpringBootApplication
public class CcriUnscheduledApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CcriUnscheduledApplication.class);

    private static String yasEncounterIdentifier = "https://fhir.yas.nhs.uk/Encounter/Identifier";

    private static String yasDocumentIdentifier = "https://fhir.yas.nhs.uk/DocumentReference/Identifier";

    private static String yasEpisodeIdentifier = "https://fhir.yas.nhs.uk/EpisodeOfCare/Identifier";

    private static String yasLocationIdentifier = "https://fhir.yas.nhs.uk/Location/Identifier";

    private static String yasConditionIdentifier = "https://fhir.yas.nhs.uk/Location/Identifier";

    final String uuidtag = "urn:uuid:";

    Organization yas;

    Organization lth;

    Location jimmy;

    FhirContext ctxFHIR = FhirContext.forDstu3();

    Integer idno = 650;
    Integer locno = 730;
    Integer conno = 12730;


    public static void main(String[] args) {
        SpringApplication.run(CcriUnscheduledApplication.class, args);
    }

    IGenericClient client = null;
    IGenericClient clientGPC = null;

    FhirBundleUtil fhirBundle;

    public static final String SNOMEDCT = "http://snomed.info/sct";


    DateFormat df = new SimpleDateFormat("HHmm_dd_MM_yyyy");

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && args[0].equals("exitcode")) {
            throw new Exception();
        }

        client = ctxFHIR.newRestfulGenericClient("https://data.developer-test.nhs.uk/ccri-fhir/STU3/");
        //client = ctxFHIR.newRestfulGenericClient("http://127.0.0.1:8183/ccri-fhir/STU3/");
        client.setEncoding(EncodingEnum.XML);

        clientGPC = ctxFHIR.newRestfulGenericClient("https://data.developer-test.nhs.uk/ccri-fhir/STU3/");
        //clientGPC = ctxFHIR.newRestfulGenericClient("http://127.0.0.1:8187/ccri/camel/fhir/gpc/");
        clientGPC.setEncoding(EncodingEnum.XML);



        postPatient("9476719931", "LS15 8FS",true, "Danzig", "LS14 6UH",-1,0,"217082002","Accidental fall");

        postPatient("9476719974", "LS25 1NT",false, null, null,-1,-15, "217133004","Fall into quarry");

        postPatient("9476719966", "LS25 2HF",false, "Elbe", "LS26 8PU" ,0,-15, "410429000","Cardiac arrest");

        postPatient("9476719958", "LS15 8ZB", false, null, null,0,-5,"418399005","Motor vehicle accident");

       // postPatient("9000000068");


    }

    public Location getCoords(Location location, String postCode) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.postcodes.io/postcodes/"+postCode;
        //System.out.println(url);
        PostCode postCodeLongLat = restTemplate.getForObject(url,PostCode.class);
        //System.out.println(postCodeLongLat.getResult().getLongitude().toString());
        location.getPosition().setLatitude(postCodeLongLat.getResult().getLatitude());
        location.getPosition().setLongitude(postCodeLongLat.getResult().getLongitude());
        return location;
    }


    public void doSetUp() {

        yas = getOrganization("RX8");
        yas.setId(fhirBundle.getNewId(yas));

        lth = getOrganization("RR8");
        lth.setId(fhirBundle.getNewId(lth));

        jimmy = new Location();
        jimmy.setId(fhirBundle.getNewId(jimmy));
        jimmy.setStatus(Location.LocationStatus.ACTIVE);
        jimmy.setName("St James's University Hospital: Emergency Department");
        jimmy.setDescription("St James's University Hospital: Emergency Department");
        jimmy.getType().addCoding()
                .setSystem("http://hl7.org/fhir/v3/RoleCode")
                .setCode("ETU")
                .setDisplay("Emergency Trauma Unit");
        jimmy.addTelecom()
                .setSystem(ContactPoint.ContactPointSystem.PHONE)
                .setValue("airwave-27051940")
                .setUse(ContactPoint.ContactPointUse.MOBILE);
        jimmy.addIdentifier().setSystem(yasLocationIdentifier).setValue("RR8-ED");
        jimmy.getPhysicalType().addCoding()
                .setSystem("http://hl7.org/fhir/location-physical-type")
                .setCode("bu")
                .setDisplay("Building");
        jimmy.getPosition()
                .setAltitude(0)
                .setLatitude(53.80634615690993)
                .setLongitude(-1.5230420347013478);
        jimmy.setManagingOrganization(new Reference(uuidtag + lth.getIdElement().getIdPart()));


    }

    public void postPatient(String nhsNumber, String encounterPostcode, Boolean ambulanceReq, String ambulanceName, String ambulancePostcode,
                            Integer hoursDiff, Integer minsDiff ,String code, String display ) {

        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.HOUR, hoursDiff);
        cal.add(Calendar.MINUTE,minsDiff);

        Date oneHourBack = cal.getTime();
        fhirBundle = new FhirBundleUtil(Bundle.BundleType.COLLECTION);

        doSetUp();

        Bundle patientBundle = getPatientBundle(nhsNumber);


        fhirBundle.processBundleResources(patientBundle);


        Bundle bundle = new Bundle();


            bundle.addEntry().setResource(yas);
            bundle.addEntry().setResource(lth);


            Location patientLoc = new Location();
            patientLoc.setId(fhirBundle.getNewId(patientLoc));
            patientLoc.setStatus(Location.LocationStatus.ACTIVE);
            patientLoc.setName("Casuaulty Location");

            patientLoc.getType().addCoding()
                    .setSystem("http://hl7.org/fhir/v3/RoleCode")
                    .setCode("ACC")
                    .setDisplay("Accident Site");
            patientLoc.addTelecom()
                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setValue("0113 12341234")
                    .setUse(ContactPoint.ContactPointUse.MOBILE);
            patientLoc.addIdentifier().setSystem(yasLocationIdentifier).setValue(locno.toString());
            patientLoc.getPhysicalType().addCoding()
                    .setSystem("http://hl7.org/fhir/location-physical-type")
                    .setCode("bu")
                    .setDisplay("Building");
            getCoords(patientLoc,encounterPostcode);

            locno++;
            bundle.addEntry().setResource(patientLoc);



            bundle.addEntry().setResource(this.jimmy);

            Condition condition = new Condition();
            condition.setId(fhirBundle.getNewId(condition));
            condition.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
            condition.setClinicalStatus(Condition.ConditionClinicalStatus.ACTIVE);
            condition.setAsserter(new Reference(uuidtag + fhirBundle.getPatient().getId()));
            condition.addIdentifier().setSystem(yasConditionIdentifier).setValue(conno.toString());
            condition.setAssertedDate(cal.getTime());
            condition.getCode().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setDisplay(display)
                    .setCode(code);
            conno++;
            bundle.addEntry().setResource(condition);

            Encounter encounter = new Encounter();
            encounter.setId(fhirBundle.getNewId(encounter));
            encounter.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
            encounter.setStatus(Encounter.EncounterStatus.INPROGRESS);
            encounter.addIdentifier().setSystem(yasEpisodeIdentifier).setValue(idno.toString());
            encounter.setServiceProvider(new Reference(uuidtag + yas.getIdElement().getIdPart()));
            encounter.getClass_().setCode("EMER").setSystem("http://hl7.org/fhir/v3/ActCode").setDisplay("emergency");
            encounter.addType().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("409971007")
                    .setDisplay("Emergency medical services");
            encounter.getPeriod().setStart(cal.getTime());
            encounter.addDiagnosis().setCondition(new Reference(uuidtag + condition.getIdElement().getIdPart()));
            idno++;
            bundle.addEntry().setResource(encounter);


            Encounter triage = new Encounter();
            triage.setId(fhirBundle.getNewId(triage));
            triage.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
            if (ambulanceReq ) {
                triage.setStatus(Encounter.EncounterStatus.FINISHED);
            } else {
                triage.setStatus(Encounter.EncounterStatus.INPROGRESS);
            }
            triage.addIdentifier().setSystem(yasEncounterIdentifier).setValue(idno.toString());
            triage.setServiceProvider(new Reference(uuidtag + yas.getIdElement().getIdPart()));
            triage.getClass_().setCode("EMER").setSystem("http://hl7.org/fhir/v3/ActCode").setDisplay("emergency");
            triage.addType().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("245581009")
                    .setDisplay("Emergency examination for triage");
            triage.addLocation().setLocation(new Reference(uuidtag + patientLoc.getId()));
            triage.setPartOf(new Reference(uuidtag + encounter.getId()));
            triage.getPeriod().setStart(cal.getTime());
            if (ambulanceReq) {
                cal.add(Calendar.MINUTE,5);
                triage.getPeriod().setEnd(cal.getTime());
            }
            idno++;
            bundle.addEntry().setResource(triage);





            if (ambulanceReq) {

                Location ambulanceVech = new Location();
                ambulanceVech.setId(fhirBundle.getNewId(ambulanceVech));
                ambulanceVech.setStatus(Location.LocationStatus.ACTIVE);
                ambulanceVech.setName(ambulanceName);
                ambulanceVech.setDescription("Box Body Ambulance");
                ambulanceVech.getType().addCoding()
                        .setSystem("http://hl7.org/fhir/v3/RoleCode")
                        .setCode("AMB")
                        .setDisplay("Ambulance");
                ambulanceVech.addTelecom()
                        .setSystem(ContactPoint.ContactPointSystem.PHONE)
                        .setValue("airwave-542329")
                        .setUse(ContactPoint.ContactPointUse.MOBILE);
                ambulanceVech.addIdentifier().setSystem(yasLocationIdentifier).setValue(ambulanceName.toUpperCase());
                ambulanceVech.getPhysicalType().addCoding()
                        .setSystem("http://hl7.org/fhir/location-physical-type")
                        .setCode("ve")
                        .setDisplay("Vehicle");
                getCoords(ambulanceVech,ambulancePostcode);

                ambulanceVech.setManagingOrganization(new Reference(uuidtag + yas.getIdElement().getIdPart()));
                bundle.addEntry().setResource(ambulanceVech);


                Encounter ambulance = new Encounter();
                ambulance.setId(fhirBundle.getNewId(ambulance));
                ambulance.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
                ambulance.setStatus(Encounter.EncounterStatus.INPROGRESS);
                ambulance.addIdentifier().setSystem(yasEncounterIdentifier).setValue(idno.toString());
                ambulance.setServiceProvider(new Reference(uuidtag + yas.getIdElement().getIdPart()));
                ambulance.addType().addCoding()
                        .setSystem("http://snomed.info/sct")
                        .setCode("11424001")
                        .setDisplay("Ambulance-based care");
                ambulance.setPartOf(new Reference(uuidtag + encounter.getId()));
                ambulance.addLocation()
                        .setLocation(new Reference(uuidtag + ambulanceVech.getId()))
                        .setStatus(Encounter.EncounterLocationStatus.ACTIVE);

                ambulance.addLocation()
                        .setLocation(new Reference(uuidtag + patientLoc.getId()))
                        .setStatus(Encounter.EncounterLocationStatus.COMPLETED);

                ambulance.addLocation()
                        .setLocation(new Reference(uuidtag + jimmy.getId()))
                        .setStatus(Encounter.EncounterLocationStatus.PLANNED);
                cal.add(Calendar.MINUTE,5);
                ambulance.getPeriod().setStart(cal.getTime());

            idno++;
                bundle.addEntry().setResource(ambulance);
            }

            getUnstructuredBundle(nhsNumber);


            // System.out.println(ctxFHIR.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle));

            fhirBundle.processBundleResources(bundle);


        System.out.println(ctxFHIR.newJsonParser().setPrettyPrint(true).encodeResourceToString(fhirBundle.getFhirDocument()));

        MethodOutcome outcome = client.create().resource(fhirBundle.getFhirDocument()).execute();

       // System.out.println(outcome.getId().toString());
       // System.out.println(ctxFHIR.newJsonParser().setPrettyPrint(true).encodeResourceToString(outcome.getOperationOutcome()));

    }

    private Bundle getPatientBundle(String NHSNumber) {

        IGenericClient callclient = clientGPC;
        Bundle bundle = callclient
                .search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().systemAndCode("https://fhir.nhs.uk/Id/nhs-number",NHSNumber))
        //
                .returnBundle(Bundle.class)
                .execute();

        if (bundle.getEntry().size()>0) {
            Patient patient = (Patient) bundle.getEntry().get(0).getResource();
            if (patient.hasManagingOrganization()) {

                Organization organization = callclient.read().resource(Organization.class).withId(patient.getManagingOrganization().getReference()).execute();
                organization.setId(fhirBundle.getNewId(organization));
                patient.setManagingOrganization(new Reference(uuidtag+organization.getId()));
                bundle.addEntry().setResource(organization);
            }
            if (patient.hasGeneralPractitioner()) {

                Practitioner practitioner = callclient.read().resource(Practitioner.class).withId(patient.getGeneralPractitioner().get(0).getReference()).execute();
                practitioner.setId(fhirBundle.getNewId(practitioner));
                patient.getGeneralPractitioner().get(0).setReference(uuidtag+practitioner.getId());
                bundle.addEntry().setResource(practitioner);
            }
        }
        return bundle;
    }

    private Bundle getUnstructuredBundle(String nhsNumber) {
        Bundle bundle = null;
        try {
            switch (nhsNumber) {
                case "9476719931":
                    getUnstructuredBundle(nhsNumber, 5);

                    break;
                case "9476719974":
                    getUnstructuredBundle(nhsNumber, 2);

                    break;
                case "9476719966":
                    getUnstructuredBundle(nhsNumber, 3);
                    getUnstructuredBundle(nhsNumber, 1);
                    break;
                case "9476719958":
                    getUnstructuredBundle(nhsNumber, 4);

                    break;

            }
        } catch (Exception ex) {

        }
        return bundle;
    }

    private Bundle getUnstructuredBundle(String patientId, Integer docExample) throws Exception {
        // Create Bundle of type Document



        Bundle bundle = new Bundle();
        // Main resource of a FHIR Bundle is a DocumentReference
        DocumentReference documentReference = new DocumentReference();
        documentReference.setId(fhirBundle.getNewId(documentReference));
        bundle.addEntry().setResource(documentReference);

        documentReference.addIdentifier()
                .setSystem(yasDocumentIdentifier)
                .setValue(docExample.toString());

        documentReference.setCreated(new Date());
        documentReference.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);


        bundle.addEntry().setResource(lth);

        documentReference.setCustodian(new Reference("Organization/"+lth.getIdElement().getIdPart()));
        // log.info("Custodian docRef"+documentReference.getCustodian().getReference());

        Practitioner consultant = getPractitioner("C2381390");

        bundle.addEntry().setResource(consultant);
        documentReference.addAuthor(new Reference("Practitioner/"+consultant.getIdElement().getIdPart()));




        Binary binary = new Binary();
        binary.setId(fhirBundle.getNewId(binary));

        if (docExample == 1) {

            documentReference.getType().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("736373009")
                    .setDisplay("End of life care plan");

            documentReference.getContext().getPracticeSetting().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("103735009")
                    .setDisplay("Palliative care");

            documentReference.getContext().getFacilityType().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("3531000175102")
                    .setDisplay("Geriatric service");

            InputStream inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("image/EOLCCheshire.jpg");
            binary.setContent(IOUtils.toByteArray(inputStream));
            binary.setContentType("image/jpeg");

        } else if (docExample == 2) {
            documentReference.getType().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("820291000000107")
                    .setDisplay("Infectious disease notification");

            documentReference.getContext().getPracticeSetting().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("394582007")
                    .setDisplay("Dermatology");

            documentReference.getContext().getFacilityType().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("700241009")
                    .setDisplay("Dermatology service");

            InputStream inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("image/DischargeSummary.pdf");
            binary.setContent(IOUtils.toByteArray(inputStream));
            binary.setContentType("application/pdf");
        }
        else if (docExample == 3) {
            documentReference.getType().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("422735006")
                    .setDisplay("Summary clinical document");

            documentReference.getContext().getPracticeSetting().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("394802001")
                    .setDisplay("General medicine");

            documentReference.getContext().getFacilityType().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("409971007")
                    .setDisplay("Emergency medical services");



            InputStream inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("image/hospital-scanned.jpg");
            binary.setContent(IOUtils.toByteArray(inputStream));
            binary.setContentType("image/jpeg");
        } else if (docExample == 4) {
            documentReference.getType().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("823571000000103")
                    .setDisplay("Scored assessment record (record artifact)");

            documentReference.getContext().getPracticeSetting().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("394802001")
                    .setDisplay("General medicine");

            documentReference.getContext().getFacilityType().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("700232004")
                    .setDisplay("General medical service");



            InputStream inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("image/VitalSigns.pdf");
            binary.setContent(IOUtils.toByteArray(inputStream));
            binary.setContentType("application/pdf");
        }
        else if (docExample == 5) {
            documentReference.getType().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("718347000")
                    .setDisplay("Mental health care plan ");

            documentReference.getContext().getPracticeSetting().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("722162001")
                    .setDisplay("Psychology");

            documentReference.getContext().getFacilityType().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("708168004")
                    .setDisplay("Mental health service");



            InputStream inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("pdf/1_STAYING WELL PLAN CMHT.pdf");
            binary.setContent(IOUtils.toByteArray(inputStream));
            binary.setContentType("application/pdf");
        } else if (docExample == 6) {
            documentReference.getType().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("718347000")
                    .setDisplay("Mental health care plan ");

            documentReference.getContext().getPracticeSetting().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("722162001")
                    .setDisplay("Psychology");

            documentReference.getContext().getFacilityType().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("708168004")
                    .setDisplay("Mental health service");



            InputStream inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("pdf/crisis and contingency questionnaire screen shot.pdf");
            binary.setContent(IOUtils.toByteArray(inputStream));
            binary.setContentType("application/pdf");
        }


        bundle.addEntry().setResource(binary).setFullUrl(binary.getId());
        documentReference.addContent()
                .getAttachment()
                .setUrl("Binary/"+binary.getId())
                .setContentType(binary.getContentType());


        // This is a synthea patient

        fhirBundle.processBundleResources(bundle);

        if (fhirBundle.getPatient() == null) throw new Exception("404 Patient not found");
        documentReference.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        fhirBundle.processReferences();

        return fhirBundle.getFhirDocument();
    }

    private Practitioner getPractitioner(String sdsCode) {
        Practitioner practitioner = null;
        Bundle bundle =  client
                .search()
                .forResource(Practitioner.class)
                .where(Practitioner.IDENTIFIER.exactly().code(sdsCode))
                .returnBundle(Bundle.class)
                .execute();
        if (bundle.getEntry().size()>0) {
            if (bundle.getEntry().get(0).getResource() instanceof Practitioner)
                practitioner = (Practitioner) bundle.getEntry().get(0).getResource();

        }
        return practitioner;
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
