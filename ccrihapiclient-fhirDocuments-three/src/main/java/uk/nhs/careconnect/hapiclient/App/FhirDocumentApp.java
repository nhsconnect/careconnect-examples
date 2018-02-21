package uk.nhs.careconnect.hapiclient.App;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
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
import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
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

    Context ctxThymeleaf = new Context();

    private XhtmlParser xhtmlParser = new XhtmlParser();

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

	public static void main(String[] args) {
        System.getProperties().put( "server.port", 8083 );
		SpringApplication.run(FhirDocumentApp.class, args).close();

	}


    FhirContext ctxFHIR = FhirContext.forDstu3();

    IGenericClient client = null;

    String HAPIServer = "http://fhirtest.uhn.ca/baseDstu2/";

    Map<String,String> referenceMap = new HashMap<>();

    DateFormat df = new SimpleDateFormat("HHmm_dd_MM_yyyy");

    final String uuidtag = "urn:uuid:";


    @Override
	public void run(String... args) throws Exception {

        if (args.length > 0 && args[0].equals("exitcode")) {
            throw new Exception();
        }


        client = ctxFHIR.newRestfulGenericClient("http://purple.testlab.nhs.uk/careconnect-ri/STU3/");


        outputCareRecord("1177");
        outputCareRecord("1098");


    }
    private void outputCareRecord(String patientId) throws Exception {
        Date date = new Date();

        Bundle careRecord = getCareRecord(patientId);
        String xmlResult = ctxFHIR.newXmlParser().encodeResourceToString(careRecord);

        Files.write(Paths.get("C:\\Temp\\"+df.format(date)+"+patient-"+patientId+".xml"),xmlResult.getBytes());

        performTransform(xmlResult,"C:\\Temp\\"+df.format(date)+"+patient-"+patientId+".html","XML/DocumentToHTML.xslt");
    }



    private Bundle getCareRecord(String patientId) throws Exception {
        // Create Bundle of type Document
        Bundle fhirDocument = new Bundle().setType(Bundle.BundleType.DOCUMENT);

        // Main resource of a FHIR Bundle is a Composition
        Composition composition = new Composition();
        composition.setId(UUID.randomUUID().toString());
        composition.setTitle("Patient Summary Care Record");


        fhirDocument.addEntry().setResource(composition);

        Patient patient = null;
        Practitioner gp = null;
        Organization practice = null;


        // This is a synthea patient
        Bundle patientBundle = getPatientBundle(patientId);

        for (Bundle.BundleEntryComponent entry : patientBundle.getEntry()) {
            if (entry.getResource() instanceof Patient) {
                patient = (Patient) entry.getResource();
                patient = generatePatientHtml(patient);
                patientId = patient.getId();
                patient.setId(getNewReferenceUri(patient));

                composition.setSubject(new Reference(uuidtag+patient.getId()));
                fhirDocument.addEntry().setResource(patient).setFullUrl(uuidtag + patient.getId());;
            }
            if (entry.getResource() instanceof Practitioner) {
                gp = (Practitioner) entry.getResource();
                gp.setId(getNewReferenceUri(gp));
                if (patient != null && patient.getGeneralPractitioner().size()==1) {
                    patient.getGeneralPractitioner().get(0).setReference(uuidtag + gp.getId());
                }
                fhirDocument.addEntry().setResource(gp).setFullUrl(uuidtag + gp.getId());
            }
            if (entry.getResource() instanceof Organization) {
                practice = (Organization) entry.getResource();
                practice.setId(getNewReferenceUri(practice));
                if (patient != null ) {
                    patient.setManagingOrganization(new Reference(uuidtag + practice.getId()));
                }
                fhirDocument.addEntry().setResource(gp).setFullUrl(uuidtag + practice.getId());
            }
        }
        if (patient == null) throw new Exception("404 Patient not found");

        /* CONDITION */

        Bundle conditionBundle = getConditionBundle(patientId);
        for (Bundle.BundleEntryComponent entry : conditionBundle.getEntry()) {
            if (entry.getResource() instanceof Condition) {
                Condition condition = (Condition) entry.getResource();

                condition.setId(getNewReferenceUri(condition));
                condition.setSubject(new Reference(uuidtag+patient.getId()));
                fhirDocument.addEntry().setResource(entry.getResource()).setFullUrl(uuidtag + condition.getId());
            }
        }
        composition.addSection(getConditionSection(conditionBundle));


        /* ALLERGY INTOLERANCE */

        Bundle allergyBundle = getAllergyBundle(patientId);
        for (Bundle.BundleEntryComponent entry : allergyBundle.getEntry()) {
            if (entry.getResource() instanceof AllergyIntolerance) {
                AllergyIntolerance allergyIntolerance = (AllergyIntolerance) entry.getResource();

                allergyIntolerance.setId(getNewReferenceUri(allergyIntolerance));
                allergyIntolerance.setPatient(new Reference(uuidtag+patient.getId()));
                fhirDocument.addEntry().setResource(entry.getResource()).setFullUrl(uuidtag + allergyIntolerance.getId());
            }
        }
        composition.addSection(getAllergySection(allergyBundle));

        /* ENCOUNTER */

        Bundle encounterBundle = getEncounterBundle(patientId);
        for (Bundle.BundleEntryComponent entry : encounterBundle.getEntry()) {
            if (entry.getResource() instanceof Encounter) {
                Encounter encounter = (Encounter) entry.getResource();
                encounter.setId(getNewReferenceUri(encounter));
                encounter.setSubject(new Reference(uuidtag+patient.getId()));
                fhirDocument.addEntry().setResource(entry.getResource()).setFullUrl(uuidtag + encounter.getId());
            }
        }
        composition.addSection(getEncounterSection(encounterBundle));


        log.debug(ctxFHIR.newJsonParser().setPrettyPrint(true).encodeResourceToString(fhirDocument));

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
    private Patient generatePatientHtml(Patient patient) {
        if (!patient.hasText()) {

            ctxThymeleaf.clearVariables();
            ctxThymeleaf.setVariable("patient", patient);

            patient.getText().setDiv(getDiv("patient")).setStatus(Narrative.NarrativeStatus.GENERATED);
            log.debug(patient.getText().getDiv().getValueAsString());
        }
        return patient;
    }

    private XhtmlNode getDiv(String template) {
        XhtmlNode xhtmlNode = null;
        String processedHtml = templateEngine.process(template, ctxThymeleaf);
        try {
            XhtmlDocument parsed = xhtmlParser.parse(processedHtml, null);
            xhtmlNode = parsed.getDocumentElement();
            log.debug(processedHtml);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return xhtmlNode;
    }

    private Composition.SectionComponent getConditionSection(Bundle bundle) {
        Composition.SectionComponent section = new Composition.SectionComponent();

        ArrayList<Condition>  conditions = new ArrayList<>();

        section.getCode().addCoding()
                .setSystem(CareConnectSystem.SNOMEDCT)
                .setCode("887151000000100")
                .setDisplay("Problems and issues");
        section.setTitle("Problems and issues");

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof Condition) {
                Condition condition = (Condition) entry.getResource();
                section.getEntry().add(new Reference("urn:uuid:"+condition.getId()));
                conditions.add(condition);
            }
        }
        ctxThymeleaf.clearVariables();
        ctxThymeleaf.setVariable("conditions", conditions);

        section.getText().setDiv(getDiv("condition")).setStatus(Narrative.NarrativeStatus.GENERATED);

        return section;
    }

    private Composition.SectionComponent getAllergySection(Bundle bundle) {
        Composition.SectionComponent section = new Composition.SectionComponent();

        ArrayList<AllergyIntolerance>  allergyIntolerances = new ArrayList<>();

        section.getCode().addCoding()
                .setSystem(CareConnectSystem.SNOMEDCT)
                .setCode("886921000000105")
                .setDisplay("Allergies and adverse reactions");
        section.setTitle("Allergies and adverse reactions");

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof AllergyIntolerance) {
                AllergyIntolerance allergyIntolerance = (AllergyIntolerance) entry.getResource();
                section.getEntry().add(new Reference("urn:uuid:"+allergyIntolerance.getId()));
                allergyIntolerances.add(allergyIntolerance);
            }
        }
        ctxThymeleaf.clearVariables();

        ctxThymeleaf.setVariable("allergies", allergyIntolerances);

        section.getText().setDiv(getDiv("allergy")).setStatus(Narrative.NarrativeStatus.GENERATED);

        return section;
    }

    private Composition.SectionComponent getEncounterSection(Bundle bundle) {
        Composition.SectionComponent section = new Composition.SectionComponent();
        // TODO Get Correct code.
        ArrayList<Encounter>  encounters = new ArrayList<>();

        section.getCode().addCoding()
                .setSystem(CareConnectSystem.SNOMEDCT)
                .setCode("713511000000103")
                .setDisplay("Encounter administration");
        section.setTitle("Encounters");

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof Encounter) {
                Encounter encounter = (Encounter) entry.getResource();
                section.getEntry().add(new Reference("urn:uuid:"+encounter.getId()));
                encounters.add(encounter);
            }
        }
        ctxThymeleaf.clearVariables();
        ctxThymeleaf.setVariable("encounters", encounters);

        section.getText().setDiv(getDiv("encounter")).setStatus(Narrative.NarrativeStatus.GENERATED);

        return section;
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

    private Bundle getAllergyBundle(String patientId) {

        return client
                .search()
                .forResource(AllergyIntolerance.class)
                .where(AllergyIntolerance.PATIENT.hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();
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


