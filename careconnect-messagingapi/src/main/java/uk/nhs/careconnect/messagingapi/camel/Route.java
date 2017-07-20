package uk.nhs.careconnect.messagingapi.camel;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.jms.*;

@Component
@PropertySource("classpath:careconnectmessagingapi.properties")
public class Route extends RouteBuilder {

	@Autowired
	protected Environment env;

	@Autowired
    protected FhirContext ctx;

	@Autowired
    protected IGenericClient fhirClient;

    @Autowired
    protected ActiveMQConnectionFactory activeMQConnectionFactory;

    Session session;

    MessageProducer producer;
	
    @Override
    public void configure() 
    {

        setupActiveMQ();

     	MQProcessor mqProcessor = new MQProcessor(ctx, fhirClient,session,producer);
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

    private void setupActiveMQ() {
        try {
            // Create a ConnectionFactory
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

            // Create a Connection
            Connection connection = connectionFactory.createConnection();
            connection.start();

            // Create a Session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the destination (Topic or Queue)
            Destination destination = session.createQueue("Elastic.Queue");

            // Create a MessageProducer from the Session to the Topic or Queue
            producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            // Create a messages

           // String text =JSONparser.setPrettyPrint(true).encodeResourceToString(audit);
           // TextMessage message = session.createTextMessage(text);

            // Tell the producer to send the message
           // System.out.println("Sent message: "+ message.hashCode() + " : " + Thread.currentThread().getName());
           // producer.send(message);

            // Clean up
          //  session.close();
          //  connection.close();
        }
        catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }



}
