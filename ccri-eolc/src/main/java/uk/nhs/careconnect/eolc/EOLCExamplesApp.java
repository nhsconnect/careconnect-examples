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

    private static String tppListIdentifier = "https://fhir.tpp.co.uk/List/Identifier";
    private static String tppConsentIdentifier = "https://fhir.tpp.co.uk/Consent/Identifier";
    private static String tppNOKIdentifier = "https://fhir.tpp.co.uk/NOK/Identifier";

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

       // KGM This is now fairly static. Use careconnect-cli to load the resource
       // postQuestionnaire();


        // Kewn example patient
        loadEOLC();

        // Example Patient 1
        //  ONE JOHN EDITESTPATIENT  999 999 9468
      //  postOneEDITESTPATIENT();


        // Example Patient 2
        // TWO EDITESTPATIENT 999 999 9476

        // RAD contains base Resources referenced in the main getMicheal load.

    }

    public Questionnaire getNEWS2Questionnaire() {
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setId(fhirBundle.getNewId(questionnaire));
        questionnaire.addIdentifier().setSystem("https://fhir.airelogic.com/STU3/Questionnaire").setValue("NEWS2");
        questionnaire.setUrl("https://fhir.airelogic.com/STU3/Questionnaire/NEWS2");
        questionnaire.setName("NEWS2");
        questionnaire.setTitle("NEWS2");
        questionnaire.setDescription("National Early Warning Score (NEWS)2");
        questionnaire.setStatus(Enumerations.PublicationStatus.DRAFT);
        questionnaire.addSubjectType("Patient");
        questionnaire.setPurpose("https://developer.nhs.uk/apis/news2-1.0.0-alpha.1/");

        Questionnaire.QuestionnaireItemComponent obs = questionnaire.addItem();
        obs.setText("BloodPressure")
                .setLinkId("NEWS2-1")
                .setDefinition("A Vital Signs profile to carry blood pressure information that contains at least one component for systolic and/or diastolic pressure.")

                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        addObsExtension(obs, "https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-BloodPressure-Observation-1");


        obs = questionnaire.addItem();
        obs.setText("Body Temperature")
                .setLinkId("NEWS2-2")
                .setDefinition("The body temperature reading used to generate the NEWS2 score. ")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        addObsExtension(obs, "https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-BodyTemperature-Observation-1");

        obs = questionnaire.addItem();
        obs.setText("ACVPU")
                .setLinkId("NEWS2-3")
                .setDefinition("This profile is used to carry alert, new-onset or worsening confusion, voice, pain, and unresponsiveness observations for a patient.")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        addObsExtension(obs, "https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-ACVPU-Observation-1");

        obs = questionnaire.addItem();
        obs.setText("Hear Rate")
                .setLinkId("NEWS2-4")
                .setDefinition("The pulse rate reading used to generate the NEWS2 score.")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        addObsExtension(obs, "https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-HeartRate-Observation-1");

        obs = questionnaire.addItem();
        obs.setText("Inspired Oxygen")
                .setLinkId("NEWS2-5")
                .setDefinition("The inspired oxygen observation used to generate the NEWS2 score.")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        addObsExtension(obs, "https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-InspiredOxygen-Observation-1");

        obs = questionnaire.addItem();
        obs.setText("Saturated Oxygen")
                .setLinkId("NEWS2-6")
                .setDefinition("The oxygen saturation reading used to generate the NEWS2 score. ")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        addObsExtension(obs, "https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-OxygenSaturation-Observation-1");

        obs = questionnaire.addItem();
        obs.setText("Respiratory Rate")
                .setLinkId("NEWS2-7")
                .setDefinition("The respiratory rate reading used to generate the NEWS2 score. ")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        addObsExtension(obs, "https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-RespiratoryRate-Observation-1");

               obs = questionnaire.addItem();
        obs.setText("Sub Score")
                .setLinkId("NEWS2-SCORE")
                .setDefinition("Used to hold the sub-scores of the observations that go to make up the overall NEWS2 score. ")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        addObsExtension(obs, "https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Subscore-Observation-1");

        obs = questionnaire.addItem();
        obs.setText("Encounter Information")
                .setLinkId("ENCOUNTER (PV1)")
                .setDefinition("Encounter information for the measurements")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        obs.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Encounter-1"));
        obs.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Encounter"));

        return questionnaire;
    }

    private void addObsExtension(Questionnaire.QuestionnaireItemComponent obs, String profile) {
            obs.addExtension()
                    .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                    .setValue(new Reference(profile));
            obs.addExtension()
                    .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                    .setValue(new CodeType().setValue("Observation"));

    }

    public Questionnaire getEOLCQuestionnaire() {
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setId(fhirBundle.getNewId(questionnaire));
        questionnaire.addIdentifier().setSystem("https://fhir.nhs.uk/STU3/Questionnaire").setValue("CareConnect-EOLC-1");
        questionnaire.setUrl("https://fhir.nhs.uk/STU3/Questionnaire/CareConnect-EOLC-1");
        questionnaire.setName("End of Life Care");
        questionnaire.setTitle("End of Life Care");
        questionnaire.setStatus(Enumerations.PublicationStatus.DRAFT);
        questionnaire.addSubjectType("Patient");
        questionnaire.setDescription("EoL National Minimum Dataset");
        questionnaire.setPurpose("EoL National Minimum Dataset (v2.3) WIP.xlsx");

    // EOL Register

        Questionnaire.QuestionnaireItemComponent register = questionnaire.addItem();
        register.setLinkId("EOL-Register-1");
        register.setText("Register");
        register.setType(Questionnaire.QuestionnaireItemType.GROUP);

        register
                .setText("EOL Register")
                .setLinkId("EOL")
                .setDefinition("EoL Register ")
                .setRepeats(false)
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        register.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/EOL-Register-Flag-1"));
        register.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Flag"));

        // consent


        Questionnaire.QuestionnaireItemComponent consent = questionnaire.addItem();
        consent.setLinkId("CON");
        consent.setText("Consent");
        consent
                .setDefinition("Consent [G3]")
                .setRepeats(false)
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        consent.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/EOL-Consent-1"));
        consent.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Consent"));

        /// CPR


        Questionnaire.QuestionnaireItemComponent cpr = questionnaire.addItem();
        cpr.setLinkId("CPR");
        cpr.setText("CPR Status");

        cpr.setType(Questionnaire.QuestionnaireItemType.GROUP);

        Questionnaire.QuestionnaireItemComponent item = cpr.addItem()
                .setText("CPR Status")
                .setLinkId("CPR001.1")
                .setDefinition("If CPR status is transmitted, then the status code is mandatory.  This will be a straight binary choice of \"For\" or \"Not For\" resuscitation.\n" +
                        "\n" +
                        "Whilst there is a code for \"not aware of decision\", this is not a logical requirement for this dataset.  Systems that are unaware of the status will logically not be sending this group.\n" +
                        "\n" +
                        "\"For CPR\" will generally be only used when reversing a \"Not for CPR\" status.  People who haven't yet had the discussion would just not have a recorded CPR decision in file.")
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
                .setDefinition("It will be a strong recommendation that a reason for the status change is included although it is unlikely that it can be mandated from the start.  Also, as this is an MVP dataset, it could be argued that UEC are most keen on seeing the status rather than additional text.  However, in the example below if the text reads \"There is a valid advance decision to refuse CPR in the following circumstances...\", then the DNACPR decision is not for all circumstances.  In that case, the business may wish to consider whether a non-blanket DNACPR would be better recorded as an Advance Treatment Preference so that where it is appropriate can be made clear.")
                .setLinkId("CPR001.2")
                .setType(Questionnaire.QuestionnaireItemType.STRING);

        cpr.addItem()
                .setText("CPR Status Mental Capacity")
                .setLinkId("CPR001.3")
                .setDefinition("CPR Status [G5]")
                .setType(Questionnaire.QuestionnaireItemType.STRING);


        item = cpr.addItem()
                .setText("Persons involved in discussion")
                .setDefinition("CPR Status [G8]")
                .setLinkId("CPR001.6")
                .setRepeats(true)
                .setType(Questionnaire.QuestionnaireItemType.GROUP);

        Questionnaire.QuestionnaireItemComponent subitem = item.addItem()
                .setText("Coded entry for people involved in the discussion")
                //.setDefinition("This group exists to specifically list those (who were not in the discussion) that have subsequently been made aware of the decision.")
                .setLinkId("CPR001.6.1")
                .setType(Questionnaire.QuestionnaireItemType.CHOICE)
                .addOption(
                        new Questionnaire.QuestionnaireItemOptionComponent()
                                .setValue(new Coding()
                                        .setCode("713656002")
                                        .setSystem("http://snomed.info/sct")
                                        .setDisplay("Discussion about cardiopulmonary resuscitation with family member (situation)")))
                .addOption(
                        new Questionnaire.QuestionnaireItemOptionComponent()
                                .setValue(new Coding()
                                        .setCode("873351000000102")
                                        .setSystem("http://snomed.info/sct")
                                        .setDisplay("Discussion about resuscitation with carer (situation)")))
                .addOption(
                        new Questionnaire.QuestionnaireItemOptionComponent()
                                .setValue(new Coding()
                                        .setCode("873341000000100")
                                        .setSystem("http://snomed.info/sct")
                                        .setDisplay("Discussion about resuscitation (procedure)")));
        subitem = item.addItem()
                .setText("Text entry for people involved in the discussionn")
              //  .setDefinition("This group exists to specifically list those (who were not in the discussion) that have subsequently been made aware of the decision.")
                .setLinkId("CPR001.6.2")
                .setRequired(true)
                .setType(Questionnaire.QuestionnaireItemType.STRING);


        item = cpr.addItem()
                .setText("Persons or organisations made aware of the decision")
                .setDefinition("This group exists to specifically list those (who were not in the discussion) that have subsequently been made aware of the decision.")
                .setLinkId("CPR001.7")
                .setRepeats(true)
                .setType(Questionnaire.QuestionnaireItemType.GROUP);

        subitem = item.addItem()
                .setText("Coded entry for people aware of the decision")
               // .setDefinition("This group exists to specifically list those (who were not in the discussion) that have subsequently been made aware of the decision.")
                .setLinkId("CPR001.7.1")
                .setType(Questionnaire.QuestionnaireItemType.CHOICE)//.setOptions(new Reference("Some valueSet2"))
                .addOption(
                        new Questionnaire.QuestionnaireItemOptionComponent()
                                .setValue(new Coding()
                                        .setCode("975311000000109")
                                        .setSystem("http://snomed.info/sct")
                                        .setDisplay("Carer informed of cardiopulmonary resuscitation clinical decision (situation)")))
                .addOption(
                    new Questionnaire.QuestionnaireItemOptionComponent()
                        .setValue(new Coding()
                                .setCode("975291000000108")
                                .setSystem("http://snomed.info/sct")
                                .setDisplay("Family member informed of cardiopulmonary resuscitation clinical decision (situation)")))
                                .addOption(
                    new Questionnaire.QuestionnaireItemOptionComponent()
                        .setValue(new Coding()
                                .setCode("845151000000104")
                                .setSystem("http://snomed.info/sct")
                                .setDisplay("Not aware of do not attempt cardiopulmonary resuscitation clinical decision (finding)")));
         subitem = item.addItem()
                .setText("Textual version of that person or group of people")
              //  .setDefinition("This group exists to specifically list those (who were not in the discussion) that have subsequently been made aware of the decision.")
                .setLinkId("CPR001.7.2")
                .setRequired(true)
                .setType(Questionnaire.QuestionnaireItemType.STRING);


        item = cpr.addItem()
                .setText("Professionals Involved In Decision")
                .setLinkId("CPR001.8")
                .setDefinition("CPR Status [G14]")
                .setRepeats(true)
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Practitioner-1"));
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Practitioner"));


        item = cpr.addItem()
                .setText("Professional Endorsing Status")
                .setLinkId("CPR001.10")
                .setDefinition("Optional group for professional endorsement, where the profession recording the status change is not senior enough to be de facto endorser.")
                .setRepeats(true)
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Practitioner-1"));
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Practitioner"));


        // Advanced Treatment Preferences



        Questionnaire.QuestionnaireItemComponent advpref = questionnaire.addItem();
        advpref.setLinkId("ATP")
                .setText("Advanced Treatment Preferences");
        advpref.setType(Questionnaire.QuestionnaireItemType.GROUP);

        Questionnaire.QuestionnaireItemComponent subgroup = advpref.addItem()
                .setLinkId("ATP001.1")

                .setText("Clinical Problems and Advised Interventions")
                .setDefinition("This is effectively making up a Treatment Escalation Plan or and Emergency Treatment Plan.")

                .setType(Questionnaire.QuestionnaireItemType.GROUP);

        item = subgroup.addItem()
                .setLinkId("ATP001.1a")
                .setText("Clinical Problems and Advised Interventions")
                .setDefinition("")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/EOL-ATPProblemList-List-1"));
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("List"));


        Questionnaire.QuestionnaireItemComponent subgroupitem = subgroup.addItem()
                .setLinkId("ATP001.1b")
                .setText("Clinical Problems and Advised Interventions")
                .setDefinition("ATP [G4]")
                .setRepeats(true)
                .setType(Questionnaire.QuestionnaireItemType.GROUP);


        item = subgroupitem.addItem()
                .setLinkId("ATP001.1.1")
                .setText("ATP Problems")
                .setDefinition("")
                .setRequired(true)
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/EOL-ATPProblemHeader-Condition-1"));
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Condition"));

        item = subgroupitem.addItem()
                .setLinkId("ATP001.1.2")
                .setText("Treatment Level")
                .setDefinition("ATP [G5] Note: the CarePlan should be linked to the Condition via CarePlan.adresses")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/EOL-ATP-CarePlan-1"));
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("CarePlan"));


        subgroup = advpref.addItem()
                .setLinkId("ATP001.2")
                .setText("Anticipatory medicines/just in case box issued")
                .setDefinition("To inform those providing care that anticipatory medicines or a just in case box have been issued. These medicines could be administered promptly by appropriate staff if indicated.")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        subgroup.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-EOL-Procedure-1"));
        subgroup.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Procedure"));

        subgroup = advpref.addItem()
                .setLinkId("ATP001.3")
                .setText("Intervention")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        subgroup.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/EOL-ADRT-Flag-1"));
        subgroup.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Flag"));


        subgroup = advpref.addItem()
                .setLinkId("ATP001.4")
                .setText("ReSPECT Care")
                .setDefinition("ATP [G16]")
                .setType(Questionnaire.QuestionnaireItemType.GROUP);

        item = subgroup.addItem()
                .setLinkId("ATP001.4.1")
                .setText("ReSPECT Patient Care Priority Scale")
                .setDefinition("1-100 Where 1= absolute priority on sustaining life and 100= absolute priority in comfort.")
                .setRequired(true)
                .setType(Questionnaire.QuestionnaireItemType.INTEGER);

        item = subgroup.addItem()
                .setLinkId("ATP001.4.2")
                .setText("ReSPECT Patient Care Priority - Textual")
                .setDefinition("ATP [G18]")
                .setType(Questionnaire.QuestionnaireItemType.STRING);


        // TODO ADD IN PROVENANCE

        // LPA


        Questionnaire.QuestionnaireItemComponent lpa = questionnaire.addItem();
        lpa.setLinkId("LPA");
        lpa.setText("Lasting Power of Attorney");
        lpa.setType(Questionnaire.QuestionnaireItemType.GROUP);

        item = lpa.addItem()
                .setLinkId("LPA001.1")
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
                .setLinkId("LPA001.2")
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
        prognosis.setLinkId("PRO");
        prognosis.setText("Prognosis")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        prognosis.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/EOL-Prognosis-ClinicalImpression-1"));
        prognosis.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("ClinicalImpression"));

        // FUNCTIONAL

        Questionnaire.QuestionnaireItemComponent func = questionnaire.addItem();
        func.setLinkId("FUN").setText("Functional Status")
                .setRepeats(true)
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        func.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-EOL-FunctionalStatus-Observation-1"));
        func.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Observation"));

        // DISABILITY


        Questionnaire.QuestionnaireItemComponent disability = questionnaire.addItem();
        disability.setLinkId("DIS")
                .setText("Disabilities");
        disability.setType(Questionnaire.QuestionnaireItemType.GROUP);

        item = disability.addItem()
                .setLinkId("DIS001a")
                .setText("Disability / Condition List")
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-ProblemList-1"));
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("List"));

        item = disability.addItem()
                .setLinkId("DIS001b")
                .setText("Disability / Condition")
                .setRepeats(true)
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-ProblemHeader-Condition-1"));
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Condition"));

        /// EOL Preferences PREF

        Questionnaire.QuestionnaireItemComponent pref = questionnaire.addItem();
        pref.setLinkId("PREF");
        pref.setText("Preferences");
        pref.setType(Questionnaire.QuestionnaireItemType.GROUP);

        subgroup = pref.addItem()
                .setText("Preferred Place Of Death")
                .setLinkId("PRE001.1")
                .setDefinition("Preferences [G4]")
                .setRequired(true)
                .setType(Questionnaire.QuestionnaireItemType.GROUP);


        subgroup.addItem()
                .setText("Preferred Place Of Death (Coded)")
                .setLinkId("PRE001.1.2")
                .setType(Questionnaire.QuestionnaireItemType.CHOICE)
                .setOptions(new Reference("https://fhir.nhs.uk/STU3/ValueSet/EOL-PreferredPlaceDeath-Code-1"));

        subgroup.addItem()
                .setText("Preferred Place Of Death (Text)")
                .setLinkId("PRE001.1.3")
                .setRequired(true)
                .setType(Questionnaire.QuestionnaireItemType.STRING);

        pref.addItem()
                .setText("Preferences and Wishes")
                .setLinkId("PRE001.2")
                .setType(Questionnaire.QuestionnaireItemType.STRING);

        pref.addItem()
                .setText("Domestic Access and Information")
                .setLinkId("PRE001.3")
                .setType(Questionnaire.QuestionnaireItemType.STRING);

        item = pref.addItem()
                .setLinkId("PRE001.4")
                .setText("Preferences Date Recorded")
                .setType(Questionnaire.QuestionnaireItemType.DATETIME);

        item = pref.addItem()
                .setLinkId("PRE001.5")
                .setText("Preferences Author")
                .setRequired(true)
                .setType(Questionnaire.QuestionnaireItemType.REFERENCE);
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")
                .setValue(new Reference("https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-Practitioner-1"));
        item.addExtension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")
                .setValue(new CodeType().setValue("Practitioner"));




        // OTHER


        Questionnaire.QuestionnaireItemComponent other = questionnaire.addItem();
        other.setLinkId("DOC")
                .setText("Other Documents")
                .setRepeats(true)
                .setDefinition("Details of other relevant planning documentsand where to find them.")
                .setType(Questionnaire.QuestionnaireItemType.GROUP);

        other.addItem()
                .setLinkId("DOC001.1")
                .setText("Document Name")
                .setDefinition("Description of name of the advance planning document")
                .setType(Questionnaire.QuestionnaireItemType.STRING)
                .setRequired(true);

        other.addItem()
                .setLinkId("DOC001.2")
                .setText("Document Location")
                .setDefinition("Location of the document")
                .setType(Questionnaire.QuestionnaireItemType.STRING);

        other.addItem()
                .setLinkId("DOC001.3")
                .setText("Document Source")
                .setDefinition("Description of the organisation where the document was created with the patient.")
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




    public Bundle loadEOLC() {

        String nhsNumber= "9658220142";
        System.out.println("Posting Patient NHS Number "+nhsNumber);

        Calendar cal = Calendar.getInstance();


        Date oneHourBack = cal.getTime();
        fhirBundle = new FhirBundleUtil(Bundle.BundleType.COLLECTION);

        doSetUp();

        Bundle patientBundle = getPatientBundle(nhsNumber);


        fhirBundle.processBundleResources(patientBundle);

        Bundle bundle = new Bundle();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        bundle.addEntry().setResource(yas);
        bundle.addEntry().setResource(lth);
        bundle.addEntry().setResource(midyorks);
        getPatientBundle("9658220142");

        QuestionnaireResponse eolc = new QuestionnaireResponse();
        eolc.setId(fhirBundle.getNewId(eolc));

        eolc.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        eolc.getIdentifier().setSystem(midYorksQuestionnaireResponseIdentifier).setValue("yas0");

        Reference qRef = new Reference();
        qRef.getIdentifier().setSystem("https://fhir.nhs.uk/STU3/Questionnaire").setValue("CareConnect-EOLC-1");
        eolc.setQuestionnaire(qRef);
        eolc.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        try {
            eolc.setAuthored(sdf.parse("2018-08-01"));
        } catch (Exception ex) {
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent register = eolc.addItem()
                .setLinkId("EOL")
                .setText("EoL Register");

        QuestionnaireResponse.QuestionnaireResponseItemComponent other = eolc.addItem()
                .setLinkId("DOC")
                .setText("Other Documents");

        QuestionnaireResponse.QuestionnaireResponseItemComponent disability = eolc.addItem()
                .setLinkId("DIS")
                .setText("Disabilitiess");

        QuestionnaireResponse.QuestionnaireResponseItemComponent func = eolc.addItem()
                .setLinkId("FUN")
                .setText("Functional Status");

        QuestionnaireResponse.QuestionnaireResponseItemComponent prog = eolc.addItem()
                .setLinkId("PRO")
                .setText("Prognosis");

        QuestionnaireResponse.QuestionnaireResponseItemComponent consent = eolc.addItem()
                .setLinkId("CON")
                .setText("Consent");

        QuestionnaireResponse.QuestionnaireResponseItemComponent preferences = eolc.addItem()
                .setLinkId("PREF")
                .setText("Preferences");

        QuestionnaireResponse.QuestionnaireResponseItemComponent adv = eolc.addItem()
                .setLinkId("ATP")
                .setText("Advanced Treatment Preferences");

        QuestionnaireResponse.QuestionnaireResponseItemComponent lpa = eolc.addItem()
                .setLinkId("LPA")
                .setText("Lasting Power of Attorney");

        QuestionnaireResponse.QuestionnaireResponseItemComponent cpr = eolc.addItem()
                .setLinkId("CPR")
                .setText("CPR Status");

        bundle.addEntry().setResource(eolc);

        Practitioner consultant = new Practitioner();
        consultant.setId(fhirBundle.getNewId(consultant));
        consultant.addIdentifier().setSystem("https://fhir.nhs.uk/Id/sds-user-id").setValue("C4012900");
        consultant.addName().setFamily("Rodger").addGiven("KF");
        bundle.addEntry().setResource(consultant);

        eolc.setAuthor(new Reference(uuidtag + consultant.getId()));





        Practitioner pal = new Practitioner();
        pal.setId(fhirBundle.getNewId(pal));
        pal.addIdentifier().setSystem(interOpenPractitionerIdentifier).setValue("x2900");
        pal.addName().setFamily("Simpson").addGiven("Mike");
        pal.addTelecom().setSystem(ContactPoint.ContactPointSystem.PHONE).setValue("07855 442038").setUse(ContactPoint.ContactPointUse.MOBILE);
        bundle.addEntry().setResource(pal);


// Advanced Treatment Preferences


        Condition condition = new Condition();
        condition.setId(fhirBundle.getNewId(condition));
        condition.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        condition.addIdentifier().setSystem(midYorksConditionIdentifier).setValue("crm1");
        condition.setClinicalStatus(Condition.ConditionClinicalStatus.ACTIVE);
        condition.setAsserter(new Reference(uuidtag + consultant.getId()));
        condition.getCode()
                .setText("Breathlessness")
                .addCoding()
                    .setDisplay("Dyspnea")
                    .setCode("267036007")
                    .setSystem("http://snomed.info/sct");
        try {
            condition.setOnset(new DateTimeType().setValue(sdf.parse("2018-08-01")));
        } catch (Exception ex) {
        }
        bundle.addEntry().setResource(condition);

        CarePlan carePlan = new CarePlan();
        carePlan.setId(fhirBundle.getNewId(carePlan));
        carePlan.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        carePlan.addIdentifier().setSystem(midYorksCarePlanIdentifier).setValue("blm1");
        // Not required carePlan.addAddresses(new Reference(uuidtag + condition.getId()));
        carePlan.addAuthor(new Reference(uuidtag + consultant.getId()));

        carePlan.addCategory()
                .addCoding()
                .setCode("736373009")
                .setSystem("http://snomed.info/sct")
                .setDisplay("End of life care plan");
        carePlan.setStatus(CarePlan.CarePlanStatus.ACTIVE);
        carePlan.setIntent(CarePlan.CarePlanIntent.PLAN);
        try {
            carePlan.getPeriod().setStart(sdf.parse("2018-08-01"));
        } catch (Exception ex) {
        }

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

        ListResource list = new ListResource();
        list.setId(fhirBundle.getNewId(list));
        list.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        list.addIdentifier().setSystem(tppListIdentifier).setValue("listATP");
        list.setSource(new Reference(uuidtag + consultant.getId()));
        try {
            list.setDate(sdf.parse("2018-08-01"));
        } catch (Exception ex) {
        }
        list.addEntry().setItem(new Reference(uuidtag + condition.getIdElement().getIdPart()));
        bundle.addEntry().setResource(list);

        QuestionnaireResponse.QuestionnaireResponseItemComponent group = adv.addItem()
                .setLinkId("ATP001")
                .setText("Clinical Problems and Advised Interventions");

        group.addItem()
                .setLinkId("ATP001a")
                .setText("Clinical Problems and Advised Interventions")
                .addAnswer()
                .setValue(new Reference(uuidtag + list.getIdElement().getIdPart()));

        QuestionnaireResponse.QuestionnaireResponseItemComponent subgroup = group.addItem()
                .setLinkId("ATP001b")
                .setText("Clinical Problems and Advised Interventions");

        subgroup.addItem()
                .setLinkId("ATP001.1.1")
                .setText("Problem or Condition")
                .addAnswer()
                .setValue(new Reference(uuidtag + condition.getIdElement().getIdPart()));

        subgroup.addItem()
                .setLinkId("ATP001.1.2")
                .setText("ATP Intervention")
                .addAnswer()
                .setValue(new Reference(uuidtag + carePlan.getIdElement().getIdPart()));

        // Add in ADRT
        Flag adrt = new Flag();
        adrt.setId(fhirBundle.getNewId(adrt));
        adrt.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        adrt.addIdentifier().setSystem(midYorksFlagIdentifier).setValue("adrt");
        adrt.setStatus(Flag.FlagStatus.ACTIVE);
        adrt.getCode().addCoding()
                .setCode("816301000000100")
                .setSystem("http://snomed.info/sct")
                .setDisplay("Has advance decision to refuse treatment (Mental Capacity Act 2005) (finding)");


        adrt.setAuthor(new Reference(uuidtag + midyorks.getIdElement().getIdPart()));
        bundle.addEntry().setResource(adrt);

        adv.addItem()
                .setLinkId("ATP001.1.3")
                .setText("Advance Decision to Refuse Treatment")
                .addAnswer()
                .setValue(new Reference(uuidtag + adrt.getIdElement().getIdPart()));

        group = adv.addItem()
                .setLinkId("ATP001.4")
                .setText("ReSPECT Care");
        group.addItem()
                .setLinkId("ATP001.4.1")
                .setText("ReSPECT Patient Care Priority Scale")
                .addAnswer()
                .setValue(new IntegerType().setValue(50));
        group.addItem()
                .setLinkId("ATP001.4.2")
                .setText("ReSPECT Patient Care Priority Priority")
                .addAnswer()
                .setValue(new StringType().setValue("Patient is to be treated and made as comfortable as possible."));



        // EOL Register


        Flag flag = new Flag();
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

        register
                .addAnswer()
                .setValue(new Reference(uuidtag + flag.getIdElement().getIdPart()));


        // Consent

        Consent consentR = new Consent();
        consentR.setId(fhirBundle.getNewId(consentR));
        consentR.getIdentifier().setSystem(tppConsentIdentifier).setValue("consent1");
        consentR.setStatus(Consent.ConsentState.ACTIVE);
        consentR.setPatient(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        try {
            consentR.setDateTime(sdf.parse("2018-08-20"));
        } catch (Exception ex) {
        }
        consentR.addPurpose()
                .setCode("882981000000105")
                .setDisplay("Consent given by legitimate patient representative for sharing end of life care coordination record (finding)")
                .setSystem(SNOMEDCT);

        CodeableConcept role =new CodeableConcept();
        role.addCoding().setSystem("http://hl7.org/fhir/v3/ParticipationType").setCode("PROV").setDisplay("Healthcare Provider");
        consentR.addActor()
                .setRole(role)
                .setReference(new Reference(uuidtag + consultant.getId()));
        bundle.addEntry().setResource(consentR);

        consent.addAnswer().setValue(new Reference(uuidtag + consentR.getIdElement().getIdPart()));


        // CPR

        flag = new Flag();
        flag.setId(fhirBundle.getNewId(flag));
        flag.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        flag.addIdentifier().setSystem(midYorksFlagIdentifier).setValue("unusmy8");
        flag.setStatus(Flag.FlagStatus.ACTIVE);
        flag.getCode().addCoding()
                .setCode("450476008")
                .setSystem("http://snomed.info/sct")
                .setDisplay("Not for attempted cardiopulmonary resuscitation");

        flag.setAuthor(new Reference(uuidtag + midyorks.getIdElement().getIdPart()));
        bundle.addEntry().setResource(flag);

        cpr.addItem()
                .setLinkId("CPR001.1")
                .setText("CPR Status")
                .addAnswer()
                .setValue(new Reference(uuidtag +flag.getIdElement().getIdPart()));

        cpr.addItem()
                .setLinkId("CPR001.2")
                .setText("Reason for CPR status")
                .addAnswer()
                .setValue(new StringType("The outcome of the CPR would not be of overall benefit to the patient"));

        cpr.addItem()
                .setLinkId("CPR001.3")
                .setText("CPR Status Mental Capacity")
                .addAnswer()
                .setValue(new StringType("This person has the mental capacity to participate in making these recommendations.  They have been fully involved in the decision making process."));

        Coding persons = new Coding();
        persons
                .setSystem("http://snomed.info/sct")
                .setCode("713656002")
                .setDisplay("Discussion about cardiopulmonary resuscitation with family member (situation)");

        group = cpr.addItem()
                .setLinkId("CPR001.6");

        group.addItem()
                .setLinkId("CPR001.6.1")
                .setText("Persons involved in discussion")
                .addAnswer()
                .setValue(persons);
        group.addItem()
                .setLinkId("CPR001.6.2")
                .setText("Persons involved in discussion")
                .addAnswer()
                .setValue(new StringType().setValue("Supporting inforamtion not provided."));

        Coding aware = new Coding();
        aware
                .setSystem("http://snomed.info/sct")
                .setCode("975291000000108")
                .setDisplay("Family member informed of cardiopulmonary resuscitation clinical decision (situation)");

        group = cpr.addItem()
                .setLinkId("CPR001.7");

        group.addItem()
                .setLinkId("CPR001.7.1")
                .setText("Persons or organisations made aware of the decision")
                .addAnswer()
                .setValue(aware);

        group.addItem()
                .setLinkId("CPR001.7.1")
                .setText("Persons or organisations made aware of the decision")
                .addAnswer()
                .setValue(new StringType().setValue("Uncle Bob has been made aware of the decision"));

        cpr.addItem()
                .setLinkId("CPR001.8")
                .setText("Professionals Involved In Decision")
                .addAnswer()
                .setValue(new Reference(uuidtag + consultant.getId()));

        cpr.addItem()
                .setLinkId("CPR001.10")
                .setText("Professional Endorsing Status")
                .addAnswer()
                .setValue(new Reference(uuidtag + consultant.getId()));


        /// OTHER

        other.addItem()
                .setLinkId("DOC001.1")
                .setText("Document Name")
                .addAnswer()
                .setValue(new StringType("Lasting power of attorney offical document"));
        other.addItem()
                .setLinkId("DOC001.2")
                .setText("Document Location")
                .addAnswer()
                .setValue(new StringType("Top left drawer in cabinet located in dining room. Documents are inside blue folder."));
        other.addItem()
                .setLinkId("DOC001.3")
                .setText("Document Source")
                .addAnswer()
                .setValue(new StringType("Document drawn up at A B Solictors, Newcastle"));


        // Preferences

        group = preferences.addItem().setLinkId("PRE001.1").setText("Preferred Place Of Death");

        group.addItem()
                .setLinkId("PRE001.1.3")
                .setText("Preferred Place Of Death Text")
                .addAnswer()
                .setValue(new StringType("At home with family"));

        preferences.addItem()
                .setLinkId("PRE001.2")
                .setText("Preferences and Wishes")
                .addAnswer()
                .setValue(new StringType("To be made comfortable and looking out onto garden"));
        preferences.addItem()
                .setLinkId("PRE001.3")
                .setText("Domestic Access and Information")
                .addAnswer()
                .setValue(new StringType("A key safe is provided to allow access to the property. Carer and related contact has code."));

        try {
            preferences.addItem()
                    .setLinkId("PRE001.4")
                    .setText("Preferences Date")
                    .addAnswer()
                    .setValue(new DateType().setValue(sdf.parse("2019-02-11")));
        } catch (Exception ex) {

        }

        preferences.addItem()
                .setLinkId("PRE001.5")
                .setText("Preferences Author")
                .addAnswer()
                .setValue(new Reference(uuidtag + consultant.getId()));


        // Prognosis


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

        prognosis.addPrognosisCodeableConcept()
               .setText("Limited life expectancy of approximately one year")
                .addCoding()
                    .setSystem(SNOMEDCT)
                    .setCode("845701000000104")
                    .setDisplay("Gold standards framework prognostic indicator stage A (blue) - year plus prognosis (finding)");
        prognosis.setDescription("Limited life expectancy of approximately one year");
        bundle.addEntry().setResource(prognosis);

        prog
                .addAnswer()
                .setValue(new Reference(uuidtag +prognosis.getIdElement().getIdPart()));



        // Functional Status

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
        observation.getCode().setText("Performance Status score");

        observation.setValue(new Quantity()
                .setValue(
                        new BigDecimal(90))
                .setUnit("score")
                .setSystem("http://unitsofmeasure.org")
                .setCode("score"));
        bundle.addEntry().setResource(observation);

        func.addAnswer()
                .setValue(new Reference(uuidtag +observation.getIdElement().getIdPart()));


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

        disability.addItem()
                .setLinkId("DIS001b")
                .setText("Disability / Condition List")
                .addAnswer()
                .setValue(new Reference(uuidtag +condition.getIdElement().getIdPart()));

        list = new ListResource();
        list.setId(fhirBundle.getNewId(list));
        list.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        list.addIdentifier().setSystem(tppListIdentifier).setValue("listDIS");
        list.setSource(new Reference(uuidtag + consultant.getId()));
        try {
            list.setDate(sdf.parse("2018-08-01"));
        } catch (Exception ex) {
        }
        list.addEntry().setItem(new Reference(uuidtag + condition.getIdElement().getIdPart()));
        bundle.addEntry().setResource(list);

        disability.addItem()
                .setLinkId("DIS001a")
                .setText("Disability / Condition List")
                .addAnswer()
                .setValue(new Reference(uuidtag +list.getIdElement().getIdPart()));


        // LPA

        Flag lpaf = new Flag();
        lpaf.setId(fhirBundle.getNewId(lpaf));
        lpaf.setSubject(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        lpaf.addIdentifier().setSystem(midYorksFlagIdentifier).setValue("lpa");
        lpaf.setStatus(Flag.FlagStatus.ACTIVE);
        lpaf.getCode().addCoding()
                .setCode("816361000000101")
                .setSystem("http://snomed.info/sct")
                .setDisplay("Has appointed person with personal welfare lasting power of attorney (Mental Capacity Act 2005) (finding)");

        lpaf.setAuthor(new Reference(uuidtag + midyorks.getIdElement().getIdPart()));
        bundle.addEntry().setResource(lpaf);

        lpa.addItem()
                .setLinkId("LPA001.1")
                .setText("Lasting Power of Attorney For Health and Welfare")
                .addAnswer()
                .setValue(new Reference(uuidtag +lpaf.getIdElement().getIdPart()));

        RelatedPerson person = new RelatedPerson();
        person.setId(fhirBundle.getNewId(person));
        person.addIdentifier()
                .setValue("person1")
                .setSystem(tppNOKIdentifier);
        person.setPatient(new Reference(uuidtag + fhirBundle.getPatient().getId()));
        CodeableConcept relation = new CodeableConcept();
        relation.addCoding().setSystem("http://hl7.org/fhir/v3/RoleCode").setDisplay("healthcare power of attorney").setCode("HPOWATT");
        person.addName().setFamily("Kewn").addPrefix("Mr").addGiven("E");

        person.setRelationship(relation);

        bundle.addEntry().setResource(person);

        lpa.addItem()
                .setLinkId("LPA001.2")
                .setText("Persons Appointed")
                .addAnswer()
                .setValue(new Reference(uuidtag + person.getIdElement().getIdPart()));

        /*
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
*/

        fhirBundle.processBundleResources(bundle);

        System.out.println(ctxFHIR.newXmlParser().setPrettyPrint(true).encodeResourceToString(fhirBundle.getFhirDocument()));


        try {
            saveBundle(nhsNumber + ".xml", "patient", fhirBundle.getFhirDocument());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        MethodOutcome outcome = client.create().resource(fhirBundle.getFhirDocument()).execute();

        return fhirBundle.getFhirDocument();
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


    public void postQuestionnaire() {


        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = Calendar.getInstance();

        Date oneHourBack = cal.getTime();
        fhirBundle = new FhirBundleUtil(Bundle.BundleType.COLLECTION);

        Bundle bundle = new Bundle();

        Questionnaire questionnaire = getEOLCQuestionnaire();
        bundle.addEntry().setResource(questionnaire);
        fhirBundle.processBundleResources(bundle);

        questionnaire = getNEWS2Questionnaire();
        bundle.addEntry().setResource(questionnaire);
        fhirBundle.processBundleResources(bundle);

        System.out.println(ctxFHIR.newXmlParser().setPrettyPrint(true).encodeResourceToString(fhirBundle.getFhirDocument()));

        MethodOutcome outcome = client.create().resource(fhirBundle.getFhirDocument()).execute();

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

        System.out.println(ctxFHIR.newXmlParser().setPrettyPrint(true).encodeResourceToString(fhirBundle.getFhirDocument()));


        try {
            saveBundle(nhsNumber + ".xml", "patient", fhirBundle.getFhirDocument());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        MethodOutcome outcome = client.create().resource(fhirBundle.getFhirDocument()).execute();

    }


}
