package uk.nhs.careconnect.hapiclient.App;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.*;

import org.hl7.fhir.utilities.xhtml.XhtmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootApplication
public class FhirUnstructuredDocumentApp implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FhirUnstructuredDocumentApp.class);

    final String uuidtag = "urn:uuid:";

    private XhtmlParser xhtmlParser = new XhtmlParser();

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

	public static void main(String[] args) {
        System.getProperties().put( "server.port", 8084 );
		SpringApplication.run(FhirUnstructuredDocumentApp.class, args).close();
	}

    FhirContext ctxFHIR = FhirContext.forDstu3();

    IGenericClient client = null;

    DateFormat df = new SimpleDateFormat("HHmm_dd_MM_yyyy");


    @Override
	public void run(String... args) throws Exception {

        if (args.length > 0 && args[0].equals("exitcode")) {
            throw new Exception();
        }


        client = ctxFHIR.newRestfulGenericClient("https://data.developer.nhs.uk/ccri-fhir/STU3/");
        //client = ctxFHIR.newRestfulGenericClient("http://127.0.0.1:8080/careconnect-gateway/STU3/");

        client.setEncoding(EncodingEnum.XML);

        outputDocument("1",1);
     ///   outputDocument("1",2);
        outputDocument("1",3);
        outputDocument("1002",4);
        outputDocument("2",5);
        outputDocument("3",6);
    }

    private void outputDocument(String patientId, Integer docExample) throws Exception {
        Date date = new Date();

        Bundle unstructDocBundle = getUnstructuredBundle(patientId,docExample);
        String xmlResult = ctxFHIR.newXmlParser().setPrettyPrint(true).encodeResourceToString(unstructDocBundle);

        Files.write(Paths.get("/Temp/"+df.format(date)+"+patient-"+patientId+"-doc-"+docExample+".xml"),xmlResult.getBytes());

        // Uncomment to send to purple
        //
        // client.create().resource(unstructDocBundle).execute();

    }

    private Bundle getUnstructuredBundle(String patientId, Integer docExample) throws Exception {
        // Create Bundle of type Document

        FhirBundleUtil fhirBundle = new FhirBundleUtil(Bundle.BundleType.COLLECTION);

        Bundle bundle = new Bundle();
        // Main resource of a FHIR Bundle is a DocumentReference
        DocumentReference documentReference = new DocumentReference();
        documentReference.setId(fhirBundle.getNewId(documentReference));
        bundle.addEntry().setResource(documentReference);


        documentReference.setCreated(new Date());
        documentReference.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);

        Organization leedsTH = getOrganization("RR8");

        bundle.addEntry().setResource(leedsTH);

        documentReference.setCustodian(new Reference("Organization/"+leedsTH.getIdElement().getIdPart()));
       // log.info("Custodian docRef"+documentReference.getCustodian().getReference());

        Practitioner consultant = getPractitioner("C2381390");

        bundle.addEntry().setResource(consultant);
        documentReference.addAuthor(new Reference("Practitioner/"+consultant.getIdElement().getIdPart()));




        Binary binary = new Binary();
        binary.setId(fhirBundle.getNewId(binary));

        if (docExample == 1) {

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
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("image/3emotng15yvy.jpg");
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
                    .setCode("73625300")
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
        else if (docExample == 7) {
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
        Bundle patientBundle = getPatientBundle(patientId);
        fhirBundle.processBundleResources(patientBundle);

        if (fhirBundle.getPatient() == null) throw new Exception("404 Patient not found");
        documentReference.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        fhirBundle.processReferences();

        return fhirBundle.getFhirDocument();
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


