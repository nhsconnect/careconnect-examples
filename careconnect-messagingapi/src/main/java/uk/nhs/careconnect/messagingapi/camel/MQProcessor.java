package uk.nhs.careconnect.messagingapi.camel;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.hl7.fhir.instance.model.*;
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
        Resource resource;
        Boolean processed;
        String originalId;
        String newId;
    }

    private boolean isNullReference(Reference reference) {
        //log.info("IsNullReference getReference="+reference.getReference());
        //log.info("IsNullReference getId="+reference.getId());
        if (reference == null ) return true;
        if (reference.getReference() == null) {
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
            compare = compare.replace(entry.resource.getResourceType() +"/","");
            log.info("Comparing to (modified) " + compare);
            if (referenceId.equals(compare) && newId == null) {

                if (entry.processed && entry.originalId != null) {
                    newId = entry.newId;
                    addNewReferences(newId);
                    log.info("### Found "+ entry.resource.getResourceType());
                }
                else
                {
                    log.info("*** Not mapped yet "+ entry.resource.getResourceType());
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
        EntryProcessing entry = findEntryProcess(allergyIntolerance.getId());
        
        if (allergyIntolerance.getIdentifier().size() == 0 ) {
            allergyIntolerance.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(allergyIntolerance.getId());
        }

        // Compostion Patient
        if (!isNullReference(allergyIntolerance.getPatient())) {

            log.debug(allergyIntolerance.getPatient().getReference());
            String referencedResource = findAcutalId(allergyIntolerance.getPatient().getReference());
            if (referencedResource != null) {
                allergyIntolerance.getPatient().setReference(referencedResource);
            } else {
                if (!processed(allergyIntolerance.getPatient().getReference())) allReferenced = false;
            }
        }

        if (allReferenced) {
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) allergyIntolerance.setMeta(new Meta());

            MethodOutcome outcome = client.update().resource(allergyIntolerance)
                    .conditionalByUrl("AllergyIntolerance?identifier=" + allergyIntolerance.getIdentifier().get(0).getSystem() + "%7C" + allergyIntolerance.getIdentifier().get(0).getValue())
                    .execute();
            allergyIntolerance.setId(outcome.getId());
            entry.processed = true;
            entry.newId = outcome.getId().toString();
            entry.resource = allergyIntolerance;
            log.info("AllergyIntolerabce: Id="+entry.originalId+" Server Id = "+outcome.getId());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(allergyIntolerance, outcome, "rest", "create", AuditEvent.AuditEventAction.C,"CareConnectMessagingAPI"));

        }
        return allReferenced;
    }

    /*

     APPOINTMENT

     */
    private boolean processAppointment(Appointment appointment) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(appointment.getId());

        if (appointment.getIdentifier().size() == 0) {
            appointment.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(appointment.getId());
        }

        // Appointment.Participants
        for (Appointment.AppointmentParticipantComponent participant : appointment.getParticipant()) {
            Reference reference  = participant.getActor();
            if (!isNullReference(reference)) {
                String referencedResource = findAcutalId(reference.getReference());
                if (referencedResource != null) {
                    reference.setReference(referencedResource);
                } else {
                    if (!processed(reference.getReference())) allReferenced = false;
                }
            }
        }

        if (allReferenced)
        {
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) appointment.setMeta(new Meta());

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
            entry.newId = outcome.getId().toString();
            log.info("Appointment: Id="+entry.originalId+" Server Id = "+outcome.getId());
        }

        return allReferenced;
    }

    private boolean processComposition(Composition composition) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(composition.getId());
        
        if (composition.getIdentifier() !=null) {
            composition.getIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(composition.getId());
        }

        // Compostion Patient
        if (!isNullReference(composition.getSubject())) {

            log.debug(composition.getSubject().getReference());
            String referencedResource = findAcutalId(composition.getSubject().getReference());
            if (referencedResource != null) {

                composition.getSubject().setReference(referencedResource);

            } else {
                if (!processed(composition.getSubject().getReference())) allReferenced = false;
            }
        }

        // Composition Encounter
        if (!isNullReference(composition.getEncounter())) {

            log.debug(composition.getEncounter().getReference());
            String referencedResource = findAcutalId(composition.getEncounter().getReference());
            if (referencedResource != null) {
                composition.getEncounter().setReference(referencedResource);
            } else {
                if (!processed(composition.getEncounter().getReference())) allReferenced = false;
            }
        }

        // Composition Custodian
        if (!isNullReference(composition.getCustodian())) {

            log.debug(composition.getCustodian().getReference());
            String referencedResource = findAcutalId(composition.getCustodian().getReference());

            if (referencedResource != null) {
                composition.getCustodian().setReference(referencedResource);
            } else {
                if (!processed(composition.getCustodian().getReference())) allReferenced = false;
            }
        }


        // Composition Author
        for(Reference reference: composition.getAuthor()) {
            if (!isNullReference(reference)) {
                String referencedResource = findAcutalId(reference.getReference());

                if (referencedResource != null) {
                    reference.setReference(referencedResource);
                } else {
                    if (!processed(reference.getReference())) allReferenced = false;
                }
            }
        }


        // Section entries
        for (Composition.SectionComponent section :composition.getSection()) {
            for(Reference reference: section.getEntry()) {
                if (!isNullReference(reference)) {
                    String referencedResource = findAcutalId(reference.getReference());

                    if (referencedResource != null) {
                        reference.setReference(referencedResource);
                    } else {
                        if (!processed(reference.getReference())) allReferenced = false;
                    }
                }
            }
        }



        if (allReferenced) {
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) composition.setMeta(new Meta());

            MethodOutcome outcome = client.update().resource(composition)
                    .conditionalByUrl("Composition?identifier=" + composition.getIdentifier().getSystem() + "%7C" + composition.getIdentifier().getValue())
                    .execute();
            composition.setId(outcome.getId());
            entry.processed = true;
            entry.newId = outcome.getId().toString();
            entry.resource = composition;
            log.info("Composition: Id="+entry.originalId+" Server Id = "+outcome.getId());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(composition, outcome, "rest", "create", AuditEvent.AuditEventAction.C,"CareConnectMessagingAPI"));

        }
        return allReferenced;
    }

    private boolean processCondition(Condition condition) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(condition.getId());

        // Having an identifier makes this a lot easier.
        if (condition.getIdentifier().size() == 0) {
            condition.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(condition.getId());
        }

        Reference reference = condition.getPatient();
        if (!isNullReference(reference)) {
            String referencedResource = findAcutalId(reference.getReference());
            if (referencedResource != null) {
                reference.setReference(referencedResource);
            } else {
                if (!processed(reference.getReference())) allReferenced = false;
            }
        }

        // bundle condition encounter
        reference = condition.getEncounter();
        if (!isNullReference(reference)) {
            String referencedResource = findAcutalId(reference.getReference());
            if (referencedResource != null) {
                reference.setReference(referencedResource);
            } else {
                if (!processed(reference.getReference())) allReferenced = false;
            }
        }



        // Bundle.Condition asserter
        reference = condition.getAsserter();
        if (!isNullReference(reference)) {
            String referencedResource = findAcutalId(reference.getReference());
            if (referencedResource != null) {
                reference.setReference(referencedResource);
            } else {
                if (!processed(reference.getReference())) allReferenced = false;
            }
        }


        if (allReferenced)
        {
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) condition.setMeta(new Meta());

            // log.info("Condition= "+parser.setPrettyPrint(true).encodeResourceToString(condition));
            MethodOutcome outcome = client.update().resource(condition)
                    .conditionalByUrl("Condition?identifier=" + condition.getIdentifier().get(0).getSystem() + "%7C" + condition.getIdentifier().get(0).getValue())
                    .execute();
            condition.setId(outcome.getId());
            entry.processed = true;
            entry.resource = condition;
            entry.newId = outcome.getId().toString();
            log.info("Condition: Id="+entry.originalId+" Server Id = "+outcome.getId());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(condition, outcome, "rest", "create", AuditEvent.AuditEventAction.C,"CareConnectMessagingAPI"));

        }

        return allReferenced;
    }


    private boolean processEncounter( Encounter encounter) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(encounter.getId());

        if (encounter.getIdentifier().size() == 0) {
            encounter.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(encounter.getId());
        }

        // Bundle.Encounter Patient
        Reference reference = encounter.getPatient();
        if (!isNullReference(reference)) {
            String referencedResource = findAcutalId(reference.getReference());
            if (referencedResource != null) {
                reference.setReference(referencedResource);
            } else {
                if (!processed(reference.getReference())) allReferenced = false;
            }
        }

        // Bundle.EncounterAppointments
        reference = encounter.getAppointment();
        if (!isNullReference(reference)) {
            String referencedResource = findAcutalId(reference.getReference());
            if (referencedResource != null) {
                reference.setReference(referencedResource);
            } else {
                if (!processed(reference.getReference())) allReferenced = false;
            }
        }



        // Bundle.Encounter Service Provider
        reference = encounter.getServiceProvider();
        if (!isNullReference(reference)) {
            String referencedResource = findAcutalId(reference.getReference());
            if (referencedResource != null) {
                reference.setReference(referencedResource);
            } else {
                if (!processed(reference.getReference())) allReferenced = false;
            }
        }


        // Bundle.Encounter Participants
        for (Encounter.EncounterParticipantComponent participant : encounter.getParticipant()) {
            reference = participant.getIndividual();
            if (!isNullReference(reference)) {
                String referencedResource = findAcutalId(reference.getReference());
                if (referencedResource != null) {
                    reference.setReference(referencedResource);
                } else {
                    if (!processed(reference.getReference())) allReferenced = false;
                }
            }
        }

        // Bundle.Encounter Locations
        for (Encounter.EncounterLocationComponent location : encounter.getLocation()) {
            reference = location.getLocation();
            if (!isNullReference(reference)) {
                String referencedResource = findAcutalId(reference.getReference());
                if (referencedResource != null) {
                    reference.setReference(referencedResource);
                } else {
                    if (!processed(reference.getReference())) allReferenced = false;
                }
            }
        }




        if (allReferenced)
        {
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) encounter.setMeta(new Meta());

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
            entry.newId = outcome.getId().toString();
            log.info("Encounter: Id="+entry.originalId+" Server Id = "+outcome.getId());

            sendToAudit(CareConnectAuditEvent.buildAuditEvent(encounter, outcome, "rest", "create", AuditEvent.AuditEventAction.C,"CareConnectMessagingAPI"));

        }


        return allReferenced;
    }

    private boolean processFlag(Flag flag) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(flag.getId());

        if (flag.getIdentifier().size() == 0) {
            flag.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(flag.getId());
        }

        // Bundle.Observation Patient
        if (!isNullReference(flag.getSubject())) {

            log.debug(flag.getSubject().getReference());
            String referencedResource = findAcutalId(flag.getSubject().getReference());

            if (referencedResource != null) {
                flag.getSubject().setReference(referencedResource);
            } else {
                if (!processed(flag.getSubject().getReference())) allReferenced = false;
            }
        }

        if (allReferenced) {
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) flag.setMeta(new Meta());


            // Perform a search to look for the identifier - work around
            Bundle searchBundle = client.search().forResource(Flag.class)
                    .where(new StringClientParam("_content").matches().value(flag.getIdentifier().get(0).getValue()))
                    .prettyPrint()
                    .encodedJson()
                    .returnBundle(Bundle.class)
                    .execute();
            // Not found so add the resource
            if (searchBundle.getEntry().size()==0) {
                IIdType id = client.create().resource(flag).execute().getId();
                flag.setId(id);
                entry.processed = true;
                entry.newId = id.getValue();
                entry.resource = flag;
                log.info("Flag: Id="+entry.originalId+" Server Id = "+id.getValue());
                sendToAudit(CareConnectAuditEvent.buildAuditEvent(flag, null, "rest", "create", AuditEvent.AuditEventAction.C,"CareConnectMessagingAPI"));

            }

        }
        return allReferenced;
    }


    private boolean processList(List_ list) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(list.getId());
        
        if (list.getIdentifier().size() == 0) {
            list.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(list.getId());
        }

        // Compostion Patient
        if (!isNullReference(list.getSubject())) {

            log.debug(list.getSubject().getReference());
            String referencedResource = findAcutalId(list.getSubject().getReference());
            if (referencedResource != null) {

                list.getSubject().setReference(referencedResource);

            } else {
                if (!processed(list.getSubject().getReference())) allReferenced = false;
            }
        }

        // List Entries
        for(List_.ListEntryComponent listentry: list.getEntry()) {
            Reference reference = listentry.getItem();
            if (!isNullReference(reference)) {
                String referencedResource = findAcutalId(reference.getReference());

                if (referencedResource != null) {
                    reference.setReference(referencedResource);
                } else {
                    if (!processed(reference.getReference())) allReferenced = false;
                }
            }
        }

        if (allReferenced) {
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) list.setMeta(new Meta());

            // Perform a search to look for the identifier - work around
            Bundle searchBundle = client.search().forResource(List_.class)
                    .where(new StringClientParam("_content").matches().value(list.getIdentifier().get(0).getValue()))
                    .prettyPrint()
                    .returnBundle(Bundle.class)
                    .encodedJson().execute();
            // Not found so add the resource
            if (searchBundle.getEntry().size()==0) {
                IIdType id = client.create().resource(list).execute().getId();
                list.setId(id);
                entry.processed = true;
                entry.newId = id.getValue();
                entry.resource = list;
                log.info("List: Id="+entry.originalId+" Server Id = "+id.getValue());
                sendToAudit(CareConnectAuditEvent.buildAuditEvent(list, null, "rest", "create", AuditEvent.AuditEventAction.C,"CareConnectMessagingAPI"));

            }

        }
        return allReferenced;
    }



    private boolean processLocation(Location location) {

        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(location.getId());

        if (location.getIdentifier().size() == 0) {
            location.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(location.getId());
        }

        if (!isNullReference(location.getManagingOrganization()))  {
            Reference reference = location.getManagingOrganization();
            if (!isNullReference(reference)) {
                String referencedResource = findAcutalId(reference.getReference());

                if (referencedResource != null) {
                    reference.setReference(referencedResource);
                } else {
                    if (!processed(reference.getReference())) allReferenced = false;
                }
            }
        }

        if (allReferenced) {

            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) location.setMeta(new Meta());

            MethodOutcome outcome = client.update().resource(location)
                    .conditionalByUrl("Location?identifier=" + location.getIdentifier().get(0).getSystem() + "%7C" + location.getIdentifier().get(0).getValue())
                    .execute();
            location.setId(outcome.getId());
            
            entry.processed = true;
            entry.newId = outcome.getId().toString();
            entry.resource = location;
            log.info("Location: Id="+entry.originalId+" Server Id = "+outcome.getId());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(location, outcome, "rest", "create", AuditEvent.AuditEventAction.C,"CareConnectMessagingAPI"));

        }
        return allReferenced;
    }

    private boolean processMedication(Medication medication) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(medication.getId());
        
        if (medication.getCode().getCoding().size() == 0) {
            medication.getCode().addCoding()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setCode(medication.getId());
        }

        if (allReferenced) {

            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) medication.setMeta(new Meta());

            MethodOutcome outcome = client.update().resource(medication)
                    .conditionalByUrl("Medication?code=" + medication.getCode().getCoding().get(0).getSystem() + "%7C" + medication.getCode().getCoding().get(0).getCode())
                    .execute();
            medication.setId(outcome.getId());
            entry.processed = true;
            entry.newId = outcome.getId().toString();
            entry.resource = medication;
            log.info("Medication: Id="+entry.originalId+" Server Id = "+outcome.getId());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(medication, outcome, "rest", "create", AuditEvent.AuditEventAction.C,"CareConnectMessagingAPI"));

        }
        return allReferenced;
    }

    private boolean processMedicationStatement( MedicationStatement medicationStatement) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(medicationStatement.getId());
        
        if (medicationStatement.getIdentifier().size() == 0) {
            medicationStatement.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(medicationStatement.getId());
        }


        if (!isNullReference(medicationStatement.getPatient())) {
            Resource referencedResource = null;
            log.debug(medicationStatement.getPatient().getReference());
            int i = 0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).originalId != null && medicationStatement.getPatient().getReference().equals(resourceList.get(h).originalId)) {
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
                if (!processed(medicationStatement.getPatient().getReference())) allReferenced = false;
            }
        }

        if (medicationStatement.getMedication() instanceof Reference) {
            if (!isNullReference((Reference) medicationStatement.getMedication())) {
                Resource referencedResource = null;
                log.debug(((Reference) medicationStatement.getMedication()).getReference());
                int i = 0;
                for (int h = 0; h < resourceList.size(); h++) {
                    if (resourceList.get(h).processed && resourceList.get(h).originalId != null && ((Reference) medicationStatement.getMedication()).getReference().equals(resourceList.get(h).originalId)) {
                        referencedResource = resourceList.get(h).resource;
                        log.debug("originalId =" + resourceList.get(h).originalId);
                        log.debug("newId =" + resourceList.get(h).newId);
                        i = h;
                    }
                }
                if (referencedResource != null) {
                    log.info("New ReferenceId = " + resourceList.get(i).newId);
                    ((Reference) medicationStatement.getMedication()).setReference(resourceList.get(i).newId);
                    addNewReferences(resourceList.get(i).newId);

                } else {
                    if (!processed(medicationStatement.getPatient().getReference())) allReferenced = false;
                }
            }
        }

        if (allReferenced) {
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) medicationStatement.setMeta(new Meta());

            MethodOutcome outcome = client.update().resource(medicationStatement)
                    .conditionalByUrl("MedicationStatement?identifier=" + medicationStatement.getIdentifier().get(0).getSystem() + "%7C" + medicationStatement.getIdentifier().get(0).getValue())
                    .execute();
            medicationStatement.setId(outcome.getId());
            entry.processed = true;
            entry.newId = outcome.getId().toString();
            entry.resource = medicationStatement;
            log.info("MedicationStatement: Id="+entry.originalId+" Server Id = "+outcome.getId());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(medicationStatement, outcome, "rest", "create", AuditEvent.AuditEventAction.C,"CareConnectMessagingAPI"));

        }
        return allReferenced;
    }


    private boolean processOrganisation(Organization organisation) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(organisation.getId());

        if (organisation.getIdentifier().size() == 0) {
            organisation.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(organisation.getId());
        }

        if (!isNullReference(organisation.getPartOf()) && !organisation.getPartOf().getReference().isEmpty()) {
            Resource referencedResource = null;
            log.debug("organisation.getPartOf()="+organisation.getPartOf().getReference());
            int i =0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).originalId != null && organisation.getPartOf().getReference().equals(resourceList.get(h).originalId)) {
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
                if (!processed(organisation.getPartOf().getReference())) allReferenced = false;
            }
        }
        if (allReferenced) {
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) organisation.setMeta(new Meta());
            MethodOutcome outcome = client.update().resource(organisation)
                    .conditionalByUrl("Organization?identifier=" + organisation.getIdentifier().get(0).getSystem() + "%7C" + organisation.getIdentifier().get(0).getValue())
                    .execute();
            organisation.setId(outcome.getId());
            entry.processed = true;
            entry.newId = outcome.getId().toString();
            entry.resource = organisation;
            log.info("Organization: Id="+entry.originalId+" Server Id = "+outcome.getId());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(organisation, outcome, "rest", "create", AuditEvent.AuditEventAction.C,"CareConnectMessagingAPI"));

        }
        return allReferenced;
    }




    private boolean processPatient(Patient patient) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(patient.getId());

        if (patient.getIdentifier().size() == 0) {
            patient.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(patient.getId());
        }

        for (int j = 0; j < patient.getCareProvider().size(); j++) {
            Resource referencedResource = null;
            log.info(patient.getCareProvider().get(j).getReference());
            if (!isNullReference(patient.getCareProvider().get(j))) {
                int i = 0;
                for (int h = 0; h < resourceList.size(); h++) {
                    if (resourceList.get(h).processed && resourceList.get(h).originalId != null && patient.getCareProvider().get(j).getReference().equals(resourceList.get(h).originalId)) {
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
                    if (!processed(patient.getCareProvider().get(j).getReference())) allReferenced = false;
                }
            }
        }

        if (allReferenced) {
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) patient.setMeta(new Meta());

            MethodOutcome outcome = client.update().resource(patient)
                    .conditionalByUrl("Patient?identifier=" + patient.getIdentifier().get(0).getSystem() + "%7C" + patient.getIdentifier().get(0).getValue())
                    .execute();
            patient.setId(outcome.getId());
            entry.processed = true;
            entry.newId = outcome.getId().toString();
            entry.resource = patient;

            sendToAudit(CareConnectAuditEvent.buildAuditEvent(patient, outcome, "rest", "create", AuditEvent.AuditEventAction.C,"CareConnectMessagingAPI"));

            log.info("Patient: Id="+entry.originalId+" Server Id = "+outcome.getId());
        }
        return allReferenced;
    }

    private boolean processProcedureRequest(ProcedureRequest procedureRequest) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(procedureRequest.getId());

        if (procedureRequest.getIdentifier().size() == 0) {
            procedureRequest.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(procedureRequest.getId());
        }

        if (!isNullReference(procedureRequest.getSubject())) {
            Resource referencedResource = null;
            log.info(procedureRequest.getSubject().getReference());
            int i =0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).originalId != null && procedureRequest.getSubject().getReference().equals(resourceList.get(h).originalId)) {
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
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) procedureRequest.setMeta(new Meta());

            MethodOutcome outcome = client.update().resource(procedureRequest)
                    .conditionalByUrl("ProcedureRequest?identifier=" + procedureRequest.getIdentifier().get(0).getSystem() + "%7C" + procedureRequest.getIdentifier().get(0).getValue())
                    .execute();
        //    if (outcome.getResource()!=null) {
       //         log.info("Outcome = " + parser.setPrettyPrint(true).encodeResourceToString(outcome.getResource()));
      //      }
            procedureRequest.setId(outcome.getId());
            entry.processed = true;
            entry.newId = outcome.getId().toString();
            entry.resource = procedureRequest;
            log.info("ProcedureRequest: Id="+entry.originalId+" Server Id = "+outcome.getId());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(procedureRequest, outcome, "rest", "create", AuditEvent.AuditEventAction.C,"CareConnectMessagingAPI"));

        }

        return allReferenced;
    }


    private boolean processObservation(Observation observation) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(observation.getId());

        // Having an identifier makes this a lot easier.
        if (observation.getIdentifier().size() == 0) {
            observation.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(observation.getId());
        }


        // Bundle.Observation Patient
        if (!isNullReference(observation.getSubject())) {
            Resource referencedResource = null;
            log.info(observation.getSubject().getReference());
            int i = 0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).originalId != null && observation.getSubject().getReference().equals(resourceList.get(h).originalId)) {
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
                if (!processed(observation.getSubject().getReference())) allReferenced = false;
            }
        }
        // bundle observation encounter
        if (!isNullReference(observation.getEncounter())) {
            Resource referencedResource = null;
            log.info(observation.getEncounter().getReference());
            int i = 0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).originalId != null && observation.getEncounter().getReference().equals(resourceList.get(h).originalId)) {
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
                if (!processed(observation.getEncounter().getReference())) allReferenced = false;
            }
        }


        // Bundle.Observation performer
        for (int j = 0; j < observation.getPerformer().size(); j++) {
            Resource referencedResource = null;
            log.info(observation.getPerformer().get(j).getReference());
            if (!isNullReference(observation.getPerformer().get(j))) {
                int i = 0;
                for (int h = 0; h < resourceList.size(); h++) {
                    if (resourceList.get(h).processed && resourceList.get(h).originalId != null && observation.getPerformer().get(j).getReference().equals(resourceList.get(h).originalId)) {
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
                    if (!processed(observation.getPerformer().get(j).getReference()))  allReferenced = false;
                }
            }
        }


        if (allReferenced)
        {
         //   log.info("Encounter = "+parser.setPrettyPrint(true).encodeResourceToString(observation));

            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) observation.setMeta(new Meta());

            MethodOutcome outcome = client.update().resource(observation)
                    .conditionalByUrl("Observation?identifier=" + observation.getIdentifier().get(0).getSystem() + "%7C" + observation.getIdentifier().get(0).getValue())
                    .execute();
          //  if (outcome.getResource()!=null) {
          //      log.info("Outcome = " + parser.setPrettyPrint(true).encodeResourceToString(outcome.getResource()));
          //  }
            observation.setId(outcome.getId());
            entry.processed = true;
            entry.resource = observation;
            entry.newId = outcome.getId().toString();
            log.info("Observation: Id="+entry.originalId+" Server Id = "+outcome.getId());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(observation, outcome, "rest", "create", AuditEvent.AuditEventAction.C,"CareConnectMessagingAPI"));

        }

        return allReferenced;
    }


    private boolean processPractitioner(Practitioner practitioner) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(practitioner.getId());
        
        if (practitioner.getIdentifier().size() == 0) {
            practitioner.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(practitioner.getId());
        }

        // Practioner role organisation
        for (Practitioner.PractitionerPractitionerRoleComponent role : practitioner.getPractitionerRole()) {
            if (!isNullReference(role.getManagingOrganization())) {
                log.info(role.getManagingOrganization().getReference());

                String referencedResource = findAcutalId(role.getManagingOrganization().getReference());
                if (referencedResource != null) {
                    role.getManagingOrganization().setReference(referencedResource);
                } else {
                    if (!processed(role.getManagingOrganization().getReference())) allReferenced = false;
                }
            }
        }

        if (allReferenced) {

            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) practitioner.setMeta(new Meta());


            MethodOutcome outcome = client.update().resource(practitioner)
                    .conditionalByUrl("Practitioner?identifier=" + practitioner.getIdentifier().get(0).getSystem() + "%7C" + practitioner.getIdentifier().get(0).getValue())
                    .execute();
            practitioner.setId(outcome.getId());
            entry.processed = true;
            entry.newId = outcome.getId().toString();
            entry.resource = practitioner;
            log.info("Practitioner: Id="+entry.originalId+" Server Id = "+outcome.getId());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(practitioner, outcome, "rest", "create", AuditEvent.AuditEventAction.C,"CareConnectMessagingAPI"));

        }
        return allReferenced;
    }

    private boolean processProcedure(Procedure procedure) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(procedure.getId());

        // Having an identifier makes this a lot easier.
        if (procedure.getIdentifier().size() == 0) {
            procedure.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(procedure.getId());
        }

        // Bundle.COndition Patient
        if (!isNullReference(procedure.getSubject())) {
            Resource referencedResource = null;
            log.info(procedure.getSubject().getReference());
            int i = 0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).originalId != null && procedure.getSubject().getReference().equals(resourceList.get(h).originalId)) {
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
                if (!processed(procedure.getSubject().getReference())) allReferenced = false;
            }
        }
        // bundle condition encounter
        if (!isNullReference(procedure.getEncounter())) {
            Resource referencedResource = null;
            log.info(procedure.getEncounter().getReference());
            int i = 0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).originalId != null && procedure.getEncounter().getReference().equals(resourceList.get(h).originalId)) {
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
                if (!processed(procedure.getEncounter().getReference())) allReferenced = false;
            }
        }

        // bundle condition location
        if (!isNullReference(procedure.getLocation())) {
            Resource referencedResource = null;
      //      log.info(procedure.getLocation().getReference());
            int i = 0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).originalId != null && procedure.getLocation().getReference().equals(resourceList.get(h).originalId)) {
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
                if (!processed(procedure.getLocation().getReference())) allReferenced = false;
            }
        }


        // Bundle.Procedure Performer
        for (int j = 0; j < procedure.getPerformer().size(); j++) {
            Resource referencedResource = null;
            log.info(procedure.getPerformer().get(j).getActor().getReference());
            if (!isNullReference(procedure.getPerformer().get(j).getActor() )) {
                int i = 0;
                for (int h = 0; h < resourceList.size(); h++) {
                    if (resourceList.get(h).processed && resourceList.get(h).originalId != null && procedure.getPerformer().get(j).getActor().getReference().equals(resourceList.get(h).originalId)) {
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
                    if (!processed(procedure.getPerformer().get(j).getActor().getReference()))  allReferenced = false;
                }
            }
        }


        if (allReferenced)
        {
      //      log.info("Procedure = "+parser.setPrettyPrint(true).encodeResourceToString(procedure));

            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) procedure.setMeta(new Meta());

            MethodOutcome outcome = client.update().resource(procedure)
                    .conditionalByUrl("Procedure?identifier=" + procedure.getIdentifier().get(0).getSystem() + "%7C" + procedure.getIdentifier().get(0).getValue())
                    .execute();
            //if (outcome.getResource()!=null) {
            //    log.info("Outcome = " + parser.setPrettyPrint(true).encodeResourceToString(outcome.getResource()));
           // }
            procedure.setId(outcome.getId());
            entry.processed = true;
            entry.resource = procedure;
            entry.newId = outcome.getId().toString();
            log.info("Procedure: Id="+entry.originalId+" Server Id = "+outcome.getId());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(procedure, outcome, "rest", "create", AuditEvent.AuditEventAction.C,"CareConnectMessagingAPI"));

        }


        return allReferenced;
    }


    private boolean processQuestionnaireResponse(QuestionnaireResponse questionnaireResponse) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(questionnaireResponse.getId());
        
        if (questionnaireResponse.getIdentifier()  == null) {
            questionnaireResponse.getIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(questionnaireResponse.getId());
        }

        // Patient
        if (!isNullReference(questionnaireResponse.getSubject())) {
            Reference resource = questionnaireResponse.getSubject();

            log.debug(resource.getReference());
            String referencedResource = findAcutalId(resource.getReference());
            if (referencedResource != null) {
                resource.setReference(referencedResource);
            } else {
                if (!processed(resource.getReference())) allReferenced = false;
            }
        }

        // Author
        if (!isNullReference(questionnaireResponse.getAuthor())) {
            Reference resource = questionnaireResponse.getAuthor();

            log.debug(resource.getReference());
            String referencedResource = findAcutalId(resource.getReference());
            if (referencedResource != null) {
                resource.setReference(referencedResource);
            } else {
                if (!processed(resource.getReference())) allReferenced = false;
            }
        }

        // Encounter
        if (!isNullReference(questionnaireResponse.getEncounter())) {
            Reference resource = questionnaireResponse.getEncounter();

            log.debug(resource.getReference());
            String referencedResource = findAcutalId(resource.getReference());
            if (referencedResource != null) {
                resource.setReference(referencedResource);
            } else {
                if (!processed(resource.getReference())) allReferenced = false;
            }
        }

        if (allReferenced) {

            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) questionnaireResponse.setMeta(new Meta());

            // Perform a search to look for the identifier - work around
            Bundle searchBundle = client.search().forResource(QuestionnaireResponse.class)
                    .where(new StringClientParam("_content").matches().value(questionnaireResponse.getIdentifier().getValue()))
                    .prettyPrint()
                    .encodedJson()
                    .returnBundle(Bundle.class)
                    .execute();
            // Not found so add the resource
            if (searchBundle.getEntry().size()==0) {
                IIdType id = client.create().resource(questionnaireResponse).execute().getId();
                questionnaireResponse.setId(id);
                entry.processed = true;
                entry.newId = id.getValue();
                entry.resource = questionnaireResponse;
                log.info("QuestionaireResponse: Id="+entry.originalId+" Server Id = "+id.getValue());
                sendToAudit(CareConnectAuditEvent.buildAuditEvent(questionnaireResponse, null, "rest", "create", AuditEvent.AuditEventAction.C,"CareConnectMessagingAPI"));

            }

        }
        return allReferenced;
    }

    private boolean processReferralRequest(ReferralRequest referralRequest) {
        Boolean allReferenced = true;
        EntryProcessing entry = findEntryProcess(referralRequest.getId());
        
        if (referralRequest.getIdentifier().size()  == 0) {
            referralRequest.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(referralRequest.getId());
        }

        // Patient
        if (!isNullReference(referralRequest.getPatient())) {
            Reference resource = referralRequest.getPatient();

            log.debug(resource.getReference());
            String referencedResource = findAcutalId(resource.getReference());
            if (referencedResource != null) {
                resource.setReference(referencedResource);
            } else {
                if (!processed(resource.getReference())) allReferenced = false;
            }
        }

        // Requester
        if (!isNullReference(referralRequest.getRequester())) {
            Reference resource = referralRequest.getRequester();

            log.debug(resource.getReference());
            String referencedResource = findAcutalId(resource.getReference());
            if (referencedResource != null) {
                resource.setReference(referencedResource);
            } else {
                if (!processed(resource.getReference())) allReferenced = false;
            }
        }
        // Recipient
        for (Reference resource : referralRequest.getRecipient()) {

            log.debug(resource.getReference());
            String referencedResource = findAcutalId(resource.getReference());
            if (referencedResource != null) {
                resource.setReference(referencedResource);
            } else {
                if (!processed(resource.getReference())) allReferenced = false;
            }
        }

        // Encounter
        if (!isNullReference(referralRequest.getEncounter())) {
            Reference resource = referralRequest.getEncounter();

            log.debug(resource.getReference());
            String referencedResource = findAcutalId(resource.getReference());
            if (referencedResource != null) {
                resource.setReference(referencedResource);
            } else {
                if (!processed(resource.getReference())) allReferenced = false;
            }
        }

        // Supporting information
        for (Reference resource : referralRequest.getSupportingInformation()) {

            log.debug(resource.getReference());
            String referencedResource = findAcutalId(resource.getReference());
            if (referencedResource != null) {
                resource.setReference(referencedResource);
            } else {
                if (!processed(resource.getReference())) allReferenced = false;
            }
        }

        if (allReferenced) {
            // Work around to cope with slicing not validated error in hapi server
            if (client.getServerBase().contains("http://fhirtest.uhn.ca/baseDstu2")) referralRequest.setMeta(new Meta());


            MethodOutcome outcome = client.update().resource(referralRequest)
                    .conditionalByUrl("ReferralRequest?identifier=" + referralRequest.getIdentifier().get(0).getSystem() + "%7C" + referralRequest.getIdentifier().get(0).getValue())
                    .execute();

            referralRequest.setId(outcome.getId());
            entry.processed = true;
            entry.resource = referralRequest;
            entry.newId = outcome.getId().toString();
            log.info("ReferralRequest: Id="+entry.originalId+" Server Id = "+outcome.getId());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(referralRequest, outcome, "rest", "create", AuditEvent.AuditEventAction.C,"CareConnectMessagingAPI"));


        }
        return allReferenced;
    }

    private void processBundle(Bundle bundle)
    {

        sendToAudit(CareConnectAuditEvent.buildAuditEvent(bundle, null, bundle.getType().toCode(), "create", AuditEvent.AuditEventAction.C,"CareConnectMessagingAPI"));

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource resource = entry.getResource();
            EntryProcessing resourceP = new EntryProcessing();
            resourceP.originalId = resource.getId();
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

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource resource = entry.getResource();

            if (entry.getResource() instanceof Bundle) {
                log.info("Another Bundle Found at entry "+entry.getResource().getId());
                processBundle((Bundle) resource);
                log.info("Returned from sub Bundle call at entry "+entry.getResource().getId());
            }
        }

        for (int g= 0;g < 5;g++) {
            allProcessed = true;
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {

                Resource resource = entry.getResource();
                log.info("---- Looking for "+resource.getResourceType()+" resource with id of = "+resource.getId());
                EntryProcessing entryProcess = findEntryProcess(resource.getId());

                if (entryProcess != null && !entryProcess.processed) {

                    //log.info("Entry "+entryProcess.resource.getResourceName() + " Id "+ entryProcess.originalId + "Processed="+entryProcess.processed  );
                    //if (entryProcess.newId != null) log.info(" New Id "+entryProcess.newId);

                    // Bundle.location
                    if (resource instanceof Location) {
                        processLocation((Location) resource);
                    }

                    // Bundle.Organization
                    if (resource instanceof Organization) {
                        processOrganisation( (Organization) resource);
                    }

                    //Bundle.Practitioner
                    if (resource instanceof Practitioner) {
                        processPractitioner( (Practitioner) resource);
                    }

                    // Bundle. Patient
                    if (resource instanceof Patient) {
                        processPatient((Patient) resource);
                    }

                    // Bundle.Procedure Request
                    if (resource instanceof ProcedureRequest) {
                        processProcedureRequest((ProcedureRequest) resource);
                    }

                    // bundle.Appointment
                    if (resource instanceof Appointment) {
                        processAppointment((Appointment) resource);
                    }

                    // Bundle.Observation
                    if (resource instanceof Observation) {
                        processObservation((Observation) resource);
                    }

                    // Bundle.Encounter
                    if (resource instanceof Encounter) {
                        processEncounter( (Encounter) resource);
                    }

                    // Bundle.Condition
                    if (resource instanceof Condition) {
                        processCondition( (Condition) resource);
                    }

                    // Bundle.Procedure
                    if (resource instanceof Procedure) {
                        processProcedure( (Procedure) resource);
                    }

                    // Bundle.MedicationStatement
                    if (resource instanceof MedicationStatement) {
                        processMedicationStatement( (MedicationStatement) resource);
                    }

                    // Bundle.Medication
                    if (resource instanceof Medication) {
                        processMedication( (Medication) resource);
                    }


                    // Bundle.Flag
                    if (resource instanceof Flag) {
                        processFlag( (Flag) resource);
                    }

                    // Bundle.Composition
                    if (resource instanceof Composition) {
                        processComposition( (Composition) resource);
                    }

                    // Bundle.List
                    if (resource instanceof List_) {
                        processList( (List_) resource);
                    }

                    // Bundle.AllergyIntolernace
                    if (resource instanceof AllergyIntolerance) {
                        processAllergyIntolerance( (AllergyIntolerance) resource);
                    }
                    // Bundle.QuestionaireResponse
                    if (resource instanceof QuestionnaireResponse) {
                        processQuestionnaireResponse( (QuestionnaireResponse) resource);
                    }

                    // Bundle.QuestionaireResponse
                    if (resource instanceof ReferralRequest) {
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
