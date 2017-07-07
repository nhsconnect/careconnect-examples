package uk.nhs.careconnect.examples.App;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.DiagnosticReport;
import ca.uhn.fhir.model.dstu2.valueset.DiagnosticReportStatusEnum;
import ca.uhn.fhir.model.primitive.InstantDt;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.GenericParser;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Terser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootApplication
public class UHSDiagnotics implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(UHSDiagnotics.class, args);
	}

	Terser terser = null;

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

		// HAPI Terser example https://sourceforge.net/p/hl7api/code/764/tree/releases/2.0/hapi-examples/src/main/java/ca/uhn/hl7v2/examples/ExampleUseTerser.java#l72

		// Ringholm hl7v2 to fhir mapping http://www.ringholm.com/docs/04350_mapping_HL7v2_FHIR.htm

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


		Parser parser = new GenericParser();
		parser.getParserConfiguration().setDefaultObx2Type("ST");
		Message hapiMsg = parser.parse(msg);

		terser = new Terser(hapiMsg);


		String sendingApplication = terser.get("/.MSH-3-1");
		System.out.println(sendingApplication);

		FhirContext ctxFHIR = FhirContext.forDstu2();
		IParser FHIRparser = ctxFHIR.newXmlParser();

		SimpleDateFormat  fmt = new SimpleDateFormat("yyyyMMddHHmmss");



		// This is to base HAPI server not the CareConnectAPI
		String serverBase = "http://127.0.0.1:8080/FHIRServer/DSTU2/";

		IGenericClient client = ctxFHIR.newRestfulGenericClient(serverBase);

		DiagnosticReport report = new DiagnosticReport();
		report.addIdentifier()
				.setSystem("https://fhir.uhs.nhs.uk/HICSS/DiagnosticReport")
				.setValue(terser.get("/.OBR-3-1"));
		report.setStatus(DiagnosticReportStatusEnum.FINAL);

		System.out.println("Time: "+terser.get("/.OBR-7-1"));
		try {
			Date date;
			date = fmt.parse(terser.get("/.OBR-7-1"));
			InstantDt instance = new InstantDt(date);
			report.setIssued(instance);

		} catch (Exception e1) {
			// TODO Auto-generated catch block
		}

		Integer orderNum=0;
		String result =null;
		do
		{
			Integer observationNo=0;
			do {
				result = terserGet("/PATIENT_RESULT/ORDER_OBSERVATION("+orderNum+")/OBSERVATION("+observationNo+")/OBX-3-1");
				if (result !=null) {
					System.out.println("/PATIENT_RESULT/ORDER_OBSERVATION("+orderNum+")/OBSERVATION("+observationNo+")/OBX-3-1 = " +result);
				}

				observationNo++;
			} while (result != null );
			orderNum++;
			result = terserGet("/PATIENT_RESULT/ORDER_OBSERVATION("+orderNum+")/OBSERVATION(0)/OBX-3-1");
		} while (result != null );


		//System.out.println("ORD OBSERVATION(0): "+terserGet("/PATIENT_RESULT/ORDER_OBSERVATION(0)/OBSERVATION(0)/OBX-3-1"));
		//System.out.println("ORD OBSERVATION(1): "+terserGet("/PATIENT_RESULT/ORDER_OBSERVATION(1)/OBSERVATION(0)/OBX-3-1"));
		//System.out.println("ORD OBSERVATION(2): "+terserGet("/PATIENT_RESULT/ORDER_OBSERVATION(2)/OBSERVATION(0)/OBX-3-1"));


		//System.out.println("OBSERVATION(2): "+terserGet("/.OBSERVATION(2)/OBX-3-1"));
		System.out.println(FHIRparser.setPrettyPrint(true).encodeResourceToString(report));

    }



}
