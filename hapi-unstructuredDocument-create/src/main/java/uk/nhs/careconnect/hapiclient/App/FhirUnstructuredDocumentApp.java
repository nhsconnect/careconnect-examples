package uk.nhs.careconnect.hapiclient.App;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.*;

import org.hl7.fhir.utilities.xhtml.XhtmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootApplication
public class FhirUnstructuredDocumentApp implements CommandLineRunner {

    @Autowired
    private TemplateEngine templateEngine;

    private static final Logger log = LoggerFactory.getLogger(FhirUnstructuredDocumentApp.class);

    Context ctxThymeleaf = new Context();

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

    Map<String,String> referenceMap = new HashMap<>();

    DateFormat df = new SimpleDateFormat("HHmm_dd_MM_yyyy");

    final String uuidtag = "urn:uuid:";


    @Override
	public void run(String... args) throws Exception {

        if (args.length > 0 && args[0].equals("exitcode")) {
            throw new Exception();
        }


        //client = ctxFHIR.newRestfulGenericClient("http://purple.testlab.nhs.uk/careconnect-ri/STU3/");
        client = ctxFHIR.newRestfulGenericClient("http://127.0.0.1:8080/careconnect-gateway/STU3/");

        client.setEncoding(EncodingEnum.XML);

        outputDocument("1098");
    }

    private void outputDocument(String patientId) throws Exception {
        Date date = new Date();

        Bundle unstructDocBundle = getUnstructuredBundle(patientId);
        String xmlResult = ctxFHIR.newXmlParser().setPrettyPrint(true).encodeResourceToString(unstructDocBundle);

        Files.write(Paths.get("/Temp/"+df.format(date)+"+patient-"+patientId+".xml"),xmlResult.getBytes());

        client.create().resource(unstructDocBundle).execute();
       // Files.write(Paths.get("/Temp/"+df.format(date)+"+patient-"+patientId+".json"),ctxFHIR.newJsonParser().setPrettyPrint(true).encodeResourceToString(careRecord).getBytes());
    }

    private Bundle getUnstructuredBundle(String patientId) throws Exception {
        // Create Bundle of type Document
        Bundle fhirDocument = new Bundle()
                .setType(Bundle.BundleType.COLLECTION);

        fhirDocument.getIdentifier().setValue(UUID.randomUUID().toString()).setSystem("https://tools.ietf.org/html/rfc4122");

        // Main resource of a FHIR Bundle is a DocumentReference
        DocumentReference documentReference = new DocumentReference();
        documentReference.setId(UUID.randomUUID().toString());
        fhirDocument.addEntry().setResource(documentReference).setFullUrl(uuidtag + documentReference.getId());


        documentReference.setCreated(new Date());
        documentReference.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);

        Organization leedsTH = getOrganization("RR8");
        leedsTH.setId(getNewReferenceUri(leedsTH));
        fhirDocument.addEntry().setResource(leedsTH).setFullUrl(uuidtag + leedsTH.getId());
        documentReference.setCustodian(new Reference(uuidtag+leedsTH.getId()));

        Practitioner consultant = getPractitioner("C2381390");
        consultant.setId(getNewReferenceUri(consultant));
        fhirDocument.addEntry().setResource(consultant).setFullUrl(uuidtag + consultant.getId());
        documentReference.addAuthor(new Reference(uuidtag+consultant.getId()));


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


        Binary binary = new Binary();
        binary.setId(UUID.randomUUID().toString());
        InputStream inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("image/3emotng15yvy.jpg");
        binary.setContent(IOUtils.toByteArray (inputStream));
        binary.setContentType("image/jpeg");
        fhirDocument.addEntry().setResource(binary).setFullUrl(uuidtag + binary.getId());

        // Short test stub
        IGenericClient  iGenericClient = ctxFHIR.newRestfulGenericClient("http://127.0.0.1:8181/STU3/");
        iGenericClient.setEncoding(EncodingEnum.XML);
        iGenericClient.create().resource(binary).execute();


        documentReference.addContent()
                .getAttachment()
                    .setUrl(binary.getId())
                    .setContentType(binary.getContentType());

        Patient patient = null;
        Practitioner gp = null;
        Organization practice = null;

        // This is a synthea patient
        Bundle patientBundle = getPatientBundle(patientId);

        for (Bundle.BundleEntryComponent entry : patientBundle.getEntry()) {
            if (entry.getResource() instanceof Patient) {
                patient = (Patient) entry.getResource();

                patientId = patient.getId();

                patient.setId(getNewReferenceUri(patient));

                documentReference.setSubject(new Reference(uuidtag+patient.getId()));
                fhirDocument.addEntry().setResource(patient).setFullUrl(uuidtag + patient.getId());;
            }
            if (entry.getResource() instanceof Practitioner) {
                gp = (Practitioner) entry.getResource();

                gp.setId(getNewReferenceUri(gp));
                if (patient != null && patient.getGeneralPractitioner().size()>0) {
                    patient.getGeneralPractitioner().get(0).setReference(uuidtag + gp.getId());
                    fhirDocument.addEntry().setResource(gp).setFullUrl(uuidtag + gp.getId());
                }

            }
            if (entry.getResource() instanceof Organization) {
                practice = (Organization) entry.getResource();

                practice.setId(getNewReferenceUri(practice));
                if (patient != null ) {
                    patient.setManagingOrganization(new Reference(uuidtag + practice.getId()));
                }
                fhirDocument.addEntry().setResource(practice).setFullUrl(uuidtag + practice.getId());
            }
        }
        if (patient == null) throw new Exception("404 Patient not found");

       // log.debug(ctxFHIR.newJsonParser().setPrettyPrint(true).encodeResourceToString(fhirDocument));

        return fhirDocument;
    }

    private Bundle getPatientBundle(String patientId) {
        return client
                .search()
                .forResource(Patient.class)
                .where(Patient.RES_ID.exactly().code(patientId))
                .include(Patient.INCLUDE_GENERAL_PRACTITIONER)
                .include(Patient.INCLUDE_ORGANIZATION)
                .returnBundle(Bundle.class)
                .execute();
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

    private String getNewReferenceUri(Resource resource) {
        return getNewReferenceUri(resource.getResourceType().toString()+"/"+resource.getId());
    }

    private String getNewReferenceUri(String reference) {
        String newReference = referenceMap.get(reference);
        if (newReference != null ) return newReference;
        newReference = UUID.randomUUID().toString();
        referenceMap.put(reference,newReference);
        return newReference;
    }



}


