package uk.nhs.careconnect.messagingapi.camel;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.base.resource.ResourceMetadataMap;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.model.dstu2.valueset.AuditEventActionEnum;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.hl7.fhir.instance.model.api.IIdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
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

    ArrayList<EntryProcessing> resourceList;

    IParser parser;

    IParser JSONparser;

    Session session;

    MessageProducer producer;

    public MQProcessor(FhirContext ctx, IGenericClient client,  Session session, MessageProducer producer)
    {
        this.ctx = ctx;
        this.client = client;
        this.session = session;
        this.producer = producer;
    }

    class EntryProcessing {
        IResource resource;
        Boolean processed;
        String originalId;
        String newId;
    }

    private boolean isNullReference(ResourceReferenceDt reference) {
        if (reference == null ) return true;
        if (reference.getReference().getValue() == null) {
            return true;
        } else {
            return false;
        }

     }

    private void sendToAudit(AuditEvent audit) {
        try {
            // Create a ConnectionFactory

            String text =JSONparser.setPrettyPrint(true).encodeResourceToString(audit);
            TextMessage message = session.createTextMessage(text);

            // Tell the producer to send the message
            System.out.println("Sent message: "+ message.hashCode() + " : " + Thread.currentThread().getName());
            producer.send(message);

        }
        catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }

     
    private EntryProcessing findEntryProcess(String originalId)
    {
        EntryProcessing entry = null;
        log.info("Search entry processing for = "+originalId);
        for (EntryProcessing ent : resourceList) {
            if (ent.originalId.equals(originalId)) entry = ent;
        }
        if (entry !=null && entry.newId != null) log.info("Found actual Id of "+entry.newId);
        return entry;
    }

    private boolean processed(String reference) {

        for(String s : newReferences)
            if(s != null && s.trim().contains(reference)) return true;
        return false;
    }
    
    private void addNewReferences(String reference)
    {
        if (reference == null) log.error("Blank reference added");
        newReferences.add(reference);
    }

    private String findAcutalId (String referenceId) {

        // If guid deteched just search on the guid itself
        int i = 0;
        String newId = null;
        log.info("Finding "+referenceId);
        if (referenceId.contains("urn:uuid:")) {
            referenceId = referenceId.replace("urn:uuid:","");
        }
        log.info("Finding (modified) "+referenceId);
        for (EntryProcessing entry:  resourceList) {
            String compare = entry.originalId;
            if (compare.contains("urn:uuid:")) {
                compare = compare.replace("urn:uuid:", "");
            }
            // Need to investigate more but need raw Id.
            compare = compare.replace(entry.resource.getResourceName()+"/","");
            log.info("Comparing to (modified) " + compare);
            if (referenceId.equals(compare) && newId == null) {

                if (entry.processed && entry.originalId != null) {
                    newId = entry.newId;
                    addNewReferences(newId);
                    log.info("### Found "+ entry.resource.getResourceName());
                }
                else
                {
                    log.info("*** Not mapped yet "+ entry.resource.getResourceName());
                }
            }
        }
        if (newId == null) log.info("Not found referenceId = "+referenceId);
        return newId;
    }

    /*

     ALLERGY INTOLERANCE

     */

    private boolean processAllergyIntolerance(AllergyIntolerance allergyIntolerance) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(allergyIntolerance.getId().getValue());
        
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
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) allergyIntolerance.setResourceMetadata(new ResourceMetadataMap());

            MethodOutcome outcome = client.update().resource(allergyIntolerance)
                    .conditionalByUrl("AllergyIntolerance?identifier=" + allergyIntolerance.getIdentifier().get(0).getSystem() + "%7C" + allergyIntolerance.getIdentifier().get(0).getValue())
                    .execute();
            allergyIntolerance.setId(outcome.getId());
            entry.processed = true;
            entry.newId = outcome.getId().getValue();
            entry.resource = allergyIntolerance;
            log.info("AllergyIntolerabce: Id="+entry.originalId+" Server Id = "+outcome.getId().getValue());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(allergyIntolerance, outcome, "rest", "create", AuditEventActionEnum.CREATE,"CareConnectMessagingAPI"));

        }
        return allReferenced;
    }

    /*

     APPOINTMENT

     */
    private boolean processAppointment(Appointment appointment) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(appointment.getId().getValue());

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
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) appointment.setResourceMetadata(new ResourceMetadataMap());

            // log.info("Appointment = "+parser.setPrettyPrint(true).encodeResourceToString(appointment));
            MethodOutcome outcome = client.update().resource(appointment)
                    .conditionalByUrl("Appointment?identifier=" + appointment.getIdentifier().get(0).getSystem() + "%7C" + appointment.getIdentifier().get(0).getValue())
                    .execute();
          //  if (outcome.getResource()!=null) {
         //       log.info("Outcome = " + parser.setPrettyPrint(true).encodeResourceToString(outcome.getResource()));
        //    }
            appointment.setId(outcome.getId());
            entry.processed = true;
            entry.resource = appointment;
            entry.newId = outcome.getId().getValue();
            log.info("Appointment: Id="+entry.originalId+" Server Id = "+outcome.getId().getValue());
        }

        return allReferenced;
    }

    private boolean processComposition(Composition composition) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(composition.getId().getValue());
        
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
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) composition.setResourceMetadata(new ResourceMetadataMap());

            MethodOutcome outcome = client.update().resource(composition)
                    .conditionalByUrl("Composition?identifier=" + composition.getIdentifier().getSystem() + "%7C" + composition.getIdentifier().getValue())
                    .execute();
            composition.setId(outcome.getId());
            entry.processed = true;
            entry.newId = outcome.getId().getValue();
            entry.resource = composition;
            log.info("Composition: Id="+entry.originalId+" Server Id = "+outcome.getId().getValue());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(composition, outcome, "rest", "create", AuditEventActionEnum.CREATE,"CareConnectMessagingAPI"));

        }
        return allReferenced;
    }

    private boolean processCondition(Condition condition) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(condition.getId().getValue());

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
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) condition.setResourceMetadata(new ResourceMetadataMap());

            // log.info("Condition= "+parser.setPrettyPrint(true).encodeResourceToString(condition));
            MethodOutcome outcome = client.update().resource(condition)
                    .conditionalByUrl("Condition?identifier=" + condition.getIdentifier().get(0).getSystem() + "%7C" + condition.getIdentifier().get(0).getValue())
                    .execute();
            condition.setId(outcome.getId());
            entry.processed = true;
            entry.resource = condition;
            entry.newId = outcome.getId().getValue();
            log.info("Condition: Id="+entry.originalId+" Server Id = "+outcome.getId().getValue());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(condition, outcome, "rest", "create", AuditEventActionEnum.CREATE,"CareConnectMessagingAPI"));

        }

        return allReferenced;
    }


    private boolean processEncounter( Encounter encounter) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(encounter.getId().getValue());

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
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) encounter.setResourceMetadata(new ResourceMetadataMap());

            //log.info("Encounter = "+parser.setPrettyPrint(true).encodeResourceToString(encounter));
            MethodOutcome outcome = client.update().resource(encounter)
                    .conditionalByUrl("Encounter?identifier=" + encounter.getIdentifier().get(0).getSystem() + "%7C" + encounter.getIdentifier().get(0).getValue())
                    .execute();
          //  if (outcome.getResource()!=null) {
          //     log.info("Outcome = " + parser.setPrettyPrint(true).encodeResourceToString(outcome.getResource()));
         //   }
            encounter.setId(outcome.getId());
            entry.processed = true;
            entry.resource = encounter;
            entry.newId = outcome.getId().getValue();
            log.info("Encounter: Id="+entry.originalId+" Server Id = "+outcome.getId().getValue());

            sendToAudit(CareConnectAuditEvent.buildAuditEvent(encounter, outcome, "rest", "create", AuditEventActionEnum.CREATE,"CareConnectMessagingAPI"));

        }


        return allReferenced;
    }

    private boolean processFlag(Flag flag) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(flag.getId().getValue());

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
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) flag.setResourceMetadata(new ResourceMetadataMap());


            // Perform a search to look for the identifier - work around
            ca.uhn.fhir.model.api.Bundle searchBundle = client.search().forResource(Flag.class)
                    .where(new StringClientParam("_content").matches().value(flag.getIdentifier().get(0).getValue()))
                    .prettyPrint()
                    .encodedJson().execute();
            // Not found so add the resource
            if (searchBundle.getEntries().size()==0) {
                IIdType id = client.create().resource(flag).execute().getId();
                flag.setId(id);
                entry.processed = true;
                entry.newId = id.getValue();
                entry.resource = flag;
                log.info("Flag: Id="+entry.originalId+" Server Id = "+id.getValue());
                sendToAudit(CareConnectAuditEvent.buildAuditEvent(flag, null, "rest", "create", AuditEventActionEnum.CREATE,"CareConnectMessagingAPI"));

            }

        }
        return allReferenced;
    }


    private boolean processList(ListResource list) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(list.getId().getValue());
        
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
        for(ListResource.Entry listentry: list.getEntry()) {
            ResourceReferenceDt reference = listentry.getItem();
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
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) list.setResourceMetadata(new ResourceMetadataMap());

            // Perform a search to look for the identifier - work around
            ca.uhn.fhir.model.api.Bundle searchBundle = client.search().forResource(ListResource.class)
                    .where(new StringClientParam("_content").matches().value(list.getIdentifier().get(0).getValue()))
                    .prettyPrint()
                    .encodedJson().execute();
            // Not found so add the resource
            if (searchBundle.getEntries().size()==0) {
                IIdType id = client.create().resource(list).execute().getId();
                list.setId(id);
                entry.processed = true;
                entry.newId = id.getValue();
                entry.resource = list;
                log.info("List: Id="+entry.originalId+" Server Id = "+id.getValue());
                sendToAudit(CareConnectAuditEvent.buildAuditEvent(list, null, "rest", "create", AuditEventActionEnum.CREATE,"CareConnectMessagingAPI"));

            }

        }
        return allReferenced;
    }



    private boolean processLocation(Location location) {

        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(location.getId().getValue());

        if (location.getIdentifier().size() == 0) {
            location.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(location.getId().getValue());
        }

        if (!isNullReference(location.getManagingOrganization()))  {
            ResourceReferenceDt reference = location.getManagingOrganization();
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

            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) location.setResourceMetadata(new ResourceMetadataMap());

            MethodOutcome outcome = client.update().resource(location)
                    .conditionalByUrl("Location?identifier=" + location.getIdentifier().get(0).getSystem() + "%7C" + location.getIdentifier().get(0).getValue())
                    .execute();
            location.setId(outcome.getId());
            
            entry.processed = true;
            entry.newId = outcome.getId().getValue();
            entry.resource = location;
            log.info("Location: Id="+entry.originalId+" Server Id = "+outcome.getId().getValue());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(location, outcome, "rest", "create", AuditEventActionEnum.CREATE,"CareConnectMessagingAPI"));

        }
        return allReferenced;
    }

    private boolean processMedication(Medication medication) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(medication.getId().getValue());
        
        if (medication.getCode().getCoding().size() == 0) {
            medication.getCode().addCoding()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setCode(medication.getId().getValue());
        }

        if (allReferenced) {

            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) medication.setResourceMetadata(new ResourceMetadataMap());

            MethodOutcome outcome = client.update().resource(medication)
                    .conditionalByUrl("Medication?code=" + medication.getCode().getCoding().get(0).getSystem() + "%7C" + medication.getCode().getCoding().get(0).getCode())
                    .execute();
            medication.setId(outcome.getId());
            entry.processed = true;
            entry.newId = outcome.getId().getValue();
            entry.resource = medication;
            log.info("Medication: Id="+entry.originalId+" Server Id = "+outcome.getId().getValue());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(medication, outcome, "rest", "create", AuditEventActionEnum.CREATE,"CareConnectMessagingAPI"));

        }
        return allReferenced;
    }

    private boolean processMedicationStatement( MedicationStatement medicationStatement) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(medicationStatement.getId().getValue());
        
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
                if (resourceList.get(h).processed && resourceList.get(h).originalId != null && medicationStatement.getPatient().getReference().getValue().equals(resourceList.get(h).originalId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("originalId =" + resourceList.get(h).originalId);
                    log.debug("newId =" + resourceList.get(h).newId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.info("New ReferenceId = " + resourceList.get(i).newId);
                medicationStatement.getPatient().setReference(resourceList.get(i).newId);
                addNewReferences(resourceList.get(i).newId);

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
                    if (resourceList.get(h).processed && resourceList.get(h).originalId != null && ((ResourceReferenceDt) medicationStatement.getMedication()).getReference().getValue().equals(resourceList.get(h).originalId)) {
                        referencedResource = resourceList.get(h).resource;
                        log.debug("originalId =" + resourceList.get(h).originalId);
                        log.debug("newId =" + resourceList.get(h).newId);
                        i = h;
                    }
                }
                if (referencedResource != null) {
                    log.info("New ReferenceId = " + resourceList.get(i).newId);
                    ((ResourceReferenceDt) medicationStatement.getMedication()).setReference(resourceList.get(i).newId);
                    addNewReferences(resourceList.get(i).newId);

                } else {
                    if (!processed(medicationStatement.getPatient().getReference().getValue())) allReferenced = false;
                }
            }
        }

        if (allReferenced) {
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) medicationStatement.setResourceMetadata(new ResourceMetadataMap());

            MethodOutcome outcome = client.update().resource(medicationStatement)
                    .conditionalByUrl("MedicationStatement?identifier=" + medicationStatement.getIdentifier().get(0).getSystem() + "%7C" + medicationStatement.getIdentifier().get(0).getValue())
                    .execute();
            medicationStatement.setId(outcome.getId());
            entry.processed = true;
            entry.newId = outcome.getId().getValue();
            entry.resource = medicationStatement;
            log.info("MedicationStatement: Id="+entry.originalId+" Server Id = "+outcome.getId().getValue());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(medicationStatement, outcome, "rest", "create", AuditEventActionEnum.CREATE,"CareConnectMessagingAPI"));

        }
        return allReferenced;
    }


    private boolean processOrganisation(Organization organisation) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(organisation.getId().getValue());

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
                if (resourceList.get(h).processed && resourceList.get(h).originalId != null && organisation.getPartOf().getReference().getValue().equals(resourceList.get(h).originalId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("originalId ="+resourceList.get(h).originalId);
                    log.debug("newId ="+resourceList.get(h).newId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.info("New ReferenceId = "+resourceList.get(i).newId);
                organisation.getPartOf().setReference(resourceList.get(i).newId);
                addNewReferences(resourceList.get(i).newId);

            } else {
                if (!processed(organisation.getPartOf().getReference().getValue())) allReferenced = false;
            }
        }
        if (allReferenced) {
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) organisation.setResourceMetadata(new ResourceMetadataMap());
            MethodOutcome outcome = client.update().resource(organisation)
                    .conditionalByUrl("Organization?identifier=" + organisation.getIdentifier().get(0).getSystem() + "%7C" + organisation.getIdentifier().get(0).getValue())
                    .execute();
            organisation.setId(outcome.getId());
            entry.processed = true;
            entry.newId = outcome.getId().getValue();
            entry.resource = organisation;
            log.info("Organization: Id="+entry.originalId+" Server Id = "+outcome.getId().getValue());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(organisation, outcome, "rest", "create", AuditEventActionEnum.CREATE,"CareConnectMessagingAPI"));

        }
        return allReferenced;
    }




    private boolean processPatient(Patient patient) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(patient.getId().getValue());

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
                    if (resourceList.get(h).processed && resourceList.get(h).originalId != null && patient.getCareProvider().get(j).getReference().getValue().equals(resourceList.get(h).originalId)) {
                        referencedResource = resourceList.get(h).resource;
                        log.debug("patient.getCareProvider() originalId =" + resourceList.get(h).originalId);
                        log.debug("patient.getCareProvider() newId =" + resourceList.get(h).newId);
                        i = h;
                    }
                }
                if (referencedResource != null) {
                    log.debug("patient.getCareProvider() New ReferenceId = " + resourceList.get(i).newId);
                    patient.getCareProvider().get(j).setReference(resourceList.get(i).newId);
                    addNewReferences(resourceList.get(i).newId);
                } else {
                    if (!processed(patient.getCareProvider().get(j).getReference().getValue())) allReferenced = false;
                }
            }
        }

        if (allReferenced) {
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) patient.setResourceMetadata(new ResourceMetadataMap());

            MethodOutcome outcome = client.update().resource(patient)
                    .conditionalByUrl("Patient?identifier=" + patient.getIdentifier().get(0).getSystem() + "%7C" + patient.getIdentifier().get(0).getValue())
                    .execute();
            patient.setId(outcome.getId());
            entry.processed = true;
            entry.newId = outcome.getId().getValue();
            entry.resource = patient;

            sendToAudit(CareConnectAuditEvent.buildAuditEvent(patient, outcome, "rest", "create", AuditEventActionEnum.CREATE,"CareConnectMessagingAPI"));

            log.info("Patient: Id="+entry.originalId+" Server Id = "+outcome.getId().getValue());
        }
        return allReferenced;
    }

    private boolean processProcedureRequest(ProcedureRequest procedureRequest) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(procedureRequest.getId().getValue());

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
                if (resourceList.get(h).processed && resourceList.get(h).originalId != null && procedureRequest.getSubject().getReference().getValue().equals(resourceList.get(h).originalId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("originalId ="+resourceList.get(h).originalId);
                    log.debug("newId ="+resourceList.get(h).newId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.info("New ReferenceId = "+resourceList.get(i).newId);
                procedureRequest.getSubject().setReference(resourceList.get(i).newId);
                addNewReferences(resourceList.get(i).newId);

            } else {
                allReferenced = false;
            }
        }

        if (allReferenced)
        {

            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) procedureRequest.setResourceMetadata(new ResourceMetadataMap());

            MethodOutcome outcome = client.update().resource(procedureRequest)
                    .conditionalByUrl("ProcedureRequest?identifier=" + procedureRequest.getIdentifier().get(0).getSystem() + "%7C" + procedureRequest.getIdentifier().get(0).getValue())
                    .execute();
        //    if (outcome.getResource()!=null) {
       //         log.info("Outcome = " + parser.setPrettyPrint(true).encodeResourceToString(outcome.getResource()));
      //      }
            procedureRequest.setId(outcome.getId());
            entry.processed = true;
            entry.newId = outcome.getId().getValue();
            entry.resource = procedureRequest;
            log.info("ProcedureRequest: Id="+entry.originalId+" Server Id = "+outcome.getId().getValue());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(procedureRequest, outcome, "rest", "create", AuditEventActionEnum.CREATE,"CareConnectMessagingAPI"));

        }

        return allReferenced;
    }


    private boolean processObservation(Observation observation) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(observation.getId().getValue());

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
                if (resourceList.get(h).processed && resourceList.get(h).originalId != null && observation.getSubject().getReference().getValue().equals(resourceList.get(h).originalId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("originalId =" + resourceList.get(h).originalId);
                    log.debug("newId =" + resourceList.get(h).newId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.info("New ReferenceId = " + resourceList.get(i).newId);
                observation.getSubject().setReference(resourceList.get(i).newId);
                addNewReferences(resourceList.get(i).newId);

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
                if (resourceList.get(h).processed && resourceList.get(h).originalId != null && observation.getEncounter().getReference().getValue().equals(resourceList.get(h).originalId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("originalId =" + resourceList.get(h).originalId);
                    log.debug("newId =" + resourceList.get(h).newId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.info("New ReferenceId = " + resourceList.get(i).newId);
                observation.getEncounter().setReference(resourceList.get(i).newId);
                addNewReferences(resourceList.get(i).newId);

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
                    if (resourceList.get(h).processed && resourceList.get(h).originalId != null && observation.getPerformer().get(j).getReference().getValue().equals(resourceList.get(h).originalId)) {
                        referencedResource = resourceList.get(h).resource;
                        log.debug("originalId =" + resourceList.get(h).originalId);
                        log.debug("newId =" + resourceList.get(h).newId);
                        i = h;
                    }
                }
                if (referencedResource != null) {
                    log.info("New ReferenceId = " + resourceList.get(i).newId);
                    observation.getPerformer().get(j).setReference(resourceList.get(i).newId);
                    addNewReferences(resourceList.get(i).newId);
                } else {
                    if (!processed(observation.getPerformer().get(j).getReference().getValue()))  allReferenced = false;
                }
            }
        }


        if (allReferenced)
        {
         //   log.info("Encounter = "+parser.setPrettyPrint(true).encodeResourceToString(observation));

            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) observation.setResourceMetadata(new ResourceMetadataMap());

            MethodOutcome outcome = client.update().resource(observation)
                    .conditionalByUrl("Observation?identifier=" + observation.getIdentifier().get(0).getSystem() + "%7C" + observation.getIdentifier().get(0).getValue())
                    .execute();
          //  if (outcome.getResource()!=null) {
          //      log.info("Outcome = " + parser.setPrettyPrint(true).encodeResourceToString(outcome.getResource()));
          //  }
            observation.setId(outcome.getId());
            entry.processed = true;
            entry.resource = observation;
            entry.newId = outcome.getId().getValue();
            log.info("Observation: Id="+entry.originalId+" Server Id = "+outcome.getId().getValue());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(observation, outcome, "rest", "create", AuditEventActionEnum.CREATE,"CareConnectMessagingAPI"));

        }

        return allReferenced;
    }


    private boolean processPractitioner(Practitioner practitioner) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(practitioner.getId().getValue());
        
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

            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) practitioner.setResourceMetadata(new ResourceMetadataMap());


            MethodOutcome outcome = client.update().resource(practitioner)
                    .conditionalByUrl("Practitioner?identifier=" + practitioner.getIdentifier().get(0).getSystem() + "%7C" + practitioner.getIdentifier().get(0).getValue())
                    .execute();
            practitioner.setId(outcome.getId());
            entry.processed = true;
            entry.newId = outcome.getId().getValue();
            entry.resource = practitioner;
            log.info("Practitioner: Id="+entry.originalId+" Server Id = "+outcome.getId().getValue());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(practitioner, outcome, "rest", "create", AuditEventActionEnum.CREATE,"CareConnectMessagingAPI"));

        }
        return allReferenced;
    }

    private boolean processProcedure(Procedure procedure) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(procedure.getId().getValue());

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
                if (resourceList.get(h).processed && resourceList.get(h).originalId != null && procedure.getSubject().getReference().getValue().equals(resourceList.get(h).originalId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("originalId =" + resourceList.get(h).originalId);
                    log.debug("newId =" + resourceList.get(h).newId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.info("New ReferenceId = " + resourceList.get(i).newId);
                procedure.getSubject().setReference(resourceList.get(i).newId);
                addNewReferences(resourceList.get(i).newId);

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
                if (resourceList.get(h).processed && resourceList.get(h).originalId != null && procedure.getEncounter().getReference().getValue().equals(resourceList.get(h).originalId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("originalId =" + resourceList.get(h).originalId);
                    log.debug("newId =" + resourceList.get(h).newId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.info("New ReferenceId = " + resourceList.get(i).newId);
                procedure.getEncounter().setReference(resourceList.get(i).newId);
                addNewReferences(resourceList.get(i).newId);

            } else {
                if (!processed(procedure.getEncounter().getReference().getValue())) allReferenced = false;
            }
        }

        // bundle condition location
        if (!isNullReference(procedure.getLocation())) {
            IResource referencedResource = null;
      //      log.info(procedure.getLocation().getReference().getValue());
            int i = 0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).originalId != null && procedure.getLocation().getReference().getValue().equals(resourceList.get(h).originalId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("originalId =" + resourceList.get(h).originalId);
                    log.debug("newId =" + resourceList.get(h).newId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.info("New ReferenceId = " + resourceList.get(i).newId);
                procedure.getLocation().setReference(resourceList.get(i).newId);
                addNewReferences(resourceList.get(i).newId);

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
                    if (resourceList.get(h).processed && resourceList.get(h).originalId != null && procedure.getPerformer().get(j).getActor().getReference().getValue().equals(resourceList.get(h).originalId)) {
                        referencedResource = resourceList.get(h).resource;
                        log.debug("originalId =" + resourceList.get(h).originalId);
                        log.debug("newId =" + resourceList.get(h).newId);
                        i = h;
                    }
                }
                if (referencedResource != null) {
                    log.info("New ReferenceId = " + resourceList.get(i).newId);
                    procedure.getPerformer().get(j).getActor().setReference(resourceList.get(i).newId);
                    addNewReferences(resourceList.get(i).newId);
                } else {
                    if (!processed(procedure.getPerformer().get(j).getActor().getReference().getValue()))  allReferenced = false;
                }
            }
        }


        if (allReferenced)
        {
      //      log.info("Procedure = "+parser.setPrettyPrint(true).encodeResourceToString(procedure));

            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) procedure.setResourceMetadata(new ResourceMetadataMap());

            MethodOutcome outcome = client.update().resource(procedure)
                    .conditionalByUrl("Procedure?identifier=" + procedure.getIdentifier().get(0).getSystem() + "%7C" + procedure.getIdentifier().get(0).getValue())
                    .execute();
            //if (outcome.getResource()!=null) {
            //    log.info("Outcome = " + parser.setPrettyPrint(true).encodeResourceToString(outcome.getResource()));
           // }
            procedure.setId(outcome.getId());
            entry.processed = true;
            entry.resource = procedure;
            entry.newId = outcome.getId().getValue();
            log.info("Procedure: Id="+entry.originalId+" Server Id = "+outcome.getId().getValue());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(procedure, outcome, "rest", "create", AuditEventActionEnum.CREATE,"CareConnectMessagingAPI"));

        }


        return allReferenced;
    }


    private boolean processQuestionnaireResponse(QuestionnaireResponse questionnaireResponse) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(questionnaireResponse.getId().getValue());
        
        if (questionnaireResponse.getIdentifier()  == null) {
            questionnaireResponse.getIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(questionnaireResponse.getId().getValue());
        }

        // Patient
        if (!isNullReference(questionnaireResponse.getSubject())) {
            ResourceReferenceDt resource = questionnaireResponse.getSubject();

            log.debug(resource.getReference().getValue());
            String referencedResource = findAcutalId(resource.getReference().getValue());
            if (referencedResource != null) {
                resource.setReference(referencedResource);
            } else {
                if (!processed(resource.getReference().getValue())) allReferenced = false;
            }
        }

        // Author
        if (!isNullReference(questionnaireResponse.getAuthor())) {
            ResourceReferenceDt resource = questionnaireResponse.getAuthor();

            log.debug(resource.getReference().getValue());
            String referencedResource = findAcutalId(resource.getReference().getValue());
            if (referencedResource != null) {
                resource.setReference(referencedResource);
            } else {
                if (!processed(resource.getReference().getValue())) allReferenced = false;
            }
        }

        // Encounter
        if (!isNullReference(questionnaireResponse.getEncounter())) {
            ResourceReferenceDt resource = questionnaireResponse.getEncounter();

            log.debug(resource.getReference().getValue());
            String referencedResource = findAcutalId(resource.getReference().getValue());
            if (referencedResource != null) {
                resource.setReference(referencedResource);
            } else {
                if (!processed(resource.getReference().getValue())) allReferenced = false;
            }
        }

        if (allReferenced) {

            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) questionnaireResponse.setResourceMetadata(new ResourceMetadataMap());

            // Perform a search to look for the identifier - work around
            ca.uhn.fhir.model.api.Bundle searchBundle = client.search().forResource(QuestionnaireResponse.class)
                    .where(new StringClientParam("_content").matches().value(questionnaireResponse.getIdentifier().getValue()))
                    .prettyPrint()
                    .encodedJson().execute();
            // Not found so add the resource
            if (searchBundle.getEntries().size()==0) {
                IIdType id = client.create().resource(questionnaireResponse).execute().getId();
                questionnaireResponse.setId(id);
                entry.processed = true;
                entry.newId = id.getValue();
                entry.resource = questionnaireResponse;
                log.info("QuestionaireResponse: Id="+entry.originalId+" Server Id = "+id.getValue());
                sendToAudit(CareConnectAuditEvent.buildAuditEvent(questionnaireResponse, null, "rest", "create", AuditEventActionEnum.CREATE,"CareConnectMessagingAPI"));

            }

        }
        return allReferenced;
    }

    private boolean processReferralRequest(ReferralRequest referralRequest) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(referralRequest.getId().getValue());
        
        if (referralRequest.getIdentifier().size()  == 0) {
            referralRequest.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(referralRequest.getId().getValue());
        }

        // Patient
        if (!isNullReference(referralRequest.getPatient())) {
            ResourceReferenceDt resource = referralRequest.getPatient();

            log.debug(resource.getReference().getValue());
            String referencedResource = findAcutalId(resource.getReference().getValue());
            if (referencedResource != null) {
                resource.setReference(referencedResource);
            } else {
                if (!processed(resource.getReference().getValue())) allReferenced = false;
            }
        }

        // Requester
        if (!isNullReference(referralRequest.getRequester())) {
            ResourceReferenceDt resource = referralRequest.getRequester();

            log.debug(resource.getReference().getValue());
            String referencedResource = findAcutalId(resource.getReference().getValue());
            if (referencedResource != null) {
                resource.setReference(referencedResource);
            } else {
                if (!processed(resource.getReference().getValue())) allReferenced = false;
            }
        }
        // Recipient
        for (ResourceReferenceDt resource : referralRequest.getRecipient()) {

            log.debug(resource.getReference().getValue());
            String referencedResource = findAcutalId(resource.getReference().getValue());
            if (referencedResource != null) {
                resource.setReference(referencedResource);
            } else {
                if (!processed(resource.getReference().getValue())) allReferenced = false;
            }
        }

        // Encounter
        if (!isNullReference(referralRequest.getEncounter())) {
            ResourceReferenceDt resource = referralRequest.getEncounter();

            log.debug(resource.getReference().getValue());
            String referencedResource = findAcutalId(resource.getReference().getValue());
            if (referencedResource != null) {
                resource.setReference(referencedResource);
            } else {
                if (!processed(resource.getReference().getValue())) allReferenced = false;
            }
        }

        // Supporting information
        for (ResourceReferenceDt resource : referralRequest.getSupportingInformation()) {

            log.debug(resource.getReference().getValue());
            String referencedResource = findAcutalId(resource.getReference().getValue());
            if (referencedResource != null) {
                resource.setReference(referencedResource);
            } else {
                if (!processed(resource.getReference().getValue())) allReferenced = false;
            }
        }

        if (allReferenced) {
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) referralRequest.setResourceMetadata(new ResourceMetadataMap());


            MethodOutcome outcome = client.update().resource(referralRequest)
                    .conditionalByUrl("ReferralRequest?identifier=" + referralRequest.getIdentifier().get(0).getSystem() + "%7C" + referralRequest.getIdentifier().get(0).getValue())
                    .execute();

            referralRequest.setId(outcome.getId());
            entry.processed = true;
            entry.resource = referralRequest;
            entry.newId = outcome.getId().getValue();
            log.info("ReferralRequest: Id="+entry.originalId+" Server Id = "+outcome.getId().getValue());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(referralRequest, outcome, "rest", "create", AuditEventActionEnum.CREATE,"CareConnectMessagingAPI"));


        }
        return allReferenced;
    }

    private void processBundle(Bundle bundle)
    {

        sendToAudit(CareConnectAuditEvent.buildAuditEvent(bundle, null, bundle.getType(), "create", AuditEventActionEnum.CREATE,"CareConnectMessagingAPI"));

        for (Bundle.Entry entry : bundle.getEntry()) {
            IResource resource = entry.getResource();
            EntryProcessing resourceP = new EntryProcessing();
            resourceP.originalId = resource.getId().getValue();
            resourceP.processed = false;
            resourceP.resource = resource;
            // Miss off MessageHeader, so start at 1 but also processing Composition now
            // if (f == 0) resourceP.processed = true;
            resourceList.add(resourceP);
        }
        // Loop 3 times now to, need to have time out but ensure all resources posted
        parser = ctx.newXmlParser();
        Boolean allProcessed;

        // Process subbundles first - this should be documents.

        for (Bundle.Entry entry : bundle.getEntry()) {
            IResource resource = entry.getResource();

            if (entry.getResource().getResourceName().equals("Bundle")) {
                log.info("Another Bundle Found at entry "+entry.getResource().getId().getValue());
                processBundle((Bundle) resource);
                log.info("Returned from sub Bundle call at entry "+entry.getResource().getId().getValue());
            }
        }

        for (int g= 0;g < 5;g++) {
            allProcessed = true;
            for (Bundle.Entry entry : bundle.getEntry()) {

                IResource resource = entry.getResource();
                log.info("---- Looking for "+resource.getResourceName()+" resource with id of = "+resource.getId().getValue());
                EntryProcessing entryProcess = findEntryProcess(resource.getId().getValue());

                if (entryProcess != null && !entryProcess.processed) {

                    //log.info("Entry "+entryProcess.resource.getResourceName() + " Id "+ entryProcess.originalId + "Processed="+entryProcess.processed  );
                    //if (entryProcess.newId != null) log.info(" New Id "+entryProcess.newId);

                    // Bundle.location
                    if (resource.getResourceName().equals("Location")) {
                        processLocation((Location) resource);
                    }

                    // Bundle.Organization
                    if (resource.getResourceName().equals("Organization")) {
                        processOrganisation( (Organization) resource);
                    }

                    //Bundle.Practitioner
                    if (resource.getResourceName().equals("Practitioner")) {
                        processPractitioner( (Practitioner) resource);
                    }

                    // Bundle. Patient
                    if (resource.getResourceName().equals("Patient")) {
                        processPatient((Patient) resource);
                    }

                    // Bundle.Procedure Request
                    if (resource.getResourceName().equals("ProcedureRequest")) {
                        processProcedureRequest((ProcedureRequest) resource);
                    }

                    // bundle.Appointment
                    if (resource.getResourceName().equals("Appointment")) {
                        processAppointment((Appointment) resource);
                    }

                    // Bundle.Observation
                    if (resource.getResourceName().equals("Observation")) {
                        processObservation((Observation) resource);
                    }

                    // Bundle.Encounter
                    if (resource.getResourceName().equals("Encounter")) {
                        processEncounter( (Encounter) resource);
                    }

                    // Bundle.Condition
                    if (resource.getResourceName().equals("Condition")) {
                        processCondition( (Condition) resource);
                    }

                    // Bundle.Procedure
                    if (resource.getResourceName().equals("Procedure")) {
                        processProcedure( (Procedure) resource);
                    }

                    // Bundle.MedicationStatement
                    if (resource.getResourceName().equals("MedicationStatement")) {
                        processMedicationStatement( (MedicationStatement) resource);
                    }

                    // Bundle.Medication
                    if (resource.getResourceName().equals("Medication")) {
                        processMedication( (Medication) resource);
                    }


                    // Bundle.Flag
                    if (resource.getResourceName().equals("Flag")) {
                        processFlag( (Flag) resource);
                    }

                    // Bundle.Composition
                    if (resource.getResourceName().equals("Composition")) {
                        processComposition( (Composition) resource);
                    }

                    // Bundle.List
                    if (resource.getResourceName().equals("List")) {
                        processList( (ListResource) resource);
                    }

                    // Bundle.AllergyIntolernace
                    if (resource.getResourceName().equals("AllergyIntolerance")) {
                        processAllergyIntolerance( (AllergyIntolerance) resource);
                    }
                    // Bundle.QuestionaireResponse
                    if (resource.getResourceName().equals("QuestionnaireResponse")) {
                        processQuestionnaireResponse( (QuestionnaireResponse) resource);
                    }

                    // Bundle.QuestionaireResponse
                    if (resource.getResourceName().equals("ReferralRequest")) {
                        processReferralRequest( (ReferralRequest) resource);
                    }

                }

            }
        }
    }


    @Override
    public void process(Exchange exchange) throws Exception {
        Bundle bundle = null;

        Reader reader = new InputStreamReader(new ByteArrayInputStream((byte[]) exchange.getIn().getBody(byte[].class)));

        JSONparser = ctx.newJsonParser();

        if (exchange.getIn().getHeader(Exchange.CONTENT_TYPE).toString().contains("json")) {
            //JsonParser parser = new JsonParser();
            IParser parser = JSONparser; // reuse the JSONParser
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
        resourceList = new ArrayList<EntryProcessing>();

        newReferences = new ArrayList<String>();

        processBundle(bundle);

    }
}
