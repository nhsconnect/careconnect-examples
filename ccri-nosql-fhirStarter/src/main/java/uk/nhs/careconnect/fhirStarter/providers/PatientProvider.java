package uk.nhs.careconnect.fhirStarter.providers;

import ca.uhn.fhir.context.FhirContext;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;

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
}
