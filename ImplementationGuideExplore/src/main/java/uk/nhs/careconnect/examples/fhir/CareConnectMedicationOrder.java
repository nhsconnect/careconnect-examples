package uk.nhs.careconnect.examples.fhir;

import org.hl7.fhir.instance.model.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kevinmayfield on 26/05/2017.
 */
public class CareConnectMedicationOrder {


    public static MedicationOrder buildCareConnectMedicationOrder(Patient patient, Practitioner prescriber) {

        //http://dmd.medicines.org.uk/DesktopDefault.aspx?VMP=10097211000001102&toc=nofloat

        MedicationOrder prescription = new MedicationOrder();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        prescription.setMeta(new Meta().addProfile(CareConnectSystem.ProfileMedicationOrder));


        Extension supplyType = new Extension();
        supplyType.setUrl(CareConnectSystem.ExtUrlMedicationSupplyType);
        CodeableConcept supplyCode = new CodeableConcept();
        supplyCode.addCoding()
                .setCode("394823007")
                .setSystem(CareConnectSystem.SNOMEDCT)
                .setDisplay("NHS Prescription");
        supplyType.setValue(supplyCode);
        prescription.addExtension(supplyType);


        prescription.addIdentifier()
                .setSystem("https://fhir.bristolccg.nhs.uk/DW/MedicationOrder")
                .setValue("6bf79485-cee5-4a20-8e4a-4bf13aba33e6");
        prescription.setPatient(new Reference(patient.getId()));
        prescription.getPatient().setDisplay(patient.getName().get(0).getText());

        Date issueDate;
        try {
            issueDate = dateFormat.parse("2017-05-25");
            prescription.setDateWritten(issueDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        prescription.setStatus(MedicationOrder.MedicationOrderStatus.ACTIVE);

        prescription.setPrescriber(new Reference(prescriber.getId()));
        prescription.getPrescriber().setDisplay(prescriber.getName().getText());

        prescription.setNote("Please explain to Bernie how to use injector.");

        CodeableConcept drugCode = new CodeableConcept();
        drugCode.addCoding()
                .setCode("10097211000001102")
                .setSystem(CareConnectSystem.SNOMEDCT)
                .setDisplay("Insulin glulisine 100units/ml solution for injection 3ml pre-filled disposable devices");

        prescription.setMedication(drugCode);


        MedicationOrder.MedicationOrderDosageInstructionComponent dosage = prescription.addDosageInstruction();
        dosage.setText("Three times a day");
        CodeableConcept additionalIns = new CodeableConcept();
        additionalIns.addCoding()
                .setCode("1521000175104")
                .setSystem(CareConnectSystem.SNOMEDCT)
                .setDisplay("After dinner");
        dosage.setAdditionalInstructions(additionalIns);


        Timing timing = new Timing();
        CodeableConcept timeCode = new CodeableConcept();
        timeCode.addCoding().setCode("TID").setSystem("http://hl7.org/fhir/v3/GTSAbbreviation");
        timing.setCode(timeCode);
        dosage.setTiming(timing);

        MedicationOrder.MedicationOrderDispenseRequestComponent dispenseRequest = new MedicationOrder.MedicationOrderDispenseRequestComponent();
        dispenseRequest.setNumberOfRepeatsAllowed(3);

        prescription.setDispenseRequest(dispenseRequest);



        return prescription;
    }
}
