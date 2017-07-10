package uk.nhs.careconnect.examples.App;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.model.dstu2.valueset.DiagnosticReportStatusEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.InstantDt;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.GenericParser;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Terser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class UHSDiagnotics implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(UHSDiagnotics.class, args);
	}

	Terser terser = null;
    String SystemNHSNumber = "https://fhir.nhs.uk/Id/nhs-number";

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
        FhirContext ctxFHIR = FhirContext.forDstu2();

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
    }


	    private void report(FhirContext ctxFHIR,IGenericClient client , String msg, String sysName ) throws Exception
        {
            // HAPI Terser example https://sourceforge.net/p/hl7api/code/764/tree/releases/2.0/hapi-examples/src/main/java/ca/uhn/hl7v2/examples/ExampleUseTerser.java#l72

            // Ringholm hl7v2 to fhir mapping http://www.ringholm.com/docs/04350_mapping_HL7v2_FHIR.htm

            String LabCodeSystem = "https://fhir.uhs.nhs.uk/"+sysName +"/CodeSystem";

            Parser parser = new GenericParser();
            parser.getParserConfiguration().setDefaultObx2Type("ST");
            Message hapiMsg = parser.parse(msg);

            terser = new Terser(hapiMsg);


            String sendingApplication = terser.get("/.MSH-3-1");
            System.out.println(sendingApplication);


            IParser FHIRparser = ctxFHIR.newXmlParser();

            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");

            Bundle results = client
                    .search()
                    .forResource(Patient.class)
                    .where(Patient.IDENTIFIER.exactly().systemAndCode(SystemNHSNumber, "9876543210"))
                    .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                    .execute();
            // Unsafe !!
            // We need to find the Id of the Patient to add a reference to the new FHIR resources.
            Patient patient = (Patient) results.getEntry().get(0).getResource();


            String result = null;
            MethodOutcome outcome = null;

            DiagnosticOrder order = new DiagnosticOrder();

            List<IdDt> orderprofiles = new ArrayList<IdDt>();
            orderprofiles.add(new IdDt("https://fhir.nhs.uk/StructureDefinition/dds-request-1-0"));
            ResourceMetadataKeyEnum.PROFILES.put(order, orderprofiles);

            order.setSubject(new ResourceReferenceDt(patient.getId()));
            // First pass of HL7v2 message
            Integer orderNum = 0;
            do {

                if (order.getIdentifier().size() == 0) {
                    order.addIdentifier()
                            .setSystem("https://fhir.uhs.nhs.uk/"+sysName +"/DiagnosticOrder")
                            .setValue(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-3-1"));
                }


                DiagnosticOrder.Item orderItem = new DiagnosticOrder.Item();
                orderItem.getCode().addCoding()
                        .setSystem(LabCodeSystem)
                        .setCode(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-4-1"))
                        .setDisplay(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-4-2"));
                order.getItem().add(orderItem);

                orderNum++;
                result = terserGet("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(0)/OBX-3-1");
            } while (result != null);

            System.out.println(FHIRparser.setPrettyPrint(true).encodeResourceToString(order));
            // Now post the referenced order
            outcome = client.update().resource(order)
                    .conditionalByUrl("DiagnosticOrder?identifier=" + order.getIdentifier().get(0).getSystem() + "%7C" + order.getIdentifier().get(0).getValue())
                    .execute();
            order.setId(outcome.getId());
            System.out.println(outcome.getId().getValue());


            // Second pass of HL7v2 message
            orderNum = 0;
            do {
                DiagnosticReport report = new DiagnosticReport();
                String text = "";

                List<IdDt> profiles = new ArrayList<IdDt>();
                profiles.add(new IdDt("https://fhir.nhs.uk/StructureDefinition/dds-report-1-0"));
                ResourceMetadataKeyEnum.PROFILES.put(report, profiles);

                report.addIdentifier()
                        .setSystem("https://fhir.uhs.nhs.uk/"+sysName +"/DiagnosticReport")
                        .setValue(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-3-1") + "-" + terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-4-1"));


                report.setStatus(DiagnosticReportStatusEnum.FINAL);

                report.setSubject(new ResourceReferenceDt(patient.getId().getValue()));

                report.getRequest().add(new ResourceReferenceDt(order.getId().getValue()));

                report.getCode().addCoding()
                        .setSystem(LabCodeSystem)
                        .setCode(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-4-1"))
                        .setDisplay(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-4-2"));


                try {
                    Date date;
                    date = fmt.parse(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-7-1"));
                    InstantDt instance = new InstantDt(date);
                    report.setIssued(instance);

                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                }


                Integer observationNo = 0;
                do {
                    result = terserGet("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-3-1");
                    if (result != null) {

                        if (!terserGet("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-2-1").equals("TX")) {
                            Observation observation = new Observation();

                            observation.addIdentifier()
                                    .setSystem("https://fhir.uhs.nhs.uk/" + sysName + "/Observation")
                                    .setValue(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBR-3-1") + "-" + terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-3-1"));

                            observation.setSubject(new ResourceReferenceDt(patient.getId().getValue()));
                            observation.getCode().addCoding()
                                    .setDisplay(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-3-2"))
                                    .setSystem(LabCodeSystem)
                                    .setCode(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-3-1"));

                            // Not converted unit and code correctly.

                            observation.setValue(
                                    new QuantityDt()
                                            .setValue(new BigDecimal(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-5-1")))
                                            .setUnit(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-6-1"))
                                            .setSystem("http://unitsofmeasure.org")
                                            .setCode(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-6-1")));

                            if (terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-7-1") != null) {
                                String[] highlow = terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-7-1").split("-");
                                BigDecimal low = new BigDecimal(highlow[0]);
                                BigDecimal high = new BigDecimal(highlow[1]);
                                observation.addReferenceRange()
                                        .setLow(new SimpleQuantityDt(low.floatValue(), "http://unitsofmeasure.org", terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-6-1")))
                                        .setHigh(new SimpleQuantityDt(high.floatValue(), "http://unitsofmeasure.org", terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-6-1")));
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
                            outcome = client.update().resource(observation)
                                    .conditionalByUrl("Observation?identifier=" + observation.getIdentifier().get(0).getSystem() + "%7C" + observation.getIdentifier().get(0).getValue())
                                    .execute();
                            observation.setId(outcome.getId());
                            System.out.println(outcome.getId().getValue());


                            report.getResult().add(new ResourceReferenceDt(observation.getId().getValue()));
                            //System.out.println("/PATIENT_RESULT/ORDER_OBSERVATION("+orderNum+")/OBSERVATION("+observationNo+")/OBX-3-1 = " +result);
                        } else {
                            // Handle text section here
                            if (terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-5-1") != null) {
                                text = text + terser.get("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(" + observationNo + ")/OBX-5-1") + "\r";
                            } else {
                                text = text + "\r";
                            }
                            report.getText().setDiv(text);
                        }
                    }

                    observationNo++;
                } while (result != null);

                System.out.println(FHIRparser.setPrettyPrint(true).encodeResourceToString(report));

                outcome = client.update().resource(report)
                        .conditionalByUrl("DiagnosticReport?identifier=" + report.getIdentifier().get(0).getSystem() + "%7C" + report.getIdentifier().get(0).getValue())
                        .execute();
                report.setId(outcome.getId());
                System.out.println(outcome.getId().getValue());

                orderNum++;
                result = terserGet("/PATIENT_RESULT/ORDER_OBSERVATION(" + orderNum + ")/OBSERVATION(0)/OBX-3-1");
            } while (result != null);

        }





}
