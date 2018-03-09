package uk.nhs.careconnect.fhirStarter.providers;

import ca.uhn.fhir.context.FhirContext;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;

import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.bson.types.ObjectId;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.careconnect.fhirStarter.dao.IPatient;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class PatientProvider implements IResourceProvider {

    @Autowired
    FhirContext ctx;

    @Autowired
    IPatient patientDao;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Patient.class;
    }

    @Create
    public MethodOutcome createPatient(HttpServletRequest theRequest, @ResourceParam Patient patient) {

        log.debug("Create Patient Provider called");

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            Patient mongoPatient = patientDao.create(ctx, patient);
            log.info(mongoPatient.getIdElement().toString());
            method.setId(mongoPatient.getIdElement());
            method.setResource(mongoPatient);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

        log.debug("called create Patient method");

        return method;
    }

    /*
    @Read
    public Patient readPatient(HttpServletRequest request, @IdParam IdType internalId) {

        Patient patient = patientDao.read(ctx,internalId);

        return patient;
    }
*/

    /*
    @Search
    public List<Resource> searchPatient(HttpServletRequest request,
                                        @OptionalParam(name= Patient.SP_BIRTHDATE) DateRangeParam birthDate,
                                        @OptionalParam(name = Patient.SP_FAMILY) StringParam familyName,
                                        @OptionalParam(name= Patient.SP_GENDER) StringParam gender ,
                                        @OptionalParam(name= Patient.SP_GIVEN) StringParam givenName ,
                                        @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
                                        @OptionalParam(name= Patient.SP_NAME) StringParam name
            , @OptionalParam(name = Patient.SP_RES_ID) TokenParam resid

    ) {
        List<Resource> results = patientDao.search(ctx,birthDate,familyName,gender,givenName,identifier,name);

        return results;
    }
    */

}
