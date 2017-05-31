package uk.nhs.careconnect.server.dstu2;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.NameUseEnum;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import org.dom4j.Document;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kevinmayfield on 27/04/2017.
 */
@Component
public class ApplicationStartup
        implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    /**
     * This event is executed as late as conceivably possible to indicate that
     * the application is ready to service requests.
     */
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        log.info("CareConnect STARTED");
        // here your code ...

        if (false) {
            FhirContext ctxFHIR = FhirContext.forDstu2();
            IParser parser = ctxFHIR.newXmlParser();

            // Pass in a folder as a parameter, use this to load the database.

            String csvFile = "/Development/FHIRTest/CareConnectLoader/src/SampleData/Patients.csv";
            BufferedReader br = null;
            String line = "";
            String cvsSplitBy = ",";
            String serverBase = "http://127.0.0.1:8181/Dstu2";


            IGenericClient client = ctxFHIR.newRestfulGenericClient(serverBase);

            try {

                br = new BufferedReader(new FileReader(csvFile));
                boolean ignoreFirstLine = true;
                while ((line = br.readLine()) != null) {
                    if (ignoreFirstLine) {
                        ignoreFirstLine = false;
                    } else {
                        // use comma as separator
                        String[] patientData = line.split(cvsSplitBy);

                        Patient patient = buildCareConnectFHIRPatient(patientData);
                        System.out.println(parser.setPrettyPrint(true).encodeResourceToString(patient));

                        MethodOutcome outcome = client.update().resource(patient)
                                .conditionalByUrl("Patient?identifier=https://fhir.nhs.net/Id/nhs-number%7C" + patientData[2])
                                .execute();
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            File dir = new File("/Development/CareConnectStructureDefinitions");

            for (File thisFile : dir.listFiles()) {
                log.info(thisFile.getName());
                Document output = null;
                try (FileReader fr = new FileReader(thisFile)) {
                    IBaseResource resource = ctxFHIR.newXmlParser().parseResource(fr);
                    // output = processResource(resource);
                    log.info(resource.getClass().toString());
                    MethodOutcome outcome = client.create().resource(resource)
                            .execute();
                } catch (Exception e) {
                    // TODO
                }
            }
        }
        return;
    }

    public static Patient buildCareConnectFHIRPatient(String[] patientData)
    {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");

        Patient patient = new Patient();

        // Add profile reference - where in dstu2??
        //patient.setMeta(new Meta().addProfile("https://fhir.leedsth.nhs.uk/Dstu2/StructureDefinition/LTHT-Patient-1"));


        IdentifierDt nhsNumber = patient.addIdentifier()
                .setSystem(new String("https://fhir.nhs.net/Id/nhs-number"))
                .setValue(patientData[2]);

        CodeableConceptDt verificationStatusCode = new CodeableConceptDt();
        verificationStatusCode
                .addCoding()
                .setSystem("https://fhir.hl7.org.uk/fhir/ValueSet/CareConnect-NhsNumberVerificationStatus")
                .setDisplay("Number present and verified")
                .setCode("01");

        ExtensionDt verificationStatus = new ExtensionDt()
                .setUrl("http://hl7.org.uk/CareConnect-NhsNumberVerificationStatus-1-Extension.structuredefinition.xml")
                .setValue(verificationStatusCode);
        nhsNumber.addUndeclaredExtension(verificationStatus);

        patient.addIdentifier()
                .setSystem(new String("https://fhir.jorvik.nhs.uk/PAS/Patient"))
                .setValue(patientData[1]);

        patient.addName()
                .setUse(NameUseEnum.USUAL)
                .addFamily(patientData[4])
                .addGiven(patientData[5])
                .addGiven(patientData[6])
                .addPrefix(patientData[7]);

        patient.addAddress()
                .addLine(patientData[8])
                .addLine(patientData[9])
                .addLine(patientData[10])
                .setCity(patientData[11])
                .setDistrict(patientData[12])
                .setPostalCode(patientData[13]);


        switch (patientData[7]) {
            case "MR" :
                patient.setGender(AdministrativeGenderEnum.MALE);
                break;
            case "MISS" :
            case "MRS" :
            case "MS" :
                patient.setGender(AdministrativeGenderEnum.FEMALE);
                break;

        }

        // Not CareConnect compliant
        patient.setGender(AdministrativeGenderEnum.MALE);

        Date birth;
        try {
            birth = dateFormat.parse(patientData[3]);
            patient.setBirthDate(new DateDt(birth));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //

        patient.setManagingOrganization(new ResourceReferenceDt("https://fhir.nhs.net/Organization/"+patientData[14]));



        return patient;
    }

}
