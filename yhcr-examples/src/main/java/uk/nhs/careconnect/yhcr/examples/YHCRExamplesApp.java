package uk.nhs.careconnect.yhcr.examples;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;

import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.*;
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

       // System.out.println(ctxFHIR.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient));
        addPatient(patient);

        patient = buildPatient("9657702070",
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

        // System.out.println(ctxFHIR.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient));
        addPatient(patient);

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
