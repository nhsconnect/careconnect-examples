package uk.nhs.careconnect.nosql.dao.transform;

import uk.nhs.careconnect.nosql.entities.Identifier;
import uk.nhs.careconnect.nosql.entities.Name;
import uk.nhs.careconnect.nosql.entities.PatientEntity;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Patient;
import org.springframework.stereotype.Component;
import org.apache.commons.collections4.Transformer;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;

@Component
public class PatientEntityToFHIRPatient  implements Transformer<PatientEntity, Patient> {
    @Override
    public Patient transform(PatientEntity patientEntity) {
        final Patient patient = new Patient();

        Meta meta = new Meta().addProfile(CareConnectProfile.Patient_1);

        patient.setMeta(meta);

        for (Identifier identifier : patientEntity.getIdentifiers()) {
            patient.addIdentifier()
                    .setSystem(identifier.getSystem())
                    .setValue(identifier.getValue());
        }
        for (Name name : patientEntity.getNames() ) {
            patient.addName().setFamily(name.getFamilyName())
                    .addGiven(name.getGivenName());
        }
        if (patientEntity.getDateOfBirth() != null) {
            patient.setBirthDate(patientEntity.getDateOfBirth());
        }
        patient.setId(patientEntity.getId().toString());

        return patient;
    }
}
