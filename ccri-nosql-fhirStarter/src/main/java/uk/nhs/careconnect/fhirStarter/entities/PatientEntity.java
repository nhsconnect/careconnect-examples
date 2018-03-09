
package uk.nhs.careconnect.fhirStarter.entities;

import org.bson.types.ObjectId;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.codesystems.AdministrativeGender;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;


@Document(collection = "idxPatient")
public class PatientEntity  {

    @Id
    private ObjectId id;

    private Date dateOfBirth;

    private Enumerations.AdministrativeGender gender;

    private Collection<Identifier> identifiers  = new LinkedHashSet<>();

    private Collection<Telecom> telecoms = new LinkedHashSet<>();

    private Collection<Name> names = new LinkedHashSet<>();

    private Collection<Address> addresses = new LinkedHashSet<>();

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Enumerations.AdministrativeGender getGender() {
        return gender;
    }

    public void setGender(Enumerations.AdministrativeGender gender) {
        this.gender = gender;
    }

    public Collection<Identifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Collection<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    public Collection<Telecom> getTelecoms() {
        return telecoms;
    }

    public void setTelecoms(Collection<Telecom> telecoms) {
        this.telecoms = telecoms;
    }

    public Collection<Name> getNames() {
        return names;
    }

    public void setNames(Collection<Name> names) {
        this.names = names;
    }

    public Collection<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(Collection<Address> addresses) {
        this.addresses = addresses;
    }
}
