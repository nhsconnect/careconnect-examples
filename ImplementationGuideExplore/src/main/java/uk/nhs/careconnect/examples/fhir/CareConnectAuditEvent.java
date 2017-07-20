package uk.nhs.careconnect.examples.fhir;

import ca.uhn.fhir.model.dstu2.resource.AuditEvent;
import ca.uhn.fhir.model.primitive.InstantDt;

import java.util.Date;

/**
 * Created by kevinmayfield on 20/07/2017.
 */
public class CareConnectAuditEvent {
    public static AuditEvent buildAuditEvent(String typeCode)
    {
        AuditEvent audit = new AuditEvent();

        AuditEvent.Event event = audit.getEvent();
        event.getType().setCode(typeCode);

        Date recordedDate = new Date();
        try {
            InstantDt instance = new InstantDt(recordedDate);
            event.setDateTime(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return audit;
    }
}
