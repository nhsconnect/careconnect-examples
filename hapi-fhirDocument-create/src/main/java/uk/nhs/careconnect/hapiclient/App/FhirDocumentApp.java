package uk.nhs.careconnect.hapiclient.App;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.utilities.xhtml.XhtmlDocument;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.hl7.fhir.utilities.xhtml.XhtmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;


import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootApplication
public class FhirDocumentApp implements CommandLineRunner {

    @Autowired
    private TemplateEngine templateEngine;

    private static final Logger log = LoggerFactory.getLogger(FhirDocumentApp.class);

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

	public static void main(String[] args) {
        System.getProperties().put( "server.port", 8083 );
		SpringApplication.run(FhirDocumentApp.class, args).close();
	}


    FhirContext ctxFHIR = FhirContext.forDstu3();

    IGenericClient client = null;

    public static final String SNOMEDCT = "http://snomed.info/sct";


    DateFormat df = new SimpleDateFormat("HHmm_dd_MM_yyyy");

    Composition composition = null;

    private FhirBundleUtil fhirBundleUtil;

    @Override
	public void run(String... args) throws Exception {

        if (args.length > 0 && args[0].equals("exitcode")) {
            throw new Exception();
        }


        client = ctxFHIR.newRestfulGenericClient("https://data.developer.nhs.uk/ccri-fhir/STU3/");

        client.setEncoding(EncodingEnum.XML);

        outputCareRecord("1098");
      //  outputCareRecord("1177");

        Bundle encounterBundle = buildEncounterDocument(client, new IdType().setValue("1700"));
        Date date = new Date();
        String xmlResult = ctxFHIR.newXmlParser().setPrettyPrint(true).encodeResourceToString(encounterBundle);

        Files.write(Paths.get("/Temp/"+df.format(date)+"+encounter-"+"1700"+"-document.xml"),xmlResult.getBytes());
    }




    private void outputCareRecord(String patientId) throws Exception {
        Date date = new Date();

        Bundle careRecord = getCareRecord(patientId);
        String xmlResult = ctxFHIR.newXmlParser().setPrettyPrint(true).encodeResourceToString(careRecord);

        Files.write(Paths.get("/Temp/"+df.format(date)+"+patientCareRecord-"+patientId+".xml"),xmlResult.getBytes());
        Files.write(Paths.get("/Temp/"+df.format(date)+"+patientCareRecord-"+patientId+".json"),ctxFHIR.newJsonParser().setPrettyPrint(true).encodeResourceToString(careRecord).getBytes());
/*
        String htmlFilename = "/Temp/"+df.format(date)+"+patient-"+patientId+".html";
        performTransform(xmlResult,htmlFilename,"XML/DocumentToHTML.xslt");
        outputPDF(htmlFilename, "/Temp/"+df.format(date)+"+patient-"+patientId+".pdf");

        IGenericClient clientTest = ctxFHIR.newRestfulGenericClient("http://127.0.0.1:8080/careconnect-gateway/STU3/");
        clientTest.create().resource(careRecord).execute();
        */
    }

    private void outputPDF(String processedHtml, String outputFileName ) throws Exception {
        FileOutputStream os = null;
        String fileName = UUID.randomUUID().toString();
        try {
            final File outputFile = new File(outputFileName);
            os = new FileOutputStream(outputFile);

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocument(new File(processedHtml));
            renderer.layout();
            renderer.createPDF(os, false);
            renderer.finishPDF();
            //return outputFile.getAbsolutePath();
        }
        finally {

            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) { /*ignore*/ }
            }
        }
    }

    public Bundle buildEncounterDocument(IGenericClient client, IdType encounterId) throws Exception {


        fhirBundleUtil = new FhirBundleUtil(Bundle.BundleType.DOCUMENT);
        Bundle compositionBundle = new Bundle();

        // Main resource of a FHIR Bundle is a Composition
        composition = new Composition();
        composition.setId(UUID.randomUUID().toString());
        compositionBundle.addEntry().setResource(composition);

        // composition.getMeta().addProfile(CareConnectProfile.Composition_1);
        composition.setTitle("Encounter Document");
        composition.setDate(new Date());
        composition.setStatus(Composition.CompositionStatus.FINAL);

        Organization leedsTH = getOrganization(client,"RR8");
        compositionBundle.addEntry().setResource(leedsTH);

        composition.addAttester()
                .setParty(new Reference("Organization/"+leedsTH.getIdElement().getIdPart()))
                .addMode(Composition.CompositionAttestationMode.OFFICIAL);


        Device device = new Device();
        device.setId(UUID.randomUUID().toString());
        device.getType().addCoding()
                .setSystem("http://snomed.info/sct")
                .setCode("58153004")
                .setDisplay("Android");
        device.setOwner(new Reference("Organization/"+leedsTH.getIdElement().getIdPart()));
        compositionBundle.addEntry().setResource(device);

        composition.addAuthor(new Reference("Device/"+device.getIdElement().getIdPart()));

        composition.getType().addCoding()
                .setCode("371531000")
                .setDisplay("Report of clinical encounter")
                .setSystem(SNOMEDCT);

        fhirBundleUtil.processBundleResources(compositionBundle);
        fhirBundleUtil.processReferences();


        Bundle encounterBundle = getEncounterBundleRev(client, encounterId.getIdPart());
        Encounter encounter = null;
        for(Bundle.BundleEntryComponent entry : encounterBundle.getEntry()) {
            Resource resource =  entry.getResource();
            if (encounter == null && entry.getResource() instanceof Encounter) {
                encounter = (Encounter) entry.getResource();
            }
        }
        String patientId = null;

        if (encounter!=null) {

            patientId = encounter.getSubject().getReferenceElement().getIdPart();
            log.debug(encounter.getSubject().getReferenceElement().getIdPart());


            // This is a synthea patient
            Bundle patientBundle = getPatientBundle(client, patientId);

            fhirBundleUtil.processBundleResources(patientBundle);

            if (fhirBundleUtil.getPatient() == null) throw new Exception("404 Patient not found");
            composition.setSubject(new Reference("Patient/"+patientId));
            patientId = fhirBundleUtil.getPatient().getId();
        }

        if (fhirBundleUtil.getPatient() == null) throw new UnprocessableEntityException();

        fhirBundleUtil.processBundleResources(encounterBundle);

        fhirBundleUtil.processReferences();

        FhirDocUtil fhirDoc = new FhirDocUtil(templateEngine);

        composition.addSection(fhirDoc.getEncounterSection(fhirBundleUtil.getFhirDocument()));

        Composition.SectionComponent section = fhirDoc.getConditionSection(fhirBundleUtil.getFhirDocument());
        if (section.getEntry().size()>0) composition.addSection(section);

        section = fhirDoc.getMedicationStatementSection(fhirBundleUtil.getFhirDocument());
        if (section.getEntry().size()>0) composition.addSection(section);

        section = fhirDoc.getMedicationRequestSection(fhirBundleUtil.getFhirDocument());
        if (section.getEntry().size()>0) composition.addSection(section);

        section = fhirDoc.getAllergySection(fhirBundleUtil.getFhirDocument());
        if (section.getEntry().size()>0) composition.addSection(section);

        section = fhirDoc.getObservationSection(fhirBundleUtil.getFhirDocument());
        if (section.getEntry().size()>0) composition.addSection(section);

        section = fhirDoc.getProcedureSection(fhirBundleUtil.getFhirDocument());
        if (section.getEntry().size()>0) composition.addSection(section);

        return fhirBundleUtil.getFhirDocument();
    }

    private Bundle getCareRecord(String patientId) throws Exception {
        // Create Bundle of type Document
        fhirBundleUtil = new FhirBundleUtil(Bundle.BundleType.DOCUMENT);
        // Main resource of a FHIR Bundle is a Composition

        Bundle compositionBundle = new Bundle();

        composition = new Composition();
        composition.setId(UUID.randomUUID().toString());
        compositionBundle.addEntry().setResource(composition);

        composition.setTitle("Patient Summary Care Record");
        composition.setDate(new Date());
        composition.setStatus(Composition.CompositionStatus.FINAL);

        Organization leedsTH = getOrganization(client,"RR8");
        compositionBundle.addEntry().setResource(leedsTH);

        composition.addAttester()
                .setParty(new Reference(leedsTH.getId()))
                .addMode(Composition.CompositionAttestationMode.OFFICIAL);

        Device device = new Device();
        device.setId(UUID.randomUUID().toString());
        device.getType().addCoding()
                .setSystem("http://snomed.info/sct")
                .setCode("58153004")
                .setDisplay("Android");
        device.setOwner(new Reference("Organization/"+leedsTH.getIdElement().getIdPart()));

        compositionBundle.addEntry().setResource(device);

        composition.addAuthor(new Reference("Device/"+device.getIdElement().getIdPart()));

        fhirBundleUtil.processBundleResources(compositionBundle);
        fhirBundleUtil.processReferences();

        // This is a synthea patient
        Bundle patientBundle = getPatientBundle(client, patientId);
        fhirBundleUtil.processBundleResources(patientBundle);

        if (fhirBundleUtil.getPatient() == null) throw new Exception("404 Patient not found");
        composition.setSubject(new Reference("Patient/"+patientId));

        FhirDocUtil fhirDoc = new FhirDocUtil(templateEngine);

        patientId = fhirBundleUtil.getPatient().getId();
        fhirDoc.generatePatientHtml(fhirBundleUtil.getPatient(),patientBundle);

        /* CONDITION */

        Bundle conditionBundle = getConditionBundle(patientId);
        fhirBundleUtil.processBundleResources(conditionBundle);
        composition.addSection(fhirDoc.getConditionSection(conditionBundle));

        /* MEDICATION STATEMENT */

        Bundle medicationStatementBundle = getMedicationStatementBundle(patientId);
        fhirBundleUtil.processBundleResources(medicationStatementBundle);
        composition.addSection(fhirDoc.getMedicationStatementSection(medicationStatementBundle));


        /* ALLERGY INTOLERANCE */

        Bundle allergyBundle = getAllergyBundle(patientId);
        fhirBundleUtil.processBundleResources(allergyBundle);
        composition.addSection(fhirDoc.getAllergySection(allergyBundle));

        /* ENCOUNTER */

        Bundle encounterBundle = getEncounterBundle(patientId);
        fhirBundleUtil.processBundleResources(encounterBundle);
        composition.addSection(fhirDoc.getEncounterSection(encounterBundle));

        fhirBundleUtil.processReferences();
        log.debug(ctxFHIR.newJsonParser().setPrettyPrint(true).encodeResourceToString(fhirBundleUtil.getFhirDocument()));

        return fhirBundleUtil.getFhirDocument();
    }


    private Bundle getPatientBundle(IGenericClient client, String patientId) {


        Bundle patientBundle = client
                .search()
                .forResource(Patient.class)
                .where(Patient.RES_ID.exactly().code(patientId))
                .include(Patient.INCLUDE_GENERAL_PRACTITIONER)
                .include(Patient.INCLUDE_ORGANIZATION)
                .returnBundle(Bundle.class)
                .execute();

        return patientBundle;
    }


    private Bundle getEncounterBundleRev(IGenericClient client, String encounterId) {

        Bundle bundle = client
                .search()
                .forResource(Encounter.class)
                .where(Patient.RES_ID.exactly().code(encounterId))
                .revInclude(new Include("*"))
                .include(new Include("*"))
                .count(100) // be careful of this TODO
                .returnBundle(Bundle.class)
                .execute();
        return bundle;
    }
    private Bundle getConditionBundle(String patientId) {

        return client
                .search()
                .forResource(Condition.class)
                .where(Condition.PATIENT.hasId(patientId))
                .and(Condition.CLINICAL_STATUS.exactly().code("active"))
                .returnBundle(Bundle.class)
                .execute();
    }
    private Bundle getEncounterBundle(String patientId) {

        return client
                .search()
                .forResource(Encounter.class)
                .where(Encounter.PATIENT.hasId(patientId))
                .count(3) // Last 3 entries same as GP Connect
                .returnBundle(Bundle.class)
                .execute();
    }

    private Organization getOrganization(IGenericClient client,String sdsCode) {
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
    private Bundle getMedicationStatementBundle(String patientId) {

        return client
                .search()
                .forResource(MedicationStatement.class)
                .where(MedicationStatement.PATIENT.hasId(patientId))
                .and(MedicationStatement.STATUS.exactly().code("active"))
                .returnBundle(Bundle.class)
                .execute();
    }

    private Bundle getMedicationRequestBundle(IGenericClient client,String patientId) {

        return client
                .search()
                .forResource(MedicationStatement.class)
                .where(MedicationRequest.PATIENT.hasId(patientId))
                .and(MedicationRequest.STATUS.exactly().code("active"))
                .returnBundle(Bundle.class)
                .execute();
    }

    private Bundle getAllergyBundle(String patientId) {

        return client
                .search()
                .forResource(AllergyIntolerance.class)
                .where(AllergyIntolerance.PATIENT.hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();
    }



    private void performTransform(String xmlInput, String htmlOutput, String styleSheet) {

        // Input xml data file
        ClassLoader classLoader = getContextClassLoader();

        // Input xsl (stylesheet) file
        String xslInput = classLoader.getResource(styleSheet).getFile();

        // Set the property to use xalan processor
        System.setProperty("javax.xml.transform.TransformerFactory",
                "org.apache.xalan.processor.TransformerFactoryImpl");

        // try with resources
        try {
            InputStream xml = new ByteArrayInputStream(xmlInput.getBytes(StandardCharsets.UTF_8));

            FileOutputStream os = new FileOutputStream(htmlOutput);
            FileInputStream xsl = new FileInputStream(xslInput);

            // Instantiate a transformer factory
            TransformerFactory tFactory = TransformerFactory.newInstance();

            // Use the TransformerFactory to process the stylesheet source and produce a Transformer
            StreamSource styleSource = new StreamSource(xsl);
            Transformer transformer = tFactory.newTransformer(styleSource);

            // Use the transformer and perform the transformation
            StreamSource xmlSource = new StreamSource(xml);
            StreamResult result = new StreamResult(os);
            transformer.transform(xmlSource, result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


}


