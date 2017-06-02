package uk.nhs.careconnect.examples.fhir;

import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.resource.Medication;
import ca.uhn.fhir.model.primitive.IdDt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevinmayfield on 26/05/2017.
 */
public class ExampleMedication {
    public static Medication buildCareConnectFHIRMedication() {

        //http://dmd.medicines.org.uk/DesktopDefault.aspx?VMP=10097211000001102&toc=nofloat

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Medication medication = new Medication();

        List<IdDt> profiles = new ArrayList<IdDt>();
        profiles.add(new IdDt(CareConnectSystem.ProfileMedication));
        ResourceMetadataKeyEnum.PROFILES.put(medication, profiles);

        CodeableConceptDt drugCode = new CodeableConceptDt();
        drugCode.addCoding()
                .setCode("10097211000001102")
                .setSystem(CareConnectSystem.SNOMEDCT)
                .setDisplay("Insulin glulisine 100units/ml solution for injection 3ml pre-filled disposable devices");

        medication.setCode(drugCode);
        /* drug is generic
        medication.getManufacturer().setDisplay("Lexon (UK) Ltd");
        */
        Medication.Product medicationProduct = new Medication.Product();
        medicationProduct.getForm().addCoding()
                .setDisplay("Solution for injection")
                .setSystem(CareConnectSystem.SNOMEDCT)
                .setCode("385219001");

        medication.setProduct(medicationProduct);

        return medication;
    }

}
