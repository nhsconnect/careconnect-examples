package uk.nhs.careconnect.examples.fhir;

/**
 * Created by kevinmayfield on 26/05/2017.
 */
public class CareConnectSystem {
    public static String SystemNHSNumber = "https://fhir.nhs.uk/Id/nhs-number";
    public static String SystemNHSNumberVerificationStatus = "https://fhir.hl7.org.uk/CareConnect-NHSNumberVerificationStatus-1";
    public static String SystemEthnicCategory ="https://fhir.hl7.org.uk/CareConnect-EthnicCategory-1";
    public static String SystemODSOrganisationCode ="https://fhir.nhs.uk/Id/ods-organization-code";
    public static String SystemSDSUserId="https://fhir.nhs.uk/Id/sds-user-id";

    public static String SystemOrganisationType ="https://fhir.hl7.org.uk/ValueSet/organisation-type-1";
    public static String SystemSDSJobRoleName="https://fhir.hl7.org.uk/ValueSet/sds-job-role-name-1";


    public static String ProfileImmunization = "https://fhir.hl7.org.uk/StructureDefinition/CareConnect-Immunization-1";
    public static String ProfilePatient = "https://fhir.hl7.org.uk/StructureDefinition/CareConnect-Patient-1";
    public static String ProfileMedicationStatement = "https://fhir.hl7.org.uk/StructureDefinition/CareConnect-MedicationStatement-1";
    public static String ProfileMedicationOrder = "https://fhir.hl7.org.uk/StructureDefinition/CareConnect-MedicationOrder-1";
    public static String ProfileMedication = "https://fhir.hl7.org.uk/StructureDefinition/CareConnect-Medication-1";
    public static String ProfileOrganization = "https://fhir.hl7.org.uk/StructureDefinition/CareConnect-Organization-1";
    public static String ProfilePractitioner = "https://fhir.hl7.org.uk/StructureDefinition/CareConnect-Practitioner-1";

    public static String ExtUrlEthnicCategory = "https://fhir.hl7.org.uk/StructureDefinition/Extension-CareConnect-EthnicCategory-1";
    public static String ExtUrlDateRecorded = "https://fhir.hl7.org.uk/StructureDefinition/Extension-CareConnect-DateRecorded-1";
    public static String ExtUrlNHSNumberVerificationStatus = "https://fhir.hl7.org.uk/StructureDefinition/Extension-CareConnect-NHSNumberVerificationStatus-1";

    public static String ExtUrlMedicationRepeatInformation = "https://fhir.hl7.org.uk/StructureDefinition/Extension-CareConnect-MedicationRepeatInformation-1";
    public static String ExtUrlMedicationStatementLastIssueDate = "https://fhir.hl7.org.uk/StructureDefinition/Extension-CareConnect-MedicationStatementLastIssueDate-1";
    public static String ExtUrlMedicationSupplyType = "https://fhir.hl7.org.uk/StructureDefinition/Extension-CareConnect-MedicationSupplyType-1";

    public static String SNOMEDCT = "http://snomed.info/sct";

    public static String HL7FHIRTimingAbbreviation = "http://hl7.org/fhir/ValueSet/timing-abbreviation";


}
