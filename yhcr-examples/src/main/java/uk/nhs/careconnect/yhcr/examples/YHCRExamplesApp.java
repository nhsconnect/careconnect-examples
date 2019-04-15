package uk.nhs.careconnect.yhcr.examples;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootApplication
public class YHCRExamplesApp implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(YHCRExamplesApp.class);



    final String uuidtag = "urn:uuid:";


    FhirContext ctxFHIR = FhirContext.forDstu3();


    public static void main(String[] args) {
        SpringApplication.run(YHCRExamplesApp.class, args);
    }

    IGenericClient client = null;

    IGenericClient clientCCRI = null;



    public static final String SNOMEDCT = "http://snomed.info/sct";


    DateFormat df = new SimpleDateFormat("HHmm_dd_MM_yyyy");

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && args[0].equals("exitcode")) {
            throw new Exception();
        }

        //client = ctxFHIR.newRestfulGenericClient("https://data.developer.nhs.uk/ccri-fhir/STU3/");
        client = ctxFHIR.newRestfulGenericClient("http://127.0.0.1:8080/fhircdr/STU3/");
        //client = ctxFHIR.newRestfulGenericClient("http://163.160.64.135:8186/ccri-fhir/STU3/");
        client.setEncoding(EncodingEnum.XML);

        loadFolder("namingSystems");

        Patient patient = buildPatient("9657702070",
                "Mrs",
                "Muzna",
                "Zara",
                 "Alizadeh",
                "",
                "12 CHURCH STREET",
                "",
                "LITTLEBOROUGH",
                "LANCS",
                "OL15 9AA",
                "P86624",
                "X",
                "1983-07-18",
                "TEST1PPM",
                null);
        addPatient(patient);

        patient = buildPatient("9657702100",
                "Mrs",
                "MATTHIA",
                "",
                "AZZOPARDI",
                "",
                "55 EXETER STREET",
                "",
                "ROCHDALE",
                "LANCS",
                "OL11 1JY",
                "P86624",
                "2",
                "1986-02-27",
                "TEST2PPM",
                null);
        addPatient(patient);

        patient = buildPatient("9657702151",
                "MS",
                "Gladys",
                "Lori",
                "CARTY",
                "1A",
                "TURF HILL ROAD",
                "",
                "ROCHDALE",
                "LANCS",
                "OL16 4XH",
                "P86624",
                "2",
                "1983-05-01",
                "TEST3PPM",
                null);
        addPatient(patient);

        patient = buildPatient("9657702437",
                "Miss",
                "DEBRA",
                "Mara",
                "Hislop",
                "",
                "1 Clara Street",
                "",
                "ROCHDALE",
                "LANCS",
                "OL16 1PG",
                "P86609",
                "2",
                "1992-04-02",
                "TEST4PPM",
                null);
        addPatient(patient);

        patient = buildPatient("9657702402",
                "Ms",
                "ALISON",
                "Jean",
                "Huxley",
                "",
                "1 STRATFORD AVENUE",
                "",
                "ROCHDALE",
                "LANCS",
                "OL16 3RA",
                "P86609",
                "9",
                "1986-07-23",
                "TEST5PPM",
                null);
        addPatient(patient);

        patient = buildPatient("9657702240",
                "Mrs",
                "Lisa",
                "Linda",
                "Lopez",
                "",
                "1 SEVERN DRIVE",
                "MILNROW",
                "ROCHDALE",
                "LANCS",
                "OL16 3ES",
                "P86609",
                "9",
                "1990-11-29",
                "TEST6PPM",
                null);
        addPatient(patient);

        patient = buildPatient("9657702291",
                "Ms",
                "Heidi",
                "Anne",
                "Manton",
                "",
                "11 CROSS STREET",
                "",
                "ROCHDALE",
                "LANCS",
                "OL16 2PJ",
                "P86609",
                "2",
                "1995-04-29",
                "TEST7PPM",
                null);
        addPatient(patient);

        // 8
        patient = buildPatient("9657702496",
                "Mr",
                "Jim",
                "",
                "Moxey",
                "",
                "11 JUDITH STREET",
                "",
                "ROCHDALE",
                "LANCS",
                "OL12 7HS",
                "P86624",
                "1",
                "1985-08-25",
                "TEST8PPM",
                null);
        addPatient(patient);

        // 9
        patient = buildPatient("9657702127",
                "Mr",
                "SARWAR",
                "",
                "TUTEJA",
                "",
                "23 CHANNING STREET",
                "",
                "ROCHDALE",
                "LANCS",
                "OL16 5PY",
                "P86624",
                "1",
                "1983-10-19",
                "TEST8PPM",
                null);
        addPatient(patient);

        // 10
        patient = buildPatient("9657702143",
                "Mrs",
                "Monica",
                "Fiona",
                "WYNNE",
                "",
                "8 ALBERT STREET",
                "",
                "LITTLEBOROUGH",
                "LANCS",
                "OL15 8BS",
                "P86624",
                "2",
                "1999-09-16",
                "TEST10PPM",
                null
                );
        addPatient(patient);

        // LTH Patient TestTwo

        patient = buildPatient("9990000913",
                "Mr",
                "Two",
                "",
                "Adnan Testtwo",
                "",
                "112 test Lane",
                "",
                "Leeds",
                "",
                "LS1 1EE",
                "B8606",
                "1",
                "1985-05-22",
                "0026640",
                "G8805083");
        patient.addIdentifier()
                .setSystem("https://fhir.leedsth.nhs.uk/Id/internal-patient-number")
                .setValue("000026821");

        patient.addTelecom()
                .setValue("01245521121")
                .setUse(ContactPoint.ContactPointUse.HOME)
                .setSystem(ContactPoint.ContactPointSystem.PHONE);
        patient.addTelecom()
                .setValue("0754121511412")
                .setUse(ContactPoint.ContactPointUse.WORK)
                .setSystem(ContactPoint.ContactPointSystem.PHONE);
        addPatient(patient);

        patient = buildPatient(null,
                "Mrs",
                "Hilary",
                "",
                "Oak",
                "",
                "11 Woodhall Drive",
                "",
                "Leeds",
                "",
                "LS5 3LQ",
                "B8606",
                "2",
                "1964-11-06",
                "0001212",
                "G8805083");
        patient.addIdentifier()
                .setSystem("https://fhir.leedsth.nhs.uk/Id/internal-patient-number")
                .setValue("000001241");
        patient.addIdentifier()
                .setSystem("https://fhir.leedsth.nhs.uk/Id/casenote-number")
                .setValue("N646448");
        patient.addTelecom()
                .setValue("2266512")
                .setUse(ContactPoint.ContactPointUse.HOME)
                .setSystem(ContactPoint.ContactPointSystem.PHONE);
        patient.addTelecom()
                .setValue("0113212121")
                .setUse(ContactPoint.ContactPointUse.WORK)
                .setSystem(ContactPoint.ContactPointSystem.PHONE);
        addPatient(patient);


        // lth test patient

        loadFolder("examples");
    }


    public void loadFolder(String folder) throws Exception {
        List<String> filenames = new ArrayList<>();

        try (
                InputStream in = getResourceAsStream(folder);
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;

            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
                System.out.println(folder + "/"+ resource);
                loadFile(folder,resource);
            }
        }

        //return filenames;
    }

    private InputStream getResourceAsStream(String resource) {
        final InputStream in
                = getContextClassLoader().getResourceAsStream(resource);

        return in == null ? getClass().getResourceAsStream(resource) : in;
    }

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public void loadFile(String folder, String filename) {
        InputStream inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream(folder + "/" +filename);
        Reader reader = new InputStreamReader(inputStream);
        IBaseResource resource = null;
        if (FilenameUtils.getExtension(filename).equals("json")) {
            resource =  ctxFHIR.newJsonParser().parseResource(reader);
        } else {
            resource =  ctxFHIR.newXmlParser().parseResource(reader);
        }

        if (resource instanceof Encounter) {
            Encounter encounter = (Encounter) resource;

            Patient patient = getPatient(encounter.getSubject().getIdentifier().getValue());

            encounter.setSubject(new Reference("Patient/"+patient.getIdElement().getIdPart()));

            for (Encounter.EncounterParticipantComponent component : encounter.getParticipant()) {
                if (component.hasIndividual() && component.getIndividual().hasIdentifier()) {
                    Practitioner practitioner = getPractitioner(component.getIndividual().getIdentifier().getValue());
                    component.setIndividual(new Reference("Practitioner/"+practitioner.getIdElement().getIdPart()));
                }
            }

            //System.out.println(ctxFHIR.newJsonParser().setPrettyPrint(true).encodeResourceToString(encounter));

            postEncounter(encounter);
        }

        if (resource instanceof EpisodeOfCare) {
            EpisodeOfCare episode = (EpisodeOfCare) resource;

            Patient patient = getPatient(episode.getPatient().getIdentifier().getValue());

            episode.setPatient(new Reference("Patient/"+patient.getIdElement().getIdPart()));

            Organization organization = getOrganization(episode.getManagingOrganization().getIdentifier().getValue());
            episode.setManagingOrganization(new Reference("Organization/"+organization.getIdElement().getIdPart()));

            Practitioner practitioner = getPractitioner(episode.getCareManager().getIdentifier().getValue());
            episode.setCareManager(new Reference("Practitioner/"+practitioner.getIdElement().getIdPart()));

            System.out.println(ctxFHIR.newJsonParser().setPrettyPrint(true).encodeResourceToString(episode));

            postEpisodeOfCare(episode);
        }

        if (resource instanceof Observation) {
            Observation observation = (Observation) resource;

            Patient patient = getPatient(observation.getSubject().getIdentifier().getValue());

            observation.setSubject(new Reference("Patient/"+patient.getIdElement().getIdPart()));


            postObservation(observation);
        }
        if (resource instanceof NamingSystem) {
            NamingSystem namingSystem = (NamingSystem) resource;



            postNamingSystem(namingSystem);
        }


    }



    private Patient buildPatient(
            String nhsNumber,
            String prefix,
            String firstName,
            String middleName,
            String lastName,
            String adr1,
            String adr2,
            String adr3,
            String adr4,
            String adr5,
            String postCode,
            String sdsPractice,
            String gender,
            String dob,
            String ppmno,
            String gp
    ) {


        Patient patient = new Patient();

        patient.getMeta().addProfile("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Patient-1");

        if (nhsNumber != null && !nhsNumber.isEmpty()) {
            Identifier nhs = patient.addIdentifier()
                    .setSystem("https://fhir.nhs.uk/Id/nhs-number")
                    .setValue(nhsNumber);
            CodeableConcept code = new CodeableConcept();
            code.addCoding()
                    .setCode("01")
                    .setSystem("https://fhir.hl7.org.uk/STU3/CodeSystem/CareConnect-NHSNumberVerificationStatus-1");

            nhs.addExtension()
                    .setUrl("https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-NHSNumberVerificationStatus-1")
                    .setValue(code);
        }

        Identifier ppm = patient.addIdentifier()
                .setSystem("https://fhir.leedsth.nhs.uk/Id/pas-number")
                .setValue(ppmno);

        patient.addName()
                .setFamily(lastName)
                .addPrefix(prefix)
                .addGiven(firstName)
                .addGiven(middleName)
                .setUse(HumanName.NameUse.OFFICIAL);

        patient.addAddress()
                .addLine(adr1)
                .addLine(adr2)
                .addLine(adr3)
                .addLine(adr4)
                .addLine(adr5)
                .setPostalCode(postCode);

        Organization practice = getOrganization(sdsPractice);
        patient.setManagingOrganization(new Reference(practice.getId()));

        if (gp != null) {
            Practitioner doc = getPractitioner(gp);
            if (doc !=null) {
                patient.addGeneralPractitioner(new Reference(doc.getId()));
            }
        }

        switch(gender) {
            case "9" :
                patient.setGender(Enumerations.AdministrativeGender.OTHER);
                break;
            case "1" :
                patient.setGender(Enumerations.AdministrativeGender.MALE);
                break;
            case "2" :
                patient.setGender(Enumerations.AdministrativeGender.FEMALE);
                break;
            case "X" :
                patient.setGender(Enumerations.AdministrativeGender.UNKNOWN);
                break;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            //format.setTimeZone(TimeZone.getTimeZone("UTC"));

           patient.setBirthDate(format.parse(dob));
        } catch (Exception e) {

        }

        return patient;
    }

    private void addPatient(Patient patient) {

        Bundle bundle =  client
                .search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().code(patient.getIdentifier().get(0).getValue()))
                .returnBundle(Bundle.class)
                .execute();
       // System.out.println(ctxFHIR.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient));
        if (bundle.getEntry().size()>0) {
            patient.setId(((Patient) bundle.getEntry().get(0).getResource()).getId());

            client.update().resource(patient).execute();
        } else {
            client.create().resource(patient).execute();
        }
    }

    private Patient getPatient(String nhsNumber) {

        System.out.println("Looking for patient nhsNumber = "+nhsNumber);
        Patient patient = null;
        Bundle bundle =  client
                .search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().code(nhsNumber))

                .returnBundle(Bundle.class)
                .execute();
        if (bundle.getEntry().size()>0) {
            if (bundle.getEntry().get(0).getResource() instanceof Patient)
                patient = (Patient) bundle.getEntry().get(0).getResource();

        } else {

        }
        return patient;
    }

    private NamingSystem postNamingSystem(NamingSystem namingSystem) {

        Bundle bundle =  client
                .search()
                .forResource(NamingSystem.class)
                .where(NamingSystem.VALUE.matches().value(namingSystem.getUniqueIdFirstRep().getValue()))
                .returnBundle(Bundle.class)
                .execute();
        if (bundle.getEntry().size()>0) {
            if (bundle.getEntry().get(0).getResource() instanceof NamingSystem) {
                NamingSystem temp = (NamingSystem) bundle.getEntry().get(0).getResource();
                namingSystem.setId(temp.getId());
                client.update().resource(namingSystem).execute();
            }

        } else {
            client.create().resource(namingSystem).execute();
        }
        return namingSystem;
    }

    private Encounter postEncounter(Encounter encounter) {

        log.info(encounter.getIdentifierFirstRep().getValue());

        Bundle bundle =  client
                .search()
                .forResource(Encounter.class)
                .where(Encounter.IDENTIFIER.exactly().code(encounter.getIdentifierFirstRep().getValue()))
                .returnBundle(Bundle.class)
                .execute();
        if (bundle.getEntry().size()>0) {
            if (bundle.getEntry().get(0).getResource() instanceof Encounter) {
                Encounter temp = (Encounter) bundle.getEntry().get(0).getResource();
                encounter.setId(temp.getId());
                client.update().resource(encounter).execute();
            }

        } else {
            client.create().resource(encounter).execute();
        }
        return encounter;
    }

    private EpisodeOfCare postEpisodeOfCare(EpisodeOfCare episode) {

        Bundle bundle =  client
                .search()
                .forResource(EpisodeOfCare.class)
                .where(EpisodeOfCare.IDENTIFIER.exactly().code(episode.getIdentifierFirstRep().getValue()))
                .returnBundle(Bundle.class)
                .execute();
        if (bundle.getEntry().size()>0) {
            if (bundle.getEntry().get(0).getResource() instanceof EpisodeOfCare) {
                EpisodeOfCare temp = (EpisodeOfCare) bundle.getEntry().get(0).getResource();
                episode.setId(temp.getId());
                client.update().resource(episode).execute();
            }

        } else {
            client.create().resource(episode).execute();
        }
        return episode;
    }

    private Observation postObservation(Observation observation) {

        Bundle bundle =  client
                .search()
                .forResource(Observation.class)
                .where(Observation.IDENTIFIER.exactly().code(observation.getIdentifierFirstRep().getValue()))
                .returnBundle(Bundle.class)
                .execute();
        if (bundle.getEntry().size()>0) {
            if (bundle.getEntry().get(0).getResource() instanceof Observation) {
                Observation temp = (Observation) bundle.getEntry().get(0).getResource();
                observation.setId(temp.getId());
                client.update().resource(observation).execute();
            }

        } else {
            client.create().resource(observation).execute();
        }
        return observation;
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

        } else {
            organization = new Organization();
            organization.addIdentifier()
                    .setSystem("https://fhir.nhs.uk/Id/ods-organization-code")
                    .setValue(sdsCode);
            MethodOutcome outcome = client.create().resource(organization).execute();
            organization = (Organization) outcome.getResource();

        }
        return organization;
    }

    private Practitioner getPractitioner(String gpCode) {


        Practitioner practitioner = null;
        Bundle bundle =  client
                .search()
                .forResource(Practitioner.class)
                .where(Practitioner.IDENTIFIER.exactly().code(gpCode))

                .returnBundle(Bundle.class)
                .execute();
        if (bundle.getEntry().size()>0) {
            if (bundle.getEntry().get(0).getResource() instanceof Practitioner)
                practitioner = (Practitioner) bundle.getEntry().get(0).getResource();

        } else {
            practitioner = new Practitioner();
            practitioner.addIdentifier()
                    .setSystem("https://fhir.nhs.uk/Id/sds-user-id")
                    .setValue(gpCode);
            practitioner.addName()
                    .setFamily("Unk");
            MethodOutcome outcome = client.create().resource(practitioner).execute();
            practitioner = (Practitioner) outcome.getResource();

        }
        return practitioner;
    }

}
