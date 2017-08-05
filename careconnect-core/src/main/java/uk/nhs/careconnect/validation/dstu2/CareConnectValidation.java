package uk.nhs.careconnect.validation.dstu2;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.client.ServerValidationModeEnum;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import org.hl7.fhir.instance.hapi.validation.IValidationSupport;
import org.hl7.fhir.instance.model.ValueSet;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class CareConnectValidation implements IValidationSupport {

    private FhirContext myCtx = FhirContext.forDstu2Hl7Org();

    private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(CareConnectValidation.class);
    @Override
    public ValueSet.ValueSetExpansionComponent expandValueSet(FhirContext theContext, ValueSet.ConceptSetComponent theInclude) {

        return null;
    }

    @Override
    public ValueSet fetchCodeSystem(FhirContext theContext, String theSystem) {
        System.out.println("CareConnectValidator-"+theSystem);
        return null;
    }

    @Override
    public <T extends IBaseResource> T fetchResource(FhirContext theContext, Class<T> theClass, String theUri) {

        if (theUri.startsWith("https://fhir-test.hl7.org.uk/") || theUri.startsWith("https://fhir.hl7.org.uk/")) {


            if (theUri.contains("/StructureDefinition/") && !theUri.contains("/StructureDefinition/Ext")){
                System.out.println("CareConnectValidator DISABLED due to slicing issue. fetch Resource - " + theUri);

                return null;
            }

         //   System.out.println("CareConnectValidator fetch Resource-" + theUri);
            String resName = myCtx.getResourceDefinition(theClass).getName();
            ourLog.info("Attempting to fetch {} at URL: {}", resName, theUri);

            myCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
            IGenericClient client = myCtx.newRestfulGenericClient("https://fhir-test.hl7.org.uk");

            T result = null;
            try {
                result = client.read(theClass, theUri);
            } catch (BaseServerResponseException e) {
                ourLog.error("FAILURE: Received HTTP " + e.getStatusCode() + ": " + e.getMessage());
            }
            ourLog.info("Successfully loaded resource");
            return result;

        }
        else {
            return null;
        }
    }

    @Override
    public boolean isCodeSystemSupported(FhirContext theContext, String theSystem) {
        System.out.println("CareConnectValidator-"+theSystem);
        return false;
    }

    @Override
    public CodeValidationResult validateCode(FhirContext theContext, String theCodeSystem, String theCode, String theDisplay) {
        System.out.println("CareConnectValidator-"+theDisplay);
        return null;
    }
}
