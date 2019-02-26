package uk.nhs.careconnect.interopen.examples;

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
public class INTEROPenExamplesApp implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(INTEROPenExamplesApp.class);

    private static String yasEncounterIdentifier = "https://fhir.yas.nhs.uk/Encounter/Identifier";

    private static String interOpenEncounterIdentifier = "https://fhir.interopen.org/Encounter/Identifier";
    private static String interOpenLocationIdentifier = "https://fhir.interopen.org/Location/Identifier";
    private static String interOpenEpisodeOfCareIdentifier = "https://fhir.interopen.org/EpisodeOfCare/Identifier";

    private static String interOpenProcedureIdentifier = "https://fhir.interopen.org/Procedure/Identifier";
    private static String interOpenPractitionerIdentifier = "https://fhir.interopen.org/Practitioner/Identifier";
    private static String interOpenMedicationRequestIdentifier = "https://fhir.interopen.org/MedicationRequest/Identifier";
    private static String interOpenMedicationDispenseIdentifier = "https://fhir.interopen.org/MedicationDispense/Identifier";
    private static String interOpenMedicationAdministrationIdentifier = "https://fhir.interopen.org/MedicationAdministration/Identifier";
    private static String interOpenDosageUnitsNOS = "https://fhir.interopen.org/Medication/DosageUnitsNOS";

    private static String yasEpisodeIdentifier = "https://fhir.yas.nhs.uk/EpisodeOfCare/Identifier";

    private static String yasLocationIdentifier = "https://fhir.yas.nhs.uk/Location/Identifier";

    private static String yasConditionIdentifier = "https://fhir.yas.nhs.uk/Condition/Identifier";

    private static String yasObservationIdentifier = "https://fhir.yas.nhs.uk/Observation/Identifier";

    private static String midYorksFlagIdentifier = "https://fhir.midyorks.nhs.uk/Flag/Identifier";

    private static String midYorksCarePlanIdentifier = "https://fhir.midyorks.nhs.uk/CarePlan/Identifier";

    private static String midYorksQuestionnaireResponseIdentifier = "https://fhir.midyorks.nhs.uk/QuestionnaireResponse/Identifier";

    private static String midYorksQuestionnaireIdentifier = "https://fhir.midyorks.nhs.uk/Questionnaire/Identifier";

    private static String midYorksConditionIdentifier = "https://fhir.midyorks.nhs.uk/Condition/Identifier";

    private static String yasDocumentIdentifier = "https://fhir.yas.nhs.uk/DocumentReference/Identifier";

    private static String westRidingCareTeamIdentifier = "https://fhir.westriding.nhs.uk/CareTeam/Identifier";
    private static String westRidingClinicalImpressionIdentifier = "https://fhir.westriding.nhs.uk/ClinicalImpressionf/Identifier";


    final String uuidtag = "urn:uuid:";

    Organization yas;

    Organization lth;
    Organization midyorks;
    Organization rkh;
    Organization hdft;

    Location jimmy;
    Location pinderfields;

    FhirContext ctxFHIR = FhirContext.forDstu3();

    Integer idno = 650;
    Integer locno = 730;
    Integer conno = 12730;
    Integer obsNo = 500;


    public static void main(String[] args) {
        SpringApplication.run(uk.nhs.careconnect.interopen.examples.INTEROPenExamplesApp.class, args);
    }

    IGenericClient client = null;
    IGenericClient clientGPC = null;
    IGenericClient clientODS = null;
    IGenericClient clientNRLS = null;

    FhirBundleUtil fhirBundle;


    public static final String SNOMEDCT = "http://snomed.info/sct";


    DateFormat df = new SimpleDateFormat("HHmm_dd_MM_yyyy");

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && args[0].equals("exitcode")) {
            throw new Exception();
        }

        //client = ctxFHIR.newRestfulGenericClient("https://data.developer.nhs.uk/ccri-fhir/STU3/");
        client = ctxFHIR.newRestfulGenericClient("http://127.0.0.1:8182/ccri-messaging/STU3/");
       //client = ctxFHIR.newRestfulGenericClient("https://data.developer-test.nhs.uk/ccri-fhir/STU3/");
        client.setEncoding(EncodingEnum.XML);

       // clientGPC = ctxFHIR.newRestfulGenericClient("https://data.developer-test.nhs.uk/ccri/camel/fhir/gpc/");
        clientGPC = ctxFHIR.newRestfulGenericClient("https://data.developer-test.nhs.uk/ccri-fhir/STU3/");
       // clientGPC = ctxFHIR.newRestfulGenericClient("http://127.0.0.1:8187/ccri/camel/fhir/gpc/");
       // clientGPC.setEncoding(EncodingEnum.XML);

        clientNRLS = ctxFHIR.newRestfulGenericClient("https://data.developer.nhs.uk/nrls-ri/");
        SSPInterceptor sspInterceptor = new SSPInterceptor();
        clientNRLS.registerInterceptor(sspInterceptor);
        //clientNRLS.setEncoding(EncodingEnum.XML);

        clientODS = ctxFHIR.newRestfulGenericClient("https://directory.spineservices.nhs.uk/STU3/");
        clientODS.setEncoding(EncodingEnum.XML);

        Boolean loadDocuments = false;


        if (!loadDocuments) {
            loadFolder("patient");
        } else {
            loadFolder("patientPlusDocs");
        }


        idno = 661;
        locno = 730;
        conno = 12734;
        obsNo = 533;
        postPatient(loadDocuments,"9658220142", "LS25 2HF", Encounter.EncounterLocationStatus.PLANNED, "Elbe", "LS26 8PU", 0, -15, "410429000", "Cardiac arrest", true);


        loadFolder("rad");

        if (loadDocuments) {
           // loadFolder("dch");
           // loadFolder("toc");
           // loadFolder("pharm");
            updateNRLS();
        }

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
        Bundle bundle = null;
        if (FilenameUtils.getExtension(filename).equals("json")) {
            bundle = (Bundle) ctxFHIR.newJsonParser().parseResource(reader);
        } else {
            bundle = (Bundle) ctxFHIR.newXmlParser().parseResource(reader);
        }
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
    }





    public void saveBundle(String fileName, String subfolder, Bundle bundle) throws Exception {
        File directory = new File(String.valueOf("/output/"+subfolder));

        if(!directory.exists()) {
            directory.mkdir();
        }
        FileOutputStream outputStream = new FileOutputStream(("/output/"+subfolder+"/"+fileName));
        byte[] strToBytes = ctxFHIR.newXmlParser().setPrettyPrint(true).encodeResourceToString(bundle).getBytes();
        outputStream.write(strToBytes);

        outputStream.close();
    }






    public void updateNRLS() {
        Bundle bundle =  client
                .search()
                .forResource(DocumentReference.class)
                .where(DocumentReference.TYPE.exactly().codes ("736253002", "736373009"))

                .returnBundle(Bundle.class)
                .execute();
        if (bundle.getEntry().size()>0) {
            if (bundle.getEntry().get(0).getResource() instanceof DocumentReference) {


                DocumentReference documentReference = (DocumentReference) bundle.getEntry().get(0).getResource();
                System.out.println(documentReference.getId());
                System.out.println(documentReference.getSubject().getReference());
               // String patientID =

                Patient patient = client.read().resource(Patient.class).withId(new IdType(documentReference.getSubject().getReference())).execute();

                if (patient != null) {
                    for (Identifier identifier : patient.getIdentifier()) {
                        if (identifier.getSystem().equals("https://fhir.nhs.uk/Id/nhs-number")) {
                            System.out.println(identifier.getValue());
                            documentReference.setId("");
                            documentReference.setSubject(new Reference("https://demographics.spineservices.nhs.uk/STU3/Patient/"+identifier.getValue()));
                            documentReference.setIndexed(documentReference.getCreated());
                            documentReference.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);
                            List<Reference> author = new ArrayList<>();
                            author.add(new Reference("https://directory.spineservices.nhs.uk/STU3/Organization/MHT01"));
                            documentReference.setAuthor(author);
                            documentReference.setCustodian(new Reference("https://directory.spineservices.nhs.uk/STU3/Organization/MHT01"));

                            documentReference.getType().getCodingFirstRep().setCode("736253002").setDisplay("Mental health crisis plan");

                            System.out.println(ctxFHIR.newJsonParser().setPrettyPrint(true).encodeResourceToString(documentReference));


                            Bundle nrls = clientNRLS.search().forResource(DocumentReference.class)
                                    .where(DocumentReference.SUBJECT.hasId(documentReference.getSubject().getReference()))
                                    .returnBundle(Bundle.class)
                                    .execute();
                            if (!nrls.hasEntry() || nrls.getEntry().size() == 0) {
                                clientNRLS.create().resource(documentReference).execute();
                            }
                        }


                    }
                }
            }
        }
    }
    public Location getCoords(Location location, String postCode) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.postcodes.io/postcodes/"+postCode;
        //System.out.println(url);
        PostCode postCodeLongLat = restTemplate.getForObject(url, PostCode.class);
        //System.out.println(postCodeLongLat.getResult().getLongitude().toString());
        location.getPosition().setLatitude(postCodeLongLat.getResult().getLatitude());
        location.getPosition().setLongitude(postCodeLongLat.getResult().getLongitude());
        return location;
    }


    public Bundle loadEOLC(Bundle bundle) {
        Flag flag = new Flag();
        flag.setId(fhirBundle.getNewId(flag));
        flag.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        flag.addIdentifier().setSystem(midYorksFlagIdentifier).setValue("unusmy8");
        flag.setStatus(Flag.FlagStatus.ACTIVE);
        flag.getCode().addCoding()
                .setCode("450476008")
                .setSystem("http://snomed.info/sct")
                .setDisplay("Not for attempted cardiopulmonary resuscitation");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            flag.getPeriod().setStart(sdf.parse("2018-08-01"));
        } catch (Exception ex) {}

        flag.setAuthor(new Reference(uuidtag + midyorks.getIdElement().getIdPart()));
        bundle.addEntry().setResource(flag);

        flag = new Flag();
        flag.setId(fhirBundle.getNewId(flag));
        flag.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        flag.addIdentifier().setSystem(midYorksFlagIdentifier).setValue("unusmy9");
        flag.setStatus(Flag.FlagStatus.ACTIVE);
        flag.getCode().addCoding()
                .setCode("526631000000108")
                .setSystem("http://snomed.info/sct")
                .setDisplay("On end of life care register (finding)");
        try {
            flag.getPeriod().setStart(sdf.parse("2018-08-01"));
        } catch (Exception ex) {}

        flag.setAuthor(new Reference(uuidtag + midyorks.getIdElement().getIdPart()));
        bundle.addEntry().setResource(flag);

        Practitioner pal = new Practitioner();
        pal.setId(fhirBundle.getNewId(pal));
        pal.addIdentifier().setSystem(interOpenPractitionerIdentifier).setValue("x2900");
        pal.addName().setFamily("Simpson").addGiven("Mike");
        pal.addTelecom().setSystem(ContactPoint.ContactPointSystem.PHONE).setValue("07855 442038").setUse(ContactPoint.ContactPointUse.MOBILE);
        bundle.addEntry().setResource(pal);

        Practitioner consultant = new Practitioner();
        consultant.setId(fhirBundle.getNewId(consultant));
        consultant.addIdentifier().setSystem("https://fhir.nhs.uk/Id/sds-user-id").setValue("C4012900");
        consultant.addName().setFamily("Rodger").addGiven("KF");
        bundle.addEntry().setResource(consultant);

        Condition condition = new Condition();
        condition.setId(fhirBundle.getNewId(condition));
        condition.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        condition.addIdentifier().setSystem(midYorksConditionIdentifier).setValue("crm1");
        condition.setClinicalStatus(Condition.ConditionClinicalStatus.ACTIVE);
        condition.setAsserter(new Reference(uuidtag + consultant.getId()));
        condition.getCode().addCoding()
                .setDisplay("Dyspnea")
                .setCode("267036007")
                .setSystem("http://snomed.info/sct");
        try {
            condition.setOnset(new DateTimeType().setValue(sdf.parse("2018-08-01")));
        } catch (Exception ex) {}

        bundle.addEntry().setResource(condition);

        Questionnaire questionnaireCPR = new Questionnaire();
        questionnaireCPR.setId(fhirBundle.getNewId(questionnaireCPR));
        questionnaireCPR.addIdentifier().setSystem(midYorksQuestionnaireIdentifier).setValue("sr1");
        questionnaireCPR.setName("EOL CPR Status");
        questionnaireCPR.setTitle("EOL CPR Status");
        questionnaireCPR.setStatus(Enumerations.PublicationStatus.DRAFT);
        bundle.addEntry().setResource(questionnaireCPR);

        Questionnaire questionnaireLPA = new Questionnaire();
        questionnaireLPA.setId(fhirBundle.getNewId(questionnaireLPA));
        questionnaireLPA.addIdentifier().setSystem(midYorksQuestionnaireIdentifier).setValue("sr3");
        questionnaireLPA.setName("EOL LPA");
        questionnaireLPA.setTitle("EOL LPA");
        questionnaireLPA.setStatus(Enumerations.PublicationStatus.DRAFT);
        bundle.addEntry().setResource(questionnaireLPA);

        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setId(fhirBundle.getNewId(questionnaire));
        questionnaire.addIdentifier().setSystem(midYorksQuestionnaireIdentifier).setValue("sr2");
        questionnaire.setName("EOL Preferences");
        questionnaire.setTitle("EOL Preferences");
        questionnaire.setStatus(Enumerations.PublicationStatus.DRAFT);
        bundle.addEntry().setResource(questionnaire);

        QuestionnaireResponse formCPR = new QuestionnaireResponse();
        formCPR.setId(fhirBundle.getNewId(formCPR));
        formCPR.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        formCPR.getIdentifier().setSystem(midYorksQuestionnaireResponseIdentifier).setValue("rjm2");
        formCPR.setAuthor(new Reference(uuidtag + consultant.getId()));
        formCPR.setQuestionnaire(new Reference(uuidtag + questionnaireCPR.getId()));
        formCPR.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        try {
            formCPR.setAuthored(sdf.parse("2018-08-01"));
        } catch (Exception ex) {}
        formCPR.addItem()
                .setLinkId("reasonForCPRStatus")
                .setText("Reason for CPR status")
                .addAnswer()
                .setValue(new StringType("At home with family"));
        formCPR.addItem()
                .setLinkId("professionalsInvolvedInDecision")
                .setText("Professionals Involved In Decision")
                .addAnswer()
                .setValue(new Reference(uuidtag + consultant.getId()));
        formCPR.addItem()
                .setLinkId("professionalEndorsingStatus")
                .setText("Professional Endorsing Status")
                .addAnswer()
                .setValue(new Reference(uuidtag + consultant.getId()));


        bundle.addEntry().setResource(formCPR);

        QuestionnaireResponse formLPA = new QuestionnaireResponse();
        formLPA.setId(fhirBundle.getNewId(formLPA));
        formLPA.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        formLPA.getIdentifier().setSystem(midYorksQuestionnaireResponseIdentifier).setValue("rjm3");
        formLPA.setAuthor(new Reference(uuidtag + consultant.getId()));
        formLPA.setQuestionnaire(new Reference(uuidtag + questionnaireLPA.getId()));
        formLPA.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        try {
            formLPA.setAuthored(sdf.parse("2018-08-01"));
        } catch (Exception ex) {}
        formLPA.addItem()
                .setLinkId("documentName")
                .setText("Document Name")
                .addAnswer()
                .setValue(new StringType("Lasting power of attorny offical document"));
        formLPA.addItem()
                .setLinkId("documentLocation")
                .setText("Document Location")
                .addAnswer()
                .setValue(new StringType("Top left drawer in cabinet located in dining room. Documents are inside blue folder."));
        formLPA.addItem()
                .setLinkId("documentSource")
                .setText("Document Source")
                .addAnswer()
                .setValue(new StringType("Document drawn up at A B Solictors, Newcastle"));


        bundle.addEntry().setResource(formLPA);

        QuestionnaireResponse form = new QuestionnaireResponse();
        form.setId(fhirBundle.getNewId(form));
        form.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        form.getIdentifier().setSystem(midYorksQuestionnaireResponseIdentifier).setValue("rjm1");
        form.setAuthor(new Reference(uuidtag + consultant.getId()));
        form.setQuestionnaire(new Reference(uuidtag + questionnaire.getId()));
        form.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        try {
            form.setAuthored(sdf.parse("2018-08-01"));
        } catch (Exception ex) {}
        form.addItem()
                .setLinkId("preferredPlaceOfDeathText")
                .setText("Preferred Place Of Death Text")
                .addAnswer()
                .setValue(new StringType("At home with family"));
        form.addItem()
                .setLinkId("preferencesAndWishes")
                .setText("Preferences and Wishes")
                .addAnswer()
                .setValue(new StringType("To be made comfortable and looking out onto garden"));
        form.addItem()
                .setLinkId("domesticAccessAndInformation")
                .setText("Domestic Access and Information")
                .addAnswer()
                .setValue(new StringType("A key safe is provided to allow access to the property. Carer and related contact has code."));
        bundle.addEntry().setResource(form);

        ClinicalImpression prognosis = new ClinicalImpression();
        prognosis.setId(fhirBundle.getNewId(prognosis));
        prognosis.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        prognosis.setStatus(ClinicalImpression.ClinicalImpressionStatus.COMPLETED);
        prognosis.addIdentifier().setSystem(westRidingClinicalImpressionIdentifier).setValue("akm1");
        prognosis.setAssessor(new Reference(uuidtag + consultant.getId()));
        try {
            prognosis.setDate(sdf.parse("2018-08-20"));
        } catch (Exception ex) {}

        prognosis.addPrognosisCodeableConcept().addCoding()
                .setSystem(SNOMEDCT)
                .setCode("845701000000104")
                .setDisplay("Gold standards framework prognostic indicator stage A (blue) - year plus prognosis (finding)");
        prognosis.setDescription("Limited life expectancy of approximately one year");
        bundle.addEntry().setResource(prognosis);


        CareTeam careTeamH = new CareTeam();
        careTeamH.setId(fhirBundle.getNewId(careTeamH));
        careTeamH.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        careTeamH.addIdentifier().setSystem(westRidingCareTeamIdentifier).setValue("blm2");
        careTeamH.addParticipant().setMember(new Reference(uuidtag + pal.getId()));
        careTeamH.setName("Paliative Care Milworthy Healthcare Trust");
        careTeamH.addNote().setText("Lead case consultant");

        bundle.addEntry().setResource(careTeamH);

        CareTeam careTeam = new CareTeam();
        careTeam.setId(fhirBundle.getNewId(careTeam));
        careTeam.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        careTeam.addIdentifier().setSystem(westRidingCareTeamIdentifier).setValue("blm1");
        careTeam.addParticipant().setMember(new Reference(uuidtag + consultant.getId()));
        careTeam.setName("Milworthy Emergency Support Team");
        careTeam.addNote().setText("24 hour emergency support team");

        bundle.addEntry().setResource(careTeam);




        CarePlan carePlan = new CarePlan();
        carePlan.setId(fhirBundle.getNewId(carePlan));
        carePlan.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        carePlan.addIdentifier().setSystem(midYorksCarePlanIdentifier).setValue("blm1");
        // Not required carePlan.addAddresses(new Reference(uuidtag + condition.getId()));
        carePlan.addAuthor(new Reference(uuidtag + consultant.getId()));
        carePlan.addCareTeam(new Reference(uuidtag + careTeam.getId()));
        carePlan.addCareTeam(new Reference(uuidtag + careTeamH.getId()));

        carePlan.addCategory().addCoding()
                .setCode("736373009")
                .setSystem("http://snomed.info/sct")
                .setDisplay("End of life care plan");
        carePlan.setStatus(CarePlan.CarePlanStatus.ACTIVE);
        carePlan.setIntent(CarePlan.CarePlanIntent.PLAN);
        try {
            carePlan.getPeriod().setStart(sdf.parse("2018-08-01"));
        } catch (Exception ex) {}
        carePlan.addSupportingInfo(new Reference(uuidtag + form.getId()));
        carePlan.addSupportingInfo(new Reference(uuidtag + formCPR.getId()));
        carePlan.addSupportingInfo(new Reference(uuidtag + formLPA.getId()));
        carePlan.addSupportingInfo(new Reference(uuidtag + prognosis.getId()));
        carePlan.addActivity()
                .getDetail().setStatus(CarePlan.CarePlanActivityStatus.NOTSTARTED).setDescription("Nebulizer can be used to make patient more comfortable")
                .getCode().addCoding().setCode("445141005").setSystem("http://snomed.info/sct").setDisplay("Nebuliser therapy using mask");
        carePlan.addActivity()
                .getDetail().setStatus(CarePlan.CarePlanActivityStatus.NOTSTARTED).setDescription("Wants to avoid hospital admission if possible, but would want to consider options as need arises.")
                .getCode().addCoding().setCode("735324008").setSystem("http://snomed.info/sct").setDisplay("Treatment escalation plan");
        carePlan.addActivity()
                .getDetail().setStatus(CarePlan.CarePlanActivityStatus.NOTSTARTED).setDescription("[18 Sept 2018] This plan is known to the Harrogate Palliative Care Team. If advice needed Monday-Friday 0830-1700 contact the team on 01423 553464. Outside these hours contact Saint Michael's Hospice (Harrogate) on 01423 872 658. [21 Nov 2018] At risk of hypercalcaemia (Corr Ca+ 3.01 on 20th Nov) - symptoms were increased confusion and drowsiness.")
                .getCode().addCoding().setCode("702779007").setSystem("http://snomed.info/sct").setDisplay("Emergency health care plan agreed");
        bundle.addEntry().setResource(carePlan);

        Observation observation = new Observation();
        observation.setId(fhirBundle.getNewId(observation));
        observation.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        observation.addIdentifier().setSystem("urn:ietf:rfc:3986").setValue("38e8e6ed-eb91-4af8-afa4-ff8fdb7be93c");
        try {
            observation.setEffective(new DateTimeType(sdf.parse("2018-08-01")));
        } catch (Exception ex) {}
        observation.addPerformer(new Reference(uuidtag + consultant.getId()));
        observation.setStatus(Observation.ObservationStatus.FINAL);
        observation.getCode().addCoding()
                .setSystem("http://snomed.info/sct")
                .setCode("761869008")
                .setDisplay("Karnofsky Performance Status score (observable entity)");

        observation.setValue(   new Quantity()
                .setValue(
                        new BigDecimal(90))
                .setUnit("score")
                .setSystem("http://unitsofmeasure.org")
                .setCode("score"));
        bundle.addEntry().setResource(observation);

        // 'disability'
        condition = new Condition();
        condition.setId(fhirBundle.getNewId(condition));
        condition.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        condition.addIdentifier().setSystem(midYorksConditionIdentifier).setValue("akm2");
        condition.setClinicalStatus(Condition.ConditionClinicalStatus.ACTIVE);
        condition.setAsserter(new Reference(uuidtag + consultant.getId()));
        condition.getCode().addCoding()
                .setDisplay("Asthma (disorder)")
                .setCode("195967001")
                .setSystem("http://snomed.info/sct");
        try {
            condition.setOnset(new DateTimeType().setValue(sdf.parse("2002-03-11")));
        } catch (Exception ex) {}

        bundle.addEntry().setResource(condition);


        return bundle;
    }


    public void doSetUp() {

        yas = getOrganization("RX8");
        yas.setId(fhirBundle.getNewId(yas));

        lth = getOrganization("RR8");
        lth.setId(fhirBundle.getNewId(lth));

        hdft = getOrganization("RCD");
        hdft.setId(fhirBundle.getNewId(hdft));

        midyorks = getOrganization("RXF");
        midyorks.setId(fhirBundle.getNewId(midyorks));



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


        pinderfields = new Location();
        pinderfields.setId(fhirBundle.getNewId(pinderfields));
        pinderfields.setStatus(Location.LocationStatus.ACTIVE);
        pinderfields.setName("Pinderfields: Emergency Department");
        pinderfields.setDescription("Pinderfields: Emergency Department");
        pinderfields.getType().addCoding()
                .setSystem("http://hl7.org/fhir/v3/RoleCode")
                .setCode("ETU")
                .setDisplay("Emergency Trauma Unit");
        pinderfields.addTelecom()
                .setSystem(ContactPoint.ContactPointSystem.PHONE)
                .setValue("airwave-87351940")
                .setUse(ContactPoint.ContactPointUse.MOBILE);
        pinderfields.addIdentifier().setSystem(yasLocationIdentifier).setValue("RXF-EDP");
        pinderfields.getPhysicalType().addCoding()
                .setSystem("http://hl7.org/fhir/location-physical-type")
                .setCode("bu")
                .setDisplay("Building");
        pinderfields = getCoords(pinderfields,"WF1 4DG");
        pinderfields.setManagingOrganization(new Reference(uuidtag + midyorks.getIdElement().getIdPart()));


    }



    private Observation createObservationCoded(String valueCode, String valueDescription, String display, String code, Encounter encounter) {
        Observation observation = createObservation(null, null, display, code, encounter);

        if (valueCode != null) {
            CodeableConcept concept = new CodeableConcept();
            concept.addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode(valueCode)
                    .setDisplay(valueDescription);

            observation.setValue(concept);
        }

        return observation;
    }

    private Observation createObservationBP(String sys, String dia, String display, String code, Encounter encounter) {
        Observation observation = createObservation(null, null, display, code, encounter);

        observation.addCategory().addCoding()
                .setSystem("http://hl7.org/fhir/observation-category")
                .setCode("vital-signs")
                .setDisplay("Vital Signs");

        Observation.ObservationComponentComponent sysComp = observation.addComponent();
        sysComp.getCode().addCoding()
                .setCode("72313002")
                .setDisplay("Systolic arterial pressure")
                .setSystem("http://snomed.info/sct");
        sysComp.setValue(
                new Quantity()
                        .setValue(new BigDecimal(sys))
                        .setUnit("mm[Hg]")
                        .setSystem("http://unitsofmeasure.org")
                        .setCode("mm[Hg]"));

        Observation.ObservationComponentComponent diaComp = observation.addComponent();
        diaComp.getCode().addCoding()
                .setCode("1091811000000102")
                .setDisplay("Diastolic arterial pressure")
                .setSystem("http://snomed.info/sct");
        diaComp.setValue(
                new Quantity()
                        .setValue(new BigDecimal(dia))
                        .setUnit("mm[Hg]")
                        .setSystem("http://unitsofmeasure.org")
                        .setCode("mm[Hg]"));

        return observation;
    }

    private Observation createObservation(String value, String valueUnits, String display, String code, Encounter encounter) {
        Observation observation = new Observation();

        observation.setId(fhirBundle.getNewId(observation));
        //observation.setMeta(new Meta().addProfile(CareConnectProfile.Observation_1));


        observation.addIdentifier()
                .setSystem(yasObservationIdentifier)
                .setValue(obsNo.toString());
        obsNo++;

        observation.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));

        observation.setStatus(Observation.ObservationStatus.FINAL);

        observation.setContext(new Reference((uuidtag+encounter.getId())));
        observation.getCode().addCoding()
                .setDisplay(display)
                .setSystem("http://snomed.info/sct")
                .setCode(code);

        // Not converted unit and code correctly.
        if (value != null ) {
            if (!code.equals("1104051000000101")) {
                observation.addCategory().addCoding()
                        .setSystem("http://hl7.org/fhir/observation-category")
                        .setCode("vital-signs")
                        .setDisplay("Vital Signs");
            } else {
                observation.addCategory().addCoding()
                        .setSystem("http://hl7.org/fhir/observation-category")
                        .setCode("survey")
                        .setDisplay("Survey");
            }
        }
        try {
            Calendar cal = Calendar.getInstance();

            observation.setEffective(new DateTimeType(cal.getTime()));

        } catch (Exception e1) {
            // TODO Auto-generated catch block
        }

        if (value != null) {
            observation.setValue(
                    new Quantity()
                            .setValue(new BigDecimal(value))
                            .setUnit(valueUnits)
                            .setSystem("http://unitsofmeasure.org")
                            .setCode(valueUnits));

        }

        return observation;
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

    private Bundle getUnstructuredDocumentBundle(String nhsNumber) {
        Bundle bundle = null;
        try {
            switch (nhsNumber) {
                case "9658218873":
                    getUnstructuredDocumentBundle(nhsNumber, 1);
                    break;
                case "9658218997":
                    getUnstructuredDocumentBundle(nhsNumber, 2);

                    break;
                case "9658220142":
                    getUnstructuredDocumentBundle(nhsNumber, 3);

                    break;
                case "9658220223":
                    getUnstructuredDocumentBundle(nhsNumber, 4);

                    break;
                case "9658220169":
                    getUnstructuredDocumentBundle(nhsNumber, 5);

                    break;
            }
        } catch (Exception ex) {

        }
        return bundle;
    }




    private Bundle getUnstructuredDocumentBundle(String patientId, Integer docExample) throws Exception {
        // Create Bundle of type Document



        Bundle bundle = new Bundle();
        // Main resource of a FHIR Bundle is a DocumentReference
        DocumentReference documentReference = new DocumentReference();
        documentReference.setId(fhirBundle.getNewId(documentReference));
        bundle.addEntry().setResource(documentReference);

        documentReference.addIdentifier()
                .setValue(docExample.toString())
                .setSystem(yasDocumentIdentifier);

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
                    .setDisplay("End Of Life Care Plan");

            documentReference.getContext().getPracticeSetting().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("103735009")
                    .setDisplay("Palliative care");

            documentReference.getContext().getFacilityType().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("Palliative medicine service")
                    .setDisplay("Dermatology service");

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
                    .setCode("736253002")
                    .setDisplay("Mental health Crisis plan ");

            documentReference.getContext().getPracticeSetting().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("722162001")
                    .setDisplay("Psychology");

            documentReference.getContext().getFacilityType().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("708168004")
                    .setDisplay("Mental health service");



            InputStream inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("pdf/SLaM.pdf");
            binary.setContent(IOUtils.toByteArray(inputStream));
            binary.setContentType("application/pdf");
        } else if (docExample == 6) {
            documentReference.getType().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("736253002")
                    .setDisplay("Mental health Crisis plan ");

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
        Bundle bundle =  clientODS
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



    private MedicationDispense getDispense(Patient patient, Medication medication, Encounter encounter, MedicationRequest request, Practitioner practitioner, String id, String date ) {

        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        MedicationDispense dispense = new MedicationDispense();
        dispense.setId(fhirBundle.getNewId(dispense));
        dispense.addIdentifier().setSystem(interOpenMedicationDispenseIdentifier).setValue(id);
        dispense.setSubject(new Reference(uuidtag + patient.getId()));
        dispense.setMedication(new Reference(uuidtag + medication.getId()));
        dispense.setStatus(MedicationDispense.MedicationDispenseStatus.COMPLETED);
        dispense.setContext(new Reference(uuidtag + encounter.getId()));
        try {
            dispense.setWhenHandedOver(sdt.parse(date)); } catch (Exception ex) {}
        dispense.addAuthorizingPrescription(new Reference(uuidtag + request.getId()));
        dispense.addPerformer().setActor(new Reference(uuidtag + practitioner.getId()));
        return dispense;
    }


    private MedicationAdministration getAdministration(Patient patient, Medication medication, Encounter encounter, MedicationRequest request, Practitioner practitioner, String id, String date ) {

        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        MedicationAdministration administration = new MedicationAdministration();
        administration.setId(fhirBundle.getNewId(administration));
        administration.addIdentifier().setSystem(interOpenMedicationAdministrationIdentifier).setValue(id);
        administration.setSubject(new Reference(uuidtag + patient.getId()));
        administration.setMedication(new Reference(uuidtag + medication.getId()));
        administration.setStatus(MedicationAdministration.MedicationAdministrationStatus.COMPLETED);
        administration.setContext(new Reference(uuidtag + encounter.getId()));
        try {
            administration.setEffective(new DateTimeType(sdt.parse(date))); } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        administration.setPrescription(new Reference(uuidtag + request.getId()));
        administration.addPerformer().setActor(new Reference(uuidtag + practitioner.getId()));

        MedicationAdministration.MedicationAdministrationDosageComponent dosageComponent= administration.getDosage();

        dosageComponent.getRoute().addCoding()
                .setSystem(SNOMEDCT)
                .setCode("34206005")
                .setDisplay("Subcutaneous route");
        dosageComponent.setText("As directed");
      /*  dosageComponent.addAdditionalInstruction()
                .addCoding()
                .setSystem(SNOMEDCT)
                .setDisplay("Follow instructions")
                .setCode("421769005"); */
        // dosageComponent.setPatientInstruction("With evening meal");

        SimpleQuantity dose = new SimpleQuantity();
        dose.setUnit("pen").setValue(1).setCode("pen").setSystem(interOpenDosageUnitsNOS);
        dosageComponent.setDose(dose);

        return administration;
    }

    private MedicationRequest getMedicationRequest(Patient patient, Medication medication, Encounter encounter, Practitioner practitioner,String id, String date ) {

        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        MedicationRequest request = new MedicationRequest();
        request.setId(fhirBundle.getNewId(request));
        request.setSubject(new Reference(uuidtag + patient.getId()));
        request.addIdentifier().setSystem(interOpenMedicationRequestIdentifier).setValue(id);
        request.setMedication(new Reference(uuidtag + medication.getId()));
        request.setContext(new Reference(uuidtag + encounter.getId()));
        request.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);
        request.setStatus(MedicationRequest.MedicationRequestStatus.COMPLETED);
        try {
            request.setAuthoredOn(sdt.parse(date)); } catch (Exception ex) {}
        request.getRequester().getAgent().setReference(uuidtag + practitioner.getId());



        return request;
    }

    private Encounter getEncounter(Patient patient, EpisodeOfCare episode, String encounterId, Encounter.EncounterStatus status, Organization provider
    , String classCode, String classDesc, String encStart, String encEnd,
                                   String typeCode, String typeDesc) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Encounter encounter = new Encounter();

        encounter.addIdentifier()
                .setSystem(interOpenEncounterIdentifier)
                .setValue(encounterId);

        encounter.setId(fhirBundle.getNewId(encounter));
        encounter.setSubject(new Reference(uuidtag + patient.getId()));
        encounter.setStatus(status);
        if (episode != null) {
            encounter.addEpisodeOfCare().setReference(uuidtag + episode.getId());
        }
        encounter.setServiceProvider(new Reference(uuidtag + provider.getId()));
        encounter.getClass_()
                .setCode(classCode)
                .setSystem("http://hl7.org/fhir/v3/ActCode")
                .setDisplay(classDesc);
        try {
            encounter.getPeriod().setStart(sdt.parse(encStart));
        } catch (Exception ex) {
            try {
                encounter.getPeriod().setStart(sdf.parse(encStart));
            } catch (Exception ex2) {

            }
        }
        if (encEnd != null) {
            try {
                encounter.getPeriod().setEnd(sdt.parse(encEnd));
            } catch (Exception ex) {
                try {
                    encounter.getPeriod().setEnd(sdf.parse(encEnd));
                } catch (Exception ex1) {}
            }
        }
        CodeableConcept type = new CodeableConcept();
        type.addCoding().setSystem(SNOMEDCT).setDisplay(typeDesc).setCode(typeCode);
        encounter.getType().add(type);

        return encounter;
    }

    public void postOneEDITESTPATIENT() {
        String nhsNumber="9999999468";
        System.out.println("Posting Patient NHS Number "+nhsNumber);

        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = Calendar.getInstance();

        Date oneHourBack = cal.getTime();
        fhirBundle = new FhirBundleUtil(Bundle.BundleType.COLLECTION);

        doSetUp();


        Patient patient = new Patient();
        patient.setId(fhirBundle.getNewId(patient));
        patient.addIdentifier()
                .setSystem("https://fhir.nhs.uk/Id/nhs-number")
                .setValue(nhsNumber);
        patient.addName().setFamily("EDITESTPATIENT").addPrefix("Mr").addGiven("One").addGiven("John");
        try {
            patient.setBirthDate(sd.parse("1936-04-29"));
        } catch (Exception ex) {
            // Do nothing
        }


        Bundle bundle = new Bundle();

        bundle.addEntry().setResource(patient);

        bundle.addEntry().setResource(hdft);

        Practitioner kath = new Practitioner();
        kath.setId(fhirBundle.getNewId(kath));
        kath.addIdentifier().setValue("4516806").setSystem("https://fhir.nhs.uk/Id/sds-user-id");
        kath.addName().addPrefix("Dr.").addGiven("Katherine").setFamily("Lambert");

        bundle.addEntry().setResource(kath);

        Flag flag = new Flag();
        flag.setId(fhirBundle.getNewId(flag));
        flag.setSubject(new Reference(uuidtag + patient.getId()));
        flag.addIdentifier().setSystem(midYorksFlagIdentifier).setValue("one1");
        flag.setStatus(Flag.FlagStatus.ACTIVE);
        flag.getCode().addCoding()
                .setCode("450476008")
                .setSystem("http://snomed.info/sct")
                .setDisplay("Not for attempted cardiopulmonary resuscitation");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            flag.getPeriod().setStart(sdf.parse("2019-01-29"));
        } catch (Exception ex) {}

        flag.setAuthor(new Reference(uuidtag + kath.getIdElement().getIdPart()));
        bundle.addEntry().setResource(flag);

        flag = new Flag();
        flag.setId(fhirBundle.getNewId(flag));
        flag.setSubject(new Reference(uuidtag + patient.getId()));
        flag.addIdentifier().setSystem(midYorksFlagIdentifier).setValue("two2");
        flag.setStatus(Flag.FlagStatus.ACTIVE);
        flag.getCode().addCoding()
                .setCode("526631000000108")
                .setSystem("http://snomed.info/sct")
                .setDisplay("On end of life care register (finding)");
        try {
            flag.getPeriod().setStart(sdf.parse("2019-01-29"));
        } catch (Exception ex) {}

        flag.setAuthor(new Reference(uuidtag + kath.getIdElement().getIdPart()));
        bundle.addEntry().setResource(flag);

        // Ignore persons involved with discussion.

        CareTeam careTeamH = new CareTeam();
        careTeamH.setId(fhirBundle.getNewId(careTeamH));
        careTeamH.setSubject(new Reference(uuidtag + patient.getId()));
        careTeamH.addIdentifier().setSystem(westRidingCareTeamIdentifier).setValue("hdftct");
        careTeamH.addParticipant().setMember(new Reference(uuidtag + kath.getId()));
        careTeamH.setName("HDFT – Palliative Care Team");

        bundle.addEntry().setResource(careTeamH);

        QuestionnaireResponse form = new QuestionnaireResponse();
        form.setId(fhirBundle.getNewId(form));
        form.setSubject(new Reference(uuidtag + patient.getId()));
        form.getIdentifier().setSystem(midYorksQuestionnaireResponseIdentifier).setValue("hdrjm1");
        form.setAuthor(new Reference(uuidtag + kath.getId()));
       // form.setQuestionnaire(new Reference(uuidtag + questionnaire.getId()));
        form.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        try {
            form.setAuthored(sdf.parse("2019-01-12"));
        } catch (Exception ex) {}
        form.addItem()
                .setLinkId("preferredPlaceOfDeathText")
                .setText("Preferred Place Of Death Text")
                .addAnswer()
                .setValue(new CodeableConcept().addCoding().setCode("110481000000108").setSystem(SNOMEDCT).setDisplay("At home with family"));
        form.addItem()
                .setLinkId("preferencesAndWishes")
                .setText("Preferences and Wishes")
                .addAnswer()
                .setValue(new StringType("Family would like to support him to die at home if possible. Hospice admission would be alternative"));
        bundle.addEntry().setResource(form);



        CarePlan carePlan = new CarePlan();
        carePlan.setId(fhirBundle.getNewId(carePlan));
        carePlan.setSubject(new Reference(uuidtag + patient.getId()));
        carePlan.addIdentifier().setSystem(midYorksCarePlanIdentifier).setValue("hdftblm1");

        carePlan.addAuthor(new Reference(uuidtag + kath.getId()));
        carePlan.addCareTeam(new Reference(uuidtag + careTeamH.getId()));

        carePlan.addCategory().addCoding()
                .setCode("736373009")
                .setSystem("http://snomed.info/sct")
                .setDisplay("End of life care plan");
        carePlan.setStatus(CarePlan.CarePlanStatus.ACTIVE);
        carePlan.setIntent(CarePlan.CarePlanIntent.PLAN);
        try {
            carePlan.getPeriod().setStart(sdf.parse("2019-01-12"));
        } catch (Exception ex) {}
        carePlan.addSupportingInfo(new Reference(uuidtag + form.getId()));
       // carePlan.addSupportingInfo(new Reference(uuidtag + formCPR.getId()));
       // carePlan.addSupportingInfo(new Reference(uuidtag + formLPA.getId()));
       // carePlan.addSupportingInfo(new Reference(uuidtag + prognosis.getId()));
        carePlan.addActivity()
                .getDetail().setStatus(CarePlan.CarePlanActivityStatus.NOTSTARTED).setDescription("ACP decisions have been made without involving the patient as they lack mental capacity (in their best interests). Wife keen to avoid hospital admissions if possible as these can be distressing but if felt that this would make a significant difference to symptom management or comfort then would consider this.");
        carePlan.addActivity()
                .getDetail().setStatus(CarePlan.CarePlanActivityStatus.NOTSTARTED).setDescription("This patient is known to the Harrogate Palliative Care Team. If advice needed Monday-Friday 08.30-17.00 contact the team on 01423 553464. Outside these hours contact Saint Michael's Hospice (Harrogate) on 01423 872658. At risk of spinal cord compression due to metastatic disease in spine. Watch for changes in sensation or mobility. May need to consider high dose steroids and investigation (MRI). Wife has been provided with information leaflet around signs and symptoms of SCC.");

        bundle.addEntry().setResource(carePlan);


        fhirBundle.processBundleResources(bundle);


         try {
            saveBundle(nhsNumber + ".xml", "patient", fhirBundle.getFhirDocument());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        MethodOutcome outcome = client.create().resource(fhirBundle.getFhirDocument()).execute();

    }

    public void postPatient(Boolean loadDocuments, String nhsNumber, String encounterPostcode, Encounter.EncounterLocationStatus ambulanceStatus, String ambulanceName, String ambulancePostcode,
                            Integer hoursDiff, Integer minsDiff ,String code, String display , Boolean hospital) {


        System.out.println("Posting Patient NHS Number "+nhsNumber);

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
        bundle.addEntry().setResource(midyorks);


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
        bundle.addEntry().setResource(this.pinderfields);

        Condition condition = new Condition();
        condition.setId(fhirBundle.getNewId(condition));
        condition.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        condition.setClinicalStatus(Condition.ConditionClinicalStatus.ACTIVE);
        if (ambulanceStatus == null) {
            condition.setVerificationStatus(Condition.ConditionVerificationStatus.PROVISIONAL); }
        else {
            condition.setVerificationStatus(Condition.ConditionVerificationStatus.CONFIRMED);
        }
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
        if (ambulanceStatus != null) {
            encounter.setStatus(Encounter.EncounterStatus.INPROGRESS);
        } else {
            encounter.setStatus(Encounter.EncounterStatus.TRIAGED);
        }
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
        if (ambulanceStatus != null ) {
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

        triage.setPartOf(new Reference(uuidtag + encounter.getId()));
        triage.getPeriod().setStart(cal.getTime());
        if (ambulanceStatus != null) {
            cal.add(Calendar.MINUTE,5);
            triage.getPeriod().setEnd(cal.getTime());
            triage.addLocation().setLocation(new Reference(uuidtag + patientLoc.getId()))
                    .setStatus(Encounter.EncounterLocationStatus.COMPLETED);
        } else {
            triage.addLocation().setLocation(new Reference(uuidtag + patientLoc.getId()))
                    .setStatus(Encounter.EncounterLocationStatus.ACTIVE);
        }
        idno++;
        bundle.addEntry().setResource(triage);


        // EOLC
        if (nhsNumber == "9658220142") {
            loadEOLC(bundle);
        }




        if (ambulanceStatus != null) {

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
            if (!hospital) {
                ambulance.addLocation()
                        .setLocation(new Reference(uuidtag + patientLoc.getId()))
                        .setStatus(ambulanceStatus);
            } else {
                ambulance.addLocation()
                        .setLocation(new Reference(uuidtag + patientLoc.getId()))
                        .setStatus(Encounter.EncounterLocationStatus.COMPLETED);
            }
            ambulance.addLocation()
                    .setLocation(new Reference(uuidtag + ambulanceVech.getId()))
                    .setStatus(Encounter.EncounterLocationStatus.ACTIVE);
            if (hospital) {
                if (!ambulanceName.equals("Elbe")) {
                    ambulance.addLocation()
                            .setLocation(new Reference(uuidtag + jimmy.getId()))
                            .setStatus(ambulanceStatus);
                } else {

                    ambulance.addLocation()
                            .setLocation(new Reference(uuidtag + pinderfields.getId()))
                            .setStatus(ambulanceStatus);

                }
            }

            cal.add(Calendar.MINUTE,5);
            ambulance.getPeriod().setStart(cal.getTime());

            idno++;
            bundle.addEntry().setResource(ambulance);


/*
Respiration rate 86290005 '/min'
Sys 72313002 mmHg Dia 271650006 mmHg
Pulse 364075005 /min
Alertness
ACPVU scale Level 2:
    Alert : 248234008|Mentally alert (finding)|
    Voice:300202002|Responds to voice (finding)|
    Pain:450847001|Responds to pain (finding)|
    Unresponsive:3 :422768004|Unresponsive (finding)|
    New confusion :130987000|Acute confusion (finding)| : value set
Temperature 276885007 'Cel'
Inspired Oxygen
    722742002 Breathing room air
    371825009 patient on oxygen
*/


            if (nhsNumber=="9658220223") {
                Observation news = createObservation("2", "score", "Royal College of Physicians NEWS2 (National Early Warning Score 2) total score","1104051000000101", ambulance);

                Observation obs = createObservation("14", "/min",  "Respiratory rate","86290005",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                obs = createObservation("70", "/min",  "Heart rate","364075005",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                obs = createObservation("96", "%",  "Blood oxygen saturation","103228002",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                obs = createObservation("36.5", "Cel",  "Core body temperature","276885007",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                obs = createObservationBP("120", "80",  "Blood pressure","75367002",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                // obs = createObservationCoded("722742002", "Breathing room air",  "Observation of breathing","301282008",ambulance);
                // bundle.addEntry().setResource(obs);

                obs = createObservationCoded(null, null,  "Breathing room air","722742002",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                obs = createObservationCoded("248234008", "Mentally alert",  "ACVPU (Alert Confusion Voice Pain Unresponsive) scale score","1104441000000107",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);
                // Conscious

                bundle.addEntry().setResource(news);

            }



            if (nhsNumber=="9658218997") {

                Observation news = createObservation("6", "score", "Royal College of Physicians NEWS2 (National Early Warning Score 2) total score","1104051000000101", ambulance);

                Observation obs = createObservation("21", "/min",  "Respiratory rate","86290005",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                obs = createObservation("95", "/min",  "Heart rate","364075005",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                obs = createObservation("93", "%",  "Blood oxygen saturation","103228002",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                obs = createObservation("38.5", "Cel",  "Core body temperature","276885007",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                obs = createObservationBP("132", "78",  "Blood pressure","75367002",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                // obs = createObservationCoded("722742002", "Breathing room air",  "Observation of breathing","301282008",ambulance);
                // bundle.addEntry().setResource(obs);

                obs = createObservationCoded(null, null,  "Patient on oxygen","371825009",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                obs = createObservationCoded("248234008", "Mentally alert",  "ACVPU (Alert Confusion Voice Pain Unresponsive) scale score","1104441000000107",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);
                // Conscious

                bundle.addEntry().setResource(news);

            }

            if (nhsNumber=="9658220142") {
//CARDIAC
                Observation news = createObservation("8", "score", "Royal College of Physicians NEWS2 (National Early Warning Score 2) total score","1104051000000101", ambulance);

                Observation obs = createObservation("15", "/min",  "Respiratory rate","86290005",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                obs = createObservation("50", "/min",  "Heart rate","364075005",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                obs = createObservation("80", "%",  "Blood oxygen saturation","103228002",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                obs = createObservation("34.5", "Cel",  "Core body temperature","276885007",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                obs = createObservationBP("90", "50",  "Blood pressure","75367002",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                // obs = createObservationCoded("722742002", "Breathing room air",  "Observation of breathing","301282008",ambulance);
                // bundle.addEntry().setResource(obs);

                obs = createObservationCoded(null, null,  "Patient on oxygen","371825009",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                obs = createObservationCoded("130987000", "Acute confusion",   "ACVPU (Alert Confusion Voice Pain Unresponsive) scale score","1104441000000107", ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);
                // Conscious

                bundle.addEntry().setResource(news);

            }
            if (nhsNumber=="9658218873") {

                Observation news = createObservation("6", "score", "Royal College of Physicians NEWS2 (National Early Warning Score 2) total score","1104051000000101", ambulance);

                Observation obs = createObservation("22", "/min",  "Respiratory rate","86290005",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                obs = createObservation("93", "/min",  "Heart rate","364075005",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                obs = createObservation("98", "%",  "Blood oxygen saturation","103228002",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                obs = createObservation("37.0", "Cel",  "Core body temperature","276885007",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                obs = createObservationBP("140", "80",  "Blood pressure","75367002",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                // obs = createObservationCoded("722742002", "Breathing room air",  "Observation of breathing","301282008",ambulance);
                // bundle.addEntry().setResource(obs);

                obs = createObservationCoded(null, null,  "Patient on oxygen","371825009",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

                obs = createObservationCoded("248234008", "Mentally alert",  "ACVPU (Alert Confusion Voice Pain Unresponsive) scale score","1104441000000107",ambulance);
                bundle.addEntry().setResource(obs);
                news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);
                // Conscious

                bundle.addEntry().setResource(news);

            }
        }


        if (loadDocuments) {
            getUnstructuredDocumentBundle(nhsNumber);
        }



        // System.out.println(ctxFHIR.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle));

        fhirBundle.processBundleResources(bundle);


        //System.out.println(ctxFHIR.newJsonParser().setPrettyPrint(true).encodeResourceToString(fhirBundle.getFhirDocument()));
        try {
            saveBundle(nhsNumber + ".xml", "patient", fhirBundle.getFhirDocument());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        MethodOutcome outcome = client.create().resource(fhirBundle.getFhirDocument()).execute();

        // System.out.println(outcome.getId().toString());
        // System.out.println(ctxFHIR.newJsonParser().setPrettyPrint(true).encodeResourceToString(outcome.getOperationOutcome()));

    }


/// MICHEAL

    /*
    private void getMichael() throws Exception {

        fhirBundle = new FhirBundleUtil(Bundle.BundleType.COLLECTION);

        rkh = getOrganization("RVV");
        rkh.setId(fhirBundle.getNewId(rkh));



        doSetUp();

        Bundle bundle = new Bundle();


        Patient patient = new Patient();

        patient.setId(fhirBundle.getNewId(patient));

        CodeableConcept verification = new CodeableConcept();
        verification.addCoding()
                .setSystem("https://fhir.hl7.org.uk/STU3/CodeSystem/CareConnect-NHSNumberVerificationStatus-1")
                .setDisplay("Number present and verified")
                .setCode("01");
        patient.addIdentifier()
                .setValue("9658218881")
                .setSystem("https://fhir.nhs.uk/Id/nhs-number")
                .addExtension().setUrl("https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-NHSNumberVerificationStatus-1")
                .setValue(verification);

        patient.addName()
                .setFamily("Meakin")
                .addGiven("Micheal")
                .addPrefix("Mr")
                .setUse(HumanName.NameUse.USUAL);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            patient.setBirthDate(sdf.parse("1960-08-01"));
        } catch (Exception ex) {}

        patient.setGender(Enumerations.AdministrativeGender.MALE);

        patient.getMaritalStatus().addCoding().setSystem("http://hl7.org/fhir/v3/MaritalStatus").setCode("S");

        patient.addAddress()
                .addLine("7 Trinity Way")
                .setCity("London")
                .setPostalCode("W3 7JF")
        .setUse(Address.AddressUse.HOME);

        patient.addTelecom().setUse(ContactPoint.ContactPointUse.HOME).setValue("0208 412 8867").setSystem(ContactPoint.ContactPointSystem.PHONE);
        patient.addTelecom().setUse(ContactPoint.ContactPointUse.MOBILE).setValue("07778 143 565").setSystem(ContactPoint.ContactPointSystem.PHONE);
        patient.addTelecom().setUse(ContactPoint.ContactPointUse.HOME).setValue("michael@interopen.org").setSystem(ContactPoint.ContactPointSystem.EMAIL);

        patient.setLanguage("English (en-GB)");



        patient.setManagingOrganization(new Reference(uuidtag + rkh.getId()));


        // Add GP

        bundle.addEntry().setResource(patient).setFullUrl(patient.getId());

        fhirBundle.processBundleResources(bundle);


        bundle.addEntry().setResource(rkh).setFullUrl(uuidtag + rkh.getId());


        Practitioner practitioner = new Practitioner();
        practitioner.setId(fhirBundle.getNewId(practitioner));
        practitioner.addIdentifier().setSystem(interOpenPractitionerIdentifier).setValue("rkh1");
        practitioner.addName().setFamily("Courday").addGiven("Elisabeth").addPrefix("Dr");
        bundle.addEntry().setResource(practitioner).setFullUrl(uuidtag+ practitioner);

        Practitioner pharm = new Practitioner();
        pharm.setId(fhirBundle.getNewId(pharm));
        pharm.addIdentifier().setSystem(interOpenPractitionerIdentifier).setValue("pharm1");
        pharm.addName().setFamily("Swift").addGiven("William").addPrefix("Dr");
        bundle.addEntry().setResource(pharm).setFullUrl(uuidtag+ pharm);


        Location ed = new Location();
        ed.setId(fhirBundle.getNewId(ed));
        ed.addIdentifier().setSystem(interOpenLocationIdentifier).setValue("ed1");
        ed.setName("William Harvey Emergency Dept");

        bundle.addEntry().setResource(ed).setFullUrl(uuidtag+ ed);


        Location ward = new Location();
        ward.setId(fhirBundle.getNewId(ward));
        ward.addIdentifier().setSystem(interOpenLocationIdentifier).setValue("ward1");
        ward.setName("Brisingamen Ward");

        bundle.addEntry().setResource(ward).setFullUrl(uuidtag+ ward);



        Practitioner erdoc = new Practitioner();
        erdoc.setId(fhirBundle.getNewId(erdoc));
        erdoc.addIdentifier().setSystem(interOpenPractitionerIdentifier).setValue("rkh2");
        erdoc.addName().setFamily("Ross").addGiven("Doug").addPrefix("Dr");
        bundle.addEntry().setResource(erdoc).setFullUrl(uuidtag+ erdoc);

        Practitioner nurse = new Practitioner();
        nurse.setId(fhirBundle.getNewId(nurse));
        nurse.addIdentifier().setSystem(interOpenPractitionerIdentifier).setValue("rkh3");
        nurse.addName().setFamily("Hathaway").addGiven("Carol").addPrefix("Ms");
        bundle.addEntry().setResource(nurse).setFullUrl(uuidtag+ nurse);


        // Encounter E8

        // Arrival at ED

        Encounter encounter = getEncounter(patient, null,"E8", Encounter.EncounterStatus.FINISHED,rkh, "EMER",
                "Emergency","2018-11-08 13:35:00", "2018-11-08 13:45:00" ,"4525004","Emergency department patient visit");
        encounter.addParticipant(new Encounter.EncounterParticipantComponent().setIndividual(new Reference(uuidtag + erdoc.getId())));
        encounter.addLocation().setLocation(new Reference(uuidtag + ed.getId()));
        Extension serviceType = encounter.addExtension();
        serviceType.setUrl("http://hl7.org/fhir/4.0/StructureDefinition/extension-Encounter.serviceType");
        CodeableConcept type = new CodeableConcept();
        type.addCoding()
                .setSystem("https://fhir.nhs.uk/STU3/CodeSystem/DCH-Specialty-1")
                .setCode("180")
                .setDisplay("ACCIDENT & EMERGENCY");
        serviceType.setValue(type);
        bundle.addEntry().setResource(encounter).setFullUrl(encounter.getId());

        Condition condition = new Condition();
        condition.addIdentifier()
                .setSystem(yasConditionIdentifier)
                .setValue("con3");

        condition.setId(fhirBundle.getNewId(condition));
        condition.setSubject(new Reference(uuidtag + patient.getId()));

        condition.setContext(new Reference(uuidtag + encounter.getId()));
        condition.setAsserter(new Reference(uuidtag + erdoc.getId()));
        condition.getCode().addCoding()
                .setCode("723926008")
                .setSystem(SNOMEDCT)
                .setDisplay("Perceptual " +
                        "disturbances and seizures " +
                        "co-occurrent and due to " +
                        "alcohol withdrawal");
        CodeableConcept category = new CodeableConcept();
        category.addCoding()
                .setSystem("https://fhir.hl7.org.uk/STU3/CodeSystem/CareConnect-ConditionCategory-1")
                .setDisplay("encounter-diagnosis")
                .setCode("Encounter diagnosis");

        condition.getCategory().add(
                category);
        try {
            condition.setAssertedDate(sdt.parse("2018-11-08 13:35"));
        } catch (Exception ex) {}

        CodeableConcept severity = new CodeableConcept();
        severity.addCoding()
                .setSystem(SNOMEDCT)
                .setDisplay("Moderate")
                .setCode("6736007");

        condition.setSeverity(severity);
        condition.setClinicalStatus(Condition.ConditionClinicalStatus.ACTIVE);
        // condition.addNote().setText("Mistaken as aggression");
        try {
            // condition.setOnset(new DateTimeType(sdf.parse("1978-01-13")));
            //  condition.setAssertedDate(sdf.parse("1978-01-13"));
        } catch (Exception ex) {}
        bundle.addEntry().setResource(condition).setFullUrl(condition.getId());


        DateTimeType dateTime = new DateTimeType();

        try {
            dateTime.setValue(sdt.parse("2018-11-08 13:35")); }
        catch(Exception ex) {
            System.out.print("Date Time Exception!!!!");
        }

// Micheal
            Observation news = createObservation("5", "score", "Royal College of Physicians NEWS2 (National Early Warning Score 2) total score","1104051000000101", encounter);
            news.setEffective(dateTime);

            Observation obs = createObservation("19", "/min",  "Respiratory rate","86290005",encounter);
            obs.setEffective(dateTime);
        obs.addPerformer(new Reference(uuidtag + erdoc.getId()));
            bundle.addEntry().setResource(obs);
            news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

            obs = createObservation("116", "/min",  "Heart rate","364075005",encounter);
        obs.setEffective(dateTime);
        obs.addPerformer(new Reference(uuidtag + erdoc.getId()));
            bundle.addEntry().setResource(obs);
            news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

            obs = createObservation("99", "%",  "Blood oxygen saturation","103228002",encounter);
        obs.setEffective (dateTime);
        obs.addPerformer(new Reference(uuidtag + erdoc.getId()));
            bundle.addEntry().setResource(obs);
            news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

            obs = createObservation("37.0", "Cel",  "Core body temperature","276885007",encounter);
        obs.setEffective(dateTime);
        obs.addPerformer(new Reference(uuidtag + erdoc.getId()));
            bundle.addEntry().setResource(obs);
            news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

            obs = createObservationBP("180", "120",  "Blood pressure","75367002",encounter);
        obs.setEffective(dateTime);
        obs.addPerformer(new Reference(uuidtag + erdoc.getId()));
            bundle.addEntry().setResource(obs);
            news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

            // obs = createObservationCoded("722742002", "Breathing room air",  "Observation of breathing","301282008",ambulance);
            // bundle.addEntry().setResource(obs);

            obs = createObservationCoded(null, null,  "Patient on oxygen","371825009",encounter);
        obs.setEffective(dateTime);
        obs.addPerformer(new Reference(uuidtag + erdoc.getId()));
            bundle.addEntry().setResource(obs);
            news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);

            obs = createObservationCoded("422768004", "Unresponsive",   "ACVPU (Alert Confusion Voice Pain Unresponsive) scale score","1104441000000107", encounter);
        obs.setEffective(dateTime);
        obs.addPerformer(new Reference(uuidtag + erdoc.getId()));
            bundle.addEntry().setResource(obs);
            news.addRelated().setTarget(new Reference(uuidtag + obs.getId())).setType(Observation.ObservationRelationshipType.DERIVEDFROM);
            // Conscious

            bundle.addEntry().setResource(news);


        EpisodeOfCare episode = new EpisodeOfCare();
        episode.addIdentifier()
                .setSystem(interOpenEpisodeOfCareIdentifier)
                .setValue("EP1");

        episode.setId(fhirBundle.getNewId(episode));
        episode.setPatient(new Reference(uuidtag + patient.getId()));
        episode.setManagingOrganization(new Reference(uuidtag + yas.getId()));
        episode.setStatus(EpisodeOfCare.EpisodeOfCareStatus.FINISHED);
        episode.addType().addCoding()
                .setSystem(SNOMEDCT)
                .setCode("394802001")
                .setDisplay("General medicine");
        try {
            episode.getPeriod().setStart(sdf.parse("2018-11-08"));
            episode.getPeriod().setEnd(sdf.parse("2018-11-18"));
        } catch (Exception ex) {}


        bundle.addEntry().setResource(episode).setFullUrl(episode.getId());

        // E9

        /// In patient admission

        encounter = getEncounter(patient, episode,"E9", Encounter.EncounterStatus.FINISHED,rkh, "IMP",
                "inpatient encounter","2018-11-08 16:45:00", "2018-11-08 17:15:00" ,"86181006","Evaluation and management of inpatient");
        encounter.addParticipant(new Encounter.EncounterParticipantComponent().setIndividual(new Reference(uuidtag + practitioner.getId())));
        encounter.addLocation().setLocation(new Reference(uuidtag + ward.getId()));
        serviceType = encounter.addExtension();
        serviceType.setUrl("http://hl7.org/fhir/4.0/StructureDefinition/extension-Encounter.serviceType");
        type = new CodeableConcept();
        type.addCoding()
                .setSystem("https://fhir.nhs.uk/STU3/CodeSystem/DCH-Specialty-1")
                .setCode("100")
                .setDisplay("GENERAL SURGERY");
        serviceType.setValue(type);
        bundle.addEntry().setResource(encounter).setFullUrl(encounter.getId());

        // E10



        //  Ward care to discharge


        encounter = getEncounter(patient, episode,"E10", Encounter.EncounterStatus.FINISHED,rkh, "IMP",
                "inpatient encounter","2018-11-09", "2018-11-18" ,"53923005","Medical consultation on inpatient");
        encounter.addParticipant(new Encounter.EncounterParticipantComponent().setIndividual(new Reference(uuidtag + practitioner.getId())));
        encounter.addLocation().setLocation(new Reference(uuidtag + ward.getId()));
        serviceType = encounter.addExtension();
        serviceType.setUrl("http://hl7.org/fhir/4.0/StructureDefinition/extension-Encounter.serviceType");
        type = new CodeableConcept();
        type.addCoding()
                .setSystem("https://fhir.nhs.uk/STU3/CodeSystem/DCH-Specialty-1")
                .setCode("100")
                .setDisplay("GENERAL SURGERY");
        serviceType.setValue(type);
        bundle.addEntry().setResource(encounter).setFullUrl(encounter.getId());

        // E11


        // treatment on ward


        encounter = getEncounter(patient,episode, "E11", Encounter.EncounterStatus.FINISHED,rkh, "IMP",
                "inpatient encounter","2018-11-16", "2018-11-18" ,"48550003","Medical consultation on nursing facility inpatient");
        encounter.addParticipant(new Encounter.EncounterParticipantComponent().setIndividual(new Reference(uuidtag + nurse.getId())));
        encounter.addLocation().setLocation(new Reference(uuidtag + ward.getId()));
        serviceType = encounter.addExtension();
        serviceType.setUrl("http://hl7.org/fhir/4.0/StructureDefinition/extension-Encounter.serviceType");
        type = new CodeableConcept();
        type.addCoding()
                .setSystem("https://fhir.nhs.uk/STU3/CodeSystem/DCH-Specialty-1")
                .setCode("100")
                .setDisplay("GENERAL SURGERY");
        serviceType.setValue(type);
        bundle.addEntry().setResource(encounter).setFullUrl(encounter.getId());

        Procedure procedure = new Procedure();
        procedure.setSubject(new Reference(uuidtag + patient.getId()));
        try {
            procedure.setPerformed(new Period().setStart(sdf.parse("2018-11-16")).setEnd(sdf.parse("2018-11-18")));
        } catch (Exception ex) {}
        procedure.setId(fhirBundle.getNewId(procedure));
        procedure.addIdentifier().setSystem(interOpenProcedureIdentifier).setValue("proc1");
        procedure.getCode().addCoding().setSystem(SNOMEDCT).setCode("386465007").setDisplay("Prescribed medication education");
        procedure.setContext(new Reference(uuidtag+encounter.getId()));
        procedure.setStatus(Procedure.ProcedureStatus.COMPLETED);
        procedure.addPerformer(new Procedure.ProcedurePerformerComponent().setActor(new Reference(uuidtag + nurse.getId())));
        bundle.addEntry().setResource(procedure).setFullUrl(procedure.getId());


        Medication medication = new Medication();
        medication.setId(fhirBundle.getNewId(medication));
        medication.getCode().addCoding()
                .setSystem(SNOMEDCT)
                .setCode("30268411000001107")
                .setDisplay("Apidra 100units/ml solution for injection 3ml pre-filled SoloStar pen (Waymade Healthcare Plc) (product)");
        bundle.addEntry().setResource(medication).setFullUrl(medication.getId());

        MedicationRequest request = getMedicationRequest(patient,medication,encounter,nurse,"mr1","2018-11-16 11:01" );
        request.addReasonCode()
                .addCoding()
                .setSystem(SNOMEDCT)
                .setCode("46635009")
                .setDisplay("Type 1 diabetes mellitus");
        Dosage dosage = request.addDosageInstruction();

        dosage.getRoute().addCoding()
                .setSystem(SNOMEDCT)
                .setCode("34206005")
                .setDisplay("Subcutaneous route");

        dosage.setText("As directed");
        dosage.addAdditionalInstruction()
                .addCoding()
                .setSystem(SNOMEDCT)
                .setDisplay("Follow Directions")
                .setCode("421769005");
        dosage.setPatientInstruction("With evening meal");

        SimpleQuantity dose = new SimpleQuantity();
        dose.setUnit("pens").setValue(1).setCode("pens").setSystem(interOpenDosageUnitsNOS);
        dosage.setDose(dose);

        dosage.getTiming().getRepeat().addWhen(Timing.EventTiming.CV);

        Duration duration = new Duration();
        duration.setValue(5).setUnit("d").setSystem("http://unitsofmeasure.org").setCode("d");
        request.getDispenseRequest().setExpectedSupplyDuration(duration);

        bundle.addEntry().setResource(request).setFullUrl(request.getId());


        MedicationDispense dispense = getDispense(patient,medication,encounter,request,nurse,"md1","2018-11-16 11:03");

        dosage = dispense.addDosageInstruction();

        dosage.getRoute().addCoding()
                .setSystem(SNOMEDCT)
                .setCode("34206005")
                .setDisplay("Subcutaneous route");
        dosage.setText("As directed");
        dosage.addAdditionalInstruction()
                .addCoding()
                .setSystem(SNOMEDCT)
                .setDisplay("Follow instructions")
                .setCode("421769005");
        dosage.setPatientInstruction("With evening meal");

        dose = new SimpleQuantity();
        dose.setUnit("pens").setValue(1).setCode("pens").setSystem(interOpenDosageUnitsNOS);
        dosage.setDose(dose);

        dose = new SimpleQuantity();
        dose.setUnit("pens").setValue(5).setCode("pens").setSystem(interOpenDosageUnitsNOS);
        dispense.setQuantity(dose);

        dosage.getTiming().getRepeat().addWhen(Timing.EventTiming.CV);

        bundle.addEntry().setResource(dispense).setFullUrl(dispense.getId());

        MedicationAdministration administration = getAdministration(patient,medication,encounter,request,nurse,"api1","2018-11-16 17:03");
        bundle.addEntry().setResource(administration).setFullUrl(administration.getId());

        administration = getAdministration(patient,medication,encounter,request,nurse,"api2","2018-11-17 18:33");
        bundle.addEntry().setResource(administration).setFullUrl(administration.getId());

        administration = getAdministration(patient,medication,encounter,request,nurse,"api3","2018-11-18 12:03");
        bundle.addEntry().setResource(administration).setFullUrl(administration.getId());

        // E12
        encounter = getEncounter(patient, episode,"E12", Encounter.EncounterStatus.FINISHED,rkh, "IMP",
                "inpatient encounter","2018-11-18 11:00", "2018-11-18 11:30" ,"83362003","Final inpatient visit with instructions at discharge");
        encounter.addLocation().setLocation(new Reference(uuidtag + ward.getId()));
        encounter.addParticipant(new Encounter.EncounterParticipantComponent().setIndividual(new Reference(uuidtag + practitioner.getId())));
        serviceType = encounter.addExtension();
        serviceType.setUrl("http://hl7.org/fhir/4.0/StructureDefinition/extension-Encounter.serviceType");
        type = new CodeableConcept();
        type.addCoding()
                .setSystem("https://fhir.nhs.uk/STU3/CodeSystem/DCH-Specialty-1")
                .setCode("100")
                .setDisplay("GENERAL SURGERY");
        serviceType.setValue(type);
        bundle.addEntry().setResource(encounter).setFullUrl(encounter.getId());


        Medication vitB = new Medication();
        vitB.setId(fhirBundle.getNewId(vitB));
        vitB.getCode().addCoding()
                .setSystem(SNOMEDCT)
                .setCode("103069003")
                .setDisplay("Vitamin B (substance)");
        bundle.addEntry().setResource(vitB).setFullUrl(vitB.getId());

        MedicationRequest requestVitB = getMedicationRequest(patient,vitB,encounter,practitioner,"mr2","2018-11-18 11:01" );
        requestVitB.addReasonCode()
                .addCoding()
                .setSystem(SNOMEDCT)
                .setCode("373453009")
                .setDisplay("Nutritional supplement");
        dosage = requestVitB.addDosageInstruction();

        dosage.getRoute().addCoding()
                .setSystem(SNOMEDCT)
                .setCode("738956005")
                .setDisplay("Oral");

        dosage.setText("As directed");
        dosage.addAdditionalInstruction()
                .addCoding()
                .setSystem(SNOMEDCT)
                .setDisplay("Follow Directions")
                .setCode("421769005");
        dosage.setPatientInstruction("Morning");

        dose = new SimpleQuantity();
        dose.setUnit("tablets").setValue(2).setCode("tbs").setSystem(interOpenDosageUnitsNOS);
        dosage.setDose(dose);

        dosage.getTiming().getRepeat().addWhen(Timing.EventTiming.MORN);

        duration = new Duration();
        duration.setValue(28).setUnit("d").setSystem("http://unitsofmeasure.org").setCode("d");
        requestVitB.getDispenseRequest().setExpectedSupplyDuration(duration);
        bundle.addEntry().setResource(requestVitB).setFullUrl(requestVitB.getId());

        dispense = getDispense(patient,vitB,encounter,requestVitB,pharm,"md4","2018-11-18 11:23");

        dosage = dispense.addDosageInstruction();

        dosage.getRoute().addCoding()
                .setSystem(SNOMEDCT)
                .setCode("738956005")
                .setDisplay("Oral");

        dosage.setText("As directed");
        dosage.addAdditionalInstruction()
                .addCoding()
                .setSystem(SNOMEDCT)
                .setDisplay("Follow Directions")
                .setCode("421769005");
        dosage.setPatientInstruction("Morning");

        dose = new SimpleQuantity();
        dose.setUnit("tablets").setValue(56).setCode("tbs").setSystem(interOpenDosageUnitsNOS);
        dispense.setQuantity(dose);

        dosage.getTiming().getRepeat().addWhen(Timing.EventTiming.CV);
        dose = new SimpleQuantity();
        dose.setUnit("tablets").setValue(2).setCode("tbs").setSystem(interOpenDosageUnitsNOS);
        dosage.setDose(dose);

        dosage.getTiming().getRepeat().addWhen(Timing.EventTiming.MORN);


        bundle.addEntry().setResource(dispense).setFullUrl(dispense.getId());


        Medication thiamine = new Medication();
        thiamine.setId(fhirBundle.getNewId(thiamine));
        thiamine.getCode().addCoding()
                .setSystem(SNOMEDCT)
                .setCode("259659006")
                .setDisplay("Thiamine (substance)");
        bundle.addEntry().setResource(thiamine).setFullUrl(thiamine.getId());

        MedicationRequest requestthiamine = getMedicationRequest(patient,thiamine,encounter,practitioner,"mr3","2018-11-18 11:01" );
        requestthiamine.addReasonCode()
                .addCoding()
                .setSystem(SNOMEDCT)
                .setCode("373453009")
                .setDisplay("Nutritional supplement");
        dosage = requestthiamine.addDosageInstruction();

        dosage.getRoute().addCoding()
                .setSystem(SNOMEDCT)
                .setCode("738956005")
                .setDisplay("Oral");

        dosage.setText("THREE TIMES A DAY");
        dosage.addAdditionalInstruction()
                .addCoding()
                .setSystem(SNOMEDCT)
                .setDisplay("Follow Directions")
                .setCode("421769005");
        dosage.setPatientInstruction("Morning");

        dose = new SimpleQuantity();
        dose.setUnit("tablets").setValue(1).setCode("tbs").setSystem(interOpenDosageUnitsNOS);
        dosage.setDose(dose);

        dosage.getTiming().getRepeat().addWhen(Timing.EventTiming.MORN);

        duration = new Duration();
        duration.setValue(28).setUnit("d").setSystem("http://unitsofmeasure.org").setCode("d");
        requestthiamine.getDispenseRequest().setExpectedSupplyDuration(duration);
        bundle.addEntry().setResource(requestthiamine).setFullUrl(requestthiamine.getId());

        dispense = getDispense(patient,thiamine,encounter,requestthiamine,pharm,"md5","2018-11-18 11:23");

        dosage = dispense.addDosageInstruction();

        dosage.getRoute().addCoding()
                .setSystem(SNOMEDCT)
                .setCode("738956005")
                .setDisplay("Oral");

        dosage.setText("As directed");
        dosage.addAdditionalInstruction()
                .addCoding()
                .setSystem(SNOMEDCT)
                .setDisplay("Follow Directions")
                .setCode("421769005");
        dosage.setPatientInstruction("Morning");

        dose = new SimpleQuantity();
        dose.setUnit("tablets").setValue(28).setCode("tbs").setSystem(interOpenDosageUnitsNOS);
        dispense.setQuantity(dose);

        dosage.getTiming().getRepeat().addWhen(Timing.EventTiming.CV);
        dose = new SimpleQuantity();
        dose.setUnit("tablets").setValue(1).setCode("tbs").setSystem(interOpenDosageUnitsNOS);
        dosage.setDose(dose);

        dosage.getTiming().getRepeat().addWhen(Timing.EventTiming.MORN);

        bundle.addEntry().setResource(dispense).setFullUrl(dispense.getId());

        Condition diabetes = new Condition();
        diabetes.addIdentifier()
                .setSystem(yasConditionIdentifier)
                .setValue("con1");

        diabetes.setId(fhirBundle.getNewId(diabetes));
        diabetes.setSubject(new Reference(uuidtag + patient.getId()));
        diabetes.getCode().addCoding()
                .setCode("46635009")
                .setSystem("http://snomed.info/sct")
                .setDisplay("Type 1 diabetes mellitus");
        category = new CodeableConcept();
        category.addCoding()
                .setSystem("https://fhir.hl7.org.uk/STU3/CodeSystem/CareConnect-ConditionCategory-1")
                .setDisplay("problem-list-item")
                .setCode("Problem list item");

        diabetes.getCategory().add(
                category);

        severity = new CodeableConcept();
        severity.addCoding()
                .setSystem("http://snomed.info/sct")
                .setDisplay("Moderate")
                .setCode("6736007");
        diabetes.setSeverity(severity);
        diabetes.addNote().setText("Taking insulin");
        diabetes.setClinicalStatus(Condition.ConditionClinicalStatus.ACTIVE);
        try {
            diabetes.setOnset(new DateTimeType(sdf.parse("1964-08-01")));
            diabetes.setAssertedDate(sdf.parse("1964-12-01"));
        } catch (Exception ex) {}

        bundle.addEntry().setResource(diabetes).setFullUrl(diabetes.getId());



        Condition anxiety = new Condition();
        anxiety.addIdentifier()
                .setSystem(yasConditionIdentifier)
                .setValue("con2");

        anxiety.setId(fhirBundle.getNewId(anxiety));
        anxiety.setSubject(new Reference(uuidtag + patient.getId()));
        anxiety.getCode().addCoding()
                .setCode("80583007")
                .setSystem("http://snomed.info/sct")
                .setDisplay("[Severe anxiety " +
                        "(panic)");
        category = new CodeableConcept();
        category.addCoding()
                .setSystem("https://fhir.hl7.org.uk/STU3/CodeSystem/CareConnect-ConditionCategory-1")
                .setDisplay("problem-list-item")
                .setCode("Problem list item");

        anxiety.getCategory().add(
                category);

        severity = new CodeableConcept();
        severity.addCoding()
                .setSystem("http://snomed.info/sct")
                .setDisplay("Severe")
                .setCode("24484000");

        anxiety.setSeverity(severity);
        anxiety.setClinicalStatus(Condition.ConditionClinicalStatus.ACTIVE);
        anxiety.addNote().setText("Mistaken as aggression");
        try {
            anxiety.setOnset(new DateTimeType(sdf.parse("1978-01-13")));
            anxiety.setAssertedDate(sdf.parse("1978-01-13"));
        } catch (Exception ex) {}


        bundle.addEntry().setResource(anxiety).setFullUrl(anxiety.getId());




        // con4

        condition = new Condition();
        condition.addIdentifier()
                .setSystem(yasConditionIdentifier)
                .setValue("con4");

        condition.setId(fhirBundle.getNewId(condition));
        condition.setSubject(new Reference(uuidtag + patient.getId()));
        condition.getCode().addCoding()
                .setCode("164881000119109")
                .setSystem("http://snomed.info/sct")
                .setDisplay("Foot " +
                        "ulcer due to type 1 diabetes " +
                        "mellitus");
        category = new CodeableConcept();
        category.addCoding()
                .setSystem("https://fhir.hl7.org.uk/STU3/CodeSystem/CareConnect-ConditionCategory-1")
                .setDisplay("problem-list-item")
                .setCode("Problem list item");


        condition.getCategory().add(
                category);

        severity = new CodeableConcept();
        severity.addCoding()
                .setSystem("http://snomed.info/sct")
                .setDisplay("Mild")
                .setCode("255604002");

        condition.setSeverity(severity);
        condition.setClinicalStatus(Condition.ConditionClinicalStatus.ACTIVE);
        // condition.addNote().setText("Mistaken as aggression");
        try {
            condition.setOnset(new DateTimeType(sdf.parse("2018-02-18")));
            condition.setAssertedDate(sdf.parse("2018-02-18"));
        } catch (Exception ex) {}
        bundle.addEntry().setResource(condition).setFullUrl(condition.getId());

        // con5

        // con6

        condition = new Condition();
        condition.addIdentifier()
                .setSystem(yasConditionIdentifier)
                .setValue("con6");

        condition.setId(fhirBundle.getNewId(condition));
        condition.setSubject(new Reference(uuidtag + patient.getId()));
        condition.getCode().addCoding()
                .setCode("41309000")
                .setSystem("http://snomed.info/sct")
                .setDisplay("Alcoholic liver damage");
        category = new CodeableConcept();
        category.addCoding()
                .setSystem("https://fhir.hl7.org.uk/STU3/CodeSystem/CareConnect-ConditionCategory-1")
                .setDisplay("encounter-diagnosis")
                .setCode("Encounter diagnosis");
        condition.getCategory().add(
                category);

        severity = new CodeableConcept();
        severity.addCoding()
                .setSystem("http://snomed.info/sct")
                .setDisplay("Mild")
                .setCode("255604002");

        condition.setSeverity(severity);
        condition.setClinicalStatus(Condition.ConditionClinicalStatus.ACTIVE);
        // condition.addNote().setText("Mistaken as aggression");
        try {
           // condition.setOnset(new DateTimeType(sdf.parse("2018-02-18")));
           // condition.setAssertedDate(sdf.parse("2018-02-18"));
        } catch (Exception ex) {}
        bundle.addEntry().setResource(condition).setFullUrl(condition.getId());

        fhirBundle.processBundleResources(bundle);

      //  System.out.println(ctxFHIR.newJsonParser().setPrettyPrint(true).encodeResourceToString(fhirBundle.getFhirDocument()));
        saveBundle("9658218881-micheal.xml","patient",fhirBundle.getFhirDocument());
//        MethodOutcome outcome = client.create().resource(fhirBundle.getFhirDocument()).execute();
    }
*/



}
