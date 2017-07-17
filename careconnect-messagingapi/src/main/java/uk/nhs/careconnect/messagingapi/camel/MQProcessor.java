package uk.nhs.careconnect.messagingapi.camel;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.hl7.fhir.instance.model.api.IIdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

/**
 * Created by kevinmayfield on 12/07/2017.
 */
public class MQProcessor implements Processor {

    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(uk.nhs.careconnect.messagingapi.camel.Route.class);

    IGenericClient client;

    ArrayList<String> newReferences;

    ArrayList<ResourceProcessing> resourceList;

    IParser parser;

    public MQProcessor(FhirContext ctx, IGenericClient client)
    {
        this.ctx = ctx;
        this.client = client;
    }
    class ResourceProcessing {
        IResource resource;
        Boolean processed;
        String bundleId;
        String actualId;
    }

    private boolean isNullReference(ResourceReferenceDt reference) {
        if (reference == null ) return true;
        if (reference.getReference().getValue() == null) {
            return true;
        } else {
            return false;
        }

     }

    private boolean processed(String reference) {

        for(String s : newReferences)
            if(s.trim().contains(reference)) return true;
        return false;
    }

    private String findAcutalId (String referenceId) {

        // If guid deteched just search on the guid itself
        int i = 0;
        String actualId = null;
        String noUuid = null;
        if (referenceId.contains("urn:uuid:")) {
            referenceId = referenceId.replace("urn:uuid:","");
        }

        for (int h = 0; h < resourceList.size(); h++) {
            String compare = resourceList.get(h).bundleId;
            if (compare.contains("urn:uuid:")) {
                compare = compare.replace("urn:uuid:","");
            }
            if (resourceList.get(h).processed && resourceList.get(h).bundleId != null) {
                if (referenceId.equals(compare)) {
                    actualId = resourceList.get(h).actualId;
                    newReferences.add(actualId);
                }
            }
        }
        if (actualId == null) log.debug("Not found referenceId="+referenceId);
        return actualId;
    }

    /*

     ALLERGY INTOLERANCE

     */

    private boolean processAllergyIntolerance(int f, AllergyIntolerance allergyIntolerance) {
        Boolean allReferenced = true;
        if (allergyIntolerance.getIdentifier().size() == 0 ) {
            allergyIntolerance.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(allergyIntolerance.getId().getValue());
        }

        // Compostion Patient
        if (!isNullReference(allergyIntolerance.getPatient())) {

            log.debug(allergyIntolerance.getPatient().getReference().getValue());
            String referencedResource = findAcutalId(allergyIntolerance.getPatient().getReference().getValue());
            if (referencedResource != null) {
                allergyIntolerance.getPatient().setReference(referencedResource);
            } else {
                if (!processed(allergyIntolerance.getPatient().getReference().getValue())) allReferenced = false;
            }
        }

        if (allReferenced) {
            MethodOutcome outcome = client.update().resource(allergyIntolerance)
                    .conditionalByUrl("AllergyIntolerance?identifier=" + allergyIntolerance.getIdentifier().get(0).getSystem() + "%7C" + allergyIntolerance.getIdentifier().get(0).getValue())
                    .execute();
            allergyIntolerance.setId(outcome.getId());
            resourceList.get(f).processed = true;
            resourceList.get(f).actualId = outcome.getId().getValue();
            resourceList.get(f).resource = allergyIntolerance;
            log.info("Composition: Id="+resourceList.get(f).bundleId+" Server Id = "+outcome.getId().getValue());
        }
        return allReferenced;
    }

    /*

     APPOINTMENT

     */
    private boolean processAppointment(int f, Appointment appointment) {
        Boolean allReferenced = true;

        if (appointment.getIdentifier().size() == 0) {
            appointment.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(appointment.getId().getValue());
        }

        // Appointment.Participants
        for (Appointment.Participant participant : appointment.getParticipant()) {
            ResourceReferenceDt reference  = participant.getActor();
            if (!isNullReference(reference)) {
                String referencedResource = findAcutalId(reference.getReference().getValue());
                if (referencedResource != null) {
                    reference.setReference(referencedResource);
                } else {
                    if (!processed(reference.getReference().getValue())) allReferenced = false;
                }
            }
        }

        if (allReferenced)
        {
            log.info("Appointment = "+parser.setPrettyPrint(true).encodeResourceToString(appointment));
            MethodOutcome outcome = client.update().resource(appointment)
                    .conditionalByUrl("Appointment?identifier=" + appointment.getIdentifier().get(0).getSystem() + "%7C" + appointment.getIdentifier().get(0).getValue())
                    .execute();
            if (outcome.getResource()!=null) {
                log.info("Outcome = " + parser.setPrettyPrint(true).encodeResourceToString(outcome.getResource()));
            }
            appointment.setId(outcome.getId());
            resourceList.get(f).processed = true;
            resourceList.get(f).resource = appointment;
            resourceList.get(f).actualId = outcome.getId().getValue();
            log.info("Appointment: Id="+resourceList.get(f).bundleId+" Server Id = "+outcome.getId().getValue());
        }

        return allReferenced;
    }

    private boolean processComposition(int f, Composition composition) {
        Boolean allReferenced = true;
        if (composition.getIdentifier() !=null) {
            composition.getIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(composition.getId().getValue());
        }

        // Compostion Patient
        if (!isNullReference(composition.getSubject())) {

            log.debug(composition.getSubject().getReference().getValue());
            String referencedResource = findAcutalId(composition.getSubject().getReference().getValue());
            if (referencedResource != null) {

                composition.getSubject().setReference(referencedResource);

            } else {
                if (!processed(composition.getSubject().getReference().getValue())) allReferenced = false;
            }
        }

        // Composition Encounter
        if (!isNullReference(composition.getEncounter())) {

            log.debug(composition.getEncounter().getReference().getValue());
            String referencedResource = findAcutalId(composition.getEncounter().getReference().getValue());
            if (referencedResource != null) {
                composition.getEncounter().setReference(referencedResource);
            } else {
                if (!processed(composition.getEncounter().getReference().getValue())) allReferenced = false;
            }
        }

        // Composition Custodian
        if (!isNullReference(composition.getCustodian())) {

            log.debug(composition.getCustodian().getReference().getValue());
            String referencedResource = findAcutalId(composition.getCustodian().getReference().getValue());

            if (referencedResource != null) {
                composition.getCustodian().setReference(referencedResource);
            } else {
                if (!processed(composition.getCustodian().getReference().getValue())) allReferenced = false;
            }
        }


        // Composition Author
        for(ResourceReferenceDt reference: composition.getAuthor()) {
            if (!isNullReference(reference)) {
                String referencedResource = findAcutalId(reference.getReference().getValue());

                if (referencedResource != null) {
                    reference.setReference(referencedResource);
                } else {
                    if (!processed(reference.getReference().getValue())) allReferenced = false;
                }
            }
        }


        // Section entries
        for (Composition.Section section :composition.getSection()) {
            for(ResourceReferenceDt reference: section.getEntry()) {
                if (!isNullReference(reference)) {
                    String referencedResource = findAcutalId(reference.getReference().getValue());

                    if (referencedResource != null) {
                        reference.setReference(referencedResource);
                    } else {
                        if (!processed(reference.getReference().getValue())) allReferenced = false;
                    }
                }
            }
        }



        if (allReferenced) {
            MethodOutcome outcome = client.update().resource(composition)
                    .conditionalByUrl("Composition?identifier=" + composition.getIdentifier().getSystem() + "%7C" + composition.getIdentifier().getValue())
                    .execute();
            composition.setId(outcome.getId());
            resourceList.get(f).processed = true;
            resourceList.get(f).actualId = outcome.getId().getValue();
            resourceList.get(f).resource = composition;
            log.info("Composition: Id="+resourceList.get(f).bundleId+" Server Id = "+outcome.getId().getValue());
        }
        return allReferenced;
    }

    private boolean processCondition(int f, Condition condition) {
        Boolean allReferenced = true;

        // Having an identifier makes this a lot easier.
        if (condition.getIdentifier().size() == 0) {
            condition.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(condition.getId().getValue());
        }

        ResourceReferenceDt reference = condition.getPatient();
        if (!isNullReference(reference)) {
            String referencedResource = findAcutalId(reference.getReference().getValue());
            if (referencedResource != null) {
                reference.setReference(referencedResource);
            } else {
                if (!processed(reference.getReference().getValue())) allReferenced = false;
            }
        }

        // bundle condition encounter
        reference = condition.getEncounter();
        if (!isNullReference(reference)) {
            String referencedResource = findAcutalId(reference.getReference().getValue());
            if (referencedResource != null) {
                reference.setReference(referencedResource);
            } else {
                if (!processed(reference.getReference().getValue())) allReferenced = false;
            }
        }



        // Bundle.Condition asserter
        reference = condition.getAsserter();
        if (!isNullReference(reference)) {
            String referencedResource = findAcutalId(reference.getReference().getValue());
            if (referencedResource != null) {
                reference.setReference(referencedResource);
            } else {
                if (!processed(reference.getReference().getValue())) allReferenced = false;
            }
        }


        if (allReferenced)
        {
            log.info("Condition= "+parser.setPrettyPrint(true).encodeResourceToString(condition));
            MethodOutcome outcome = client.update().resource(condition)
                    .conditionalByUrl("Condition?identifier=" + condition.getIdentifier().get(0).getSystem() + "%7C" + condition.getIdentifier().get(0).getValue())
                    .execute();
            condition.setId(outcome.getId());
            resourceList.get(f).processed = true;
            resourceList.get(f).resource = condition;
            resourceList.get(f).actualId = outcome.getId().getValue();
            log.info("Condition: Id="+resourceList.get(f).bundleId+" Server Id = "+outcome.getId().getValue());
        }

        return allReferenced;
    }


    private boolean processEncounter(int f, Encounter encounter) {
        Boolean allReferenced = true;

        if (encounter.getIdentifier().size() == 0) {
            encounter.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(encounter.getId().getValue());
        }

        // Bundle.Encounter Patient
        ResourceReferenceDt reference = encounter.getPatient();
        if (!isNullReference(reference)) {
            String referencedResource = findAcutalId(reference.getReference().getValue());
            if (referencedResource != null) {
                reference.setReference(referencedResource);
            } else {
                if (!processed(reference.getReference().getValue())) allReferenced = false;
            }
        }

        // Bundle.EncounterAppointments
        reference = encounter.getAppointment();
        if (!isNullReference(reference)) {
            String referencedResource = findAcutalId(reference.getReference().getValue());
            if (referencedResource != null) {
                reference.setReference(referencedResource);
            } else {
                if (!processed(reference.getReference().getValue())) allReferenced = false;
            }
        }



        // Bundle.Encounter Service Provider
        reference = encounter.getServiceProvider();
        if (!isNullReference(reference)) {
            String referencedResource = findAcutalId(reference.getReference().getValue());
            if (referencedResource != null) {
                reference.setReference(referencedResource);
            } else {
                if (!processed(reference.getReference().getValue())) allReferenced = false;
            }
        }


        // Bundle.Encounter Participants
        for (Encounter.Participant participant : encounter.getParticipant()) {
            reference = participant.getIndividual();
            if (!isNullReference(reference)) {
                String referencedResource = findAcutalId(reference.getReference().getValue());
                if (referencedResource != null) {
                    reference.setReference(referencedResource);
                } else {
                    if (!processed(reference.getReference().getValue())) allReferenced = false;
                }
            }
        }

        // Bundle.Encounter Locations
        for (Encounter.Location location : encounter.getLocation()) {
            reference = location.getLocation();
            if (!isNullReference(reference)) {
                String referencedResource = findAcutalId(reference.getReference().getValue());
                if (referencedResource != null) {
                    reference.setReference(referencedResource);
                } else {
                    if (!processed(reference.getReference().getValue())) allReferenced = false;
                }
            }
        }




        if (allReferenced)
        {
            log.info("Encounter = "+parser.setPrettyPrint(true).encodeResourceToString(encounter));
            MethodOutcome outcome = client.update().resource(encounter)
                    .conditionalByUrl("Encounter?identifier=" + encounter.getIdentifier().get(0).getSystem() + "%7C" + encounter.getIdentifier().get(0).getValue())
                    .execute();
            if (outcome.getResource()!=null) {
                log.info("Outcome = " + parser.setPrettyPrint(true).encodeResourceToString(outcome.getResource()));
            }
            encounter.setId(outcome.getId());
            resourceList.get(f).processed = true;
            resourceList.get(f).resource = encounter;
            resourceList.get(f).actualId = outcome.getId().getValue();
            log.info("Encounter: Id="+resourceList.get(f).bundleId+" Server Id = "+outcome.getId().getValue());
        }


        return allReferenced;
    }

    private boolean processFlag(int f, Flag flag) {
        Boolean allReferenced = true;

        if (flag.getIdentifier().size() == 0) {
            flag.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(flag.getId().getValue());
        }

        // Bundle.Observation Patient
        if (!isNullReference(flag.getSubject())) {

            log.debug(flag.getSubject().getReference().getValue());
            String referencedResource = findAcutalId(flag.getSubject().getReference().getValue());

            if (referencedResource != null) {
                flag.getSubject().setReference(referencedResource);
            } else {
                if (!processed(flag.getSubject().getReference().getValue())) allReferenced = false;
            }
        }

        if (allReferenced) {

            // Perform a search to look for the identifier - work around
            ca.uhn.fhir.model.api.Bundle searchBundle = client.search().forResource(Flag.class)
                    .where(new StringClientParam("_content").matches().value(flag.getIdentifier().get(0).getValue()))
                    .prettyPrint()
                    .encodedJson().execute();
            // Not found so add the resource
            if (searchBundle.getEntries().size()==0) {
                IIdType id = client.create().resource(flag).execute().getId();
                flag.setId(id);
                resourceList.get(f).processed = true;
                resourceList.get(f).actualId = id.getValue();
                resourceList.get(f).resource = flag;
                log.info("Flag: Id="+resourceList.get(f).bundleId+" Server Id = "+id.getValue());
            }

        }
        return allReferenced;
    }


    private boolean processList(int f, ListResource list) {
        Boolean allReferenced = true;
        if (list.getIdentifier().size() == 0) {
            list.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(list.getId().getValue());
        }

        // Compostion Patient
        if (!isNullReference(list.getSubject())) {

            log.debug(list.getSubject().getReference().getValue());
            String referencedResource = findAcutalId(list.getSubject().getReference().getValue());
            if (referencedResource != null) {

                list.getSubject().setReference(referencedResource);

            } else {
                if (!processed(list.getSubject().getReference().getValue())) allReferenced = false;
            }
        }

        // List Entries
        for(ListResource.Entry entry: list.getEntry()) {
            ResourceReferenceDt reference = entry.getItem();
            if (!isNullReference(reference)) {
                String referencedResource = findAcutalId(reference.getReference().getValue());

                if (referencedResource != null) {
                    reference.setReference(referencedResource);
                } else {
                    if (!processed(reference.getReference().getValue())) allReferenced = false;
                }
            }
        }




        if (allReferenced) {
            // Perform a search to look for the identifier - work around
            ca.uhn.fhir.model.api.Bundle searchBundle = client.search().forResource(ListResource.class)
                    .where(new StringClientParam("_content").matches().value(list.getIdentifier().get(0).getValue()))
                    .prettyPrint()
                    .encodedJson().execute();
            // Not found so add the resource
            if (searchBundle.getEntries().size()==0) {
                IIdType id = client.create().resource(list).execute().getId();
                list.setId(id);
                resourceList.get(f).processed = true;
                resourceList.get(f).actualId = id.getValue();
                resourceList.get(f).resource = list;
                log.info("List: Id="+resourceList.get(f).bundleId+" Server Id = "+id.getValue());
            }

        }
        return allReferenced;
    }



    private boolean processLocation(int f, Location location) {

        Boolean allReferenced = true;

        if (location.getIdentifier().size() == 0) {
            location.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(location.getId().getValue());
        }

        if (!isNullReference(location.getManagingOrganization()))  {
            IResource referencedResource = null;
            log.info(location.getManagingOrganization().getReference().getValue());
            int i =0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && location.getManagingOrganization().getReference().getValue().equals(resourceList.get(h).bundleId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("BundleId ="+resourceList.get(h).bundleId);
                    log.debug("ActualId ="+resourceList.get(h).actualId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.debug("New ReferenceId = "+resourceList.get(i).actualId);
                location.getManagingOrganization().setReference(resourceList.get(i).actualId);
                newReferences.add(resourceList.get(i).actualId);

            } else {
                allReferenced = false;
            }
        }

        if (allReferenced) {
            MethodOutcome outcome = client.update().resource(location)
                    .conditionalByUrl("Location?identifier=" + location.getIdentifier().get(0).getSystem() + "%7C" + location.getIdentifier().get(0).getValue())
                    .execute();
            location.setId(outcome.getId());
            resourceList.get(f).processed = true;
            resourceList.get(f).actualId = outcome.getId().getValue();
            resourceList.get(f).resource = location;
            log.info("Location: Id="+resourceList.get(f).bundleId+" Server Id = "+outcome.getId().getValue());
        }
        return allReferenced;
    }

    private boolean processMedication(int f, Medication medication) {
        Boolean allReferenced = true;
        if (medication.getCode().getCoding().size() == 0) {
            medication.getCode().addCoding()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setCode(medication.getId().getValue());
        }

        if (allReferenced) {
            MethodOutcome outcome = client.update().resource(medication)
                    .conditionalByUrl("Medication?code=" + medication.getCode().getCoding().get(0).getSystem() + "%7C" + medication.getCode().getCoding().get(0).getCode())
                    .execute();
            medication.setId(outcome.getId());
            resourceList.get(f).processed = true;
            resourceList.get(f).actualId = outcome.getId().getValue();
            resourceList.get(f).resource = medication;
            log.info("Medication: Id="+resourceList.get(f).bundleId+" Server Id = "+outcome.getId().getValue());
        }
        return allReferenced;
    }

    private boolean processMedicationStatement(int f, MedicationStatement medicationStatement) {
        Boolean allReferenced = true;
        if (medicationStatement.getIdentifier().size() == 0) {
            medicationStatement.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(medicationStatement.getId().getValue());
        }


        if (!isNullReference(medicationStatement.getPatient())) {
            IResource referencedResource = null;
            log.debug(medicationStatement.getPatient().getReference().getValue());
            int i = 0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && medicationStatement.getPatient().getReference().getValue().equals(resourceList.get(h).bundleId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("BundleId =" + resourceList.get(h).bundleId);
                    log.debug("ActualId =" + resourceList.get(h).actualId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.debug("New ReferenceId = " + resourceList.get(i).actualId);
                medicationStatement.getPatient().setReference(resourceList.get(i).actualId);
                newReferences.add(resourceList.get(i).actualId);

            } else {
                if (!processed(medicationStatement.getPatient().getReference().getValue())) allReferenced = false;
            }
        }

        if (medicationStatement.getMedication() instanceof ResourceReferenceDt) {
            if (!isNullReference((ResourceReferenceDt) medicationStatement.getMedication())) {
                IResource referencedResource = null;
                log.debug(((ResourceReferenceDt) medicationStatement.getMedication()).getReference().getValue());
                int i = 0;
                for (int h = 0; h < resourceList.size(); h++) {
                    if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && ((ResourceReferenceDt) medicationStatement.getMedication()).getReference().getValue().equals(resourceList.get(h).bundleId)) {
                        referencedResource = resourceList.get(h).resource;
                        log.debug("BundleId =" + resourceList.get(h).bundleId);
                        log.debug("ActualId =" + resourceList.get(h).actualId);
                        i = h;
                    }
                }
                if (referencedResource != null) {
                    log.debug("New ReferenceId = " + resourceList.get(i).actualId);
                    ((ResourceReferenceDt) medicationStatement.getMedication()).setReference(resourceList.get(i).actualId);
                    newReferences.add(resourceList.get(i).actualId);

                } else {
                    if (!processed(medicationStatement.getPatient().getReference().getValue())) allReferenced = false;
                }
            }
        }

        if (allReferenced) {
            MethodOutcome outcome = client.update().resource(medicationStatement)
                    .conditionalByUrl("MedicationStatement?identifier=" + medicationStatement.getIdentifier().get(0).getSystem() + "%7C" + medicationStatement.getIdentifier().get(0).getValue())
                    .execute();
            medicationStatement.setId(outcome.getId());
            resourceList.get(f).processed = true;
            resourceList.get(f).actualId = outcome.getId().getValue();
            resourceList.get(f).resource = medicationStatement;
            log.info("MedicationStatement: Id="+resourceList.get(f).bundleId+" Server Id = "+outcome.getId().getValue());
        }
        return allReferenced;
    }


    private boolean processOrganisation(int f, Organization organisation) {
        Boolean allReferenced = true;

        if (organisation.getIdentifier().size() == 0) {
            organisation.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(organisation.getId().getValue());
        }

        if (!isNullReference(organisation.getPartOf()) && !organisation.getPartOf().getReference().getValue().isEmpty()) {
            IResource referencedResource = null;
            log.debug("organisation.getPartOf()="+organisation.getPartOf().getReference().getValue());
            int i =0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && organisation.getPartOf().getReference().getValue().equals(resourceList.get(h).bundleId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("BundleId ="+resourceList.get(h).bundleId);
                    log.debug("ActualId ="+resourceList.get(h).actualId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.debug("New ReferenceId = "+resourceList.get(i).actualId);
                organisation.getPartOf().setReference(resourceList.get(i).actualId);
                newReferences.add(resourceList.get(i).actualId);

            } else {
                allReferenced = false;
            }
        }
        if (allReferenced) {
            MethodOutcome outcome = client.update().resource(organisation)
                    .conditionalByUrl("Organization?identifier=" + organisation.getIdentifier().get(0).getSystem() + "%7C" + organisation.getIdentifier().get(0).getValue())
                    .execute();
            organisation.setId(outcome.getId());
            resourceList.get(f).processed = true;
            resourceList.get(f).actualId = outcome.getId().getValue();
            resourceList.get(f).resource = organisation;
            log.info("Organization: Id="+resourceList.get(f).bundleId+" Server Id = "+outcome.getId().getValue());
        }
        return allReferenced;
    }




    private boolean processPatient(int f, Patient patient) {
        Boolean allReferenced = true;

        if (patient.getIdentifier().size() == 0) {
            patient.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(patient.getId().getValue());
        }

        for (int j = 0; j < patient.getCareProvider().size(); j++) {
            IResource referencedResource = null;
            log.info(patient.getCareProvider().get(j).getReference().getValue());
            if (!isNullReference(patient.getCareProvider().get(j))) {
                int i = 0;
                for (int h = 0; h < resourceList.size(); h++) {
                    if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && patient.getCareProvider().get(j).getReference().getValue().equals(resourceList.get(h).bundleId)) {
                        referencedResource = resourceList.get(h).resource;
                        log.debug("patient.getCareProvider() BundleId =" + resourceList.get(h).bundleId);
                        log.debug("patient.getCareProvider() ActualId =" + resourceList.get(h).actualId);
                        i = h;
                    }
                }
                if (referencedResource != null) {
                    log.debug("patient.getCareProvider() New ReferenceId = " + resourceList.get(i).actualId);
                    patient.getCareProvider().get(j).setReference(resourceList.get(i).actualId);
                    newReferences.add(resourceList.get(i).actualId);
                } else {
                    if (!processed(patient.getCareProvider().get(j).getReference().getValue())) allReferenced = false;
                }
            }
        }

        if (allReferenced) {
            MethodOutcome outcome = client.update().resource(patient)
                    .conditionalByUrl("Patient?identifier=" + patient.getIdentifier().get(0).getSystem() + "%7C" + patient.getIdentifier().get(0).getValue())
                    .execute();
            patient.setId(outcome.getId());
            resourceList.get(f).processed = true;
            resourceList.get(f).actualId = outcome.getId().getValue();
            resourceList.get(f).resource = patient;
            log.info("Patient: Id="+resourceList.get(f).bundleId+" Server Id = "+outcome.getId().getValue());
        }
        return allReferenced;
    }

    private boolean processProcedureRequest(int f, ProcedureRequest procedureRequest) {
        Boolean allReferenced = true;

        if (procedureRequest.getIdentifier().size() == 0) {
            procedureRequest.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(procedureRequest.getId().getValue());
        }

        if (!isNullReference(procedureRequest.getSubject())) {
            IResource referencedResource = null;
            log.info(procedureRequest.getSubject().getReference().getValue());
            int i =0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && procedureRequest.getSubject().getReference().getValue().equals(resourceList.get(h).bundleId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("BundleId ="+resourceList.get(h).bundleId);
                    log.debug("ActualId ="+resourceList.get(h).actualId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.debug("New ReferenceId = "+resourceList.get(i).actualId);
                procedureRequest.getSubject().setReference(resourceList.get(i).actualId);
                newReferences.add(resourceList.get(i).actualId);

            } else {
                allReferenced = false;
            }
        }

        if (allReferenced)
        {
            log.info("ProcedureRequest = "+parser.setPrettyPrint(true).encodeResourceToString(procedureRequest));
            MethodOutcome outcome = client.update().resource(procedureRequest)
                    .conditionalByUrl("ProcedureRequest?identifier=" + procedureRequest.getIdentifier().get(0).getSystem() + "%7C" + procedureRequest.getIdentifier().get(0).getValue())
                    .execute();
            if (outcome.getResource()!=null) {
                log.info("Outcome = " + parser.setPrettyPrint(true).encodeResourceToString(outcome.getResource()));
            }
            procedureRequest.setId(outcome.getId());
            resourceList.get(f).processed = true;
            resourceList.get(f).actualId = outcome.getId().getValue();
            resourceList.get(f).resource = procedureRequest;
            log.info("ProcedureRequest: Id="+resourceList.get(f).bundleId+" Server Id = "+outcome.getId().getValue());
        }

        return allReferenced;
    }


    private boolean processObservation(int f, Observation observation) {
        Boolean allReferenced = true;

        // Having an identifier makes this a lot easier.
        if (observation.getIdentifier().size() == 0) {
            observation.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(observation.getId().getValue());
        }


        // Bundle.Observation Patient
        if (!isNullReference(observation.getSubject())) {
            IResource referencedResource = null;
            log.info(observation.getSubject().getReference().getValue());
            int i = 0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && observation.getSubject().getReference().getValue().equals(resourceList.get(h).bundleId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("BundleId =" + resourceList.get(h).bundleId);
                    log.debug("ActualId =" + resourceList.get(h).actualId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.debug("New ReferenceId = " + resourceList.get(i).actualId);
                observation.getSubject().setReference(resourceList.get(i).actualId);
                newReferences.add(resourceList.get(i).actualId);

            } else {
                if (!processed(observation.getSubject().getReference().getValue())) allReferenced = false;
            }
        }
        // bundle observation encounter
        if (!isNullReference(observation.getEncounter())) {
            IResource referencedResource = null;
            log.info(observation.getEncounter().getReference().getValue());
            int i = 0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && observation.getEncounter().getReference().getValue().equals(resourceList.get(h).bundleId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("BundleId =" + resourceList.get(h).bundleId);
                    log.debug("ActualId =" + resourceList.get(h).actualId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.debug("New ReferenceId = " + resourceList.get(i).actualId);
                observation.getEncounter().setReference(resourceList.get(i).actualId);
                newReferences.add(resourceList.get(i).actualId);

            } else {
                if (!processed(observation.getEncounter().getReference().getValue())) allReferenced = false;
            }
        }


        // Bundle.Observation performer
        for (int j = 0; j < observation.getPerformer().size(); j++) {
            IResource referencedResource = null;
            log.info(observation.getPerformer().get(j).getReference().getValue());
            if (!isNullReference(observation.getPerformer().get(j))) {
                int i = 0;
                for (int h = 0; h < resourceList.size(); h++) {
                    if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && observation.getPerformer().get(j).getReference().getValue().equals(resourceList.get(h).bundleId)) {
                        referencedResource = resourceList.get(h).resource;
                        log.debug("BundleId =" + resourceList.get(h).bundleId);
                        log.debug("ActualId =" + resourceList.get(h).actualId);
                        i = h;
                    }
                }
                if (referencedResource != null) {
                    log.debug("New ReferenceId = " + resourceList.get(i).actualId);
                    observation.getPerformer().get(j).setReference(resourceList.get(i).actualId);
                    newReferences.add(resourceList.get(i).actualId);
                } else {
                    if (!processed(observation.getPerformer().get(j).getReference().getValue()))  allReferenced = false;
                }
            }
        }


        if (allReferenced)
        {
            log.info("Encounter = "+parser.setPrettyPrint(true).encodeResourceToString(observation));
            MethodOutcome outcome = client.update().resource(observation)
                    .conditionalByUrl("Observation?identifier=" + observation.getIdentifier().get(0).getSystem() + "%7C" + observation.getIdentifier().get(0).getValue())
                    .execute();
            if (outcome.getResource()!=null) {
                log.info("Outcome = " + parser.setPrettyPrint(true).encodeResourceToString(outcome.getResource()));
            }
            observation.setId(outcome.getId());
            resourceList.get(f).processed = true;
            resourceList.get(f).resource = observation;
            resourceList.get(f).actualId = outcome.getId().getValue();
            log.info("Observation: Id="+resourceList.get(f).bundleId+" Server Id = "+outcome.getId().getValue());
        }

        return allReferenced;
    }


    private boolean processPractitioner(int f, Practitioner practitioner) {
        Boolean allReferenced = true;
        if (practitioner.getIdentifier().size() == 0) {
            practitioner.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(practitioner.getId().getValue());
        }

        // Practioner role organisation
        for (Practitioner.PractitionerRole role : practitioner.getPractitionerRole()) {
            if (!isNullReference(role.getManagingOrganization())) {
                log.info(role.getManagingOrganization().getReference().getValue());

                String referencedResource = findAcutalId(role.getManagingOrganization().getReference().getValue());
                if (referencedResource != null) {
                    role.getManagingOrganization().setReference(referencedResource);
                } else {
                    if (!processed(role.getManagingOrganization().getReference().getValue())) allReferenced = false;
                }
            }
        }

        if (allReferenced) {
            MethodOutcome outcome = client.update().resource(practitioner)
                    .conditionalByUrl("Practitioner?identifier=" + practitioner.getIdentifier().get(0).getSystem() + "%7C" + practitioner.getIdentifier().get(0).getValue())
                    .execute();
            practitioner.setId(outcome.getId());
            resourceList.get(f).processed = true;
            resourceList.get(f).actualId = outcome.getId().getValue();
            resourceList.get(f).resource = practitioner;
            log.info("Practitioner: Id="+resourceList.get(f).bundleId+" Server Id = "+outcome.getId().getValue());
        }
        return allReferenced;
    }

    private boolean processProcedure(int f, Procedure procedure) {
        Boolean allReferenced = true;

        // Having an identifier makes this a lot easier.
        if (procedure.getIdentifier().size() == 0) {
            procedure.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(procedure.getId().getValue());
        }

        // Bundle.COndition Patient
        if (!isNullReference(procedure.getSubject())) {
            IResource referencedResource = null;
            log.info(procedure.getSubject().getReference().getValue());
            int i = 0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && procedure.getSubject().getReference().getValue().equals(resourceList.get(h).bundleId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("BundleId =" + resourceList.get(h).bundleId);
                    log.debug("ActualId =" + resourceList.get(h).actualId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.debug("New ReferenceId = " + resourceList.get(i).actualId);
                procedure.getSubject().setReference(resourceList.get(i).actualId);
                newReferences.add(resourceList.get(i).actualId);

            } else {
                if (!processed(procedure.getSubject().getReference().getValue())) allReferenced = false;
            }
        }
        // bundle condition encounter
        if (!isNullReference(procedure.getEncounter())) {
            IResource referencedResource = null;
            log.info(procedure.getEncounter().getReference().getValue());
            int i = 0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && procedure.getEncounter().getReference().getValue().equals(resourceList.get(h).bundleId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("BundleId =" + resourceList.get(h).bundleId);
                    log.debug("ActualId =" + resourceList.get(h).actualId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.debug("New ReferenceId = " + resourceList.get(i).actualId);
                procedure.getEncounter().setReference(resourceList.get(i).actualId);
                newReferences.add(resourceList.get(i).actualId);

            } else {
                if (!processed(procedure.getEncounter().getReference().getValue())) allReferenced = false;
            }
        }

        // bundle condition location
        if (!isNullReference(procedure.getLocation())) {
            IResource referencedResource = null;
            log.info(procedure.getLocation().getReference().getValue());
            int i = 0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && procedure.getLocation().getReference().getValue().equals(resourceList.get(h).bundleId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("BundleId =" + resourceList.get(h).bundleId);
                    log.debug("ActualId =" + resourceList.get(h).actualId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.debug("New ReferenceId = " + resourceList.get(i).actualId);
                procedure.getLocation().setReference(resourceList.get(i).actualId);
                newReferences.add(resourceList.get(i).actualId);

            } else {
                if (!processed(procedure.getLocation().getReference().getValue())) allReferenced = false;
            }
        }


        // Bundle.Procedure Performer
        for (int j = 0; j < procedure.getPerformer().size(); j++) {
            IResource referencedResource = null;
            log.info(procedure.getPerformer().get(j).getActor().getReference().getValue());
            if (!isNullReference(procedure.getPerformer().get(j).getActor() )) {
                int i = 0;
                for (int h = 0; h < resourceList.size(); h++) {
                    if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && procedure.getPerformer().get(j).getActor().getReference().getValue().equals(resourceList.get(h).bundleId)) {
                        referencedResource = resourceList.get(h).resource;
                        log.debug("BundleId =" + resourceList.get(h).bundleId);
                        log.debug("ActualId =" + resourceList.get(h).actualId);
                        i = h;
                    }
                }
                if (referencedResource != null) {
                    log.debug("New ReferenceId = " + resourceList.get(i).actualId);
                    procedure.getPerformer().get(j).getActor().setReference(resourceList.get(i).actualId);
                    newReferences.add(resourceList.get(i).actualId);
                } else {
                    if (!processed(procedure.getPerformer().get(j).getActor().getReference().getValue()))  allReferenced = false;
                }
            }
        }


        if (allReferenced)
        {
            log.info("Procedure = "+parser.setPrettyPrint(true).encodeResourceToString(procedure));
            MethodOutcome outcome = client.update().resource(procedure)
                    .conditionalByUrl("Procedure?identifier=" + procedure.getIdentifier().get(0).getSystem() + "%7C" + procedure.getIdentifier().get(0).getValue())
                    .execute();
            //if (outcome.getResource()!=null) {
            //    log.info("Outcome = " + parser.setPrettyPrint(true).encodeResourceToString(outcome.getResource()));
           // }
            procedure.setId(outcome.getId());
            resourceList.get(f).processed = true;
            resourceList.get(f).resource = procedure;
            resourceList.get(f).actualId = outcome.getId().getValue();
            log.info("Procedure: Id="+resourceList.get(f).bundleId+" Server Id = "+outcome.getId().getValue());
        }


        return allReferenced;
    }



    @Override
    public void process(Exchange exchange) throws Exception {
        Bundle bundle = null;

        Reader reader = new InputStreamReader(new ByteArrayInputStream((byte[]) exchange.getIn().getBody(byte[].class)));



        if (exchange.getIn().getHeader(Exchange.CONTENT_TYPE).toString().contains("json")) {
            //JsonParser parser = new JsonParser();
            IParser parser = ctx.newJsonParser();
            try {
                bundle = parser.parseResource(Bundle.class, reader);
            } catch (Exception ex) {
                // log.error("#9 JSON Parse failed "+ex.getMessage());
            }
        } else {
            // XmlParser parser = new XmlParser();
            IParser parser = ctx.newXmlParser();
            try {
                bundle = parser.parseResource(Bundle.class, reader);
            } catch (Exception ex) {
                // log.error("#10 XML Parse failed "+ex.getMessage());
            }
        }
        resourceList = new ArrayList<ResourceProcessing>();

        newReferences = new ArrayList<String>();


        for (int f = 0; f < bundle.getEntry().size(); f++) {
            IResource resource = bundle.getEntry().get(f).getResource();
            ResourceProcessing resourceP = new ResourceProcessing();
            resourceP.bundleId = resource.getId().getValue();
            resourceP.processed = false;
            // Miss off MessageHeader, so start at 1 but also processing Composition now
           // if (f == 0) resourceP.processed = true;
            resourceList.add(resourceP);
        }
        // Loop 3 times now to, need to have time out but ensure all resources posted
        parser = ctx.newXmlParser();
        Boolean allProcessed;
        for (int g= 0;g < 5;g++) {
            allProcessed = true;
            for (int f = 0; f < bundle.getEntry().size(); f++) {

                IResource resource = bundle.getEntry().get(f).getResource();


                if (!resourceList.get(f).processed) {

                    log.info("Entry number " + f + " Resource = " +resource.getResourceName() + " Id " + resource.getId().getValue());

                    // Bundle.location
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("Location")) {
                       processLocation(f,(Location) resource);
                    }

                    // Bundle.Organization
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("Organization")) {
                         processOrganisation(f,(Organization) resource);
                    }

                    //Bundle.Practitioner
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("Practitioner")) {
                        processPractitioner(f, (Practitioner) resource);
                    }

                    // Bundle. Patient
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("Patient")) {
                        processPatient( f, (Patient) resource);
                    }

                    // Bundle.Procedure Request
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("ProcedureRequest")) {
                        processProcedureRequest(f, (ProcedureRequest) resource);
                    }

                    // bundle.Appointment
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("Appointment")) {
                        processAppointment(f, (Appointment) resource);
                    }

                    // Bundle.Observation
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("Observation")) {
                        processObservation(f, (Observation) resource);
                    }

                    // Bundle.Encounter
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("Encounter")) {
                        processEncounter(f, (Encounter) resource);
                    }

                    // Bundle.Condition
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("Condition")) {
                        processCondition(f, (Condition) resource);
                    }

                    // Bundle.Procedure
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("Procedure")) {
                        processProcedure(f, (Procedure) resource);
                    }

                    // Bundle.MedicationStatement
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("MedicationStatement")) {
                        processMedicationStatement(f, (MedicationStatement) resource);
                    }

                    // Bundle.Medication
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("Medication")) {
                        processMedication(f, (Medication) resource);
                    }


                    // Bundle.Flag
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("Flag")) {
                        processFlag(f, (Flag) resource);
                    }

                    // Bundle.Composition
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("Composition")) {
                        processComposition(f, (Composition) resource);
                    }

                    // Bundle.List
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("List")) {
                        processList(f, (ListResource) resource);
                    }

                    // Bundle.AllergyIntolernace
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("AllergyIntolerance")) {
                        processAllergyIntolerance(f, (AllergyIntolerance) resource);
                    }

                }
            }
        }
    }
}
