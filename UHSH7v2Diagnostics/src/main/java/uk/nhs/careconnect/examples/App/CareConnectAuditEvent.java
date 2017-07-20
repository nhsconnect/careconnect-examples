package uk.nhs.careconnect.examples.App;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.AuditEvent;
import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu2.valueset.AuditEventActionEnum;
import ca.uhn.fhir.model.dstu2.valueset.AuditEventOutcomeEnum;
import ca.uhn.fhir.model.primitive.InstantDt;
import ca.uhn.fhir.rest.api.MethodOutcome;

import java.net.InetAddress;
import java.util.Date;

/**
 * Created by kevinmayfield on 20/07/2017.
 */
public class CareConnectAuditEvent {
    public static AuditEvent buildAuditEvent(IResource resource , MethodOutcome outcome, String typeCode, String subTypeCode, AuditEventActionEnum actionCode, String systemValue)
    {
        AuditEvent audit = new AuditEvent();

        AuditEvent.Event event = audit.getEvent();
        event.getType()
                .setSystem("http://hl7.org/fhir/audit-event-type")
                .setCode(typeCode);
        event.addSubtype()
                .setSystem("http://hl7.org/fhir/restful-interaction")
                .setCode(subTypeCode);

        event.setAction(actionCode);

        audit.addObject()
                .setReference(new ResourceReferenceDt(resource));

        Date recordedDate = new Date();
        try {
            InstantDt instance = new InstantDt(recordedDate);
            event.setDateTime(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            audit.getSource().getIdentifier()
                    .setSystem(String.valueOf(InetAddress.getLocalHost()))
                    .setValue(systemValue);
        } catch(Exception e)
        {

        }
        audit.getSource()
                .setSite("application")
                .addType()
                    .setSystem("http://hl7.org/fhir/security-source-type")
                    .setCode("1")
                    .setDisplay("User Device");

        if (outcome!=null && outcome.getOperationOutcome() instanceof OperationOutcome)
        {
            OperationOutcome operationOutcome = (OperationOutcome) outcome.getOperationOutcome();
            System.out.println(operationOutcome.getIssueFirstRep().getCode());
            switch (operationOutcome.getIssueFirstRep().getCode()) {
                case  "informational":
                    event.setOutcome(AuditEventOutcomeEnum.SUCCESS);
                    break;
                default:
                    event.setOutcome(AuditEventOutcomeEnum.MINOR_FAILURE);

            }
            event.setOutcomeDesc(operationOutcome.getText().getDivAsString());
           // event.set
        }

        return audit;

    }
}
