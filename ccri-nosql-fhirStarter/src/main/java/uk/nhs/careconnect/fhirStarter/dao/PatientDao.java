package uk.nhs.careconnect.fhirStarter.dao;

import ca.uhn.fhir.context.FhirContext;

import org.hl7.fhir.dstu3.model.*;
import uk.nhs.careconnect.fhirStarter.dao.transform.PatientEntityToFHIRPatient;
import uk.nhs.careconnect.fhirStarter.entities.Name;
import uk.nhs.careconnect.fhirStarter.entities.PatientEntity;
import org.bson.types.ObjectId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;

import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhirStarter.entities.Telecom;

@Repository
public class PatientDao implements IPatient {

    @Autowired
    MongoOperations mongo;

    @Autowired
    PatientEntityToFHIRPatient patientEntityToFHIRPatient;

    @Override
    public Patient create(FhirContext ctx, Patient patient) {


        PatientEntity patientEntity = new PatientEntity();

        for (Identifier identifier : patient.getIdentifier()) {
            uk.nhs.careconnect.fhirStarter.entities.Identifier identifierE = new uk.nhs.careconnect.fhirStarter.entities.Identifier();
            identifierE.setSystem(identifier.getSystem());
            identifierE.setValue(identifier.getValue().replaceAll(" ",""));

            patientEntity.getIdentifiers().add(identifierE);
        }
        for (HumanName name : patient.getName()) {
            Name nameE = new Name();
            nameE.setFamilyName(name.getFamily());
            nameE.setGivenName(name.getGivenAsSingleString());
            if (name.hasPrefix()) {
                nameE.setPrefix(name.getPrefix().get(0).getValue());
            }
            if (name.hasUse()) {
                nameE.setNameUse(name.getUse());
            }
            patientEntity.getNames().add(nameE);
        }
        if (patient.hasBirthDate()) {
            patientEntity.setDateOfBirth(patient.getBirthDate());
        }
        if (patient.hasGender()) {
            patientEntity.setGender(patient.getGender());
        }
        for (ContactPoint contactPoint : patient.getTelecom()) {
            Telecom telecom = new Telecom();
            telecom.setValue(contactPoint.getValue());
            if (contactPoint.hasSystem()) {
                telecom.setSystem(contactPoint.getSystem());
            }
            patientEntity.getTelecoms().add(telecom);
        }
        for (Address address : patient.getAddress()) {
            uk.nhs.careconnect.fhirStarter.entities.Address addressEntity = new uk.nhs.careconnect.fhirStarter.entities.Address();

            for (StringType line : address.getLine()) {
                addressEntity.getLines().add(line.toString());
            }

            if (address.hasCity()) {
                addressEntity.setCity(address.getCity());
            }
            if (address.hasPostalCode()) {
                addressEntity.setPostcode(address.getPostalCode());
            }
            if (address.hasUse()) {
                addressEntity.setUse(address.getUse());
            }

            patientEntity.getAddresses().add(addressEntity);
        }
        mongo.save(patientEntity);

        ObjectId bundleId = patientEntity.getId();

        return patientEntityToFHIRPatient.transform(patientEntity);
    }


}
