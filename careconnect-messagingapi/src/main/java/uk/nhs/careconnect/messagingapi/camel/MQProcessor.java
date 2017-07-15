package uk.nhs.careconnect.messagingapi.camel;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
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

    private boolean processed(String reference) {

        for(String s : newReferences)
            if(s.trim().contains(reference)) return true;
        return false;
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
        ArrayList<ResourceProcessing> resourceList = new ArrayList<ResourceProcessing>();

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
        IParser parser = ctx.newXmlParser();
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
                        Location location = (Location) resource;
                        MethodOutcome outcome = client.update().resource(location)
                                .conditionalByUrl("Location?identifier=" + location.getIdentifier().get(0).getSystem() + "%7C" + location.getIdentifier().get(0).getValue())
                                .execute();
                        location.setId(outcome.getId());
                        resourceList.get(f).processed = true;
                        resourceList.get(f).actualId = outcome.getId().getValue();
                        resourceList.get(f).resource = location;
                        System.out.println(outcome.getId().getValue());
                    }
                    // Bundle.Organization
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("Organization")) {
                        Organization organisation = (Organization) resource;
                        MethodOutcome outcome = client.update().resource(organisation)
                                .conditionalByUrl("Organization?identifier=" + organisation.getIdentifier().get(0).getSystem() + "%7C" + organisation.getIdentifier().get(0).getValue())
                                .execute();
                        organisation.setId(outcome.getId());
                        resourceList.get(f).processed = true;
                        resourceList.get(f).actualId = outcome.getId().getValue();
                        resourceList.get(f).resource = organisation;
                        System.out.println(outcome.getId().getValue());
                    }
                    //Bundle.Practitioner
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("Practitioner")) {
                        Practitioner practitioner = (Practitioner) resource;
                        MethodOutcome outcome = client.update().resource(practitioner)
                                .conditionalByUrl("Practitioner?identifier=" + practitioner.getIdentifier().get(0).getSystem() + "%7C" + practitioner.getIdentifier().get(0).getValue())
                                .execute();
                        practitioner.setId(outcome.getId());
                        resourceList.get(f).processed = true;
                        resourceList.get(f).actualId = outcome.getId().getValue();
                        resourceList.get(f).resource = practitioner;
                        System.out.println(outcome.getId().getValue());
                    }

                    // Bundle. Patient
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("Patient")) {
                        Patient patient = (Patient) resource;
                        MethodOutcome outcome = client.update().resource(patient)
                                .conditionalByUrl("Patient?identifier=" + patient.getIdentifier().get(0).getSystem() + "%7C" + patient.getIdentifier().get(0).getValue())
                                .execute();
                        patient.setId(outcome.getId());
                        resourceList.get(f).processed = true;
                        resourceList.get(f).actualId = outcome.getId().getValue();
                        resourceList.get(f).resource = patient;
                        System.out.println(outcome.getId().getValue());
                    }

                    // Bundle.Procedure Request
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("ProcedureRequest")) {
                        ProcedureRequest procedureRequest = (ProcedureRequest) resource;
                        Boolean allReferenced = true;
                        if (procedureRequest.getSubject()!=null) {
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

                    }
                    // bundle.Appointment

                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("Appointment")) {

                        Appointment appointment = (Appointment) resource;
                        Boolean allReferenced = true;

                        // Appointment.Participants
                        for (int j = 0; j < appointment.getParticipant().size(); j++) {
                            IResource referencedResource = null;
                            log.info(appointment.getParticipant().get(j).getActor().getReference().getValue());
                            if (appointment.getParticipant().get(j).getActor() !=null) {
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

                    }

                    // Bundle.Observation
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("Observation")) {

                        Observation observation = (Observation) resource;
                        // Having an identifier makes this a lot easier.
                        if (observation.getIdentifier().size() == 0) {
                            observation.addIdentifier()
                                    .setSystem("https://tools.ietf.org/html/rfc4122")
                                    .setValue(observation.getId().getValue());
                        }

                        Boolean allReferenced = true;
                        // Bundle.Observation Patient
                        if (observation.getSubject() != null) {
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
                        if (observation.getEncounter() != null) {
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
                            if (observation.getPerformer().get(j).getReference().getValue() !=null) {
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

                    }
                    // Bundle.Encounter - 1
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("Encounter")) {

                        Encounter encounter = (Encounter) resource;
                        Boolean allReferenced = true;
                        // Bundle.Encounter Patient
                        if (encounter.getPatient() != null) {
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

                        if (encounter.getAppointment().getReference().getValue() != null) {
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
                        if (encounter.getServiceProvider() != null) {
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
                            if (encounter.getParticipant().get(j).getIndividual() !=null) {
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
                            if (encounter.getParticipant().get(j).getIndividual() !=null) {
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

                    }

                    /*
                    // Bundle.Encounter -2
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("Encounter")) {

                        Encounter encounter = (Encounter) resource;
                        Boolean allReferenced = true;
                        // Bundle.Encounter Patient
                        if (encounter.getPatient() != null) {
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

                        if (encounter.getAppointment().getReference().getValue() != null) {
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
                        if (encounter.getServiceProvider() != null) {
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
                            if (encounter.getParticipant().get(j).getIndividual() !=null) {
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
                            if (encounter.getParticipant().get(j).getIndividual() !=null) {
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

                    }
                    */

                    // Bundle.Condition
                    if (bundle.getEntry().get(f).getResource().getResourceName().equals("Condition")) {

                        Condition condition = (Condition) resource;
                        // Having an identifier makes this a lot easier.
                        if (condition.getIdentifier().size() == 0) {
                            condition.addIdentifier()
                                    .setSystem("https://tools.ietf.org/html/rfc4122")
                                    .setValue(condition.getId().getValue());
                        }

                        Boolean allReferenced = true;
                        // Bundle.COndition Patient
                        if (condition.getPatient() != null) {
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
                        if (condition.getEncounter() != null) {
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
                        if (condition.getAsserter() != null) {
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
                            log.info("Encounter = "+parser.setPrettyPrint(true).encodeResourceToString(condition));
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

                    }
                }
            }

        }




/*

        //Condition
        if (exchange.getIn().getHeader("FHIRResource").equals("Condition")) {
            Condition condition = parser.parseResource(Condition.class,reader);

            if (condition.getAsserter().getReference() != null) {
                condition.getAsserter().setReference("http://workaround.com/Practitioner/"+condition.getAsserter().getReference().getValue());
            }
            if (condition.getEncounter() != null) {
                condition.getEncounter().setReference("http://workaround.com/Encounter/"+condition.getEncounter().getReference().getValue());
            }
            condition.getPatient().setReference("http://workaround.com/Patient/"+condition.getPatient().getReference().getValue());
            exchange.getIn().setBody( ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(condition));
            exchange.getIn().setHeader("FHIRConditionUrl","identifier="+condition.getIdentifier().get(0).getSystem()+"%7C"+condition.getIdentifier().get(0).getValue());
        }

        // Procedure
        if (exchange.getIn().getHeader("FHIRResource").equals("Procedure")) {
            Procedure procedure = parser.parseResource(Procedure.class,reader);
            if (procedure.getIdentifier().size()==0) {
                procedure.addIdentifier()
                        .setSystem("https://tools.ietf.org/html/rfc4122")
                        .setValue(exchange.getIn().getHeader("FHIRResourceId").toString());
            }
            if (procedure.getPerformer().size()>0) {
                procedure.getPerformer().get(0).setActor(new ResourceReferenceDt("http://workaround.com/Practitioner/"+procedure.getPerformer().get(0).getActor().getReference().getValue()));
            }
            if (procedure.getSubject() != null) {
                procedure.setSubject(new ResourceReferenceDt("http://workaround.com/Patient/"+procedure.getSubject().getReference().getValue()));
            }
            if (procedure.getLocation() != null) {
                procedure.setLocation(new ResourceReferenceDt("http://workaround.com/Location/"+procedure.getLocation().getReference().getValue()));
            }
            if (procedure.getEncounter() != null) {
                procedure.setEncounter(new ResourceReferenceDt("http://workaround.com/Encounter/"+procedure.getEncounter().getReference().getValue()));
            }
            exchange.getIn().setBody( ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(procedure));
            exchange.getIn().setHeader("FHIRConditionUrl", "identifier=" + procedure.getIdentifier().get(0).getSystem() + "%7C" + procedure.getIdentifier().get(0).getValue());
        }




    }
     */

    }
}
