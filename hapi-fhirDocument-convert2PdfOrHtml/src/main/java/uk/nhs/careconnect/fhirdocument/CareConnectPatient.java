package uk.nhs.careconnect.fhirdocument;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class CareConnectPatient  {
    private Patient patient;

    CareConnectPatient(Patient patient) {
        this.patient= patient;
    }
    public String getNHSNumber() {
        for (Identifier identifier : this.patient.getIdentifier()) {
            if (identifier.getSystem().equals(CareConnectSystem.NHSNumber)) {
                return identifier.getValue();
            }
        }
        return null;
    }
    public String getName() {
        if (this.patient.getNameFirstRep() == null) return "";
        return this.patient.getNameFirstRep().getNameAsSingleString();
    }
    public String getDateOfBirth() {
        if (this.patient.getBirthDate() == null) return "";
        DateFormat df = new SimpleDateFormat("dd MMM yyyy");
        return df.format(this.patient.getBirthDate());
    }
}

