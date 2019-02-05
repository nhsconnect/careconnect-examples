package uk.nhs.careconnect.ri.client.validation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import uk.nhs.careconnect.ri.client.validation.rest.HttpClient;
import uk.nhs.careconnect.ri.client.validation.rest.HttpClientBuilder;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;

public class ValidationApplication {

    private static final String SUCCESS = "Success";
    private static final String ERROR = "Error";
    private static final String FAILURE = "Failure";
    private HttpClient client;
//    private static final String SERVER_BASE = "http://127.0.0.1:8186/ccri-fhirserver/STU3";
    private static final String SERVER_BASE = "http://127.0.0.1:8181/STU3";

    public ValidationApplication() {
        client = new HttpClientBuilder().build();
    }

    public static void main(String[] args) {
        ValidationApplication application = new ValidationApplication();
        // We're connecting to a DSTU1 compliant server in this example
        FhirContext ctx = FhirContext.forDstu3();
        IGenericClient client = ctx.newRestfulGenericClient(SERVER_BASE);

        CapabilityStatement conf = client.capabilities().ofType(CapabilityStatement.class).execute();
        DataRepresent dataRepresent = new DataRepresent();
        application.validateServerInfo(conf, dataRepresent);

        application.validateResource(conf, dataRepresent);
        dataRepresent.represent();

//        SpringApplication.run(ValidationApplication.class, args);
    }

    private void validateResource(CapabilityStatement conf, DataRepresent dataRepresent) {

        List<ResourceStatus> resourceStatusList = conf.getRest().get(0).getResource()
                .stream()
                .map(this::checkResource)
                .collect(Collectors.toList());
        dataRepresent.setResourceStatusList(resourceStatusList);

    }

    private ResourceStatus checkResource(CapabilityStatement.CapabilityStatementRestResourceComponent resource) {
        String resourceType = resource.getType();
        ResourceStatus.ResourceStatusBuilder builder = ResourceStatus.builder().name(resourceType);
        String profileReferennce = resource.getProfile().getReference();
        if(isEmpty(profileReferennce)) {
            builder.profileStatus("Error");
        }
        else if(!profileReferennce.startsWith("https://fhir.hl7.org.uk/STU3/StructureDefinition/")) {
            builder.profileStatus("Warn");
        }
        else {
            builder.profileStatus("Success");
        }
        String uri = SERVER_BASE + "/" + resourceType;
        String wholeData = client.fetchData(uri, HttpMethod.GET, null);
        List<InteractionStatus> interactionStatus = resource.getInteraction()
                .stream()
                .map(interaction -> this.checkInteraction(resourceType, wholeData, interaction, resource.getSearchParam()))
                .collect(Collectors.toList());
        return builder.interactionStatusList(interactionStatus).build();
    }

    private InteractionStatus checkInteraction(String resourceType, String wholeData, CapabilityStatement.ResourceInteractionComponent resourceInteractionComponent, List<CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent> searchParam) {
        String uri = SERVER_BASE + "/" + resourceType;
        String interactionName = resourceInteractionComponent.getCode().getDisplay();

        if (CapabilityStatement.TypeRestfulInteraction.READ == resourceInteractionComponent.getCode()) {
            String resourceOutcome = fetchOutcome(wholeData);
            if(!SUCCESS.equals(resourceOutcome)) {
                return InteractionStatus.builder().interactionName(interactionName).status(FAILURE).build();
            }
            Integer size = fetchSize(wholeData);
            return prepareReadStatus(uri, interactionName, wholeData, size);
        }
        else if (CapabilityStatement.TypeRestfulInteraction.UPDATE == resourceInteractionComponent.getCode()) {

        }
        else if (CapabilityStatement.TypeRestfulInteraction.CREATE == resourceInteractionComponent.getCode()) {

        }
        else if (CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE == resourceInteractionComponent.getCode()) {
            return prepareSearchStatus(searchParam, uri, interactionName);
        }
        return InteractionStatus.builder().interactionName(interactionName).status("NotYetImplemented").build();
    }

    private InteractionStatus prepareReadStatus(String uri, String interactionName, String wholeData, Integer size) {
        if(size == 0) {
            //No Data to read.
            return InteractionStatus.builder().interactionName(interactionName).status(SUCCESS).data(wholeData).build();
        }
        else {
            String id = identifyId(wholeData);
            String data = client.fetchData(uri + "/" + id, HttpMethod.GET, null);
//            System.out.println("interactionValue:" + interactionName + ":::" + data);
            String serverId = identifyResourceId(data);
            if(serverId.equalsIgnoreCase(id)) {
                return InteractionStatus.builder().interactionName(interactionName).status(SUCCESS).data(data).build();
            }
            else {
                return InteractionStatus.builder().interactionName(interactionName).status(ERROR).data(data).build();
            }
        }
    }

    private InteractionStatus prepareSearchStatus(List<CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent> searchParam, String uri, String interactionName) {
        String searchParams = searchParam.stream().map(this::mapQueryParams).collect(Collectors.joining( "&"));
        System.out.println("URI:" + uri + "?" + searchParams);
        String data = client.fetchData(uri + "?" + searchParams, HttpMethod.GET, null);
//        System.out.println("interactionValue:" + interactionName + ":::" + data);
        List<InteractionStatus> subInteraction = searchParam.stream()
                .map(this::mapQueryParams)
                .map(singleSearchParam -> fetchSingleInteraction(uri, singleSearchParam))
                .collect(Collectors.toList());
        return InteractionStatus.builder()
                .interactionName(interactionName)
                .status(fetchOutcome(data))
                .data(data)
                .subInteraction(subInteraction)
                .build();
    }

    private InteractionStatus fetchSingleInteraction(String uri, String singleSearchParam) {
        String data = client.fetchData(uri + "?" + singleSearchParam, HttpMethod.GET, null);
        return InteractionStatus.builder().interactionName(singleSearchParam).status(fetchOutcome(data)).data(data).build();
    }

    private String mapQueryParams(CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent capabilityStatementRestResourceSearchParamComponent) {

//        String name = capabilityStatementRestResourceSearchParamComponent.getName();
        if(capabilityStatementRestResourceSearchParamComponent.getType() == Enumerations.SearchParamType.STRING) {
            return capabilityStatementRestResourceSearchParamComponent.getName() + "=123";
        }
        else if(capabilityStatementRestResourceSearchParamComponent.getType() == Enumerations.SearchParamType.NUMBER) {
            return capabilityStatementRestResourceSearchParamComponent.getName() + "=123";
        }
        else if(capabilityStatementRestResourceSearchParamComponent.getType() == Enumerations.SearchParamType.DATE) {
            return capabilityStatementRestResourceSearchParamComponent.getName() + "=2008-09-15T15:53:00";
        }
        else if(capabilityStatementRestResourceSearchParamComponent.getType() == Enumerations.SearchParamType.QUANTITY) {
            return capabilityStatementRestResourceSearchParamComponent.getName() + "=1";
        }
        else if(capabilityStatementRestResourceSearchParamComponent.getType() == Enumerations.SearchParamType.TOKEN) {
//            String[] tokens = capabilityStatementRestResourceSearchParamComponent.getDocumentation().split("|");
//            if (tokens.length > 0) {
//                return capabilityStatementRestResourceSearchParamComponent.getName() + "=" + tokens[0];
//            }
//            else {
                return capabilityStatementRestResourceSearchParamComponent.getName() + "=4624cf5b";
//            }
        }
        else if(capabilityStatementRestResourceSearchParamComponent.getType() == Enumerations.SearchParamType.REFERENCE) {
            return capabilityStatementRestResourceSearchParamComponent.getName() + "=123";
        }
        return capabilityStatementRestResourceSearchParamComponent.getName() + "=345";
    }

    private String identifyResourceId(String data) {
        JSONObject obj = new JSONObject(data);
        try {
            return obj.getString("id");
        }
        catch (JSONException exception) {
            return "";
        }
    }

    private Integer fetchSize(String data) {
//        System.out.println("Data:" + data);
        JSONObject obj = new JSONObject(data);
        return obj.getInt("total");
    }

    private String fetchOutcome(String data) {
        String resourceType = fetchResource(data);
        if("Bundle".equalsIgnoreCase(resourceType)) {
            return SUCCESS;
        }
        else if("OperationOutcome".equalsIgnoreCase(resourceType)) {
            return FAILURE;
        }
        return ERROR;
    }

    private String fetchResource(String data) {
//        System.out.println("Data:" + data);
        JSONObject obj = new JSONObject(data);
        return obj.getString("resourceType");
    }

    private String identifyId(String data) {
        JSONObject obj = new JSONObject(data);
        return obj.getJSONArray("entry").getJSONObject(0).getJSONObject("resource").getString("id");
    }

    private void validateServerInfo(CapabilityStatement conf, DataRepresent dataRepresent) {
        List<String> allowedFormats = Arrays.asList("application/fhir+xml", "application/fhir+json");
        String version = conf.getFhirVersion();
        String status = conf.getStatus().getDisplay();
        String publisher = conf.getPublisher();
        List<String> formatsList = conf.getFormat().stream().map(format -> format.getValue()).collect(Collectors.toList());
        String formats = conf.getFormat().stream().map(format -> format.getValue()).collect(Collectors.joining( ", "));

        assert "3.0.1".equalsIgnoreCase(version) : "Version not supported";
        assert "active".equalsIgnoreCase(status)  : "Status not active";
//        assert "NHS Digital".equalsIgnoreCase(publisher):  "Publisher not supported";
//        formats.stream().no

        // If wanna check all available formats are supported.
        assert allowedFormats.stream().allMatch(format -> formatsList.contains(format)) : "Some of the formats are not supported";

        // If wanna check all Server formats are supported as available formats.
//        assert formats.stream().allMatch(format -> allowedFormats.contains(format)) : "Some of the formats are not supported";

        ServerStatus serverStatus = ServerStatus.builder()
                .version(version)
                .versionAvailable(!isEmpty(version))
                .status(status)
                .isStatusActive("active".equalsIgnoreCase(status))
                .publisher(publisher)
                .isPublisherSpecified(!isEmpty(publisher) && !"Not provided".equalsIgnoreCase(publisher))
                .formats(formats)
                .isValidFormat(allowedFormats.stream().allMatch(format -> formatsList.contains(format)))
                .build();
        dataRepresent.setServerStatus(serverStatus);
    }

}

