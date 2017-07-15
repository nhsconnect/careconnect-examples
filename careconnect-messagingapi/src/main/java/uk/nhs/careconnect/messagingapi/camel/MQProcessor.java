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
        for (int g= 0;g < 3;g++) {
            allProcessed = true;
            for (int f = 0; f < bundle.getEntry().size(); f++) {
                log.info("Entry number " + f);
                IResource resource = bundle.getEntry().get(f).getResource();
                log.info(resource.getResourceName());
                log.info(resource.getId().getValue());
                if (!resourceList.get(f).processed) {

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
                            resourceList.get(f).resource = procedureRequest;
                            System.out.println(outcome.getId().getValue());
                        }

                    }
                }
            }

        }
            /*
        if (exchange.getIn().getHeader("FHIRResource").equals("Observation")) {
            Observation observation = parser.parseResource(Observation.class,reader);
            if (observation.getIdentifier().size()==0) {
                observation.addIdentifier()
                        .setSystem("https://tools.ietf.org/html/rfc4122")
                        .setValue(exchange.getIn().getHeader("FHIRResourceId").toString());
            }
            if (observation.getPerformer().size()>0) {
                observation.getPerformer().get(0).setReference(("http://workaround.com/Practitioner/"+observation.getPerformer().get(0).getReference().getValue()));
            }
            if (observation.getEncounter() != null) {
                observation.getEncounter().setReference(("http://workaround.com/Encounter/"+observation.getEncounter().getReference().getValue()));
            }

            observation.setSubject(new ResourceReferenceDt("http://workaround.com/Patient/"+observation.getSubject().getReference().getValue()));
            exchange.getIn().setBody( ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(observation));
            exchange.getIn().setHeader("FHIRConditionUrl", "identifier=" + observation.getIdentifier().get(0).getSystem() + "%7C" + observation.getIdentifier().get(0).getValue());

        }

        if (exchange.getIn().getHeader("FHIRResource").equals("Organization")) {
            Organization organization = parser.parseResource(Organization.class,reader);
            exchange.getIn().setHeader("FHIRConditionUrl","identifier="+organization.getIdentifier().get(0).getSystem()+"%7C"+organization.getIdentifier().get(0).getValue());
        }

        if (exchange.getIn().getHeader("FHIRResource").equals("Patient")) {
            Patient patient = parser.parseResource(Patient.class,reader);
            exchange.getIn().setHeader("FHIRConditionUrl","identifier="+patient.getIdentifier().get(0).getSystem()+"%7C"+patient.getIdentifier().get(0).getValue());
        }
        */

        /* The conditional url isn't supported in HAPI
        if (exchange.getIn().getHeader("FHIRResource").equals("QuestionnaireResponse")) {
            QuestionnaireResponse questionnaireResponse = parser.parseResource(QuestionnaireResponse.class,reader);
            exchange.getIn().setHeader("FHIRConditionUrl","identifier="+questionnaireResponse.getIdentifier().getSystem()+"%7C"+questionnaireResponse.getIdentifier().getValue());
        }
        */
    /*
        // Encounter
        if (exchange.getIn().getHeader("FHIRResource").equals("Encounter")) {
            Encounter encounter = parser.parseResource(Encounter.class,reader);
            // Work around to get Encounter stored
            encounter.getParticipant().get(0).setIndividual(new ResourceReferenceDt("http://workaround.com/Practitioner/"+encounter.getParticipant().get(0).getIndividual().getReference().getValue()));
            encounter.getPatient().setReference("http://workaround.com/Patient/"+encounter.getPatient().getReference().getValue());
            if (encounter.getLocation().size()>0) {
                encounter.getLocation().get(0).setLocation(new ResourceReferenceDt("http://workaround.com/Location/"+encounter.getLocation().get(0).getLocation().getReference().getValue()));
            }
            if (encounter.getAppointment()!=null) {
                encounter.getAppointment().setReference("http://workaround.com/Appointment/"+encounter.getAppointment().getReference().getValue());
            }

            exchange.getIn().setBody( ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(encounter));
            exchange.getIn().setHeader("FHIRConditionUrl","identifier="+encounter.getIdentifier().get(0).getSystem()+"%7C"+encounter.getIdentifier().get(0).getValue());
        }

        // Practitioner
        if (exchange.getIn().getHeader("FHIRResource").equals("Practitioner")) {
            Practitioner practitioner = parser.parseResource(Practitioner.class,reader);
            exchange.getIn().setHeader("FHIRConditionUrl","identifier="+practitioner.getIdentifier().get(0).getSystem()+"%7C"+practitioner.getIdentifier().get(0).getValue());
        }

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

        //ProcedureRequest
        if (exchange.getIn().getHeader("FHIRResource").equals("ProcedureRequest")) {
            ProcedureRequest procedureRequest = parser.parseResource(ProcedureRequest.class,reader);
            if (procedureRequest.getSubject()!=null) {
                procedureRequest.getSubject().setReference("http://workaround.com/Patient/"+procedureRequest.getSubject().getReference().getValue());
            }
            if (procedureRequest.getPerformer() !=null && procedureRequest.getPerformer().getReference() !=null) {
                procedureRequest.getPerformer().setReference("http://workaround.com/Practitioner/"+procedureRequest.getPerformer().getReference().getValue());
            }

            exchange.getIn().setBody( ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(procedureRequest));
            exchange.getIn().setHeader("FHIRConditionUrl","identifier="+procedureRequest.getIdentifier().get(0).getSystem()+"%7C"+procedureRequest.getIdentifier().get(0).getValue());
        }
        if (exchange.getIn().getHeader("FHIRResource").equals("Appointment")) {
            Appointment appointment = parser.parseResource(Appointment.class,reader);
            if (appointment.getParticipant().size()>0) {
                appointment.getParticipant().get(0).getActor().setReference("http://workaround.com/Patient/"+appointment.getParticipant().get(0).getActor().getReference().getValue());
            }
            if (appointment.getParticipant().size()>1) {
                appointment.getParticipant().get(1).getActor().setReference("http://workaround.com/Practitioner/"+appointment.getParticipant().get(1).getActor().getReference().getValue());
            }
            exchange.getIn().setBody( ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(appointment));
            exchange.getIn().setHeader("FHIRConditionUrl","identifier="+appointment.getIdentifier().get(0).getSystem()+"%7C"+appointment.getIdentifier().get(0).getValue());
        }
        if (exchange.getIn().getHeader("FHIRResource").equals("Location")) {
            Location location = parser.parseResource(Location.class,reader);
            exchange.getIn().setHeader("FHIRConditionUrl","identifier="+location.getIdentifier().get(0).getSystem()+"%7C"+location.getIdentifier().get(0).getValue());
        }
        if (exchange.getIn().getHeader("FHIRConditionUrl") != null)
        {
            exchange.getIn().setHeader(Exchange.CONTENT_TYPE,"application/xml+fhir");
            exchange.getIn().setHeader(Exchange.HTTP_QUERY,exchange.getIn().getHeader("FHIRConditionUrl"));
            exchange.getIn().setHeader(Exchange.HTTP_METHOD,"PUT");
            exchange.getIn().setHeader(Exchange.HTTP_PATH,exchange.getIn().getHeader("FHIRResource"));
        }

    }
     */

    }
}
