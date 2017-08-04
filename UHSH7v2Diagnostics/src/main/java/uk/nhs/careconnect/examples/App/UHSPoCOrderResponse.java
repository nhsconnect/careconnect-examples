package uk.nhs.careconnect.examples.App;

import org.hl7.fhir.instance.model.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kevinmayfield on 15/06/2017.
 */
public class UHSPoCOrderResponse {

    public static Order buildFHIROrder(Patient patient,DiagnosticReport report, Practitioner gp, Practitioner consultant) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Order order = new Order();

        order.addIdentifier()
                .setSystem("https://fhir.uhs.nhs.uk/OrderComms/Order")
                .setValue("ABCDE");

        order.getSubject().setReference(patient.getId());

        CodeableConcept classCode = new CodeableConcept();
        classCode.addCoding()
                .setCode("324861000000109")
                .setSystem("http://snomed.info/sct")
                .setDisplay("Review of patient laboratory test report");
        order.setReason(classCode);

        order.setSource(new Reference(consultant.getId()));
        order.setTarget(new Reference(gp.getId()));

        order.addDetail().setReference(report.getId());

        return order;

    }

    public static Bundle converttoBundle(Order order)
    {
        Bundle bundle = new Bundle();

        bundle.setType(Bundle.BundleType.MESSAGE);

        MessageHeader messageHeader =new MessageHeader()
                .setSource(new MessageHeader.MessageSourceComponent(new UriType("https:///ordercomms.uhs.nhs.uk")))
                .setReceiver(new Reference("https:///ordercoms.system.org"))
                .setResponsible(new Reference("https:///resultsviewer.system.org"));

        messageHeader.getEvent().setCode("task-update").setSystem("https://fhir.uhs.nhs.uk/message-event-type");
        messageHeader.setTimestamp(new Date());

        bundle.addEntry().setResource(messageHeader);


        bundle.addEntry().setResource(order);

        return bundle;

    }

    public static OrderResponse buildFHIROrderResponse(Order order, Patient patient, Practitioner gp) {


        OrderResponse task = new OrderResponse();

        task.addIdentifier()
                .setSystem("https://fhir.uhs.nhs.uk/MobileApp/OrderResponse")
                .setValue("A12345");

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");


        task.setDate(new Date());

        task.getWho().setReference(gp.getId());

        //task.setTarget(new ResourceReferenceDt("https://fhir.uhs.nhs.uk/Device/OrderComms"));
        task.getRequest().setResource(order);
        task.setOrderStatus(OrderResponse.OrderStatus.COMPLETED);

        order.addDetail().setReference("https://fhir.uhs.nhs.uk/OrderComms/DiagnosticReport/12345ReportId");

        return task;

    }
}
