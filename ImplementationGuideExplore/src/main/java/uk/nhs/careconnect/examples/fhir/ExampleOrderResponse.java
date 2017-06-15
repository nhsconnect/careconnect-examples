package uk.nhs.careconnect.examples.fhir;

import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.OrderStatusEnum;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by kevinmayfield on 15/06/2017.
 */
public class ExampleOrderResponse {

    public static OrderResponse buildFHIROrderResponse() {


        OrderResponse task = new OrderResponse();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Order order = new Order();
        order.setId("#order");
        Patient patient = new Patient();

        patient.setId("#pat");


        patient.addIdentifier().setSystem("https://fhir.nhs.uk/Id/nhs-number").setValue("9876543210");


        patient.addIdentifier().setSystem("https://fhir.uhs.nhs.uk/CRIS/Patient/").setValue("ABC123");
        patient.addIdentifier().setSystem("https://fhir.uhs.nhs.uk/PAS/Patient/").setValue("1234DEF");
        patient.addName().addFamily("Kanfeld").addGiven("Bernie");
        patient.setGender(AdministrativeGenderEnum.FEMALE);
        Date birth;
        try {
            birth = dateFormat.parse("1998-03-19");
            patient.setBirthDate(new DateDt(birth));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Add patient as a contained resource reference

        List<IdDt> profiles = new ArrayList<IdDt>();
        profiles.add(new IdDt("https://fhir-test.hl7.org.uk/StructureDefinition/CareConnect-Patient-1"));
        ResourceMetadataKeyEnum.PROFILES.put(patient, profiles);
        order.getSubject().setResource(patient);

        CodeableConceptDt classCode = new CodeableConceptDt();
        classCode.addCoding()
                .setCode("324861000000109")
                .setSystem("http://snomed.info/sct")
                .setDisplay("Review of patient laboratory test report");
        order.setReason(classCode);


        DateTimeDt orderDate = new DateTimeDt();
        orderDate.setValue(new Date());
        task.setDate(orderDate);

        Practitioner gp = new Practitioner();
        gp.setId("#prac");

        List<IdDt> gpprofiles = new ArrayList<IdDt>();
        gpprofiles.add(new IdDt("https://fhir-test.hl7.org.uk/StructureDefinition/CareConnect-Practitioner-1"));
        ResourceMetadataKeyEnum.PROFILES.put(gp, gpprofiles);

        gp.addIdentifier().setSystem("https://fhir.nhs.uk/Id/sds-user-id").setValue("123455");
        task.getWho().setResource(gp);

        //task.setTarget(new ResourceReferenceDt("https://fhir.uhs.nhs.uk/Device/OrderComms"));
        task.getRequest().setResource(order);
        task.setOrderStatus(OrderStatusEnum.COMPLETED);

        order.setDetail(new ArrayList<ResourceReferenceDt>());
        ResourceReferenceDt detail = order.addDetail();
        detail.setReference("https://fhir.uhs.nhs.uk/OrderComms/DiagnosticReport/12345ReportId");

        return task;

    }
}
