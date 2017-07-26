package uk.nhs.careconnect.examples.App;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.GenericParser;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Terser;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.hl7.fhir.instance.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.instance.model.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.jms.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootApplication
public class UHSDiagnotics implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(UHSDiagnotics.class, args);
	}

	Terser terser = null;
    String SystemNHSNumber = "https://fhir.nhs.uk/Id/nhs-number";

    IParser JSONparser = null;

    FhirValidator validator;

    FhirInstanceValidator instanceValidator;

    ActiveMQConnectionFactory connectionFactory;

    Connection connection;

    Session session;

    Destination destination;

    MessageProducer producer;

	private String terserGet(String query)
	{
		String result = null;
		try
		{
			result = terser.get(query);
		}
		catch(HL7Exception hl7ex)
		{
			// Could add some extra code here

		}
		catch(Exception ex)
		{
			// Exception thrown on no data
		}

		return result;
	}

    @Override
	public void run(String... args) throws Exception {

        // This is to base HAPI server not the CareConnectAPI
        String serverBase = "http://127.0.0.1:8080/FHIRServer/DSTU2/";
        //String serverBase = "http://fhirtest.uhn.ca/baseDstu2/";
        FhirContext ctxFHIR = FhirContext.forDstu2Hl7Org();

       // ctxValidator  = FhirContext.forDstu2Hl7Org();

        validator = ctxFHIR.newValidator();
        instanceValidator = new FhirInstanceValidator();
        validator.registerValidatorModule(instanceValidator);

        JSONparser = ctxFHIR.newJsonParser();

        FhirValidator validator = ctxFHIR.newValidator();


        connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

        // Create a Connection
        connection = connectionFactory.createConnection();
        connection.start();

        // Create a Session
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create the destination (Topic or Queue)
        destination = session.createQueue("Elastic.Queue");

        // Create a MessageProducer from the Session to the Topic or Queue
        producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);


        JSONparser = ctxFHIR.newXmlParser();

        IGenericClient client = ctxFHIR.newRestfulGenericClient(serverBase);

        String msg = "MSH|^~\\&|HICSS|eQuest|ROUTE|ROUTE|20170425042348||ORU^R01|22392142__20170425042348|D|2.4|||AL|AL\r"
                + "PID|1||0772951\r"
                //+ "PD1|||^^J82022^|G9436235^A^COURT SJ^^^\r"
                + "PD1|||^^^|G9436235^A^COURT SJ^^^\r"
                + "NTE|1||For Clinical advice please bleep 2612. If the patient\\.br\\is of African-Caribbean or African family origin,\\.br\\multiply the eGFR result by 1.159. Do not use eGFR to\\.br\\calculate drug doses.\r"
                + "OBR|1||B50129418U|CC_CA^Calcium^L|||20170425093500|||||||20170425113217|CC^CA|LZW|||||||||C\r"
                + "OBX|1||CC_CA^Calcium^SUHT||1.85|mmol/L|||||U\r"
                + "OBX|2||CC_CCA2^Calcium (adjusted)^SUHT||2.11|mmol/L|2.20-2.60|L|||U\r"
                + "OBR|2||B50129418U|CC_CRPM^C-Reactive protein(CRP)^L|||20170425093500|||||||20170425113217|CC^CRPM|LZW|||||||||Z\r"
                + "OBX|1||CC_CRP^C-Reactive Protein (CRP)^SUHT||57|mg/L|0-7.5|H|||U\r"
                + "OBR|3||B50129418U|CC_LFT3^Liver Profile^L|||20170425093500|||||||20170425113217|CC^LFT3|LZW|||||||||Z\r"
                + "OBX|1||CC_ALB2^Albumin^SUHT||21|g/L|35-50|L|||U\r"
                + "OBX|2||CC_ALP3^Alkaline Phosphatase^SUHT||80|U/L|30-130|N|||U\r"
                + "OBX|3||CC_ALT^ALT^SUHT||33|U/L|10-40|N|||U\r"
                + "OBX|4||CC_TB2^Bilirubin^SUHT||13|umol/L|0-20|N|||U\r"
                + "OBX|5||CC_TP^Total protein^SUHT||47|g/L|60-80|L|||U\r"
                + "OBR|4||B50129418U|CC_RE2^Renal Profile(U\\T\\Es)^L|||20170425093500|||||||20170425113217|CC^RE2|LZW|||||||||Z\r"
                + "OBX|1||CC_CR2^Creatinine^SUHT||66|umol/L|80-115|L|||U\r"
                + "OBX|2||CC_GFR3^eGFRcreat (CKD-EPI)^SUHT||84|mL/min/1.73m2|||||U\r"
                + "OBX|3||CC_K^Potassium^SUHT||3.6|mmol/L|3.5-5.3|N|||U\r"
                + "OBX|4||CC_NA^Sodium^SUHT||138|mmol/L|133-146|N|||U\r"
                + "OBX|5||CC_UR^Urea^SUHT||8.9|mmol/L|2.5-7.8|H|||U\r";

        report(ctxFHIR, client, msg,"HICSS");

        msg=  "MSH|^~\\&|CRIS|LIVE|||20170426110809||ORU^R01|J1YMT3JX:31H5|P|2.4\r"
               // + "PID|||30908579^^^CRIS^PI~RHM7000610^^^RHM01^MR\r"
                + "PID|1||0772951\r"
                + "PV1||||||||||||||||||A|8606809\r"
                + "ORC|SC|30850206|65347767^CRIS||CM||^^^201704231918^^1||201704261106|SWHALECOM||C3328523^FENNELL JMB (JMF)|RHM01GICU||201704230000||190|SHTDX01|SWHALECOM||^^\r"
                + "OBR||30850206|65347767^CRIS|XCHES^XR Chest||||||||||||||||64714248|RHM65347767|201704261106||R|||^^^201704231918^^1|||||RHMLECOMA&Dr Amy Lecomte^201704251703|&Blank Clincian|RHMCAPANJ&Jeorge Capangpangan^201704231926^201704231936^^MOB181^^RHM01|RHMLONGLL&Lesley Longland^201704260814\r"
                + "OBX|1|TX|XCHES^XR Chest^CRIS3||XR Chest : 23.4.17||||||F|||201704261106||SWHALECOM^Amy LECOMTE\r"
                + "OBX|2|TX|XCHES^XR Chest^CRIS3||||||||F|||201704261106||SWHALECOM^Amy LECOMTE\r"
                + "OBX|3|TX|XCHES^XR Chest^CRIS3||An AP portable semi upright chest xray compared to 23.4.17 shows right sided central venous catheter tip over the SVC. Nasogastric tube tip reaches the stomach, median sternotomy wires and surgical clips over the chest. Diffuse opacifications of the lungs have progressed bilaterally.||||||F|||201704261106||SWHALECOM^Amy LECOMTE\r"
                + "OBX|4|TX|XCHES^XR Chest^CRIS3||||||||F|||201704261106||SWHALECOM^Amy LECOMTE\r"
                + "OBX|5|TX|XCHES^XR Chest^CRIS3||Dr A Lecomte||||||F|||201704261106||SWHALECOM^Amy LECOMTE\r"
                + "OBX|6|TX|XCHES^XR Chest^CRIS3||||||||F|||201704261106||SWHALECOM^Amy LECOMTE\r";

        report(ctxFHIR, client, msg,"CRIS");

        // Clean up ActiveMQ
        session.close();
        connection.close();
    }


	    private void report(FhirContext ctxFHIR,IGenericClient client , String msg, String sysName ) throws Exception
        {
            // HAPI Terser example https://sourceforge.net/p/hl7api/code/764/tree/releases/2.0/hapi-examples/src/main/java/ca/uhn/hl7v2/examples/ExampleUseTerser.java#l72

            // Ringholm hl7v2 to fhir mapping http://www.ringholm.com/docs/04350_mapping_HL7v2_FHIR.htm

            // Note this code is differing from rene's blog as it's using Procedure to record tests in OBR segments

            String LabCodeSystem = "https://fhir.uhs.nhs.uk/"+sysName +"/CodeSystem";

            Parser parser = new GenericParser();
            parser.getParserConfiguration().setDefaultObx2Type("ST");
            Message hapiMsg = parser.parse(msg);

            terser = new Terser(hapiMsg);


            String sendingApplication = terser.get("/.MSH-3-1");
            System.out.println(sendingApplication);


            IParser FHIRparser = ctxFHIR.newXmlParser();

            SimpleDateFormat fmtss = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat fmtmm = new SimpleDateFormat("yyyyMMddHHmm");

            Bundle results = client
                    .search()
                    .byUrl("Patient?identifier="+SystemNHSNumber+"|9876543210")
                    .returnBundle(Bundle.class)
                    .execute();
            // Unsafe !!
            // We need to find the Id of the Patient to add a reference to the new FHIR resources.
            Patient patient = (Patient) results.getEntry().get(0).getResource();
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(results, null, "rest", "read", AuditEvent.AuditEventAction.R,"UHSDiagnostics.java"));

            results = client
                    .search()
                    .byUrl("Practitioner?identifier=https://fhir.nhs.net/Id/sds-role-profile-id|PT1357")
                    .returnBundle(Bundle.class)
                    .execute();
            // Unsafe !!
            // We need to find the Id of the Patient to add a reference to the new FHIR resources.
            Practitioner consultant = (Practitioner) results.getEntry().get(0).getResource();
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(results, null, "rest", "read", AuditEvent.AuditEventAction.R,"UHSDiagnostics.java"));



            String result = null;
            MethodOutcome outcome = null;

            //
            // DIAGNOSTIC ORDER
            //
            DiagnosticOrder order = new DiagnosticOrder();


            order.setMeta(new Meta().addProfile("https://fhir.nhs.uk/StructureDefinition/dds-request-1-0"));

            order.setSubject(new Reference(patient.getId()));
            // First pass of HL7v2 message
            Integer orderNum = 0;
            do {

                if (order.getIdentifier().size() == 0) {
                    order.addIdentifier()
                            .setSystem("https://fhir.uhs.nhs.uk/"+sysName +"/DiagnosticOrder")
                            .setValue(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-3-1"));
                }


                DiagnosticOrder.DiagnosticOrderItemComponent orderItem = new DiagnosticOrder.DiagnosticOrderItemComponent();
                orderItem.getCode().addCoding()
                        .setSystem(LabCodeSystem)
                        .setCode(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-4-1"))
                        .setDisplay(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-4-2"));
                order.getItem().add(orderItem);

                orderNum++;
                result = terserGet("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(0)/OBX-3-1");
            } while (result != null);

            System.out.println(FHIRparser.setPrettyPrint(true).encodeResourceToString(order));
            validate(FHIRparser.setPrettyPrint(true).encodeResourceToString(order));
            // Now post the referenced order
            outcome = client.update().resource(order)
                    .conditionalByUrl("DiagnosticOrder?identifier=" + order.getIdentifier().get(0).getSystem() + "%7C" + order.getIdentifier().get(0).getValue())
                    .execute();
            order.setId(outcome.getId());
            System.out.println(outcome.getId().getValue());
            sendToAudit(CareConnectAuditEvent.buildAuditEvent(order, outcome, "rest", "create", AuditEvent.AuditEventAction.C,"UHSDiagnostics.java"));


            //
            // DIAGNOSTIC REPORT
            //

            DiagnosticReport report = new DiagnosticReport();
            String text = "";

            report.setMeta(new Meta().addProfile("https://fhir.nhs.uk/StructureDefinition/dds-report-1-0"));
            orderNum = 0;
            do {
                if (report.getIdentifier().size() == 0) {
                    report.addIdentifier()
                            .setSystem("https://fhir.uhs.nhs.uk/" + sysName + "/DiagnosticReport")
                            .setValue(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-3-1") + "-" + terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-4-1"));
                }
                if (report.getIssued() == null) {
                    try {
                        Date date = null;

                        if (terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-7-1") == null || terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-7-1").isEmpty())
                        {
                            date = fmtmm.parse(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-22-1"));
                            /*
                            for (int f=15;f<30;f++) {
                                System.out.println(f+ " - " + terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-"+f+"-1"));
                            }
                            */
                        }
                        else {
                            date = fmtss.parse(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-7-1"));
                        }
                        //InstantDt instance = new InstantDt(date);
                        if (date!=null) {

                            report.setIssued(date);
                            report.setEffective(new DateTimeType(date));
                        }

                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        System.out.println("Date conversion error "+e1.getMessage());
                    }
                }

                orderNum++;
                result = terserGet("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(0)/OBX-3-1");
            } while (result != null);

            report.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);

            report.setSubject(new Reference(patient.getId()));

            report.getRequest().add(new Reference(order.getId()));

            report.setPerformer(new Reference(consultant.getId()));

           // Don't have an code for the report itself. Revisit!!!!
            report.getCode().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("15220000")
                    .setDisplay("Laboratory test");



            // Second pass of HL7v2 message
            orderNum = 0;
            do {

                // Add Observations

                Integer observationNo = 0;
                do {
                    result = terserGet("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-3-1");
                    if (result != null) {

                        if (!terserGet("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-2-1").equals("TX")) {
                            Observation observation = new Observation();

                            observation.addIdentifier()
                                    .setSystem("https://fhir.uhs.nhs.uk/" + sysName + "/Observation")
                                    .setValue(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-3-1") + "-" + terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-3-1"));

                            observation.setSubject(new Reference(patient.getId()));

                            observation.setStatus(Observation.ObservationStatus.FINAL);

                            observation.getCode().addCoding()
                                    .setDisplay(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-3-2"))
                                    .setSystem(LabCodeSystem)
                                    .setCode(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-3-1"));

                            // Not converted unit and code correctly.

                            observation.getCategory().addCoding()
                                    .setSystem("http://hl7.org/fhir/observation-category")
                                    .setCode("laboratory")
                                    .setDisplay("Laboratory");

                            try {
                                Date date;
                                date = fmtss.parse(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-7-1"));
                             //   DateTimeDt effectiveDate = new DateTimeDt(date);
                                observation.setEffective(new DateTimeType(date));

                            } catch (Exception e1) {
                                // TODO Auto-generated catch block
                            }


                            observation.setValue(
                                    new Quantity()
                                            .setValue(new BigDecimal(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-5-1")))
                                            .setUnit(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-6-1"))
                                            .setSystem("http://unitsofmeasure.org")
                                            .setCode(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-6-1")));

                            if (terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-7-1") != null) {
                                String[] highlow = terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-7-1").split("-");

                                SimpleQuantity lowsq = new SimpleQuantity();
                                lowsq.setValue(new BigDecimal(highlow[0])).setSystem("http://unitsofmeasure.org").setCode(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-6-1"));

                                SimpleQuantity highsq = new SimpleQuantity();
                                highsq.setValue(new BigDecimal(highlow[1])).setSystem("http://unitsofmeasure.org").setCode(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-6-1"));

                                observation.addReferenceRange()
                                        .setLow(lowsq)
                                        .setHigh(highsq);
                            }
                            if (terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-8-1") != null) {
                                switch (terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-8-1")) {
                                    case "H":
                                        observation.getInterpretation().addCoding().setCode("H").setSystem("http://hl7.org/fhir/v2/0078").setDisplay("High");
                                        break;
                                    case "N":
                                        observation.getInterpretation().addCoding().setCode("N").setSystem("http://hl7.org/fhir/v2/0078").setDisplay("Normal");
                                        break;
                                    case "L":
                                        observation.getInterpretation().addCoding().setCode("L").setSystem("http://hl7.org/fhir/v2/0078").setDisplay("Low");
                                        break;
                                    case "A":
                                        observation.getInterpretation().addCoding().setCode("A").setSystem("http://hl7.org/fhir/v2/0078").setDisplay("Abnormal");
                                        break;

                                }

                            }

                            System.out.println(FHIRparser.setPrettyPrint(true).encodeResourceToString(observation));
                            validate(FHIRparser.setPrettyPrint(true).encodeResourceToString(observation));
                            outcome = client.update().resource(observation)
                                    .conditionalByUrl("Observation?identifier=" + observation.getIdentifier().get(0).getSystem() + "%7C" + observation.getIdentifier().get(0).getValue())
                                    .execute();
                            observation.setId(outcome.getId());
                            System.out.println(outcome.getId().getValue());
                            sendToAudit(CareConnectAuditEvent.buildAuditEvent(observation, outcome, "rest", "create", AuditEvent.AuditEventAction.C,"UHSDiagnostics.java"));


                            report.getResult().add(new Reference(observation.getId()));

                        } else {
                            // Handle text section here
                            if (terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-5-1") != null) {
                                text = text + terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-5-1") + "\r";
                            } else {
                                text = text + "\r";
                            }
                            report.getText().setDivAsString(text);

                            report.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
                        }
                    }

                    observationNo++;
                } while (result != null);

                System.out.println(FHIRparser.setPrettyPrint(true).encodeResourceToString(report));
                validate(FHIRparser.setPrettyPrint(true).encodeResourceToString(report));
                outcome = client.update().resource(report)
                        .conditionalByUrl("DiagnosticReport?identifier=" + report.getIdentifier().get(0).getSystem() + "%7C" + report.getIdentifier().get(0).getValue())
                        .execute();
                report.setId(outcome.getId());
                System.out.println(outcome.getId().getValue());
                sendToAudit(CareConnectAuditEvent.buildAuditEvent(report, outcome, "rest", "create", AuditEvent.AuditEventAction.C,"UHSDiagnostics.java"));


                orderNum++;
                result = terserGet("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(0)/OBX-3-1");
            } while (result != null);

            //

            // PROCEDURE

            //

            // Third pass of HL7v2 message
            orderNum = 0;
            do {
                Procedure procedure = new Procedure();
                text = "";

                procedure.setMeta(new Meta().addProfile("https://fhir.hl7.org.uk/StructureDefinition/CareConnect-Procedure-1"));

                procedure.addIdentifier()
                        .setSystem("https://fhir.uhs.nhs.uk/"+sysName +"/Test/Observation")
                        .setValue(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-3-1") + "-" + terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-4-1"));


                procedure.setSubject(new Reference(patient.getId()));

                procedure.setStatus(Procedure.ProcedureStatus.COMPLETED);

                procedure.getCategory().addCoding()
                        .setSystem("http://snomed.info/sct")
                        .setCode("15220000")
                        .setDisplay("Laboratory Test");

                procedure.getCode().addCoding()
                        .setSystem(LabCodeSystem)
                        .setCode(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-4-1"))
                        .setDisplay(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-4-2"));


                try {
                    Date date;
                    date = fmtss.parse(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-7-1"));
                    DateTimeType instance = new DateTimeType(date);
                    procedure.setPerformed(instance);

                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                }
                // Link procedure to the report
                procedure.addReport().setReference(report.getId());

                // Now store the observation as this is referred to in the child observations below

                System.out.println(FHIRparser.setPrettyPrint(true).encodeResourceToString(procedure));
                validate(FHIRparser.setPrettyPrint(true).encodeResourceToString(procedure));
                outcome = client.update().resource(procedure)
                        .conditionalByUrl("Procedure?identifier=" + procedure.getIdentifier().get(0).getSystem() + "%7C" + procedure.getIdentifier().get(0).getValue())
                        .execute();
                procedure.setId(outcome.getId());
                System.out.println(outcome.getId().getValue());

                orderNum++;
                result = terserGet("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(0)/OBX-3-1");
            } while (result != null);


        }

    private void validate(String resource)
    {
        ValidationResult result = validator.validateWithResult(resource);



       // System.out.println(result.isSuccessful()); // false

        // Show the issues
        // Colour values https://github.com/yonchu/shell-color-pallet/blob/master/color16
        for (SingleValidationMessage next : result.getMessages()) {
            switch (next.getSeverity())
            {
                case ERROR:
                    System.out.println(" Next issue " + (char)27 + "[31mERROR" + (char)27 + "[0m" + " - " +  next.getLocationString() + " - " + next.getMessage());
                    break;
                case WARNING:
                    System.out.println(" Next issue " + (char)27 + "[33mWARNING" + (char)27 + "[0m" + " - " +  next.getLocationString() + " - " + next.getMessage());
                    break;
                case INFORMATION:
                    System.out.println(" Next issue " + (char)27 + "[34mINFORMATION" + (char)27 + "[0m" + " - " +  next.getLocationString() + " - " + next.getMessage());
                    break;
                default:
                    System.out.println(" Next issue " + next.getSeverity() + " - " + next.getLocationString() + " - " + next.getMessage());
            }
        }

    }

    private void sendToAudit(AuditEvent audit) {
        try {
            // Create a ConnectionFactory

            // Create a messages

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


}
