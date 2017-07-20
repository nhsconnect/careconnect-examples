package uk.nhs.careconnect.messagingapi.camel;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:careconnectmessagingapi.properties")
public class Route extends RouteBuilder {

	@Autowired
	protected Environment env;

	@Autowired
    protected FhirContext ctx;

	@Autowired
    protected IGenericClient fhirClient;
	
    @Override
    public void configure() 
    {
     	MQProcessor mqProcessor = new MQProcessor(ctx, fhirClient);
        ConvertToJSON convertToJSON = new ConvertToJSON(ctx);

	    restConfiguration()
	    	.component("servlet")
	    	.bindingMode(RestBindingMode.off)
	    	.contextPath("CareConnectMessagingAPI")
	    	.port(8080)
	    	.dataFormatProperty("prettyPrint","true");
	
		
	    rest("/")
			.description("CareConnect Messaging API")

            .post("/Bundle")
                .description("Receive FHIR Message Bundle")
                .route()
                .routeId("Bundle POST")
                //.split().method( messasgeSplitter.class,"extractResources")
                //
                //.end()
                // Send message to queue, should now post an ok after checking the message is valid
                .to("vm:tomq")
                .endRest();
	
		from("vm:tomq")
                .routeId("FHIR MQ Send")
                // This sends out the bundle ASync
              .wireTap("activemq:HAPIBundle")
        .end();


		from("activemq:HAPIBundle")
			.routeId("FHIR MQ Process")
			.to("file:/FHIRServer/Before?fileName=${id}-$simple{date:now:yyyyMMdd}.xml")
            .process(mqProcessor)
            .to("log:uk.nhs.careconnect.messagingapi?showAll=true&multiline=true&level=INFO")
            .to("file:/FHIRServer/After?fileName=${id}-$simple{date:now:yyyyMMdd}.xml");

        from("activemq:Elastic.Queue")
                .routeId("Elastic Send from MQ")
                .process(convertToJSON)
                .to("http://127.0.0.1:9200?throwExceptionOnFailure=false&bridgeEndpoint=true")
                .to("file:/FHIRServer/Elastic?fileName=${id}-$simple{date:now:yyyyMMdd}.xml");

    }
}
