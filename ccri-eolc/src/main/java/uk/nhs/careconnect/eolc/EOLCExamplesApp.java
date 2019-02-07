package uk.nhs.careconnect.eolc;

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
public class EOLCExamplesApp implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(EOLCExamplesApp.class);

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
        SpringApplication.run(EOLCExamplesApp.class, args);
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


        clientODS = ctxFHIR.newRestfulGenericClient("https://directory.spineservices.nhs.uk/STU3/");
        clientODS.setEncoding(EncodingEnum.XML);

        Boolean loadDocuments = false;

        // Example Patient 1
        //  ONE JOHN EDITESTPATIENT  999 999 9468
        postOneEDITESTPATIENT();


        // Example Patient 2
        // TWO EDITESTPATIENT 999 999 9476

        // RAD contains base Resources referenced in the main getMicheal load.

    }

    public Questionnaire getEOLCQuestionnaire() {
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setId(fhirBundle.getNewId(questionnaire));
        questionnaire.addIdentifier().setSystem("https://fhir.nhs.uk/STU3/Questionnaire/").setValue("CareConnect-EOLC-1");
        questionnaire.setName("End of Life Care");
        questionnaire.setTitle("End of Life Care");
        questionnaire.setStatus(Enumerations.PublicationStatus.DRAFT);
        questionnaire.addSubjectType("Patient");
        questionnaire.setPurpose("EoL National Minimum Dataset (v2.2) WIP.xlsx");



        // consent


        Questionnaire.QuestionnaireItemComponent consent = questionnaire.addItem();
        consent.setLinkId("EOL-Consent-1");
        consent.setText("EOL Consent");
        consent.setType(Questionnaire.QuestionnaireItemType.GROUP);

        Questionnaire.QuestionnaireItemComponent item = consent.addItem()
                .setText("Consent")
                .setLinkId("EOL-Consent")
                .setDefinition("Consent [G3]")
                .setRepeats(false)
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/EOL-Consent-1"));
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Consent"));

        /// CPR


        Questionnaire.QuestionnaireItemComponent cpr = questionnaire.addItem();
        cpr.setLinkId("EOL-CPRStatus-1");
        cpr.setText("CPR Status");

        cpr.setType(Questionnaire.QuestionnaireItemType.GROUP);

        item = cpr.addItem()
                .setText("CPR Status")
                .setLinkId("CPRStatus")
                .setDefinition("CPR Status [G3]")
                .setRepeats(false)
                .setRequired(true)
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/EOL-CPRStatus-Flag-1"));
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Flag"));

        cpr.addItem()
                .setText("Reason for CPR status")
                .setDefinition("CPR Status [G4]")
                .setLinkId("reasonForCPRStatus")
                .setType(Questionnaire.QuestionnaireItemType.STRING);

        cpr.addItem()
                .setText("CPR Status Mental Capacity")
                .setLinkId("cPRStatusMentalCapacity")
                .setDefinition("CPR Status [G5]")
                .setType(Questionnaire.QuestionnaireItemType.STRING);


        item = cpr.addItem()
                .setText("Persons involved in discussion")
                .setDefinition("CPR Status [G8]")
                .setLinkId("personsInvolvedInDiscussion")
                .setRepeats(true)
                .setType(Questionnaire.QuestionnaireItemType.GROUP);

        item.addItem().setText("Non-professionals involved in CPR status discussion (Coded)")
                .setLinkId("personsInvolvedInDiscussionCoded")
                .setType(Questionnaire.QuestionnaireItemType.CHOICE)
                .setOptions(new Reference("Some valueSet1"));


        item = cpr.addItem()
                .setText("Persons or organisations made aware of the decision")
                .setDefinition("CPR Status [G11]")
                .setLinkId("awarenessOfDecision")
                .setRepeats(true)
                .setType(Questionnaire.QuestionnaireItemType.GROUP);

        item.addItem().setText("Non-professionals involved in CPR status discussion (Coded)")
                .setLinkId("awarenessOfDecision")
                .setType(Questionnaire.QuestionnaireItemType.CHOICE)
                .setOptions(new Reference("Some valueSet2"));


        item = cpr.addItem()
                .setText("Professionals Involved In Decision")
                .setLinkId("professionalsInvolvedInDecision")
                .setDefinition("CPR Status [G14]")
                .setRepeats(true)
                .setType(Questionnaire.QuestionnaireItemType.GROUP);

        Questionnaire.QuestionnaireItemComponent groupitem = item.addItem()
                .setText("Professionals Involved In Decision")
                .setLinkId("professionalsInvolvedInDecision")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        groupitem.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Practitioner-1"));
        groupitem.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Practitioner"));


        item = cpr.addItem()
                .setText("Professional Endorsing Status")
                .setLinkId("professionalEndorsingStatus")
                .setDefinition("CPR Status [G24]")
                .setRepeats(true)
                .setType(Questionnaire.QuestionnaireItemType.GROUP);

        groupitem = item.addItem()
                .setText("Professional Endorsing Status Reference")
                .setLinkId("professionalEndorsingStatusReference")
                .setRepeats(true)
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        groupitem.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Practitioner-1"));
        groupitem.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Practitioner"));


        // Advanced Treatment Preferences



        Questionnaire.QuestionnaireItemComponent advpref = questionnaire.addItem();
        advpref.setLinkId("EOL-Advanced-Treatment-Preferences-1")
                .setText("Advanced Treatment Preferences");
        advpref.setType(Questionnaire.QuestionnaireItemType.GROUP);

        Questionnaire.QuestionnaireItemComponent subgroup = advpref.addItem()
                .setLinkId("EOL-ATPProblemHeader-Condition-1")

                .setText("Clinical Problems and Advised Interventions")
                .setDefinition("ATP [G3]")
                .setRepeats(true)
                .setType(Questionnaire.QuestionnaireItemType.GROUP);

        item = subgroup.addItem()
                .setLinkId("EOL-ATPProblemHeader-Condition-1")
                .setText("Problem or Condition")
                .setDefinition("ATP [G4]")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/EOL-ATPProblemHeader-Condition-1"));
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Condition"));


        item = subgroup.addItem()
                .setLinkId("EOL-ATP-Intervention-1")
                .setText("ATP Intervention")
                .setDefinition("ATP [G5]")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/EOL-ATP-CarePlan-1"));
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("CarePlan"));


        subgroup = advpref.addItem()
                .setLinkId("EOL-ATP-Medicine-Issued")
                .setText("Anticipatory medicines/just in case box issued")
                .setDefinition("ATP [G7]")
                .setType(Questionnaire.QuestionnaireItemType.GROUP);

        item = subgroup.addItem()
                .setLinkId("EOL-ATP-Medicine-Box-1")
                .setText("ATP Intervention")
                .setDefinition("ATP [G8]")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-EOL-Procedure-1"));
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Procedure"));

        subgroup = advpref.addItem()
                .setLinkId("EOL-ADRT")
                .setText("Advance Decision to Refuse Treatment")
                .setDefinition("ATP [G12]")
                .setType(Questionnaire.QuestionnaireItemType.GROUP);

        item = subgroup.addItem()
                .setLinkId("EOL-ATP-ADRT-1")
                .setText("Advance Decision to Refuse Treatment - Coded")
                .setDefinition("ATP [G8]")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/EOL-ADRT-Flag-1"));
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Flag"));


        subgroup = advpref.addItem()
                .setLinkId("EOL-ATP-Respect")
                .setText("ReSPECT Care")
                .setDefinition("ATP [G16]")
                .setType(Questionnaire.QuestionnaireItemType.GROUP);

        item = subgroup.addItem()
                .setLinkId("EOL-ATP-Respect-Scale")
                .setText("ReSPECT Care Priority Scale")
                .setDefinition("ATP [G17]")
                .setRequired(true)
                .setType(Questionnaire.QuestionnaireItemType.CHOICE)
                .setOptions(new Reference("to be defined"));

        item = subgroup.addItem()
                .setLinkId("EOL-ATP-Respect-Priority")
                .setText("ReSPECT Care Priority Priority")
                .setDefinition("ATP [G18]")
                .setType(Questionnaire.QuestionnaireItemType.CHOICE)
                .setOptions(new Reference("to be defined"));


        // TODO ADD IN PROVENANCE

        // LPA


        Questionnaire.QuestionnaireItemComponent lpa = questionnaire.addItem();
        lpa.setLinkId("EOL-LPA-1");
        lpa.setText("Lasting Power of Attorney");
        lpa.setType(Questionnaire.QuestionnaireItemType.GROUP);

        item = lpa.addItem()
                .setLinkId("EOL-LPA-Flag-1")
                .setText("Lasting Power of Attorney For Health and Welfare")
                .setDefinition("LPA [G2]")
                .setRequired(true)
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/EOL-LPA-Flag-1"));
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Flag"));

        item = lpa.addItem()
                .setLinkId("EOL-LPA-RelatedPerson-1")
                .setText("Persons Appointed")
                .setDefinition("LPA [G4]")
                .setRepeats(true)
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/EOL-LPA-RelatedPerson-1"));
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("RelatedPerson"));

        // Prognosis


        Questionnaire.QuestionnaireItemComponent prognosis = questionnaire.addItem();
        prognosis.setLinkId("EOL-Prognosis-1");
        prognosis.setText("Prognosis");
        prognosis.setType(Questionnaire.QuestionnaireItemType.GROUP);

        item = prognosis.addItem()
                .setLinkId("EOL-Prognosis-ClinicalImpression")
                .setText("Prognosis")
                .setDefinition("Prognosis [G2]")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/EOL-Prognosis-ClinicalImpression-1"));
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("ClinicalImpression"));

        // FUNCTIONAL

        Questionnaire.QuestionnaireItemComponent func = questionnaire.addItem();
        func.setLinkId("EOL-FunctionalStatus-1").setText("Functional Status");
        func.setType(Questionnaire.QuestionnaireItemType.GROUP);

        item = func.addItem()
                .setLinkId("EOL-FunctionalStatus-Observation-1")
                .setText("Functional Status")
                .setRepeats(true)
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-EOL-FunctionalStatus-Observation-1"));
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Observation"));

        // DISABILITY


        Questionnaire.QuestionnaireItemComponent disability = questionnaire.addItem();
        disability.setLinkId("EOL-Disabilitiess-1")
                .setText("Disabilities");
        disability.setType(Questionnaire.QuestionnaireItemType.GROUP);

        item = disability.addItem()
                .setLinkId("EOL-Disabilities-Condition-1")
                .setText("Disability / Condition List")
                .setRepeats(true)
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-ProblemHeader-Condition-1"));
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Condition"));


        /// EOL Preferences PREF

        Questionnaire.QuestionnaireItemComponent pref = questionnaire.addItem();
        pref.setLinkId("EOL-Preferences-1");
        pref.setText("Preferences");
        pref.setType(Questionnaire.QuestionnaireItemType.GROUP);

        subgroup = pref.addItem()
                .setText("Preferred Place Of Death")
                .setLinkId("preferredPlaceOfDeathCoded")
                .setDefinition("Preferences [G4]")
                .setRequired(true)
                .setType(Questionnaire.QuestionnaireItemType.GROUP);


        subgroup.addItem()
                .setText("Preferred Place Of Death (Coded)")
                .setLinkId("preferredPlaceOfDeathCoded")
                .setType(Questionnaire.QuestionnaireItemType.CHOICE)
                .setOptions(new Reference("https://fhir.nhs.uk/STU3/ValueSet/EOL-PreferredPlaceDeath-Code-1"));

        subgroup.addItem()
                .setText("Preferred Place Of Death (Text)")
                .setLinkId("preferredPlaceOfDeathText")
                .setRequired(true)
                .setType(Questionnaire.QuestionnaireItemType.STRING);

        pref.addItem()
                .setText("Preferences and Wishes")
                .setLinkId("preferencesAndWishes")
                .setType(Questionnaire.QuestionnaireItemType.STRING);

        pref.addItem()
                .setText("Domestic Access and Information")
                .setLinkId("domesticAccessAndInformation")
                .setType(Questionnaire.QuestionnaireItemType.STRING);

        pref.addItem()
                .setText("Domestic Access and Information")
                .setLinkId("domesticAccessAndInformation")
                .setType(Questionnaire.QuestionnaireItemType.STRING);

        item = pref.addItem()
                .setLinkId("EOL-Preferences-author")
                .setText("Preferences Author")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-Practitioner-1"));
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Practitioner"));

        item = pref.addItem()
                .setLinkId("EOL-Preferences-authoredDate")
                .setText("Preferences Date Recorded")
                .setType(Questionnaire.QuestionnaireItemType.DATETIME);
        // OTHER


        Questionnaire.QuestionnaireItemComponent other = questionnaire.addItem();
        other.setLinkId("EOL-OtherDocuments-1")
                .setText("Other Documents")
                .setRepeats(true)
                .setType(Questionnaire.QuestionnaireItemType.GROUP);

        other.addItem()
                .setLinkId("documentName")
                .setText("Document Name")
                .setType(Questionnaire.QuestionnaireItemType.STRING)
                .setRequired(true);

        other.addItem()
                .setLinkId("documentLocation")
                .setText("Document Location")
                .setType(Questionnaire.QuestionnaireItemType.STRING);

        other.addItem()
                .setLinkId("documentSource")
                .setText("Document Source")
                .setType(Questionnaire.QuestionnaireItemType.STRING);




        return questionnaire;
    }


    private InputStream getResourceAsStream(String resource) {
        final InputStream in
                = getContextClassLoader().getResourceAsStream(resource);

        return in == null ? getClass().getResourceAsStream(resource) : in;
    }

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }


    public void saveBundle(String fileName, String subfolder, Bundle bundle) throws Exception {
        File directory = new File(String.valueOf("/output/" + subfolder));

        if (!directory.exists()) {
            directory.mkdir();
        }
        FileOutputStream outputStream = new FileOutputStream(("/output/" + subfolder + "/" + fileName));
        byte[] strToBytes = ctxFHIR.newXmlParser().setPrettyPrint(true).encodeResourceToString(bundle).getBytes();
        outputStream.write(strToBytes);

        outputStream.close();
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
        } catch (Exception ex) {
        }

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
        } catch (Exception ex) {
        }

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
        } catch (Exception ex) {
        }

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
        } catch (Exception ex) {
        }
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
        } catch (Exception ex) {
        }
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
        } catch (Exception ex) {
        }
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
        } catch (Exception ex) {
        }

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
        } catch (Exception ex) {
        }
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
        } catch (Exception ex) {
        }
        observation.addPerformer(new Reference(uuidtag + consultant.getId()));
        observation.setStatus(Observation.ObservationStatus.FINAL);
        observation.getCode().addCoding()
                .setSystem("http://snomed.info/sct")
                .setCode("761869008")
                .setDisplay("Karnofsky Performance Status score (observable entity)");

        observation.setValue(new Quantity()
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
        } catch (Exception ex) {
        }

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

        observation.setContext(new Reference((uuidtag + encounter.getId())));
        observation.getCode().addCoding()
                .setDisplay(display)
                .setSystem("http://snomed.info/sct")
                .setCode(code);

        // Not converted unit and code correctly.
        if (value != null) {
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
                .where(Patient.IDENTIFIER.exactly().systemAndCode("https://fhir.nhs.uk/Id/nhs-number", NHSNumber))
                //
                .returnBundle(Bundle.class)
                .execute();

        if (bundle.getEntry().size() > 0) {
            Patient patient = (Patient) bundle.getEntry().get(0).getResource();
            if (patient.hasManagingOrganization()) {

                Organization organization = callclient.read().resource(Organization.class).withId(patient.getManagingOrganization().getReference()).execute();
                organization.setId(fhirBundle.getNewId(organization));
                patient.setManagingOrganization(new Reference(uuidtag + organization.getId()));
                bundle.addEntry().setResource(organization);
            }
            if (patient.hasGeneralPractitioner()) {

                Practitioner practitioner = callclient.read().resource(Practitioner.class).withId(patient.getGeneralPractitioner().get(0).getReference()).execute();
                practitioner.setId(fhirBundle.getNewId(practitioner));
                patient.getGeneralPractitioner().get(0).setReference(uuidtag + practitioner.getId());
                bundle.addEntry().setResource(practitioner);
            }
        }
        return bundle;
    }


    private Practitioner getPractitioner(String sdsCode) {
        Practitioner practitioner = null;
        Bundle bundle = client
                .search()
                .forResource(Practitioner.class)
                .where(Practitioner.IDENTIFIER.exactly().code(sdsCode))
                .returnBundle(Bundle.class)
                .execute();
        if (bundle.getEntry().size() > 0) {
            if (bundle.getEntry().get(0).getResource() instanceof Practitioner)
                practitioner = (Practitioner) bundle.getEntry().get(0).getResource();

        }
        return practitioner;
    }

    private Organization getOrganization(String sdsCode) {


        Organization organization = null;
        Bundle bundle = clientODS
                .search()
                .forResource(Organization.class)
                .where(Organization.IDENTIFIER.exactly().code(sdsCode))

                .returnBundle(Bundle.class)
                .execute();
        if (bundle.getEntry().size() > 0) {
            if (bundle.getEntry().get(0).getResource() instanceof Organization)
                organization = (Organization) bundle.getEntry().get(0).getResource();

        }
        return organization;
    }


    private MedicationDispense getDispense(Patient patient, Medication medication, Encounter encounter, MedicationRequest request, Practitioner practitioner, String id, String date) {

        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        MedicationDispense dispense = new MedicationDispense();
        dispense.setId(fhirBundle.getNewId(dispense));
        dispense.addIdentifier().setSystem(interOpenMedicationDispenseIdentifier).setValue(id);
        dispense.setSubject(new Reference(uuidtag + patient.getId()));
        dispense.setMedication(new Reference(uuidtag + medication.getId()));
        dispense.setStatus(MedicationDispense.MedicationDispenseStatus.COMPLETED);
        dispense.setContext(new Reference(uuidtag + encounter.getId()));
        try {
            dispense.setWhenHandedOver(sdt.parse(date));
        } catch (Exception ex) {
        }
        dispense.addAuthorizingPrescription(new Reference(uuidtag + request.getId()));
        dispense.addPerformer().setActor(new Reference(uuidtag + practitioner.getId()));
        return dispense;
    }


    private MedicationAdministration getAdministration(Patient patient, Medication medication, Encounter encounter, MedicationRequest request, Practitioner practitioner, String id, String date) {

        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        MedicationAdministration administration = new MedicationAdministration();
        administration.setId(fhirBundle.getNewId(administration));
        administration.addIdentifier().setSystem(interOpenMedicationAdministrationIdentifier).setValue(id);
        administration.setSubject(new Reference(uuidtag + patient.getId()));
        administration.setMedication(new Reference(uuidtag + medication.getId()));
        administration.setStatus(MedicationAdministration.MedicationAdministrationStatus.COMPLETED);
        administration.setContext(new Reference(uuidtag + encounter.getId()));
        try {
            administration.setEffective(new DateTimeType(sdt.parse(date)));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        administration.setPrescription(new Reference(uuidtag + request.getId()));
        administration.addPerformer().setActor(new Reference(uuidtag + practitioner.getId()));

        MedicationAdministration.MedicationAdministrationDosageComponent dosageComponent = administration.getDosage();

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

    private MedicationRequest getMedicationRequest(Patient patient, Medication medication, Encounter encounter, Practitioner practitioner, String id, String date) {

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
            request.setAuthoredOn(sdt.parse(date));
        } catch (Exception ex) {
        }
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
                } catch (Exception ex1) {
                }
            }
        }
        CodeableConcept type = new CodeableConcept();
        type.addCoding().setSystem(SNOMEDCT).setDisplay(typeDesc).setCode(typeCode);
        encounter.getType().add(type);

        return encounter;
    }

    public void postOneEDITESTPATIENT() {
        String nhsNumber = "9999999468";
        System.out.println("Posting Patient NHS Number " + nhsNumber);

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
        } catch (Exception ex) {
        }

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
        } catch (Exception ex) {
        }

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
        } catch (Exception ex) {
        }
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
        } catch (Exception ex) {
        }
        carePlan.addSupportingInfo(new Reference(uuidtag + form.getId()));
        // carePlan.addSupportingInfo(new Reference(uuidtag + formCPR.getId()));
        // carePlan.addSupportingInfo(new Reference(uuidtag + formLPA.getId()));
        // carePlan.addSupportingInfo(new Reference(uuidtag + prognosis.getId()));
        carePlan.addActivity()
                .getDetail().setStatus(CarePlan.CarePlanActivityStatus.NOTSTARTED).setDescription("ACP decisions have been made without involving the patient as they lack mental capacity (in their best interests). Wife keen to avoid hospital admissions if possible as these can be distressing but if felt that this would make a significant difference to symptom management or comfort then would consider this.");
        carePlan.addActivity()
                .getDetail().setStatus(CarePlan.CarePlanActivityStatus.NOTSTARTED).setDescription("This patient is known to the Harrogate Palliative Care Team. If advice needed Monday-Friday 08.30-17.00 contact the team on 01423 553464. Outside these hours contact Saint Michael's Hospice (Harrogate) on 01423 872658. At risk of spinal cord compression due to metastatic disease in spine. Watch for changes in sensation or mobility. May need to consider high dose steroids and investigation (MRI). Wife has been provided with information leaflet around signs and symptoms of SCC.");

        bundle.addEntry().setResource(carePlan);

        Questionnaire questionnaire = getEOLCQuestionnaire();

        System.out.println(ctxFHIR.newXmlParser().setPrettyPrint(true).encodeResourceToString(questionnaire));
        bundle.addEntry().setResource(questionnaire);

        fhirBundle.processBundleResources(bundle);


        try {
            saveBundle(nhsNumber + ".xml", "patient", fhirBundle.getFhirDocument());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        MethodOutcome outcome = client.create().resource(fhirBundle.getFhirDocument()).execute();

    }


}
