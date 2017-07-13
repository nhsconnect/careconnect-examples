package uk.nhs.careconnect.messagingapi.camel;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.parser.IParser;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Created by kevinmayfield on 12/07/2017.
 */
public class MQProcessor implements Processor {

    FhirContext ctx;

    public MQProcessor(FhirContext ctx)
    {
        this.ctx = ctx;
    }
    @Override
    public void process(Exchange exchange) throws Exception {

     //   InputStream is = (InputStream) exchange.getIn().getBody();
     //   is.reset();
        String reader = exchange.getIn().getBody(String.class);
        IParser parser = ctx.newXmlParser();

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
        /*
        if (exchange.getIn().getHeader("FHIRResource").equals("QuestionnaireResponse")) {
            QuestionnaireResponse questionnaireResponse = parser.parseResource(QuestionnaireResponse.class,reader);
            exchange.getIn().setHeader("FHIRConditionUrl","identifier="+questionnaireResponse.getIdentifier().getSystem()+"%7C"+questionnaireResponse.getIdentifier().getValue());
        }
        */
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


        if (exchange.getIn().getHeader("FHIRResource").equals("Procedure")) {
            Procedure procedure = parser.parseResource(Procedure.class,reader);
            if (procedure.getIdentifier().size()==0) {
                procedure.addIdentifier()
                        .setSystem("https://tools.ietf.org/html/rfc4122")
                        .setValue(exchange.getIn().getHeader("FHIRResourceId").toString());
                exchange.getIn().setBody( ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(procedure));
            }
            exchange.getIn().setHeader("FHIRConditionUrl", "identifier=" + procedure.getIdentifier().get(0).getSystem() + "%7C" + procedure.getIdentifier().get(0).getValue());
        }

        if (exchange.getIn().getHeader("FHIRResource").equals("ProcedureRequest")) {
            ProcedureRequest procedureRequest = parser.parseResource(ProcedureRequest.class,reader);
            if (procedureRequest.getSubject()!=null) {
                procedureRequest.getSubject().setReference("http://workaround.com/Patient/"+procedureRequest.getSubject().getReference().getValue());
            }
            exchange.getIn().setHeader("FHIRConditionUrl","identifier="+procedureRequest.getIdentifier().get(0).getSystem()+"%7C"+procedureRequest.getIdentifier().get(0).getValue());
        }
        if (exchange.getIn().getHeader("FHIRResource").equals("Appointment")) {
            Appointment appointment = parser.parseResource(Appointment.class,reader);
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
}
