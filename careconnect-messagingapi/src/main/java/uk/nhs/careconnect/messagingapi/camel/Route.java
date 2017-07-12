package uk.nhs.careconnect.messagingapi.camel;


import ca.uhn.fhir.context.FhirContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.messagingapi.securityConfig.messasgeSplitter;

@Component
@PropertySource("classpath:careconnectmessagingapi.properties")
public class Route extends RouteBuilder {

	@Autowired
	protected Environment env;

	@Autowired
    protected FhirContext ctx;
	
    @Override
    public void configure() 
    {
     	MQProcessor mqProcessor = new MQProcessor(ctx);

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
                .split().method( messasgeSplitter.class,"extractResources")
                    .to("vm:tomq")
                .end()
                .endRest();
	
		from("vm:tomq")
                .routeId("FHIR MQ Send")
              //  .to("log:uk.nhs.careconnect.messagingapi?level=INFO&showHeaders=true&showHeaders=true")
                .to("activemq:HAPIBundle");


		from("activemq:HAPIBundle")
			.routeId("FHIR MQ Recv")
			.to("file:/FHIRServer?fileName=${id}-$simple{date:now:yyyyMMdd}.xml")
            .process(mqProcessor)
            .to("log:uk.nhs.careconnect.messagingapi?level=INFO&showHeaders=true&showHeaders=true");
			//.to("http://localhost:8080/FHIRServer/DSTU2?throwExceptionOnFailure=false&bridgeEndpoint=true");
			

    }
}
