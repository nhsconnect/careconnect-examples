package uk.nhs.careconnect.fhirserver.test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.client.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.hl7.fhir.instance.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.instance.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.instance.hapi.validation.IValidationSupport;
import org.hl7.fhir.instance.hapi.validation.ValidationSupportChain;
import org.hl7.fhir.instance.model.Bundle;
import uk.nhs.careconnect.core.dstu2.CareConnectSystem;
import uk.nhs.careconnect.validation.dstu2.CareConnectValidation;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ExampleServerIT {
    


    private static Server ourServer;
    private static String ourServerBase;
    private static int ourPort;

    @After
    public static void afterClass() throws Exception {
        ourServer.stop();
    }


    public final static FhirContext ctxFHIR = FhirContext.forDstu2Hl7Org();

    public final static org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ExampleServerIT.class);;

    public IGenericClient ourClient;

    @Before
    public IGenericClient getClient() throws Exception {
		/*
		 * This runs under maven, and I'm not sure how else to figure out the target directory from code..
		 */


        ourLog.info("Lets Do this!");

        String path = ExampleServerIT.class.getClassLoader().getResource("application.properties").getPath();
        ourLog.info("Properties Path = "+path);
        path = new File(path).getParent();
        path = new File(path).getParent();
        path = new File(path).getParent();

        ourLog.info("Project base path is: {}", path);

        ourPort = RandomServerPortProvider.findFreePort();
        ourServer = new Server(ourPort);

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setDescriptor(path + "/src/main/webapp/WEB-INF/web.xml");
        webAppContext.setResourceBase(path + "/target/careconnect-ris");
        webAppContext.setParentLoaderPriority(true);

        ourServer.setHandler(webAppContext);
        ourServer.start();

        ctxFHIR.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
        ctxFHIR.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
        ourServerBase = "http://localhost:" + ourPort + "/DSTU2";
        ourClient = ctxFHIR.newRestfulGenericClient(ourServerBase);
        ourClient.registerInterceptor(new LoggingInterceptor(true));
        return ourClient;
    }



    Bundle resource;

    @Given("^I search for a Patient by NHS Number (\\w+)$")
    public void i_search_for_a_Patient_by_NHS_Number(String nhsNumber) throws Throwable {

        assertNotNull(ourClient);
        resource = ourClient
                .search()
                .byUrl("Patient?identifier="+ CareConnectSystem.NHSNumber+"|" + nhsNumber)
                .returnBundle(Bundle.class)
                .execute();
    }


    @Given("^I search for a Organisation by ODS Code (\\w+)$")
    public void i_search_for_a_Organisation_by_ODS_Code_R_A(String ODSCode) throws Throwable {

        resource = ourClient
                .search()
                .byUrl("Organization?identifier="+ CareConnectSystem.ODSOrganisationCode+"|" + ODSCode)
                .returnBundle(Bundle.class)
                .execute();
    }

    @Given("^I search for a Practitioner by SDS User Id (\\w+)$")
    public void i_search_for_a_Practitioner_by_SDS_User_Id_G(String SDSUserId) throws Throwable {


        resource = ourClient
                .search()
                .byUrl("Practitioner?identifier="+ CareConnectSystem.SDSUserId+"|" + SDSUserId)
                .returnBundle(Bundle.class)
                .execute();
    }

    @Then("^the result should be a valid FHIR Bundle$")
    public void the_result_should_be_a_valid_FHIR_Bundle() throws Throwable {

        assertNotNull(resource);
    }

    @Then("^the results should be valid CareConnect Profiles$")
    public void the_results_should_be_valid_CareConnect_Profiles() throws Throwable {

        //FhirContext ctxFHIR = FhirContext.forDstu2Hl7Org();
        FhirValidator validator = ctxFHIR.newValidator();
        FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
        validator.registerValidatorModule(instanceValidator);

        IValidationSupport valSupport = new CareConnectValidation();
        ValidationSupportChain support = new ValidationSupportChain(new DefaultProfileValidationSupport(), valSupport);
        instanceValidator.setValidationSupport(support);

        ValidationResult result = validator.validateWithResult(resource);

        // Show the issues
        // Colour values https://github.com/yonchu/shell-color-pallet/blob/master/color16
        for (SingleValidationMessage next : result.getMessages()) {
            switch (next.getSeverity())
            {
                case ERROR:
                    fail("FHIR Validation ERROR - "+ next.getMessage());
                    break;
                case WARNING:
                    //fail("FHIR Validation WARNING - "+ next.getMessage());
                    System.out.println(  (char)27 + "[34mWARNING" + (char)27 + "[0m" + " - " +  next.getLocationString() + " - " + next.getMessage());
                    break;
                case INFORMATION:
                    System.out.println( (char)27 + "[34mINFORMATION" + (char)27 + "[0m" + " - " +  next.getLocationString() + " - " + next.getMessage());
                    break;
                default:
                    System.out.println(" Next issue " + next.getSeverity() + " - " + next.getLocationString() + " - " + next.getMessage());
            }
        }
    }




}
