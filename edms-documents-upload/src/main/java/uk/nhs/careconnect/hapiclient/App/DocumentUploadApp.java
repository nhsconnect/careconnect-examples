package uk.nhs.careconnect.hapiclient.App;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.*;

import org.hl7.fhir.utilities.xhtml.XhtmlParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.print.Doc;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootApplication
public class DocumentUploadApp implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DocumentUploadApp.class);

    final String uuidtag = "urn:uuid:";

    private XhtmlParser xhtmlParser = new XhtmlParser();

    private Patient patient = null;

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

	public static void main(String[] args) {
        System.getProperties().put( "server.port", 8084 );
		SpringApplication.run(DocumentUploadApp.class, args).close();
	}

    FhirContext ctxFHIR = FhirContext.forDstu3();

    IGenericClient clientEDMS = null;
    IGenericClient clientEPR = null;

    IGenericClient clientNRLS = null;
    DateFormat df = new SimpleDateFormat("HHmm_dd_MM_yyyy");


    @Override
	public void run(String... args) throws Exception {

        if (args.length > 0 && args[0].equals("exitcode")) {
            throw new Exception();
        }


        clientEPR = ctxFHIR.newRestfulGenericClient("https://data.developer.nhs.uk/ccri-fhir/STU3/");
        //client = ctxFHIR.newRestfulGenericClient("http://127.0.0.1:8080/careconnect-gateway/STU3/");

        clientEPR.setEncoding(EncodingEnum.XML);

        clientEDMS = ctxFHIR.newRestfulGenericClient("https://data.developer.nhs.uk/ccri/camel/ccri-document/STU3/");
       // clientEDMS = ctxFHIR.newRestfulGenericClient("https://data.developer-test.nhs.uk/ccri/camel/ccri-document/STU3/");
        // clientEDMS = ctxFHIR.newRestfulGenericClient("https://edms.35.176.40.215.xip.io/STU3");
        //clientEDMS = ctxFHIR.newRestfulGenericClient("http://127.0.0.1:8181/STU3/");

        clientEDMS.setEncoding(EncodingEnum.XML);

        clientNRLS = ctxFHIR.newRestfulGenericClient("https://nrl.dwp.hippodigital.cloud/STU3");

        getSimple();


        outputDocument("9658218873",2);
        outputDocument("9658218873", 3);
        outputDocument("9658218881", 4);
        outputDocument("9658218997", 5);
        outputDocument("9658220169", 6);

        loadFolder("fhirdocuments");

        updateNRLS();
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

    public void loadFile(String folder, String filename) {
        InputStream inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream(folder + "/" +filename);
        Reader reader = new InputStreamReader(inputStream);
        Bundle bundle = null;
        if (FilenameUtils.getExtension(filename).equals("json")) {
            bundle = (Bundle) ctxFHIR.newJsonParser().parseResource(reader);
        } else {
            bundle = (Bundle) ctxFHIR.newXmlParser().parseResource(reader);
        }
        try {
            MethodOutcome outcome = clientEDMS.create().resource(bundle).execute();
        } catch (ResourceVersionConflictException ex) {
            // System.out.println("ERROR - "+filename);
            // System.out.println(ctxFHIR.newXmlParser().encodeResourceToString(ex.getOperationOutcome()));
            if (ex.getStatusCode()==422) {
                System.out.println("Trying to update "+filename+ ": Bundle?identifier="+bundle.getIdentifier().getSystem()+"|"+bundle.getIdentifier().getValue());
                MethodOutcome outcome = clientEDMS.update().resource(bundle).conditionalByUrl("Bundle?identifier="+bundle.getIdentifier().getSystem()+"|"+bundle.getIdentifier().getValue()).execute();
            }
        }
    }


    private void outputDocument(String patientId, Integer docExample) throws Exception {
        Date date = new Date();

        Bundle unstructDocBundle = getUnstructuredBundle(patientId,docExample);
        String xmlResult = ctxFHIR.newXmlParser().setPrettyPrint(true).encodeResourceToString(unstructDocBundle);

        Files.write(Paths.get("/Temp/"+df.format(date)+"+patient-"+patientId+"-doc-"+docExample+".xml"),xmlResult.getBytes());

        // Uncomment to send to purple
        //
        try {
            MethodOutcome outcome = clientEDMS.create().resource(unstructDocBundle).execute();


        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }



    private Bundle getSimple() {
        Bundle bundle = null;

        Binary binary = new Binary();
        binary.setId(UUID.randomUUID().toString());
        String dummyContent = "<!DOCTYPE html><html><body>SOME TEXT</body></html>";
        binary.setContent (dummyContent.getBytes());
        binary.setContentType("text/html");
        //System.out.println(FhirContext.forDstu3().newJsonParser().setPrettyPrint(true).encodeResourceToString(binary));

        DocumentReference doc = new DocumentReference();
        doc.setId(UUID.randomUUID().toString());
      //  doc.getMeta().addProfile("https://fhir.nhs.uk/STU3/StructureDefinition/GPConnect-DocumentReference-1");
        doc.setSubject(new Reference("https://demographics.spineservices.nhs.uk/STU3/Patient/9658220169"));
        doc.addIdentifier(
                new Identifier().setSystem("https://fhir.elmetccg.nhs.uk").setValue("sample")
        );
        doc.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);
        doc.getType().addCoding()
                .setSystem("http://snomed.info/sct")
                .setCode("734163000")
                .setDisplay("Care plan");
        doc.setIndexed(new Date());
        doc.setCreated(new Date());
        doc.setDescription("A document");
        doc.getContext().getPracticeSetting().addCoding()
                .setSystem("http://snomed.info/sct")
                .setCode("408467006")
                .setDisplay("Adult mental illness");
        doc.addContent().getAttachment()
                .setContentType(binary.getContentType())
                .setUrl("urn:uuid:" + binary.getId());
        //System.out.println(FhirContext.forDstu3().newJsonParser().setPrettyPrint(true).encodeResourceToString(doc));

        IGenericClient clientODS = ctxFHIR.newRestfulGenericClient("https://directory.spineservices.nhs.uk/STU3/");
        clientODS.setEncoding(EncodingEnum.XML);
        Organization organization =  clientODS
                .read()
                .resource(Organization.class)
                .withId("RR8").execute();
        organization.setId(UUID.randomUUID().toString());
        doc.addAuthor(new Reference("urn:uuid:" + organization.getId()).setDisplay(organization.getName()));

        Patient patient = null;



        clientODS.setEncoding(EncodingEnum.XML);
        Bundle patientSearchbundle =  clientEPR
                .search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().systemAndCode("https://fhir.nhs.uk/Id/nhs-number","9658220169"))
                .returnBundle(Bundle.class)
                .execute();
        if (patientSearchbundle.getEntry().size()>0) {
            if (patientSearchbundle.getEntry().get(0).getResource() instanceof Patient)
               patient = (Patient) patientSearchbundle.getEntry().get(0).getResource();
        }

        if (patient != null) {
            patient.setId(UUID.randomUUID().toString());
            doc.setSubject(new Reference("urn:uuid:" + patient.getId()));
            patient.setManagingOrganization(null);
            patient.setGeneralPractitioner(new ArrayList<>());

            bundle = new Bundle();
            bundle.addEntry().setResource(doc).setFullUrl("urn:uuid:" + doc.getId());
            bundle.addEntry().setResource(binary).setFullUrl("urn:uuid:" + binary.getId());
            bundle.addEntry().setResource(organization).setFullUrl("urn:uuid:" + organization.getId());
            bundle.addEntry().setResource(patient).setFullUrl("urn:uuid:" + patient.getId());
            bundle.setType(Bundle.BundleType.COLLECTION);
           // System.out.println(FhirContext.forDstu3().newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle));

            try {
                MethodOutcome outcome = clientEDMS.create().resource(bundle).execute();


            } catch (Exception ex) {
                System.out.println("Already exists?");
            }
        }

        return bundle;
    }

    private void updateNRLS() {

        Bundle bundle = clientEDMS.search()
                .forResource(DocumentReference.class)
                .where(DocumentReference.TYPE.exactly()
                        .systemAndCode("http://snomed.info/sct","73625300"))
                .returnBundle(Bundle.class)
                .execute();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            DocumentReference documentReference = (DocumentReference) entry.getResource();
            sendNRLS(documentReference);
        }

        bundle = clientEDMS.search()
                .forResource(DocumentReference.class)
                .where(DocumentReference.TYPE.exactly()
                        .systemAndCode("http://snomed.info/sct","373942005"))
                .returnBundle(Bundle.class)
                .execute();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            DocumentReference documentReference = (DocumentReference) entry.getResource();
            if (documentReference.hasContext()
                    && documentReference.getContext().hasPracticeSetting()
            && documentReference.getContext().getPracticeSetting().hasCoding()
                    ) {
                switch (documentReference.getContext().getPracticeSetting().getCodingFirstRep().getCode()) {

                    case "892811000000109":
                        sendNRLS(documentReference);
                        break;
                }
            }
        }

    }

    private void sendNRLS(DocumentReference documentReference) {
        System.out.println(documentReference.getId());

        Boolean found = false;
        if (documentReference.getSubject().hasIdentifier()) {
            Bundle bundle = clientNRLS.search()
                    .forResource(DocumentReference.class)
                    .where(DocumentReference.PATIENT.hasId(documentReference.getSubject().getIdentifier().getValue()))
                    .returnBundle(Bundle.class)
                    .execute();
            System.out.println(documentReference.getSubject().getIdentifier().getValue() + " - "+ bundle.getEntry().size());
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                if (entry.getResource() instanceof DocumentReference) {
                    DocumentReference nrlsDoc = (DocumentReference) entry.getResource();
                    for (Identifier identifierNRLS : nrlsDoc.getIdentifier()) {
                        for (Identifier identifier : documentReference.getIdentifier()) {
                            if (identifier.getSystem().equals(identifierNRLS.getSystem())
                            && identifier.getValue().equals(identifierNRLS.getValue())) {
                                found = true;
                                break;
                            }
                        }

                    }
                }
            }
        }
        if (!found) {
            System.out.println("Not found");
            documentReference.getType().getCodingFirstRep().setCode("736253002");
            documentReference.addAuthor().setReference("https://directory.spineservices.nhs.uk/STU3/Organization/MHT01");
            documentReference.setCustodian(new Reference("https://directory.spineservices.nhs.uk/STU3/Organization/MHT01"));
            clientNRLS.create().resource(documentReference).execute();
        }

    }




    private Bundle getUnstructuredBundle(String patientId, Integer docExample) throws Exception {
        // Create Bundle of type Document

        FhirBundleUtil fhirBundle = new FhirBundleUtil(Bundle.BundleType.COLLECTION);

        Bundle bundle = new Bundle();
        // Main resource of a FHIR Bundle is a DocumentReference
        DocumentReference documentReference = new DocumentReference();
        documentReference.setId(fhirBundle.getNewId(documentReference));
        bundle.addEntry().setResource(documentReference);

        documentReference.addIdentifier(
                new Identifier().setSystem("https://fhir.elmetccg.nhs.uk").setValue(docExample.toString())
        );


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
        Bundle bundle = clientEPR
                .search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().code(patientId))
                .include(Patient.INCLUDE_GENERAL_PRACTITIONER)
                .include(Patient.INCLUDE_ORGANIZATION)
                .returnBundle(Bundle.class)
                .execute();
        if (bundle.getEntryFirstRep().getResource() instanceof Patient) {
            patient = (Patient) bundle.getEntryFirstRep().getResource();
        }
        return bundle;
    }

    private Practitioner getPractitioner(String sdsCode) {
        Practitioner practitioner = null;
        Bundle bundle =  clientEPR
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
        Bundle bundle =  clientEPR
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


