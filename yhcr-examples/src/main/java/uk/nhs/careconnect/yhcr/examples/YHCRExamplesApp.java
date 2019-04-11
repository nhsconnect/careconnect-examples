package uk.nhs.careconnect.yhcr.examples;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;

import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.math.BigDecimal;
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


    FhirBundleUtil fhirBundle;


    public static final String SNOMEDCT = "http://snomed.info/sct";


    DateFormat df = new SimpleDateFormat("HHmm_dd_MM_yyyy");

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && args[0].equals("exitcode")) {
            throw new Exception();
        }

        client = ctxFHIR.newRestfulGenericClient("https://data.developer-test.nhs.uk/ccri-fhir/STU3/");
        client.setEncoding(EncodingEnum.XML);

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
                "TEST1PPM");
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
                "TEST2PPM");
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
                "TEST3PPM");
        addPatient(patient);

        patient = buildPatient("9657702437",
                "Miss",
                "Debra",
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
                "TEST4PPM");
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
                "1983-07-23",
                "TEST5PPM");
        addPatient(patient);

        patient = buildPatient("9657702402",
                "Ms",
                "ALISON",
                "Jean",
                "Huxley",
                "",
                "1 SEVERN DRIVE",
                "MILNROW",
                "ROCHDALE",
                "LANCS",
                "OL16 3ES",
                "P86609",
                "9",
                "1990-11-29",
                "TEST6PPM");
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
                "TEST7PPM");
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
                "TEST8PPM");
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
                "TEST8PPM");
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
                "TEST10PPM");
        addPatient(patient);

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
           // log.info(patient.getIdElement().getIdPart());
            encounter.setSubject(new Reference("Patient/"+patient.getIdElement().getIdPart()));

            System.out.println(ctxFHIR.newJsonParser().setPrettyPrint(true).encodeResourceToString(encounter));

            client.search().forResource(Encounter.class).
        }

        /*
        try {
            MethodOutcome outcome = client.create().resource(bundle).execute();
        } catch (UnprocessableEntityException ex) {
            System.out.println("ERROR - "+filename);
            System.out.println(ctxFHIR.newXmlParser().encodeResourceToString(ex.getOperationOutcome()));
            if (ex.getStatusCode()==422) {
                System.out.println("Trying to update "+filename+ ": Bundle?identifier="+bundle.getIdentifier().getSystem()+"|"+bundle.getIdentifier().getValue());
                MethodOutcome outcome = client.update().resource(bundle).conditionalByUrl("Bundle?identifier="+bundle.getIdentifier().getSystem()+"|"+bundle.getIdentifier().getValue()).execute();
            }
        }

         */
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
            String ppmno
    ) {
        Organization practice = getOrganization(sdsPractice);

        Patient patient = new Patient();

        patient.getMeta().addProfile("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Patient-1");

        Identifier nhs = patient.addIdentifier()
                .setSystem("https://fhir.nhs.uk/Id/nhs-number")
                .setValue(nhsNumber);
        CodeableConcept code= new CodeableConcept();
                code.addCoding()
                    .setCode("01")
                    .setSystem("https://fhir.hl7.org.uk/STU3/CodeSystem/CareConnect-NHSNumberVerificationStatus-1");

        nhs.addExtension()
                .setUrl("https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-NHSNumberVerificationStatus-1")
                .setValue(code);

        Identifier ppm = patient.addIdentifier()
                .setSystem("https://fhir.leedsth.nhs.uk/Id/ppm-number")
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

        patient.setManagingOrganization(new Reference(practice.getId()));

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
            format.setTimeZone(TimeZone.getTimeZone("UTC"));

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
        if (bundle.getEntry().size()>0) {
            patient.setId(((Patient) bundle.getEntry().get(0).getResource()).getId());
            client.update().resource(patient).execute();
        } else {
            client.create().resource(patient).execute();
        }
    }

    private Patient getPatient(String nhsNumber) {
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

    private Encounter postEncounter(Encounter encounter) {
        Encounter encounter = null;
        Bundle bundle =  client
                .search()
                .forResource(Encounter.class)
                .where(Encounter.IDENTIFIER.exactly().code(encounter.getIdentifierFirstRep().getValue()))
                .returnBundle(Bundle.class)
                .execute();
        if (bundle.getEntry().size()>0) {
            if (bundle.getEntry().get(0).getResource() instanceof Encounter) {
                Encounter temp = (Encounter) bundle.getEntry().get(0).getResource();
            }

        } else {

        }
        return encounter;
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



}
