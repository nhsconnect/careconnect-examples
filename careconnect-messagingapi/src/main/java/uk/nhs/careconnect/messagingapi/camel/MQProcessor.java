package uk.nhs.careconnect.messagingapi.camel;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
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
            System.out.println(outcome.getId().getValue());
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
            log.info("organisation.getPartOf()="+organisation.getPartOf().getReference().getValue());
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
        //    System.out.println(outcome.getId().getValue());
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
        for (int j = 0; j < practitioner.getPractitionerRole().size(); j++) {
            IResource referencedResource = null;
            log.info(practitioner.getPractitionerRole().get(j).getManagingOrganization().getReference().getValue());
            if (!isNullReference(practitioner.getPractitionerRole().get(j).getManagingOrganization())) {
                int i = 0;
                for (int h = 0; h < resourceList.size(); h++) {
                    if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && practitioner.getPractitionerRole().get(j).getManagingOrganization().getReference().getValue().equals(resourceList.get(h).bundleId)) {
                        referencedResource = resourceList.get(h).resource;
                        log.info("Appointment.Participants BundleId =" + resourceList.get(h).bundleId);
                        log.info("Appointment.Participants ActualId =" + resourceList.get(h).actualId);
                        i = h;
                    }
                }
                if (referencedResource != null) {
                    log.info("Appointment.Participants New ReferenceId = " + resourceList.get(i).actualId);
                    practitioner.getPractitionerRole().get(j).getManagingOrganization().setReference(resourceList.get(i).actualId);
                    newReferences.add(resourceList.get(i).actualId);
                } else {
                    if (!processed(practitioner.getPractitionerRole().get(j).getManagingOrganization().getReference().getValue())) allReferenced = false;
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
        //    System.out.println(outcome.getId().getValue());
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
           // System.out.println(outcome.getId().getValue());
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
            System.out.println(outcome.getId().getValue());
        }

        return allReferenced;
    }

    private boolean processAppointment(int f, Appointment appointment) {
        Boolean allReferenced = true;

        if (appointment.getIdentifier().size() == 0) {
            appointment.addIdentifier()
                    .setSystem("https://tools.ietf.org/html/rfc4122")
                    .setValue(appointment.getId().getValue());
        }



        // Appointment.Participants
        for (int j = 0; j < appointment.getParticipant().size(); j++) {
            IResource referencedResource = null;
            log.info(appointment.getParticipant().get(j).getActor().getReference().getValue());
            if (!isNullReference(appointment.getParticipant().get(j).getActor())) {
                int i = 0;
                for (int h = 0; h < resourceList.size(); h++) {
                    if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && appointment.getParticipant().get(j).getActor().getReference().getValue().equals(resourceList.get(h).bundleId)) {
                        referencedResource = resourceList.get(h).resource;
                        log.info("Appointment.Participants BundleId =" + resourceList.get(h).bundleId);
                        log.info("Appointment.Participants ActualId =" + resourceList.get(h).actualId);
                        i = h;
                    }
                }
                if (referencedResource != null) {
                    log.info("Appointment.Participants New ReferenceId = " + resourceList.get(i).actualId);
                    appointment.getParticipant().get(j).getActor().setReference(resourceList.get(i).actualId);
                    newReferences.add(resourceList.get(i).actualId);
                } else {
                    if (!processed(appointment.getParticipant().get(j).getActor().getReference().getValue())) allReferenced = false;
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
            System.out.println(outcome.getId().getValue());
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
            System.out.println(outcome.getId().getValue());
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
        if (!isNullReference(encounter.getPatient())) {
            IResource referencedResource = null;
            log.info(encounter.getPatient().getReference().getValue());
            int i = 0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && encounter.getPatient().getReference().getValue().equals(resourceList.get(h).bundleId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("BundleId =" + resourceList.get(h).bundleId);
                    log.debug("ActualId =" + resourceList.get(h).actualId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.debug("New ReferenceId = " + resourceList.get(i).actualId);
                encounter.getPatient().setReference(resourceList.get(i).actualId);
                newReferences.add(resourceList.get(i).actualId);

            } else {
                if (!processed(encounter.getPatient().getReference().getValue())) allReferenced = false;
            }
        }
        // Bundle.EncounterAppointments

        if (!isNullReference(encounter.getAppointment())) {
            log.info("encounter.getAppointment().getReference()="+encounter.getAppointment().getReference());

            IResource referencedResource = null;
            log.info(encounter.getAppointment().getReference().getValue());
            int i = 0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && encounter.getAppointment().getReference().getValue().equals(resourceList.get(h).bundleId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("BundleId =" + resourceList.get(h).bundleId);
                    log.debug("ActualId =" + resourceList.get(h).actualId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.debug("New ReferenceId = " + resourceList.get(i).actualId);
                encounter.getAppointment().setReference(resourceList.get(i).actualId);
                newReferences.add(resourceList.get(i).actualId);
            } else {
                if (!processed(encounter.getAppointment().getReference().getValue())) allReferenced = false;
            }
        }



        // Bundle.Encounter Service Provider
        if (!isNullReference(encounter.getServiceProvider())) {
            IResource referencedResource = null;
            log.info(encounter.getServiceProvider().getReference().getValue());
            int i = 0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && encounter.getServiceProvider().getReference().getValue().equals(resourceList.get(h).bundleId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("BundleId =" + resourceList.get(h).bundleId);
                    log.debug("ActualId =" + resourceList.get(h).actualId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.debug("New ReferenceId = " + resourceList.get(i).actualId);
                encounter.getServiceProvider().setReference(resourceList.get(i).actualId);
                newReferences.add(resourceList.get(i).actualId);
            } else {
                if (!processed(encounter.getServiceProvider().getReference().getValue())) allReferenced = false;
            }
        }

        // Bundle.Encounter Participants
        for (int j = 0; j < encounter.getParticipant().size(); j++) {
            IResource referencedResource = null;
            log.info(encounter.getParticipant().get(j).getIndividual().getReference().getValue());
            if (!isNullReference(encounter.getParticipant().get(j).getIndividual())) {
                int i = 0;
                for (int h = 0; h < resourceList.size(); h++) {
                    if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && encounter.getParticipant().get(j).getIndividual().getReference().getValue().equals(resourceList.get(h).bundleId)) {
                        referencedResource = resourceList.get(h).resource;
                        log.debug("BundleId =" + resourceList.get(h).bundleId);
                        log.debug("ActualId =" + resourceList.get(h).actualId);
                        i = h;
                    }
                }
                if (referencedResource != null) {
                    log.debug("New ReferenceId = " + resourceList.get(i).actualId);
                    encounter.getParticipant().get(j).getIndividual().setReference(resourceList.get(i).actualId);
                    newReferences.add(resourceList.get(i).actualId);
                } else {
                    if (!processed(encounter.getParticipant().get(j).getIndividual().getReference().getValue()))  allReferenced = false;
                }
            }
        }

        // Bundle.Encounter Locations
        for (int j = 0; j < encounter.getLocation().size(); j++) {
            IResource referencedResource = null;
            log.info(encounter.getLocation().get(j).getLocation().getReference().getValue());
            if (!isNullReference(encounter.getLocation().get(j).getLocation())) {
                int i = 0;
                for (int h = 0; h < resourceList.size(); h++) {
                    if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && encounter.getLocation().get(j).getLocation().getReference().getValue().equals(resourceList.get(h).bundleId)) {
                        referencedResource = resourceList.get(h).resource;
                        log.debug("BundleId =" + resourceList.get(h).bundleId);
                        log.debug("ActualId =" + resourceList.get(h).actualId);
                        i = h;
                    }
                }
                if (referencedResource != null) {
                    log.debug("New ReferenceId = " + resourceList.get(i).actualId);
                    encounter.getLocation().get(j).getLocation().setReference(resourceList.get(i).actualId);
                    newReferences.add(resourceList.get(i).actualId);
                } else {
                    if (!processed(encounter.getLocation().get(j).getLocation().getReference().getValue())) allReferenced = false;
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
            System.out.println(outcome.getId().getValue());
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


        // Bundle.COndition Patient
        if (!isNullReference(condition.getPatient())) {
            IResource referencedResource = null;
            log.info(condition.getPatient().getReference().getValue());
            int i = 0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && condition.getPatient().getReference().getValue().equals(resourceList.get(h).bundleId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("BundleId =" + resourceList.get(h).bundleId);
                    log.debug("ActualId =" + resourceList.get(h).actualId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.debug("New ReferenceId = " + resourceList.get(i).actualId);
                condition.getPatient().setReference(resourceList.get(i).actualId);
                newReferences.add(resourceList.get(i).actualId);

            } else {
                if (!processed(condition.getPatient().getReference().getValue())) allReferenced = false;
            }
        }
        // bundle condition encounter
        if (!isNullReference(condition.getEncounter()) && !condition.getEncounter().getReference().getValue().isEmpty()) {
            IResource referencedResource = null;
            log.info(condition.getEncounter().getReference().getValue());
            int i = 0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && condition.getEncounter().getReference().getValue().equals(resourceList.get(h).bundleId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("BundleId =" + resourceList.get(h).bundleId);
                    log.debug("ActualId =" + resourceList.get(h).actualId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.debug("New ReferenceId = " + resourceList.get(i).actualId);
                condition.getEncounter().setReference(resourceList.get(i).actualId);
                newReferences.add(resourceList.get(i).actualId);

            } else {
                if (!processed(condition.getEncounter().getReference().getValue())) allReferenced = false;
            }
        }


        // Bundle.Condition asserter
        if (!isNullReference(condition.getAsserter()) && !condition.getAsserter().getReference().getValue().isEmpty()) {
            IResource referencedResource = null;
            log.info(condition.getAsserter().getReference().getValue());
            int i = 0;
            for (int h = 0; h < resourceList.size(); h++) {
                if (resourceList.get(h).processed && resourceList.get(h).bundleId != null && condition.getAsserter().getReference().getValue().equals(resourceList.get(h).bundleId)) {
                    referencedResource = resourceList.get(h).resource;
                    log.debug("BundleId =" + resourceList.get(h).bundleId);
                    log.debug("ActualId =" + resourceList.get(h).actualId);
                    i = h;
                }
            }
            if (referencedResource != null) {
                log.debug("New ReferenceId = " + resourceList.get(i).actualId);
                condition.getAsserter().setReference(resourceList.get(i).actualId);
                newReferences.add(resourceList.get(i).actualId);

            } else {
                if (!processed(condition.getAsserter().getReference().getValue())) allReferenced = false;
            }
        }


        if (allReferenced)
        {
            log.info("Condition= "+parser.setPrettyPrint(true).encodeResourceToString(condition));
            MethodOutcome outcome = client.update().resource(condition)
                    .conditionalByUrl("Condition?identifier=" + condition.getIdentifier().get(0).getSystem() + "%7C" + condition.getIdentifier().get(0).getValue())
                    .execute();
            if (outcome.getResource()!=null) {
                log.info("Outcome = " + parser.setPrettyPrint(true).encodeResourceToString(outcome.getResource()));
            }
            condition.setId(outcome.getId());
            resourceList.get(f).processed = true;
            resourceList.get(f).resource = condition;
            resourceList.get(f).actualId = outcome.getId().getValue();
            System.out.println(outcome.getId().getValue());
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
            if (outcome.getResource()!=null) {
                log.info("Outcome = " + parser.setPrettyPrint(true).encodeResourceToString(outcome.getResource()));
            }
            procedure.setId(outcome.getId());
            resourceList.get(f).processed = true;
            resourceList.get(f).resource = procedure;
            resourceList.get(f).actualId = outcome.getId().getValue();
            System.out.println(outcome.getId().getValue());
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
            // Miss off MessageHeader, so start at 1
            if (f == 0) resourceP.processed = true;
            resourceList.add(resourceP);
        }
        // Loop 3 times now to, need to have time out but ensure all resources posted
        parser = ctx.newXmlParser();
        Boolean allProcessed;
        for (int g= 0;g < 5;g++) {
            allProcessed = true;
            for (int f = 0; f < bundle.getEntry().size(); f++) {
                log.info("Entry number " + f);
                IResource resource = bundle.getEntry().get(f).getResource();
                log.info(resource.getResourceName());
                log.info(resource.getId().getValue());

                if (!resourceList.get(f).processed) {

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
                        Procedure procedure = (Procedure) resource;
                    }

                }
            }
        }
    }
}
